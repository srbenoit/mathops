package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.IProgressListener;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.TermWeekRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A program that goes back several terms, runs "urgency" calculations for every student on every day of the term, and
 * generates a large CSV file with student ID and urgency values at the end of each day of the term (15 * 5 days).
 *
 * <p>
 * This data can then be averaged and standard deviations found so trend lines can be plotted for each term, and
 * compared term-on-term or year-on-year.
 */
final class UrgencyHistoryAnalysis {

    /** The data cache. */
    private final Cache cache;

    /** An optional progress listener. */
    private final IProgressListener listener;

    /** Flag that can cancel an in-progress scan. */
    private final AtomicBoolean canceled;

    /** The active term. */
    private TermRec active;

    /** All registrations in the term, including dropped. */
    private List<RawStcourse> registrations;

    /** All milestones in the term. */
    private List<RawMilestone> milestones;

    /** Map from student ID to effective registration in each course. */
    private Map<String, List<RawStcourse>> studentRegs;

    /**
     * Constructs a new {@code UrgencyHistoryAnalysis}.
     *
     * @param theCache    the data cache
     * @param theListener an optional progress listener
     */
    private UrgencyHistoryAnalysis(final Cache theCache, final IProgressListener theListener) {

        this.cache = theCache;
        this.listener = theListener;
        this.canceled = new AtomicBoolean(false);
    }

    /**
     * Cancels the operations.
     */
    public void cancel() {

        this.canceled.set(true);
    }

