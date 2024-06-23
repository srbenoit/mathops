package dev.mathops.db.old.logic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawRemoteMpeLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawRemoteMpe;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A container for logic relating to a student's placement status and eligibility to take placement exams. This object
 * does not provide eligibility for challenge exams or to access tutorials or take tutorial exams.
 *
 * <p>
 * The Math Placement Tool will come in several versions, each using a different proctoring method. This allows the
 * version number to help identify how the tool was taken. Version numbers are:
 *
 * <ul>
 * <li>MPTUN - Unproctored
 * <li>MPTTC - Proctored in the Department testing center (or proctored online by department staff)
 * <li>MPTUT - Proctored by the University testing center (or Student Disability Center)
 * <li>MPTPU - Proctored through ProctorU
 * <li>MPTRM - Proctored through Respondus Monitor (future)
 * <li>MPTHL - Proctored through HonorLock (future)
 * <li>MPTRW - Proctored through RamWork
 * </ul>
 *
 * <p>
 * The general design is that a student gets two attempts on the math placement tool. For incoming students (or DCE
 * students), the first of these can be unproctored.
 *
 * <p>
 * This is a change to an older policy that allowed two proctored attempts, plus an additional unproctored attempt
 * for incoming students. To transition between the two policies, we use the following logic:
 *
 * <ul>
 * <li>Let the total attempts used (N) be the sum of all attempts with version PPPPP or any of the versions above
 * (POOOO attempts are ignored).
 * <li>If the total attempts used is 2 or more, the student is no longer eligible to complete the placement tool.
 * Otherwise, they have 2-N attempts remaining.
 * <li>If a student is eligible for an unproctored attempt based on student status (DCE student or inbound first-year
 * student based on application term), and has not used either an unproctored attempt or two proctored attempts, they
 * are eligible to complete the tool unproctored as one of these attempts.
 * <li>
 * </ul>
 * <p>
 * Logic permits the option (as a static boolean) of whether to allow DCE/DCEN students (as indicated in special
 * student records) to use the unproctored exam. Detailed logic is as follows:
 *
 * <ul>
 * <li>Count attempts on record, classify as proctored or unproctored, determine if any attempts remain or not.
 * <li>If attempts remain, do the following to determine when each form of testing is available:
 * <ul>
 * <li>Query all remote MPE records with course "M 100P" based on the student's application term (use an empty list
 * if the student has no application term) - these will indicate when unproctored placement exams are available.
 * <li>Query all special student records for the student.
 * <li>Create a list of date ranges for unproctored exams
 * <li>For each remote MPE record, crate a date range and add to the list.
 * <li>If any date ranges in these lists are current or future, set the flag that indicates the student is allowed to
 * use the unproctored exam (this does not mean they are eligible now, or have not used their unproctored attempt)
 * <li>If the student is a member of the "DCE" or "DCEN" special category, and the boolean flag indicates DCE
 * students are allowed unproctored attempts, add a synthetic remote MPE record to the list with date ranges from the
 * special student record
 * <li>Merge any overlapping date ranges that result
 * <li>If the student is a member of the "ORIENTN" special category, remove date ranges for those special student
 * records from the list.
 * <li>Categorize date ranges in the list as past, current, and future based on a given "today" date.
 * <li>Based on current available types of testing and the supported types of remote proctoring, generate a list of
 * exam IDs available in each of the three formats.
 * </ul>
 * </ul>
 */
public class PlacementLogic {

    /** An exam ID. */
    private static final String LEGACY_PROCTORED_MPE_ID = "PPPPP";

    /** An exam ID. */
    private static final String LEGACY_UNPROCTORED_MPE_ID = "POOOO";

    /** An exam ID. */
    public static final String PROCTORED_MPT_DEPT_TC_ID = "MPTTC";

    /** An exam ID. */
    public static final String PROCTORED_MPT_PROCTORU_ID = "MPTPU";

    /** An exam ID. */
    private static final String PROCTORED_MPT_RAMWORK = "MPTRW";

    /** An exam ID. */
    private static final String UNPROCTORED_MPT_ID = "MPTUN";

    /** The list of locally proctored IDs currently supported (one per online proctoring service). */
    private static final List<String> LOCALLY_PROCTORED_IDS = List.of(PROCTORED_MPT_DEPT_TC_ID);

