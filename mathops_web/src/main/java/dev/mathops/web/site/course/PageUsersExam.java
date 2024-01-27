package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawUsersLogic;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.servlet.StudentInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * The user's exam page.
 */
enum PageUsersExam {
    ;

    /**
     * Generates a page with student User's exam status and button to launch exam if appropriate.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        doPageContent(cache, site, htm, session);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page content.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param htm     the HMTL builder to which to append
     * @param session the user's login session information
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPageContent(final Cache cache, final CourseSite site, final HtmlBuilder htm,
                                      final ImmutableSessionInfo session) throws SQLException {

        final StudentInfo info = new StudentInfo(site.getDbProfile());

        final String stuId = session.getEffectiveUserId();
        info.gatherData(cache, stuId);

        htm.sH(2).add("<img style='width:40px;height:40px;vertical-align:middle;' ",
                "src='/images/users_exam.png'/> &nbsp; User's Exam").eH(2);

        // TODO: Find the earliest date, for any of the courses the student is in, that the
        // user's exam will be available and display that here instead of the button.

        htm.sDiv("indent11");

        if (info.isRequireLicensed()) {
            htm.div("vgap");
            htm.sP().add("The User's Exam covers the course policies and rules explained in the ",
                    "<a href='https://www.math.colostate.edu/Precalc/Precalc-Student-Guide.pdf' ",
                    "class='ulink' target='_blank'>Student Guide</a>.").eP();

            if (info.isLicensed()) {
                htm.hr().sP("indent").add("You have passed the User's Exam.").eP();
            } else {
                htm.sP().add("You must take and pass the User's Exam before you can begin work in ",
                        "any of your Precalculus courses.").hr();

                htm.sDiv("indent");
                htm.sP().add("You have not yet passed the User's Exam.").eP();

                final TermRec active = TermLogic.get(cache).queryActive(cache);
                final RawCusection cusection = RawCusectionLogic.query(cache, //
                        RawRecordConstants.M100U, "1", Integer.valueOf(1), active.term);
                final boolean avail;
                if (cusection == null) {
                    htm.eP();
                    htm.sP().add("Information on User's Exam not found.");
                    avail = false;
                } else if ((cusection.firstTestDt == null)
                        || !cusection.firstTestDt.isAfter(session.getNow().toLocalDate())) {
                    avail = true;
                } else {
                    if (RawSpecialStusLogic.isSpecialType(cache, stuId,
                            session.getNow().toLocalDate(), "STEVE", "ADMIN", "TUTOR")) {
                        avail = true;
                    } else {
                        htm.eP();
                        htm.sP().add("The User's Exam will be available ",
                                TemporalUtils.FMT_WMD_LONG.format(cusection.firstTestDt), CoreConstants.DOT);
                        avail = false;
                    }
                }

                if (avail) {
                    htm.addln("<form method='get' action='run_review.html'>");
                    htm.addln(" <input type='hidden' name='mode' value='course'>");
                    htm.addln(" <input type='hidden' name='exam' value='UOOOO'>");
                    htm.addln(" <button class='btn' type='submit'>Take User's Exam</button>");
                    htm.addln("</form>");
                }

                htm.eP();
                htm.eDiv(); // indent
            }
        } else if (info.isLicensed()) {
            htm.sP("indent")
                    .add("You have passed the User's Exam.").eP();
        } else {
            htm.sP().add("You are not required to take the Users Exam before working in Precalculus courses.").eP();
        }

        final List<RawUsers> exams =
                RawUsersLogic.queryByStudent(cache, session.getEffectiveUserId());

        for (final RawUsers exam : exams) {
            final String path = ExamWriter.makeWebExamPath(exam.termKey.shortString,
                    session.getEffectiveUserId(), exam.serialNbr.longValue());

            htm.sDiv("indent");

            htm.addln(" <a href='see_past_exam.html?",
                    "exam=", exam.version,
                    "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                    "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                    "'>View the ", exam.getExamLabel(), "</a>");

            htm.eDiv();
        }
    }
}
