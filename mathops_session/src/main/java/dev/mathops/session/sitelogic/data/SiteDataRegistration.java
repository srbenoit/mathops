package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.field.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.course.PaceTrackLogic;
import dev.mathops.db.logic.course.PrerequisiteLogic;
import dev.mathops.db.schema.legacy.impl.RawFfrTrnsLogic;
import dev.mathops.db.schema.legacy.impl.RawStcourseLogic;
import dev.mathops.db.schema.legacy.impl.RawSttermLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawAdminHold;
import dev.mathops.db.schema.legacy.rec.RawCsection;
import dev.mathops.db.schema.legacy.rec.RawFfrTrns;
import dev.mathops.db.schema.legacy.rec.RawPacingStructure;
import dev.mathops.db.schema.legacy.rec.RawSpecialStus;
import dev.mathops.db.schema.legacy.rec.RawStcourse;
import dev.mathops.db.schema.legacy.rec.RawStterm;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.schema.main.rec.TermRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A container for the registration-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataRegistration {

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The active term. */
    private TermRec active = null;

    /** All completed student course records, including past terms. */
    private List<RawStcourse> allCompletedCourses = null;

    /** All student course records in the current term. */
    private List<RawStcourse> registrations = null;

    /** All student course records in the current term. */
    private List<TermRec> registrationTerms = null;

    /** The student course records in the current term that count towards pace . */
    private List<RawStcourse> paceRegistrations = null;

    /** The student transfer credit records (CTransferCredit). */
    private List<RawFfrTrns> transferCredit = null;

    /** The list of prerequisites for each course (course ID). */
    private List<List<String>> prereqs = null;

    /** A flag indicating student has at least one incomplete that is not completed. */
    private boolean nonPacedIncompletePending = false;

    /**
     * A flag indicating student has fatal holds, but they are all type "30" (locked out), meaning the student may not
     * work in paced courses.
     */
    private boolean lockedOut = true;

    /**
     * Constructs a new {@code SiteDataRegistration}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataRegistration(final SiteData theOwner) {

        this.owner = theOwner;
    }

    /**
     * Gets the complete set of all student course records, including those for past terms. This data is used to check
     * for prerequisites.
     *
     * @return the student course records
     */
    public List<RawStcourse> getAllCompletedCourses() {

        return this.allCompletedCourses == null ? null : new ArrayList<>(this.allCompletedCourses);
    }

    /**
     * Gets the active term.
     *
     * @return the active term
     */
    public TermRec getActiveTerm() {

        return this.active;
    }

    /**
     * Gets the current term student course record for a particular course and section.
     *
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @return the student course record
     */
    RawStcourse getRegistration(final String courseId, final String sectionNum) {

        RawStcourse result = null;

        for (final RawStcourse registration : this.registrations) {
            if (registration.course.equals(courseId) && registration.sect.equals(sectionNum)) {
                result = registration;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the student's registration in a specified course.
     *
     * @param course the course ID
     * @return the registration record; null if none found
     */
    public RawStcourse getRegistration(final String course) {

        RawStcourse result = null;

        for (final RawStcourse test : this.registrations) {
            if (test.course.equals(course)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Gets the term record in which the student registered for a current term registration (typically the current term,
     * but for incompletes, this is the incomplete term).
     *
     * @param courseId   the course ID
     * @param sectionNum the section number
     * @return the term record (CTerm)
     */
    public TermRec getRegistrationTerm(final String courseId, final String sectionNum) {

        TermRec result = null;

        final int count = this.registrations.size();
        for (int i = 0; i < count; ++i) {
            final RawStcourse reg = this.registrations.get(i);

            if (reg.course.equals(courseId) && this.registrations.get(i).sect.equals(sectionNum)) {
                result = this.registrationTerms.get(i);
                break;
            }
        }

        return result;
    }

    /**
     * Gets the student registration records for the current term (including advance placement and forfeit courses, but
     * excluding drop and withdrawal courses).
     *
     * @return the registration records (CStudentCourse)
     */
    public List<RawStcourse> getRegistrations() {

        return new ArrayList<>(this.registrations);
    }

    /**
     * Gets the student registration records for the current term that are included in the student's pace.
     *
     * @return the registration records (CStudentCourse)
     */
    public List<RawStcourse> getPaceRegistrations() {

        return this.paceRegistrations == null ? null : new ArrayList<>(this.paceRegistrations);
    }

    /**
     * Gets the student transfer credit records.
     *
     * @return the student transfer credit records (CTransferCredit)
     */
    public List<RawFfrTrns> getTransferCredit() {

        return this.transferCredit == null ? null : new ArrayList<>(this.transferCredit);
    }

    /**
     * Gets the prerequisites for a student registration.
     *
     * @param courseId the course ID
     * @return the prerequisites (CRuleSet)
     */
    public List<String> getPrerequisites(final String courseId) {

        List<String> result = null;

        if (this.registrations != null) {
            final int count = this.registrations.size();
            for (int i = 0; i < count; ++i) {
                if (courseId.equals(this.registrations.get(i).course)) {
                    result = this.prereqs.get(i);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether the student has any non-paced incompletes yet to complete.
     *
     * @return {@code true} if the student has one or more non-paced incompletes that are not yet complete
     */
    boolean isNonPacedIncompletePending() {

        return this.nonPacedIncompletePending;
    }

    /**
     * Tests whether the student is locked out of paced courses by a type "30" hold.
     *
     * @return {@code true} if the student is locked out (fatal holds exist on the student's account, but are all type
     *         "30")
     */
    boolean isLockedOut() {

        return this.lockedOut;
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, all pace track rules, the {@code SiteDataContext} object, the {@code SiteDataStudent} object, and the
     * {@code SiteDataProfile} object.
     *
     * @param cache   the data cache
     * @param session the login session
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final Cache cache, final ImmutableSessionInfo session) throws SQLException {

        final SystemData systemData = cache.getSystemData();
        this.active = systemData.getActiveTerm();

        final String studentId = this.owner.studentData.getStudent().stuId;

        final boolean b1 = loadRegistrations(cache, studentId);

        this.transferCredit = RawFfrTrnsLogic.queryByStudent(cache, studentId);

        final boolean b2 = buildSpecialStuRegs(cache, session);

        boolean success = b1 && b2;

        if (success) {
            final int numCourses = getRegistrations().size();

            this.prereqs = new ArrayList<>(numCourses);
            for (final RawStcourse registration : this.registrations) {
                final List<String> prerequisites = systemData.getPrerequisitesByCourse(registration.course);
                this.prereqs.add(prerequisites);
            }

            final boolean b6 = checkPrerequisites(cache, studentId);
            final boolean b7 = loadPaceRegistrations(cache);
            final boolean b8 = determineStudentRuleSet(cache);
            final boolean b9 = determinePaceTrack(cache);

            success = b6 && b7 && b8 && b9;

            // Determine whether the student may work in paced and non-paced, only non-paced, or
            // neither based on hold status
            final SiteDataStudent stuData = this.owner.studentData;

            final RawStudent stu = stuData.getStudent();
            if ("F".equals(stu.sevAdminHold)) {
                final List<RawAdminHold> holds = stuData.getStudentHolds();
                boolean all30 = true;
                for (final RawAdminHold hold : holds) {
                    if (!"30".equals(hold.holdId)) {
                        all30 = false;
                        break;
                    }
                }

                this.lockedOut = all30;
            }
        }

        return success;
    }

    /**
     * Updates the pace order in the database for a registration record.
     *
     * @param cache        the data cache
     * @param reg          the registration record
     * @param newPaceOrder the new pace order
     * @throws SQLException if there is an error accessing the database
     */
    public static void updatePaceOrder(final Cache cache, final RawStcourse reg,
                                       final Integer newPaceOrder) throws SQLException {

        if (RawStcourseLogic.updatePaceOrder(cache, reg.stuId, reg.course, reg.sect, reg.termKey, newPaceOrder)) {
            reg.paceOrder = newPaceOrder;
        }
    }

    /**
     * Loads all completed course records (regardless of term) for a student, as well as all current term records that
     * have not been dropped. This will include "ignored" records.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadRegistrations(final Cache cache, final String studentId) throws SQLException {

        final boolean success = true;

        // Load all courses marked as completed (for all terms), that are not credit by exam, used for testing
        // prerequisites (we store credit by exam elsewhere)
        final List<RawStcourse> allPastAndCurrent = RawStcourseLogic.queryByStudent(cache, studentId, false, false);

        this.allCompletedCourses = new ArrayList<>(allPastAndCurrent.size());
        for (final RawStcourse past : allPastAndCurrent) {
            if ("Y".equals(past.completed)) {
                this.allCompletedCourses.add(past);
            }
        }

        // Now load current term registrations, but discard any "dropped" (keep those forfeit)

        final List<RawStcourse> curTermReg =
                RawStcourseLogic.queryByStudent(cache, studentId, this.active.term, true, false);

        this.registrations = new ArrayList<>(curTermReg.size());
        this.registrationTerms = new ArrayList<>(curTermReg.size());

        for (final RawStcourse cur : curTermReg) {

            // Ignore courses with an incomplete deadline date but which are not incompletes in progress and are not
            // counted in pace

            if (cur.iDeadlineDt != null && "N".equals(cur.iInProgress) && !"Y".equals(cur.iCounted)) {
                continue;
            }

            this.registrations.add(cur);
            Log.info("Found registration in ", cur.course);

            // If we find a course that has an incomplete deadline date, is not completed, and is not counted in pace,
            // flag that since the student will have to work on those courses before starting any paced courses.
            if ("Y".equals(cur.iInProgress) && "N".equals(cur.iCounted) && "N".equals(cur.completed)) {
                this.nonPacedIncompletePending = true;
            }

            if (cur.iTermKey == null || "Y".equals(cur.iCounted)) {
                this.registrationTerms.add(this.active);
            } else {
                // Load the term in which the incomplete was earned
                final TermRec incTerm = cache.getSystemData().getTerm(cur.iTermKey);

                if (incTerm == null) {
                    Log.warning("Unable to query I term - using current");
                    this.owner.setError("Unable to query I term - using current");
                    this.registrationTerms.add(this.active);
                } else {
                    this.registrationTerms.add(incTerm);
                }
            }

            if ("Y".equals(cur.iInProgress)) {
                this.owner.courseData.addCourse(cache, cur.course, cur.sect, cur.iTermKey);
            } else {
                this.owner.courseData.addCourse(cache, cur.course, cur.sect, cur.termKey);
            }
        }

        // Finally, go through all the student's current and past registrations, and see which courses they have access
        // to via a purchased e-text. For each such course, load the course information.
        final String[] courseIds = this.owner.studentData.getEtextCourseIds();
        outer:
        for (final String courseId : courseIds) {

            // Add current registrations first, so we get the current section number
            for (final RawStcourse test : this.registrations) {
                if (courseId.equals(test.course)) {
                    this.owner.courseData.addCourse(cache, test.course, test.sect, test.termKey);
                    continue outer;
                }
            }

            // If course was not found in current registrations, but student has an e-text for
            // it, they can still access in practice mode if they have taken the course in the
            // past. In this case, use their past registration's section number.

            for (final RawStcourse test : this.allCompletedCourses) {
                if (courseId.equals(test.course)) {
                    this.owner.courseData.addCourse(cache, test.course, test.sect, test.termKey);
                    break;
                }
            }
        }

        return success;
    }

    /**
     * Scans the special student categories and installs synthetic student course records to grant course access to
     * certain special categories.
     *
     * @param cache   the data cache
     * @param session the login session
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean buildSpecialStuRegs(final Cache cache, final ImmutableSessionInfo session)
            throws SQLException {

        boolean addSpecials = false;

        final SystemData systemData = cache.getSystemData();

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            addSpecials = true;
        } else {
            final List<RawSpecialStus> specials = this.owner.studentData.getSpecialStudents();

            for (final RawSpecialStus spec : specials) {
                final String type = spec.stuType;

                if ("ADMIN".equals(type) || "TUTOR".equals(type) || "M384".equals(type)) {
                    addSpecials = true;
                    break;
                }
            }
        }

        if (addSpecials) {
            final Collection<String> courseIds = new ArrayList<>(7);
            courseIds.add(RawRecordConstants.M117);
            courseIds.add(RawRecordConstants.M118);
            courseIds.add(RawRecordConstants.M124);
            courseIds.add(RawRecordConstants.M125);
            courseIds.add(RawRecordConstants.M126);
            courseIds.add(RawRecordConstants.MATH125);
            courseIds.add(RawRecordConstants.MATH126);

            int paceOrder = 1;
            outer:
            for (final String courseId : courseIds) {
                // If there's already a registration for the course, skip
                for (final RawStcourse registration : this.registrations) {
                    if (courseId.equals(registration.course)) {
                        Log.warning("Registration already exists for ", courseId);
                        continue outer;
                    }
                }

                // Make a synthetic registration for it - try section 001 first, section 401 next, then take any
                // section we can get
                RawCsection sect = cache.getSystemData().getCourseSection(courseId, "001", this.active.term);
                if (sect == null) {
                    sect = cache.getSystemData().getCourseSection(courseId, "401", this.active.term);
                    if (sect == null) {
                        final List<RawCsection> all = systemData.getCourseSectionsByCourse(courseId,
                                this.active.term);
                        if (!all.isEmpty()) {
                            sect = all.getFirst();
                        }
                    }
                }

                if (sect == null) {
                    Log.warning("Unable to find section of ", courseId, " for SpecialStus access");
                } else {
                    SiteDataCfgCourse cfgCourse = this.owner.courseData.getCourse(courseId, sect.sect);

                    if (cfgCourse == null) {
                        cfgCourse = this.owner.courseData.addCourse(cache, courseId, sect.sect, this.active.term);
                    }

                    if (cfgCourse == null) {
                        Log.warning("Unable to find course configuration for ", courseId, " for SpecialStus access");
                    } else if (cfgCourse.courseSection == null) {
                        Log.warning("Unable to find course section for ", courseId, " for SpecialStus access");
                    } else {
                        final String type = cfgCourse.courseSection.instrnType;

                        if ("RI".equals(type) || "CE".equals(type)) {

                            this.registrations.add(makeSyntheticReg(courseId, sect.sect, paceOrder));

                            ++paceOrder;
                            this.registrationTerms.add(this.active);

                            this.owner.courseData.addCourse(cache, courseId, sect.sect, this.active.term);
                        } else {
                            Log.warning("Course section for ", courseId, " for SpecialStus access is type ", type);
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Creates a synthetic student course record for a tutorial, course accessed as a visiting student, or course
     * accessed via a special category.
     *
     * @param courseId   the courseId
     * @param sectionNum the section number
     * @param paceOrder  the pace order
     * @return the generated synthetic {@code StudentCourse} record
     */
    private RawStcourse makeSyntheticReg(final String courseId, final String sectionNum, final int paceOrder) {

        final RawStcourse result =
                new RawStcourse(this.active.term, this.owner.studentData.getStudent().stuId, courseId, sectionNum,
                        Integer.valueOf(paceOrder), "Y", null, "N", null, null, "Y", "N", "N", "N", null, null, null,
                        null, "N", null, null, null, null, "RI", null, null, null, null);

        result.synthetic = true;

        return result;
    }

    /**
     * Populates course prerequisite records for all student registrations.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadCoursePrereqs(final Cache cache) throws SQLException {

        final boolean success = true;

        final SystemData systemData = cache.getSystemData();

        for (final RawStcourse registration : this.registrations) {
            this.prereqs.add(systemData.getPrerequisitesByCourse(registration.course));
        }

        return success;
    }

    /**
     * Tests the prerequisite on a course registration which was not already flagged as having its prerequisites
     * satisfied.
     *
     * @param cache the data cache
     * @param stuId the student ID
     * @return {@code true} if process succeeded; {@code false} on error
     * @throws SQLException if an error occurs reading data
     */
    private boolean checkPrerequisites(final Cache cache, final String stuId) throws SQLException {

        final boolean success = true;

        final PrerequisiteLogic logic = new PrerequisiteLogic(cache, stuId);

        for (final RawStcourse stcourse : this.registrations) {
            if ("Y".equals(stcourse.prereqSatis)) {
                continue;
            }

            Log.info("Checking prereqs in ", stcourse.course);
            if (logic.hasSatisfiedPrerequisitesFor(stcourse.course)) {
                Log.info("Marking prereq as cleared in ", stcourse.course);

                if (RawStcourseLogic.updatePrereqSatisfied(cache, stcourse.stuId, stcourse.course,
                        stcourse.sect, stcourse.termKey, "Y")) {
                    stcourse.prereqSatis = "Y";
                }
            } else {
                RawStcourseLogic.testProvisionalPrereqSatisfied(stcourse);
            }
        }

        return success;
    }

    /**
     * Scans the list of student registrations, and uses course section data to determine which are included in the
     * student's pace, compiling a separate list of pace registrations.
     *
     * @param cache the data
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadPaceRegistrations(final Cache cache) throws SQLException {

        final int count = this.registrations.size();
        final List<RawStcourse> paceReg = new ArrayList<>(count);

        for (int i = 0; i < count; ++i) {
            final RawStcourse stcourse = this.registrations.get(i);

            if (stcourse.synthetic) {
                continue;
            }

            // Call this now, so we load the course data, even if it's an incomplete

            final SiteDataCfgCourse coursedata;
            if ("Y".equals(stcourse.iInProgress) && stcourse.iTermKey != null) {
                coursedata = this.owner.courseData.getCourse(cache, stcourse.course, stcourse.sect, stcourse.iTermKey);
            } else {
                coursedata = this.owner.courseData.getCourse(cache, stcourse.course, stcourse.sect, this.active.term);
            }

            // Placement-credit and forfeit registrations are not included in pace
            if ("G".equals(stcourse.openStatus) || (stcourse.iDeadlineDt != null && "N".equals(stcourse.iCounted))) {
                // Don't alter the existing pace order, but don't count toward pace
                continue;
            }

            final RawPacingStructure ruleSet = coursedata.pacingStructure;

            if (ruleSet != null && "pace".equals(ruleSet.scheduleSource)) {
                paceReg.add(stcourse);
            } else if (stcourse.paceOrder != null) {
                // Does not count toward pace, so make sure it has no pace order
                Log.info("loadPaceRegistrations setting " + stcourse.course
                         + " pace order to null for " + stcourse.stuId);
                updatePaceOrder(cache, stcourse, null);
                this.registrations.set(i, stcourse);
            }
        }

        this.paceRegistrations = paceReg;

        return true;
    }

    /**
     * Scans the registrations that fall within this context, and see if they have a consistent rule set. If so, set
     * that as the student rule set. If not, store a default rule set.
     *
     * @param cache the data cache
     * @return {@code true} if process succeeded; {@code false} on error
     * @throws SQLException if an error occurs reading data
     */
    private boolean determineStudentRuleSet(final Cache cache) throws SQLException {

        final Map<String, RawPacingStructure> ruleSets = new TreeMap<>();
        boolean success = true;

        for (final RawStcourse stcourse : this.registrations) {
            // Placement-credit, forfeit registrations, and incompletes are not considered
            if (stcourse.iDeadlineDt != null
                // FIXME remove once getRegistrationData omits AP credit records
                || "OT".equals(stcourse.instrnType) || "G".equals(stcourse.openStatus)) {
                continue;
            }

            final SiteDataCfgCourse coursedata;

            if ("Y".equals(stcourse.iInProgress) && stcourse.iTermKey != null) {
                coursedata = this.owner.courseData.getCourse(cache, stcourse.course, stcourse.sect, stcourse.iTermKey);
            } else {
                coursedata = this.owner.courseData.getCourse(cache, stcourse.course, stcourse.sect, this.active.term);
            }

            final RawPacingStructure ruleSet = coursedata.pacingStructure;
            if (ruleSet != null) {
                ruleSets.put(ruleSet.pacingStructure, ruleSet);
            }
        }

        if (ruleSets.size() == 1) {
            final List<RawPacingStructure> set = new ArrayList<>(ruleSets.values());
            this.owner.studentData.setStudentPacingStructure(set.getFirst());

            final RawStudent student = this.owner.studentData.getStudent();
            if (student.pacingStructure == null) {
                // Update the rule set ID in the database
                RawStudentLogic.updatePacingStructure(cache, student.stuId, set.getFirst().pacingStructure);
            }
        }

        if (this.owner.studentData.getStudentPacingStructure() == null) {
            final SystemData systemData = cache.getSystemData();

            // Query for the default rule set
            final RawPacingStructure record =
                    systemData.getPacingStructure(RawPacingStructure.DEF_PACING_STRUCTURE, this.active.term);

            if (record == null) {
                this.owner.setError("Unable to query for default pacing structure "
                                    + RawPacingStructure.DEF_PACING_STRUCTURE);
                success = false;
            } else {
                this.owner.studentData.setStudentPacingStructure(record);
            }
        }

        return success;
    }

    /**
     * Determines the student's pace track and updates/verifies the student term record.
     *
     * @param cache the data cache
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean determinePaceTrack(final Cache cache) throws SQLException {

        final boolean success;

        final int pace = PaceTrackLogic.determinePace(this.paceRegistrations);
        String track = PaceTrackLogic.determinePaceTrack(this.paceRegistrations);

        // If no rules matched, assign the default track based on the student's rule set
        if (track == null) {
            final RawPacingStructure pacingstructure =
                    this.owner.studentData.getStudentPacingStructure();
            track = pacingstructure == null ? "A" : pacingstructure.defPaceTrack;
        }

        if (pace == 0) {
            success = true;
        } else {
            // Determine "first" course
            Collections.sort(this.paceRegistrations);
            final String first = PaceTrackLogic.determineFirstCourse(this.paceRegistrations);

            // If there is a student term record, validate it; if not, create it
            final String key = this.active.term.shortString;
            final RawStterm stTerm = this.owner.milestoneData.getStudentTerm(key);

            if (stTerm == null) {
                // No record exists - create one and store in database (if we have data to
                // insert)

                if (pace > 0) {
                    final TermRec term = this.active;
                    final RawStterm model =
                            new RawStterm(term.term, this.owner.studentData.getStudent().stuId,
                                    Integer.valueOf(pace), track, first, null, null, null);

                    RawSttermLogic.insert(cache, model);
                    // Now, refresh the milestone object's data, so it's current
                    this.owner.milestoneData.preload(cache);
                }
                success = true;
            } else // Record exists - verify its contents, and update if needed
                if (Integer.valueOf(pace).equals(stTerm.pace) && track.equals(stTerm.paceTrack)
                    && first.equals(stTerm.firstCourse)) {

                    // All fields match - no change needed
                    success = true;
                } else {
                    // Need to update the record - if there is a "!" on pace track in the
                    // record, do not update that
                    String t = stTerm.paceTrack;
                    if (t == null || !(!t.isEmpty() && t.charAt(t.length() - 1) == '!')) {
                        t = track;
                    }

                    success = RawSttermLogic.updatePaceTrackFirstCourse(cache, stTerm.stuId,
                            stTerm.termKey, pace, t, first);
                }
        }

        return success;
    }
}
