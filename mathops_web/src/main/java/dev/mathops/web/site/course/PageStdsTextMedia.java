package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        htm.sH(4).add("Module 1: Angles and Angle Measure").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR01_SR1_01", "TR01_SR1_01");
        emitExample(htm, M125, "Example TR01_SR2_01", "TR01_SR2_01");
        emitExample(htm, M125, "Example TR01_SR2_02", "TR01_SR2_02");
        emitExample(htm, M125, "Example TR01_SR2_03", "TR01_SR2_03");
        emitExample(htm, M125, "Example TR01_SR3_01", "TR01_SR3_01");
        emitExample(htm, M125, "Example TR01_SR3_02", "TR01_SR3_02");
        emitExample(htm, M125, "Example TR01_SR4_01", "TR01_SR4_01");
        emitExample(htm, M125, "Example TR01_SR4_02", "TR01_SR4_02");
        emitExample(htm, M125, "Example TR01_SR5_01", "TR01_SR5_01");
        emitExample(htm, M125, "Example TR01_SR5_02", "TR01_SR5_02");

        htm.add("<strong>Learning Target 1.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR01_ST1A_01", "TR01_ST1A_01");
        emitExample(htm, M125, "Example TR01_ST1A_02", "TR01_ST1A_02");
        emitExample(htm, M125, "Example TR01_ST1A_03", "TR01_ST1A_03");
        emitExample(htm, M125, "Example TR01_ST1C_01", "TR01_ST1C_01");
        emitExample(htm, M125, "Example TR01_ST1C_02", "TR01_ST1C_02");
        emitExample(htm, M125, "Example TR01_ST1D_01", "TR01_ST1D_01");
        emitExample(htm, M125, "Example TR01_ST1E_01", "TR01_ST1E_01");
        emitExample(htm, M125, "Example TR01_ST1F_01", "TR01_ST1F_01");

        htm.add("<strong>Learning Target 1.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR01_ST2A_01", "TR01_ST2A_01");
        emitExample(htm, M125, "Example TR01_ST2A_02", "TR01_ST2A_02");
        emitExample(htm, M125, "Example TR01_ST2B_01", "TR01_ST2B_01");
        emitExample(htm, M125, "Example TR01_ST2C_01", "TR01_ST2C_01");
        emitExample(htm, M125, "Example TR01_ST2E_01", "TR01_ST2E_01");
        emitExample(htm, M125, "Example TR01_ST2F_01", "TR01_ST2F_01");

        htm.add("<strong>Learning Target 1.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR01_ST3A_01", "TR01_ST3A_01");
        emitExample(htm, M125, "Example TR01_ST3C_01", "TR01_ST3C_01");
        emitExample(htm, M125, "Example TR01_ST3C_02", "TR01_ST3C_02");
        emitExample(htm, M125, "Example TR01_ST3D_01", "TR01_ST3D_01");
        emitExample(htm, M125, "Example TR01_ST3D_02", "TR01_ST3D_02");
        emitExample(htm, M125, "Example TR01_ST3E_01", "TR01_ST3E_01");
        emitExample(htm, M125, "Example TR01_ST3E_02", "TR01_ST3E_02");
        emitExample(htm, M125, "Example TR01_ST3F_01", "TR01_ST3F_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 2: The Unit Circle").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR02_SR1_01a", "TR02_SR1_01a");
        emitExample(htm, M125, "Example TR02_SR1_01c", "TR02_SR1_01c");
        emitExample(htm, M125, "Example TR02_SR1_01d", "TR02_SR1_01d");
        emitExample(htm, M125, "Example TR02_SR1_01e", "TR02_SR1_01e");
        emitExample(htm, M125, "Example TR02_SR1_01f", "TR02_SR1_01f");
        emitExample(htm, M125, "Example TR02_SR2_01", "TR02_SR2_01");
        emitExample(htm, M125, "Example TR02_SR3_01", "TR02_SR3_01");
        emitExample(htm, M125, "Example TR02_SR4_01a", "TR02_SR4_01a");
        emitExample(htm, M125, "Example TR02_SR5_01a", "TR02_SR5_01a");

        htm.add("<strong>Learning Target 2.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR02_ST1A_01", "TR02_ST1A_01");
        emitExample(htm, M125, "Example TR02_ST1B_01", "TR02_ST1B_01");
        emitExample(htm, M125, "Example TR02_ST1B_02", "TR02_ST1B_02");
        emitExample(htm, M125, "Application TR02_ST1B_APP_01B", "TR02_ST1B_APP_01B");
        emitExample(htm, M125, "Example TR02_ST1C_01", "TR02_ST1C_01");
        emitExample(htm, M125, "Application TR02_ST1C_APP_01", "TR02_ST1C_APP_01");
        emitExample(htm, M125, "Example TR02_ST1D_01", "TR02_ST1D_01");
        emitExample(htm, M125, "Application TR02_ST1D_APP_01", "TR02_ST1D_APP_01");
        emitExample(htm, M125, "Application TR02_ST1D_APP_02", "TR02_ST1D_APP_02");

        htm.add("<strong>Learning Target 2.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR02_ST2A_01", "TR02_ST2A_01");
        emitExample(htm, M125, "Example TR02_ST2B_01", "TR02_ST2B_01");
        emitExample(htm, M125, "Example TR02_ST2B_02", "TR02_ST2B_02");
        emitExample(htm, M125, "Example TR02_ST2B_03", "TR02_ST2B_03");
        emitExample(htm, M125, "Example TR02_ST2C_01", "TR02_ST2C_01");

        htm.add("<strong>Learning Target 2.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR02_ST3A_01", "TR02_ST3A_01");
        emitExample(htm, M125, "Example TR02_ST3B_01", "TR02_ST3B_01");
        emitExample(htm, M125, "Example TR02_ST3B_02", "TR02_ST3B_02");
        emitExample(htm, M125, "Application TR02_ST3B_APP_01", "TR02_ST3B_APP_01");
        emitExample(htm, M125, "Example TR02_ST3C_01", "TR02_ST3C_01");
        emitExample(htm, M125, "Application TR02_ST3C_APP_01", "TR02_ST3C_APP_01");
        emitExample(htm, M125, "Example TR02_ST3D_01", "TR02_ST3D_01");
        emitExample(htm, M125, "Application TR02_ST3D_APP_01", "TR02_ST3D_APP_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 3: Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR03_SR1_01", "TR03_SR1_01");
        emitExample(htm, M125, "Example TR03_SR2_01", "TR03_SR2_01");
        emitExample(htm, M125, "Example TR03_SR3_01", "TR03_SR3_01");
        emitExample(htm, M125, "Example TR03_SR4_01", "TR03_SR4_01");
        emitExample(htm, M125, "Example TR03_SR5_01", "TR03_SR5_01");
        emitExample(htm, M125, "Example TR03_SR6_01", "TR03_SR6_01");

        htm.add("<strong>Learning Target 3.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR03_ST1A_01", "TR03_ST1A_01");
        emitExample(htm, M125, "Example TR03_ST1A_02", "TR03_ST1A_02");
        emitExample(htm, M125, "Example TR03_ST1B_01", "TR03_ST1B_01");
        emitExample(htm, M125, "Example TR03_ST1C_01", "TR03_ST1C_01");
        emitExample(htm, M125, "Example TR03_ST1D_01", "TR03_ST1D_01");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_01", "TR03_ST1E_EXP_01");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_02", "TR03_ST1E_EXP_02");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_03", "TR03_ST1E_EXP_03");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_04", "TR03_ST1E_EXP_04");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_05", "TR03_ST1E_EXP_05");
        emitExample(htm, M125, "Exploration TR03_ST1E_EXP_06", "TR03_ST1E_EXP_06");
        emitExample(htm, M125, "Exploration TR03_ST1F_EXP_01", "TR03_ST1F_EXP_01");
        emitExample(htm, M125, "Exploration TR03_ST1F_EXP_02", "TR03_ST1F_EXP_02");
        emitExample(htm, M125, "Exploration TR03_ST1F_EXP_03", "TR03_ST1F_EXP_03");

        htm.add("<strong>Learning Target 3.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR03_ST2A_01", "TR03_ST2A_01");
        emitExample(htm, M125, "Example TR03_ST2B_01", "TR03_ST2B_01");
        emitExample(htm, M125, "Example TR03_ST2B_02", "TR03_ST2B_02");
        emitExample(htm, M125, "Example TR03_ST2D_01", "TR03_ST2D_01");
        emitExample(htm, M125, "Example TR03_ST2F_01", "TR03_ST2F_01");
        emitExample(htm, M125, "Example TR03_ST2F_02", "TR03_ST2F_02");
        emitExample(htm, M125, "Example TR03_ST2F_03", "TR03_ST2F_03");
        emitExample(htm, M125, "Example TR03_ST2G_01", "TR03_ST2G_01");
        emitExample(htm, M125, "Example TR03_ST2H_01", "TR03_ST2H_01");

        htm.add("<strong>Learning Target 3.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR03_ST3A_01", "TR03_ST3A_01");
        emitExample(htm, M125, "Example TR03_ST3B_01", "TR03_ST3B_01");
        emitExample(htm, M125, "Example TR03_ST3C_01", "TR03_ST3C_01");
        emitExample(htm, M125, "Example TR03_ST3C_03", "TR03_ST3C_03");
        emitExample(htm, M125, "Example TR03_ST3C_05", "TR03_ST3C_05");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 4: Transformations of Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR04_SR1_01", "TR04_SR1_01");
        emitExample(htm, M125, "Example TR04_SR2_01", "TR04_SR2_01");
        emitExample(htm, M125, "Example TR04_SR3_01", "TR04_SR3_01");
        emitExample(htm, M125, "Example TR04_SR4_01", "TR04_SR4_01");
        emitExample(htm, M125, "Example TR04_SR5_01", "TR04_SR5_01");
        emitExample(htm, M125, "Example TR04_SR6_01", "TR04_SR6_01");
        emitExample(htm, M125, "Example TR04_SR7_01", "TR04_SR7_01");
        emitExample(htm, M125, "Example TR04_SR8_01", "TR04_SR8_01");
        emitExample(htm, M125, "Example TR04_SR9_01", "TR04_SR9_01");
        emitExample(htm, M125, "Example TR04_SR10_01", "TR04_SR10_01");

        htm.add("<strong>Learning Target 4.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR04_ST1A_01", "TR04_ST1A_01");
        emitExample(htm, M125, "Example TR04_ST1A_02", "TR04_ST1A_02");
        emitExample(htm, M125, "Example TR04_ST1A_03a", "TR04_ST1A_03a");
        emitExample(htm, M125, "Example TR04_ST1A_03b", "TR04_ST1A_03b");
        emitExample(htm, M125, "Example TR04_ST1A_04a", "TR04_ST1A_04a");
        emitExample(htm, M125, "Example TR04_ST1A_04b", "TR04_ST1A_04b");
        emitExample(htm, M125, "Example TR04_ST1A_05a", "TR04_ST1A_05a");
        emitExample(htm, M125, "Example TR04_ST1A_05b", "TR04_ST1A_05b");
        emitExample(htm, M125, "Example TR04_ST1A_05c", "TR04_ST1A_05c");
        emitExample(htm, M125, "Example TR04_ST1A_05d", "TR04_ST1A_05d");
        emitExample(htm, M125, "Example TR04_ST1A_05e", "TR04_ST1A_05e");
        emitExample(htm, M125, "Example TR04_ST1A_05f", "TR04_ST1A_05f");
        emitExample(htm, M125, "Example TR04_ST1B_01", "TR04_ST1B_01");
        emitExample(htm, M125, "Example TR04_ST1B_02", "TR04_ST1B_02");
        emitExample(htm, M125, "Example TR04_ST1B_03a", "TR04_ST1B_03a");
        emitExample(htm, M125, "Example TR04_ST1B_03b", "TR04_ST1B_03b");
        emitExample(htm, M125, "Example TR04_ST1B_04a", "TR04_ST1B_04a");
        emitExample(htm, M125, "Example TR04_ST1B_04b", "TR04_ST1B_04b");
        emitExample(htm, M125, "Example TR04_ST1B_05a", "TR04_ST1B_05a");
        emitExample(htm, M125, "Example TR04_ST1B_05b", "TR04_ST1B_05b");
        emitExample(htm, M125, "Example TR04_ST1B_05c", "TR04_ST1B_05c");
        emitExample(htm, M125, "Example TR04_ST1B_05d", "TR04_ST1B_05d");
        emitExample(htm, M125, "Example TR04_ST1B_05e", "TR04_ST1B_05e");
        emitExample(htm, M125, "Example TR04_ST1B_05f", "TR04_ST1B_05f");
        emitExample(htm, M125, "Example TR04_ST1B_06", "TR04_ST1B_06");
        emitExample(htm, M125, "Example TR04_ST1B_07", "TR04_ST1B_07");
        emitExample(htm, M125, "Example TR04_ST1C_01", "TR04_ST1C_01");
        emitExample(htm, M125, "Example TR04_ST1D_01", "TR04_ST1D_01");
        emitExample(htm, M125, "Example TR04_ST1D_02", "TR04_ST1D_02");
        emitExample(htm, M125, "Example TR04_ST1D_03a", "TR04_ST1D_03a");
        emitExample(htm, M125, "Example TR04_ST1D_03b", "TR04_ST1D_03b");
        emitExample(htm, M125, "Example TR04_ST1D_04a", "TR04_ST1D_04a");
        emitExample(htm, M125, "Example TR04_ST1D_04b", "TR04_ST1D_04b");
        emitExample(htm, M125, "Example TR04_ST1D_05a", "TR04_ST1D_05a");
        emitExample(htm, M125, "Example TR04_ST1D_05b", "TR04_ST1D_05b");
        emitExample(htm, M125, "Example TR04_ST1D_05c", "TR04_ST1D_05c");
        emitExample(htm, M125, "Example TR04_ST1D_05d", "TR04_ST1D_05d");
        emitExample(htm, M125, "Example TR04_ST1D_05e", "TR04_ST1D_05e");
        emitExample(htm, M125, "Example TR04_ST1D_05f", "TR04_ST1D_05f");
        emitExample(htm, M125, "Example TR04_ST1E_01", "TR04_ST1E_01");
        emitExample(htm, M125, "Example TR04_ST1E_02", "TR04_ST1E_02");
        emitExample(htm, M125, "Example TR04_ST1E_03a", "TR04_ST1E_03a");
        emitExample(htm, M125, "Example TR04_ST1E_03b", "TR04_ST1E_03b");
        emitExample(htm, M125, "Example TR04_ST1E_04a", "TR04_ST1E_04a");
        emitExample(htm, M125, "Example TR04_ST1E_04b", "TR04_ST1E_04b");
        emitExample(htm, M125, "Example TR04_ST1E_05a", "TR04_ST1E_05a");
        emitExample(htm, M125, "Example TR04_ST1E_05b", "TR04_ST1E_05b");
        emitExample(htm, M125, "Example TR04_ST1E_05c", "TR04_ST1E_05c");
        emitExample(htm, M125, "Example TR04_ST1E_05d", "TR04_ST1E_05d");
        emitExample(htm, M125, "Example TR04_ST1E_05e", "TR04_ST1E_05e");
        emitExample(htm, M125, "Example TR04_ST1E_05f", "TR04_ST1E_05f");
        emitExample(htm, M125, "Example TR04_ST1E_06", "TR04_ST1E_06");
        emitExample(htm, M125, "Example TR04_ST1E_07", "TR04_ST1E_07");
        emitExample(htm, M125, "Exploration TR04_ST1E_EXP_01", "TR04_ST1E_EXP_01");

        htm.add("<strong>Learning Target 4.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR04_ST2A_01", "TR04_ST2A_01");
        emitExample(htm, M125, "Example TR04_ST2B_01", "TR04_ST2B_01");
        emitExample(htm, M125, "Example TR04_ST2B_02", "TR04_ST2B_02");
        emitExample(htm, M125, "Example TR04_ST2B_03", "TR04_ST2B_03");
        emitExample(htm, M125, "Example TR04_ST2B_04", "TR04_ST2B_04");
        emitExample(htm, M125, "Example TR04_ST2B_05", "TR04_ST2B_05");
        emitExample(htm, M125, "Example TR04_ST2C_01", "TR04_ST2C_01");

        htm.add("<strong>Learning Target 4.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR04_ST3A_01", "TR04_ST3A_01");
        emitExample(htm, M125, "Example TR04_ST3A_02", "TR04_ST3A_02");
        emitExample(htm, M125, "Example TR04_ST3A_03", "TR04_ST3A_03");
        emitExample(htm, M125, "Application TR04_ST3B_APP_01", "TR04_ST3B_APP_01");
        emitExample(htm, M125, "Application TR04_ST3B_APP_02", "TR04_ST3B_APP_02");
        emitExample(htm, M125, "Application TR04_ST3B_APP_03", "TR04_ST3B_APP_03");
        emitExample(htm, M125, "Application TR04_ST3B_APP_04", "TR04_ST3B_APP_04");
        emitExample(htm, M125, "Example TR04_ST3C_01", "TR04_ST3C_01");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 5: Right Triangles").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR05_SR1_01", "TR05_SR1_01");
        emitExample(htm, M125, "Example TR05_SR1_02", "TR05_SR1_02");
        emitExample(htm, M125, "Example TR05_SR1_03", "TR05_SR1_03");
        emitExample(htm, M125, "Example TR05_SR2_01", "TR05_SR2_01");
        emitExample(htm, M125, "Example TR05_SR3_01", "TR05_SR3_01");
        emitExample(htm, M125, "Example TR05_SR4_01", "TR05_SR4_01");
        emitExample(htm, M125, "Example TR05_SR5_01", "TR05_SR5_01");

        htm.add("<strong>Learning Target 5.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR06_1_F01_01", "TR05_ST1A_01");
        emitExample(htm, M125, "Example TR06_1_F02_01", "TR05_ST1A_02");
        emitExample(htm, M125, "Example TR06_1_F03_01", "TR05_ST1B_01");
        emitExample(htm, M125, "Example TR06_1_F04_01", "TR05_ST1C_01");
        emitExample(htm, M125, "Example TR06_1_F05_01", "TR05_ST1C_02");
        emitExample(htm, M125, "Example TR06_1_F06_01", "TR05_ST1C_03");

        htm.add("<strong>Learning Target 5.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR05_ST2A_01", "TR05_ST2A_01");
        emitExample(htm, M125, "Example TR05_ST2A_02", "TR05_ST2A_02");
        emitExample(htm, M125, "Example TR05_ST2B_01", "TR05_ST2B_01");
        emitExample(htm, M125, "Example TR05_ST2B_02", "TR05_ST2B_02");
        emitExample(htm, M125, "Example TR05_ST2C_01", "TR05_ST2C_01");
        emitExample(htm, M125, "Example TR05_ST2C_02", "TR05_ST2C_02");
        emitExample(htm, M125, "Example TR05_ST2D_01", "TR05_ST2D_01");
        emitExample(htm, M125, "Application TR05_ST2D_APP_01a", "TR05_ST2D_APP_01a");
        emitExample(htm, M125, "Application TR05_ST2D_APP_01c", "TR05_ST2D_APP_01c");
        emitExample(htm, M125, "Application TR05_ST2D_APP_02a", "TR05_ST2D_APP_02a");
        emitExample(htm, M125, "Application TR05_ST2D_APP_02b", "TR05_ST2D_APP_02b");
        emitExample(htm, M125, "Application TR05_ST2D_APP_03a", "TR05_ST2D_APP_03a");
        emitExample(htm, M125, "Application TR05_ST2D_APP_03b", "TR05_ST2D_APP_03b");
        emitExample(htm, M125, "Application TR05_ST2D_APP_05a", "TR05_ST2D_APP_05a");
        emitExample(htm, M125, "Application TR05_ST2D_APP_06a", "TR05_ST2D_APP_06a");

        htm.add("<strong>Learning Target 5.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR05_ST3A_01", "TR05_ST3A_01");
        emitExample(htm, M125, "Example TR05_ST3A_02", "TR05_ST3A_02");
        emitExample(htm, M125, "Example TR05_ST3A_03", "TR05_ST3A_03");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 6: Inverse Trigonometric Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR06_SR1_01", "TR06_SR1_01");
        emitExample(htm, M125, "Example TR06_SR1_02", "TR06_SR1_02");
        emitExample(htm, M125, "Example TR06_SR2_01", "TR06_SR2_01");
        emitExample(htm, M125, "Example TR06_SR2_02", "TR06_SR2_02");
        emitExample(htm, M125, "Example TR06_SR3_01", "TR06_SR3_01");
        emitExample(htm, M125, "Example TR06_SR3_02", "TR06_SR3_02");
        emitExample(htm, M125, "Example TR06_SR4_01", "TR06_SR4_01");
        emitExample(htm, M125, "Example TR06_SR5_01", "TR06_SR5_01");
        emitExample(htm, M125, "Example TR06_SR6_01", "TR06_SR6_01");
        emitExample(htm, M125, "Example TR06_SR7_01", "TR06_SR7_01");

        htm.add("<strong>Learning Target 6.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR06_ST1A_01", "TR06_ST1A_01");
        emitExample(htm, M125, "Example TR06_ST1B_01", "TR06_ST1B_01");
        emitExample(htm, M125, "Example TR06_ST1C_01", "TR06_ST1C_01");
        emitExample(htm, M125, "Example TR06_ST1D_01", "TR06_ST1D_01");
        emitExample(htm, M125, "Example TR06_ST1E_01", "TR06_ST1E_01");
        emitExample(htm, M125, "Example TR06_ST1E_02", "TR06_ST1E_02");
        emitExample(htm, M125, "Example TR06_ST1E_03", "TR06_ST1E_03");
        emitExample(htm, M125, "Example TR06_ST1E_04", "TR06_ST1E_04");
        emitExample(htm, M125, "Example TR06_ST1E_05", "TR06_ST1E_05");

        htm.add("<strong>Learning Target 6.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR06_ST2A_01", "TR06_ST2A_01");
        emitExample(htm, M125, "Example TR06_ST2A_02", "TR06_ST2A_02");
        emitExample(htm, M125, "Example TR06_ST2A_03", "TR06_ST2A_03");
        emitExample(htm, M125, "Example TR06_ST2B_01", "TR06_ST2B_01");
        emitExample(htm, M125, "Example TR06_ST2C_01", "TR06_ST2C_01");
        emitExample(htm, M125, "Example TR06_ST2C_02", "TR06_ST2C_02");
        emitExample(htm, M125, "Example TR06_ST2C_03", "TR06_ST2C_03");

        htm.add("<strong>Learning Target 6.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR06_ST3A_01", "TR06_ST3A_01");
        emitExample(htm, M125, "Example TR06_ST3A_02", "TR06_ST3A_02");
        emitExample(htm, M125, "Example TR06_ST3A_03", "TR06_ST3A_03");
        emitExample(htm, M125, "Application TR06_ST3A_APP_01", "TR06_ST3A_APP_01");
        emitExample(htm, M125, "Application TR06_ST3A_APP_02", "TR06_ST3A_APP_02");
        emitExample(htm, M125, "Application TR06_ST3A_APP_03", "TR06_ST3A_APP_03");
        emitExample(htm, M125, "Application TR06_ST3A_APP_04", "TR06_ST3A_APP_04");
        emitExample(htm, M125, "Application TR06_ST3A_APP_05", "TR06_ST3A_APP_05");
        emitExample(htm, M125, "Example TR06_ST3B_01", "TR06_ST3B_01");
        emitExample(htm, M125, "Application TR06_ST3B_APP_01", "TR06_ST3B_APP_01");
        emitExample(htm, M125, "Example TR06_ST3C_01", "TR06_ST3C_01");
        emitExample(htm, M125, "Example TR06_ST3C_02", "TR06_ST3C_02");
        emitExample(htm, M125, "Example TR06_ST3C_03", "TR06_ST3C_03");
        emitExample(htm, M125, "Example TR06_ST3C_04", "TR06_ST3C_04");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 7: Law of Sines and Law of Cosines").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR07_SR1_01", "TR07_SR1_01");
        emitExample(htm, M125, "Example TR07_SR2_01", "TR07_SR2_01");
        emitExample(htm, M125, "Example TR07_SR3_01", "TR07_SR3_01");
        emitExample(htm, M125, "Example TR07_SR4_01", "TR07_SR4_01");
        emitExample(htm, M125, "Example TR07_SR5_01", "TR07_SR5_01");
        emitExample(htm, M125, "Example TR07_SR6_01", "TR07_SR6_01");
        emitExample(htm, M125, "Example TR07_SR7_01", "TR07_SR7_01");

        htm.add("<strong>Learning Target 7.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR07_ST1A_01", "TR07_ST1A_01");
        emitExample(htm, M125, "Example TR07_ST1B_01", "TR07_ST1B_01");
        emitExample(htm, M125, "Example TR07_ST1B_02", "TR07_ST1B_02");
        emitExample(htm, M125, "Example TR07_ST1C_01", "TR07_ST1C_01");
        emitExample(htm, M125, "Example TR07_ST1C_02", "TR07_ST1C_02");
        emitExample(htm, M125, "Example TR07_ST1C_03", "TR07_ST1C_03");

        htm.add("<strong>Learning Target 7.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR07_ST2A_01", "TR07_ST2A_01");
        emitExample(htm, M125, "Exploration TR07_ST2A_EXP_01", "TR07_ST2A_EXP_01");
        emitExample(htm, M125, "Example TR07_ST2B_01", "TR07_ST2B_01");
        emitExample(htm, M125, "Example TR07_ST2C_01", "TR07_ST2C_01");

        htm.add("<strong>Learning Target 7.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR07_ST3A_01", "TR07_ST3A_01");
        emitExample(htm, M125, "Example TR07_ST3A_02", "TR07_ST3A_02");
        emitExample(htm, M125, "Example TR07_ST3A_03", "TR07_ST3A_03");
        emitExample(htm, M125, "Example TR07_ST3A_04", "TR07_ST3A_04");
        emitExample(htm, M125, "Example TR07_ST3A_05", "TR07_ST3A_05");
        emitExample(htm, M125, "Example TR07_ST3A_06", "TR07_ST3A_06");
        emitExample(htm, M125, "Application TR07_ST3A_APP_01", "TR07_ST3A_APP_01");
        emitExample(htm, M125, "Application TR07_ST3A_APP_02", "TR07_ST3A_APP_02");
        emitExample(htm, M125, "Application TR07_ST3A_APP_03", "TR07_ST3A_APP_03");
        emitExample(htm, M125, "Application TR07_ST3A_APP_04", "TR07_ST3A_APP_04");
        emitExample(htm, M125, "Example TR07_ST3B_01", "TR07_ST3B_01");
        emitExample(htm, M125, "Example TR07_ST3B_02", "TR07_ST3B_02");
        emitExample(htm, M125, "Example TR07_ST3B_03", "TR07_ST3B_03");
        emitExample(htm, M125, "Example TR07_ST3B_04", "TR07_ST3B_04");
        emitExample(htm, M125, "Application TR07_ST3B_APP_01", "TR07_ST3B_APP_01");
        emitExample(htm, M125, "Application TR07_ST3B_APP_02", "TR07_ST3B_APP_02");
        emitExample(htm, M125, "Application TR07_ST3B_APP_03", "TR07_ST3B_APP_03");
        emitExample(htm, M125, "Application TR07_ST3B_APP_04", "TR07_ST3B_APP_04");
        emitExample(htm, M125, "Application TR07_ST3B_APP_05", "TR07_ST3B_APP_05");

        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 8: Vectors and Trigonometry").eH(4);

        htm.sDiv("indent22");
        htm.add("<strong>Skills Review</strong>").br().addln();
        emitExample(htm, M125, "Example TR08_SR1_01", "TR08_SR1_01");
        emitExample(htm, M125, "Example TR08_SR2_01", "TR08_SR2_01");
        emitExample(htm, M125, "Example TR08_SR3_01", "TR08_SR3_01");
        emitExample(htm, M125, "Example TR08_SR4_01", "TR08_SR4_01");
        emitExample(htm, M125, "Example TR08_SR5_01", "TR08_SR5_01");
        emitExample(htm, M125, "Example TR08_SR6_01", "TR08_SR6_01");

        htm.add("<strong>Learning Target 9.1</strong>").br().addln();
        emitExample(htm, M125, "Example TR08_ST1A_01", "TR08_ST1A_01");
        emitExample(htm, M125, "Example TR08_ST1B_01", "TR08_ST1B_01");
        emitExample(htm, M125, "Application TR08_ST1B_APP_01", "TR08_ST1B_APP_01");
        emitExample(htm, M125, "Example TR08_ST1C_01", "TR08_ST1C_01");
        emitExample(htm, M125, "Example TR08_ST1D_01", "TR08_ST1D_01");
        emitExample(htm, M125, "Exploration TR08_ST1D_EXP_01", "TR08_ST1D_EXP_01");

        htm.add("<strong>Learning Target 9.2</strong>").br().addln();
        emitExample(htm, M125, "Example TR08_ST2A_01", "TR08_ST2A_01");
        emitExample(htm, M125, "Example TR08_ST2A_02", "TR08_ST2A_02");
        emitExample(htm, M125, "Example TR08_ST2B_02", "TR08_ST2B_02");
        emitExample(htm, M125, "Example TR08_ST2C_01", "TR08_ST2C_01");

        htm.add("<strong>Learning Target 9.3</strong>").br().addln();
        emitExample(htm, M125, "Example TR08_ST3A_01", "TR08_ST3A_01");
        emitExample(htm, M125, "Example TR08_ST3B_01", "TR08_ST3B_01");
        emitExample(htm, M125, "Example TR08_ST3C_01", "TR08_ST3C_01");
        emitExample(htm, M125, "Example TR08_ST3C_02", "TR08_ST3C_02");

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
        htm.sH(4).add("Module 1: Fundamental Trigonometric Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 2: Sum and Difference Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 3: Multiple- and Half-Angle Identities").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 5: Triangles and Composition").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 4: Trigonometric Equations").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 6: Polar Coordinates").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 7: Polar Functions").eH(4);

        htm.sDiv("indent22");
        htm.add("(Not yet designed.)");
        htm.eDiv();

        htm.div("vgap");
        htm.sH(4).add("Module 8: Complex Numbers").eH(4);

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
