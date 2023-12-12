package dev.mathops.web.site.course;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataActivity;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the content of a single unit within a course, including all standards and activities in the course.
 * Status data is integrated into the page, but can be accessed in summary form from a separate page.
 *
 * <p>
 * It is assumed that this page can only be accessed by someone who has passed the user's exam and has legitimate access
 * to the e-text. This page does not check those conditions.
 */
enum PageStdsTextExploration {
    ;

    /** The directory for MATH 125 content on the media server. */
    private static final String M125_DIR = "M125";

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param site     the owning site
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ESiteType siteType, final CourseSite site,
                      final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String exploration = req.getParameter("exploration");
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(exploration)
                || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  exploration='", exploration, "'");
            Log.warning("  mode='", mode, "'");
            PageError.doGet(cache, site, req, resp, session,
                    "No course, exploration, and mode provided for course exploration page");
        } else if (course == null || exploration == null || mode == null) {
            PageError.doGet(cache, site, req, resp, session,
                    "No course, exploration, and mode provided for course exploration page");
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv(null, "style='padding:0 10pt 10pt 10pt;'");

            showCourseExploration(siteType, site, session, logic, course, exploration, mode, htm);

            htm.eDiv(); // padding
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Creates the HTML of the course exploration.
     *
     * @param siteType the site type
     * @param site     the owning site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param courseId the course for which to generate the status page
     * @param unit     the unit number
     * @param mode     the mode - one of "course" (normal access), "practice" (not in course, just using e-text),
     *                 "locked" (in course, but past all deadlines so practice access only)
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void showCourseExploration(final ESiteType siteType, final CourseSite site,
                                              final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                              final String courseId,
                                              final String unit, final String mode, final HtmlBuilder htm) {

        final SiteData data = logic.data;
        final RawStcourse reg = data.registrationData.getRegistration(courseId);

        if (reg == null) {
            htm.sP().add("ERROR: Unable to find course registration.").eP();
        } else {
            final SiteDataCfgCourse courseCfg = data.courseData.getCourse(courseId, reg.sect);

            // Course title
            htm.sDiv(null, "style='font-family:Serif; font-size:1.6rem; font-weight:500;",
                    "color:#333; margin-bottom:.1em; padding-left:.1em;'");

            if ("Y".equals(courseCfg.courseSection.courseLabelShown)) {
                htm.add(courseCfg.course.courseLabel);
                htm.add(": ");
            }
            htm.add(courseCfg.course.courseName);
            htm.eDiv();

            // This is essentially a table of contents for the e-text, with student-specific status
            // on each entry, and selected enable/disable.

            if (RawRecordConstants.MATH125.equals(reg.course)) {
                doM125Unit(siteType, site, session, logic, unit, mode, htm);
            } else if (RawRecordConstants.MATH126.equals(reg.course)) {
                doM126Unit(siteType, site, session, logic, unit, mode, htm);
            }
        }
    }

    /**
     * Generates the MATH 125 course outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param unit     the unit
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit(final ESiteType siteType, final CourseSite site,
                                   final ImmutableSessionInfo session, final CourseSiteLogic logic, final String unit,
                                   final String mode, final HtmlBuilder htm) {

        if ("1".equals(unit)) {
            doM125Unit1(siteType, site, session, logic, mode, htm);
        } else if ("2".equals(unit)) {
            doM125Unit2(siteType, site, session, logic, mode, htm);
        } else if ("3".equals(unit)) {
            doM125Unit3(siteType, site, session, logic, mode, htm);
        } else if ("4".equals(unit)) {
            doM125Unit4(siteType, site, session, logic, mode, htm);
        } else if ("5".equals(unit)) {
            doM125Unit5(siteType, site, session, logic, mode, htm);
        } else if ("6".equals(unit)) {
            doM125Unit6(siteType, site, session, logic, mode, htm);
        } else if ("7".equals(unit)) {
            doM125Unit7(siteType, site, session, logic, mode, htm);
        } else if ("8".equals(unit)) {
            doM125Unit8(siteType, site, session, logic, mode, htm);
        } else if ("9".equals(unit)) {
            doM125Unit9(siteType, site, session, logic, mode, htm);
        } else if ("10".equals(unit)) {
            doM125Unit10(siteType, site, session, logic, mode, htm);
        } else {
            htm.sP().add("ERROR: Invalid unit number.").eP();
        }
    }

    /**
     * Generates the MATH 125 unit 1 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit1(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        final SiteDataActivity activity = logic.data.activityData;
        final List<RawStexam> exams = activity.getStudentExams(RawRecordConstants.M125);
        final List<RawSthomework> homeworks = activity.getStudentHomeworks(RawRecordConstants.M125);

        emitUnitTitle(htm, "Unit 1 &ndash; <strong style='color:#196F43'>Angle Measure and Right Triangles</strong>");

        // Skills Review

        final boolean triedSR1 = hasAttemptedHw(homeworks, "SR41H");
        final boolean passedSR1 = triedSR1 && hasPassedHw(homeworks, "SR41H");

        htm.addln("<details style='padding-left:20px;'>");

        emitStandardAssignment(htm, M125_DIR, "1", "Skills Review Assignment", "SR41H", mode, triedSR1, passedSR1);

        htm.sDiv(null, "style='padding-left:20px; color:black;'");

        if (triedSR1) {
            if (passedSR1) {
                htm.sP().add("You have already passed this assignment - you may access ",
                        "the three content standards in this unit.").eP();
                htm.div("vgap");
            }
        } else {
            htm.sP().add("We recommend that you try the Skills Review assignment first, and only ",
                    "work through the review materials you need after that attempt.").eP();
            htm.div("vgap");
        }

        htm.sDiv(null, "style='border:1px solid black;padding:6px 6px 0 6px;background:#F5F5F5;'");
        htm.sH(3).add("Review Materials").eH(3);

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Review Topic: Unit Conversions",
                new String[]{"SR41_01_01"}, new String[]{"Multi-step Unit Conversion"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Review Topic: Addition and Subtraction with Fractions",
                new String[]{"SR41_02_01", "SR41_02_02", "SR41_02_03"},
                new String[]{"Addition of fractions",
                        "Addition of fractions including variables",
                        "Subtraction of fractions including variables"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Review Topic: Multiplication and Division with Fractions",
                new String[]{"SR41_03_01", "SR41_03_02"},
                new String[]{"Multiplication of fractions",
                        "Division of fractions"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Review Topic: Evaluating and Manipulating Square Roots",
                new String[]{"SR41_04_01", "SR41_04_02"},
                new String[]{"Simplifying a rational expression",
                        "Evaluating a rational expression with variable"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Review Topic: Exponents and Distribution",
                new String[]{"SR41_05_01", "SR41_05_02"},
                new String[]{"Properties of arithmetic with expressions",
                        "Cubing a binomial"});
        htm.eDiv(); // border

        htm.eDiv(); // padding-left
        htm.addln("</details>");  // End of Skills Review section

        // Standard 41.1

        final boolean triedS1 = hasAttemptedHw(homeworks, "S411H");
        final boolean passedS1 = triedS1 && hasPassedHw(homeworks, "S411H");

        htm.addln("<details style='padding-left:20px;'>");
        emitStandardAssignment(htm, M125_DIR, "1", "Standard 1.1", "S411H", mode, triedS1, passedS1);

        htm.sDiv(null, "style='padding-left:20px; color:black;'");

        // TODO: Print Standard Objectives

        htm.sDiv(null, "style='border:1px solid black;padding:6px 6px 0 6px;background:#F5F5F5;'");
        htm.sH(3).add("Lecture and Examples").eH(3);

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Recognizing congruent, complementary, and supplementary angles",
                new String[]{"ST41_1_F01_01"},
                new String[]{"Identify congruent complementary and supplementary angles in a diagram"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Computing complement or supplement of angles",
                new String[]{"ST41_1_F02_01"},
                new String[]{"Calculate complement and supplement in both degrees and radians"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Interpreting degree and radian measure",
                new String[]{"ST41_1_F03_01"},
                new String[]{"Interpret degree or radian measure in terms of complete rotations"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Unit conversion: degrees to radians",
                new String[]{"ST41_1_F04_01"},
                new String[]{"Convert from degree to radian measure"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Unit conversion: radians to degrees",
                new String[]{"ST41_1_F05_01"},
                new String[]{"Convert from radian to degree measure"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Estimating angle measure",
                new String[]{"ST41_1_F06_01"},
                new String[]{"Estimating an angle measure in both degrees and radians"});
        htm.eDiv(); // border

        htm.eDiv(); // padding-left
        htm.addln("</details>");  // End of Standard 41.1

        // Standard 41.2

        final boolean triedS2 = hasAttemptedHw(homeworks, "S412H");
        final boolean passedS2 = triedS2 && hasPassedHw(homeworks, "S412H");

        htm.addln("<details style='padding-left:20px;'>");
        emitStandardAssignment(htm, M125_DIR, "1", "Standard 1.2", "S412H", mode, triedS2, passedS2);

        htm.sDiv(null, "style='padding-left:20px; color:black;'");

        // TODO: Print Standard Objectives

        htm.sDiv(null, "style='border:1px solid black;padding:6px 6px 0 6px;background:#F5F5F5;'");
        htm.sH(3).add("Lecture and Examples").eH(3);

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Locate angles in standard position and identify quadrants",
                new String[]{"ST41_2_F01_01"},
                new String[]{"Sketch angles in each quadrant from their measure."});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Identify co-terminal angles in degrees",
                new String[]{"ST41_2_F02_01"},
                new String[]{"Identify angles co-terminal to an angle given in degrees"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Identify co-terminal angles in radians",
                new String[]{"ST41_2_F03_01"},
                new String[]{"Identify angles co-terminal to an angle given in radians"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Compute co-terminal angles in degrees",
                new String[]{"ST41_2_F04_01"},
                new String[]{"Find an angle in a requested range co-terminal to a given angle"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Compute co-terminal angles in radians",
                new String[]{"ST41_2_F05_01"},
                new String[]{"Find an angle in a requested range co-terminal to a given angle"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Finding reference angles in each quadrant in degrees",
                new String[]{"ST41_2_F06_01"},
                new String[]{
                        "Given angles in degrees, identify their quadrant and calculate their reference angle"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Finding reference angles in each quadrant in radians",
                new String[]{"ST41_2_F07_01"},
                new String[]{"Given angles in radians, identify their quadrant and calculate their reference angle"});
        htm.eDiv(); // border

        htm.eDiv(); // padding-left
        htm.addln("</details>");  // End of Standard 41.2

        // Standard 41.3

        final boolean triedS3 = hasAttemptedHw(homeworks, "S413H");
        final boolean passedS3 = triedS3 && hasPassedHw(homeworks, "S413H");

        htm.addln("<details style='padding-left:20px;'>");
        emitStandardAssignment(htm, M125_DIR, "1", "Standard 1.3", "S413H", mode, triedS3, passedS3);

        htm.sDiv(null, "style='padding-left:20px; color:black;'");

        // TODO: Print Standard Objectives

        htm.sDiv(null, "style='border:1px solid black;padding:6px 6px 0 6px;background:#F5F5F5;'");
        htm.sH(3).add("Lecture and Examples").eH(3);

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "The Pythagorean Theorem",
                new String[]{"ST41_3_F01_01"},
                new String[]{"Calculate length of one side in a right triangle given the other two"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Diagonals of Rectangles",
                new String[]{"ST41_3_F02_01"},
                new String[]{"Calculate the length of the diagonal of a rectangle of know size"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Measures of interior angles of a triangle sum to 180&deg;",
                new String[]{"ST41_3_F03_01"},
                new String[]{"Find degree measure of one angle in a triangle given the other two"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Measures of interior angles of a triangle sum to 2&pi; radians",
                new String[]{"ST41_3_F04_01"},
                new String[]{"Find radian measure of one angle in a triangle given the other two"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Identifying similar triangles",
                new String[]{"ST41_3_F05_01"},
                new String[]{"Find all triangles in a diagram similar to a specified triangle"});

        emitExampleBlock(htm, M125_DIR, RawRecordConstants.M125, mode,
                "Use similarity relationships to calculate triangle side lengths",
                new String[]{"ST41_3_F06_01"},
                new String[]{"Use proportion and similarity to calculate side length in triangle"});
        htm.eDiv(); // border

        htm.eDiv(); // padding-left
        htm.addln("</details>");  // End of Standard 41.3

    }

    /**
     * Generates the MATH 125 unit 2 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit2(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 2: The Unit Circle");
    }

    /**
     * Generates the MATH 125 unit 3 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit3(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 3: The Trigonometric Functions");
    }

    /**
     * Generates the MATH 125 unit 4 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit4(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 4: Transformations of Trigonometric Functions");
    }

    /**
     * Generates the MATH 125 unit 5 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit5(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 5: Modeling with Trigonometric Functions");
    }

    /**
     * Generates the MATH 125 unit 6 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit6(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 6: Trigonometric Functions in Right Triangles");
    }

    /**
     * Generates the MATH 125 unit 7 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit7(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 7: Inverse Trigonometric Functions");
    }

    /**
     * Generates the MATH 125 unit 8 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit8(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 8: Triangles, the Law of Sines, and the Law of Cosines");
    }

    /**
     * Generates the MATH 125 unit 9 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit9(final ESiteType siteType, final CourseSite site,
                                    final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                    final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 9: Vectors and Trigonometry");
    }

    /**
     * Generates the MATH 125 unit 10 outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM125Unit10(final ESiteType siteType, final CourseSite site,
                                     final ImmutableSessionInfo session, final CourseSiteLogic logic, final String mode,
                                     final HtmlBuilder htm) {

        emitUnitTitle(htm, "Unit 10: Applications of Trigonometric Functions");
    }

    /**
     * Generates the MATH 126 course outline.
     *
     * @param siteType the site type
     * @param site     the course site
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @param unit     the unit
     * @param mode     the mode ("course", "practice", or "locked")
     * @param htm      the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doM126Unit(final ESiteType siteType, final CourseSite site,
                                   final ImmutableSessionInfo session, final CourseSiteLogic logic, final String unit,
                                   final String mode, final HtmlBuilder htm) {

        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 1: Fundamental Trigonometric Identities");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 2: Sum and Difference Identities");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 3: Multiple-Angle and Half-Angle Identities");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 4: Trigionometric Equations");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 5: Applications of Trigonometric Equations");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 6: Imaginary and Complex Numbers");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 7: Polar Coordinates");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 8: Polar Functions");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 9: Cylindrical Coordinates");
        htm.addln("Todo...");
        htm.addln("</details>");

        htm.hr();
        htm.addln("<details>");
        emitUnitTitle(htm, "Unit 10: Spherical Coordinates");
        htm.addln("Todo...");
        htm.addln("</details>");
    }

    /**
     * Emits the unit title.
     *
     * @param htm   the {@code HtmlBuilder} to which to append the HTML
     * @param title the unit title
     */
    private static void emitUnitTitle(final HtmlBuilder htm, final String title) {

        htm.sDiv(null, "style='font-family:serif; font-weight:500; font-size:1.4rem;",
                "margin-bottom:.4em; padding-left:1em; color:#333;",
                "border-style:solid; border-color:#999; border-width:0 0 2px 0;'");
        htm.add(title);
        htm.eDiv();
    }

    /**
     * Emits a block of examples, each with links to the video and PDF representations. The arrays of titles and IDs
     * must be the same length.
     *
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @param courseDir the directory on the media server (nibbler) under which media files are located (typically of
     *                  the form "M###")
     * @param course    the course
     * @param mode      the mode
     * @param title     the title for the block
     * @param ids       the IDs (used to generate filenames for external files)
     * @param labels    a label for each example
     */
    private static void emitExampleBlock(final HtmlBuilder htm, final String courseDir,
                                         final String course, final String mode, final String title, final String[] ids,
                                         final String[] labels) {

        htm.sDiv().add("<strong>").add(title).add("</strong>").eDiv();

        final int count = Math.min(ids.length, labels.length);

        htm.sDiv(null, "style='margin:0 0 10px 20px;'");
        for (int i = 0; i < count; ++i) {
            htm.sP().add("Example ", Integer.toString(i + 1),
                    ": ", labels[i]).eP();

            htm.sDiv(null, "style='padding-left:40px;'");
            htm.add("<img src='/images/etext/pdf.png' alt='' ",
                            "style='padding-right:3px'/>",
                            "<a class='linkbtn' href='https://nibbler.math.colostate.edu/media/",
                            courseDir, "/pdf/", ids[i], ".pdf'>Example with Solution (PDF)</a>")
                    .add("<img src='/images/etext/video_icon.png' alt='' ",
                            "style='padding-left:20px;padding-right:3px'/>",
                            "<a class='linkbtn' href='video_example.html?dir=", courseDir,
                            "&id=", ids[i], "&course=", course, "&mode=", mode, "'>Video Walkthrough</a>");
            htm.eDiv();
        }
        htm.eDiv();
    }

    /**
     * Emits the title for a standard and the link to its assessment.
     *
     * @param htm        the {@code HtmlBuilder} to which to append the HTML
     * @param course     the course
     * @param unit       the unit
     * @param title      the title for the block
     * @param assignment the assignment ID
     * @param mode       the mode
     * @param attempted  true if the assessment has been attempted
     * @param mastered   true if the assessment has been mastered
     */
    private static void emitStandardAssignment(final HtmlBuilder htm, final String course,
                                               final String unit, final String title, final String assignment,
                                               final String mode, final boolean attempted, final boolean mastered) {

        htm.add("<summary style='font-family:serif;",
                "font-weight:500;font-size:1.3rem;color:#196F43;margin-bottom:.3em;'>");

        htm.addln(title);
        htm.addln("<form style='display:inline' method='get' action='run_homework.html'>");
        htm.addln("  <input type='hidden' name='course' value='", course, "'/>");
        htm.addln("  <input type='hidden' name='unit' value='", unit, "'/>");
        htm.addln("  <input type='hidden' name='lesson' value='0'/>");
        htm.addln("  <input type='hidden' name='coursemode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='mode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='assign' value='", assignment, "'/>");
        htm.addln("  <input class='smallbtn' type='submit' value='Start Assignment'/>");
        htm.addln("</form>");

        if (attempted) {
            if (mastered) {
                htm.addln(" <span class='why_done'>Mastered</span>");
            } else {
                htm.addln(" <span class='why_unavail'>Not Yet Mastered</span>");
            }
        } else {
            htm.addln(" <span class='why_unavail'>Not Yet Attempted</span>");
        }
        htm.addln("</summary>");
    }

    /**
     * Tests whether a student has attempted a homework by version.
     *
     * @param homeworks the list of all homeworks on record
     * @param version   the version
     * @return true if there is at least one attempt on record
     */
    private static boolean hasAttemptedHw(final Iterable<RawSthomework> homeworks, final String version) {

        boolean found = false;

        for (final RawSthomework rec : homeworks) {
            if (rec.version.equals(version)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Tests whether a student has passed a homework by version.
     *
     * @param homeworks the list of all homeworks on record
     * @param version   the version
     * @return true if the student has passed the homework
     */
    private static boolean hasPassedHw(final Iterable<RawSthomework> homeworks, final String version) {

        boolean found = false;

        for (final RawSthomework rec : homeworks) {
            if (rec.version.equals(version) && "Y".equals(rec.passed)) {
                found = true;
                break;
            }
        }

        return found;
    }

//    /**
//     * Tests whether a student has attempted an exam by version.
//     *
//     * @param exams   the list of all exams on record
//     * @param version the version
//     * @return true if there is at least one attempt on record
//     */
//    private static boolean hasAttemptedExam(final Iterable<? extends RawStexam> exams, final String version) {
//
//        boolean found = false;
//
//        for (final RawStexam rec : exams) {
//            if (rec.version.equals(version)) {
//                found = true;
//                break;
//            }
//        }
//
//        return found;
//    }

//    /**
//     * Tests whether a student has passed an exam by version.
//     *
//     * @param exams   the list of all exams on record
//     * @param version the version
//     * @return true if the student has passed the exam
//     */
//    private static boolean hasPassedExam(final Iterable<? extends RawStexam> exams, final String version) {
//
//        boolean found = false;
//
//        for (final RawStexam rec : exams) {
//            if (rec.version.equals(version) && "Y".equals(rec.passed)) {
//                found = true;
//                break;
//            }
//        }
//
//        return found;
//    }

//    /**
//     * Retrieves the highest passing exam score for the student on an exam.
//     *
//     * @param exams   the list of all exams on record
//     * @param version the version
//     * @return the highest passing exam score, -1 if exam has not yet been passed
//     */
//    private static int getHighestPassingExamScore(final Iterable<? extends RawStexam> exams, final String version) {
//
//        int highest = -1;
//
//        for (final RawStexam rec : exams) {
//            if (rec.version.equals(version) && "Y".equals(rec.passed)
//                    && rec.examScore != null) {
//                highest = Math.max(highest, rec.examScore.intValue());
//            }
//        }
//
//        return highest;
//    }
}
