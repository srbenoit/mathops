package dev.mathops.web.site.course;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.rawrecord.RawStexam;
import dev.mathops.db.rawrecord.RawSthomework;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataActivity;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.data.CourseData;
import dev.mathops.web.site.course.data.ExampleBlock;
import dev.mathops.web.site.course.data.ExampleData;
import dev.mathops.web.site.course.data.LearningTargetData;
import dev.mathops.web.site.course.data.MathCourses;
import dev.mathops.web.site.course.data.ModuleData;
import dev.mathops.web.site.course.data.SkillsReviewData;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This page shows the content of a single unit within a course, including all standards and activities in the course.
 * Status data is integrated into the page, but can be accessed in summary form from a separate page.
 *
 * <p>
 * It is assumed that this page can only be accessed by someone who has passed the user's exam and has legitimate access
 * to the e-text. This page does not check those conditions.
 */
enum PageStdsTextModule {
    ;

    /** A color. */
    private static final String CSU_GREEN = "#1E4D2B";

    /** A common string. */
    private static final String REVIEW_MATERIALS = "Review Materials";

    /** A common string. */
    private static final String REVIEW_TOPIC = "Review Topic: ";

    /** A common string. */
    private static final String LESSONS_AND_EXAMPLES = "Lessons and Examples";

