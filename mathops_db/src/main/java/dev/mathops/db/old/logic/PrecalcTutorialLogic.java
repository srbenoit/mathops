package dev.mathops.db.old.logic;

import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawCampusCalendarLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for logic relating to a student's status with respect to the ELM and Precalculus Tutorials. This object
 * does not provide eligibility for placement or challenge exams.
 *
 * <p>
 * The general design is that a student must complete the Math Placement Tool before gaining access to the ELM Tutorial,
 * and must be eligible for a course in order to access the corresponding Precalculus Tutorial. Precalculus Tutorials
 * are available for a limited date range (Summers and the start of the Fall term until the add deadline), and only to
 * incoming first-year students.
 */
public class PrecalcTutorialLogic {

    /** The student ID. */
    private final String studentId;

    /** The student record. */
    private final RawStudent student;

    /** The list of all student attempts on ELM unit exams. */
    private final List<RawStmpe> allPlacementAttempts;

    /** The list of placement or credit earned. */
    private final List<RawMpeCredit> allCreditEarned;

    /** All special categories to which the student currently belongs. */
    private final List<RawSpecialStus> allSpecials;

    /** The student's status with respect to the math placement tool. */
    public final PrecalcTutorialStatus status;

    /**
     * Constructs a new {@code PrecalcTutorialLogic}.
     *
     * @param cache        the data cache
     * @param theStudentId the student ID
     * @param today        the date/time to consider "now"
     * @param prereqLogic  prerequisite logic
     * @throws SQLException if there is an error accessing the database
     */
    public PrecalcTutorialLogic(final Cache cache, final String theStudentId, final LocalDate today,
                                final PrerequisiteLogic prereqLogic) throws SQLException {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date may not be null");
        }

        this.studentId = theStudentId;

        this.student = RawStudentLogic.query(cache, this.studentId, false);

        // We need placement attempts and credit to determine tutorial eligibility
        this.allPlacementAttempts = RawStmpeLogic.queryLegalByStudent(cache, this.studentId);

        this.allCreditEarned = RawMpeCreditLogic.queryByStudent(cache, this.studentId);

        // Special student status can allow a non-incoming student to access Precalc Tutorials
        this.allSpecials = RawSpecialStusLogic.queryActiveByStudent(cache, this.studentId, today);

        this.status = new PrecalcTutorialStatus();

