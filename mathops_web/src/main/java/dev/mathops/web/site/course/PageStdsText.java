package dev.mathops.web.site.course;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.PaceTrackLogic;
import dev.mathops.db.rawrecord.RawMilestone;
import dev.mathops.db.rawrecord.RawRecordConstants;
import dev.mathops.db.rawrecord.RawStcourse;
import dev.mathops.db.rawrecord.RawStexam;
import dev.mathops.db.rawrecord.RawSthomework;
import dev.mathops.db.rawrecord.RawStmilestone;
import dev.mathops.db.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.session.sitelogic.data.SiteDataActivity;
import dev.mathops.session.sitelogic.data.SiteDataMilestone;
import dev.mathops.session.sitelogic.data.SiteDataRegistration;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.data.CourseData;
import dev.mathops.web.site.course.data.MathCourses;
import dev.mathops.web.site.course.data.ModuleData;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * This page shows the outline and E-text content for a single standards-based course. This is an outline page with a
 * list of modules, each with its status.
 *
 * <p>
 * It is assumed that this page can only be accessed by someone who has passed the user's exam and has legitimate access
 * to the e-text. This page does not check those conditions.
 */
enum PageStdsText {
    ;

    /** A color. */
    private static final String CSU_GREEN = "#1E4D2B";

    /** A color. */
    private static final String CSU_ORANGE = "#D9782D";

    /** A commonly used string. */
    private static final String OPEN = "Open";

    /** A commonly used string. */
    private static final String EPLOR_1 = "Explorations 1";

    /** A commonly used string. */
    private static final String EPLOR_2 = "Explorations 2";

