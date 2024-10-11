package dev.mathops.session;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.AbstractLogicModule;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.LiveReg;
import dev.mathops.db.old.svc.term.TermRec;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Performs a check against live registration data when a student logs in.
 */
public final class CsuLiveRegChecker {

    /** Common string in status messages. */
    private static final String IN_COURSE = " IN COURSE ";

    /** A commonly-used integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /**
     * Private constructor to prevent instantiation.
     */
    private CsuLiveRegChecker() {

        super();
    }

    /**
     * Checks the live registration system for updates to student or course registration data.
     *
     * @param cache the data cache
     * @param stuId the student ID to test
     * @throws SQLException if there was an error accessing the database
     */
    public static void checkLiveReg(final Cache cache, final String stuId) throws SQLException {

        Log.info("Checking live registrations for student ", stuId, "...");

        if (!stuId.isEmpty() && (int) stuId.charAt(0) == '8' && !"888888888".equals(stuId)) {

            final SystemData systemData = cache.getSystemData();

            final TermRec active = systemData.getActiveTerm();
            final String activeStr = active == null ? null : active.term.longString;

            if (active != null && activeStr != null) {

                if (AbstractLogicModule.isBannerDown()) {
                    Log.warning("Banner is currently down - skipping live query...");
                    return;
                }

                final List<LiveReg> liveRegs = LiveRegCache.queryLiveStudentRegs(cache, stuId);

                // The above will indicate Banner is down if the query fails, so abort here if so.
                if (AbstractLogicModule.isBannerDown()) {
                    Log.warning("Detected that Banner is currently down...");
                    return;
                }

                final HtmlBuilder liveStr = new HtmlBuilder(50);
                liveStr.add("Found ").add(liveRegs.size()).add(" live reg records for student ", stuId, ": ");
                for (final LiveReg liveReg : liveRegs) {
                    liveStr.add(liveReg.courseId).add('(').add(liveReg.sectionNum, ") ");
                }
                Log.info(liveStr.toString());

                if (!liveRegs.isEmpty()) {
                    checkForStudentUpdate(cache, stuId, liveRegs.getFirst());
                }

                // Update live registration "instruction type" based on course-section
                for (final LiveReg live : liveRegs) {
                    final String instrnType = systemData.getInstructionType(live.courseId, live.sectionNum,
                            active.term);
                    if (instrnType != null) {
                        live.setInstructionType(instrnType);
                    }
                }

                // Get all registrations (past and present)

                final List<RawStcourse> regs1 = RawStcourseLogic.queryByStudent(cache, stuId, active.term, true, false);

                final HtmlBuilder regStr = new HtmlBuilder(50);
                regStr.add("Found ").add(regs1.size()).add(" existing reg records for student ", stuId, ": ");
                for (final RawStcourse rawStcourse : regs1) {
                    regStr.add(rawStcourse.course, "(", rawStcourse.sect, ") ");
                }
                Log.info(regStr.toString());

                // See if there are new registrations not in the local database
                checkForNewReg(cache, regs1, liveRegs, activeStr);

                // Re-query
                final List<RawStcourse> regs2 = RawStcourseLogic.queryByStudent(cache, stuId, active.term, true, false);
                final HtmlBuilder regStr2 = new HtmlBuilder(50);
                regStr2.add("Post-add, found ").add(regs2.size()).add(" existing reg records for student ", stuId,
                        ": ");
                for (final RawStcourse rawStcourse : regs2) {
                    regStr2.add(rawStcourse.course, "(", rawStcourse.sect, ") ");
                }
                Log.info(regStr2.toString());

                // Now see if any "current" registrations are not in the live list
                scanForDrops(cache, regs1, liveRegs, active);

                final List<RawStcourse> regs3 = RawStcourseLogic.queryByStudent(cache, stuId, active.term, true, false);
                final HtmlBuilder regStr3 = new HtmlBuilder(50);
                regStr3.add("Post-drop, found ").add(regs3.size()).add(" existing reg records for student ", stuId,
                        ": ");
                for (final RawStcourse rawStcourse : regs3) {
                    regStr3.add(rawStcourse.course, "(", rawStcourse.sect, ") ");
                }
                Log.info(regStr3.toString());

                // Re-query the new list of registrations so we can validate...
                final List<RawStcourse> updated = RawStcourseLogic.queryByStudent(cache, stuId, active.term, true,
                        false);

                // Check for a mixture of rule sets. If one is found, add a hold 23 if such a
                // hold does not already exist for the student. If none are found, delete any
                // hold 23 or hold 07 on the student account.
                if (hasMixedRuleSets(cache, updated, stuId)) {
                    addFatalHold(cache, stuId, "23", ZERO);
                } else {
                    removeFatalHolds(cache, stuId, "23", "07");
                }
            }
        }
    }

