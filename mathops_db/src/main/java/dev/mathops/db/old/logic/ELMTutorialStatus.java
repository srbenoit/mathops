package dev.mathops.db.old.logic;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A data class containing a student's status with respect to the various Tutorials.
 */
public final class ELMTutorialStatus {

    /** The student record. */
    public final RawStudent student;

    /** All holds the student currently has. */
    public final HoldsStatus holds;

    /**
     * Flag indicating student has completed the math placement tool and is eligible for the ELM Tutorial.
     */
    public boolean eligibleForElmTutorial;

    /**
     * Flag indicating student has passed the unit 4 review exam and is eligible for the ELM Exam.
     */
    public boolean eligibleForElmExam;

    /**
     * Flag indicating the student has passed the proctored ELM Exam, which makes them eligible for MATH 117.
     */
    public boolean elmExamPassed;

    /** Number of failed ELM Exam attempts since the last passing Unit 4 Review Exam. */
    public int failedElmExamsSinceLastPassingReview;

    /** Date ranges when ELM Tutorial site will be available (RemotePlacement - M 100T). */
    public DateRangeGroups webSiteAvailability;

    /**
     * Date ranges when ELM Tutorial Exams available using online proctoring (all dates except placement maintenance
     * windows).
     */
    public DateRangeGroups onlineProctoredExamAvailability;

    /**
     * Flag indicating the student has passed the proctored Unit 3 exam for the ELM Tutorial, which satisfies the
     * transfer requirement in Math.
     */
    public boolean elm3Passed;

    /**
     * Constructs a new {@code ELMTutorialStatus}.
     *
     * @param theStudent the student record
     * @param theHolds   the holds status
     */
    private ELMTutorialStatus(final RawStudent theStudent, final HoldsStatus theHolds) {

        this.student = theStudent;
        this.holds = theHolds;
    }

    /**
     * Gets the ELM Tutorial status for a student.
     *
     * @param cache        the data cache
     * @param theStudentId the student ID
     * @param now          the date/time to consider "now"
     * @param theHolds     the holds status
     * @return the generated status object
     * @throws SQLException if there is an error accessing the database
     */
    public static ELMTutorialStatus of(final Cache cache, final String theStudentId, final ZonedDateTime now,
                                       final HoldsStatus theHolds) throws SQLException {

        return of(cache, RawStudentLogic.query(cache, theStudentId, false), now, theHolds);
    }

