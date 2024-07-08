package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawPacingRulesLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawrecord.RawLessonComponent;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.servlet.CourseLesson;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the web page that displays a lesson within a course, tailored to a student's position and
 * status in the course.
 */
enum PageLesson {
    ;

    /**
     * Generates the page with contact information.
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
        final String unitStr = req.getParameter("unit");
        final String lessonStr = req.getParameter("lesson");
        final String mode = req.getParameter("mode");
        final String srcourse = req.getParameter("srcourse");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(unitStr)
                || AbstractSite.isParamInvalid(lessonStr) || AbstractSite.isParamInvalid(mode)
                || AbstractSite.isParamInvalid(srcourse)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  unit='", unitStr, "'");
            Log.warning("  lesson='", lessonStr, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  srcourse='", srcourse, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if (unitStr == null || lessonStr == null) {
            resp.sendRedirect("home.html");
        } else {
            try {
                final int unit = Long.valueOf(unitStr).intValue();
                final int seqNum = Long.valueOf(lessonStr).intValue();

                final HtmlBuilder htm = new HtmlBuilder(2000);
                Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                        false, true);

                htm.sDiv("menupanelu");
                CourseMenu.buildMenu(cache, site, session, logic, htm);
                htm.sDiv("panelu");

                buildCourseLessonPage(cache, site, session, course, unit, seqNum, mode, srcourse, htm);

                htm.eDiv(); // panelu
                htm.eDiv(); // menupanelu

                Page.endOrdinaryPage(cache, site, htm, true);

                AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                        htm.toString().getBytes(StandardCharsets.UTF_8));

            } catch (final NumberFormatException ex) {
                resp.sendRedirect("home.html");
            }
        }
    }

    /**
     * Creates the HTML of the course lesson.
     *
     * @param cache              the data cache
     * @param site               the course site
     * @param session            the user's login session information
     * @param courseId           the course ID
     * @param unit               the unit
     * @param objective          the objective
     * @param mode               the mode ("course", "practice", or "locked")
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildCourseLessonPage(final Cache cache, final CourseSite site,
                                              final ImmutableSessionInfo session, final String courseId, final int unit,
                                              final int objective, final String mode, final String skillsReviewCourse,
                                              final HtmlBuilder htm) throws SQLException {

        final DbProfile dbProfile = site.getDbProfile();
        final CourseLesson less = new CourseLesson(dbProfile);
        final String studentId = session.getEffectiveUserId();

        // find the rule set under which the student is working

        final SystemData systemData = cache.getSystemData();

        final TermKey activeKey = systemData.getActiveTerm().term;

        final RawStcourse reg = RawStcourseLogic.getRegistration(cache, studentId, courseId);
        final String ruleSet = reg == null ? null : systemData.getRuleSetId(courseId, reg.sect, activeKey);
        final String ruleSetId = ruleSet == null ? RawPacingStructure.DEF_PACING_STRUCTURE : ruleSet;

        if (less.gatherData(cache, courseId, Integer.valueOf(unit), Integer.valueOf(objective))) {

            final StudentCourseStatus status = new StudentCourseStatus(dbProfile);
            if (status.gatherData(cache, session, studentId, courseId, false, !"course".equals(mode))) {

                final int count = less.getNumComponents();

                if (count == 0) {
                    // No lesson components - read lesson from static file
                    readLessonFile(less, courseId, unit, objective, mode, skillsReviewCourse, htm);
                } else {
                    // There are lesson components, so append a course title, then the components them to the HTML
                    if (skillsReviewCourse == null) {

                        htm.add("<h2 class='title'>");
                        htm.add(" <a name='top'></a>");
                        if ("Y".equals(status.getCourseSection().courseLabelShown)) {
                            htm.add(status.getCourse().courseLabel, ": ");
                        }
                        htm.addln(status.getCourse().courseName);
                        htm.add("</h2>");

                        htm.sDiv("nav");
                        htm.sDiv("aslines");
                        htm.add("  <a class='linkbtn' href='course.html?course=", courseId, "&mode=", mode, "'><em>");
                        htm.add("Return to the Course Outline");
                        htm.addln("</em></a>");
                    } else {
                        final StudentCourseStatus reviewStatus = new StudentCourseStatus(dbProfile);
                        reviewStatus.gatherData(cache, session, skillsReviewCourse, studentId, true,
                                !"course".equals(mode));

                        htm.add("<h2 class='title'>");
                        htm.add(" <a name='top'></a>");
                        if ("Y".equals(status.getCourseSection().courseLabelShown)) {
                            htm.add(status.getCourse().courseLabel, ": ");
                        }
                        htm.addln(status.getCourse().courseName, " - SKILLS REVIEW");
                        htm.add("</h2>");

                        htm.sDiv("gap").add("&nbsp;").eDiv();
                        htm.sDiv("nav");
                        htm.sDiv("aslines");
                        htm.addln("<a class='linkbtn' href='skills_review.html?course=", skillsReviewCourse, "&mode=",
                                mode, "'><em>Return to the Skills Review Outline</em></a>");
                    }
                    htm.eDiv();
                    htm.eDiv();

                    htm.div("clear");
                    htm.sDiv("gap").add("&nbsp;").eDiv();

                    for (int i = 0; i < count; ++i) {
                        final RawLessonComponent comp = less.getLessonComponent(i);
                        final String type = comp.type;

                        // "PREMED" types are media that appear in the course outline
                        if ("PREMED".equals(type)) {
                            continue;
                        }

                        // For examples and media objects, install the appropriate arguments
                        final String xml = comp.xmlData;
                        if ("EX".equals(type) || "MED".equals(type)) {
                            if (skillsReviewCourse == null) {
                                htm.add(xml.replace("%%MODE%%", mode));
                            } else {
                                htm.add(xml.replace("%%MODE%%", mode + "&srcourse=" + skillsReviewCourse));
                            }
                        } else {
                            htm.add(xml);
                        }
                    }
                }

                // Button to launch the assignment (with status)
                if (status.hasHomework(unit, objective)) {
                    final AssignmentRec hw = systemData.getActiveAssignment(courseId, Integer.valueOf(unit),
                            Integer.valueOf(objective), "HW");

                    if (hw != null) {
                        final Boolean isTut = less.getCourseIsTutorial();

                        if (hw.assignmentId == null) {
                            Log.warning("Null assignment for unit " + unit + " obj " + objective);
                        } else if (Boolean.TRUE.equals(isTut) || skillsReviewCourse != null) {
                            doAssignment(cache, site, session, activeKey, courseId, unit, objective, mode, "practice",
                                    hw.assignmentId, ruleSetId, htm);
                        } else {
                            doAssignment(cache, site, session, activeKey, courseId, unit, objective, mode, mode,
                                    hw.assignmentId, ruleSetId, htm);
                        }
                    }
                }
            } else {
                htm.sP().add("FAILED TO GET LESSON STATUS DATA!").br();
                if (status.getErrorText() != null) {
                    htm.addln(status.getErrorText());
                }
                htm.eP();
            }
        } else {
            htm.sP().add("FAILED TO GET LESSON DATA!").br();
            if (less.getErrorText() != null) {
                htm.addln(less.getErrorText());
            }
            htm.eP();
        }
    }

    /**
     * Generates the HTM form to launch the assignment configured for the lesson.
     *
     * @param cache          the data cache
     * @param site           the course site
     * @param session        the user's login session information
     * @param activeKey      the key of the active term
     * @param courseId       the course ID
     * @param unit           the unit
     * @param objective      the objective of the lesson to display
     * @param courseMode     the mode ("course", "practice", or "locked")
     * @param assignmentMode the assignment mode ("course", "practice", or "locked") - could be "practice" while the
     *                       course is still in "course" mode
     * @param assignId       the assignment ID
     * @param pacing         the ID of the rule set under which the student is working
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doAssignment(final Cache cache, final CourseSite site, final ImmutableSessionInfo session,
                                     final TermKey activeKey, final String courseId, final int unit,
                                     final int objective, final String courseMode, final String assignmentMode,
                                     final String assignId, final String pacing, final HtmlBuilder htm)
            throws SQLException {

        final StudentCourseStatus courseStatus = new StudentCourseStatus(site.getDbProfile());

        if (courseStatus.gatherData(cache, session, session.getEffectiveUserId(), courseId, false, //
                !"course".equals(assignmentMode))) {

            if (courseStatus.hasHomework(unit, objective)) {

                if ("course".equals(assignmentMode)) {

                    final boolean hw = courseStatus.isHomeworkAvailable(unit, objective);
                    final String status = courseStatus.getHomeworkStatus(unit, objective);
                    final String reason = courseStatus.getHomeworkReason(unit, objective);

                    htm.div("gap2");
                    htm.addln("<form method='get' action='run_homework.html'>");
                    htm.sDiv("indent11");

                    htm.addln("  <input type='hidden' name='course' value='", courseId, "'/>");
                    htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
                    htm.addln("  <input type='hidden' name='lesson' value='", Integer.toString(objective), "'/>");
                    htm.addln("  <input type='hidden' name='coursemode' value='", courseMode, "'/>");
                    htm.addln("  <input type='hidden' name='mode' value='", assignmentMode, "'/>");
                    htm.addln("  <input type='hidden' name='assign' value='", assignId, "'/>");

                    final boolean mustWatchLecture =
                            RawPacingRulesLogic.isRequired(cache, activeKey, pacing,
                                    RawPacingRulesLogic.ACTIVITY_HOMEWORK, RawPacingRulesLogic.LECT_VIEWED);
                    final boolean viewed = RawStcuobjectiveLogic.hasLectureBeenViewed(cache,
                            session.getEffectiveUserId(), courseId, Integer.valueOf(unit),
                            Integer.valueOf(objective));

                    if (hw) {
                        // If the student has already done it, enable the button
                        if ("May Move On".equals(status) || "Completed".equals(status)) {
                            htm.addln("  <input class='btn' type='submit' value='Objective ",
                                    Integer.toString(unit), CoreConstants.DOT,
                                    Integer.toString(objective), " Required Assignment'/>");
                            htm.addln("<img src='/images/check.png' alt=''/> <span class='green'>", status, "</span>");
                        } else if (mustWatchLecture && !viewed
                                && !session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
                            htm.addln("  <input class='btn' type='submit' value='Objective ",
                                    Integer.toString(objective),
                                    " Required Assignment' disabled='disabled'/>");
                            htm.addln(" <span class='red'>Instructor Lecture not yet Viewed</span>");
                        } else {
                            htm.addln("  <input class='btn' type='submit' value='Objective ",
                                    Integer.toString(unit), CoreConstants.DOT,
                                    Integer.toString(objective), " Required Assignment'/>");
                        }
                    } else {
                        htm.addln("  <input class='btn' type='submit' value='Objective ", Integer.toString(objective),
                                " Required Assignment' disabled='disabled'/>");
                        if (reason != null) {
                            htm.addln(" <span class='red'>", reason, "</span>");
                        }
                    }

                    htm.eDiv();
                    htm.addln("</form>");
                } else if ("practice".equals(assignmentMode) || "locked".equals(assignmentMode)) {

                    htm.div("gap2");

                    htm.addln("<form method='get' action='run_homework.html'>");
                    htm.addln(" <div class='indent11'>");
                    htm.addln("  <input type='hidden' name='course' value='", courseId, "'/>");
                    htm.addln("  <input type='hidden' name='unit' value='", Integer.toString(unit), "'/>");
                    htm.addln("  <input type='hidden' name='lesson' value='", Integer.toString(objective), "'/>");
                    htm.addln("  <input type='hidden' name='coursemode' value='", courseMode, "'/>");
                    htm.addln("  <input type='hidden' name='mode' value='", assignmentMode, "'/>");
                    htm.addln("  <input type='hidden' name='assign' value='", assignId, "'/>");
                    htm.addln("  <input class='btn' type='submit' value='Objective ",
                            Integer.toString(unit), CoreConstants.DOT, Integer.toString(objective),
                            " Practice Problems'/>");
                    if ("locked".equals(assignmentMode)) {
                        htm.addln("<span class='red'>Past final exam deadline, practice only</span>");
                    }
                    htm.eDiv();
                    htm.addln("</form>");
                }
            }
        } else {
            Log.warning("Failed to gather available lesson data",
                    courseStatus.getErrorText());
        }
    }

    /**
     * Reads a lesson from a file, fixes video links, and appends to an {@code HtmlBuilder}.
     *
     * @param less               the course lesson
     * @param courseId           the course ID
     * @param unit               the unit
     * @param seqNum             the sequence number of the lesson to display
     * @param mode               the mode ("course", "practice", or "locked")
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     */
    private static void readLessonFile(final CourseLesson less, final String courseId,
                                       final int unit, final int seqNum, final String mode,
                                       final String skillsReviewCourse,
                                       final HtmlBuilder htm) {

        // Load static page, then replace all media references of the form
        // "<a href='video.html?course=...&unit=...&lesson=...&mode=...&media-id=[media-id]'>"

        final String lessonId = less.getLesson().lessonId;
        final String origStr = "video.html\\?media-id=";
        final HtmlBuilder fixed = new HtmlBuilder(100);
        fixed.add("video.html?course=", courseId, "&unit=", Integer.toString(unit), "&lesson=",
                Integer.toString(seqNum), "&mode=", mode);

        if (skillsReviewCourse != null) {
            fixed.add("&srcourse=", skillsReviewCourse);
        }
        fixed.add("&media-id=");

        if (lessonId != null) {
            final String[] lines = FileLoader.loadFileAsLines(new File("/opt/zircon/lessons/" + lessonId + ".html"),
                    true);

            if (lines == null) {
                htm.sP().add("FAILED TO READ LESSON ", lessonId, "!").eP();
            } else {
                boolean inBody = false;

                final String fixedStr = fixed.toString();

                for (final String line : lines) {

                    if (inBody) {

                        if ("</body>".equals(line)) {
                            inBody = false;
                        } else {
                            htm.addln(line.replace(origStr, fixedStr));
                        }
                    } else if ("<body>".equals(line)) {
                        inBody = true;
                    }
                }
            }
        } else {
            htm.sP().add("FAILED TO LOAD LESSON!").eP();
        }
    }
}
