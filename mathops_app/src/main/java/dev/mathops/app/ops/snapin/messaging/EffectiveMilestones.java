package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.RawCampusCalendar;
import dev.mathops.db.schema.legacy.RawMilestone;
import dev.mathops.db.schema.legacy.RawStmilestone;
import dev.mathops.db.rec.TermWeekRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A container for the effective milestone dates that apply to a student in a single course.
 */
public class EffectiveMilestones {

    /** The user's exam milestone date; null if not the first course. */
    public LocalDate us;

    /** The Skills Review exam milestone date. */
    public LocalDate sr;

    /** The Homework 1.1 milestone date. */
    LocalDate h11;

    /** The Homework 1.2 milestone date. */
    LocalDate h12;

    /** The Homework 1.3 milestone date. */
    LocalDate h13;

    /** The Homework 1.4 milestone date. */
    LocalDate h14;

    /** The Homework 1.5 milestone date. */
    LocalDate h15;

    /** The Unit 1 Review Exam milestone date. */
    public final LocalDate r1;

    /** The Unit 1 Unit Exam milestone date. */
    public LocalDate u1;

    /** The Homework 2.1 milestone date. */
    LocalDate h21;

    /** The Homework 2.2 milestone date. */
    LocalDate h22;

    /** The Homework 2.3 milestone date. */
    LocalDate h23;

    /** The Homework 2.4 milestone date. */
    LocalDate h24;

    /** The Homework 2.5 milestone date. */
    LocalDate h25;

    /** The Unit 2 Review Exam milestone date. */
    public final LocalDate r2;

    /** The Unit 2 Unit Exam milestone date. */
    public LocalDate u2;

    /** The Homework 3.1 milestone date. */
    public LocalDate h31;

    /** The Homework 3.2 milestone date. */
    public LocalDate h32;

    /** The Homework 3.3 milestone date. */
    LocalDate h33;

    /** The Homework 3.4 milestone date. */
    public LocalDate h34;

    /** The Homework 3.5 milestone date. */
    LocalDate h35;

    /** The Unit 3 Review Exam milestone date. */
    public final LocalDate r3;

    /** The Unit 3 Unit Exam milestone date. */
    LocalDate u3;

    /** The Homework 4.1 milestone date. */
    LocalDate h41;

    /** The Homework 4.2 milestone date. */
    LocalDate h42;

    /** The Homework 4.3 milestone date. */
    LocalDate h43;

    /** The Homework 4.4 milestone date. */
    LocalDate h44;

    /** The Homework 4.5 milestone date. */
    public LocalDate h45;

    /** The Unit 4 Review Exam milestone date. */
    public final LocalDate r4;

    /** The Unit 4 Unit Exam milestone date. */
    public LocalDate u4;

    /** The Final Exam milestone date. */
    public final LocalDate fin;

    /** The Final Exam last try milestone date. */
    public LocalDate last;

    /** The number of "last try" attempts allowed. */
    final int lastTryCount;