    /** The list of online proctored IDs currently supported (one per online proctoring service). */
    private static final List<String> ONLINE_PROCTORED_IDS = List.of(PROCTORED_MPT_PROCTORU_ID);

    /** The list of unproctored IDs currently supported. */
    private static final List<String> UNPROCTORED_IDS = List.of(UNPROCTORED_MPT_ID);

    /**
     * The maximum number of unproctored attempts on math placement allowed (available only if student is an incoming
     * first-year student).
     */
    private static final int MAX_UNPROCTORED_PLACEMENT_ATTEMPTS = 1;

    /** The maximum number of total attempts on math placement allowed. */
    private static final int MAX_TOTAL_PLACEMENT_ATTEMPTS = 2;

    /**
     * Flag indicating DCE/DCEN special student types are allowed unproctored attempts as if they were incoming
     * first-year students.
     */
    private static final boolean IS_DCE_ALLOWED_UNPROCTORED = true;

    //

    /** The student ID. */
    private final String studentId;

    /** The student's application term, null if not known. */
    private final TermKey applicationTerm;

    /** The student record. */
    private final RawStudent student;

    /** The list of all student attempts on record. */
    private final List<RawStmpe> allAttempts;

    /** The list of placement or credit earned. */
    private final List<RawMpeCredit> allPlacementCredit;

    /** All special categories to which the student currently belongs. */
    private final SpecialCategoriesStatus allSpecials;

    /** The student's status with respect to the math placement tool. */
    public final PlacementStatus status;

    /**
     * Constructs a new {@code PlacementLogic}.
     *
     * @param cache              the data cache
     * @param theStudentId       the student ID
     * @param theApplicationTerm the student's application term, null if not known
     * @param now                the date/time to consider "now" @ throws SQLException if there is an error accessing
     *                           the database
     * @throws SQLException if there is an error accessing the database
     */
    public PlacementLogic(final Cache cache, final String theStudentId, final TermKey theApplicationTerm,
                          final ZonedDateTime now) throws SQLException {

        if (cache == null) {
            throw new IllegalArgumentException("Cache may not be null");
        }
        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (now == null) {
            throw new IllegalArgumentException("Current date/time may not be null");
        }

        this.studentId = theStudentId;
        this.applicationTerm = theApplicationTerm;
        this.status = new PlacementStatus();
        this.student = RawStudentLogic.query(cache, this.studentId, false);
        this.allAttempts = RawStmpeLogic.queryLegalByStudent(cache, this.studentId);
        this.allPlacementCredit = RawMpeCreditLogic.queryByStudent(cache, this.studentId);
        this.allSpecials = SpecialCategoriesStatus.of(cache, this.studentId);

        final LocalDate today = now.toLocalDate();
        computeStatus(cache, today);
    }

