package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.logic.PrecalcTutorialLogic;
import dev.mathops.db.old.logic.PrecalcTutorialStatus;
import dev.mathops.db.old.logic.PrerequisiteLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.CourseSiteLogicCourse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * A menu that lists the courses a user is registered for, along with links to manage e-texts, and view a recommended
 * progress schedule.
 */
enum CourseMenu {
    ;

    /**
     * Builds the menu based on the current logged-in user session and appends its HTML representation to an
     * {@code HtmlBuilder}.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param session the session
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    public static void buildMenu(final WebViewData data, final CourseSite site,
                                 final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                 final HtmlBuilder htm) throws SQLException {

        htm.addln("<nav class='menu'>");
        htm.sDiv("menubox");

        if (logic.isError()) {
            final String error = logic.getError();
            htm.sSpan("red").add(error).eSpan();
        } else {
            buildMenuContent(data, site, session, logic, htm);
        }

        htm.eDiv();
        htm.addln("</nav>");
    }

    /**
     * Builds the menu once available courses have been determined.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param session the session
     * @param logic   the site logic
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void buildMenuContent(final WebViewData data, final CourseSite site,
                                         final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                         final HtmlBuilder htm) throws SQLException {

        final ZonedDateTime now = session.getNow();

        final boolean isTutor = "AACTUTOR".equals(session.getEffectiveUserId())
                || logic.data.studentData.isSpecialType(now, "TUTOR");
        final boolean isAdmin = session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR);

        final int numLockouts = logic.data.studentData.getNumLockouts();
        final boolean isLocked = numLockouts > 0;

        // Name, profile, logout links, home, student guide, user's exam, and e-text links
        emitTopMatter(htm);

        final CourseSiteLogicCourse courses = logic.course;

        if (!isTutor) {
            // Placement info
            emitPlacementInfoAndStatus(htm);

            // Display information on incompletes, if any exist
            emitIncompleteCourses(site, logic, isLocked, htm);
        }

        htm.div("vgap");

        final int numRegular = courses.tutorials.size() + courses.otCreditCourses.size()
                + courses.completedCourses.size() + courses.pastDeadlineCourses.size()
                + courses.inProgressCourses.size() + courses.availableCourses.size()
                + courses.unavailableCourses.size() + courses.noPrereqCourses.size()
                + courses.notAvailableCourses.size() + courses.forfeitCourses.size();

        if (numRegular > 0) {
            htm.sDiv("courses");

            // Display courses for the current term
            final SystemData systemData = data.getSystemData();
            final TermRec activeTerm = systemData.getActiveTerm();

            htm.sH(1, "menufirst");
            htm.add(activeTerm.term.longString, " Courses");
            htm.eH(1);
            htm.div("vgap0");

            htm.sDiv(null, "style='text-align:left;'");

            final StudentData studentData = data.getEffectiveUser();

            final PrerequisiteLogic pl = new PrerequisiteLogic(studentData);
            final LocalDate today = now.toLocalDate();

            final PrecalcTutorialStatus tstat = new PrecalcTutorialLogic(studentData, today, pl).status;

            final boolean lockTut = tstat.webSiteAvailability == null || tstat.webSiteAvailability.current == null;

            if (!courses.tutorials.isEmpty()) {
                htm.div("startmenu");
                emitCourseMenuItems(courses.tutorials, htm, lockTut, false);
            }

            if (!isTutor && !isAdmin) {

                if (!courses.otCreditCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Placement Credit:</h2>");
                    emitCourseLabels(courses.otCreditCourses, htm, false);
                    htm.div("vgap0");
                }

                if (!courses.pastDeadlineCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Past Final Deadline:</h2>");
                    emitCourseMenuItems(courses.pastDeadlineCourses, htm, isLocked, true);
                    htm.div("vgap0");
                }

                if (!courses.completedCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Completed:</h2>");
                    emitCourseMenuItems(courses.completedCourses, htm, isLocked, false);
                    htm.div("vgap0");
                }

                if (!courses.notAvailableCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>No Longer Available:</h2>");
                    emitCourseMenuItems(courses.notAvailableCourses, htm, true, false);
                    htm.div("vgap0");
                }
            }

            if (!courses.inProgressCourses.isEmpty()) {
                if (isTutor) {
                    htm.addln("<h2 class='menu'>Tutor Access:</h2>");
                } else if (isAdmin) {
                    htm.addln("<h2 class='menu'>Administrator Access:</h2>");
                } else {
                    htm.addln("<h2 class='menu'>In Progress:</h2>");
                }
                emitCourseMenuItems(courses.inProgressCourses, htm, isLocked, false);
                htm.div("vgap0");
            }

            if (!isTutor && !isAdmin) {
                if (!courses.availableCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Available To Start:</h2>");
                    emitStartCourses(courses.availableCourses, htm);
                    htm.div("vgap0");
                }

                if (!courses.unavailableCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Prerequisites Met:</h2>");
                    emitCourseLabels(courses.unavailableCourses, htm, true);
                    htm.div("vgap0");
                }

                if (!courses.noPrereqCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Needing Prerequisites:</h2>");
                    emitCourseLabels(courses.noPrereqCourses, htm, true);
                    htm.div("vgap0");
                }

                if (!courses.forfeitCourses.isEmpty()) {
                    htm.addln("<h2 class='menu'>Forfeit:</h2>");
                    emitCourseLabels(courses.forfeitCourses, htm, false);
                    htm.div("vgap0");
                }
            }

            htm.eDiv(); // Green background
            htm.eDiv(); // left-aligned
        }
    }

    /**
     * Emits the top portion of the menu (before the course lists).
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void emitTopMatter(final HtmlBuilder htm) {

        htm.addln("<a class='ulink' href='home.html'>Home</a>");

        htm.div("vgap0").hr().div("vgap0");

        htm.addln("<a class='ulink' target='_blank' href='",
                "https://www.math.colostate.edu/Precalc/Precalc-Student-Guide.pdf'>Student Guide</a>");
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='orientation.html'>Orientation</a>");
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='onlinehelp.html'><b>Getting Help</b></a>");
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='users_exam.html'>User's Exam</a>");
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='etexts.html'>My e-texts</a>");
        htm.div("vgap0");

        // htm.addln("<a class='ulink' href='calendar.html'>Recommended Schedule</a>").br();
        // htm.div("vgap0");

        htm.addln("<a class='ulink' href='schedule.html'>My Exam Deadlines</a>").br();
        htm.div("vgap0");
    }

    /**
     * Emits placement information.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void emitPlacementInfoAndStatus(final HtmlBuilder htm) {

        htm.sH(1, "menu").add("Placement Information").eH(1);

        htm.addln("<a class='ulink' href='placement_report.html'>My Placement Results</a>");
        htm.div("vgap0");
    }

    /**
     * Emits the section of the menu covering incomplete courses.
     *
     * @param site   the owning site
     * @param logic  the course site logic
     * @param htm    the {@code HtmlBuilder} to which to append the HTML
     * @param locked {@code true} if student is locked out, {@code false} otherwise
     */
    private static void emitIncompleteCourses(final CourseSite site, final CourseSiteLogic logic,
                                              final boolean locked, final HtmlBuilder htm) {

        final CourseSiteLogicCourse courses = logic.course;

        final int count = courses.inProgressIncCourses.size();
        final int count2 = courses.availableIncCourses.size();
        final int count3 = courses.noPrereqIncCourses.size();
        final int count4 = courses.completedIncCourses.size();
        final int count5 = courses.pastDeadlineIncCourses.size();
        final int count6 = courses.unavailableIncCourses.size();

        if (count + count2 + count3 + count4 + count5 + count6 > 0) {

            htm.div("vgap2");
            htm.addln("<h1 class='menu'>Incomplete Courses</h1>");

            if (count5 > 0) {
                htm.addln("<h2 class='menu'>Deadline Passed:</h2>");
                emitCourseMenuItems(courses.pastDeadlineIncCourses, htm, true, true);
                htm.div("vgap0");
            }

            if (count4 > 0) {
                htm.addln("<h2 class='menu'>Completed:</h2>");
                emitCourseMenuItems(courses.completedIncCourses, htm, locked, false);
                htm.div("vgap0");
            }

            if (count > 0) {
                htm.addln("<h2 class='menu'>In Progress:</h2>");
                emitCourseMenuItems(courses.inProgressIncCourses, htm, locked, false);
                htm.div("vgap0");
            }

            if (count2 > 0) {
                htm.addln("<h2 class='menu'>Available To Start:</h2>");
                emitStartCourses(courses.availableIncCourses, htm);
                htm.div("vgap0");
            }

            if (count6 > 0) {
                htm.addln("<h2 class='menu'>Prerequisites Met:</h2>");
                emitCourseLabels(courses.unavailableIncCourses, htm, true);
                htm.div("vgap0");
            }

            if (count3 > 0) {
                htm.addln("<h2 class='menu'>Needing Prerequisites:</h2>");
                emitCourseLabels(courses.noPrereqIncCourses, htm, true);
                htm.div("vgap0");
            }
        }
    }

