package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawDisciplineLogic;
import dev.mathops.db.old.rawlogic.RawResourceLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawHoldType;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Scans registration data and applies relevant holds.
 */
public enum SetHolds {
    ;

    /** Hold ID indicating a rental calculator is overdue. */
    private static final String LATE_RENTAL_CALC_HOLD = "36";

    /** Flag to set batch into debug mode. */
    private static final boolean DEBUG = false;

    /** A commonly used string. */
    private static final String FOR_STUDENT = " for student ";

    /** A commonly used value. */
    private static final Integer ZERO = Integer.valueOf(0);

    /**
     * Runs the report, writing the result to a file.
     */
    public static void execute() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        try {
            setHolds(cache);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Sets holds.
     *
     * @param cache the data cache
     * @throws SQLException if there is an error accessing the database
     */
    static void setHolds(final Cache cache) throws SQLException {

        final LocalDate sysDate = LocalDate.now();
        final LocalDate endDate;

        final TermRec active = cache.getSystemData().getActiveTerm();

        final List<RawStresource> resources = RawStresourceLogic.queryAll(cache);

        // Add warning hold for students who have an unreturned rental calculator (once per term, near the end)
        endDate = active.endDate.minusDays(8L);
        if (sysDate.equals(endDate)) {
            applyRentalCalcHolds(cache, resources);
        }

        // Add hold for students who have an unreturned/overdue item
        addOverdueItemHold(cache, resources);

        // Look for students with combinations of course registrations that are invalid, and apply holds
        addRegistrationHolds(cache, active);

        // Reconcile the sev_admin_hold field in Student vs. actual holds that exist for that student
        reconcileStudentTableWithHolds(cache);
    }

    /**
     * Applies "overdue rental calculator" holds.
     *
     * @param cache     the cache
     * @param resources the list of student resource records
     * @throws SQLException if there is an error accessing the database
     */
    private static void applyRentalCalcHolds(final Cache cache, final Collection<RawStresource> resources)
            throws SQLException {

        int count = 0;

        // Look up the severity of hold
        final RawHoldType type = cache.getSystemData().getHoldType(LATE_RENTAL_CALC_HOLD);
        final String sev = type == null ? "N" : type.sevAdminHold;

        if (!resources.isEmpty()) {
            final Collection<String> students = new HashSet<>(10);

            for (final RawStresource test : resources) {
                // Rental calculators all have resource IDs starting with "77"
                if (test.resourceId.startsWith("77") && test.returnDt == null) {
                    students.add(test.stuId);
                }
            }

            // TODO: Set the "times" field in the new hold from the stresource record

            final LocalDate sysDate = LocalDate.now();
            for (final String student : students) {
                if (RawAdminHoldLogic.query(cache, student, LATE_RENTAL_CALC_HOLD) == null) {
                    final RawAdminHold hold = new RawAdminHold(student, LATE_RENTAL_CALC_HOLD, sev,
                            ZERO, sysDate);

                    Log.info("  Adding hold " + LATE_RENTAL_CALC_HOLD + FOR_STUDENT + student);

                    if (!DEBUG) {
                        RawAdminHoldLogic.insert(cache, hold);
                    }
                } else {
                    Log.info("  Retaining existing hold " + LATE_RENTAL_CALC_HOLD + FOR_STUDENT + student);
                }

                ++count;
            }
        }

        Log.info("  Number of rental calc holds that have been applied/retained: " + count);
    }

    /**
     * Applies "overdue loan" holds.
     *
     * @param cache     the cache
     * @param resources the list of student resource records
     * @throws SQLException if there is an error accessing the database
     */
    private static void addOverdueItemHold(final Cache cache, final Iterable<RawStresource> resources)
            throws SQLException {

        final LocalDate sysDate = LocalDate.now();

        int count = 0;
        final Map<String, List<RawStresource>> lent = new HashMap<>(10);

        for (final RawStresource test : resources) {
            if (test.dueDt != null && test.dueDt.isBefore(sysDate) && test.returnDt == null) {
                final List<RawStresource> list = lent.computeIfAbsent(test.stuId, s -> new ArrayList<>(2));
                list.add(test);
            }
        }

        for (final Map.Entry<String, List<RawStresource>> entry : lent.entrySet()) {

            for (final RawStresource stres : entry.getValue()) {

                // Look up the hold associated with the resource
                final RawResource res = RawResourceLogic.query(cache, stres.resourceId);
                if (res == null) {
                    Log.info("  ** WARNING: Unrecognized resource ID '" + stres.resourceId + "' in STRESOURCE for "
                             + stres.stuId);
                    continue;
                }

                if (res.holdId != null) {
                    // TODO: Increment "times" field in stres and replace the "stresource" object.

                    final RawHoldType type = cache.getSystemData().getHoldType(res.holdId);
                    final String sev = type == null ? "N" : type.sevAdminHold;

                    if (RawAdminHoldLogic.query(cache, stres.stuId, res.holdId) == null) {
                        final RawAdminHold hold = new RawAdminHold(stres.stuId,
                                res.holdId, sev, stres.timesDisplay, sysDate);

                        Log.info("  Adding hold " + res.holdId + FOR_STUDENT + stres.stuId);

                        if (!DEBUG) {
                            RawAdminHoldLogic.insert(cache, hold);
                        }
                        ++count;
                    } else {
                        Log.info("  Retaining existing hold " + res.holdId + FOR_STUDENT + stres.stuId);
                    }
                }
            }
        }

        Log.info("  Number of holds for overdue items that have been applied: " + count);
    }

    /**
     * Scans for students who have incompatible registrations, and applies the appropriate hold:
     * <ul>
     * <li>Hold "03(F)" if a student is registered for both a non-550 and a 550 section of a course</li>
     * <li>Hold "04(F)" if a student is registered for a course and has a DISCIPLINE table record for that course with
     * an action code of "04" that indicates they cannot ever register for that course again</li>
     * <li>Hold "16(F)" if the student has multiple registrations for the same course</li>
     * <li>Hold "23(F)" if the student is in courses with incompatible pacing structures</li>
     * <li>Hold "25(F)" if the student has an active Incomplete, but registered for the course again</li>
     * <li>Hold "27(F)" if the student is registered for 550 credit without a passing challenge exam</li>
     * </ul>
     *
     * @param cache  the cache
     * @param active the active term
     * @throws SQLException if there is an error accessing the database
     */
    private static void addRegistrationHolds(final Cache cache, final TermRec active) throws SQLException {

        final LocalDate today = LocalDate.now();
        // The following includes OT (challenge credit) courses but not dropped
        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, true, false);

        int num03Applied = 0;
        int num04Applied = 0;
        int num16Applied = 0;
        int num23Applied = 0;
        int num25Applied = 0;
        int num27Applied = 0;
        int num03Removed = 0;
        int num04Removed = 0;
        int num16Removed = 0;
        int num23Removed = 0;
        int num25Removed = 0;
        int num27Removed = 0;

        // Group registrations by student
        final Map<String, List<RawStcourse>> studentRegs = new HashMap<>(5000);
        for (final RawStcourse reg : allRegs) {
            final String stuId = reg.stuId;
            final List<RawStcourse> stuRegs = studentRegs.computeIfAbsent(stuId, s -> new ArrayList<>(5));
            stuRegs.add(reg);
        }

        final List<RawDiscipline> allDiscipline = RawDisciplineLogic.queryByActionCode(cache, "04");

        // Scan each student for conditions that warrant a hold
        final Map<String, RawAdminHold> holdsToApply = new HashMap<>(10);
        for (final Map.Entry<String, List<RawStcourse>> entry : studentRegs.entrySet()) {
            final String stuId = entry.getKey();
            final List<RawStcourse> regs = entry.getValue();

            // Check for registrations in an OT section and a non-OT section of the same course
            for (final RawStcourse reg : regs) {
                if ("OT".equals(reg.instrnType)) {
                    for (final RawStcourse reg2 : regs) {
                        if ("Y".equals(reg2.iInProgress)) {
                            continue;
                        }
                        if (!"OT".equals(reg2.instrnType) && reg.course.equals(reg2.course)) {
                            Log.warning("Student '", stuId, "' is registered for both an OT and a non-OT section of ",
                                    reg.course, " - adding hold 03");
                            final RawAdminHold hold03 = new RawAdminHold(stuId, "03", "F", ZERO, today);
                            holdsToApply.put("03", hold03);
                            ++num03Applied;
                            break;
                        }
                    }
                }
            }

            // Check for DISCIPLINE row that prevents registrations
            for (final RawDiscipline discip : allDiscipline) {
                if (discip.stuId.equals(stuId)) {
                    final String discipCourse = discip.course;
                    if (discipCourse != null) {
                        for (final RawStcourse reg : regs) {
                            if ("Y".equals(reg.iInProgress)) {
                                continue;
                            }
                            if (discip.course.equals(reg.course)) {
                                Log.warning("Student '", stuId, "' is registered for ", reg.course,
                                        " but has a DISCIPLINE row that prevents re-registering in that course - ",
                                        "adding hold 04");
                                final RawAdminHold hold04 = new RawAdminHold(stuId, "04", "F", ZERO, today);
                                holdsToApply.put("04", hold04);
                                ++num04Applied;
                                break;
                            }
                        }
                    }
                }
            }

            // Check for multiple registrations for the same course (both non-OT sections)
            for (final RawStcourse reg : regs) {
                if ("OT".equals(reg.instrnType) || "Y".equals(reg.iInProgress)) {
                    continue;
                }
                for (final RawStcourse reg2 : regs) {
                    if ("OT".equals(reg2.instrnType)) {
                        continue;
                    }
                    if (reg.course.equals(reg2.course) && !reg.sect.equals(reg2.sect)) {
                        Log.warning("Student '", stuId, "' is registered for multiple sections of ", reg.course,
                                " - adding hold 16");
                        final RawAdminHold hold16 = new RawAdminHold(stuId, "16", "F", ZERO, today);
                        holdsToApply.put("05", hold16);
                        ++num16Applied;
                        break;
                    }
                }
            }

            // Check for students in a mix of courses incompatible pacing structures
//            boolean hasNormalRI = false;
//            boolean hasLateStartRI = false;
            boolean hasNormalCE = false;
//            boolean hasLateStartCE = false;

            boolean hasF2F117Sect002 = false;
//            boolean hasF2F117Sect003 = false;
//            boolean hasF2F117Sect005 = false;
//            boolean hasF2F117Sect006 = false;
//
//            boolean hasF2F118Sect003 = false;
//
//            boolean hasF2F125Sect003 = false;
//            boolean hasF2F125Sect004 = false;
//            boolean hasF2F125Sect005 = false;
//
//            boolean hasF2F126Sect003 = false;

            for (final RawStcourse reg : regs) {
                if ("OT".equals(reg.instrnType)) {
                    continue;
                }
                if ("Y".equals(reg.iInProgress) && "N".equals(reg.iCounted)) {
                    continue;
                }

                final String course = reg.course;
                final String sect = reg.sect;

                if ("M 117".equals(course)) {
                    switch (sect) {
                        case "002" -> hasF2F117Sect002 = true;
//                        case "001" -> hasNormalRI = true;
//                        case "002" -> hasLateStartRI = true;
//                        case "003" -> hasF2F117Sect003 = true;
//                        case "005" -> hasF2F117Sect005 = true;
//                        case "006" -> hasF2F117Sect006 = true;
                        case "401", "801", "809" -> hasNormalCE = true;
                        case null, default -> Log.warning("Unexpected ", course, " section number: ", sect);
                    }
                } else if ("M 118".equals(course)) {
                    switch (sect) {
//                        case "001" -> hasNormalRI = true;
//                        case "002" -> hasLateStartRI = true;
//                        case "003" -> hasF2F118Sect003 = true;
                        case "401", "801", "809" -> hasNormalCE = true;
                        case null, default -> Log.warning("Unexpected ", course, " section number: ", sect);
                    }
                } else if ("M 124".equals(course)) {
                    switch (sect) {
//                        case "001" -> hasNormalRI = true;
//                        case "002" -> hasLateStartRI = true;
                        case "401", "801", "809" -> hasNormalCE = true;
                        case null, default -> Log.warning("Unexpected ", course, " section number: ", sect);
                    }
                } else if ("M 125".equals(course)) {
                    switch (sect) {
//                        case "001" -> hasNormalRI = true;
//                        case "002" -> hasLateStartRI = true;
//                        case "003" -> hasF2F125Sect003 = true;
//                        case "004" -> hasF2F125Sect004 = true;
//                        case "005" -> hasF2F125Sect005 = true;
                        case "401", "801" -> hasNormalCE = true;
//                        case "802" -> hasLateStartCE = true;
                        case null, default -> Log.warning("Unexpected ", course, " section number: ", sect);
                    }
                } else if ("M 126".equals(course)) {
                    switch (sect) {
//                        case "001" -> hasNormalRI = true;
//                        case "002" -> hasLateStartRI = true;
//                        case "003" -> hasF2F126Sect003 = true;
                        case "401", "801" -> hasNormalCE = true;
                        case null, default -> Log.warning("Unexpected ", course, " section number: ", sect);
                    }
                }
            }

            final boolean hasF2F = hasF2F117Sect002;

            boolean applyHold23 = false;

            if (hasNormalCE) { // || hasLateStartCE) {
//                if (hasNormalRI || hasLateStartRI) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for both online and distance sections - adding hold 23");
//                    applyHold23 = true;
//                } else
                if (hasF2F) {
                    Log.warning("Student '", stuId,
                            "' is registered for both distance and face-to-face sections - adding hold 23");
                    applyHold23 = true;
//                } else if (hasNormalCE && hasLateStartCE) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for both regular and late-start online sections - adding hold 23");
//                    applyHold23 = true;
                }
//            } else if (hasLateStartRI) {
//                if (hasNormalRI) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for both regular and late-start sections - adding hold 23");
//                    applyHold23 = true;
//                } else if (hasF2F) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for both late-start and face-to-face sections - adding hold 23");
//                    applyHold23 = true;
//                }
//            } else if (hasNormalRI) {
//                if (hasF2F) {
//                    Log.warning("Student '", stuId, "' is registered for both regular and face-to-face sections - ",
//                            "try to make it work with deadline overrides");
//                }
//            } else if (hasF2F117Sect003) {
//                if (hasF2F125Sect003 || hasF2F125Sect004 || hasF2F125Sect005 || hasF2F126Sect003) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for face-to-face MATH 117 and Trigonometry - adding hold 23");
//                    applyHold23 = true;
//                }
//            } else if (hasF2F117Sect005 || hasF2F117Sect006) {
//                if (hasF2F118Sect003) {
//                    Log.warning("Student '", stuId,
//                            "' has MATH 117 (005/006), but MATH 118 (003) - adding hold 23");
//                    applyHold23 = true;
//                } else if (hasF2F125Sect003 || hasF2F125Sect004 || hasF2F125Sect005 || hasF2F126Sect003) {
//                    Log.warning("Student '", stuId,
//                            "' is registered for face-to-face MATH 117 and Trigonometry - adding hold 23");
//                    applyHold23 = true;
//                }
//            } else if (hasF2F118Sect003) {
//                Log.warning("Student '", stuId, "' has MATH 118 (003), but not MATH 117 (003) - adding hold 23");
//                applyHold23 = true;
            }

            if (applyHold23) {
                final RawAdminHold hold23 = new RawAdminHold(stuId, "23", "F", ZERO, today);
                holdsToApply.put("23", hold23);
                ++num23Applied;
            }

            // Check for incompletes with another registration in the same course
            for (final RawStcourse reg : regs) {
                if ("Y".equals(reg.iInProgress)) {
                    for (final RawStcourse reg2 : regs) {
                        if ("N".equals(reg2.iInProgress) && reg.course.equals(reg2.course)) {
                            Log.warning("Student '", stuId,
                                    "' has both an Incomplete and an active registration for ",
                                    reg.course, " - adding hold 25");
                            final RawAdminHold hold25 = new RawAdminHold(stuId, "25", "F", ZERO, today);
                            holdsToApply.put("25", hold25);
                            ++num25Applied;
                            break;
                        }
                    }
                }
            }

            // Check that students with an OT registration have challenge credit
            for (final RawStcourse reg : regs) {
                if ("OT".equals(reg.instrnType)) {
                    boolean searching = true;
                    final List<RawStchallenge> challenges =
                            RawStchallengeLogic.queryByStudentCourse(cache, stuId, reg.course);
                    for (final RawStchallenge chal : challenges) {
                        if ("Y".equals(chal.passed)) {
                            searching = false;
                            break;
                        }
                    }

                    if (searching) {
                        Log.warning("Student '", stuId, "' registered for sect ", reg.sect, " of ", reg.course,
                                " but does not have a passing challenge exam result - adding hold 27");
                        final RawAdminHold hold27 = new RawAdminHold(stuId, "27", "F", ZERO, today);
                        holdsToApply.put("27", hold27);
                        ++num27Applied;
                        break;
                    }
                }
            }

            // Finally, reconcile the "holds to apply" list against the actual database
            final List<RawAdminHold> existingHolds = RawAdminHoldLogic.queryByStudent(cache, stuId);
            for (final RawAdminHold hold : holdsToApply.values()) {

                boolean searching = true;
                for (final RawAdminHold test : existingHolds) {
                    if (test.holdId.equals(hold.holdId)) {
                        RawAdminHoldLogic.updateAdminHoldDate(cache, hold);
                        searching = false;
                        break;
                    }
                }
                if (searching) {
                    RawAdminHoldLogic.insert(cache, hold);
                }
            }

            // Clear all existing holds (of the types this method detects) that this method did not indicate
            // should be // present
            for (final RawAdminHold test : existingHolds) {
                final String holdId = test.holdId;
                if ("03".equals(holdId) || "04".equals(holdId) || "16".equals(holdId) || "23".equals(holdId)
                    || "25".equals(holdId) || "27".equals(holdId)) {

                    boolean searching = true;
                    for (final RawAdminHold hold : holdsToApply.values()) {
                        if (holdId.equals(hold.holdId)) {
                            searching = false;
                            break;
                        }
                    }

                    if (searching) {
                        Log.info("Removing hold ", holdId, " for student '", stuId, "'");
                        RawAdminHoldLogic.delete(cache, test);

                        switch (holdId) {
                            case "03" -> ++num03Removed;
                            case "04" -> ++num04Removed;
                            case "16" -> ++num16Removed;
                            case "23" -> ++num23Removed;
                            case "25" -> ++num25Removed;
                            case "27" -> ++num27Removed;
                        }
                    }
                }
            }

            holdsToApply.clear();
        }

        Log.info("  Hold 03: Added ", Integer.toString(num03Applied), ", removed ", Integer.toString(num03Removed));
        Log.info("  Hold 04: Added ", Integer.toString(num04Applied), ", removed ", Integer.toString(num04Removed));
        Log.info("  Hold 16: Added ", Integer.toString(num16Applied), ", removed ", Integer.toString(num16Removed));
        Log.info("  Hold 23: Added ", Integer.toString(num23Applied), ", removed ", Integer.toString(num23Removed));
        Log.info("  Hold 25: Added ", Integer.toString(num25Applied), ", removed ", Integer.toString(num25Removed));
        Log.info("  Hold 27: Added ", Integer.toString(num27Applied), ", removed ", Integer.toString(num27Removed));
    }