        computeStatus(cache, prereqLogic, today);
    }

    /**
     * Constructs a new {@code PrecalcTutorialLogic} when the student record is present.
     *
     * @param cache       the data cache
     * @param theStudent  the student record
     * @param today       the date/time to consider "now"
     * @param prereqLogic prerequisite logic
     * @throws SQLException if there is an error accessing the database
     */
    public PrecalcTutorialLogic(final Cache cache, final RawStudent theStudent,
                                final LocalDate today, final PrerequisiteLogic prereqLogic) throws SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (theStudent == null) {
            throw new IllegalArgumentException("Student record may not be null");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date may not be null");
        }

        this.student = theStudent;
        this.studentId = theStudent.stuId;
        this.status = new PrecalcTutorialStatus();

        // We need placement attempts and credit to determine tutorial eligibility
        this.allPlacementAttempts = RawStmpeLogic.queryLegalByStudent(cache, this.studentId);

        this.allCreditEarned = RawMpeCreditLogic.queryByStudent(cache, this.studentId);

        // Special student status can allow a non-incoming student to access Precalc Tutorials
        this.allSpecials = RawSpecialStusLogic.queryActiveByStudent(cache, this.studentId, today);

        computeStatus(cache, prereqLogic, today);
    }

    /**
     * Computes the status over all tutorials.
     *
     * <p>
     * The ELM Tutorial is available if the following conditions are satisfied:
     * <ul>
     * <li>The student has a Math Placement attempt on record
     * <li>The student does not have a passing ELM Exam on record
     * </ul>
     *
     * <p>
     * A Precalculus Tutorial is available if the following conditions are satisfied:
     * <ul>
     * <li>The student has a Math Placement attempt on record
     * <li>A window is active based on the student's application term for course "M 100P" (the "Math
     * Placement" course).
     * <li>The student is eligible for the corresponding course
     * <li>The student has not placed out of or earned credit in the corresponding course
     * </ul>
     *
     * @param cache       the data cache
     * @param prereqLogic prerequisite logic
     * @param today       the current day
     * @throws SQLException if there is an error accessing the database
     */
    private void computeStatus(final Cache cache, final PrerequisiteLogic prereqLogic,
                               final LocalDate today) throws SQLException {

        final boolean pct117 = RawSpecialStusLogic.isSpecialType(cache, this.studentId, today, "PCT117");
        final boolean pct118 = RawSpecialStusLogic.isSpecialType(cache, this.studentId, today, "PCT118");
        final boolean pct124 = RawSpecialStusLogic.isSpecialType(cache, this.studentId, today, "PCT124");
        final boolean pct125 = RawSpecialStusLogic.isSpecialType(cache, this.studentId, today, "PCT125");
        final boolean pct126 = RawSpecialStusLogic.isSpecialType(cache, this.studentId, today, "PCT126");

        this.status.holds.addAll(RawAdminHoldLogic.queryByStudent(cache, this.studentId));

        final boolean okFor117 = prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M117) || pct117;
        final boolean okFor118 = prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M118) || pct118;
        final boolean okFor124 = prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M124) || pct124;
        final boolean okFor125 = prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M125) || pct125;
        final boolean okFor126 = prereqLogic.hasSatisfiedPrereqsFor(RawRecordConstants.M126) || pct126;

        boolean doneWith117 = hasPlacedOut(RawRecordConstants.M117)
                || prereqLogic.hasCreditFor(RawRecordConstants.M117);

        boolean doneWith118 = hasPlacedOut(RawRecordConstants.M118)
                || prereqLogic.hasCreditFor(RawRecordConstants.M118);

        boolean doneWith124 = hasPlacedOut(RawRecordConstants.M124)
                || prereqLogic.hasCreditFor(RawRecordConstants.M124);

        boolean doneWith125 = hasPlacedOut(RawRecordConstants.M125)
                || prereqLogic.hasCreditFor(RawRecordConstants.M125);

        boolean doneWith126 = hasPlacedOut(RawRecordConstants.M126)
                || prereqLogic.hasCreditFor(RawRecordConstants.M126);

        final boolean needsPrecalc = !doneWith117 || !doneWith118 || !doneWith124 || !doneWith125 || !doneWith126;

        // Students not OK for 117 can still take Precalc Tutorial if they have taken MPE
        final boolean hasPlacement = !this.allPlacementAttempts.isEmpty();

        // Only available during SUMMER or FALL terms, for students applying in that FALL term.

        TermKey aplnTerm = this.student.aplnTerm;
        if (aplnTerm != null) {
            for (final RawSpecialStus spec : this.allSpecials) {
                final String type = spec.stuType;

                if ("PLCSP".equals(type)) {
                    // Force a "SPRING" application term
                    if (aplnTerm.name != ETermName.SPRING) {
                        aplnTerm = new TermKey(ETermName.SPRING, aplnTerm.year.intValue() + 1);
                    }
                } else if ("PLCSM".equals(type)) {
                    // Force a "SUMMER" application term
                    if (aplnTerm.name == ETermName.SPRING) {
                        aplnTerm = new TermKey(ETermName.SUMMER, aplnTerm.year.intValue());
                    } else if (aplnTerm.name == ETermName.FALL) {
                        aplnTerm = new TermKey(ETermName.SUMMER, aplnTerm.year.intValue() + 1);
                    }
                } else if ("PLCFA".equals(type) || "PCT117".equals(type)
                        || "PCT118".equals(type) || "PCT124".equals(type)
                        || "PCT125".equals(type) || "PCT126".equals(type)) {

                    // Force a "FALL" application term
                    if (aplnTerm.name != ETermName.FALL) {
                        aplnTerm = new TermKey(ETermName.FALL, aplnTerm.year.intValue());
                    }
                }
            }
        }

        // Allow ourselves to give someone who has transfer credit for a course the ability to access the Precalc
        // Tutorial by using the PCT### special student types - this can help a student with a transfer C who needs
        // the B (or placement) to get into Calculus.

        if (pct126 && doneWith126 && prereqLogic.hasSatisfiedPrereqsByTransferFor(RawRecordConstants.M126)) {
            doneWith126 = false;
        }
        if (pct125 && doneWith125 && prereqLogic.hasSatisfiedPrereqsByTransferFor(RawRecordConstants.M125)) {
            doneWith125 = false;
        }
        if (pct124 && doneWith124 && prereqLogic.hasSatisfiedPrereqsByTransferFor(RawRecordConstants.M124)) {
            doneWith124 = false;
        }
        if (pct118 && doneWith118 && prereqLogic.hasSatisfiedPrereqsByTransferFor(RawRecordConstants.M118)) {
            doneWith118 = false;
        }
        if (pct117 && doneWith117 && prereqLogic.hasSatisfiedPrereqsByTransferFor(RawRecordConstants.M117)) {
            doneWith117 = false;
        }

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        boolean isIncoming = false;
        if (active != null && aplnTerm != null && aplnTerm.name == ETermName.FALL
                && (active.term.name == ETermName.SUMMER || active.term.name == ETermName.FALL)) {
            isIncoming = active.term.year.equals(aplnTerm.year);
        }

        this.status.eligibleForPrecalcTutorial = isIncoming
                && ((hasPlacement && needsPrecalc) || pct117 || pct118 || pct124 || pct125 || pct126);

        String next = null;
        if (okFor124 && !doneWith124) {
            next = RawRecordConstants.M1240;
        } else if (okFor126 && !doneWith126) {
            next = RawRecordConstants.M1260;
        } else if (okFor125 && !doneWith125) {
            next = RawRecordConstants.M1250;
        } else if (okFor118 && !doneWith118) {
            next = RawRecordConstants.M1180;
        } else if (okFor117 && !doneWith117 || hasPlacement) {
            next = RawRecordConstants.M1170;
        }

        if (next != null) {
            this.status.nextPrecalcTutorial = next;

            final List<RawStexam> stexam =
                    RawStexamLogic.getExams(cache, this.studentId, next, Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(next);
            }
        }

        // The "PCT***" special categories make exams available if work completed in all cases
        if (pct117) {
            final List<RawStexam> stexam = RawStexamLogic.getExams(cache, this.studentId, RawRecordConstants.M1170,
                    Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(RawRecordConstants.M1170);
            }
        }
        if (pct118) {
            final List<RawStexam> stexam = RawStexamLogic.getExams(cache, this.studentId, RawRecordConstants.M1180,
                    Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(RawRecordConstants.M1170);
            }
        }
        if (pct124) {
            final List<RawStexam> stexam = RawStexamLogic.getExams(cache, this.studentId, RawRecordConstants.M1240,
                    Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(RawRecordConstants.M1240);
            }
        }
        if (pct125) {
            final List<RawStexam> stexam = RawStexamLogic.getExams(cache, this.studentId, RawRecordConstants.M1250,
                    Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(RawRecordConstants.M1250);
            }
        }
        if (pct126) {
            final List<RawStexam> stexam = RawStexamLogic.getExams(cache, this.studentId, RawRecordConstants.M1260,
                    Integer.valueOf(4), true, "R");
            if (!stexam.isEmpty()) {
                this.status.eligiblePrecalcExamCourses.add(RawRecordConstants.M1260);
            }
        }

        if (doneWith117) {
            this.status.completedPrecalcTutorials.add(RawRecordConstants.M1170);
        }
        if (doneWith118) {
            this.status.completedPrecalcTutorials.add(RawRecordConstants.M1180);
        }
        if (doneWith124) {
            this.status.completedPrecalcTutorials.add(RawRecordConstants.M1240);
        }
        if (doneWith125) {
            this.status.completedPrecalcTutorials.add(RawRecordConstants.M1250);
        }
        if (doneWith126) {
            this.status.completedPrecalcTutorials.add(RawRecordConstants.M1260);
        }

        // Check dates when Precalc will be available

        if (this.status.eligibleForPrecalcTutorial) {
            // Get date range from campus calendar
            final List<RawCampusCalendar> calendars = RawCampusCalendarLogic.INSTANCE.queryAll(cache);

            LocalDate startDate = null;
            LocalDate endDate = null;
            for (final RawCampusCalendar test : calendars) {
                if (RawCampusCalendar.DT_DESC_TUT_START.equals(test.dtDesc)) {
                    startDate = test.campusDt;
                }
                if (RawCampusCalendar.DT_DESC_TUT_END.equals(test.dtDesc)) {
                    endDate = test.campusDt;
                }
            }

            final List<DateRange> web = new ArrayList<>(1);
            if (startDate != null && endDate != null) {
                web.add(new DateRange(startDate, endDate));
            }
            this.status.webSiteAvailability = new DateRangeGroups(web, today);
        }
    }

    /**
     * Tests whether the student has placed out of a course.
     *
     * @param course the course ID
     * @return true if the student has placed out
     */
    private boolean hasPlacedOut(final String course) {

        boolean placed = false;

        if (course != null) {
            for (final RawMpeCredit test : this.allCreditEarned) {
                if (course.equals(test.course)) {
                    placed = true;
                    break;
                }
            }
        }

        return placed;
    }