    /**
     * Determines whether the student record needs to be updated.
     *
     * @param cache   the data cache
     * @param userId  the user ID to test
     * @param liveReg a live registration record with updated student data
     * @throws SQLException if there is an error accessing the database
     */
    private static void checkForStudentUpdate(final Cache cache, final String userId,
                                              final LiveReg liveReg) throws SQLException {

        final RawStudent student = RawStudentLogic.query(cache, userId, true);

        final Integer sat = liveReg.satrScore == null ? liveReg.satScore : liveReg.satrScore;

        if (student == null) {
            Log.info(" *** INSERTING STUDENT RECORD FOR STUDENT ", userId);
            RawStudentLogic.INSTANCE.insertFromLive(cache, liveReg);
        } else if (isDifferent(liveReg.studentId, student.stuId)
                   || isDifferent(liveReg.internalId, student.pidm)
                   || isDifferent(liveReg.firstName, student.firstName)
                   || isDifferent(liveReg.lastName, student.lastName)
                   || isDifferent(liveReg.classLevel, student.clazz)
                   || isDifferent(liveReg.college, student.college)
                   || isDifferent(liveReg.department, student.dept)
                   || isDifferent(liveReg.major1, student.programCode)
                   || isDifferent(liveReg.anticGradTerm, student.estGraduation)
                   || isDifferent(liveReg.highSchoolCode, student.hsCode)
                   || isDifferent(liveReg.highSchoolGpa, student.hsGpa)
                   || isDifferent(liveReg.highSchoolClassRank, student.hsClassRank)
                   || isDifferent(liveReg.highSchoolClassSize, student.hsSizeClass)
                   || isDifferent(liveReg.actScore, student.actScore) //
                   || isDifferent(sat, student.satScore) //
                   || isDifferent(liveReg.apScore, student.apScore)
                   || isDifferent(liveReg.birthDate, student.birthdate)
                   || isDifferent(liveReg.gender, student.gender)
                   || isDifferent(liveReg.email, student.stuEmail)
                   || isDifferent(liveReg.adviserEmail, student.adviserEmail)
                   || isDifferent(liveReg.campus, student.campus)
                   || isDifferent(liveReg.admitType, student.admitType)
                   || isDifferent(liveReg.numTransferCredits, student.trCredits)) {

            Log.fine(" *** Transfer Credits: ", student.trCredits, " -> ", liveReg.numTransferCredits);
            Log.fine(" *** Resident:         ", student.resident, " -> ", liveReg.residency);
            Log.fine(" *** Admit Type:       ", student.admitType, " -> ", liveReg.admitType);
            Log.fine(" *** Campus:           ", student.campus, " -> ", liveReg.campus);

            Log.fine(" *** UPDATING STUDENT RECORD FOR STUDENT ", userId);
            RawStudentLogic.updateFromLive(cache, student, liveReg);
        }
    }

    /**
     * Tests whether two objects are different, where either may be {@code null}.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     * @return {@code true} if the objects are different
     */
    private static boolean isDifferent(final Object obj1, final Object obj2) {

        final boolean different;

        // FIXME: Once "LiveReg" is a Pojo, the "Long vs Integer" part below can go away

        if (obj1 == null) {
            different = obj2 != null;
        } else if ((obj1 instanceof Long) && (obj2 instanceof Integer)) {
            different = ((Long) obj1).intValue() != ((Integer) obj2).intValue();
        } else {
            different = !obj1.equals(obj2);
        }

        return different;
    }

