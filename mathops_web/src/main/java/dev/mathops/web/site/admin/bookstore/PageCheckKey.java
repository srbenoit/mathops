package dev.mathops.web.site.admin.bookstore;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawEtextKeyLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
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
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void checkEtextKey(final Cache cache, final AdminSite site,
                              final ServletRequest req, final HttpServletResponse resp,
                              final ImmutableSessionInfo session) throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = BookstorePage.startBookstorePage(cache, site, session);

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    BookstorePage.emitKeyForm(htm, key, Res.get(Res.KEY_NOT_FOUND));
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");

                    if (keyModel.activeDt == null) {
                        htm.sDiv("center");
                        htm.addln("<strong>", Res.get(Res.KEY_NOT_ACTIVE),
                                "</strong>");
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);

                        if (stetext == null) {
                            htm.sDiv("center");
                            htm.addln("<strong> ",
                                    Res.fmt(Res.KEY_ACTIVE_NO_USER,
                                            TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt)),
                                    "</strong>");
                        } else {
                            final RawStudent stu =
                                    RawStudentLogic.query(cache, stetext.stuId, true);

                            htm.sDiv("center");
                            htm.addln("<strong>");
                            if (stu == null) {
                                htm.addln(Res.fmt(Res.KEY_ACTIVE_NO_STU,
                                        TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt),
                                        stetext.stuId));
                                htm.addln("</strong>");
                                htm.eDiv(); // center
                            } else {
                                htm.addln(Res.fmt(Res.KEY_ACTIVE_STU,
                                        TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt),
                                        stu.firstName, stu.lastName, stetext.stuId));
                                htm.addln("</strong>");
                                htm.eDiv(); // center

                                emitWorkSincePurchase(cache, stu, keyModel.activeDt, htm);
                            }
                            htm.div("gap2");

                            htm.sDiv("center");
                            emitDeactiveForm(htm, key);
                        }
                    }
                    htm.eDiv(); // center
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Tests whether the student has taken exams since purchasing the e-text (used when a refund is requested).
     *
     * @param cache         the data cache
     * @param stu           the student record
     * @param whenActivated the date/time the key was activated
     * @param htm           the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitWorkSincePurchase(final Cache cache, final RawStudent stu,
                                              final ChronoLocalDateTime<LocalDate> whenActivated,
                                              final HtmlBuilder htm) throws SQLException {

        final List<RawSthomework> homeworks =
                RawSthomeworkLogic.queryByStudent(cache, stu.stuId, false);

        final Iterator<RawSthomework> hi = homeworks.iterator();
        while (hi.hasNext()) {
            final RawSthomework rec = hi.next();
            if (rec.getStartDateTime() != null && rec.getStartDateTime().isBefore(whenActivated)) {
                hi.remove();
            }
            if (rec.course != null && rec.course.startsWith("M 100")) {
                hi.remove();
            }
        }
        homeworks.sort(new RawSthomework.FinishDateTimeComparator());

        final List<RawStexam> exams = RawStexamLogic.queryByStudent(cache, stu.stuId, false);

        final Iterator<RawStexam> ei = exams.iterator();
        while (ei.hasNext()) {
            final RawStexam rec = ei.next();
            if (rec.getStartDateTime() != null && rec.getStartDateTime().isBefore(whenActivated)) {
                ei.remove();
            }
            if (rec.course != null && rec.course.startsWith("M 100")) {
                ei.remove();
            }
        }
        exams.sort(new RawStexam.FinishDateTimeComparator());

        htm.sDiv("indent22");
        htm.hr().div("gap2");

        if (homeworks.isEmpty()) {
            if (exams.isEmpty()) {
                htm.addln("Student has not completed any homework assignments or exams ",
                        "since purchasing the e-text");
            } else {
                htm.addln(
                        "Student has completed <strong>" + exams.size()
                                + " exams</strong> since purchasing the e-text, ",
                        "submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawStexam row : exams) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    htm.addln("<li>",
                            (fin == null ? "N/A" : TemporalUtils.FMT_MDY_AT_HM_A.format(fin)), //
                            "</li>");
                }
                htm.addln("</ul>");
            }
        } else {
            if (exams.isEmpty()) {
                htm.addln(
                        "Student has completed <strong>" + homeworks.size()
                                + " homework sets</strong> since purchasing the e-text, ",
                        "submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawSthomework row : homeworks) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    htm.addln("<li>",
                            (fin == null ? "N/A" : TemporalUtils.FMT_MDY_AT_HM_A.format(fin)), //
                            "</li>");
                }
            } else {
                htm.addln(
                        "Student has completed <strong>" + homeworks.size()
                                + " homework sets</strong> and <strong>" + exams.size()
                                + " exams</strong> since purchasing the e-text, ",
                        "submitting work on the following dates:");
                htm.addln("<ul>");
                for (final RawSthomework row : homeworks) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    htm.addln("<li>Homework: ",
                            (fin == null ? "N/A" : TemporalUtils.FMT_MDY_AT_HM_A.format(fin)), //
                            "</li>");
                }
                for (final RawStexam row : exams) {
                    final LocalDateTime fin = row.getFinishDateTime();
                    htm.addln("<li>Exam: ",
                            (fin == null ? "N/A" : TemporalUtils.FMT_MDY_AT_HM_A.format(fin)), //
                            "</li>");
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
        htm.addln(" <input type='hidden' name='key' value='", key,
                "'/>");
        htm.addln(" <input class='btn' type='submit' value='", Res.get(Res.DEACTIVATE_BTN_LBL),
                "'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.div("gap2").hr().eDiv();
    }
}
