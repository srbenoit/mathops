package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PrecalcTutorialLogic;
import dev.mathops.db.old.logic.PrecalcTutorialStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.UnitExamSession;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Generates the content of the home page.
 */
enum PageTakingExamPrecalc {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("panel");

        doPage(cache, site, htm, session);

        htm.eDiv(); // (end "panel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Displays the instructions for what to do after the exam, then launches the exam.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the user's login session information
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPage(final Cache cache, final PrecalcTutorialSite site, final HtmlBuilder htm,
                               final ImmutableSessionInfo session) throws SQLException {

        htm.sH(2).add("Precalculus Tutorial Administered by ProctorU").eH(2);

        String version = CoreConstants.EMPTY;

        final String studentId = session.getEffectiveUserId();
        final PrerequisiteLogic pl = new PrerequisiteLogic(cache, studentId);
        final PrecalcTutorialStatus precalcStat = new PrecalcTutorialLogic(cache, studentId,
                session.getNow().toLocalDate(), pl).status;

        final Set<String> courses = precalcStat.eligiblePrecalcExamCourses;

        Log.info("Eligible precalc exam: ", courses);

        if (courses.isEmpty()) {
            Log.info("Student ", session.getEffectiveUserId(), " not eligible for any precalc tutorial exams");
            Log.info("  Student has already completed:");
            for (final String s : precalcStat.completedPrecalcTutorials) {
                Log.info("    ", s);
            }
        } else {
            String course = courses.iterator().next();

            if (courses.size() > 1) {
                // There are more than 1 available exam - this page needs (1) at this point, so
                // pick the most likely based on which has the most recent passed unit 4 exam.
                LocalDateTime mostRecent = null;

                for (final String testCourse : courses) {
                    final List<RawStexam> passed = RawStexamLogic.getExams(cache, studentId, testCourse,
                            Integer.valueOf(4), true, "U");

                    for (final RawStexam testExam : passed) {
                        final LocalDateTime fin = testExam.getFinishDateTime();

                        if (fin != null) {
                            if (mostRecent == null || fin.isAfter(mostRecent)) {
                                mostRecent = fin;
                                course = testCourse;
                            }
                        }
                    }
                }
            }

            final SystemData systemData = cache.getSystemData();
            final RawExam exam = systemData.getActiveExamByCourseUnitType(course, Integer.valueOf(4), "U");
            if (exam != null) {
                version = exam.version;
            }

            Log.info("Student ", session.getEffectiveUserId(), " ready for precalc exam ", exam, " in ", courses);

            // FIXME: The following duplicates code in HtmlUnitExamPage - refactor to re-use

            final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
            UnitExamSession us = store.getUnitExamSession(session.loginSessionId, version);

            if (us == null) {
                final String redirect = "home.html?course=" + course.replace(CoreConstants.SPC, "%20");

                Log.info("Starting unit exam for session ", session.loginSessionId, " user ",
                        session.getEffectiveUserId(), " exam ", version);

                us = new UnitExamSession(cache, site.siteProfile, session.loginSessionId, session.getEffectiveUserId(),
                        course, version, redirect);
                store.setUnitExamSession(us);
            } else {
                Log.info("Found existing unit exam for session ", session.loginSessionId, " exam ", version);
            }

            htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' ", "method='POST'>");
            htm.addln(" <input type='hidden' name='exam' value='", version, "'>");
            htm.addln(" <input type='hidden' name='course' value='", courses, "'>");
            htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
            us.generateHtml(cache, session, htm);
            htm.addln("</form>");
        }
    }
}