    /**
     * Emits a menu item for a course with the label of the course on the left and no links.
     *
     * @param list            the list of courses
     * @param htm             the {@code HtmlBuilder} to which to append the HTML
     * @param showDeadlineMsg {@code true} to show a message regarding upcoming deadlines in the course
     */
    private static void emitCourseLabels(final Iterable<CourseInfo> list, final HtmlBuilder htm,
                                         final boolean showDeadlineMsg) {

        for (final CourseInfo courseInfo : list) {
            htm.sDiv("nodots");
            htm.sDiv("left");
            htm.addln("<span class='menu2'><span class='dim'>", courseInfo.label, "</span></span>");
            htm.addln(" </div>");
            if (showDeadlineMsg) {
                htm.sDiv("right");
                htm.addln("<a class='ulink' href='schedule.html'><span class='red'><em>Check Schedule</em></span></a>");
                htm.eDiv();
            }
            htm.div("clear");
            htm.eDiv();
        }
    }

    /**
     * Emits a menu item allowing the user to start a course.
     *
     * @param list the list of courses
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     */
    private static void emitStartCourses(final Iterable<CourseInfo> list, final HtmlBuilder htm) {

        for (final CourseInfo courseInfo : list) {
            htm.add("<a class='smallbtn' style='margin:4px 0 4px 10px;' href='start_course.html?course=",
                    courseInfo.course, "&mode=course'>Start ", courseInfo.label, "</a>");
        }
    }