    /**
     * Checks whether any of the registrations in the live registration list are not in the local registration list, and
     * adds any that are not.
     *
     * @param cache     the data cache
     * @param regs      the student's current local registration records
     * @param liveRegs  the live registration records for the student
     * @param activeStr the active term string
     * @throws SQLException if there was an error accessing the database
     */
    private static void checkForNewReg(final Cache cache, final List<RawStcourse> regs,
                                       final List<LiveReg> liveRegs, final String activeStr) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        final TermRec active = systemData.getActiveTerm();

        // Remove useless rows from the live reg query
        final Iterator<LiveReg> iter = liveRegs.iterator();
        while (iter.hasNext()) {
            final LiveReg live = iter.next();

            if (!live.term.longString.equals(activeStr)) {
                Log.info("Ignoring live registration ", live.courseId, "(", live.sectionNum, ") - different term");
                iter.remove();
            } else if (live.courseId == null || live.sectionNum == null || live.sectionNum.isEmpty()) {
                Log.info("Ignoring live registration ", live.courseId, "(", live.sectionNum, ") - incomplete data");
                iter.remove();
            } else if (systemData.getCourseSection(live.courseId, live.sectionNum, active.term) == null) {
                Log.info("Ignoring live registration ", live.courseId, "(", live.sectionNum,
                        ") - course/section not in system");
                iter.remove();
            }
        }

        // First pass - it's possible that a student has two registrations for the same course
        // in live regs, different sections. If this occurs, we need to add the fatal HOLD to the
        // student, but we will discard all but one section in the live data (favoring the section
        // that's in the local database, if one exists).
        scanForDuplicateRegs(cache, regs, liveRegs);

