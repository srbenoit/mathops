package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteData;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.course.data.CourseData;
import dev.mathops.web.site.course.data.Math125;
import dev.mathops.web.site.course.data.Math126;
import dev.mathops.web.site.course.data.ModuleData;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;
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

    /** A commonly used div class. */
    private static final String CLEAR = "clear";

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final WebViewData data, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String mode = req.getParameter("mode");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(mode)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  mode='", mode, "'");
            PageError.doGet(data, site, req, resp, session, "No course and mode provided for course outline");
        } else if (course == null || mode == null) {
            PageError.doGet(data, site, req, resp, session, "No course and mode provided for course outline");
        } else {
            final List<RawStcourse> paceRegs = logic.data.siteRegistrationData.getPaceRegistrations();
            final int pace = paceRegs == null ? 0 : PaceTrackLogic.determinePace(paceRegs);
            final String paceTrack = paceRegs == null ? CoreConstants.EMPTY :
                    PaceTrackLogic.determinePaceTrack(paceRegs, pace);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();
            Page.startOrdinaryPage(htm, siteTitle, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                    true);

            htm.sDiv("menupanelu");
            CourseMenu.buildMenu(data, site, session, logic, htm);
            htm.sDiv("panelu");

            final RawStcourse reg = logic.data.siteRegistrationData.getRegistration(course);
            if (reg == null) {
                htm.sP("error").addln("You are not registered in this course.").eP();
            } else {
                final ZonedDateTime now = session.getNow();
                final boolean isTutor = logic.data.siteStudentData.isSpecialType(now, "TUTOR");
                final StdsMasteryStatus masteryStatus = new StdsMasteryStatus(data, pace, paceTrack, reg, isTutor);

                if (RawRecordConstants.MATH125.equals(course)) {
                    showCourseText(session, Math125.MATH_125, logic, masteryStatus, mode, htm);
                } else if (RawRecordConstants.MATH126.equals(course)) {
                    showCourseText(session, Math126.MATH_126, logic, masteryStatus, mode, htm);
                } else {
                    htm.sP("error").addln("Invalid course ID").eP();
                }

            }

            htm.eDiv(); // panelu
            htm.eDiv(); // menupanelu

            final SystemData systemData = data.getSystemData();
            Page.endOrdinaryPage(systemData, site, htm, true);

            final String htmStr = htm.toString();
            final byte[] bytes = htmStr.getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
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
                                       final CourseSiteLogic logic, final StdsMasteryStatus masteryStatus,
                                       final String mode, final HtmlBuilder htm) {

        final SiteData data = logic.data;
        final RawStcourse reg = data.siteRegistrationData.getRegistration(courseData.courseId);

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
                                     final StdsMasteryStatus masteryStatus,
                                     final String mode, final HtmlBuilder htm) {

        final RawStcourse reg = logic.data.siteRegistrationData.getRegistration(courseData.courseId);

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

            for (int i = 0; i < 8; ++i) {
                final ModuleData moduleData = courseData.modules.get(i);
                final String heading = "Module " + moduleData.moduleNumber;
                final String howToOpenM1 = emitThumb(htm, session, moduleData, masteryStatus, mode, "Open " + heading);

                emitModuleTitle(htm, heading, moduleData.moduleTitle, CSU_GREEN);
                emitModuleStatus(htm, masteryStatus, moduleData.moduleNumber, howToOpenM1);
                htm.div(CLEAR);
            }
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
                                       final StdsMasteryStatus status, final int moduleNumber) {

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
                                    final ModuleData moduleData, final StdsMasteryStatus masteryStatus,
                                    final String mode, final String buttonLabel) {

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
    private static void emitModuleStatus(final HtmlBuilder htm, final StdsMasteryStatus masteryStatus,
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

            htm.sTable("modulestatus");

            htm.sTr().sTh("status").add("Skills&nbsp;Review:").eTh().sTd("status");
            if (srStatus == 2) {
                htm.add("Passed!");
            } else if (srStatus == 1) {
                htm.add("Not yet passed");
            } else {
                htm.add("Not yet attempted");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;1:  ").eTh().sTd("status");
            if (srStatus == 2) {
                if (srStatus == 2) {
                    if (s1Status == 3) {
                        htm.add("Mastered! (after due date, 4 points earned)");
                    } else if (s1Status == 2) {
                        htm.add("Mastered! (5 points earned)");
                    } else if (s1Status == 1) {
                        htm.add("In progress...");
                    } else if (a1Status == 2) {
                        htm.add("<strong>Ready to attempt mastery</strong>");
                    } else if (a1Status == 1) {
                        htm.add("Working on practice problems");
                    } else {
                        htm.add("Available to start");
                    }
                } else {
                    htm.add("Will be available after Skills Review completed");
                }
            } else {
                htm.add("Opens when Skills Review completed");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;2:  ").eTh().sTd();
            if (srStatus == 2) {
                if (a1Status == 2) {
                    if (s2Status == 3) {
                        htm.add("Mastered! (after due date, 4 points earned)");
                    } else if (s2Status == 2) {
                        htm.add("Mastered! (5 points earned)");
                    } else if (s2Status == 1) {
                        htm.add("In progress...");
                    } else if (a2Status == 2) {
                        htm.add("<strong>Ready to attempt mastery</strong>");
                    } else if (a2Status == 1) {
                        htm.add("Working on practice problems");
                    } else {
                        htm.add("Available to start");
                    }
                } else {
                    htm.add("Available to start");
                }
            } else {
                htm.add("Opens when Skills Review completed");
            }
            htm.eTd().eTr();

            htm.sTr().sTh("status").add("Learning&nbsp;Target&nbsp;3:  ").eTh().sTd();
            if (srStatus == 2) {
                if (a2Status == 2) {
                    if (s3Status == 3) {
                        htm.add("Mastered! (after due date, 4 points earned)");
                    } else if (s3Status == 2) {
                        htm.add("Mastered! (5 points earned)");
                    } else if (s3Status == 1) {
                        htm.add("In progress...");
                    } else if (a3Status == 2) {
                        htm.add("<strong>Ready to attempt mastery</strong>");
                    } else if (a3Status == 1) {
                        htm.add("Working on practice problems");
                    } else {
                        htm.add("Available to start");
                    }
                } else {
                    htm.add("Available to start");
                }
            } else {
                htm.add("Opens when Skills Review completed");
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
}