    /**
     * Computes placement status.
     *
     * @param cache the data cache
     * @param today the current date
     * @throws SQLException if there is an error accessing the database
     */
    private void computeStatus(final Cache cache, final LocalDate today) throws SQLException {

        if (this.student == null || this.allAttempts == null || this.allPlacementCredit == null
                || this.allSpecials == null) {

            // Can't query required data - make nothing available
            this.status.allowedToUseUnproctored = false;
            this.status.attemptsRemaining = 0;
            this.status.attemptsUsed = 0;
            final String admMsg = Res.get(Res.PLC_CANT_LOOKUP_RECORD_L);
            final String stuMsg = Res.get(Res.PLC_CANT_LOOKUP_RECORD_S);
            this.status.setWhyProctoredUnavailable(admMsg, stuMsg);
        } else {
            this.status.placementAttempted = !this.allAttempts.isEmpty();

            // Count used attempts
            int numUnproctored = 0;
            int numAttempts = 0;
            int numCountedAttempts = 0;

            int maxTries = MAX_TOTAL_PLACEMENT_ATTEMPTS;
            for (final RawSpecialStus test : this.allSpecials.getActive(today)) {
                if ("MPT3".equals(test.stuType)) {
                    ++maxTries;
                }
            }

            // The following should include all historical exam IDs as well as current IDs
            for (final RawStmpe attempt : this.allAttempts) {
                if (LEGACY_UNPROCTORED_MPE_ID.equals(attempt.version)) {
                    // NOTE: this is not "counted"
                    ++numAttempts;
                    ++numUnproctored;
                } else if (UNPROCTORED_MPT_ID.equals(attempt.version)) {
                    ++numAttempts;
                    ++numUnproctored;
                    ++numCountedAttempts;
                } else if (LEGACY_PROCTORED_MPE_ID.equals(attempt.version)
                        || PROCTORED_MPT_DEPT_TC_ID.equals(attempt.version)
                        || PROCTORED_MPT_PROCTORU_ID.equals(attempt.version)
                        || PROCTORED_MPT_RAMWORK.equals(attempt.version)) {
                    ++numAttempts;
                    ++numCountedAttempts;
                }
            }

            this.status.unproctoredUsed = numUnproctored >= MAX_UNPROCTORED_PLACEMENT_ATTEMPTS;
            this.status.attemptsUsed = numAttempts;

            if (numCountedAttempts >= maxTries) {
                // All attempts used - make nothing available
                this.status.allowedToUseUnproctored = false;
                this.status.attemptsRemaining = 0;
                this.status.setWhyProctoredUnavailable(Res.get(Res.PLC_NO_ATTEMPTS_REMAIN_L),
                        Res.get(Res.PLC_NO_ATTEMPTS_REMAIN_S));
            } else {
                this.status.attemptsRemaining = maxTries - numCountedAttempts;
                computeStatusAttemptsRemain(cache, today, numUnproctored, numCountedAttempts,
                        maxTries);
            }

            buildClearedList();
        }
    }