    /**
     * Reconciles the "sev_admin_hold" field in the student table with the actual set of holds that exist for a student,
     * and updates the Student table accordingly.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void reconcileStudentTableWithHolds(final Cache cache) throws SQLException {

        final List<RawAdminHold> allStudentHolds = RawAdminHoldLogic.queryAll(cache);
        final int numStudentHolds = allStudentHolds.size();

        final Collection<String> withFatalHolds = new HashSet<>(numStudentHolds);
        final Collection<String> withNonfatalHolds = new HashSet<>(numStudentHolds);

        for (final RawAdminHold stuHold : allStudentHolds) {
            if ("F".equals(stuHold.sevAdminHold)) {
                withFatalHolds.add(stuHold.stuId);
            } else {
                withNonfatalHolds.add(stuHold.stuId);
            }
        }

        final List<RawStudent> allStudents = RawStudentLogic.queryAll(cache);

        int numCleared = 0;
        int numFatalSet = 0;
        int numNonfatalSet = 0;
        int numFatalRetained = 0;
        int numNonfatalRetained = 0;

        for (final RawStudent test : allStudents) {
            if (test.sevAdminHold == null) {
                // Currently clear - see if there should be a hold
                if (withFatalHolds.contains(test.stuId)) {
                    // Need to add a "F" severity to STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "F");
                    }
                    ++numFatalSet;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    // Need to add a "N" severity to STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "N");
                    }
                    ++numNonfatalSet;
                }
            } else if ("F".equals(test.sevAdminHold)) {
                if (withFatalHolds.contains(test.stuId)) {
                    ++numFatalRetained;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    // Need to change severity from "F" to "N" in STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "N");
                    }
                    ++numNonfatalSet;
                } else {
                    // Need to clear severity on STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, null);
                    }
                    ++numCleared;
                }
            } else // STUDENT record has "N"
                if (withFatalHolds.contains(test.stuId)) {
                    // Need to change severity from "N" to "F" in STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, "F");
                    }
                    ++numFatalSet;
                } else if (withNonfatalHolds.contains(test.stuId)) {
                    ++numNonfatalRetained;
                } else {
                    // Need to clear severity on STUDENT record
                    if (!DEBUG) {
                        RawStudentLogic.updateHoldSeverity(cache, test.stuId, null);
                    }
                    ++numCleared;
                }
        }

        Log.info("  Students whose hold severity was cleared: " + numCleared);
        Log.info("  Students whose hold severity was set to 'F': " + numFatalSet);
        Log.info("  Students whose hold severity was set to 'N': " + numNonfatalSet);
        Log.info("  Students whose hold severity 'F' was retained: " + numFatalRetained);
        Log.info("  Students whose hold severity 'N' was retained: " + numNonfatalRetained);
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        execute();
    }
}