    /** A commonly used div class. */
    private static final String CLEAR = "clear";

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
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  mode='", mode, "'");
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else if (course == null || mode == null) {
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else {
            final MasteryStatus masteryStatus = new MasteryStatus(logic, course);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, site.getTitle(), session, false,
                    Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(cache, site, session, logic, htm);
            htm.sDiv("panelu");

            if (RawRecordConstants.MATH125.equals(course)) {
                showCourseText(session, MathCourses.MATH_125, logic, masteryStatus, mode, htm);
            } else if (RawRecordConstants.MATH126.equals(course)) {
                showCourseText(session, MathCourses.MATH_126, logic, masteryStatus, mode, htm);
            } else {
                htm.sP("error").addln("Invalid course ID").eP();
            }

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                    htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Creates the HTML of the course text.
     *
     * @param session       the login session
     * @param courseData    the course data
     * @param logic         the course site logic
     * @param masteryStatus the mastery status
     * @param mode          the mode - one of "course" (normal access), "practice" (not in course, just using e-text),
     *                      "locked" (in course, but past all deadlines so practice access only)
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     */
    private static void showCourseText(final ImmutableSessionInfo session, final CourseData courseData,
                                       final CourseSiteLogic logic, final MasteryStatus masteryStatus,
                                       final String mode, final HtmlBuilder htm) {

        final SiteData data = logic.data;
        final RawStcourse reg = data.registrationData.getRegistration(courseData.courseId);

        if (reg == null) {
            htm.sP().add("ERROR: Unable to find course registration.").eP();
        } else {
            htm.sH(2, "title");
            htm.add(courseData.courseNumber, ": ");
            htm.sSpan(null, "style='color:#D9782D'").add(courseData.courseTitle).eSpan().br();
            htm.add("<small>", "Section ", reg.sect, "</small>");
            htm.eH(2);

            // This is essentially a table of contents for the e-text, with student-specific status
            // on each entry, and selected enable/disable.

            doCourseText(session, courseData, logic, masteryStatus, mode, htm);
        }
    }

    /**
     * Generates the MATH 125 course outline.
     *
     * @param session       the login session
     * @param courseData    the course data
     * @param logic         the course site logic
     * @param masteryStatus the mastery status
     * @param mode          the mode ("course", "practice", or "locked")
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doCourseText(final ImmutableSessionInfo session,
                                     final CourseData courseData, final CourseSiteLogic logic,
                                     final MasteryStatus masteryStatus,
                                     final String mode, final HtmlBuilder htm) {

        final RawStcourse reg = logic.data.registrationData.getRegistration(courseData.courseId);

        if ("Y".equals(reg.iInProgress)) {
            // TODO: Incomplete status
        } else if (reg.paceOrder == null) {
            htm.sP();
            htm.addln("Unable to determine class schedule.");
            htm.eP();
        } else {
            emitNavThumb(htm, courseData.courseId, 0, mode, OPEN);
            emitModuleTitle(htm, null, "How to Successfully Navigate this Course", CSU_ORANGE);
            htm.div(CLEAR);

            for (int i = 0; i < 5; ++i) {
                final ModuleData moduleData = courseData.modules.get(i);
                final String heading = "Module " + moduleData.moduleNumber;
                final String howToOpenM1 = emitThumb(htm, session, moduleData, masteryStatus, mode, "Open " + heading);

                emitModuleTitle(htm, heading, moduleData.moduleTitle, CSU_GREEN);
                emitModuleStatus(htm, masteryStatus, 1, howToOpenM1);
                htm.div(CLEAR);
            }

            final String howToOpenEx1 = emitExplorationThumb(htm, masteryStatus, 1, courseData.courseId, mode, OPEN);
            emitModuleTitle(htm, null, EPLOR_1, CSU_ORANGE);
            emitExplorationStatus(htm, masteryStatus, 1, howToOpenEx1);
            htm.div(CLEAR);

            for (int i = 5; i < 10; ++i) {
                final ModuleData moduleData = courseData.modules.get(i);
                final String heading = "Module " + moduleData.moduleNumber;
                final String howToOpenM1 = emitThumb(htm, session, moduleData, masteryStatus, mode, "Open " + heading);

                emitModuleTitle(htm, heading, moduleData.moduleTitle, CSU_GREEN);
                emitModuleStatus(htm, masteryStatus, 1, howToOpenM1);
                htm.div(CLEAR);
            }

            final String howToOpenEx2 = emitExplorationThumb(htm, masteryStatus, 2, courseData.courseId, mode, OPEN);
            emitModuleTitle(htm, null, EPLOR_2, CSU_ORANGE);
            emitExplorationStatus(htm, masteryStatus, 2, howToOpenEx2);
            htm.div(CLEAR).hr();
        }
    }

    /**
     * Determines whether a module is "open", meaning the student can access the module and open assignments.
     *
     * @param session      the login session (to allow modules to be open for administrators)
     * @param status       the student's mastery status in the course
     * @param moduleNumber the module number (from 1 to 10)
     * @return {@code null} if the module is open, and if not, a message to the student explaining what needs to be done
     *         to open the module
     */
    private static String isModuleOpen(final ImmutableSessionInfo session,
                                       final MasteryStatus status, final int moduleNumber) {

        final String result = null;

        if (session.role != ERole.SYSADMIN && session.role != ERole.ADMINISTRATOR) {

            // If module 1 is automatically "open" - others need to be checked
            if (moduleNumber > 1) {
                final int moduleIndex = moduleNumber - 1;

                final boolean foundWork = status.skillsReviewStatus[moduleIndex] > 0
                        || status.assignmentStatus[moduleIndex * 3] > 0
                        || status.assignmentStatus[moduleIndex * 3 + 1] > 0
                        || status.assignmentStatus[moduleIndex * 3 + 2] > 0
                        || status.standardStatus[moduleIndex * 3] > 0
                        || status.standardStatus[moduleIndex * 3 + 1] > 0
                        || status.standardStatus[moduleIndex * 3 + 2] > 0;

                if (!foundWork) {
                    // No work has yet been done - for such a module to be "open", all the prior
                    // module's assignments must have been passed, AND there may be no more than 7
                    // standards not yet mastered whose assignment has been passed.

                    // FIXME: Pilot - all modules open...

                    // final boolean priorModuleAssignmentsDone =
                    // status.skillsReviewStatus[moduleIndex - 1] > 1
                    // && status.assignmentStatus[moduleIndex * 3 - 3] > 1
                    // && status.assignmentStatus[moduleIndex * 3 - 2] > 1
                    // && status.assignmentStatus[moduleIndex * 3 - 1] > 1;
                    //
                    // if (priorModuleAssignmentsDone) {
                    // if (status.numStandardsPending > 7) {
                    // final int toMaster = status.numStandardsPending - 7;
                    // result = "Module " + moduleNumber
                    // + " will become available when you have reached " + toMaster
                    // + " more learning targets";
                    // }
                    // } else {
                    // result = "Module " + moduleNumber
                    // + " will become available when all assignments in module "
                    // + (moduleNumber - 1) + " have been completed.";
                    // }
                }
            }
        }

        return result;
    }

    /**
     * Determines whether an exploration is "open", meaning the student can access the exploration and open its
     * assignment.
     *
     * @param status            the student's mastery status in the course
     * @param explorationNumber the exploration number (from 1 to 2)
     * @return {@code null} if the exploration is open, and if not, a message to the student explaining what needs to be
     *         done to open the exploration
     */
    private static String isExplorationOpen(final MasteryStatus status, final int explorationNumber) {

        String result = null;

        // Explorations open when the prior unit's assignments have been passed

        final int priorUnitIndex = explorationNumber == 1 ? 12 : 27;

        final boolean priorModAssignPend = status.assignmentStatus[priorUnitIndex + 1] <= 1
                || status.assignmentStatus[priorUnitIndex + 2] <= 1
                || status.assignmentStatus[priorUnitIndex + 3] <= 1;

        if (priorModAssignPend) {
            final String priorModules = explorationNumber == 1 ? "1 through 5" : "6 through 10";
            result = SimpleBuilder.concat("This exploration will become available when the assignments in Modules ",
                    priorModules, " have been completed.");
        }

        return result;
    }

    /**
     * Emits a thumbnail image and button to go into the navigating the course module.
     *
     * @param htm         the {@code HtmlBuilder} to which to append the HTML
     * @param course      the course number, such as "M 125"
     * @param unit        the unit number
     * @param mode        the page mode
     * @param buttonLabel the button label
     */
    private static void emitNavThumb(final HtmlBuilder htm, final String course, final int unit,
                                     final String mode, final String buttonLabel) {

        final String course2 = course.replaceAll(CoreConstants.SPC, "%20");

        final String href = SimpleBuilder.concat("course_text_module.html?course=",
                course2, "&module=", Integer.toString(unit), "&mode=", mode);

        htm.hr();
        htm.sDiv("left");
        htm.addln("<a href='", href, "'>",
                "<img style='margin-right:16px; border:1px gray solid;' ",
                "src='/www/images/etext/navigation-thumb.png'/></a>").br();
        htm.add("<a class='smallbtn' href='", href,
                "' style='width:152px;margin-right:16px;text-align:center;' >",
                buttonLabel, "</a>");
        htm.eDiv();
    }

    /**
     * Emits a thumbnail image and button to go into a module.
     *
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     * @param session       the login session
     * @param masteryStatus the mastery status
     * @param moduleData    the module data
     * @param mode          the page mode
     * @param buttonLabel   the button label
     * @return {@code null} if the module is open, and if not, a message to the student explaining what needs to be done
     *         to open the module
     */
    private static String emitThumb(final HtmlBuilder htm, final ImmutableSessionInfo session,
                                    final ModuleData moduleData, final MasteryStatus masteryStatus, final String mode,
                                    final String buttonLabel) {

        final String course2 = moduleData.course.courseId.replaceAll(CoreConstants.SPC, "%20");
        final String href = SimpleBuilder.concat(//
                "course_text_module.html?course=", course2,
                "&module=", Integer.toString(moduleData.moduleNumber),
                "&mode=", mode);

        final String howToOpen = isModuleOpen(session, masteryStatus, moduleData.moduleNumber);

        htm.hr();
        htm.sDiv("left");
        if (howToOpen == null) {
            htm.addln("<a href='", href, "'>",
                    "<img style='margin-right:16px; border:1px gray solid;' ",
                    "src='/www/images/etext/", moduleData.thumbnailImage, "'/></a>").br();
            htm.add("<a class='smallbtn' href='", href,
                    "' style='width:152px;margin-right:16px;text-align:center;' >",
                    buttonLabel, "</a>");
        } else {
            htm.addln("<img style='margin-right:16px; border:1px gray solid;' ",
                    "src='/www/images/etext/", moduleData.thumbnailImage, "'/>").br();
            htm.add("<a class='smallbtndim' ",
                    "style='width:152px;margin-right:16px;text-align:center;' >",
                    buttonLabel, "</a>");
        }
        htm.eDiv();

        return howToOpen;
    }

    /**
     * Emits a thumbnail image and button to go into an exploration.
     *
     * @param htm               the {@code HtmlBuilder} to which to append the HTML
     * @param masteryStatus     the mastery status
     * @param explorationNumber the exploration number (from 1 to 2)
     * @param course            the course number, such as "M 125"
     * @param mode              the page mode
     * @param buttonLabel       the button label
     * @return {@code null} if the exploration is open, and if not, a message to the student explaining what needs to be
     *         done to open the exploration
     */
    private static String emitExplorationThumb(final HtmlBuilder htm, final MasteryStatus masteryStatus,
                                               final int explorationNumber, final String course, final String mode,
                                               final String buttonLabel) {

        final String course2 = course.replaceAll(CoreConstants.SPC, "%20");
        final String href = SimpleBuilder.concat("course_text_exploration.html?course=", course2,
                "&course_text_exploration=", Integer.toString(explorationNumber), "&mode=", mode);

        final String howToOpen = isExplorationOpen(masteryStatus, explorationNumber);
        htm.hr();
        htm.sDiv("left");
        if (howToOpen == null) {
            htm.addln("<a href='", href, "'><img style='margin-right:16px; border:1px gray solid;' ",
                    "src='/www/images/etext/explorations-thumb.png'/></a>").br();
            htm.add("<a class='smallbtn' style='width:152px;text-align:center;' href='", href, "'>", buttonLabel,
                    "</a>");
        } else {
            htm.addln("<img style='margin-right:16px; border:1px gray solid;' ",
                    "src='/www/images/etext/explorations-thumb.png'/>").br();
            htm.add("<a class='smallbtndim' style='width:152px;text-align:center;'>", buttonLabel, "</a>");
        }
        htm.eDiv();

        return howToOpen;
    }

    /**
     * Emits a module title.
     *
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @param heading an optional heading
     * @param title   the unit title
     * @param color   the title color
     */
    private static void emitModuleTitle(final HtmlBuilder htm, final String heading,
                                        final String title, final String color) {

        if (heading != null) {
            htm.sDiv(null, "style='font-family:factoria-medium;sans-serif;font-weight:800;font-size:19px;color:",
                    CSU_GREEN, "; margin-bottom:3px;'");
            htm.add(heading);
            htm.eDiv();
        }

        htm.sDiv(null, "style='font-family:factoria-medium;sans-serif;font-weight:500;font-size:18px;color:", color,
                ";'");
        htm.add(title);
        htm.eDiv();
    }

    /**
     * Emits the student's status in a module.
     *
     * @param htm           the {@code HtmlBuilder} to which to append the HTML
     * @param masteryStatus the mastery status
     * @param moduleNumber  the module number
     * @param howToOpen     {@code null} if the module is open; a phrase describing to the student how to open the
     *                      module if not
     */
    private static void emitModuleStatus(final HtmlBuilder htm, final MasteryStatus masteryStatus,
                                         final int moduleNumber, final String howToOpen) {

        if (howToOpen == null) {
            final int index = moduleNumber - 1;

            final int srStatus = masteryStatus.skillsReviewStatus[index];
            final int a1Status = masteryStatus.assignmentStatus[index * 3];
            final int a2Status = masteryStatus.assignmentStatus[index * 3 + 1];
            final int a3Status = masteryStatus.assignmentStatus[index * 3 + 2];
            final int s1Status = masteryStatus.standardStatus[index * 3];
            final int s2Status = masteryStatus.standardStatus[index * 3 + 1];
            final int s3Status = masteryStatus.standardStatus[index * 3 + 2];

            htm.sTable(null, "border=1", "frame=hsides", "rules=rows",
                    "style='font-size:15px;font-family:prox-regular,sans-serif;'");

            htm.sTr().sTh("status").add("Skills&nbsp;Review:").eTh().sTd();
            if (srStatus == 2) {
                htm.add("Passed!");
            } else if (srStatus == 1) {
                htm.add("Not yet passed");
            } else {
                htm.add("Not yet attempted");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;1:  ").eTh().sTd();
            if (srStatus == 2) {
                if (s1Status == 3) {
                    htm.add("Reached! (after deadline, 4 points awarded)");
                } else if (s1Status == 2) {
                    htm.add("Reached! (5 points awarded)");
                } else if (s1Status == 1) {
                    htm.add("In progress...");
                } else if (a1Status == 2) {
                    htm.add("Ready to attempt mastery");
                } else if (a1Status == 1) {
                    htm.add("Working on practice problems");
                } else {
                    htm.add("Available to start");
                }
            } else {
                htm.add("Will be available after Skills Review completed");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;2:  ").eTh().sTd();
            if (a1Status == 2) {
                if (s2Status == 3) {
                    htm.add("Reached! (after deadline, 4 points awarded)");
                } else if (s2Status == 2) {
                    htm.add("Reached! (5 points awarded)");
                } else if (s2Status == 1) {
                    htm.add("In progress...");
                } else if (a2Status == 2) {
                    htm.add("Ready to attempt mastery");
                } else if (a2Status == 1) {
                    htm.add("Working on practice problems");
                } else {
                    htm.add("Available to start");
                }
            } else {
                htm.add("Will be available after Learning Target 1 practice set completed");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;3:  ").eTh().sTd();
            if (a2Status == 2) {
                if (s3Status == 3) {
                    htm.add("Reached! (after deadline, 4 points awarded)");
                } else if (s3Status == 2) {
                    htm.add("Reached! (5 points awarded)");
                } else if (s3Status == 1) {
                    htm.add("In progress...");
                } else if (a3Status == 2) {
                    htm.add("Ready to attempt mastery");
                } else if (a3Status == 1) {
                    htm.add("Working on practice problems");
                } else {
                    htm.add("Available to start");
                }
            } else {
                htm.add("Will be available after Learning Target 2 practice set completed");
            }
            htm.eTd().eTr();

            htm.eTable();
        } else {
            htm.sDiv(null, SimpleBuilder.concat("style='background:#f0f0f0;",
                    "border:1px solid gray;margin:0 20px 0 170px;padding:2px 6px;'"));
            htm.addln(howToOpen);
            htm.eDiv();
        }
    }

    /**
     * Emits the student's status in an exploration.
     *
     * @param htm               the {@code HtmlBuilder} to which to append the HTML
     * @param masteryStatus     the mastery status
     * @param explorationNumber the exploration number
     * @param howToOpen         {@code null} if the exploration is open; a phrase describing to the student how to open
     *                          the exploration if not
     */
    private static void emitExplorationStatus(final HtmlBuilder htm, final MasteryStatus masteryStatus,
                                              final int explorationNumber, final String howToOpen) {

        final String color = howToOpen == null ? "fffff0" : "f0f0f0";

        htm.sDiv(null, SimpleBuilder.concat("style='background:#", color,
                ";border:1px solid gray;margin-left:170px;padding:2px 6px;'"));

        if (howToOpen == null) {
            final int index = explorationNumber - 1;
            final int status = masteryStatus.explorationStatus[index];

            if (status == 1) {
                htm.addln("Attempted, 5 points earned");
            } else if (status == 2) {
                htm.addln("Attempted (late), 4 points earned");
            } else if (status == 3) {
                htm.addln("Passed, 10 points earned");
            } else if (status == 4) {
                htm.addln("Passed (nearly on time), 9 points earned");
            } else if (status == 5) {
                htm.addln("Passed (late), 8 points earned");
            } else {
                htm.addln("Not yet attempted");
            }
        } else {
            htm.addln(howToOpen);
        }

        htm.eDiv();
    }

    /**
     * A container for mastery status.
     */
    static final class MasteryStatus {

        /** Status in 10 Skills Reviews (0=not tried, 1=tried, 2=passed). */
        final int[] skillsReviewStatus;

        /** Status in 30 assignments (0=not tried, 1=tried, 2=passed). */
        final int[] assignmentStatus;

        /** Status in 30 standards (0=not tried, 1=tried, 2=mastered on time, 3=mastered late). */
        final int[] standardStatus;

        /** Dates when each standard was first mastered. */
        final LocalDate[] standardFirstMastered;

        /** Dates by which each standard is due. */
        final LocalDate[] standardDeadlines;

        /**
         * Status in 2 explorations (0=not tried, 1=tried, 2=tried late, 3=passed on time, 4=passed 1-day late, 5 =
         * passed late).
         */
        final int[] explorationStatus;

        /** Dates when each exploration was first attempted. */
        final LocalDate[] explorationFirstAttempted;

        /** Dates when each exploration was first passed. */
        final LocalDate[] explorationFirstPassed;

        /** Dates by which each exploration is due. */
        final LocalDate[] explorationDeadlines;

        /** Dates by which each exploration is considered "1 day late". */
        final LocalDate[] explorationLastTry;

        /** The number of standards that are "pending" (assignment passed but not mastered). */
        int numStandardsPending;

        /**
         * Constructs a new {@code MasteryStatus}.
         *
         * @param logic  the course site logic
         * @param course the course ID
         */
        MasteryStatus(final CourseSiteLogic logic, final String course) {

            this.skillsReviewStatus = new int[10];
            this.assignmentStatus = new int[30];
            this.standardStatus = new int[30];
            this.standardFirstMastered = new LocalDate[30];
            this.standardDeadlines = new LocalDate[30];
            this.explorationStatus = new int[2];
            this.explorationFirstAttempted = new LocalDate[2];
            this.explorationFirstPassed = new LocalDate[2];
            this.explorationDeadlines = new LocalDate[2];
            this.explorationLastTry = new LocalDate[2];

            final SiteDataRegistration regData = logic.data.registrationData;
            final RawStcourse reg = regData.getRegistration(course);

            if ("Y".equals(reg.iInProgress) && !("Y".equals(reg.iCounted))) {

                // TODO: Incomplete that is not counted in current pace. Due dates from the I term
                // should govern for all work completed in the I term, but current-term dates should
                // govern new work. How to do?

            } else if (reg.paceOrder != null) {
                // Current term "in pace" registration
                final List<RawStcourse> paceRegs = regData.getPaceRegistrations();
                final int pace = PaceTrackLogic.determinePace(paceRegs);
                final int order = reg.paceOrder.intValue();
                final String paceTrack = PaceTrackLogic.determinePaceTrack(paceRegs, pace);

                final TermRec activeTerm = regData.getActiveTerm();

                final SiteDataMilestone msData = logic.data.milestoneData;
                final List<RawMilestone> milestones = msData.getMilestones(activeTerm.term);
                final List<RawStmilestone> stmilestones = msData.getStudentMilestones(activeTerm.term);

                final SiteDataActivity acData = logic.data.activityData;
                final List<RawStexam> stexams = acData.getStudentExams(course);
                final List<RawSthomework> sthomeworks = acData.getStudentHomeworks(course);

                //
                // Collect deadlines for standards and explorations
                //

                for (int index = 0; index < 30; ++index) {
                    final int unit = ((index / 3) << 2) + 2;
                    final int msNbr = pace * 1000 + order * 100 + unit;

                    LocalDate onTime = null;
                    for (final RawMilestone ms : milestones) {
                        if (ms.paceTrack.equals(paceTrack) && ms.msNbr.intValue() == msNbr) {
                            if (RawMilestone.STANDARD_MASTERY.equals(ms.msType)) {
                                onTime = ms.msDate;
                                break;
                            }
                        }
                    }
                    for (final RawStmilestone stms : stmilestones) {
                        if (stms.paceTrack.equals(paceTrack) && stms.msNbr.intValue() == msNbr) {
                            if (RawMilestone.STANDARD_MASTERY.equals(stms.msType)) {
                                onTime = stms.msDate;
                                break;
                            }
                        }
                    }

                    this.standardDeadlines[index] = onTime;
                }

                for (int index = 0; index < 2; ++index) {
                    final int unit = 41 + index;
                    final int msNbr = pace * 1000 + order * 100 + unit;

                    LocalDate onTime = null;
                    LocalDate late = null;
                    for (final RawMilestone ms : milestones) {
                        if (ms.paceTrack.equals(paceTrack) && ms.msNbr.intValue() == msNbr) {
                            if (RawMilestone.EXPLORATION.equals(ms.msType)) {
                                onTime = ms.msDate;
                            } else if (RawMilestone.EXPLORATION_1_DAY_LATE.equals(ms.msType)) {
                                late = ms.msDate;
                            }
                        }
                    }
                    for (final RawStmilestone stms : stmilestones) {
                        if (stms.paceTrack.equals(paceTrack) && stms.msNbr.intValue() == msNbr) {
                            if (RawMilestone.EXPLORATION.equals(stms.msType)) {
                                onTime = stms.msDate;
                            } else if (RawMilestone.EXPLORATION_1_DAY_LATE.equals(stms.msType)) {
                                late = stms.msDate;
                            }
                        }
                    }

                    this.explorationDeadlines[index] = onTime;
                    this.explorationLastTry[index] = late;
                }

                //
                // Gather homework-based status for Skills Reviews and standard assignments
                //

                for (final RawSthomework sthomework : sthomeworks) {
                    final int u = sthomework.unit.intValue();
                    if (u < 1 || u > 40) {
                        continue;
                    }

                    final int value = "Y".equals(sthomework.passed) ? 2 : 1;

                    // unit 1, 5, 9, ..., 37 are Skills Reviews
                    if ((u - 1) % 4 == 0) {
                        final int index = (u - 1) / 4;
                        this.skillsReviewStatus[index] = Math.max(this.skillsReviewStatus[index], value);
                    } else {
                        // Not a "Skills Review" unit - must be an assignment
                        final int block = (u - 1) / 4;
                        final int std = ((u - 1) % 4) - 1;
                        final int index = block * 3 + std;
                        this.assignmentStatus[index] = Math.max(this.assignmentStatus[index], value);
                    }
                }

                //
                // Gather exam-based status for Standard Mastery exams and Explorations
                //

                for (final RawStexam stexam : stexams) {
                    final int u = stexam.unit.intValue();
                    if (u < 1 || u > 42) {
                        continue;
                    }

                    final int value = "Y".equals(stexam.passed) ? 2 : 1;

                    if (u > 40) {
                        // Exploration
                        final int index = u - 41;
                        this.explorationStatus[index] = Math.max(this.explorationStatus[index], value);
                        if (value == 2) {
                            if (this.explorationFirstPassed[index] == null
                                    || this.explorationFirstPassed[index].isAfter(stexam.examDt)) {
                                this.explorationFirstPassed[index] = stexam.examDt;
                            }
                        }
                        if (this.explorationFirstAttempted[index] == null
                                || this.explorationFirstAttempted[index].isAfter(stexam.examDt)) {
                            this.explorationFirstAttempted[index] = stexam.examDt;
                        }
                    } else if ((u - 1) % 4 != 0) {
                        // Not a "Skills Review" unit - must be a standard mastery exam
                        final int block = (u - 1) / 4;
                        final int std = ((u - 1) % 4) - 1;
                        final int index = block * 3 + std;
                        this.standardStatus[index] = Math.max(this.standardStatus[index], value);
                        if (value == 2) {
                            if (this.standardFirstMastered[index] == null
                                    || this.standardFirstMastered[index].isAfter(stexam.examDt)) {
                                this.standardFirstMastered[index] = stexam.examDt;
                            }
                        }
                    }
                }

                //
                // We now have the earliest dates for all mastered standards and explorations. We have
                // marked all of them as "on time" - update any that were late to indicate "late"
                //

                for (int index = 0; index < 30; ++index) {
                    if (this.standardFirstMastered[index] != null && this.standardDeadlines[index] != null
                            && this.standardFirstMastered[index].isAfter(this.standardDeadlines[index])) {

                        // Standard was mastered late!
                        this.standardStatus[index] = 3;
                    }
                }

                for (int index = 0; index < 2; ++index) {
                    if (this.explorationFirstPassed[index] != null) {
                        // Exploration has been passed

                        if (this.explorationDeadlines[index] != null
                                && this.explorationFirstPassed[index].isAfter(this.explorationDeadlines[index])) {
                            // Exploration was passed late - but was it "one day late" or "very
                            // late"?)

                            if (this.explorationLastTry[index] != null
                                    && this.explorationFirstPassed[index].isAfter(this.explorationLastTry[index])) {
                                // Very late
                                this.explorationStatus[index] = 5;
                            } else {
                                // Just one day late
                                this.explorationStatus[index] = 4;
                            }
                        }
                    } else if (this.explorationFirstAttempted[index] != null) {
                        // Exploration has been attempted, but not passed

                        if (this.explorationDeadlines[index] != null
                                && this.explorationFirstAttempted[index].isAfter(this.explorationDeadlines[index])) {
                            // First attempt was late
                            this.explorationStatus[index] = 2;
                        }
                    }
                }
            }

            // Count the number of standards "pending"
            for (int i = 0; i < 30; ++i) {
                if (this.assignmentStatus[i] == 2 && this.standardStatus[i] < 2) {
                    ++this.numStandardsPending;
                }
            }
        }
    }
}