    /**
     * Computes the status of the unproctored placement tool format once the database data has been validated as
     * present.
     *
     * @param cache          the data cache
     * @param today          the current date
     * @param numUnproctored the number of unproctored attempts used
     * @param numTotal       the total number of attempts used
     * @param maxTries       the maximum number of total attempts allowed for this student
     * @throws SQLException if there is an error accessing the database
     */
    private void computeStatusAttemptsRemain(final Cache cache, final LocalDate today, final int numUnproctored,
                                             final int numTotal, final int maxTries) throws SQLException {

        // Create list of date ranges for unproctored exams

        final List<DateRange> unproctoredRanges = new ArrayList<>(10);

        // For each remote MPE record, create a date range and add to list.

        if (this.applicationTerm != null) {
            boolean hasFutureRemote = false;
            for (final RawRemoteMpe rec : RawRemoteMpeLogic.queryByCourse(cache, RawRecordConstants.M100P)) {
                if (this.applicationTerm.equals(rec.aplnTerm)) {
                    final DateRange range = new DateRange(rec.startDt, rec.endDt);
                    unproctoredRanges.add(range);
                    if (rec.endDt == null || !rec.endDt.isBefore(today)) {
                        hasFutureRemote = true;
                    }
                }
            }

            // If any date ranges in these lists are current or future, set the flag that indicates
            // the student is allowed to use the unproctored exam (this does not mean they are
            // eligible now, or have not used their unproctored attempt)

            if (hasFutureRemote) {
                this.status.allowedToUseUnproctored = true;
                this.status.whyUnproctoredAllowed = Res.get(Res.PLC_WHY_FIRST_YEAR);
            }
        }

        // If the student is a member of the "DCE" or "DCEN" special category, and the boolean flag
        // indicates DCE students are allowed unproctored attempts, add a synthetic remote MPE
        // record to the list with date ranges from the special student record

        final List<RawSpecialStus> active = this.allSpecials.getActive(today);

        if (IS_DCE_ALLOWED_UNPROCTORED) {
            for (final RawSpecialStus spec : active) {
                if ("DCE".equals(spec.stuType) || "DCEN".equals(spec.stuType)) {

                    final DateRange range = new DateRange(spec.startDt, spec.endDt);

                    unproctoredRanges.add(range);
                    if (!this.status.allowedToUseUnproctored) {
                        this.status.allowedToUseUnproctored = true;
                        this.status.whyUnproctoredAllowed = Res.get(Res.PLC_WHY_CSU_ONLINE);
                    }
                }
            }
        }

        // Merge any overlapping date ranges that result
        DateRange.mergeRanges(unproctoredRanges);

        // If the student is a member of the "ORIENTN" special category, remove date
        // ranges for those special student records from the list.

        // boolean isOrientationToday = false;
        // for (final StudentCategory spec : active) {
        // if (spec.getStartDate() != null && spec.getEndDate() != null
        // && ("ORIENTN".equals(spec.getSpecialCategory()))) {
        //
        // if (!(spec.getStartDate().isAfter(today) || spec.getEndDate().isBefore(today))) {
        // isOrientationToday = true;
        // }
        //
        // DateRange.removeRange(spec.getStartDate(), spec.getEndDate(), unproctoredRanges);
        // }
        // }

        // END FIXME:

        final DateRangeGroups unproc = new DateRangeGroups(unproctoredRanges, today);
        this.status.unproctoredDateRanges = unproc;

        // Based on current available types of testing and the supported types of remote proctoring,
        // generate a list of exam IDs available in each of the three formats.

        if (numTotal < maxTries) {
            // boolean isDceStudent = false;
            // for (final RawSpecialStus spec : active) {
            // if ("DCE".equals(spec.stuType)
            // || "DCEN".equals(spec.stuType)) {
            // isDceStudent = true;
            // break;
            // }
            // }

            // Open ProctorU during winter break.
            // final boolean winterBreak = today.isAfter(LocalDate.of(2021, 12, 15))
            // && today.isBefore(LocalDate.of(2023, 1, 18));

            this.status.availableLocalProctoredIds.addAll(LOCALLY_PROCTORED_IDS);
            // if (isDceStudent || winterBreak || this.status.allowedToUseUnproctored) {
            this.status.availableOnlineProctoredIds.addAll(ONLINE_PROCTORED_IDS);
            // }

            if (this.status.allowedToUseUnproctored) {
                if (numUnproctored >= MAX_UNPROCTORED_PLACEMENT_ATTEMPTS) {
                    this.status.whyUnproctoredUnavailable = Res.get(Res.PLC_WHY_ALL_ATTEMPTS_USED);
                } else if (unproc.current == null) {
                    if (unproc.future.isEmpty()) {
                        final DateRange mostRecent = unproc.getMostRecentPast();

                        if (mostRecent == null) {
                            // No dates remain - should never occur, but handle just in case
                            final String msg = Res.fmt(Res.PLC_ERR_NO_DATE_RANGES, this.studentId);
                            Log.warning(msg);
                            this.status.allowedToUseUnproctored = false;
                        } else {
                            final String endStr = TemporalUtils.FMT_MDY.format(mostRecent.end);
                            this.status.whyUnproctoredUnavailable = Res.fmt(Res.PLC_WHY_PAST_LAST_DATE, endStr);
                        }
                    } else {
                        final DateRange futureRange = unproc.future.getFirst();
                        final String startStr = TemporalUtils.FMT_MDY.format(futureRange.start);
                        this.status.whyUnproctoredUnavailable = Res.fmt(Res.PLC_WHY_PAST_LAST_DATE, startStr);
                    }
                } else {
                    this.status.availableUnproctoredIds.addAll(UNPROCTORED_IDS);
                }
            }
        }
    }