    /**
     * Constructs a new {@code EffectiveMilestones}.
     *
     * @param cache   the data cache
     * @param pace    the pace
     * @param index   the index (from 1 to {@code pace})
     * @param context the messaging context
     */
    public EffectiveMilestones(final Cache cache, final int pace, final int index, final MessagingContext context) {

        final int base = 100 * pace;
        final int unit0 = base + 10 * index;
        final int unit1 = unit0 + 1;
        final int unit2 = unit0 + 2;
        final int unit3 = unit0 + 3;
        final int unit4 = unit0 + 4;
        final int unit5 = unit0 + 5;

        final List<RawMilestone> ms = context.milestones;
        final List<RawStmilestone> stuMs = context.studentMilestones;

        // The database will likely have milestones for RE1 through RE4, FE, and F1 - we need to
        // calculate the rest based on spread of dates.

        // We first build an ordered list of all days in the term (using semester_calendar and
        // HOLIDAY rows from campus_calendar). Then we convert the milestone dates into "day of
        // the term" integers, and then interpolate the missing deadlines
        final List<LocalDate> daysOfTerm = new ArrayList<>(75);

        try {
            final Collection<LocalDate> holidays = new ArrayList<>(10);

            final List<RawCampusCalendar> campusRows = cache.getSystemData().getCampusCalendarsByType(
                    RawCampusCalendar.DT_DESC_HOLIDAY);
            for (final RawCampusCalendar row : campusRows) {
                holidays.add(row.campusDt);
            }

            final List<TermWeekRec> semesterRows = cache.getSystemData().getTermWeeks();

            int maxWeek = 0;
            for (final TermWeekRec test : semesterRows) {
                maxWeek = Math.max(test.weekNbr.intValue(), maxWeek);
            }

            // Accumulate days from all weeks (in order) except week 0 and the last week
            for (int week = 1; week < maxWeek; ++week) {
                for (final TermWeekRec test : semesterRows) {
                    if (test.weekNbr.intValue() == week) {
                        // Start date is Sunday - move to Monday
                        final LocalDate monday = test.startDate.plusDays(1L);
                        daysOfTerm.add(monday);
                        final LocalDate tuesday = monday.plusDays(1L);
                        daysOfTerm.add(tuesday);
                        final LocalDate wednesday = tuesday.plusDays(1L);
                        daysOfTerm.add(wednesday);
                        final LocalDate thursday = wednesday.plusDays(1L);
                        daysOfTerm.add(thursday);
                        final LocalDate friday = thursday.plusDays(1L);
                        daysOfTerm.add(friday);
                    }
                }
            }

            daysOfTerm.removeAll(holidays);
        } catch (final SQLException ex) {
            Log.warning("Failed to build list of days of term", ex);
        }

        int r1Index = 0;
        int r2Index = 1;
        int r3Index = 2;
        int r4Index = 3;
        int feIndex = 4;

        this.r1 = findDate(ms, stuMs, RawMilestone.UNIT_REVIEW_EXAM, unit1);
        if (this.r1 == null) {
            Log.warning("No R1 milestone for pace " + pace + " track " + context.track);
        } else {
            r1Index = indexOf(daysOfTerm, this.r1);
            if (r1Index == -1) {
                Log.warning("Unable to find day index of R1=", this.r1);
            }
        }

        this.r2 = findDate(ms, stuMs, RawMilestone.UNIT_REVIEW_EXAM, unit2);
        if (this.r2 == null) {
            Log.warning("No R2 milestone for pace " + pace + " track " + context.track);
        } else {
            r2Index = indexOf(daysOfTerm, this.r2);
            if (r2Index == -1) {
                Log.warning("Unable to find day index of R2=", this.r2);
            }
        }

        this.r3 = findDate(ms, stuMs, RawMilestone.UNIT_REVIEW_EXAM, unit3);
        if (this.r3 == null) {
            Log.warning("No R3 milestone for pace " + pace + " track " + context.track);
        } else {
            r3Index = indexOf(daysOfTerm, this.r3);
            if (r3Index == -1) {
                Log.warning("Unable to find day index of R3=", this.r3);
            }
        }

        this.r4 = findDate(ms, stuMs, RawMilestone.UNIT_REVIEW_EXAM, unit4);
        if (this.r4 == null) {
            Log.warning("No R4 milestone for pace " + pace + " track " + context.track);
        } else {
            r4Index = indexOf(daysOfTerm, this.r4);
            if (r4Index == -1) {
                Log.warning("Unable to find day index of R4=", this.r4);
            }
        }

        this.fin = findDate(ms, stuMs, RawMilestone.FINAL_EXAM, unit5);
        if (this.fin == null) {
            Log.warning("No FE milestone for pace " + pace + " track " + context.track);
        } else {
            feIndex = indexOf(daysOfTerm, this.fin);
            if (feIndex == -1) {
                Log.warning("Unable to find day index of FE=", this.fin);
            }
        }

        this.last = findDate(ms, stuMs, RawMilestone.FINAL_LAST_TRY, unit5);
        if (this.last == null) {
            Log.warning("No F1 milestone for pace " + pace + " track " + context.track);
        }

//        Log.info("Pace ", context.pace, " Track ", context.track,
//                " R1=", r1Index, ", R2=", r2Index, ", R3=", r3Index, ", R4=", r4Index, ", FE=", feIndex);

        //

        this.us = index == 1 ? findDate(ms, stuMs, RawMilestone.USERS_EXAM, base) : null;
        if (index == 1 && this.us == null) {
            final int usIndex = r1Index / 2;
            this.us = daysOfTerm.get(usIndex);
        }

        this.sr = findDate(ms, stuMs, RawMilestone.SKILLS_REVIEW, unit0);
        if (this.sr == null) {
            final int srIndex = r1Index * 3 / 4;
            this.sr = daysOfTerm.get(srIndex);
        }

        this.h11 = findDate(ms, stuMs, RawMilestone.HOMEWORK_1, unit1);
        if (this.h11 == null) {
            final int h11Index = r1Index + (r2Index - r1Index) / 6;
            this.h11 = daysOfTerm.get(h11Index);
        }

        this.h12 = findDate(ms, stuMs, RawMilestone.HOMEWORK_2, unit1);
        if (this.h12 == null) {
            final int h12Index = r1Index + ((r2Index - r1Index) << 1) / 6;
            this.h12 = daysOfTerm.get(h12Index);
        }

        this.h13 = findDate(ms, stuMs, RawMilestone.HOMEWORK_3, unit1);
        if (this.h13 == null) {
            final int h13Index = r1Index + (r2Index - r1Index) * 3 / 6;
            this.h13 = daysOfTerm.get(h13Index);
        }

        this.h14 = findDate(ms, stuMs, RawMilestone.HOMEWORK_4, unit1);
        if (this.h14 == null) {
            final int h14Index = r1Index + ((r2Index - r1Index) << 2) / 6;
            this.h14 = daysOfTerm.get(h14Index);
        }

        this.h15 = findDate(ms, stuMs, RawMilestone.HOMEWORK_5, unit1);
        if (this.h15 == null) {
            final int h15Index = r1Index + (r2Index - r1Index) * 5 / 6;
            this.h15 = daysOfTerm.get(h15Index);
        }

        this.u1 = findDate(ms, stuMs, RawMilestone.UNIT_EXAM, unit1);
        if (this.u1 == null) {
            final int u1Index = Math.min(r1Index + 1, daysOfTerm.size() - 1);
            this.u1 = daysOfTerm.get(u1Index);
        }

        //

        this.h21 = findDate(ms, stuMs, RawMilestone.HOMEWORK_1, unit2);
        if (this.h21 == null) {
            final int h21Index = r2Index + (r3Index - r2Index) / 6;
            this.h21 = daysOfTerm.get(h21Index);
        }

        this.h22 = findDate(ms, stuMs, RawMilestone.HOMEWORK_2, unit2);
        if (this.h22 == null) {
            final int h22Index = r2Index + ((r3Index - r2Index) << 1) / 6;
            this.h22 = daysOfTerm.get(h22Index);
        }

        this.h23 = findDate(ms, stuMs, RawMilestone.HOMEWORK_3, unit2);
        if (this.h23 == null) {
            final int h23Index = r2Index + (r3Index - r2Index) * 3 / 6;
            this.h23 = daysOfTerm.get(h23Index);
        }

        this.h24 = findDate(ms, stuMs, RawMilestone.HOMEWORK_4, unit2);
        if (this.h24 == null) {
            final int h24Index = r2Index + ((r3Index - r2Index) << 2) / 6;
            this.h24 = daysOfTerm.get(h24Index);
        }

        this.h25 = findDate(ms, stuMs, RawMilestone.HOMEWORK_5, unit2);
        if (this.h25 == null) {
            final int h25Index = r2Index + (r3Index - r2Index) * 5 / 6;
            this.h25 = daysOfTerm.get(h25Index);
        }

        this.u2 = findDate(ms, stuMs, RawMilestone.UNIT_EXAM, unit2);
        if (this.u2 == null) {
            final int u2Index = Math.min(r1Index + 1, daysOfTerm.size() - 1);
            this.u2 = daysOfTerm.get(u2Index);
        }

        //

        this.h31 = findDate(ms, stuMs, RawMilestone.HOMEWORK_1, unit3);
        if (this.h31 == null) {
            final int h31Index = r3Index + (r4Index - r3Index) / 6;
            this.h31 = daysOfTerm.get(h31Index);
        }

        this.h32 = findDate(ms, stuMs, RawMilestone.HOMEWORK_2, unit3);
        if (this.h32 == null) {
            final int h32Index = r3Index + ((r4Index - r3Index) << 1) / 6;
            this.h32 = daysOfTerm.get(h32Index);
        }

        this.h33 = findDate(ms, stuMs, RawMilestone.HOMEWORK_3, unit3);
        if (this.h33 == null) {
            final int h33Index = r3Index + (r4Index - r3Index) * 3 / 6;
            this.h33 = daysOfTerm.get(h33Index);
        }

        this.h34 = findDate(ms, stuMs, RawMilestone.HOMEWORK_4, unit3);
        if (this.h34 == null) {
            final int h34Index = r3Index + ((r4Index - r3Index) << 2) / 6;
            this.h34 = daysOfTerm.get(h34Index);
        }

        this.h35 = findDate(ms, stuMs, RawMilestone.HOMEWORK_5, unit3);
        if (this.h35 == null) {
            final int h35Index = r3Index + (r4Index - r3Index) * 5 / 6;
            this.h35 = daysOfTerm.get(h35Index);
        }

        this.u3 = findDate(ms, stuMs, RawMilestone.UNIT_EXAM, unit3);
        if (this.u3 == null) {
            final int u3Index = Math.min(r3Index + 1, daysOfTerm.size() - 1);
            this.u3 = daysOfTerm.get(u3Index);
        }

        //

        this.h41 = findDate(ms, stuMs, RawMilestone.HOMEWORK_1, unit4);
        if (this.h41 == null) {
            final int h41Index = r4Index + (feIndex - r4Index) / 6;
            this.h41 = daysOfTerm.get(h41Index);
        }

        this.h42 = findDate(ms, stuMs, RawMilestone.HOMEWORK_2, unit4);
        if (this.h42 == null) {
            final int h42Index = r4Index + ((feIndex - r4Index) << 1) / 6;
            this.h42 = daysOfTerm.get(h42Index);
        }

        this.h43 = findDate(ms, stuMs, RawMilestone.HOMEWORK_3, unit4);
        if (this.h43 == null) {
            final int h43Index = r4Index + (feIndex - r4Index) * 3 / 6;
            this.h43 = daysOfTerm.get(h43Index);
        }

        this.h44 = findDate(ms, stuMs, RawMilestone.HOMEWORK_4, unit4);
        if (this.h44 == null) {
            final int h44Index = r4Index + ((feIndex - r4Index) << 2) / 6;
            this.h44 = daysOfTerm.get(h44Index);
        }

        this.h45 = findDate(ms, stuMs, RawMilestone.HOMEWORK_5, unit4);
        if (this.h45 == null) {
            final int h45Index = r4Index + (feIndex - r4Index) * 5 / 6;
            this.h45 = daysOfTerm.get(h45Index);
        }

        this.u4 = findDate(ms, stuMs, RawMilestone.UNIT_EXAM, unit4);
        if (this.u4 == null) {
            final int u4Index = Math.min(r4Index + 1, daysOfTerm.size() - 1);
            this.u4 = daysOfTerm.get(u4Index);
        }

        // Homeworks and unit exams are generally not moved when a review exam is moved (for
        // example, if the unit 2 review is moved back, the unit 2 exam and some unit 3 homework
        // deadlines can fall before the adjusted review deadline). Scan for these situations, and
        // move the homework/unit exam due dates to match preceding review exam due dates

        if (this.u1.isBefore(this.r1)) {
            this.u1 = this.r1;
        }
        if (this.h21.isBefore(this.r1)) {
            this.h21 = this.r1;
        }
        if (this.h22.isBefore(this.r1)) {
            this.h22 = this.r1;
        }
        if (this.h23.isBefore(this.r1)) {
            this.h23 = this.r1;
        }
        if (this.h24.isBefore(this.r1)) {
            this.h24 = this.r1;
        }
        if (this.h25.isBefore(this.r1)) {
            this.h25 = this.r1;
        }

        if (this.u2.isBefore(this.r2)) {
            this.u2 = this.r2;
        }
        if (this.h31.isBefore(this.r2)) {
            this.h31 = this.r2;
        }
        if (this.h32.isBefore(this.r2)) {
            this.h32 = this.r2;
        }
        if (this.h33.isBefore(this.r2)) {
            this.h33 = this.r2;
        }
        if (this.h34.isBefore(this.r2)) {
            this.h34 = this.r2;
        }
        if (this.h35.isBefore(this.r2)) {
            this.h35 = this.r2;
        }

        if (this.u3.isBefore(this.r3)) {
            this.u3 = this.r3;
        }
        if (this.h41.isBefore(this.r3)) {
            this.h41 = this.r3;
        }
        if (this.h42.isBefore(this.r3)) {
            this.h42 = this.r3;
        }
        if (this.h43.isBefore(this.r3)) {
            this.h43 = this.r3;
        }
        if (this.h44.isBefore(this.r3)) {
            this.h44 = this.r3;
        }
        if (this.h45.isBefore(this.r3)) {
            this.h45 = this.r3;
        }

        if (this.u4.isBefore(this.r4)) {
            this.u4 = this.r4;
        }

        if (!this.last.isAfter(this.fin)) {
            this.last = this.fin;
        }

        Integer tries = null;
        for (final RawMilestone msRow : ms) {
            if (msRow.msType.equals(RawMilestone.FINAL_LAST_TRY) && msRow.msNbr.intValue() == unit5
                && msRow.nbrAtmptsAllow != null) {
                tries = msRow.nbrAtmptsAllow;
            }
        }

        if (tries != null) {
            for (final RawStmilestone stuMsRow : stuMs) {
                if (stuMsRow.msType.equals(RawMilestone.FINAL_LAST_TRY)
                    && stuMsRow.msNbr.intValue() == unit5 && stuMsRow.nbrAtmptsAllow != null) {
                    tries = stuMsRow.nbrAtmptsAllow;
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }
        }

        this.lastTryCount = tries == null ? 0 : tries.intValue();
    }

    /**
     * Finds the index of the first day of the term that falls on or after a date.
     *
     * @param daysOfTerm the list of days of the term
     * @param date       the date to test
     * @return the index (the index of the day after the last day of the term if the given date falls after every date
     *         in the list)
     */
    private static int indexOf(final List<LocalDate> daysOfTerm, final LocalDate date) {

        int index = daysOfTerm.indexOf(date);

        if (index == -1) {
            final int count = daysOfTerm.size();

            index = count;
            for (int i = 0; i < count; ++i) {
                if (!daysOfTerm.get(i).isBefore(date)) {
                    index = i;
                    break;
                }
            }
        }

        return index;
    }

    /**
     * Uses the Milestone and StMilestone records to generate a due date.
     *
     * @param milestones   the list of all milestones for the student's pace and track
     * @param stmilestones the list of student overrides (if any)
     * @param type         the milestone type whose date to find
     * @param number       the milestone number whose date to find
     * @return the milestone date
     */
    private static LocalDate findDate(final Iterable<RawMilestone> milestones,
                                      final Iterable<RawStmilestone> stmilestones, final String type,
                                      final int number) {

        LocalDate due = null;

        for (final RawMilestone ms : milestones) {
            if (ms.msType.equals(type) && ms.msNbr.intValue() == number) {
                due = ms.msDate;
            }
        }

        if (due != null) {
            for (final RawStmilestone sms : stmilestones) {
                if (sms.msType.equals(type) && sms.msNbr.intValue() == number) {
                    due = sms.msDate;
                    // Don't break - student milestones are sorted by deadline date, and if there are multiple, we want
                    // the later date
                }
            }
        }

        return due;
    }
}