//    /**
//     * Main method to exercise the logic object.
//     *
//     * @param args command-line arguments
//     */
//     public static void main(final String... args) {
//
//     final ContextMap map = ContextMap.getDefaultInstance();
//     DbConnection.registerDrivers();
//
//     final DbProfile dbProfile =
//     map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH).getDbProfile();
//
//     final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
//
//     try {
//     final DbConnection conn = ctx.checkOutConnection();
//     final Cache cache = new Cache(dbProfile, conn);
//
//     try {
//     final LocalDate today = LocalDate.now();
//
//     final PrerequisiteLogic prereq =
//     new PrerequisiteLogic(cache, dbProfile, "888888888");
//     final PrecalcTutorialLogic logic = new PrecalcTutorialLogic(cache, dbProfile, //
//     "888888888", today, prereq);
//
//     mainPrintStatus(logic.getStatus());
//     } finally {
//     ctx.checkInConnection(conn);
//     }
//     } catch (SQLException ex) {
//     Log.warning(ex);
//     }
//     }

//    /**
//     * Prints the contents of a {@code PrecalcTutorialStatus}.
//     *
//     * @param status the {@code PrecalcTutorialStatus} whose contents to print
//     */
//     private static void mainPrintStatus(final PrecalcTutorialStatus status) {
//
//     Log.fine("Student ", status.student.stuId);
//     Log.fine(" Application term: ", status.student.aplnTerm);
//
//     Log.fine(" Eligible for Precalc Tutorial : "
//     + status.eligibleForPrecalcTutorial);
//
//     final DateRangeGroups site = status.webSiteAvailability;
//     for (final DateRange r : site.past) {
//     Log.fine(" Precalc site was available : ", r);
//     }
//     if (site.current != null) {
//     Log.fine(" Precalc site is available : ", site.current);
//     }
//     for (final DateRange r : site.future) {
//     Log.fine(" Precalc site will be available : ", r);
//     }
//
//     for (final String completed : status.completedPrecalcTutorials) {
//     Log.fine(" Completed Precalc : ", completed);
//     }
//
//     Log.fine(" Next Precalc : ", status.getNextPrecalcTutorial());
//     }
}