    /**
     * Emits a menu item for a course with the label of the course.
     *
     * @param list           the list of courses
     * @param htm            the {@code HtmlBuilder} to which to append the HTML
     * @param locked         {@code true} to disable link, {@code false} to enable link
     * @param isPastDeadline {@code true} if the course is not yet passed but is beyond the final exam deadline (student
     *                       can do no more work in the course)
     */
    private static void emitCourseMenuItems(final Iterable<CourseInfo> list, final HtmlBuilder htm,
                                            final boolean locked, final boolean isPastDeadline) {

        for (final CourseInfo courseInfo : list) {

            final String courseLbl = courseInfo.label;

            final String mode;
            if (isPastDeadline) {
                mode = "locked";
            } else {
                mode = "course";
            }

            // FIXME: Courses that are "past deadline" but which have never been opened should
            // NOT show the e-text and status buttons.

            if (locked || !courseInfo.available) {
                htm.addln("&bull; <strong class='menu2 gray'>", courseLbl, "</strong>");
            } else if (courseInfo.course.startsWith("MATH")) {
                htm.add("<a class='smallbtn' style='margin:4px 0 4px 10px;' href='course.html?course=",
                        courseInfo.course, "&mode=", mode, "'>", courseLbl, " E-Text</a>");
            } else {
                htm.add("<a class='smallbtn' style='margin:4px 0 4px 10px;' href='course.html?course=",
                        courseInfo.course, "&mode=", mode, "'>", courseLbl, " E-text</a>");
            }
        }
    }
}