    /**
     * Gets the ELM Tutorial status for a student.
     *
     * @param cache      the data cache
     * @param theStudent the student record
     * @param now        the date/time to consider "now"
     * @param theHolds   the holds status
     * @return the generated status object
     * @throws SQLException if there is an error accessing the database
     */
    public static ELMTutorialStatus of(final Cache cache, final RawStudent theStudent, final ZonedDateTime now,
                                       final HoldsStatus theHolds) throws SQLException {

        final ELMTutorialStatus status = new ELMTutorialStatus(theStudent, theHolds);

        if (theStudent != null && now != null) {
            final String studentId = theStudent.stuId;

            final List<RawStmpe> allPlacementAttempts = RawStmpeLogic.queryLegalByStudent(cache, studentId);

            final List<RawStexam> passedElmUnit4RE = RawStexamLogic.getExams(cache, studentId,
                    RawRecordConstants.M100T, Integer.valueOf(4), true, "R");

            final List<RawStexam> allElmExams = RawStexamLogic.getExams(cache, studentId, RawRecordConstants.M100T,
                    false, "U");

            if (allPlacementAttempts.isEmpty()) {
                // Check for "special_stus" record with
                if (RawSpecialStusLogic.isSpecialType(cache, studentId, now.toLocalDate(), RawSpecialStus.ELM)) {
                    status.eligibleForElmTutorial = true;
                }
            } else {
                status.eligibleForElmTutorial = true;
            }

            // Find the most recent passing unit 4 review
            LocalDateTime mostRecent = null;
            for (final RawStexam exam : passedElmUnit4RE) {
                final LocalDateTime fin = exam.getFinishDateTime();
                if (mostRecent == null || fin != null && mostRecent.isBefore(fin)) {
                    mostRecent = fin;
                }
            }

            int failedSinceLastRE = 0;
            for (final RawStexam test : allElmExams) {
                if (Integer.valueOf(3).equals(test.unit)) {
                    status.elm3Passed = true;
                } else if (Integer.valueOf(4).equals(test.unit)) {
                    if ("Y".equals(test.passed)) {
                        status.elmExamPassed = true;
                    } else if (mostRecent != null && "N".equals(test.passed)
                               && test.getFinishDateTime() != null && test.getFinishDateTime().isAfter(mostRecent)) {
                        ++failedSinceLastRE;
                    }
                }
            }

            status.failedElmExamsSinceLastPassingReview = failedSinceLastRE;
            status.eligibleForElmExam = !passedElmUnit4RE.isEmpty() && failedSinceLastRE < 2;

            // Determine dates when the tutorial site and online proctored ELM Exam will be available. This is
            // available except during the scheduled placement maintenance periods.
            final List<DateRange> ranges = new ArrayList<>(5);
            final List<RawCampusCalendar> calendars = cache.getSystemData().getCampusCalendars();

            final LocalDate today = now.toLocalDate();
            LocalDate s1 = null;
            LocalDate s2 = null;
            LocalDate s3 = null;
            LocalDate e1 = null;
            LocalDate e2 = null;
            LocalDate e3 = null;

            for (final RawCampusCalendar cal : calendars) {
                final String type = cal.dtDesc;

                if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START1.equals(type)) {
                    s1 = cal.campusDt;
                } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END1.equals(type)) {
                    e1 = cal.campusDt;
                } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START2.equals(type)) {
                    s2 = cal.campusDt;
                } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END2.equals(type)) {
                    e2 = cal.campusDt;
                } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_START3.equals(type)) {
                    s3 = cal.campusDt;
                } else if (RawCampusCalendar.DT_DESC_PLACEMENT_MAINT_END3.equals(type)) {
                    e3 = cal.campusDt;
                }
            }

            LocalDate start = today;
            if (s1 != null && e1 != null) {
                if (s1.isAfter(start)) {
                    ranges.add(new DateRange(today, s1.minusDays(1L)));
                }
                start = e1.plusDays(1L);
            }
            if (s2 != null && e2 != null) {
                if (s2.isAfter(start)) {
                    ranges.add(new DateRange(start, s2.minusDays(1L)));
                }
                start = e2.plusDays(1L);
            }
            if (s3 != null && e3 != null) {
                if (s3.isAfter(start)) {
                    ranges.add(new DateRange(start, s3.minusDays(1L)));
                }
            }

            status.webSiteAvailability = new DateRangeGroups(ranges, today);
            status.onlineProctoredExamAvailability = new DateRangeGroups(ranges, today);
        }

        return status;
    }

//    /**
//     * Main method to exercise the logic object.
//     *
//     * @param args command-line arguments
//     */
//    public static void main(final String... args) {
//
//        final ContextMap map = ContextMap.getDefaultInstance();
//        DbConnection.registerDrivers();
//
//        final WebSiteProfile webProfile = map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH);
//        if (webProfile == null) {
//            Log.warning("Web profile not found");
//        } else {
//            final ZonedDateTime now = ZonedDateTime.now();
//
//            final DbProfile dbProfile = webProfile.dbProfile;
//
//            final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
//
//            try {
//                final DbConnection conn = ctx.checkOutConnection();
//                final Cache cache = new Cache(dbProfile, conn);
//
//                try {
//                    final HoldsStatus holds = HoldsStatus.of(cache, "888888888");
//                    final ELMTutorialStatus status = of(cache, "888888888", now, holds);
//
//                    mainPrintResult(status);
//                } finally {
//                    ctx.checkInConnection(conn);
//                }
//            } catch (final SQLException ex) {
//                Log.warning(ex);
//            }
//        }
//    }
//
//    /**
//     * Prints results of one test.
//     *
//     * @param status the logic object
//     */
//    private static void mainPrintResult(final ELMTutorialStatus status) {
//
//        Log.info("Student               : ", status.student.stuId);
//        Log.info("Holds                 : ", status.holds.holds);
//        Log.info("Eligible for Tutorial : ", Boolean.toString(status.eligibleForElmTutorial));
//        Log.info("Eligible for ELM Exam : ", Boolean.toString(status.eligibleForElmExam));
//        Log.info("Passed Unit 3 Exam    : ", Boolean.toString(status.elm3Passed));
//        Log.info("Passed ELM Exam       : ", Boolean.toString(status.elmExamPassed));
//
//        final List<DateRange> site = status.webSiteAvailability.getAll();
//        for (final DateRange r : site) {
//            Log.info("Site availble         : ", r);
//        }
//        final List<DateRange> exam = status.onlineProctoredExamAvailability.getAll();
//        for (final DateRange r : exam) {
//            Log.info("Online Exams availble : ", r);
//        }
//    }
}
