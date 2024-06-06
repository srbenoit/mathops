package dev.mathops.web.site.admin.bookstore;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.rawlogic.RawEtextKeyLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawrecord.RawEtextKey;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * The bookstore site home page.
 */
enum PageCheckKey {
    ;

    /**
     * Generates the page that tests the computer for compatibility with the website.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void checkEtextKey(final WebViewData data, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = BookstorePage.startBookstorePage(data, site, session);

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final Cache cache = data.getCache();
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    final String msg = Res.get(Res.KEY_NOT_FOUND);
                    BookstorePage.emitKeyForm(htm, key, msg);
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");

                    if (keyModel.activeDt == null) {
                        final String msg = Res.get(Res.KEY_NOT_ACTIVE);
                        htm.sDiv("center");
                        htm.addln("<strong>", msg, "</strong>");
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);
                        final String activeDtStr = TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt);

                        if (stetext == null) {
                            final String msg = Res.fmt(Res.KEY_ACTIVE_NO_USER, activeDtStr);
                            htm.sDiv("center");
                            htm.addln("<strong> ", msg, "</strong>");
                        } else {
                            final StudentData studentData = new StudentData(data.getCache(), data.getSystemData(),
                                    stetext.stuId, ELiveRefreshes.NONE);
                            final RawStudent stu = studentData.getStudentRecord();

                            htm.sDiv("center");
                            htm.addln("<strong>");
                            if (stu == null) {
                                final String msg = Res.fmt(Res.KEY_ACTIVE_NO_STU, activeDtStr, stetext.stuId);
                                htm.addln(msg);
                                htm.addln("</strong>");
                                htm.eDiv(); // center
                            } else {
                                final String msg = Res.fmt(Res.KEY_ACTIVE_STU, activeDtStr, stu.firstName, stu.lastName,
                                        stetext.stuId);
                                htm.addln(msg);
                                htm.addln("</strong>");
                                htm.eDiv(); // center

                                emitWorkSincePurchase(studentData, stu, keyModel.activeDt, htm);
                            }
                            htm.div("gap2");

                            htm.sDiv("center");
                            emitDeactiveForm(htm, key);
                        }
                    }
                    htm.eDiv(); // center
                }
            }

            Page.endOrdinaryPage(data, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        }
    }

    /**
     * Tests whether the student has taken exams since purchasing the e-text (used when a refund is requested).
     *
     * @param studentData   the student data object
     * @param whenActivated the date/time the key was activated
     * @param htm           the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitWorkSincePurchase(final StudentData studentData,
                                              final ChronoLocalDateTime<LocalDate> whenActivated,
                                              final HtmlBuilder htm) throws SQLException {

        final List<RawSthomework> homeworks = studentData.getStudentHomework();

        final Iterator<RawSthomework> hi = homeworks.iterator();
        while (hi.hasNext()) {
            final RawSthomework rec = hi.next();
            final String passed = rec.passed;
            if ("Y".equals(passed) || "N".equals(passed)) {
                if (rec.getStartDateTime() != null && rec.getStartDateTime().isBefore(whenActivated)) {
                    hi.remove();
                }
                if (rec.course != null && rec.course.startsWith("M 100")) {
                    hi.remove();
                }
            } else {
                hi.remove();
            }
        }
        homeworks.sort(new RawSthomework.FinishDateTimeComparator());

        final List<RawStexam> exams = studentData.getStudentExams();

        final Iterator<RawStexam> ei = exams.iterator();
        while (ei.hasNext()) {
            final RawStexam rec = ei.next();
            final String passed = rec.passed;

            if ("Y".equals(passed) || "N".equals(passed)) {
                if (rec.getStartDateTime() != null && rec.getStartDateTime().isBefore(whenActivated)) {
                    ei.remove();
                }
                if (rec.course != null && rec.course.startsWith("M 100")) {
                    ei.remove();
                }
            } else {
                ei.remove();
            }
        }
        exams.sort(new RawStexam.FinishDateTimeComparator());

        htm.sDiv("indent22");
        htm.hr().div("gap2");

        final int numExams = exams.size();

        if (homeworks.isEmpty()) {
            if (exams.isEmpty()) {
                htm.addln("Student has not completed any homework assignments or exams ",
                        "since purchasing the e-text");
            } else {
                htm.addln(
                        "Student has completed <strong>" + numExams
                                + " exams</strong> since purchasing the e-text, ",
                        "submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawStexam row : exams) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    if (fin == null) {
                        htm.addln("<li>N/A</li>");
                    } else {
                        final String finStr = TemporalUtils.FMT_MDY_AT_HM_A.format(fin);
                        htm.addln("<li>", finStr, "</li>");
                    }
                }
                htm.addln("</ul>");
            }
        } else {
            final int numHw = homeworks.size();
            final String numHwStr = Integer.toString(numHw);

            if (exams.isEmpty()) {
                htm.addln("Student has completed <strong>", numHwStr,
                        " homework sets</strong> since purchasing the e-text, submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawSthomework row : homeworks) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    if (fin == null) {
                        htm.addln("<li>N/A</li>");
                    } else {
                        final String finStr = TemporalUtils.FMT_MDY_AT_HM_A.format(fin);
                        htm.addln("<li>", finStr, "</li>");
                    }
                }
            } else {
                final String numExamsStr = Integer.toString(numExams);

                htm.addln("Student has completed <strong>", numHwStr,
                        " homework sets</strong> and <strong>", numExamsStr,
                        " exams</strong> since purchasing the e-text, submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawSthomework row : homeworks) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    if (fin == null) {
                        htm.addln("<li>Homework: N/A</li>");
                    } else {
                        final String finStr = TemporalUtils.FMT_MDY_AT_HM_A.format(fin);
                        htm.addln("<li>Homework: ", finStr, "</li>");
                    }
                }
                for (final RawStexam row : exams) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    if (fin == null) {
                        htm.addln("<li>Homework: N/A</li>");
                    } else {
                        final String finStr = TemporalUtils.FMT_MDY_AT_HM_A.format(fin);
                        htm.addln("<li>Exam: ", finStr, "</li>");
                    }
                }
            }
            htm.addln("</ul>");
        }

        htm.div("gap2").eDiv();
    }

    /**
     * Emits the form used to deactivate an e-text key.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param key the initial value to load in for the key
     */
    private static void emitDeactiveForm(final HtmlBuilder htm, final String key) {

        htm.sDiv("indent22");
        htm.hr().div("gap2");
        htm.addln("<form action='deactivate_etext_key.html' method='post'>");
        htm.sDiv("center");
        htm.addln(Res.get(Res.DEACTIVATE_PROMPT));
        htm.div("gap2");
        htm.addln(" <input type='hidden' name='key' value='", key, "'/>");
        final String lbl = Res.get(Res.DEACTIVATE_BTN_LBL);
        htm.addln(" <input class='btn' type='submit' value='", lbl, "'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.div("gap2").hr().eDiv();
    }
}
