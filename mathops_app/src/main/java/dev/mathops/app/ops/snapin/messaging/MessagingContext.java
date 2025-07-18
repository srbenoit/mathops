package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.db.logic.course.PrerequisiteLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A container class for the data used to select and construct messages.
 */
public final class MessagingContext {

    /** The active term. */
    public final TermRec activeTerm;

    /** The student object. */
    public final RawStudent student;

    /** The student's pace. */
    public final int pace;

    /** The student's pace track. */
    public final String track;

    /** The student-term object. */
    final RawStterm studentTerm;

    /** The student's registrations. */
    private final List<RawStcourse> registrations;

    /** Registrations, sorted by pace order. */
    public final List<RawStcourse> sortedRegs;

    /** Index of the "current" registration. */
    public final int currentRegIndex;

    /** The current date. */
    public final LocalDate today;

    /** The last day in the term to test. */
    public final LocalDate lastDayToTest;

    /** The day of the term (1 for the first day of the term, etc.). */
    private final int dayOfTerm;

    /** The list of all milestones. */
    public final List<RawMilestone> milestones;

    /** The list of all student-milestone overrides. */
    final List<RawStmilestone> studentMilestones;

    /** The list of all exams the student has taken. */
    public final List<RawStexam> exams;

    /** The list of all homeworks the student has submitted. */
    public final List<RawSthomework> homeworks;

    /** The list of all messages the student has been sent. */
    public final List<RawStmsg> messages;

    /** Prerequisites logic object used to test whether prerequisites have been met. */
    public final PrerequisiteLogic prereqLogic;

    /** The list of special categories to which the student belongs. */
    public final List<RawSpecialStus> specials;

    /**
     * Constructs a new {@code MessagingContext}.
     *
     * @param theActiveTerm        the active term
     * @param theStudent           the student
     * @param thePace              the student's pace
     * @param theTrack             the student's pace track
     * @param theStudentTerm       the student term
     * @param theRegistrations     the student's registrations
     * @param theToday             the current date
     * @param theMilestones        the milestones for the course
     * @param theStudentMilestones the student milestone overrides
     * @param theExams             the student's exams
     * @param theHomeworks         the student's homeworks
     * @param theMessages          the messages sent to the student so far
     * @param theSpecials          the special student categories to which the student belongs
     * @param theLastClassDay      the last day of classes
     * @param thePrereqLogic       the prerequisite logic
     */
    public MessagingContext(final TermRec theActiveTerm, final RawStudent theStudent,
                            final int thePace, final String theTrack, final RawStterm theStudentTerm,
                            final List<RawStcourse> theRegistrations, final LocalDate theToday,
                            final List<RawMilestone> theMilestones, final List<RawStmilestone> theStudentMilestones,
                            final List<RawStexam> theExams, final List<RawSthomework> theHomeworks,
                            final List<RawStmsg> theMessages, final List<RawSpecialStus> theSpecials,
                            final LocalDate theLastClassDay, final PrerequisiteLogic thePrereqLogic) {

        this.activeTerm = theActiveTerm;
        this.student = theStudent;
        this.pace = thePace;
        this.track = theTrack;
        this.studentTerm = theStudentTerm;
        this.registrations = theRegistrations;
        this.today = theToday;
        this.milestones = theMilestones;
        this.studentMilestones = theStudentMilestones;
        this.exams = theExams;
        this.homeworks = theHomeworks;
        this.messages = theMessages;
        this.specials = theSpecials;
        this.prereqLogic = thePrereqLogic;

        if (thePace == 5) {
            this.sortedRegs = EmailsNeeded.sort5ByPaceOrder(new ArrayList<>(theRegistrations));
        } else if (thePace == 4) {
            this.sortedRegs = EmailsNeeded.sort4ByPaceOrder(new ArrayList<>(theRegistrations));
        } else if (thePace == 3) {
            this.sortedRegs = EmailsNeeded.sort3ByPaceOrder(new ArrayList<>(theRegistrations));
        } else if (thePace == 2) {
            this.sortedRegs = EmailsNeeded.sort2ByPaceOrder(new ArrayList<>(theRegistrations));
        } else {
            this.sortedRegs = new ArrayList<>(theRegistrations);
        }

        int index = 0;
        for (int test = this.sortedRegs.size() - 1; test >= 0; --test) {
            if ("Y".equals(this.sortedRegs.get(test).openStatus)) {
                index = test;
                break;
            }
        }

        // "current reg" is now the last open course - but if that course is finished, and there
        // is a next course, consider that next course "current".
        if ("Y".equals(this.sortedRegs.get(index).completed)
                && this.sortedRegs.size() >= index + 1) {
            ++index;
        }

        if (index >= this.sortedRegs.size()) {
            this.currentRegIndex = this.sortedRegs.size() - 1;
        } else {
            this.currentRegIndex = index;
        }

        int day;
        LocalDate date = this.activeTerm.startDate;
        if (theToday.isBefore(date)) {
            day = 0;
        } else {
            day = 1;
            while (!date.isAfter(theToday)) {
                final DayOfWeek weekday = date.getDayOfWeek();
                if (weekday.ordinal() < 5) {
                    ++day;
                }

                date = date.plusDays(1L);
            }
        }
        this.dayOfTerm = day;

        this.lastDayToTest = theLastClassDay;
    }
}