    /** A pre-compiled regular expression pattern. */
    private static final Pattern SPC_PATTERN = Pattern.compile(CoreConstants.SPC);

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
        final String module = req.getParameter("module");
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(module)
                || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  module='", module, "'");
            Log.warning("  mode='", mode, "'");
            PageError.doGet(cache, site, req, resp, session,
                    "No course, module, and mode provided for course module page");
        } else if (course == null || module == null || mode == null) {
            PageError.doGet(cache, site, req, resp, session,
                    "No course, module, and mode provided for course module page");
        } else {
            try {
                final int modNumber = Integer.parseInt(module);

                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                        false, true);

                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv(null, "style='padding:0 10pt 10pt 10pt;'");

                showCourseModule(logic, course, modNumber, mode, htm);

                htm.eDiv(); // padding
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                        htm.toString().getBytes(StandardCharsets.UTF_8));
            } catch (final NumberFormatException ex) {
                PageError.doGet(cache, site, req, resp, session,
                        "Invalid module number provided for course module page");
            }
        }
    }

    /**
     * Creates the HTML of the course module.
     *
     * @param logic        the course site logic
     * @param courseId     the course for which to generate the status page
     * @param moduleNumber the unit number
     * @param mode         the mode - one of "course" (normal access), "practice" (not in course, just using e-text),
     *                     "locked" (in course, but past all deadlines so practice access only)
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void showCourseModule(final CourseSiteLogic logic, final String courseId,
                                         final int moduleNumber, final String mode, final HtmlBuilder htm) {

        final SiteData data = logic.data;
        final RawStcourse reg = data.registrationData.getRegistration(courseId);

        if (reg == null) {
            htm.sP().add("ERROR: Unable to find course registration.").eP();
        } else {
            final SiteDataCfgCourse courseCfg = data.courseData.getCourse(courseId, reg.sect);

            if (RawRecordConstants.MATH125.equals(reg.course)) {
                doModule(MathCourses.MATH_125, moduleNumber, logic, courseCfg, mode, htm);
            } else if (RawRecordConstants.MATH126.equals(reg.course)) {
                doModule(MathCourses.MATH_126, moduleNumber, logic, courseCfg, mode, htm);
            }
        }
    }

    /**
     * Generates a course outline.
     *
     * @param courseData   the course data
     * @param logic        the course site logic
     * @param courseCfg    the course configuration data
     * @param moduleNumber the module number
     * @param mode         the mode ("course", "practice", or "locked")
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doModule(final CourseData courseData, final int moduleNumber,
                                 final CourseSiteLogic logic, final SiteDataCfgCourse courseCfg, final String mode,
                                 final HtmlBuilder htm) {

        final SiteDataActivity activity = logic.data.activityData;
        final List<RawStexam> examList = activity.getStudentExams(courseData.courseId);
        final List<RawSthomework> hwList = activity.getStudentHomeworks(courseData.courseId);

        if (moduleNumber == 0) {
            doHowToNavigate(courseCfg, mode, htm);
        } else if (moduleNumber >= 1 && moduleNumber <= 10) {
            final ModuleData moduleData = courseData.modules.get(moduleNumber - 1);

            doModule(moduleData, courseCfg, examList, hwList, mode, htm);
        } else {
            htm.sP().add("ERROR: Invalid module number.").eP();
        }
    }

    /**
     * Generates a page with instructions on navigating the course.
     *
     * @param courseCfg the course configuration data
     * @param mode      the mode ("course", "practice", or "locked")
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doHowToNavigate(final SiteDataCfgCourse courseCfg, final String mode,
                                        final HtmlBuilder htm) {

        emitModuleTitle(htm, courseCfg, 0, "How to Successfully Navigate this Course",
                "navigation-thumb.png", courseCfg.course.course, mode);

        htm.sH(3).add("Completing the Course").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .addln("Passing this course requires that you do two things:").eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>Master at least 12 learning targets (out of 15) in the first half of ",
                "the course.</li>");
        htm.addln("<li>Master at least 12 learning targets (out of 15) in the second half of ",
                "the course.</li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-top:0;'")
                .add("If you achieve these two goals, you will pass the course.  The grade you earn ",
                        "will then be based on the number of points you have accumulated.")
                .eP();

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .add("There are 170 points possible (150 from learning targets, ",
                        "20 from explorations).");

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>153 (90%) points or higher earns an <strong>A</strong></li>");
        htm.addln("<li>136 (80%) to 152 points earns a <strong>B</strong></li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-top:0;'")
                .add("Any number of points less than 136, as long as you have mastered 12 learning ",
                        "targets in each half of the course, earns a <strong>C</strong>.  If you do not ",
                        "master 12 learning targets in each half of the course, a <strong>U</strong> ",
                        "grade will be recorded (a U grade does not affect GPA).")
                .eP();

        htm.hr();

        htm.sH(3).add("Assignments and Earning Points").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("The course is divided into ten Units, and two Explorations. ",
                        "Units have three learning targets each, for a total of 30 learning targets.")
                .eP();

        htm.sH(4).add("Skills Review").eH(4);

        htm.sDiv("indent");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("Each unit begins with a <b>Skills Review</b>.  This is a (hopefully) easy ",
                        "assignment to remind you of some skills you will need in the unit.  You have to ",
                        "complete this assignment before you can move on to the learning targets in the ",
                        "unit. Skills Review assignments do not earn points.")
                .eP();

        htm.eDiv();

        htm.sH(4).add("Learning Targets").eH(4);

        htm.sDiv("indent");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("Each learning target has a <b>Learning Target Assignment</b> that lets you ",
                        "practice the skills for that learning target.  You get unlimited tries on this ",
                        "assignment, and can practice as much as you want.  You must pass this assignment ",
                        "to \"unlock\" the learning target for mastery.  These assignments have no due ",
                        "dates and do not earn points.")
                .eP();

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0'")
                .add("Once you have unlocked a learning target for mastery, you can go to the ",
                        "Precalculus Center and take a <b>Mastery Exam</b> to demonstrate mastery of that ",
                        "learning target.")
                .eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>Mastering a learning target by its due date earns <b>5 points</b>.</li>");
        htm.addln("<li>Mastering a learning target after its due date earns <b>4 points</b>.</li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("You do not need to go to the Precalculus Center once for every learning ",
                        "target - you can complete several Learning Target assignments, then go to the ",
                        "Precalculus Center and your Mastery Exam will include all learning targets you are ",
                        "eligible for.  However, if you have 7 or more learning targets that have not yet ",
                        "been mastered, you will not be able to move on to the next Unit until you master ",
                        "some learning targets to get the number open below 7.  This is to prevent ",
                        "someone from leaving all the mastery exams until the end.")
                .eP();

        htm.eDiv();

        htm.sH(4).add("Explorations").eH(4);

        htm.sDiv("indent");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("Finally, the course has two Explorations (one after Unit 5, one after Unit 10).")
                .eP();

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0'").addln(
                "Explorations have an assignment to complete with a due date. ",
                "An exploration can either be scored as 'mastered' or 'attempted'.");

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>Mastery of an exploration by its due date earns <b>10 points</b>.");
        htm.addln("<li>Mastery of an exploration after its due date earns <b>8 points</b> ",
                "(<b>9</b> if it's less than 24 hours late).");
        htm.addln("<li>Attempting, but not mastering, an exploration by its due date earns ",
                "<b>5 points</b>.");
        htm.addln("<li>Attempting, but not mastering, an exploration after its due date earns ",
                "<b>4 points</b>.");
        htm.addln("</ul>");
        htm.eP();

        htm.eDiv();

        htm.hr();

        htm.sH(3).add("Strategies for Success").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .add("To succeed in this course, we recommend these core strategies:").eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln(
                "<li>Work ahead - never wait until a deadline to do work that's due that day.</li>");
        htm.addln("<li>Do a little work each day or every couple of days, rather than trying ",
                "to pack a lot of work into one day each week.  Schedule a regular time to work on ",
                "this course.</li>");
        htm.addln("<li>Give yourself time and space.  Time, so you don't feel rushed or panicked, ",
                "and a quiet study space where you can focus.</li>");
        htm.addln("<li>Use the resources provided.  Take advantage of in-person and online help ",
                "from the Precalculus Center.  Watch course videos and read the  solutions. ",
                "Use textbooks or Internet resources when something does not make sense.</li>");
        htm.addln("<li>If you start to get behind, reach out quickly and get back on track. ",
                "The Precalculus Center team wants to help you succeed!  Help us to help you by ",
                "bringing us in when you need it.</li>");
        htm.addln("</ul>");
    }

    /**
     * Generates a module outline.
     *
     * @param moduleData the module data
     * @param courseCfg  the course configuration data
     * @param examList   the list of student exams in the course
     * @param hwList     the list of student homeworks in the course
     * @param mode       the mode ("course", "practice", or "locked")
     * @param htm        the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doModule(final ModuleData moduleData, final SiteDataCfgCourse courseCfg,
                                 final Iterable<RawStexam> examList,
                                 final Iterable<RawSthomework> hwList, final String mode,
                                 final HtmlBuilder htm) {

        emitModuleTitle(htm, courseCfg, moduleData.moduleNumber, moduleData.moduleTitle,
                moduleData.thumbnailImage, moduleData.course.courseId, mode);

        startSkillsReview(htm, moduleData, hwList, mode);
        for (final ExampleBlock block : moduleData.skillsReview.exampleBlocks) {
            emitExampleBlock(htm, block, REVIEW_TOPIC);
        }
        endSkillsReview(htm);

        for (final LearningTargetData learningTarget : moduleData.learningTargets) {
            startLearningTarget(htm, learningTarget);
            for (final ExampleBlock block : learningTarget.exampleBlocks) {
                emitExampleBlock(htm, block, CoreConstants.EMPTY);
            }
            endLearningTarget(htm, learningTarget, mode, examList, hwList);
        }
    }

    /**
     * Emits the module title.
     *
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @param courseCfg    the course configuration
     * @param moduleNumber the module number
     * @param title        the module title
     * @param imgSource    the image source filename
     * @param course       the course ID
     * @param mode         the page mode
     */
    private static void emitModuleTitle(final HtmlBuilder htm, final SiteDataCfgCourse courseCfg,
                                        final int moduleNumber, final String title, final String imgSource,
                                        final CharSequence course, final String mode) {

        htm.sDiv("left");
        htm.addln("<img style='margin-right:16px; border:1px gray solid;' src='/www/images/etext/", imgSource, "'/>");
        htm.eDiv();

        htm.sH(2, "title");
        if ("Y".equals(courseCfg.courseSection.courseLabelShown)) {
            htm.add(courseCfg.course.courseLabel);
            htm.add(": ");
        }
        htm.sSpan(null, "style='color:#D9782D'").add(courseCfg.course.courseName).eSpan().br();

        htm.sSpan(null, "style='font-family:factoria-medium;sans-serif;",
                "font-weight:800;font-size:19px;color:", CSU_GREEN, ";'");
        if (moduleNumber > 0) {
            htm.add("Module ", Integer.toString(moduleNumber), "&nbsp;&ndash;&nbsp;");
        }
        htm.eSpan();

        htm.sSpan(null, "style='font-family:factoria-medium;sans-serif;",
                "font-weight:500;font-size:18px;color:", CSU_GREEN, ";'");
        htm.add(title);
        htm.eSpan().eH(2);

        final String course2 = SPC_PATTERN.matcher(course).replaceAll("%20");
        htm.sDiv("indent22");
        htm.addln("<a class='ulink' href='course_text.html?course=", course2,
                "&mode=", mode, "'>Return to E-text outline</a>");
        htm.eDiv();

        htm.div("clear").hr();
    }

    /**
     * Emits a "Skills Review" title and text that depends on whether the student has attempted (or passed) the skills
     * review assignment.
     *
     * @param htm        the {@code HtmlBuilder} to which to append the HTML
     * @param moduleData the module data
     * @param homeworks  the list of student homeworks
     * @param mode       the page mode
     */
    private static void startSkillsReview(final HtmlBuilder htm, final ModuleData moduleData,
                                          final Iterable<RawSthomework> homeworks, final String mode) {

        final SkillsReviewData data = moduleData.skillsReview;

        htm.sH(3).add("Skills Review").eH(3);

        final boolean tried = hasAttemptedHw(homeworks, data.assignmentId);
        final boolean passed = tried && hasPassedHw(homeworks, data.assignmentId);

        if (tried) {
            if (passed) {
                htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                        .add("You have already passed this assignment - you may access ",
                                "the three learning targets in this unit.")
                        .eP();
            }
        } else {
            htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                    .add("We recommend that you try the Skills Review assignment first, and only ",
                            "work through the review materials you need after that attempt.")
                    .eP();
        }

        emitStandardAssignment(htm, moduleData.course.courseId, data.moduleNumber,
                "Skills Review Assignment",
                data.assignmentId, mode, tried, passed);

        startDetailsBlock(htm, REVIEW_MATERIALS);
    }

    /**
     * Emits a "Learning Target #.#" title and a list of learning outcomes.
     *
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @param learningTarget the learning target data
     */
    private static void startLearningTarget(final HtmlBuilder htm,
                                            final LearningTargetData learningTarget) {

        htm.sH(3).add("Learning Target ", learningTarget.targetNumber).eH(3);

        htm.sDiv("learning_target").addln(learningTarget.mainOutcome);

        if (learningTarget.subOutcomes != null && learningTarget.subOutcomes.length > 0) {
            htm.addln("<ul>");
            for (final String item : learningTarget.subOutcomes) {
                htm.addln("<li>", item, "</li>");
            }
        }
        htm.eDiv();

        startDetailsBlock(htm, LESSONS_AND_EXAMPLES);
    }

    /**
     * Emits a block of examples, each with links to the video and PDF representations. The arrays of titles and IDs
     * must be the same length.
     *
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param block   the example block data
     * @param heading the heading for the example block, such as "Review Topic" or a blank string
     */
    private static void emitExampleBlock(final HtmlBuilder htm, final ExampleBlock block, final String heading) {

        htm.sDiv()
                .add("<img style='padding-right:8px;position:relative;top:-3px;margin-bottom:-3px;' ",
                        "src='/www/images/etext/orange_bullet.png'/><strong style='color:#777;'>",
                        heading, "<span style='color:", CSU_GREEN, ";'>",
                        block.title, "</span></strong>")
                .eDiv();

        final List<ExampleData> examples = block.examples;
        final int count = examples.size();

        htm.sDiv(null, "style='margin:0 0 8px 22px;'");
        for (int i = 0; i < count; ++i) {
            final ExampleData ex = examples.get(i);

            htm.sDiv(null, "style='font-size:15px;margin:6px 0 4px 0'")
                    .add("Example ", Integer.toString(i + 1), ": ", ex.label).eDiv();

            htm.sDiv(null, "style='font-size:15px;padding-left:22px;'");
            htm.add("<img src='/images/etext/pdf.png' alt='' ",
                            "style='padding-right:5px'/>",
                            "<a class='linkbtn' target='_blank' ",
                            "href='https://nibbler.math.colostate.edu/media/", block.course.mediaDir,
                            "/pdf/", ex.mediaId, ".pdf'>Example with Solution (PDF)</a>")
                    .add("<img src='/images/etext/video_icon.png' alt='' ",
                            "style='padding-left:20px;padding-right:5px'/>",
                            "<a class='linkbtn' target='_blank' href='video_example.html?dir=",
                            block.course.mediaDir, "&id=", ex.mediaId, "&course=", block.course.courseId,
                            "'>Video Walkthrough</a>");
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
    private static void emitStandardAssignment(final HtmlBuilder htm, final String course, final int unit,
                                               final String title, final String assignment, final String mode,
                                               final boolean attempted, final boolean mastered) {

        htm.add("<div style='font-family:prox-regular,sans-serif;",
                "font-weight:400;font-size:15px;color:#196F43;margin:0 0 .3em 16px;'>");

        htm.addln("<form style='display:inline' method='get' action='run_homework.html'>");
        htm.addln("  <input type='hidden' name='course' value='", course, "'/>");
        htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
        htm.addln("  <input type='hidden' name='lesson' value='0'/>");
        htm.addln("  <input type='hidden' name='coursemode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='mode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='assign' value='", assignment, "'/>");
        htm.addln("  <input class='smallbtn' type='submit' value='", title, "'/>");
        htm.addln("</form>");

        if (attempted) {
            if (mastered) {
                htm.addln(" <span style='background-color:#EBF9EB;color:#105456;",
                        "border:1px #105456 solid;border-radius:6px;padding:3px 19px;",
                        "margin-left:16px;'>Mastered</span>");
            } else {
                htm.addln(" <span style='color:#B00000;",
                        "border:1px #B00000 solid;border-radius:6px;padding:3px 19px;",
                        "margin-left:16px;'>Not Yet Mastered</span>");
            }
        } else {
            htm.addln(" <span style='color:#B00000;",
                    "border:1px #B00000 solid;border-radius:6px;padding:3px 19px;",
                    "margin-left:16px;'>Not Yet Attempted</span>");
        }
        htm.eDiv();
    }

    /**
     * Emits the start of a "details" block.
     *
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     * @param text the text for the expandable heading
     */
    private static void startDetailsBlock(final HtmlBuilder htm, final String text) {

        htm.addln("<details style='padding-left:20px;'>");

        htm.addln("<summary style='font-family:prox-regular,sans-serif;margin-bottom:6px;'>",
                text, " (expand with arrow on the left)", "</summary>");

        htm.sDiv(null, "style='padding-left:20px;line-height:1.1em;'");
        htm.sDiv(null, "style='font-family:prox-regular,sans-serif; color:black;",
                // "border:1px solid black;padding:6px 6px 0 6px;background:#eeecd3;'");
                "border:1px solid black;padding:6px 6px 0 6px;background:#f8f8f8;'");
    }

    /**
     * Emits the end of a Skills Review block.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void endSkillsReview(final HtmlBuilder htm) {

        htm.eDiv(); // font, border, colors
        htm.eDiv(); // padding-left
        htm.addln("</details>").hr();
    }

    /**
     * Emits the end of a "details" block.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void endDetailsBlock(final HtmlBuilder htm) {

        htm.eDiv(); // font, border, colors
        htm.eDiv(); // padding-left
        htm.addln("</details>");
    }

    /**
     * Emits the student's status with respect to demonstrating mastery of a learning target.
     *
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @param learningTarget the learning target data
     * @param mode           the page mode
     * @param exams          the student exams
     * @param homeworks      the student homeworks
     */
    private static void endLearningTarget(final HtmlBuilder htm,
                                          final LearningTargetData learningTarget, final String mode,
                                          final Iterable<RawStexam> exams,
                                          final Iterable<RawSthomework> homeworks) {

        final boolean triedHw = hasAttemptedHw(homeworks, learningTarget.assignmentId);
        final boolean passedHw = triedHw && hasPassedHw(homeworks, learningTarget.assignmentId);
        final boolean mastered = hasPassedExam(exams, learningTarget.assignmentId);

        endDetailsBlock(htm); // End of Standard 41.3

        emitStandardAssignment(htm, learningTarget.module.course.courseId, learningTarget.unit,
                "Learning Target " + learningTarget.targetNumber + " Assignment",
                learningTarget.assignmentId, mode, triedHw, passedHw);

        if (mastered) {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("This learning target has been reached!").eDiv();
        } else if (passedHw) {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("You are eligible for this learning target in the testing center.").eDiv();
        } else {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("Once you pass the Learning Target ", learningTarget.targetNumber, " Assignment, you will ",
                            "become eligible for this learning target in the testing center.")
                    .eDiv();
        }
        htm.hr();
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

    /**
     * Tests whether a student has passed an exam by version.
     *
     * @param exams   the list of all exams on record
     * @param version the version
     * @return true if the student has passed the exam
     */
    private static boolean hasPassedExam(final Iterable<RawStexam> exams, final String version) {

        boolean found = false;

        for (final RawStexam rec : exams) {
            if (rec.version.equals(version) && "Y".equals(rec.passed)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