    /**
     * Executes the job.
     *
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     */
    private void calculate(final Map<String, ? extends List<String>> incCourseSections) {

        // In terms of progress, the first 1% will be loading data, then next 99 will be scanning
        // students.

        final File desktop = new File("/Users/benoit/Desktop");

        final List<StudentReportRow> stuReportRows = new ArrayList<>(2400);

        this.canceled.set(false);

        try {
            gatherOneTimeInformation();
            gatherStudentRegs(incCourseSections);

            for (final Map.Entry<String, List<RawStcourse>> entry : this.studentRegs.entrySet()) {
                final String stuId = entry.getKey();
                final List<RawStcourse> stuRegs = entry.getValue();

                // Sort by course ID - this provides default pace ordering for courses with no work
                Collections.sort(stuRegs);

                processStudent(stuId, stuRegs, stuReportRows);
            }

        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        final int numDays = stuReportRows.getFirst().pace.length;
        final int numStudents = stuReportRows.size();
        final float[] average = new float[numDays];

        final String prefix = this.active.term.shortString;
        final HtmlBuilder htm = new HtmlBuilder(200 * stuReportRows.size());

        // REPORT 1: student pace by day

        // startReport(htm, numDays);
        // for (final StudentReportRow row : stuReportRows) {
        // htm.add(row.stuId);
        // for (int i = 0; i < numDays; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(row.pace[i]);
        // average[i] += row.pace[i];
        // }
        // htm.addln();
        // }
        // htm.add("AVERAGE");
        // for (int i = 0; i < numDays; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(average[i] / numStudents);
        // }
        // htm.addln();
        // writeReport(new File(desktop, prefix + "_pace_by_day.csv"), htm.toString());
        // for (int i = 0; i < numDays; ++i) {
        // average[i] = 0.0f;
        // }

        // REPORT 2: number of RE passed on-time by day

        // startReport(htm, numDays);
        // for (final StudentReportRow row : stuReportRows) {
        // htm.add(row.stuId);
        // for (int i = 0; i < row.pace.length; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(row.reviewsPassedOnTime[i]);
        // }
        // htm.addln();
        // }
        // writeReport(new File(desktop, prefix + "_re_passed_ontime_by_day.csv"), htm.toString());
        // for (int i = 0; i < numDays; ++i) {
        // average[i] = 0.0f;
        // }

        // startReport(htm, numDays);
        // for (final StudentReportRow row : stuReportRows) {
        // htm.add(row.stuId);
        // for (int i = 0; i < row.pace.length; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(row.reviewsPassedLate[i]);
        // }
        // htm.addln();
        // }
        // writeReport(new File(desktop, prefix + "_re_passed_late_by_day.csv"), htm.toString());
        // for (int i = 0; i < numDays; ++i) {
        // average[i] = 0.0f;
        // }

        // startReport(htm, numDays);
        // for (final StudentReportRow row : stuReportRows) {
        // htm.add(row.stuId);
        // for (int i = 0; i < row.pace.length; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(row.reviewsPassedOnTime[i] + row.reviewsPassedLate[i]);
        // }
        // htm.addln();
        // }
        // writeReport(new File(desktop, prefix + "_re_passed_by_day.csv"), htm.toString());
        // for (int i = 0; i < numDays; ++i) {
        // average[i] = 0.0f;
        // }

        // startReport(htm, numDays);
        // for (final StudentReportRow row : stuReportRows) {
        // htm.add(row.stuId);
        // for (int i = 0; i < row.pace.length; ++i) {
        // htm.add(CoreConstants.COMMA);
        // htm.add(row.reviewsDueNotPassed[i]);
        // }
        // htm.addln();
        // }
        // writeReport(new File(desktop, prefix + "_re_missed_by_day.csv"), htm.toString());
        // for (int i = 0; i < numDays; ++i) {
        // average[i] = 0.0f;
        // }

        startReport(htm, numDays);
        for (final StudentReportRow row : stuReportRows) {

            htm.add(row.stuId);
            for (int i = 0; i < numDays; ++i) {
                final float passed = (float) (row.reviewsPassedOnTime[i] + row.reviewsPassedLate[i]);
                final float missed = (float) row.reviewsDueNotPassed[i];
                final float due = passed + missed;
                final float rate = due <= 0.0f ? 1.0f : (passed / due);

                htm.add(CoreConstants.COMMA);
                htm.add(rate);

                average[i] += rate;
            }
            htm.addln();

        }
        htm.add("AVERAGE");
        for (int i = 0; i < numDays; ++i) {
            htm.add(CoreConstants.COMMA);
            htm.add(average[i] / (float) numStudents);
        }
        htm.addln();
        writeReport(new File(desktop, prefix + "_re_completion_rate_by_day.csv"), htm.toString());
    }

    /**
     * Starts a report.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param numDays the number of days
     */
    private static void startReport(final HtmlBuilder htm, final int numDays) {

        htm.reset();
        htm.add("Student");
        for (int i = 0; i < numDays; ++i) {
            htm.add(CoreConstants.COMMA);
            htm.add("Day " + (i + 1));
        }
        htm.addln();
    }

    /**
     * Writes a report file.
     *
     * @param target  the target file
     * @param content the report content
     */
    private static void writeReport(final File target, final String content) {

        try (final FileWriter w = new FileWriter(target, StandardCharsets.UTF_8)) {
            w.write(content);
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Collects "one-time" information, including all student registrations, all milestones, and the active term row for
     * each term database.
     *
     * @throws SQLException if there is an error accessing the database
     */
    private void gatherOneTimeInformation() throws SQLException {

        final SystemData systemData = this.cache.getSystemData();

        fireProgress("Querying active term", 1);
        this.active = systemData.getActiveTerm();
        Log.info("    Active term is ", this.active.term);

        fireProgress("Querying registrations term", 2);
        this.registrations = RawStcourseLogic.queryByTerm(this.cache, this.active.term, false, false);
        Log.info("    Queried ", Integer.toString(this.registrations.size()), " registrations");

        for (final RawStcourse reg : this.registrations) {
            if ("D".equals(reg.openStatus)) {
                Log.info("Dropped after " + reg.lastClassRollDt);
            }
        }

        fireProgress("Querying milestones", 4);
        this.milestones = systemData.getMilestones(this.active.term);
        Log.info("    Queried ", Integer.toString(this.milestones.size()), " milstones");
    }

    /**
     * Gathers registrations by student and filters so there is (at most) one registration in each course for each
     * student. Dropped registrations are included - if there are multiple dropped for a single course, the one with the
     * latest date is retained.
     *
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     */
    private void gatherStudentRegs(final Map<String, ? extends List<String>> incCourseSections) {

        this.studentRegs = new HashMap<>(5000);

        // Organize by student, filtering out registrations that are not in "included" list
        for (final RawStcourse reg : this.registrations) {
            final List<String> sections = incCourseSections.get(reg.course);

            if (sections != null && sections.contains(reg.sect)) {
                final List<RawStcourse> list = this.studentRegs.computeIfAbsent(reg.stuId, s -> new ArrayList<>(5));
                list.add(reg);
            }
        }

        // Filter. If a non-dropped reg exist, remove all dropped for that course. If there are
        // multiple dropped keep only the one with the latest date
        final Map<String, RawStcourse> latestDropped = new HashMap<>(incCourseSections.size());
        final Map<String, RawStcourse> notDropped = new HashMap<>(incCourseSections.size());

        for (final Map.Entry<String, List<RawStcourse>> entry : this.studentRegs.entrySet()) {
            final List<RawStcourse> stuRegs = entry.getValue();

            for (final RawStcourse row : stuRegs) {
                if (row.iDeadlineDt != null) {
                    continue;
                }

                final String course = row.course;

                if ("D".equals(row.openStatus) || "G".equals(row.openStatus)) {
                    final RawStcourse latest = latestDropped.get(course);
                    if (latest == null || latest.lastClassRollDt.isAfter(row.lastClassRollDt)) {
                        latestDropped.put(course, row);
                    }
                } else {
                    if (notDropped.get(course) != null) {
                        Log.warning("Multiple active rows for ", row.stuId, " in ", course, "/", row.sect);
                    }
                    notDropped.put(course, row);
                }
            }

            for (final String course : incCourseSections.keySet()) {
                if (notDropped.containsKey(course)) {
                    latestDropped.remove(course);
                }
            }

            stuRegs.clear();
            stuRegs.addAll(notDropped.values());
            stuRegs.addAll(latestDropped.values());

            latestDropped.clear();
            notDropped.clear();
        }
    }

    /**
     * Processes a single student. This generates a single report row with entries for each day.
     *
     * @param stuId      the student ID
     * @param stuRegs    the list of student registrations
     * @param reportRows the list to which to add report rows
     * @throws SQLException if there is an error accessing the database
     */
    private void processStudent(final String stuId, final Iterable<RawStcourse> stuRegs,
                                final Collection<? super StudentReportRow> reportRows) throws SQLException {

        final List<TermWeekRec> weeks = this.cache.getSystemData().getTermWeeks();
        Collections.sort(weeks);

        final List<RawStexam> stexams = RawStexamLogic.queryByStudent(this.cache, stuId, true);
        final List<RawStmilestone> stmilestones = RawStmilestoneLogic.getStudentMilestones(this.cache,
                this.active.term, stuId);
        stmilestones.sort(null);

        final int maxWeek = this.active.term.name == ETermName.SUMMER ? 12 : 15;
        final StudentReportRow studentRow = new StudentReportRow(stuId, maxWeek * 5);
        reportRows.add(studentRow);

        final List<RawCampusCalendar> holidayRows = this.cache.getSystemData().getCampusCalendarsByType(
                RawCampusCalendar.DT_DESC_HOLIDAY);
        final Collection<LocalDate> holidays = new ArrayList<>(holidayRows.size());
        for (final RawCampusCalendar holidayRow : holidayRows) {
            holidays.add(holidayRow.campusDt);
        }

        int dayIndex = 0;
        for (final TermWeekRec week : weeks) {
            final int weekNumber = week.weekNbr.intValue();
            if (weekNumber == 0 || weekNumber > maxWeek) {
                continue;
            }

            LocalDate day = week.startDate;
            while (!day.isAfter(week.endDate)) {
                if (day.getDayOfWeek() == DayOfWeek.SUNDAY
                    || day.getDayOfWeek() == DayOfWeek.SATURDAY || holidays.contains(day)) {
                    day = day.plusDays(1L);
                    continue;
                }

                // Log.info("Week " + weekNumber + " day index "
                // + dayIndex+ " is " + TemporalUtils.FMT_WMDY.format(day));

                processStudentDay(stuRegs, day, dayIndex, studentRow, stexams, stmilestones);
                ++dayIndex;

                day = day.plusDays(1L);
            }
        }
    }

    /**
     * Processes a student's status on a single day
     *
     * @param stuRegs          the list of student registrations
     * @param day              the day
     * @param studentReportRow the student report row
     * @param dayIndex         the day index
     * @param stexams          the list of student exams
     * @param stmilestones     the list of student milestones
     */
    private void processStudentDay(final Iterable<RawStcourse> stuRegs, final LocalDate day, final int dayIndex,
                                   final StudentReportRow studentReportRow, final Iterable<RawStexam> stexams,
                                   final Iterable<RawStmilestone> stmilestones) {

        // As the "day" passes the last roll date on stcourse rows, we remove those rows
        final Iterator<RawStcourse> iter = stuRegs.iterator();
        while (iter.hasNext()) {
            final RawStcourse row = iter.next();
            if ("G".equals(row.openStatus)) {
                iter.remove();
            }
            if ("D".equals(row.openStatus) && row.lastClassRollDt != null
                && row.lastClassRollDt.isBefore(day)) {
                iter.remove();
            }
            row.paceOrder = null;
        }

        // The "RawStcourse" records that remain represent the user's pace at this point
        // Assign them a pace order based on the order in which work was performed (we only need to
        // check "stexam" since first course activity is a SR exam
        LocalDate first117 = null;
        LocalDate first118 = null;
        LocalDate first124 = null;
        LocalDate first125 = null;
        LocalDate first126 = null;
        for (final RawStexam stexam : stexams) {
            if (RawRecordConstants.M117.equals(stexam.course)
                || RawRecordConstants.MATH117.equals(stexam.course)) {
                if (first117 == null || first117.isAfter(stexam.examDt)) {
                    first117 = stexam.examDt;
                }
            } else if (RawRecordConstants.M118.equals(stexam.course)
                       || RawRecordConstants.MATH118.equals(stexam.course)) {
                if (first118 == null || first118.isAfter(stexam.examDt)) {
                    first118 = stexam.examDt;
                }
            } else if (RawRecordConstants.M124.equals(stexam.course)
                       || RawRecordConstants.MATH124.equals(stexam.course)) {
                if (first124 == null || first124.isAfter(stexam.examDt)) {
                    first124 = stexam.examDt;
                }
            } else if (RawRecordConstants.M125.equals(stexam.course)
                       || RawRecordConstants.MATH125.equals(stexam.course)) {
                if (first125 == null || first125.isAfter(stexam.examDt)) {
                    first125 = stexam.examDt;
                }
            } else if (RawRecordConstants.M126.equals(stexam.course)
                       || RawRecordConstants.MATH126.equals(stexam.course)
                          && (first126 == null || first126.isAfter(stexam.examDt))) {
                first126 = stexam.examDt;
            }
        }

        // Sort course numbers based on first activity date
        final Map<LocalDate, String> sortedCourses = new TreeMap<>();
        if (first117 != null) {
            sortedCourses.put(first117, RawRecordConstants.M117);
        }
        if (first118 != null) {
            sortedCourses.put(first118, RawRecordConstants.M118);
        }
        if (first124 != null) {
            sortedCourses.put(first124, RawRecordConstants.M124);
        }
        if (first125 != null) {
            sortedCourses.put(first125, RawRecordConstants.M125);
        }
        if (first126 != null) {
            sortedCourses.put(first126, RawRecordConstants.M126);
        }

        int paceOrder = 1;
        // Assign pace orders in courses where work was performed, in work order
        for (final String courseId : sortedCourses.values()) {
            for (final RawStcourse reg : stuRegs) {
                if (reg.course.equals(courseId)) {
                    reg.paceOrder = Integer.valueOf(paceOrder);
                    ++paceOrder;
                    break;
                }
            }
        }
        // Assign pace orders to rest of courses in course number order
        for (final RawStcourse reg : stuRegs) {
            if (reg.paceOrder == null) {
                reg.paceOrder = Integer.valueOf(paceOrder);
                ++paceOrder;
            }
        }

        // We now have our best guess for the set of courses, and pace order settings, that the
        // student would have been working under on the day in question

        final int pace = PaceTrackLogic.determinePace(stuRegs);
        studentReportRow.pace[dayIndex] = pace;
        studentReportRow.date[dayIndex] = day;

        final String track = PaceTrackLogic.determinePaceTrack(stuRegs, pace);

        final Collection<RawStmilestone> effStMilestones = new ArrayList<>(10);
        for (final RawStmilestone row : stmilestones) {
            if (row.getPace() == pace && row.paceTrack.equals(track)) {
                effStMilestones.add(row);
            }
        }

        int passedOnTime = 0;
        int passedLate = 0;
        int dueNotPassed = 0;

        for (int index = 1; index <= pace; ++index) {
            final LocalDate r1 = getREDueDate(pace, track, index, effStMilestones, 1);
            final LocalDate r2 = getREDueDate(pace, track, index, effStMilestones, 2);
            final LocalDate r3 = getREDueDate(pace, track, index, effStMilestones, 3);
            final LocalDate r4 = getREDueDate(pace, track, index, effStMilestones, 4);

            RawStcourse currentReg = null;
            for (final RawStcourse reg : stuRegs) {
                if (reg.paceOrder != null && reg.paceOrder.intValue() == index) {
                    currentReg = reg;
                    break;
                }
            }

            if (currentReg == null) {
                Log.warning("No current reg - should not happen");
                // Course not opened - no exams
                if (r1.isBefore(day)) {
                    ++dueNotPassed;
                }
                if (r2.isBefore(day)) {
                    ++dueNotPassed;
                }
                if (r3.isBefore(day)) {
                    ++dueNotPassed;
                }
                if (r4.isBefore(day)) {
                    ++dueNotPassed;
                }
            } else {
                if (r1.isBefore(day)) {
                    if (passedRE(currentReg, stexams, 1, r1)) {
                        ++passedOnTime;
                    } else if (passedRE(currentReg, stexams, 1, day)) {
                        ++passedLate;
                    } else {
                        ++dueNotPassed;
                    }
                }
                if (r2.isBefore(day)) {
                    if (passedRE(currentReg, stexams, 2, r2)) {
                        ++passedOnTime;
                    } else if (passedRE(currentReg, stexams, 2, day)) {
                        ++passedLate;
                    } else {
                        ++dueNotPassed;
                    }
                }
                if (r3.isBefore(day)) {
                    if (passedRE(currentReg, stexams, 3, r3)) {
                        ++passedOnTime;
                    } else if (passedRE(currentReg, stexams, 3, day)) {
                        ++passedLate;
                    } else {
                        ++dueNotPassed;
                    }
                }
                if (r4.isBefore(day)) {
                    if (passedRE(currentReg, stexams, 4, r4)) {
                        ++passedOnTime;
                    } else if (passedRE(currentReg, stexams, 4, day)) {
                        ++passedLate;
                    } else {
                        ++dueNotPassed;
                    }
                }
            }
        }

        studentReportRow.reviewsPassedOnTime[dayIndex] = passedOnTime;
        studentReportRow.reviewsPassedLate[dayIndex] = passedLate;
        studentReportRow.reviewsDueNotPassed[dayIndex] = dueNotPassed;

    }

    /**
     * Tests whether the student had passed a review exam on or before a given date
     *
     * @param reg     the course registration
     * @param stexams the list of exams student has submitted
     * @param unit    the unit number
     * @param date    the date
     * @return true if a review exam in the unit was passed on or before the date
     */
    private static boolean passedRE(final RawStcourse reg, final Iterable<RawStexam> stexams,
                                    final int unit, final ChronoLocalDate date) {

        boolean passedByDate = false;

        for (final RawStexam row : stexams) {
            if (row.stuId.equals(reg.stuId) && row.course.equals(reg.course)
                && row.unit.intValue() == unit && "R".equals(row.examType)
                && "Y".equals(row.passed) && !row.examDt.isAfter(date)) {

                passedByDate = true;
                break;
            }
        }

        return passedByDate;
    }

    /**
     * Attempts to find a Review exam due date.
     *
     * @param pace            the pace
     * @param track           the pace track
     * @param index           the index
     * @param effStMilestones the effective student milestones
     * @param unit            the unit number
     * @return the review exam due date
     */
    private LocalDate getREDueDate(final int pace, final String track, final int index,
                                   final Iterable<RawStmilestone> effStMilestones, final int unit) {

        LocalDate result = null;

        final int msNbr = pace * 100 + index * 10 + unit;

        for (final RawMilestone test : this.milestones) {
            if (test.pace.intValue() == pace && test.paceTrack.equals(track)
                && test.msNbr.intValue() == msNbr && "RE".equals(test.msType)) {
                result = test.msDate;
                break;
            }
        }

        if (result == null) {
            Log.warning("Cannot find due date for RE " + unit + " for course " + index + " in pace " + pace
                        + " track" + track);
        } else {
            for (final RawStmilestone test : effStMilestones) {
                if (test.paceTrack.equals(track) && test.msNbr.intValue() == msNbr
                    && "RE".equals(test.msType) && test.msDate.isAfter(result)) {
                    result = test.msDate;
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }
        }

        return result;
    }

    /**
     * Fires a progress notification if there is a listener installed.
     *
     * @param description a description of the current step
     * @param done        the number of steps done
     */
    private void fireProgress(final String description, final int done) {

        // Log.info(description);
        if (this.listener != null) {
            this.listener.progress(description, done, 200);
        }
    }

    /**
     * A single student row in the report.
     */
    static final class StudentReportRow {

        /** The student ID. */
        final String stuId;

        /** The student pace. */
        final int[] pace;

        /** The date. */
        final LocalDate[] date;

        /** Number of reviews passed on time. */
        final int[] reviewsPassedOnTime;

        /** Number of reviews passed late. */
        final int[] reviewsPassedLate;

        /** Number of reviews due but not yet passed. */
        final int[] reviewsDueNotPassed;

        /**
         * Constructs a new {@code StudentReportRow}.
         *
         * @param theStuId   the student ID
         * @param theNumDays the number of days
         */
        StudentReportRow(final String theStuId, final int theNumDays) {

            this.stuId = theStuId;
            this.pace = new int[theNumDays];
            this.date = new LocalDate[theNumDays];
            this.reviewsPassedOnTime = new int[theNumDays];
            this.reviewsPassedLate = new int[theNumDays];
            this.reviewsDueNotPassed = new int[theNumDays];
        }
    }

    /**
     * Executes the analysis for a specified term.
     *
     * @param profile           the profile
     * @param incCourseSections map from course ID to a list of section numbers to include in the scan
     */
    private static void executeWithProfile(final Profile profile,
                                           final Map<String, ? extends List<String>> incCourseSections) {

        if (profile == null) {
            Log.warning("Unable to create production context.");
        } else {
            final Cache cache = new Cache(profile);
            Log.info("Connected to " + profile.id);

            final UrgencyHistoryAnalysis obj = new UrgencyHistoryAnalysis(cache, null);
            obj.calculate(incCourseSections);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final Map<String, List<String>> incCourseSections = new HashMap<>(10);

        final String[] sections = {"001", "002", "401", "801", "809"};
        final List<String> sect117 = Arrays.asList(sections);
        final List<String> sect118 = Arrays.asList(sections);
        final List<String> sect124 = Arrays.asList(sections);
        final List<String> sect125 = Arrays.asList(sections);
        final List<String> sect126 = Arrays.asList(sections);

        incCourseSections.put(RawRecordConstants.M117, sect117);
        incCourseSections.put(RawRecordConstants.M118, sect118);
        incCourseSections.put(RawRecordConstants.M124, sect124);
        incCourseSections.put(RawRecordConstants.M125, sect125);
        incCourseSections.put(RawRecordConstants.M126, sect126);

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);

        executeWithProfile(profile, incCourseSections);
    }
}