    /**
     * Populates the list of courses that the student is cleared to take based on placement results.
     */
    private void buildClearedList() {

        final Set<String> cleared = this.status.clearedFor;
        final Set<String> placedOut = this.status.placedOutOf;
        final Set<String> earnedCredit = this.status.earnedCreditFor;

        cleared.add("MATH 101");
        cleared.add("MATH 105");
        cleared.add("STAT 100");

        for (final RawMpeCredit cred : this.allPlacementCredit) {
            final String course = cred.course;

            if (RawRecordConstants.M100C.equals(course)) {
                cleared.add(RawRecordConstants.MATH117);
            } else if ("M 100A".equals(course)) {
                cleared.add(RawRecordConstants.MATH117);
            } else if (RawRecordConstants.M117.equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH117);
                } else {
                    placedOut.add(RawRecordConstants.MATH117);
                }
                cleared.add(RawRecordConstants.MATH118);
            } else if (RawRecordConstants.M118.equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH118);
                } else {
                    placedOut.add(RawRecordConstants.MATH118);
                }
                cleared.add(RawRecordConstants.MATH124);
                cleared.add(RawRecordConstants.MATH125);
                cleared.add("MATH 141");
            } else if ("M 120".equals(course) || "M 120A".equals(course) || "M 121".equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH117);
                    earnedCredit.add(RawRecordConstants.MATH118);
                } else {
                    placedOut.add(RawRecordConstants.MATH117);
                    placedOut.add(RawRecordConstants.MATH118);
                }
                cleared.add(RawRecordConstants.MATH124);
                cleared.add(RawRecordConstants.MATH125);
            } else if (RawRecordConstants.M124.equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH124);
                } else {
                    placedOut.add(RawRecordConstants.MATH124);
                }
            } else if (RawRecordConstants.M125.equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH125);
                } else {
                    placedOut.add(RawRecordConstants.MATH125);
                }
                cleared.add(RawRecordConstants.MATH126);
            } else if (RawRecordConstants.M126.equals(course)) {
                if ("C".equals(cred.examPlaced)) {
                    earnedCredit.add(RawRecordConstants.MATH126);
                } else {
                    placedOut.add(RawRecordConstants.MATH126);
                }
            }

            if (placedOut.contains(RawRecordConstants.MATH124) && placedOut.contains(RawRecordConstants.MATH125)) {
                cleared.add("MATH 155");
            }
            if (placedOut.contains(RawRecordConstants.MATH124) && placedOut.contains(RawRecordConstants.MATH126)) {
                cleared.add("MATH 156");
                cleared.add("MATH 160");
            }
        }

        // Don't list courses for which the student has placed out or earned credit as "available" to add
        cleared.removeAll(placedOut);
        cleared.removeAll(earnedCredit);
    }

    /**
     * Main method to exercise the logic object.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final WebSiteProfile webProfile =map.getWebSiteProfile(Contexts.PLACEMENT_HOST, Contexts.ROOT_PATH);
        if (webProfile == null) {
            Log.warning("Web profile not found");
        } else {
            final DbProfile dbProfile = webProfile.dbProfile;
            final DbContext ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

            final ZonedDateTime now = ZonedDateTime.now();

            try {
                final DbConnection conn = ctx.checkOutConnection();

                try {
                    final Cache cache = new Cache(dbProfile, conn);

                    final PlacementLogic logic = new PlacementLogic(cache, "888888888",
                            new TermKey(ETermName.SPRING, 2025), now);

                    mainPrintStatus(logic.status);
                } finally {
                    ctx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Prints the contents of a {@code PlacementStatus}.
     *
     * @param status the {@code PlacementStatus} whose contents to print
     */
    private static void mainPrintStatus(final PlacementStatus status) {

        Log.fine(" Proctored IDs available : " + status.availableLocalProctoredIds.size());
        Log.fine(" Unproctored IDs available : " + status.availableUnproctoredIds.size());
        Log.fine(" Allowed to take Unproctored : " + status.allowedToUseUnproctored);

        if (status.whyUnproctoredAllowed != null) {
            Log.fine(" Reason Unproctored allowed : ", status.whyUnproctoredAllowed);
        }

        if (status.whyUnproctoredUnavailable != null) {
            Log.fine(" Why unproctored unavailable : ", status.whyUnproctoredUnavailable);
        }

        Log.fine(" Attempts : " + status.attemptsUsed + " used, " + status.attemptsRemaining + " remaining");

        if (status.whyProctoredUnavailable != null) {
            Log.fine(" Why unavailable : ", status.whyProctoredUnavailable);
        }
        if (status.shortWhyProctoredUnavailable != null) {
            Log.fine(" Short why unavailable : ", status.shortWhyProctoredUnavailable);
        }

        final DateRangeGroups unproc = status.unproctoredDateRanges;

        if (unproc != null) {
            for (final DateRange r : unproc.past) {
                Log.fine(" Unproctored was available : ", r);
            }
            if (unproc.current != null) {
                Log.fine(" Unproctored is available : ", unproc.current);
            }
            for (final DateRange r : unproc.future) {
                Log.fine(" Unproctored will be available : ", r);
            }
        }

        for (final String placed : status.placedOutOf) {
            Log.fine(" Placed out of : ", placed);
        }
        for (final String credit : status.earnedCreditFor) {
            Log.fine(" Earned credit for : ", credit);
        }
        for (final String cleared : status.clearedFor) {
            Log.fine(" Cleared to register for : ", cleared);
        }
    }
}
