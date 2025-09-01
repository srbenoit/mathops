package dev.mathops.web.host.precalc.course;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.course.PaceTrackLogic;
import dev.mathops.db.schema.legacy.RawCampusCalendar;
import dev.mathops.db.schema.legacy.RawCourse;
import dev.mathops.db.schema.legacy.RawMilestone;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates the content of the web page that displays a semester calendar with recommended dates for each activity
 * based on the student's pace and pace track.
 */
enum PageCalendar {
    ;

    /** A commonly used string. */
    private static final String HOLIDAY = "Holiday";

    /** Month names. */
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    /** A commonly used string. */
    private static final String SKILLS_REVIEW = "Skills Review";

    /** A commonly used string. */
    private static final String OBJECTIVE = "Objective ";

    /** A commonly used string. */
    private static final String UNIT = "Unit ";

    /** A commonly used string. */
    private static final String REVIEW = " Review";

    /** A commonly used string. */
    private static final String EXAM = " Exam";

    /** A commonly used string. */
    private static final String FINAL_EXAM = "Final Exam";

    /**
     * Generates the page that shows the student's term schedule and deadlines.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @param logic   the site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR,
                null, false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        doCalendarContent(cache, logic, htm, false);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Generates the page that shows the student's term schedule and deadlines.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @param logic the site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGetPrintable(final Cache cache, final CourseSite site,
                               final ServletRequest req, final HttpServletResponse resp,
                               final CourseSiteLogic logic)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startEmptyPage(htm, site.getTitle(), true);

        doCalendarContent(cache, logic, htm, true);

        Page.endEmptyPage(htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Constructs the course schedule content.
     *
     * @param cache     the data cache
     * @param logic     the course site logic
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param printable true if being presented in printable format
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCalendarContent(final Cache cache, final CourseSiteLogic logic,
                                          final HtmlBuilder htm, final boolean printable) throws SQLException {

        htm.sH(2).add("Recommended Progress Schedule").eH(2);

        // Determine pace and track
        final TermRec active = logic.data.registrationData.getActiveTerm();

        final List<RawStcourse> paceRegs = logic.data.registrationData.getPaceRegistrations();
        final int pace = paceRegs == null ? 0 : PaceTrackLogic.determinePace(paceRegs);
        final String track = paceRegs == null ? "A" : PaceTrackLogic.determinePaceTrack(paceRegs, pace);

        final List<RawMilestone> allMilestones =
                logic.data.milestoneData.getMilestones(active.term);
        final Collection<RawMilestone> milestones = new ArrayList<>(pace << 5);
        for (final RawMilestone row : allMilestones) {
            if (pace == row.pace.intValue() && track.equals(row.paceTrack)) {
                milestones.add(row);
            }
        }

        htm.sDiv("right");
        htm.addln(printable ? "<a href='calendar.html'>Web view</a>" //
                : "<a href='calendar_print.html'>Printable view</a>");
        htm.eDiv();

        if (pace == 0) {
            htm.sP("indent22").add("You are not registered in any precalculus courses this semester.").eP();
            htm.div("clear");
        } else if (milestones.isEmpty()) {
            htm.sP("indent22").add("Unable to look up due dates for your combination of courses.").eP();
            htm.div("clear");
        } else {
            htm.sP("indent22").add("You are on the <strong>", Integer.toString(pace), " course, Track ", track,
                    "</strong> schedule.").eP();

            htm.sP("indent22 blue").add("<img style='position:relative;top:-2px;' src='/images/info.png' alt=''/> ",
                    "<strong>TIP: Try to work ahead of schedule to be sure due dates are met.</strong>").eP();
            htm.div("clear");

            // Determine the ordering of courses and sort in pace order (paceRegs known to be non-null here)
            sortPaceOrder(paceRegs);

            final Collection<LocalDate> holidays = new ArrayList<>(7);
            final List<RawCampusCalendar> calendarDays = cache.getSystemData().getCampusCalendars();
            for (final RawCampusCalendar row : calendarDays) {
                if (RawCampusCalendar.DT_DESC_HOLIDAY.equals(row.dtDesc)) {
                    holidays.add(row.campusDt);
                }
            }

            if (pace == 1) {
                present1Calendar(paceRegs, milestones, holidays, logic, htm);
            } else if (pace == 2) {
                present2Calendar(paceRegs, milestones, holidays, logic, htm);
            } else if (pace == 3) {
                present3Calendar(paceRegs, milestones, holidays, logic, htm);
            } else if (pace == 4) {
                present4Calendar(paceRegs, milestones, holidays, logic, htm);
            } else if (pace == 5) {
                present5Calendar(paceRegs, milestones, holidays, logic, htm);
            }
        }
    }

    /**
     * Scans the list of registrations ensuring that all registrations which will be included in the pace have a pace
     * order assigned, that the order sequence has no gaps, and that completed courses come before open courses come
     * before unopened courses, with incompletes sorted before current term courses.
     *
     * @param paceRegs those registrations which contribute to pace
     */
    private static void sortPaceOrder(final List<RawStcourse> paceRegs) {

        boolean allInOrder = true;

        // Verify all have a pace order specified
        for (final RawStcourse reg : paceRegs) {
            if (reg.paceOrder == null) {
                allInOrder = false;
                break;
            }
        }

        // Verify that all pace orders from 1 to N are present (guarantees no duplicates or gaps)
        int which = 1;
        final int numPaceRegs = paceRegs.size();
        while (allInOrder && which <= numPaceRegs) {
            allInOrder = false;
            for (final RawStcourse reg : paceRegs) {
                if (reg.paceOrder.intValue() == which) {
                    allInOrder = true;
                    break;
                }
            }
            ++which;
        }

        if (allInOrder) {
            // Sort them in pace order
            RawStcourse hold;
            for (int i = 0; i < numPaceRegs; ++i) {
                if (paceRegs.get(i).paceOrder.intValue() == i + 1) {
                    // Already in its proper place
                    continue;
                }
                // Find the one that belongs at [i] and swap it into place
                for (int j = i + 1; j < numPaceRegs; ++j) {
                    if (paceRegs.get(j).paceOrder.intValue() == i + 1) {
                        hold = paceRegs.get(i);
                        paceRegs.set(i, paceRegs.get(j));
                        paceRegs.set(j, hold);
                        break;
                    }
                }
                // Now, [i] contains the correct row.
            }

            // See if the order makes sense

            // FIXME: This appears bogus.

            ESchedulePhase phase = ESchedulePhase.inc_completed;
            for (final RawStcourse reg : paceRegs) {

                if (reg.iDeadlineDt == null) {
                    if ("Y".equals(reg.completed)
                            || "N".equals(reg.openStatus)) {
                        // Completed incomplete - must be within inc_completed phase
                        if (phase != ESchedulePhase.inc_completed) {
                            allInOrder = false;
                            break;
                        }
                    } else if ("Y".equals(reg.openStatus)) {
                        // Open incomplete - must be within inc_open phase or earlier
                        if (phase == ESchedulePhase.inc_completed) {
                            phase = ESchedulePhase.inc_open;
                        } else if (phase != ESchedulePhase.inc_open) {
                            allInOrder = false;
                            break;
                        }
                    } else // Not yet open incomplete - must be within inc_unopened or earlier phase
                        if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open) {
                            phase = ESchedulePhase.inc_unopened;
                        } else if (phase != ESchedulePhase.inc_unopened) {
                            allInOrder = false;
                            break;
                        }
                } else if ("Y".equals(reg.completed) || "N".equals(reg.openStatus)) {
                    // Completed regular course - must be within completed or earlier phase
                    if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open
                            || phase == ESchedulePhase.inc_unopened) {
                        phase = ESchedulePhase.completed;
                    } else if (phase != ESchedulePhase.completed) {
                        allInOrder = false;
                        break;
                    }
                } else if ("Y".equals(reg.openStatus)) {
                    // Open regular course - must be within open phase or earlier
                    if (phase == ESchedulePhase.inc_completed || phase == ESchedulePhase.inc_open
                            || phase == ESchedulePhase.inc_unopened || phase == ESchedulePhase.completed) {
                        phase = ESchedulePhase.open;
                    }
                }
            }
        }
    }

    /**
     * Presents the Pace 1, Track A calendar.
     *
     * @param paceRegs   the list of pace registrations (of length 1)
     * @param milestones the list of milestones for the pace and track
     * @param holidays   the list of holidays in the semester
     * @param logic      the course site logic
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void present1Calendar(final List<RawStcourse> paceRegs,
                                         final Iterable<RawMilestone> milestones,
                                         final Collection<LocalDate> holidays,
                                         final CourseSiteLogic logic, final HtmlBuilder htm) {

        final RawStcourse reg1 = paceRegs.getFirst();

        final SiteDataCfgCourse courseData1 =
                logic.data.courseData.getCourse(reg1.course, reg1.sect);
        final RawCourse course1 = courseData1 == null ? null : courseData1.course;

        final Collection<RawMilestone> course1Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 0 || index == 1) {
                course1Milestones.add(row);
            }
        }
        emitCourseCalendar(course1, reg1, course1Milestones, holidays, htm);
    }

    /**
     * Presents the Pace 2, Track A calendar.
     *
     * @param paceRegs   the list of pace registrations (of length 2)
     * @param milestones the list of milestones for the pace and track
     * @param holidays   the list of holidays in the semester
     * @param logic      the course site logic
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void present2Calendar(final List<RawStcourse> paceRegs,
                                         final Iterable<RawMilestone> milestones,
                                         final Collection<LocalDate> holidays,
                                         final CourseSiteLogic logic, final HtmlBuilder htm) {

        final RawStcourse reg1 = paceRegs.get(0);
        final RawStcourse reg2 = paceRegs.get(1);

        final SiteDataCfgCourse courseData1 =
                logic.data.courseData.getCourse(reg1.course, reg1.sect);
        final RawCourse course1 = courseData1 == null ? null : courseData1.course;

        final SiteDataCfgCourse courseData2 =
                logic.data.courseData.getCourse(reg2.course, reg2.sect);
        final RawCourse course2 = courseData2 == null ? null : courseData2.course;

        final Collection<RawMilestone> course1Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 0 || index == 1) {
                course1Milestones.add(row);
            }
        }
        emitCourseCalendar(course1, reg1, course1Milestones, holidays, htm);

        final Collection<RawMilestone> course2Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 2) {
                course2Milestones.add(row);
            }
        }
        emitCourseCalendar(course2, reg2, course2Milestones, holidays, htm);
    }

    /**
     * Presents the Pace 3, Track A calendar.
     *
     * @param paceRegs   the list of pace registrations (of length 3)
     * @param milestones the list of milestones for the pace and track
     * @param holidays   the list of holidays in the semester
     * @param logic      the course site logic
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void present3Calendar(final List<RawStcourse> paceRegs,
                                         final Iterable<RawMilestone> milestones,
                                         final Collection<LocalDate> holidays,
                                         final CourseSiteLogic logic, final HtmlBuilder htm) {

        final RawStcourse reg1 = paceRegs.get(0);
        final RawStcourse reg2 = paceRegs.get(1);
        final RawStcourse reg3 = paceRegs.get(2);

        final SiteDataCfgCourse courseData1 = logic.data.courseData.getCourse(reg1.course, reg1.sect);
        final RawCourse course1 = courseData1 == null ? null : courseData1.course;

        final SiteDataCfgCourse courseData2 = logic.data.courseData.getCourse(reg2.course, reg2.sect);
        final RawCourse course2 = courseData2 == null ? null : courseData2.course;

        final SiteDataCfgCourse courseData3 = logic.data.courseData.getCourse(reg3.course, reg3.sect);
        final RawCourse course3 = courseData3 == null ? null : courseData3.course;

        final Collection<RawMilestone> course1Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 0 || index == 1) {
                course1Milestones.add(row);
            }
        }
        emitCourseCalendar(course1, reg1, course1Milestones, holidays, htm);

        final Collection<RawMilestone> course2Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 2) {
                course2Milestones.add(row);
            }
        }
        emitCourseCalendar(course2, reg2, course2Milestones, holidays, htm);

        final Collection<RawMilestone> course3Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 3) {
                course3Milestones.add(row);
            }
        }
        emitCourseCalendar(course3, reg3, course3Milestones, holidays, htm);
    }

    /**
     * Presents the Pace 4, Track A calendar.
     *
     * @param paceRegs   the list of pace registrations (of length 4)
     * @param milestones the list of milestones for the pace and track
     * @param holidays   the list of holidays in the semester
     * @param logic      the course site logic
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void present4Calendar(final List<RawStcourse> paceRegs,
                                         final Iterable<RawMilestone> milestones,
                                         final Collection<LocalDate> holidays,
                                         final CourseSiteLogic logic, final HtmlBuilder htm) {

        final RawStcourse reg1 = paceRegs.get(0);
        final RawStcourse reg2 = paceRegs.get(1);
        final RawStcourse reg3 = paceRegs.get(2);
        final RawStcourse reg4 = paceRegs.get(3);

        final SiteDataCfgCourse courseData1 = logic.data.courseData.getCourse(reg1.course, reg1.sect);
        final RawCourse course1 = courseData1 == null ? null : courseData1.course;

        final SiteDataCfgCourse courseData2 = logic.data.courseData.getCourse(reg2.course, reg2.sect);
        final RawCourse course2 = courseData2 == null ? null : courseData2.course;

        final SiteDataCfgCourse courseData3 = logic.data.courseData.getCourse(reg3.course, reg3.sect);
        final RawCourse course3 = courseData3 == null ? null : courseData3.course;

        final SiteDataCfgCourse courseData4 = logic.data.courseData.getCourse(reg4.course, reg4.sect);
        final RawCourse course4 = courseData4 == null ? null : courseData4.course;

        final Collection<RawMilestone> course1Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 0 || index == 1) {
                course1Milestones.add(row);
            }
        }
        emitCourseCalendar(course1, reg1, course1Milestones, holidays, htm);

        final Collection<RawMilestone> course2Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 2) {
                course2Milestones.add(row);
            }
        }
        emitCourseCalendar(course2, reg2, course2Milestones, holidays, htm);

        final Collection<RawMilestone> course3Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 3) {
                course3Milestones.add(row);
            }
        }
        emitCourseCalendar(course3, reg3, course3Milestones, holidays, htm);

        final Collection<RawMilestone> course4Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 4) {
                course4Milestones.add(row);
            }
        }
        emitCourseCalendar(course4, reg4, course4Milestones, holidays, htm);
    }

    /**
     * Presents the Pace 5, Track A calendar.
     *
     * @param paceRegs   the list of pace registrations (of length 5)
     * @param milestones the list of milestones for the pace and track
     * @param holidays   the list of holidays in the semester
     * @param logic      the course site logic
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void present5Calendar(final List<RawStcourse> paceRegs,
                                         final Iterable<RawMilestone> milestones,
                                         final Collection<LocalDate> holidays,
                                         final CourseSiteLogic logic, final HtmlBuilder htm) {

        final RawStcourse reg1 = paceRegs.get(0);
        final RawStcourse reg2 = paceRegs.get(1);
        final RawStcourse reg3 = paceRegs.get(2);
        final RawStcourse reg4 = paceRegs.get(3);
        final RawStcourse reg5 = paceRegs.get(4);

        final SiteDataCfgCourse courseData1 = logic.data.courseData.getCourse(reg1.course, reg1.sect);
        final RawCourse course1 = courseData1 == null ? null : courseData1.course;

        final SiteDataCfgCourse courseData2 = logic.data.courseData.getCourse(reg2.course, reg2.sect);
        final RawCourse course2 = courseData2 == null ? null : courseData2.course;

        final SiteDataCfgCourse courseData3 = logic.data.courseData.getCourse(reg3.course, reg3.sect);
        final RawCourse course3 = courseData3 == null ? null : courseData3.course;

        final SiteDataCfgCourse courseData4 = logic.data.courseData.getCourse(reg4.course, reg4.sect);
        final RawCourse course4 = courseData4 == null ? null : courseData4.course;

        final SiteDataCfgCourse courseData5 = logic.data.courseData.getCourse(reg5.course, reg5.sect);
        final RawCourse course5 = courseData5 == null ? null : courseData5.course;

        final Collection<RawMilestone> course1Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 0 || index == 1) {
                course1Milestones.add(row);
            }
        }
        emitCourseCalendar(course1, reg1, course1Milestones, holidays, htm);

        final Collection<RawMilestone> course2Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 2) {
                course2Milestones.add(row);
            }
        }
        emitCourseCalendar(course2, reg2, course2Milestones, holidays, htm);

        final Collection<RawMilestone> course3Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 3) {
                course3Milestones.add(row);
            }
        }
        emitCourseCalendar(course3, reg3, course3Milestones, holidays, htm);

        final Collection<RawMilestone> course4Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 4) {
                course4Milestones.add(row);
            }
        }
        emitCourseCalendar(course4, reg4, course4Milestones, holidays, htm);

        final Collection<RawMilestone> course5Milestones = new ArrayList<>(32);
        for (final RawMilestone row : milestones) {
            final int index = row.getIndex();
            if (index == 5) {
                course5Milestones.add(row);
            }
        }
        emitCourseCalendar(course5, reg5, course5Milestones, holidays, htm);
    }

    /**
     * Generates the calendar display for a single course.
     *
     * @param course     the course
     * @param reg        the registration record
     * @param milestones the list of milestones for the course
     * @param holidays   the list of holidays in the semester
     * @param htm        the {@code HtmlBuilder} to which to append
     */
    private static void emitCourseCalendar(final RawCourse course, final RawStcourse reg,
                                           final Iterable<RawMilestone> milestones,
                                           final Collection<LocalDate> holidays, final HtmlBuilder htm) {

        htm.hr().div("vgap0");
        if (course == null) {
            htm.sH(3, "cal").add(reg.course.replace("M ", "MATH ")).eH(3);
        } else {
            htm.sH(3, "cal").add(course.courseLabel, ": ", course.courseName).eH(3);
        }

        // Identify the earliest and latest milestone dates
        LocalDate earliest = null;
        LocalDate latest = null;
        for (final RawMilestone row : milestones) {
            if ("F1".equals(row.msType)) {
                // Not a displayed milestone...
                continue;
            }
            if (earliest == null || earliest.isAfter(row.msDate)) {
                earliest = row.msDate;
            }
            if (latest == null || latest.isBefore(row.msDate)) {
                latest = row.msDate;
            }
        }

        if (earliest != null && latest != null) {
            LocalDate firstDay = earliest;
            LocalDate lastDay = latest;

            // Expand the range to display to consist of whole weeks.
            while (firstDay.getDayOfWeek() != DayOfWeek.SUNDAY) {
                firstDay = firstDay.minusDays(1L);
            }
            while (lastDay.getDayOfWeek() != DayOfWeek.SATURDAY) {
                lastDay = lastDay.plusDays(1L);
            }

            htm.sTable("calendar");
            emitWeekdayRow(htm);

            // Run through each week
            LocalDate current = firstDay;
            Month activeMonth = null;
            while (current.isBefore(lastDay)) {
                htm.sTr();

                // Sunday
                if (current.isBefore(earliest) || current.isAfter(latest)) {
                    emitEmptyDimCell(htm);
                } else {
                    emitDimDayCell(htm, current.getDayOfMonth());
                }
                current = current.plusDays(1L);

                // Monday through Friday
                for (int i = 0; i < 5; ++i) {
                    if (holidays.contains(current)) {
                        emitHolidayCell(htm, current.getDayOfMonth(), HOLIDAY);
                    } else if (current.isBefore(earliest) || current.isAfter(latest)) {
                        emitEmptyCell(htm);
                    } else {
                        final List<Task> tasks = identifyTasks(reg, current, milestones);
                        String monthName = null;
                        if (activeMonth != current.getMonth()) {
                            activeMonth = current.getMonth();
                            monthName = MONTHS[activeMonth.ordinal()];
                        }
                        emitDayCell(htm, monthName, current.getDayOfMonth(), tasks);
                    }
                    current = current.plusDays(1L);
                }

                // Saturday
                if (current.isBefore(earliest) || current.isAfter(latest)) {
                    emitEmptyDimCell(htm);
                } else {
                    emitDimDayCell(htm, current.getDayOfMonth());
                }
                current = current.plusDays(1L);

                htm.eTr();
            }

            htm.eTable();
        }
    }

    /**
     * Identifies tasks due on a specified date.
     *
     * @param reg        the registration
     * @param date       the date
     * @param milestones the list of milestones
     * @return the list of tasks
     */
    private static List<Task> identifyTasks(final RawStcourse reg, final LocalDate date,
                                            final Iterable<RawMilestone> milestones) {

        final boolean open = "Y".equals(reg.openStatus);

        final List<Task> tasks = new ArrayList<>(3);

        for (final RawMilestone row : milestones) {
            if (row.msDate.equals(date)) {
                final String type = row.msType;

                if ("US".equals(type)) {
                    tasks.add(new UsersExamTask());
                } else if ("SR".equals(type)) {
                    tasks.add(new SkillsReviewTask(reg.course, open));
                } else {
                    final int unit = row.getUnit();
                    if ("H1".equals(type)) {
                        tasks.add(new ObjectiveTask(reg.course, unit + ".1", open));
                    } else if ("H2".equals(type)) {
                        tasks.add(new ObjectiveTask(reg.course, unit + ".2", open));
                    } else if ("H3".equals(type)) {
                        tasks.add(new ObjectiveTask(reg.course, unit + ".3", open));
                    } else if ("H4".equals(type)) {
                        tasks.add(new ObjectiveTask(reg.course, unit + ".4", open));
                    } else if ("H5".equals(type)) {
                        tasks.add(new ObjectiveTask(reg.course, unit + ".5", open));
                    } else if ("RE".equals(type)) {
                        tasks.add(new UnitReviewTask(reg.course, Integer.toString(unit), open));
                    } else if ("UE".equals(type)) {
                        tasks.add(new UnitExamTask(reg.course, Integer.toString(unit), open));
                    } else if ("FE".equals(type)) {
                        tasks.add(new FinalExamTask(reg.course, open));
                    }
                }
            }
        }

        return tasks;
    }

    /**
     * Emits the header row at the top of the calendar with weekday abbreviations.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitWeekdayRow(final HtmlBuilder htm) {

        htm.sTr()//
                .sTh("caldim").add("SUN").eTh()
                .sTh("cal").add("MON").eTh()
                .sTh("cal").add("TUE").eTh()
                .sTh("cal").add("WED").eTh()
                .sTh("cal").add("THU").eTh()
                .sTh("cal").add("FRI").eTh()
                .sTh("caldim").add("SAT").eTh().eTr();
    }

    /**
     * Emits a calendar day cell for a day with optional tasks.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param month  the month name; null to omit
     * @param number the day number
     * @param tasks  tasks to present on the day; empty if no task
     */
    private static void emitDayCell(final HtmlBuilder htm, final String month, final int number,
                                    final Iterable<? extends Task> tasks) {

        htm.sTd("calday");

        if (month != null) {
            htm.sDiv("caldaymonth").add(month).eDiv();
        }

        htm.sDiv("caldaynum").add(Integer.toString(number)).eDiv().div("clear");

        for (final Task task : tasks) {
            task.emit(htm);
        }

        htm.eTd();
    }

    /**
     * Emits a dim day number for the calendar.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param number the day number
     */
    private static void emitDimDayCell(final HtmlBuilder htm, final int number) {

        htm.sTd("dimcalday").sDiv("dimcaldaynum").add(Integer.toString(number)).eDiv().div("clear").eTd();
    }

    /**
     * Emits a day number for a holiday in the calendar.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param number the day number
     * @param label  an optional label for the day
     */
    private static void emitHolidayCell(final HtmlBuilder htm, final int number, final String label) {

        htm.sTd("dimcalday").sDiv("calreddaynum").add(Integer.toString(number)).eDiv().div("clear");

        if (label != null) {
            htm.sDiv("callabel").add(label).eDiv();
        }

        htm.eTd();
    }

    /**
     * Emits an empty placeholder cell.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitEmptyCell(final HtmlBuilder htm) {

        htm.sTd("calday").eDiv().eTd();
    }

    /**
     * Emits an empty placeholder cell.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitEmptyDimCell(final HtmlBuilder htm) {

        htm.sTd("dimcalday").eDiv().eTd();
    }

    /**
     * A task that can appear in the calendar.
     */
    static class Task {

        /** The value of a "class='...'" property for the emitted task. */
        final String cls;

        /** The task name. */
        final String content;

        /** The value of a "style='...'" property for the emitted task. */
        final String style;

        /**
         * Constructs a new {@code Task}.
         *
         * @param theCls     the value of a "class" property for the emitted task
         * @param theContent the task content (HTML, perhaps a hyperlink)
         * @param theStyle   the value of a "style" property for the emitted task
         */
        Task(final String theCls, final String theContent, final String theStyle) {

            this.cls = theCls;
            this.content = theContent;
            this.style = theStyle;
        }

        /**
         * Emits the task.
         *
         * @param htm the {@code HtmlBuilder} to which to append
         */
        final void emit(final HtmlBuilder htm) {

            htm.sDiv(this.cls, "style='" + this.style + "'").add(this.content).eDiv();
        }
    }

    /**
     * A task that represents the User's Exam.
     */
    static final class UsersExamTask extends Task {

        /**
         * Constructs a new {@code UsersExamTask}.
         */
        UsersExamTask() {

            super("caltask", "<a href='users_exam.html'>User's Exam</a>", "background-color:#eee");
        }
    }

    /**
     * A task that represents a Skills Review exam in a course.
     */
    static final class SkillsReviewTask extends Task {

        /**
         * Constructs a new {@code SkillsReviewTask}.
         *
         * @param courseId the course ID
         * @param open     true if the course is currently open (links on buttons)
         */
        SkillsReviewTask(final String courseId, final boolean open) {

            super("caltask",
                    open ? SimpleBuilder.concat("<a href='course.html?course=", courseId.replace(" ", "%20"),
                            "&mode=course'>", SKILLS_REVIEW, "</a>") : SKILLS_REVIEW,
                    "background-color:#ffc");
        }
    }

    /**
     * A task that represents an objective and its associated assignment.
     */
    static final class ObjectiveTask extends Task {

        /**
         * Constructs a new {@code ObjectiveTask}.
         *
         * @param courseId  the course ID
         * @param objective the objective, like "1.1"
         * @param open      true if the course is currently open (links on buttons)
         */
        ObjectiveTask(final String courseId, final String objective, final boolean open) {

            super("caltask",
                    open ? SimpleBuilder.concat("<a href='course.html?course=", courseId.replace(" ", "%20"),
                            "&mode=course'>", OBJECTIVE, objective, "</a>")
                            : SimpleBuilder.concat(OBJECTIVE, objective), "background-color:#ffc");
        }
    }

    /**
     * A task that represents a unit review exam.
     */
    static final class UnitReviewTask extends Task {

        /**
         * Constructs a new {@code UnitReviewTask}.
         *
         * @param courseId the course ID
         * @param unit     the unit, like "1"
         * @param open     true if the course is currently open (links on buttons)
         */
        UnitReviewTask(final String courseId, final String unit, final boolean open) {

            super("caltask",
                    open ? SimpleBuilder.concat("<a href='course.html?course=", courseId.replace(" ", "%20"),
                            "&mode=course'>", UNIT, unit, REVIEW, "</a>")
                            : SimpleBuilder.concat(UNIT, unit, REVIEW), "background-color:#ffe5b2");
        }
    }

    /**
     * A task that represents a unit exam.
     */
    static final class UnitExamTask extends Task {

        /**
         * Constructs a new {@code UnitExamTask}.
         *
         * @param courseId the course ID
         * @param unit     the unit, like "1"
         * @param open     true if the course is currently open (links on buttons)
         */
        UnitExamTask(final String courseId, final String unit, final boolean open) {

            super("caltask",
                    open ? SimpleBuilder.concat("<a href='course.html?course=", courseId.replace(" ", "%20"),
                            "&mode=course'>", UNIT, unit, EXAM, "</a>")
                            : SimpleBuilder.concat(UNIT, unit, EXAM), "background-color:#ffe5b2");
        }
    }

    /**
     * A task that represents a final exam.
     */
    static final class FinalExamTask extends Task {

        /**
         * Constructs a new {@code FinalExamTask}.
         *
         * @param courseId the course ID
         * @param open     true if the course is currently open (links on buttons)
         */
        FinalExamTask(final String courseId, final boolean open) {

            super("caltask",
                    open ? SimpleBuilder.concat("<a href='course.html?course=", courseId.replace(" ", "%20"),
                            "&mode=course'>", FINAL_EXAM, "</a>") : FINAL_EXAM, "background-color:#ffe5b2");
        }
    }
}
