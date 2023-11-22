package dev.mathops.web.site.course;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the web page that displays all content in all courses, for easy review.
 */
enum PageStdsTextMedia {
    ;

    /** A server directory. */
    private static final String M125 = "M125";

//    /** A server directory. */
//    private static final String M126 = "M126";

    /** A reason string. */
    private static final String NOT_DONE = "NOT YET DONE";

    /** A reason string. */
    private static final String DEFERRED = "DEFERRED";

    /**
     * Starts the page that shows the course outline with student progress.
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

        final String course = req.getParameter("course");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else if (course == null) {
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            // htm.sDiv("panelu");

            if (RawRecordConstants.MATH125.equals(course)) {
                do125Media(htm);
            } else if (RawRecordConstants.MATH126.equals(course)) {
                do126Media(htm);
            }

            // htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Presents all content in the MATH 125 course.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void do125Media(final HtmlBuilder htm) {

        htm.sH(3).add("MATH 125 Course Materials").eH(3);

        htm.div("vgap0");
        htm.sH(4).add("Unit 1 (Chapter 41): Angles and Angle Measure").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR41_01_01", "SR41_01_01");
        emitExample(htm, M125, "Example SR41_02_01", "SR41_02_01");
        emitExample(htm, M125, "Example SR41_02_02", "SR41_02_02");
        emitExample(htm, M125, "Example SR41_02_03", "SR41_02_03");
        emitExample(htm, M125, "Example SR41_03_01", "SR41_03_01");
        emitExample(htm, M125, "Example SR41_03_02", "SR41_03_02");
        emitExample(htm, M125, "Example SR41_04_01", "SR41_04_01");
        emitExample(htm, M125, "Example SR41_04_02", "SR41_04_02");
        emitExample(htm, M125, "Example SR41_05_01", "SR41_05_01");
        emitExample(htm, M125, "Example SR41_05_02", "SR41_05_02");

        htm.add("<strong>Standard 41.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST41_1_F01_01", "ST41_1_F01_01");
        emitExample(htm, M125, "Example ST41_1_F02_01", "ST41_1_F02_01");
        emitExample(htm, M125, "Example ST41_1_F03_01", "ST41_1_F03_01");
        emitExample(htm, M125, "Example ST41_1_F04_01", "ST41_1_F04_01");
        emitExample(htm, M125, "Example ST41_1_F05_01", "ST41_1_F05_01");
        emitExample(htm, M125, "Example ST41_1_F06_01", "ST41_1_F06_01");

        htm.add("<strong>Standard 41.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST41_2_F01_01", "ST41_2_F01_01");
        emitExample(htm, M125, "Example ST41_2_F02_01", "ST41_2_F02_01");
        emitExample(htm, M125, "Example ST41_2_F03_01", "ST41_2_F03_01");
        emitExample(htm, M125, "Example ST41_2_F04_01", "ST41_2_F04_01");
        emitExample(htm, M125, "Example ST41_2_F05_01", "ST41_2_F05_01");
        emitExample(htm, M125, "Example ST41_2_F06_01", "ST41_2_F06_01");
        emitExample(htm, M125, "Example ST41_2_F07_01", "ST41_2_F07_01");

        htm.add("<strong>Standard 41.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST41_3_F01_01", "ST41_3_F01_01");
        emitExample(htm, M125, "Example ST41_3_F02_01", "ST41_3_F02_01");
        emitExample(htm, M125, "Example ST41_3_F03_01", "ST41_3_F03_01");
        emitExample(htm, M125, "Example ST41_3_F04_01", "ST41_3_F04_01");
        emitExample(htm, M125, "Example ST41_3_F05_01", "ST41_3_F05_01");
        emitExample(htm, M125, "Example ST41_3_F06_01", "ST41_3_F06_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 2 (Chapter 42): The Unit Circle").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR42_01_01", "SR42_01_01");
        emitExample(htm, M125, "Example SR42_01_03", "SR42_01_03");
        emitExample(htm, M125, "Example SR42_01_04", "SR42_01_04");
        emitExample(htm, M125, "Example SR42_01_05", "SR42_01_05");
        emitExample(htm, M125, "Example SR42_01_06", "SR42_01_06");
        emitExample(htm, M125, "Example SR42_02_01", "SR42_02_01");
        emitExample(htm, M125, "Example SR42_03_01", "SR42_03_01");
        emitExample(htm, M125, "Example SR42_04_01", "SR42_04_01");
        emitExample(htm, M125, "Example SR42_04_02", "SR42_04_02");

        htm.add("<strong>Standard 42.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST42_1_F01_01", "ST42_1_F01_01");
        emitExample(htm, M125, "Example ST42_1_F02_01", "ST42_1_F02_01");
        emitExample(htm, M125, "Example ST42_1_F02_02", "ST42_1_F02_02");
        emitExample(htm, M125, "Example ST42_1_F03_01", "ST42_1_F03_01");
        emitExample(htm, M125, "Example ST42_1_F04_01", "ST42_1_F04_01");
        emitExample(htm, M125, "Example ST42_1_F05_01", "ST42_1_F05_01");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F05_02");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F05_03");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F05_04");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F05_05");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F05_06");
        emitExample(htm, M125, "Example ST42_1_F06_01", "ST42_1_F06_01");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F06_02");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F06_03");
        emitExample(htm, M125, "Example ST42_1_F07_01", "ST42_1_F07_01");
        emitExample(htm, M125, "Example ST42_1_F07_02", "ST42_1_F07_02");
        emitDeferredExample(htm, DEFERRED, "Example ST42_1_F07_03");

        htm.add("<strong>Standard 42.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST42_2_F01_01", "ST42_2_F01_01");
        emitExample(htm, M125, "Example ST42_2_F02_01", "ST42_2_F02_01");
        emitExample(htm, M125, "Example ST42_2_F03_01", "ST42_2_F03_01");
        emitExample(htm, M125, "Example ST42_2_F04_01", "ST42_2_F04_01");
        emitExample(htm, M125, "Example ST42_2_F05_01", "ST42_2_F05_01");

        htm.add("<strong>Standard 42.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST42_3_F01_01", "ST42_3_F01_01");
        emitExample(htm, M125, "Example ST42_3_F02_01", "ST42_3_F02_01");
        emitExample(htm, M125, "Example ST42_3_F02_02", "ST42_3_F02_02");
        emitExample(htm, M125, "Example ST42_3_F03_01", "ST42_3_F03_01");
        emitExample(htm, M125, "Example ST42_3_F04_01", "ST42_3_F04_01");
        emitExample(htm, M125, "Example ST42_3_F05_01", "ST42_3_F05_01");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F05_02");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F05_03");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F05_04");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F05_05");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F05_06");
        emitExample(htm, M125, "Example ST42_3_F06_01", "ST42_3_F06_01");
        emitDeferredExample(htm, DEFERRED, "Example ST42_3_F06_02");
        emitExample(htm, M125, "Example ST42_3_F07_01", "ST42_3_F07_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 3 (Chapter 43): Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR43_01_01", "SR43_01_01");
        emitExample(htm, M125, "Example SR43_02_01", "SR43_02_01");
        emitExample(htm, M125, "Example SR43_03_01", "SR43_03_01");
        emitExample(htm, M125, "Example SR43_04_01", "SR43_04_01");
        emitExample(htm, M125, "Example SR43_05_01", "SR43_05_01");

        htm.add("<strong>Standard 43.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST43_1_F01_01", "ST43_1_F01_01");
        emitExample(htm, M125, "Example ST43_1_F02_01", "ST43_1_F02_01");
        emitExample(htm, M125, "Example ST43_1_F03_01", "ST43_1_F03_01");
        emitExample(htm, M125, "Example ST43_1_F04_01", "ST43_1_F04_01");
        emitExample(htm, M125, "Example ST43_1_F05_01", "ST43_1_F05_01");
        emitExample(htm, M125, "Example ST43_1_F06_01", "ST43_1_F06_01");

        htm.add("<strong>Standard 43.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST43_2_F01_01", "ST43_2_F01_01");
        emitExample(htm, M125, "Example ST43_2_F02_01", "ST43_2_F02_01");
        emitExample(htm, M125, "Example ST43_2_F03_01", "ST43_2_F03_01");
        emitExample(htm, M125, "Example ST43_2_F04_01", "ST43_2_F04_01");
        emitExample(htm, M125, "Example ST43_2_F05_01", "ST43_2_F05_01");
        emitExample(htm, M125, "Example ST43_2_F06_01", "ST43_2_F06_01");
        emitExample(htm, M125, "Example ST43_2_F07_01", "ST43_2_F07_01");
        emitExample(htm, M125, "Example ST43_2_F08_01", "ST43_2_F08_01");

        htm.add("<strong>Standard 43.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST43_3_F01_01", "ST43_3_F01_01");
        emitExample(htm, M125, "Example ST43_3_F02_01", "ST43_3_F02_01");
        emitExample(htm, M125, "Example ST43_3_F03_01", "ST43_3_F03_01");
        emitExample(htm, M125, "Example ST43_3_F04_01", "ST43_3_F04_01");
        emitExample(htm, M125, "Example ST43_3_F05_01", "ST43_3_F05_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 4 (Chapter 44): Transformations of Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR44_01_01", "SR44_01_01");
        emitExample(htm, M125, "Example SR44_02_01", "SR44_02_01");
        emitExample(htm, M125, "Example SR44_03_01", "SR44_03_01");
        emitExample(htm, M125, "Example SR44_04_01", "SR44_04_01");
        emitExample(htm, M125, "Example SR44_05_01", "SR44_05_01");
        emitExample(htm, M125, "Example SR44_06_01", "SR44_06_01");
        emitExample(htm, M125, "Example SR44_07_01", "SR44_07_01");
        emitExample(htm, M125, "Example SR44_08_01", "SR44_08_01");

        htm.add("<strong>Standard 44.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST44_1_F01_01", "ST44_1_F01_01");
        emitExample(htm, M125, "Example ST44_1_F01_02", "ST44_1_F01_02");
        emitExample(htm, M125, "Example ST44_1_F02_01", "ST44_1_F02_01");
        emitExample(htm, M125, "Example ST44_1_F02_02", "ST44_1_F02_02");
        emitExample(htm, M125, "Example ST44_1_F03_01", "ST44_1_F03_01");
        emitExample(htm, M125, "Example ST44_1_F03_02", "ST44_1_F03_02");
        emitExample(htm, M125, "Example ST44_1_F04_01", "ST44_1_F04_01");
        emitExample(htm, M125, "Example ST44_1_F04_02", "ST44_1_F04_02");
        emitExample(htm, M125, "Example ST44_1_F04_03", "ST44_1_F04_03");
        emitExample(htm, M125, "Example ST44_1_F04_04", "ST44_1_F04_04");
        emitExample(htm, M125, "Example ST44_1_F04_05", "ST44_1_F04_05");
        emitExample(htm, M125, "Example ST44_1_F04_06", "ST44_1_F04_06");
        emitExample(htm, M125, "Example ST44_1_F05_01", "ST44_1_F05_01");
        emitExample(htm, M125, "Example ST44_1_F05_02", "ST44_1_F05_02");
        emitExample(htm, M125, "Example ST44_1_F06_01", "ST44_1_F06_01");
        emitExample(htm, M125, "Example ST44_1_F06_02", "ST44_1_F06_02");
        emitExample(htm, M125, "Example ST44_1_F07_01", "ST44_1_F07_01");
        emitExample(htm, M125, "Example ST44_1_F07_02", "ST44_1_F07_02");
        emitExample(htm, M125, "Example ST44_1_F08_01", "ST44_1_F08_01");
        emitExample(htm, M125, "Example ST44_1_F08_02", "ST44_1_F08_02");
        emitExample(htm, M125, "Example ST44_1_F08_03", "ST44_1_F08_03");
        emitExample(htm, M125, "Example ST44_1_F08_04", "ST44_1_F08_04");
        emitExample(htm, M125, "Example ST44_1_F08_05", "ST44_1_F08_05");
        emitExample(htm, M125, "Example ST44_1_F08_06", "ST44_1_F08_06");
        emitExample(htm, M125, "Example ST44_1_F09_01", "ST44_1_F09_01");
        emitExample(htm, M125, "Example ST44_1_F09_02", "ST44_1_F09_02");
        emitExample(htm, M125, "Example ST44_1_F10_01", "ST44_1_F10_01");

        htm.add("<strong>Standard 44.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST44_2_F01_01", "ST44_2_F01_01");
        emitExample(htm, M125, "Example ST44_2_F01_02", "ST44_2_F01_02");
        emitExample(htm, M125, "Example ST44_2_F02_01", "ST44_2_F02_01");
        emitExample(htm, M125, "Example ST44_2_F02_02", "ST44_2_F02_02");
        emitExample(htm, M125, "Example ST44_2_F03_01", "ST44_2_F03_01");
        emitExample(htm, M125, "Example ST44_2_F03_02", "ST44_2_F03_02");
        emitExample(htm, M125, "Example ST44_2_F04_01", "ST44_2_F04_01");
        emitExample(htm, M125, "Example ST44_2_F04_02", "ST44_2_F04_02");
        emitExample(htm, M125, "Example ST44_2_F04_03", "ST44_2_F04_03");
        emitExample(htm, M125, "Example ST44_2_F04_04", "ST44_2_F04_04");
        emitExample(htm, M125, "Example ST44_2_F04_05", "ST44_2_F04_05");
        emitExample(htm, M125, "Example ST44_2_F04_06", "ST44_2_F04_06");
        emitExample(htm, M125, "Example ST44_2_F05_01", "ST44_2_F05_01");
        emitExample(htm, M125, "Example ST44_2_F05_02", "ST44_2_F05_02");
        emitExample(htm, M125, "Example ST44_2_F06_01", "ST44_2_F06_01");
        emitExample(htm, M125, "Example ST44_2_F06_02", "ST44_2_F06_02");
        emitExample(htm, M125, "Example ST44_2_F07_01", "ST44_2_F07_01");
        emitExample(htm, M125, "Example ST44_2_F07_02", "ST44_2_F07_02");
        emitExample(htm, M125, "Example ST44_2_F08_01", "ST44_2_F08_01");
        emitExample(htm, M125, "Example ST44_2_F08_02", "ST44_2_F08_02");
        emitExample(htm, M125, "Example ST44_2_F08_03", "ST44_2_F08_03");
        emitExample(htm, M125, "Example ST44_2_F08_04", "ST44_2_F08_04");
        emitExample(htm, M125, "Example ST44_2_F08_05", "ST44_2_F08_05");
        emitExample(htm, M125, "Example ST44_2_F08_06", "ST44_2_F08_06");
        emitExample(htm, M125, "Example ST44_2_F09_01", "ST44_2_F09_01");
        emitExample(htm, M125, "Example ST44_2_F09_02", "ST44_2_F09_02");
        emitExample(htm, M125, "Example ST44_2_F10_01", "ST44_2_F10_01");

        htm.add("<strong>Standard 44.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST44_3_F01_01", "ST44_3_F01_01");
        emitExample(htm, M125, "Example ST44_3_F01_02", "ST44_3_F01_02");
        emitExample(htm, M125, "Example ST44_3_F02_01", "ST44_3_F02_01");
        emitExample(htm, M125, "Example ST44_3_F02_02", "ST44_3_F02_02");
        emitExample(htm, M125, "Example ST44_3_F03_01", "ST44_3_F03_01");
        emitExample(htm, M125, "Example ST44_3_F04_01", "ST44_3_F04_01");
        emitExample(htm, M125, "Example ST44_3_F05_01", "ST44_3_F05_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 5 (Chapter 45): Modeling with Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR45_01_01", "SR45_01_01");
        emitExample(htm, M125, "Example SR45_02_01", "SR45_02_01");
        emitExample(htm, M125, "Example SR45_03_01", "SR45_03_01");
        emitExample(htm, M125, "Example SR45_04_01", "SR45_04_01");
        emitExample(htm, M125, "Example SR45_05_01", "SR45_05_01");

        htm.add("<strong>Standard 45.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST45_1_F01_01", "ST45_1_F01_01");
        emitExample(htm, M125, "Example ST45_1_F02_01", "ST45_1_F02_01");
        emitExample(htm, M125, "Example ST45_1_F03_01", "ST45_1_F03_01");
        emitExample(htm, M125, "Example ST45_1_F04_01", "ST45_1_F04_01");
        // $NON-NLS-2$
        htm.add("<strong>Standard 45.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST45_2_F01_01", "ST45_2_F01_01");
        emitExample(htm, M125, "Example ST45_2_F02_01", "ST45_2_F02_01");
        emitExample(htm, M125, "Example ST45_2_F03_01", "ST45_2_F03_01");
        emitExample(htm, M125, "Example ST45_2_F04_01", "ST45_2_F04_01");
        emitExample(htm, M125, "Example ST45_2_F05_01", "ST45_2_F05_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_2_F06_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_2_F07_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_2_F08_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_2_F09_01");

        htm.add("<strong>Standard 45.3</strong>").br().addln();
        emitDeferredExample(htm, NOT_DONE, "Example ST45_3_F01_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_3_F02_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_3_F03_01");
        emitDeferredExample(htm, NOT_DONE, "Example ST45_3_F04_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Synthesis Explorations").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 6 (Chapter 46): Right Triangles").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR46_01_01", "SR46_01_01");
        emitExample(htm, M125, "Example SR46_01_02", "SR46_01_02");
        emitExample(htm, M125, "Example SR46_01_03", "SR46_01_03");
        emitExample(htm, M125, "Example SR46_02_01", "SR46_02_01");
        emitExample(htm, M125, "Example SR46_03_01", "SR46_03_01");
        emitExample(htm, M125, "Example SR46_04_01", "SR46_04_01");
        emitExample(htm, M125, "Example SR46_05_01", "SR46_05_01");

        htm.add("<strong>Standard 46.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST46_1_F01_01", "ST46_1_F01_01");
        emitExample(htm, M125, "Example ST46_1_F02_01", "ST46_1_F02_01");
        emitExample(htm, M125, "Example ST46_1_F03_01", "ST46_1_F03_01");
        emitExample(htm, M125, "Example ST46_1_F04_01", "ST46_1_F04_01");
        emitExample(htm, M125, "Example ST46_1_F05_01", "ST46_1_F05_01");
        emitExample(htm, M125, "Example ST46_1_F06_01", "ST46_1_F06_01");

        htm.add("<strong>Standard 46.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST46_2_F01_01", "ST46_2_F01_01");
        emitExample(htm, M125, "Example ST46_2_F01_02", "ST46_2_F01_02");
        emitExample(htm, M125, "Example ST46_2_F02_01", "ST46_2_F02_01");
        emitExample(htm, M125, "Example ST46_2_F02_02", "ST46_2_F02_02");
        emitExample(htm, M125, "Example ST46_2_F03_01", "ST46_2_F03_01");
        emitExample(htm, M125, "Example ST46_2_F03_02", "ST46_2_F03_02");
        emitExample(htm, M125, "Example ST46_2_F04_01", "ST46_2_F04_01");
        emitExample(htm, M125, "Example ST46_2_F05_01", "ST46_2_F05_01");
        emitExample(htm, M125, "Example ST46_2_F06_01", "ST46_2_F06_01");
        emitExample(htm, M125, "Example ST46_2_F06_02", "ST46_2_F06_02");

        htm.add("<strong>Standard 46.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST46_3_F01_01", "ST46_3_F01_01");
        emitExample(htm, M125, "Example ST46_3_F02_01", "ST46_3_F02_01");
        emitExample(htm, M125, "Example ST46_3_F03_01", "ST46_3_F03_01");
        emitExample(htm, M125, "Example ST46_3_F04_01", "ST46_3_F04_01");
        emitExample(htm, M125, "Example ST46_3_F05_01", "ST46_3_F05_01");
        emitExample(htm, M125, "Example ST46_3_F06_01", "ST46_3_F06_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 7 (Chapter 47): Inverse Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR47_01_01", "SR47_01_01");
        emitExample(htm, M125, "Example SR47_01_02", "SR47_01_02");
        emitExample(htm, M125, "Example SR47_02_01", "SR47_02_01");
        emitExample(htm, M125, "Example SR47_02_02", "SR47_02_02");
        emitExample(htm, M125, "Example SR47_03_01", "SR47_03_01");
        emitExample(htm, M125, "Example SR47_03_02", "SR47_03_02");
        emitExample(htm, M125, "Example SR47_04_01", "SR47_04_01");
        emitExample(htm, M125, "Example SR47_05_01", "SR47_05_01");
        emitExample(htm, M125, "Example SR47_06_01", "SR47_06_01");

        htm.add("<strong>Standard 47.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST47_1_F01_01", "ST47_1_F01_01");
        emitExample(htm, M125, "Example ST47_1_F02_01", "ST47_1_F02_01");
        emitExample(htm, M125, "Example ST47_1_F03_01", "ST47_1_F03_01");
        emitExample(htm, M125, "Example ST47_1_F04_01", "ST47_1_F04_01");
        emitExample(htm, M125, "Example ST47_1_F05_01", "ST47_1_F05_01");
        emitExample(htm, M125, "Example ST47_1_F06_01", "ST47_1_F06_01");
        emitExample(htm, M125, "Example ST47_1_F07_01", "ST47_1_F07_01");
        emitExample(htm, M125, "Example ST47_1_F08_01", "ST47_1_F08_01");
        emitExample(htm, M125, "Example ST47_1_F09_01", "ST47_1_F09_01");

        htm.add("<strong>Standard 47.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST47_2_F01_01", "ST47_2_F01_01");
        emitExample(htm, M125, "Example ST47_2_F02_01", "ST47_2_F02_01");
        emitExample(htm, M125, "Example ST47_2_F03_01", "ST47_2_F03_01");
        emitExample(htm, M125, "Example ST47_2_F04_01", "ST47_2_F04_01");
        emitExample(htm, M125, "Example ST47_2_F05_01", "ST47_2_F05_01");
        emitExample(htm, M125, "Example ST47_2_F06_01", "ST47_2_F06_01");
        emitExample(htm, M125, "Example ST47_2_F07_01", "ST47_2_F07_01");

        htm.add("<strong>Standard 47.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST47_3_F01_01", "ST47_3_F01_01");
        emitExample(htm, M125, "Example ST47_3_F02_01", "ST47_3_F02_01");
        emitExample(htm, M125, "Example ST47_3_F03_01", "ST47_3_F03_01");
        emitExample(htm, M125, "Example ST47_3_F04_01", "ST47_3_F04_01");
        emitExample(htm, M125, "Example ST47_3_F05_01", "ST47_3_F05_01");
        emitExample(htm, M125, "Example ST47_3_F06_01", "ST47_3_F06_01");
        emitExample(htm, M125, "Example ST47_3_F06_03", "ST47_3_F06_03");
        emitDeferredExample(htm, NOT_DONE, "Example ST47_3_F06_05");

        htm.div("vgap");
        htm.sH(4).add("Unit 8 (Chapter 48): Law of Sines and Law of Cosines").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR48_01_01", "SR48_01_01");
        emitExample(htm, M125, "Example SR48_02_01", "SR48_02_01");
        emitExample(htm, M125, "Example SR48_03_01", "SR48_03_01");
        emitExample(htm, M125, "Example SR48_04_01", "SR48_04_01");
        emitExample(htm, M125, "Example SR48_05_01", "SR48_05_01");

        htm.add("<strong>Standard 48.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST48_1_F01_01", "ST48_1_F01_01");
        emitExample(htm, M125, "Example ST48_1_F02_01", "ST48_1_F02_01");
        emitExample(htm, M125, "Example ST48_1_F03_01", "ST48_1_F03_01");
        emitExample(htm, M125, "Example ST48_1_F04_01", "ST48_1_F04_01");
        emitExample(htm, M125, "Example ST48_1_F05_01", "ST48_1_F05_01");
        emitExample(htm, M125, "Example ST48_1_F06_01", "ST48_1_F06_01");

        htm.add("<strong>Standard 48.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST48_2_F01_01", "ST48_2_F01_01");
        emitExample(htm, M125, "Example ST48_2_F02_01", "ST48_2_F02_01");
        emitExample(htm, M125, "Example ST48_2_F03_01", "ST48_2_F03_01");
        emitExample(htm, M125, "Example ST48_2_F04_01", "ST48_2_F04_01");

        htm.add("<strong>Standard 48.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST48_3_F01_01", "ST48_3_F01_01");
        emitExample(htm, M125, "Example ST48_3_F02_01", "ST48_3_F02_01");
        emitExample(htm, M125, "Example ST48_3_F03_01", "ST48_3_F03_01");
        emitExample(htm, M125, "Example ST48_3_F04_01", "ST48_3_F04_01");
        emitExample(htm, M125, "Example ST48_3_F05_01", "ST48_3_F05_01");
        emitExample(htm, M125, "Example ST48_3_F06_01", "ST48_3_F06_01");
        emitExample(htm, M125, "Example ST48_3_F07_01", "ST48_3_F07_01");
        emitExample(htm, M125, "Example ST48_3_F08_01", "ST48_3_F08_01");
        emitExample(htm, M125, "Example ST48_3_F09_01", "ST48_3_F09_01");
        emitExample(htm, M125, "Example ST48_3_F10_01", "ST48_3_F10_01");

        htm.div("vgap");
        htm.sH(4).add("Unit 9 (Chapter 49): Vectors and Trigonometry").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR49_01_01", "ST48_3_F10_01");
        emitExample(htm, M125, "Example SR49_02_01", "ST48_3_F10_01");
        emitExample(htm, M125, "Example SR49_03_01", "ST48_3_F10_01");
        emitExample(htm, M125, "Example SR49_04_01", "ST48_3_F10_01");
        emitExample(htm, M125, "Example SR49_05_01", "ST48_3_F10_01");
        emitExample(htm, M125, "Example SR49_06_01", "ST48_3_F10_01");

        htm.add("<strong>Standard 49.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST49_1_F01_01", "ST49_1_F01_01");
        emitExample(htm, M125, "Example ST49_1_F02_01", "ST49_1_F02_01");
        emitExample(htm, M125, "Example ST49_1_F03_01", "ST49_1_F03_01");
        emitExample(htm, M125, "Example ST49_1_F04_01", "ST49_1_F04_01");
        emitExample(htm, M125, "Example ST49_1_F05_01", "ST49_1_F05_01");
        emitExample(htm, M125, "Example ST49_1_F06_01", "ST49_1_F06_01");

        htm.add("<strong>Standard 49.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST49_2_F01_01", "ST49_2_F01_01");
        emitExample(htm, M125, "Example ST49_2_F02_01", "ST49_2_F02_01");
        emitExample(htm, M125, "Example ST49_2_F03_01", "ST49_2_F03_01");
        emitExample(htm, M125, "Example ST49_2_F04_01", "ST49_2_F04_01");

        htm.add("<strong>Standard 49.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST49_3_F01_01", "ST49_3_F01_01");
        emitExample(htm, M125, "Example ST49_3_F02_01", "ST49_3_F02_01");
        emitExample(htm, M125, "Example ST49_3_F03_01", "ST49_3_F03_01");
        emitExample(htm, M125, "Example ST49_3_F04_01", "ST49_3_F04_01");

        htm.div("vgap");
        htm.sH(4).add("Unit 10 (Chapter 50): Applications of Trigonometry").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example SR50_01_01", "SR50_01_01");
        emitExample(htm, M125, "Example SR50_02_01", "SR50_02_01");
        emitExample(htm, M125, "Example SR50_03_01", "SR50_03_01");
        emitExample(htm, M125, "Example SR50_04_01", "SR50_04_01");
        emitExample(htm, M125, "Example SR50_05_01", "SR50_05_01");
        emitExample(htm, M125, "Example SR50_06_01", "SR50_06_01");
        emitExample(htm, M125, "Example SR50_07_01", "SR50_07_01");
        emitExample(htm, M125, "Example SR50_08_01", "SR50_08_01");

        htm.add("<strong>Standard 50.1</strong>").br().addln();
        emitExample(htm, M125, "Example ST50_1_F01_01", "ST50_1_F01_01");
        emitExample(htm, M125, "Example ST50_1_F01_02", "ST50_1_F01_02");
        emitExample(htm, M125, "Example ST50_1_F02_01", "ST50_1_F02_01");
        emitExample(htm, M125, "Example ST50_1_F02_02", "ST50_1_F02_02");
        emitExample(htm, M125, "Example ST50_1_F03_01", "ST50_1_F03_01");
        emitExample(htm, M125, "Example ST50_1_F04_01", "ST50_1_F04_01");
        emitExample(htm, M125, "Example ST50_1_F05_01", "ST50_1_F05_01");
        emitExample(htm, M125, "Example ST50_1_F06_01", "ST50_1_F06_01");

        htm.add("<strong>Standard 50.2</strong>").br().addln();
        emitExample(htm, M125, "Example ST50_2_F01_01", "ST50_2_F01_01");
        emitExample(htm, M125, "Example ST50_2_F02_01", "ST50_2_F02_01");
        emitExample(htm, M125, "Example ST50_2_F03_01", "ST50_2_F03_01");
        emitExample(htm, M125, "Example ST50_2_F04_01", "ST50_2_F04_01");

        htm.add("<strong>Standard 50.3</strong>").br().addln();
        emitExample(htm, M125, "Example ST50_3_F01_01", "ST50_3_F01_01");
        emitExample(htm, M125, "Example ST50_3_F02_01", "ST50_3_F02_01");
        emitExample(htm, M125, "Example ST50_3_F03_01", "ST50_3_F03_01");
        emitExample(htm, M125, "Example ST50_3_F04_01", "ST50_3_F04_01");
        emitExample(htm, M125, "Example ST50_3_F05_01", "ST50_3_F05_01");

        htm.div("vgap");
        htm.sH(4).add("Synthesis Explorations").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();
    }

    /**
     * Presents all content in the MATH 126 course.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void do126Media(final HtmlBuilder htm) {

        htm.sH(3).add("MATH 126 Course Materials").eH(3);

        htm.div("vgap0");
        htm.sH(4).add("Unit 1 (Chapter 51): Fundamental Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 2 (Chapter 52): Sum and Difference Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 3 (Chapter 53): Multiple- and Half-Angle Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 4 (Chapter 54): Trigonometric Equations").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 5 (Chapter 55): Applications of Trigonometric Equations").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Synthesis Activities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 6 (Chapter 56): Complex Numbers").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 7 (Chapter 57): Polar Coordinates").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 8 (Chapter 58): Polar Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 9 (Chapter 59): 3D Cartesian and Cylindrical Coordinates").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Unit 10 (Chapter 60): Spherical Coordiantes").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Synthesis Activities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();
    }

    /**
     * Emits an example.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param courseDir the directory on the web servers where course files are located
     * @param title     the title
     * @param id        the media ID
     */
    private static void emitExample(final HtmlBuilder htm, final String courseDir, final String title,
                                    final String id) {

        htm.addln("<details>");

        htm.addln("<summary>", title, ": &nbsp; ");
        htm.addln("<a href='https://nibbler.math.colostate.edu/media/", courseDir, "/pdf/", id,
                ".pdf' target='_blank'>Writeup (PDF)</a>");
        htm.addln("</summary>");

        htm.addln("<video width='960' height='540' controls style='border:2px #777 solid;'>");
        htm.addln("  <source src='https://nibbler.math.colostate.edu/media/", courseDir, "/mp4/", id,
                ".mp4' type='video/mp4'/>");
        htm.addln("  <track  src='/www/math/", courseDir, "/vtt/", id,
                ".vtt' kind='subtitles' srclang='en' label='English' default/>");
        htm.addln("Your browser does not support inline video.");
        htm.addln("</video>").br();

        htm.addln("<a href='/www/math/", courseDir, "/transcripts/", id,
                ".txt' target='_blank'>Plain-text transcript</a>");

        htm.addln("</details>");
    }

    /**
     * Emits a placeholder for an example that is not yet done for some reason.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param reason a reason, like "DEFERRED", "NOT YET DONE", etc.
     * @param title  the title
     */
    private static void emitDeferredExample(final HtmlBuilder htm, final String reason,
                                            final String title) {

        htm.sDiv(null, "style='margin-left:18px; color:gray;'");
        htm.addln(title, " (", reason, ")");
        htm.eDiv();
    }
}