        // Process each live registration in turn, adding a new student course record or updating
        // the existing record in place
        for (final LiveReg live : liveRegs) {
            checkRegistrations(cache, regs, live, activeStr);
        }
    }

    /**
     * Test for duplicate registrations (multiple registrations for the same course but in different sections) in the
     * live data. If any is found, make sure the fatal hold is applied, and remove the duplicate sections (if there is a
     * row in the local database, keep that one).
     *
     * @param cache    the data cache
     * @param regs     the student's current local registration records
     * @param liveRegs the live registration records for the student
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanForDuplicateRegs(final Cache cache, final Iterable<RawStcourse> regs,
                                             final List<LiveReg> liveRegs) throws SQLException {

        // Gather unique course IDs
        final Collection<String> courseIds = new HashSet<>(liveRegs.size());
        for (final LiveReg live : liveRegs) {
            courseIds.add(live.courseId);
        }

        // Quick size test to see if there's nothing to do
        if (courseIds.size() < liveRegs.size()) {

            final List<String> liveSections = new ArrayList<>(liveRegs.size());

            // Course list size differs - must be at least one duplicate
            for (final String courseId : courseIds) {

                // Count number of rows in the live data with this course ID
                liveSections.clear();
                for (final LiveReg live : liveRegs) {
                    if (courseId.equals(live.courseId)) {
                        liveSections.add(live.sectionNum);
                    }
                }

                if (liveSections.size() > 1) {
                    // There is a duplicate - see if there is a local section
                    String localSection = null;
                    for (final RawStcourse reg : regs) {
                        if (courseId.equals(reg.course)) {
                            localSection = reg.sect;
                            break;
                        }
                    }

                    // If there is a local section, see if it is also in live data
                    LiveReg localInLive = null;
                    if (localSection != null) {
                        for (final LiveReg live : liveRegs) {
                            if (courseId.equals(live.courseId) && localSection.equals(live.sectionNum)) {
                                localInLive = live;
                                break;
                            }
                        }
                    }

                    if (localSection == null || localInLive == null) {
                        // There is no local section, or the local section will get deleted since
                        // it is not in the live data - pick the "live" section with the lowest
                        // section number to keep (is this the right choice?)
                        Collections.sort(liveSections);
                        final String keepSect = liveSections.getFirst();
                        liveRegs.removeIf(next -> courseId.equals(next.courseId) && !keepSect.equals(next.sectionNum));
                    } else {
                        // There is a local section, and it appears in the live data, so remove all
                        // other sections from the live data.
                        final Iterator<LiveReg> iter = liveRegs.iterator();
                        while (iter.hasNext()) {
                            final LiveReg next = iter.next();
                            if (courseId.equals(next.courseId) && !localInLive.equals(next)) {
                                iter.remove();
                            }
                        }
                    }

                    // There was a duplicate - add the fatal hold if it's not already there.
                    final String studentId = liveRegs.getFirst().studentId;
                    Log.warning("ADDING HOLD 14 (RI and CE registrations for same course) FOR STUDENT ", studentId,
                            " FOR COURSE ", courseId);
                    addFatalHold(cache, studentId, "14", ZERO);
                }
            }
        }
    }

    /**
     * Checks all local registrations against a live registration.
     *
     * @param cache     the data cache
     * @param regs      the student's current local registration records
     * @param live      the live registration record being tested for the student
     * @param activeStr the active term string
     * @return {@code true} if the live registration can be skipped
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean checkRegistrations(final Cache cache, final List<RawStcourse> regs,
                                              final LiveReg live, final String activeStr) throws SQLException {

        boolean found = false;

        final int numRegs = regs.size();
        for (int j = 0; j < numRegs && !found; ++j) {
            found = checkRegistration(cache, regs.get(j), live, activeStr);
        }

        final boolean skip;

        if (found) {
            skip = true;
        } else {
            final String sect = live.sectionNum;
            final String courseId = live.courseId;

            if ("M 101".equals(courseId) && (int) sect.charAt(0) == 'L') {
                // Do not add registration rows for lab sections
                skip = true;
            } else if (("M 160".equals(courseId) || "M 161".equals(courseId) || "M 261".equals(courseId))
                       && (int) sect.charAt(0) != '8' && (int) sect.charAt(0) != '4') {
                // Only add 8** and 4** sections of Calculus
                skip = true;
            } else {
                if ("550".equals(sect)) {
                    final boolean foundC = checkPlacementCredit(cache, live);
                    addRegistration(cache, live, foundC);
                } else {
                    addRegistration(cache, live, false);
                }

                skip = false;
            }
        }

        return skip;
    }

    /**
     * Makes an appropriate grading option value from that stored in the live registration record.
     *
     * @param live the live registration record
     * @return the grading option value
     */
    private static String makeGradingOption(final LiveReg live) {

        String option = live.gradingOption;

        if ("M 101".equals(live.courseId) && ("2".equals(option) || "S".equals(option) || "L".equals(option))) {
            option = "T";
        }

        return option;
    }

    /**
     * Makes an appropriate instruction type value from that stored in the live registration record.
     *
     * @param live the live registration record
     * @return the instruction type value
     */
    private static String makeInstructionType(final LiveReg live) {

        String instrType = live.instructionType;

        if ("550".equals(live.sectionNum) && "RI".equals(instrType)) {
            instrType = "OT";
        } else if ("CT".equals(instrType)) {
            instrType = "RI";
        }

        // FIXME - lookup in Course section table.

        return instrType;
    }

    /**
     * Compare a local registration record to a live registration record. If they match, update any fields in the local
     * registration record needed to bring it up to date.
     *
     * @param cache     the data cache
     * @param reg       the local registration record
     * @param live      the live registration record
     * @param activeStr the active term string
     * @return {@code true} if the records represent the same registration
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean checkRegistration(final Cache cache, final RawStcourse reg, final LiveReg live,
                                             final String activeStr) throws SQLException {

        final boolean found;

        // If there is a registration, but not for the current term, it must be an incomplete from
        // a prior term, and so we don't want to insert the new registration. Rather, we want to
        // apply a hold for a student who has a current term registration in a course for which
        // they have an active incomplete from an earlier term
        if (!reg.termKey.longString.equals(activeStr) && "Y".equals(reg.iInProgress)) {

            Log.warning("ADDING HOLD 25 (active reg while working on incomplete) FOR STUDENT ", reg.stuId,
                    " FOR COURSE ", reg.course);
            addFatalHold(cache, reg.stuId, "25", ZERO);

            found = true;
        } else {
            final String courseId = live.courseId;
            final String sect = live.sectionNum;

            if (courseId.equals(reg.course) && sect.equals(reg.sect)) {
                updateAsNeeded(cache, reg, live);
                found = true;
            } else {
                found = false;
            }
        }

        return found;
    }

    /**
     * Updates a local registration record with data from a live registration record.
     *
     * @param cache the data cache
     * @param reg   the local registration record
     * @param live  the live registration record
     * @throws SQLException if there is an error accessing the database
     */
    private static void updateAsNeeded(final Cache cache, final RawStcourse reg, final LiveReg live)
            throws SQLException {

        final String option = makeGradingOption(live);
        if (option != null && !Objects.equals(option, reg.gradingOption)) {
            Log.info(" *** STUDENT ", live.studentId, IN_COURSE, live.courseId, " Grading Option:      ",
                    reg.gradingOption, " -> ", option);

            if (!RawStcourseLogic.updateGradingOption(cache, reg.stuId, reg.course, reg.sect, reg.termKey, option)) {
                Log.warning("Failed to update stcourse record");
            }
        }

        if (live.registrationStatus != null
            && !Objects.equals(live.registrationStatus, reg.registrationStatus)) {
            Log.info(" *** STUDENT ", live.studentId, IN_COURSE, live.courseId, " Registration Status: ",
                    reg.registrationStatus, " -> ", live.registrationStatus);

            if (!RawStcourseLogic.updateRegistrationStatus(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                    live.registrationStatus)) {
                Log.warning("Failed to update stcourse record");
            }
        }

        final String newInstrnType = makeInstructionType(live);
        if (newInstrnType != null && !Objects.equals(newInstrnType, reg.instrnType)) {
            Log.info(" *** STUDENT ", live.studentId, IN_COURSE, live.courseId, " Instruction type:    ",
                    newInstrnType, " -> ", live.registrationStatus);

            if (!RawStcourseLogic.updateInstructionType(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                    newInstrnType)) {
                Log.warning("Failed to update stcourse record");
            }
        }
    }

    /**
     * Checks that a placement credit registration is matched by a record of placement credit being awarded.
     *
     * @param cache the data cache
     * @param live  the live registration of the placement credit record
     * @return {@code true} if a suitable placement record was found to justify the 550 row
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean checkPlacementCredit(final Cache cache, final LiveReg live) throws SQLException {

        final List<RawMpeCredit> results = RawMpeCreditLogic.queryByStudent(cache, live.studentId);
        boolean foundC = false;

        for (final RawMpeCredit result : results) {
            if (live.courseId.equals(result.course) && "C".equals(result.examPlaced) && result.dtCrRefused == null) {

                foundC = true;
                break;
            }
        }

        if (!foundC) {
            // FIXME: Send an email or log to an exception table
            Log.info(" *** GOT 550 SECTION, NO MPE CREDIT FOR STUDENT ", live.studentId, IN_COURSE, live.courseId);
            addFatalHold(cache, live.studentId, "27", ZERO);
        }

        return foundC;
    }

    /**
     * Adds a new registration for a student base on a live registration record.
     *
     * @param cache  the data cache
     * @param live   the live registration record
     * @param foundC for a row with section 550, this flag is true of a matching placement credit record was found
     * @throws SQLException if there is an error accessing the database
     */
    private static void addRegistration(final Cache cache, final LiveReg live, final boolean foundC)
            throws SQLException {

        Log.info(" *** ADDING REGISTRATION FOR STUDENT ", live.studentId, IN_COURSE, live.courseId, "(",
                live.sectionNum, ")");

        final String sect = live.sectionNum;
        final String option = makeGradingOption(live);
        final String instrType = makeInstructionType(live);

        final RawStcourse reg = new RawStcourse(live.term, // term
                live.studentId, // stuId
                live.courseId, // course
                sect, // sect
                null, // paceOrder
                null, // openStatus
                option, // gradingOption
                "N", // completed
                null, // score
                null, // courseGrade
                null, // prereqSatis
                "N", // initClassRoll
                "N", // stuProvided
                "Y", // finalClassRoll
                "550".equals(sect) && foundC ? "M" : null, // examPlaced
                null, // zeroUnit
                null, // timeoutFactor
                null, // forfeitI
                "N", // iInProgress
                null, // iCounted
                "N", // ctrlTest
                null, // deferredFDt
                ZERO, // bypassTimeout
                instrType, // instrType
                live.registrationStatus, // registrationStatus
                LocalDate.now(), // lastClassRollDt
                null, // iTermKey
                null); // iDeadlineDt

        RawStcourseLogic.INSTANCE.insert(cache, reg);

        final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, live.studentId, live.term);

        PaceTrackLogic.updateStudentTerm(cache, live.studentId, regs);
    }

    /**
     * Check for registrations that are present in the local database but not in the live registration data (which
     * indicates a course was dropped).
     *
     * @param cache    the data cache
     * @param regs     the student's current local registration records
     * @param liveRegs the live registration records for the student
     * @param active   the active term
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanForDrops(final Cache cache, final Iterable<RawStcourse> regs,
                                     final Collection<LiveReg> liveRegs, final TermRec active) throws SQLException {

        for (final RawStcourse reg : regs) {

            // Leave rows from prior terms and dropped rows in place
            // Make sure the row has course/section data (should be unnecessary)
            if (!reg.termKey.longString.equals(active.term.longString) || "D".equals(reg.openStatus)
                || reg.course == null || reg.sect == null) {
                continue;
            }

            scanForDrop(cache, reg, liveRegs);
        }
    }

    /**
     * Examine a single local course registration to see if it is not in the live registration list. If it is not, the
     * local record is marked as having been dropped.
     *
     * @param cache    the data cache
     * @param reg      the registration to check
     * @param liveRegs the list of live registration records
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanForDrop(final Cache cache, final RawStcourse reg,
                                    final Collection<LiveReg> liveRegs) throws SQLException {

        boolean searching = true;
        for (final LiveReg live : liveRegs) {
            if (reg.course.equals(live.courseId) && reg.sect.equals(live.sectionNum)) {
                searching = false;
                break;
            }
        }

        // Temporary patch: we can't tell if a live-reg query failed, or simply returned no
        // results. If it failed, we will be marking rows as dropped that are not really dropped!
        // To prevent this, we will require that at least one live reg was returned, which will
        // mitigate the possibility that the query failed outright. We will miss the case where a
        // student has dropped their last course, but these students should not be logging back in
        // any way. To address this, I added "&& !liveRegs.isEmpty()" to the test below.

        if (searching && "N".equals(reg.iInProgress) && !liveRegs.isEmpty()) {
            if ("550".equals(reg.sect)) {
                Log.warning(" *** FOUND DROPPED 550 REGISTRATION FOR STUDENT ", reg.stuId, IN_COURSE, reg.course);
            }

            Log.info(" *** MARKING REGISTRATION FOR STUDENT ", reg.stuId, IN_COURSE, reg.course, " AS DROPPED");

            if (RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, reg.stuId, reg.course, reg.sect, reg.termKey,
                    "D", "N", reg.lastClassRollDt)) {
                reg.openStatus = "D";
                reg.finalClassRoll = "N";
            }

            final List<RawStcourse> regs = RawStcourseLogic.getActiveForStudent(cache, reg.stuId, reg.termKey);

            PaceTrackLogic.updateStudentTerm(cache, reg.stuId, regs);
        }
    }

    /**
     * Adds a fatal hold to a student's record, if the hold does not already exist.
     *
     * @param cache        the data cache
     * @param studentId    the student ID
     * @param holdId       the hold ID
     * @param timesApplied the number of times the hold has been applied for a resource loan or rental (zero for holds
     *                     that are not for overdue resource loans/rentals)
     * @throws SQLException if there is an error accessing the database
     */
    private static void addFatalHold(final Cache cache, final String studentId, final String holdId,
                                     final Integer timesApplied) throws SQLException {

        final RawAdminHold existing = RawAdminHoldLogic.query(cache, studentId, holdId);

        if (existing == null) {
            final RawAdminHold hold = new RawAdminHold(studentId, holdId, "F", timesApplied, LocalDate.now());

            if (RawAdminHoldLogic.INSTANCE.insert(cache, hold)) {

                // Indicate the hold is fatal.
                final RawStudent stu = RawStudentLogic.query(cache, studentId, true);
                if (!"F".equals(stu.sevAdminHold)) {
                    RawStudentLogic.updateHoldSeverity(cache, stu.stuId, "F");
                }
            }
        }
    }

    /**
     * Removes any fatal holds of one or more types from a student's record, and if these represent the last fatal holds
     * on the student account, clears the hold severity flag in the student record.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @param holdIds   the hold IDs to remove
     * @throws SQLException if there is an error accessing the database
     */
    private static void removeFatalHolds(final Cache cache, final String studentId,
                                         final String... holdIds) throws SQLException {

        for (final String holdId : holdIds) {
            final RawAdminHold existing = RawAdminHoldLogic.query(cache, studentId, holdId);

            if (existing != null) {
                RawAdminHoldLogic.INSTANCE.delete(cache, existing);
            }
        }

        final List<RawAdminHold> holds = RawAdminHoldLogic.queryByStudent(cache, studentId);

        String severity = null;
        for (final RawAdminHold test : holds) {
            if ("F".equals(test.sevAdminHold)) {
                severity = "F";
                break;
            } else if ("N".equals(test.sevAdminHold)) {
                severity = "N";
            }
        }

        final RawStudent student = RawStudentLogic.query(cache, studentId, true);

        if (Objects.nonNull(student)) {
            if (severity == null && Objects.nonNull(student.sevAdminHold)) {
                RawStudentLogic.updateHoldSeverity(cache, student.stuId, null);
            } else if (Objects.nonNull(severity) && !severity.equals(student.sevAdminHold)) {
                RawStudentLogic.updateHoldSeverity(cache, student.stuId, severity);
            }
        }
    }

    /**
     * Tests whether the student has a mixture of rule sets in active, paced, current-term registrations.
     * FIXME: We really should do this only for courses that apply in the current login context.
     *
     * @param cache  the data cache
     * @param regs   the student registrations
     * @param userId the ID of the user
     * @return true if student has a mixture of rule sets
     * @throws SQLException if there was an error accessing the database
     */
    private static boolean hasMixedRuleSets(final Cache cache, final Iterable<RawStcourse> regs,
                                            final String userId) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        final TermRec active = systemData.getActiveTerm();

        final Collection<String> pacingStructures = new HashSet<>(4);

        for (final RawStcourse reg : regs) {

            if ("G".equals(reg.openStatus)) {
                continue;
            }

            // Find each registration's course section
            final RawCsection csection = systemData.getCourseSection(reg.course, reg.sect, active.term);
            if (csection == null || "OT".equals(csection.instrnType)) {
                continue;
            }

            final RawPacingStructure ruleSet = systemData.getPacingStructure(csection.pacingStructure, active.term);
            if (ruleSet == null || !"pace".equals(ruleSet.scheduleSource)) {
                continue;
            }

            pacingStructures.add(ruleSet.pacingStructure);
        }

        boolean mixed = false;
        String pacingStructure = null;
        if (pacingStructures.size() > 1) {
            boolean hasS = false;
            boolean hasM = false;
            boolean hasO = false;
            for (final String test : pacingStructures) {
                if ("S".equals(test)) {
                    hasS = true;
                }
                if ("M".equals(test)) {
                    hasM = true;
                }
                if ("O".equals(test)) {
                    hasO = true;
                }
            }

            if (hasS) {
                if (hasO) {
                    mixed = true;
                } else if (hasM) {
                    // FIXME: We choose M since new courses don't use online assignments yet...
                    pacingStructure = "M";
                } else {
                    pacingStructure = "S";
                }
            } else {
                mixed = hasM && hasO;
            }
        }

        // If there is no mixture, but a rule set ID was found, make sure the student record is
        // marked with that rule set ID
        if (!mixed && pacingStructure != null) {
            final RawStudent student = RawStudentLogic.query(cache, userId, true);

            if (student != null && student.pacingStructure == null) {
                RawStudentLogic.updatePacingStructure(cache, student.stuId, pacingStructure);
            }
        }

        return mixed;
    }
}
