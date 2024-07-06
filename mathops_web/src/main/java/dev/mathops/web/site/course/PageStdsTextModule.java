package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.data.CourseData;
import dev.mathops.web.site.course.data.ExampleBlock;
import dev.mathops.web.site.course.data.ExampleData;
import dev.mathops.web.site.course.data.LearningTargetData;
import dev.mathops.web.site.course.data.Math125;
import dev.mathops.web.site.course.data.Math126;
import dev.mathops.web.site.course.data.ModuleData;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
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
                final String siteTitle = site.getTitle();
                Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                        true);

                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv(null, "style='padding:0 10pt 10pt 10pt;'");

                showCourseModule(cache, logic, course, modNumber, mode, htm);

                htm.eDiv(); // padding
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                final String htmString = htm.toString();
                final byte[] bytes = htmString.getBytes(StandardCharsets.UTF_8);
                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
            } catch (final NumberFormatException ex) {
                PageError.doGet(cache, site, req, resp, session,
                        "Invalid module number provided for course module page");
            }
        }
    }

    /**
     * Creates the HTML of the course module.
     *
     * @param cache        the cache
     * @param logic        the course site logic
     * @param courseId     the course for which to generate the status page
     * @param moduleNumber the unit number
     * @param mode         the mode - one of "course" (normal access), "practice" (not in course, just using e-text),
     *                     "locked" (in course, but past all deadlines so practice access only)
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void showCourseModule(final Cache cache, final CourseSiteLogic logic, final String courseId,
                                         final int moduleNumber, final String mode, final HtmlBuilder htm) {

        final SiteData data = logic.data;
        final RawStcourse reg = data.registrationData.getRegistration(courseId);

        if (reg == null) {
            htm.sP().add("ERROR: Unable to find course registration.").eP();
        } else {
            final List<RawStcourse> paceRegs = logic.data.registrationData.getPaceRegistrations();
            final int pace = paceRegs == null ? 0 : PaceTrackLogic.determinePace(paceRegs);
            final String paceTrack = paceRegs == null ? CoreConstants.EMPTY :
                    PaceTrackLogic.determinePaceTrack(paceRegs, pace);

            final ZonedDateTime now = ZonedDateTime.now();
            final boolean isTutor = data.studentData.isSpecialType(now, "TUTOR");
            final StdsMasteryStatus masteryStatus = new StdsMasteryStatus(cache, pace, paceTrack, reg, isTutor);
            final SiteDataCfgCourse courseCfg = data.courseData.getCourse(courseId, reg.sect);

            if (RawRecordConstants.MATH125.equals(reg.course)) {
                doModule(cache, reg.stuId, Math125.MATH_125, moduleNumber, courseCfg, masteryStatus, mode, htm);
            } else if (RawRecordConstants.MATH126.equals(reg.course)) {
                doModule(cache, reg.stuId, Math126.MATH_126, moduleNumber, courseCfg, masteryStatus, mode, htm);
            }
        }
    }

    /**
     * Generates module content.
     *
     * @param cache         the cache
     * @param stuId         the student ID
     * @param courseData    the course data
     * @param courseCfg     the course configuration data
     * @param masteryStatus the mastery status
     * @param moduleNumber  the module number
     * @param mode          the mode ("course", "practice", or "locked")
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doModule(final Cache cache, final String stuId, final CourseData courseData,
                                 final int moduleNumber, final SiteDataCfgCourse courseCfg,
                                 final StdsMasteryStatus masteryStatus, final String mode, final HtmlBuilder htm) {

        if (moduleNumber == 0) {
            doHowToNavigate(courseCfg, mode, htm);
        } else if (moduleNumber >= 1 && moduleNumber <= 8) {
            final ModuleData moduleData = courseData.modules.get(moduleNumber - 1);

            doModule(cache, stuId, moduleData, courseCfg, masteryStatus, mode, htm);
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
    private static void doHowToNavigate(final SiteDataCfgCourse courseCfg, final String mode, final HtmlBuilder htm) {

        emitModuleTitle(htm, courseCfg, 0, "How to Successfully Navigate this Course",
                "navigation-thumb.png", courseCfg.course.course, mode);

        htm.sH(3).add("Completing the Course").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .addln("Passing this course requires that you do two things:").eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>Master at least 10 learning targets (out of 12) in the <strong>first half</strong> of ",
                "the course (Modules 1 through 4).</li>");
        htm.addln("<li>Master at least 10 learning targets (out of 12) in the <strong>second half</strong> of ",
                "the course (modules 5 through 8).</li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-top:0;'")
                .add("If you achieve these two goals, you will pass the course.  The grade you earn ",
                        "will then be based on the number of points you have accumulated.")
                .eP();

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .add("There are 120 points possible.");

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>108 (90%) points or higher earns an <strong>A</strong></li>");
        htm.addln("<li>96 (80%) to 107 points earns a <strong>B</strong></li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-top:0;'")
                .add("Any number of points less than 96, as long as you have mastered 10 learning ",
                        "targets in each half of the course, earns a <strong>C</strong>.  If you do not ",
                        "master 10 learning targets in each half of the course, a <strong>U</strong> ",
                        "grade will be recorded (a U grade does not affect GPA).")
                .eP();

        htm.hr();

        htm.sH(3).add("Assignments and Earning Points").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("The course is divided into eight modules. ",
                        "Each module has three learning targets, for a total of 24 learning targets.")
                .eP();

        htm.sH(4).add("Skills Review").eH(4);

        htm.sDiv("indent");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("Each module begins with a <b>Skills Review</b>.  This is a (hopefully) easy ",
                        "assignment to remind you of some skills you will need in the module.  You have to ",
                        "complete this assignment before you can move on to the learning targets in the ",
                        "module. Skills Review assignments do not earn points.")
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

        htm.eDiv();

        htm.sH(4).add("Mastering Learning Targets").eH(4);

        htm.sDiv("indent");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0'")
                .add("Once you have unlocked a learning target for mastery, you can go to the Precalculus Center and ",
                        "take a <b>Mastery Exam</b> to demonstrate mastery of that learning target.  The Mastery Exam ",
                        "will have two questions for each learning target - you must answer both correctly to master ",
                        "that learning target.  You have unlimited attempts on Mastery Exams.")
                .eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li>Mastering a learning target by its due date earns <b>5 points</b>.</li>");
        htm.addln("<li>Mastering a learning target after its due date earns <b>4 points</b>.</li>");
        htm.addln("</ul>");

        htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                .add("You do not need to go to the Precalculus Center once for every learning ",
                        "target - you can complete several Learning Target assignments, then go to the ",
                        "Precalculus Center and your Mastery Exam will include all learning targets you are ",
                        "eligible for.  However, if you have six or more learning targets that have not yet ",
                        "been mastered, you will not be able to move on to the next Module until you master ",
                        "some learning targets to get the number open below six.  This is to prevent ",
                        "someone from leaving all the mastery exams until the end of the semester.")
                .eP();

        htm.eDiv();

        htm.hr();

        htm.sH(3).add("Strategies for Success").eH(3);

        htm.sP(null, "style='font-family:prox-regular,sans-serif;margin-bottom:0;'")
                .add("To succeed in this course, we recommend these core strategies:").eP();

        htm.addln("<ul style='font-family:prox-regular,sans-serif;padding-top:0;'>");
        htm.addln("<li><strong>Work ahead</strong> - never wait until a deadline to do work that's due that day.</li>");
        htm.addln("<li><strong>Do a little work each day</strong> or every couple of days, rather than trying to pack",
                "a lot of work into one day each week.  Schedule a regular time to work on this course.</li>");
        htm.addln("<li><strong>Give yourself time and space</strong>.  Time, so you don't feel rushed or panicked, ",
                "and a quiet study space where you can focus.</li>");
        htm.addln("<li><strong>Use the resources provided</strong>.  Take advantage of in-person and online help from ",
                "the Precalculus Center.  Watch course videos and read the  solutions. Use textbooks or Internet ",
                "resources when something does not make sense.</li>");
        htm.addln("<li>If you start to get behind, <strong>reach out quickly</strong> and get back on track.  The ",
                "Precalculus Center team wants to help you succeed!  Help us to help you by bringing us in when you ",
                "need it.</li>");
        htm.addln("</ul>");
    }

    /**
     * Generates a module outline.
     *
     * @param cache         the cache
     * @param stuId         the student ID
     * @param moduleData    the module data
     * @param courseCfg     the course configuration data
     * @param masteryStatus the mastery status
     * @param mode          the mode ("course", "practice", or "locked")
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doModule(final Cache cache, final String stuId, final ModuleData moduleData,
                                 final SiteDataCfgCourse courseCfg,
                                 final StdsMasteryStatus masteryStatus, final String mode,
                                 final HtmlBuilder htm) {

        emitModuleTitle(htm, courseCfg, moduleData.moduleNumber, moduleData.moduleTitle,
                moduleData.thumbnailImage, moduleData.course.courseId, mode);

        if (!moduleData.skillsReview.exampleBlocks.isEmpty()) {
            startSkillsReview(cache, stuId, htm, moduleData, masteryStatus, mode);
            htm.addln("<ul>");
            boolean first = true;
            for (final ExampleBlock block : moduleData.skillsReview.exampleBlocks) {
                emitExampleBlock(htm, block, REVIEW_TOPIC, first);
                first = false;
            }
            htm.addln("</ul>");
        }
        endSkillsReview(htm);

        for (final LearningTargetData learningTarget : moduleData.learningTargets) {
            startLearningTarget(htm, learningTarget);
            if (!learningTarget.exampleBlocks.isEmpty()) {
                htm.addln("<ul>");
                boolean first = true;
                for (final ExampleBlock block : learningTarget.exampleBlocks) {
                    emitExampleBlock(htm, block, CoreConstants.EMPTY, first);
                    first = false;
                }
                htm.addln("</ul>");
            }
            endLearningTarget(cache, stuId, htm, learningTarget, mode, masteryStatus);
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
     * @param cache         the cache
     * @param stuId         the student ID
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     * @param moduleData    the module data
     * @param masteryStatus the mastery status
     * @param mode          the page mode
     * @return true if the student has not passed the Skills Review, and is not yet eligible for standard assignments
     */
    private static boolean startSkillsReview(final Cache cache, final String stuId, final HtmlBuilder htm,
                                             final ModuleData moduleData, final StdsMasteryStatus masteryStatus,
                                             final String mode) {

        htm.sH(3).add("Skills Review").eH(3);

        final int srStatus = masteryStatus.skillsReviewStatus[moduleData.moduleNumber - 1];
        final boolean tried = srStatus >= 1;
        final boolean passed = srStatus >= 2;

        final String title;
        if (tried) {
            if (passed) {
                htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                        .add("You have passed this assignment - you may access the module learning targets.")
                        .eP();
                title = "Practice Skills Review Problems";
            } else {
                title = "Skills Review Assignment";
            }
        } else {
            htm.sP(null, "style='font-family:prox-regular,sans-serif;'")
                    .add("We recommend that you try the Skills Review assignment first, and only work through any ",
                            "review materials you need after that attempt.")
                    .eP();
            title = "Skills Review Assignment";
        }

        emitStandardAssignment(cache, stuId, htm, moduleData.course.courseId, moduleData.moduleNumber, 0,
                title, moduleData.skillsReview.assignmentId, mode, false, tried, passed);

        startDetailsBlock(htm, REVIEW_MATERIALS);

        return !passed;
    }

    /**
     * Emits the end of a Skills Review block.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void endSkillsReview(final HtmlBuilder htm) {

        endDetailsBlock(htm);
    }

    /**
     * Emits a "Learning Target #.#" title and a list of learning outcomes.
     *
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @param learningTarget the learning target data
     */
    private static void startLearningTarget(final HtmlBuilder htm, final LearningTargetData learningTarget) {

        htm.hr();
        htm.sH(3).add("Learning Target ", learningTarget.targetNumber).eH(3);

        htm.sDiv("learning_target").addln(learningTarget.mainOutcome);

        if (learningTarget.subOutcomes != null && learningTarget.subOutcomes.length > 0) {
            htm.addln("<ul>");
            for (final String item : learningTarget.subOutcomes) {
                htm.addln("<li>", item, "</li>");
            }
            htm.addln("</ul>");
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
     * @param first   true if this is the first example in the block
     */
    private static void emitExampleBlock(final HtmlBuilder htm, final ExampleBlock block, final String heading,
                                         final boolean first) {

        htm.add(first ? "<li>" : "<li style='border-top:1px solid gray;'>");

        htm.add(heading, "<strong style='color:", CSU_GREEN, ";'>", block.title, "</strong>");

        final List<ExampleData> examples = block.examples;
        final int count = examples.size();

        htm.sDiv(null, "style='margin:0 0 8px 0;'");
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

        htm.addln("</li>");
    }

    /**
     * Emits the title for a standard and the link to its assessment.
     *
     * @param cache        the cache
     * @param stuId        the student ID
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @param course       the course
     * @param module       the unit
     * @param objective    the objective
     * @param title        the title for the block
     * @param assignmentId the assignment ID
     * @param mode         the mode
     * @param ineligible   true if the student has not yet passed the Skills Review to become eligible for the
     *                     assignment
     * @param attempted    true if the assessment has been attempted
     * @param mastered     true if the assessment has been mastered
     */
    private static void emitStandardAssignment(final Cache cache, final String stuId, final HtmlBuilder htm,
                                               final String course, final int module, final int objective,
                                               final String title, final String assignmentId, final String mode,
                                               final boolean ineligible, final boolean attempted,
                                               final boolean mastered) {

        htm.add("<div style='font-family:prox-regular,sans-serif;",
                "font-weight:400;font-size:15px;color:#196F43;margin:0 0 .3em 16px;'>");

        htm.addln("<form style='display:inline' method='get' action='run_lta.html'>");
        htm.addln("  <input type='hidden' name='course' value='", course, "'/>");
        htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(module), "'/>");
        htm.addln("  <input type='hidden' name='lesson' value='0'/>");
        htm.addln("  <input type='hidden' name='coursemode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='mode' value='", mode, "'/>");
        htm.addln("  <input type='hidden' name='assign' value='", assignmentId, "'/>");
        if (ineligible) {
            htm.addln("  <input class='smallbtndim' type='submit' value='", title, "' disabled />");
        } else {
            htm.addln("  <input class='smallbtn' type='submit' value='", title, "'/>");
        }
        htm.addln("</form>");

        if (ineligible) {
            htm.addln(" <span style='color:#B00000;border:1px #B00000 solid;border-radius:6px;padding:3px 19px;",
                    "margin-left:16px;'>Skills Review not yet completed</span>");
        } else if (attempted) {
            if (mastered) {
                htm.addln(" <span style='background-color:#EBF9EB;color:#105456;",
                        "border:1px #105456 solid;border-radius:6px;padding:3px 19px;",
                        "margin-left:16px;'>Passed</span>");
            } else {
                htm.addln(" <span style='color:#B00000;border:1px #B00000 solid;border-radius:6px;padding:3px 19px;",
                        "margin-left:16px;'>Not Yet Passed</span>");
            }
        } else {
            htm.addln(" <span style='color:#B00000;border:1px #B00000 solid;border-radius:6px;padding:3px 19px;",
                    "margin-left:16px;'>Not Yet Attempted</span>");
        }
        htm.eDiv();

        try {
            final TermRec active = cache.getSystemData().getActiveTerm();
            final List<RawSthomework> attempts = RawSthomeworkLogic.queryByStudentCourseUnitObjective(cache, stuId,
                    course, Integer.valueOf(module), Integer.valueOf(objective), false);

            if (active != null && !attempts.isEmpty()) {
                htm.sDiv("indent2");

                if (attempts.size() > 5) {
                    htm.addln("<details>");
                    htm.addln("<summary>Review your submitted assignments and solutions</summary>");
                }

                for (final RawSthomework attempt : attempts) {
                    final LocalDateTime whenFinished = attempt.getFinishDateTime();

                    if (whenFinished != null) {
                        final String path = ExamWriter.makeWebExamPath(active.term.shortString, stuId,
                                attempt.serialNbr.longValue());
                        final String sanitized = course.replace(CoreConstants.SPC, "%20");

                        final String when = TemporalUtils.FMT_MDY_AT_HM_A.format(whenFinished);

                        htm.sDiv("indent1");
                        htm.addln(" <a class='ulink' href='see_past_lta.html?course=", sanitized,
                                "&unit=", Integer.toString(module),
                                "&mode=", mode,
                                "&exam=", attempt.version,
                                "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                                "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                                "'>Review the Skills Review Assignment submitted ", when, "</a>");

                        htm.eDiv();
                    }
                }

                if (attempts.size() > 5) {
                    htm.addln("</details>");
                }
                htm.eDiv();
                htm.div("vgap");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            htm.sDiv("indent2");
            htm.addln("(There was an error looking up your submitted assignments)");
            htm.eDiv();
            htm.div("vgap");
        }
    }

    /**
     * Emits the start of a "details" block.
     *
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     * @param text the text for the expandable heading
     */
    private static void startDetailsBlock(final HtmlBuilder htm, final String text) {

        htm.addln("<details style='padding-left:20px;'>");

        htm.addln("<summary style='font-family:prox-regular,sans-serif;margin-bottom:6px;'>", text,
                " (expand with arrow on the left)</summary>");

        htm.sDiv(null, "style='padding-left:20px;line-height:1.1em;'");
        htm.sDiv(null, "style='font-family:prox-regular,sans-serif;color:black;",
                "border:1px solid black;background:#f8f8f8;'");
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
     * @param cache          the cache
     * @param stuId          the student ID
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @param learningTarget the learning target data
     * @param mode           the page mode
     * @param masteryStatus  the mastery status
     */
    private static void endLearningTarget(final Cache cache, final String stuId, final HtmlBuilder htm,
                                          final LearningTargetData learningTarget, final String mode,
                                          final StdsMasteryStatus masteryStatus) {

        final int unitIndex = learningTarget.unit - 1;
        final int objIndex = learningTarget.objective - 1;
        final int arrayIndex = unitIndex * 3 + objIndex;
        final int assignmentStatus = masteryStatus.assignmentStatus[arrayIndex];
        final int standardStatus = masteryStatus.standardStatus[arrayIndex];
        final int srStatus = masteryStatus.skillsReviewStatus[unitIndex];

        final boolean triedHw = assignmentStatus > 0;
        final boolean passedHw = assignmentStatus > 1;
        final boolean mastered = standardStatus > 1;
        final boolean ineligible = srStatus < 2 && !masteryStatus.tutor;

        endDetailsBlock(htm);

        final String title;
        if (passedHw) {
            title = "Practice Learning Target " + learningTarget.targetNumber + " Assignment";
        } else {
            title = "Learning Target " + learningTarget.targetNumber + " Assignment";
        }

        emitStandardAssignment(cache, stuId, htm, learningTarget.module.course.courseId, learningTarget.unit,
                learningTarget.objective, title, learningTarget.assignmentId, mode, ineligible, triedHw, passedHw);

        if (ineligible) {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("This assignment will become available when you have completed the ",
                            "<strong>Skills Review</strong>.")
                    .eDiv();
        } else if (mastered) {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("This learning target has <strong>already been mastered</strong>!").eDiv();
        } else if (passedHw) {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("You are <strong style='color:#D9782D'>eligible to master this learning target</strong> ",
                            "in the testing center!")
                    .eDiv();
        } else {
            htm.sDiv(null, "style='padding-left:24px;font-family:prox-regular,sans-serif;'")
                    .add("Once you pass the <strong>Learning Target ", learningTarget.targetNumber,
                            " Assignment</strong>, you will become eligible to master this learning target in the ",
                            "testing center.")
                    .eDiv();
        }
    }
}
