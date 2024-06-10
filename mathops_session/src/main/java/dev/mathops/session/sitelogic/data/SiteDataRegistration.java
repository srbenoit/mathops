package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
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
    private TermRec active;

    /** All completed student course records, including past terms. */
    private List<RawStcourse> allCompletedCourses;

    /** All student course records in the current term. */
    private List<RawStcourse> registrations;

    /** All student course records in the current term. */
    private List<TermRec> registrationTerms;

    /** The student course records in the current term that count towards pace . */
    private List<RawStcourse> paceRegistrations;

    /** The student transfer credit records (CTransferCredit). */
    private List<RawFfrTrns> transferCredit;

    /** The list of prerequisites for each course (course ID). */
    private List<List<String>> prereqs;

    /** A flag indicating student has at least one incomplete that is not completed. */
    private boolean nonPacedIncompletePending;

    /**
     * A flag indicating student has fatal holds, but they are all type "30" (locked out), meaning the student may not
     * work in paced courses.
     */
    private boolean lockedOut;

    /**
     * Constructs a new {@code SiteDataRegistration}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataRegistration(final SiteData theOwner) {

        this.owner = theOwner;

        this.active = null;
        this.allCompletedCourses = null;
        this.registrations = null;
        this.registrationTerms = null;
        this.paceRegistrations = null;
        this.transferCredit = null;
        this.prereqs = null;
        this.lockedOut = true;
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
     * Gets the term records corresponding to the student registration records for the current term (including advance
     * placement and forfeit courses, but excluding drop and withdrawal courses).
     *
     * @return the registration records (CStudentCourse)
     */
    public List<TermRec> getRegistrationTerms() {

        return new ArrayList<>(this.registrationTerms);
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
     * @param studentData the student data object
     * @param session     the login session
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final StudentData studentData, final ImmutableSessionInfo session) throws SQLException {

        final SystemData sysData = studentData.getSystemData();

        this.active = sysData.getActiveTerm();

        final String studentId = this.owner.siteStudentData.getStudent().stuId;

        final boolean b1 = loadRegistrations(studentData);

        this.transferCredit = studentData.getTransferCredit();

        final boolean b2 = buildSpecialStuRegs(sysData, session);

        boolean success = b1 && b2;

        if (success) {
            final int numCourses = getRegistrations().size();

            // Allocate arrays
            this.prereqs = new ArrayList<>(numCourses);

            final boolean b5 = loadCoursePrereqs(sysData);
            final boolean b6 = checkPrerequisites(studentData);
            final boolean b7 = loadPaceRegistrations(studentData);
            final boolean b8 = determineStudentRuleSet(studentData);
            final boolean b9 = determinePaceTrack(studentData);

            success = b5 && b6 && b7 && b8 && b9;

            // Determine whether the student may work in paced and non-paced, only non-paced, or
            // neither based on hold status
            final SiteDataStudent stuData = this.owner.siteStudentData;

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
     * @param studentData  the student data object
     * @param reg          the registration record
     * @param newPaceOrder the new pace order
     * @throws SQLException if there is an error accessing the database
     */
    public static void updatePaceOrder(final StudentData studentData, final RawStcourse reg,
                                       final Integer newPaceOrder) throws SQLException {

        final Cache cache = studentData.getCache();

        if (RawStcourseLogic.updatePaceOrder(cache, reg.stuId, reg.course, reg.sect, reg.termKey, newPaceOrder)) {
            studentData.forgetRegistrations();
            reg.paceOrder = newPaceOrder;
        }
    }

    /**
     * Loads all completed course records (regardless of term) for a student, as well as all current term records that
     * have not been dropped. This will include "ignored" records.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadRegistrations(final StudentData studentData) throws SQLException {

        final boolean success = true;

        // Load all courses marked as completed (for all terms), that are not credit by exam, used for testing
        // prerequisites (we store credit by exam elsewhere)
        final List<RawStcourse> allPastAndCurrent = studentData.getNonDroppedRegistrations();

        this.allCompletedCourses = new ArrayList<>(allPastAndCurrent.size());
        for (final RawStcourse past : allPastAndCurrent) {
            if ("OT".equals(past.instrnType)) {
                continue;
            }
            if ("Y".equals(past.completed)) {
                this.allCompletedCourses.add(past);
            }
        }

        // Now load current term registrations, but discard any "dropped" (keep those forfeit)

        final List<RawStcourse> curTermReg = studentData.getActiveRegistrations(this.active.term, true);

        this.registrations = new ArrayList<>(curTermReg.size());
        this.registrationTerms = new ArrayList<>(curTermReg.size());

        final SystemData sysData = studentData.getSystemData();

        for (final RawStcourse cur : curTermReg) {

            // Ignore courses with an incomplete deadline date but which are not incompletes in progress and are not
            // counted in pace

            if (cur.iDeadlineDt != null && "N".equals(cur.iInProgress) && !"Y".equals(cur.iCounted)) {
                continue;
            }

            this.registrations.add(cur);
//            Log.info("Found registration in ", cur.course);

            // If we find a course that has an incomplete deadline date, is not completed, and is not counted in pace,
            // flag that since the student will have to work on those courses before starting any paced courses.
            if ("Y".equals(cur.iInProgress) && "N".equals(cur.iCounted) && "N".equals(cur.completed)) {
                this.nonPacedIncompletePending = true;
            }

            if (cur.iTermKey == null) {
                this.registrationTerms.add(this.active);
            } else {

                // Load the term in which the incomplete was earned
                final TermRec incTerm = sysData.getTerm(cur.iTermKey);

                if (incTerm == null) {
                    Log.warning("Unable to query I term - using current");
                    this.owner.setError("Unable to query I term - using current");
                    this.registrationTerms.add(this.active);
                } else {
                    this.registrationTerms.add(incTerm);
                }
            }

            if ("Y".equals(cur.iInProgress)) {
                this.owner.siteCourseData.addCourse(sysData, cur.course, cur.sect, cur.iTermKey);
            } else {
                this.owner.siteCourseData.addCourse(sysData, cur.course, cur.sect, cur.termKey);
            }
        }

        // Finally, go through all the student's current and past registrations, and see which courses they have access
        // to via a purchased e-text. For each such course, load the course information.
        final String[] courseIds = this.owner.siteStudentData.getEtextCourseIds();
        outer:
        for (final String courseId : courseIds) {

            // Add current registrations first, so we get the current section number
            for (final RawStcourse test : this.registrations) {
                if (courseId.equals(test.course)) {
                    this.owner.siteCourseData.addCourse(sysData, test.course, test.sect, test.termKey);
                    continue outer;
                }
            }

            // If course was not found in current registrations, but student has an e-text for
            // it, they can still access in practice mode if they have taken the course in the
            // past. In this case, use their past registration's section number.

            // HACK: If section is "401", and the current term is not "SM", then change section
            // to "801", and if section is 00*, change to 001
//            final boolean isNotSummer = this.active.term.name != ETermName.SUMMER;

            for (final RawStcourse test : this.allCompletedCourses) {
                if (courseId.equals(test.course)) {
//                    String sect = test.sect;
//
//                    if ("002".equals(sect) || "003".equals(sect) || "004".equals(sect)) {
//                        sect = "001";
//                    } else if ((isNotSummer && "401".equals(sect)) || "809".equals(sect)) {
//                        sect = "801";
//                    }

                    this.owner.siteCourseData.addCourse(sysData, test.course, test.sect, test.termKey);
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
     * @param systemData the system data object
     * @param session    the login session
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean buildSpecialStuRegs(final SystemData systemData, final ImmutableSessionInfo session)
            throws SQLException {

        boolean addSpecials = false;

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            addSpecials = true;
        } else {
            final List<RawSpecialStus> specials = this.owner.siteStudentData.getSpecialStudents();

            for (final RawSpecialStus spec : specials) {
                final String type = spec.stuType;

                if ("ADMIN".equals(type) || "TUTOR".equals(type) || "M384".equals(type)) {
                    addSpecials = true;
                    break;
                }
            }
        }

        final Collection<String> courseIds = new ArrayList<>(5);
        if (addSpecials) {
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
                        continue outer;
                    }
                }

                // Make a synthetic registration for it - try section 001 first, section 401 next, then take any
                // section we can get
                RawCsection sect = systemData.getCourseSection(courseId, "001", this.active.term);
                if (sect == null) {
                    sect = systemData.getCourseSection(courseId, "401", this.active.term);
                    if (sect == null) {
                        final List<RawCsection> all = systemData.getCourseSectionsByCourse(courseId, this.active.term);
                        if (all != null && !all.isEmpty()) {
                            sect = all.getFirst();
                        }
                    }
                }

                if (sect != null) {
                    SiteDataCfgCourse cfgCourse = this.owner.siteCourseData.getCourse(courseId, sect.sect);

                    if (cfgCourse == null) {
                        cfgCourse = this.owner.siteCourseData.addCourse(systemData, courseId, sect.sect,
                                this.active.term);
                    }

                    if (cfgCourse != null && cfgCourse.courseSection != null) {
                        final String type = cfgCourse.courseSection.instrnType;

                        if ("RI".equals(type) || "CE".equals(type)) {

                            this.registrations.add(makeSyntheticReg(courseId, sect.sect, paceOrder));
                            ++paceOrder;
                            this.registrationTerms.add(this.active);

                            this.owner.siteCourseData.addCourse(systemData, courseId, sect.sect, this.active.term);
                            break;
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
                new RawStcourse(this.active.term, this.owner.siteStudentData.getStudent().stuId, courseId, sectionNum,
                        Integer.valueOf(paceOrder), "Y", null, "N", null, null, "Y", "N", "N", "N", null, null, null,
                        null, "N", null, null, null, null, "RI", null, null, null, null);

        result.synthetic = true;

        return result;
    }

    /**
     * Populates course prerequisite records for all student registrations.
     *
     * @param systemData the system data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadCoursePrereqs(final SystemData systemData) throws SQLException {

        final boolean success = true;

        for (final RawStcourse registration : this.registrations) {
            this.prereqs.add(systemData.getPrerequisitesByCourse(registration.course));
        }

        return success;
    }

    /**
     * Tests the prerequisite on a course registration which was not already flagged as having its prerequisites
     * satisfied.
     *
     * @param studentData the student data object
     * @return {@code true} if process succeeded; {@code false} on error
     * @throws SQLException if an error occurs reading data
     */
    private boolean checkPrerequisites(final StudentData studentData) throws SQLException {

        final boolean success = true;

        final int count = this.registrations.size();
        for (int i = 0; i < count; ++i) {
            final RawStcourse stcourse = this.registrations.get(i);

            if ("Y".equals(stcourse.prereqSatis)) {
                continue;
            }

            final List<String> coursePrereqs = this.prereqs.get(i);

            boolean prereq = coursePrereqs.isEmpty();

            if (!prereq) {
                // See if student has completed any course prerequisites
                outer:
                for (final String coursePrereq : coursePrereqs) {
                    for (final RawStcourse comp : this.allCompletedCourses) {
                        if (coursePrereq.equals(comp.course)) {
                            prereq = true;
                            break outer;
                        }
                    }
                }
            }

            if (!prereq) {
                // See if there is a placement result satisfying prerequisite
                final List<RawMpeCredit> placeCred = this.owner.siteStudentData.getStudentPlacementCredit();
                outer:
                for (final String coursePrereq : coursePrereqs) {
                    for (final RawMpeCredit cred : placeCred) {
                        if (coursePrereq.equals(cred.course)) {
                            prereq = true;
                            break outer;
                        }
                    }
                }
            }

            if (!prereq) {
                // See if there are transfer credits satisfying the prerequisite
                outer:
                for (final String coursePrereq : coursePrereqs) {
                    for (final RawFfrTrns cred : this.transferCredit) {
                        if (coursePrereq.equals(cred.course)) {
                            prereq = true;
                            break outer;
                        }
                    }
                }
            }

            if (prereq) {
                final Cache cache = studentData.getCache();
                if (RawStcourseLogic.updatePrereqSatisfied(cache, stcourse.stuId, stcourse.course,
                        stcourse.sect, stcourse.termKey, "Y")) {
                    stcourse.prereqSatis = "Y";
                }

                this.registrations.set(i, stcourse);
            } else if (RawStcourseLogic.testProvisionalPrereqSatisfied(stcourse)) {
                this.registrations.set(i, stcourse);
            }
        }

        return success;
    }

    /**
     * Scans the list of student registrations, and uses course section data to determine which are included in the
     * student's pace, compiling a separate list of pace registrations.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadPaceRegistrations(final StudentData studentData) throws SQLException {

        final int count = this.registrations.size();
        final List<RawStcourse> paceReg = new ArrayList<>(count);
        final boolean success = true;

        for (int i = 0; i < count; ++i) {
            final RawStcourse stcourse = this.registrations.get(i);

            if (stcourse.synthetic) {
                continue;
            }

            // Call this now, so we load the course data, even if it's an incomplete
            final SystemData sysData = studentData.getSystemData();

            final SiteDataCfgCourse coursedata;
            if ("Y".equals(stcourse.iInProgress) && stcourse.iTermKey != null) {
                coursedata = this.owner.siteCourseData.getCourse(sysData, stcourse.course, stcourse.sect,
                        stcourse.iTermKey);
            } else {
                coursedata = this.owner.siteCourseData.getCourse(sysData, stcourse.course, stcourse.sect,
                        this.active.term);
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
                Log.info("loadPaceRegistrations setting ", stcourse.course, " pace order to null for ", stcourse.stuId);
                updatePaceOrder(studentData, stcourse, null);
                this.registrations.set(i, stcourse);
            }
        }

        if (success) {
            this.paceRegistrations = paceReg;
        }

        return success;
    }

    /**
     * Scans the registrations that fall within this context, and see if they have a consistent rule set. If so, set
     * that as the student rule set. If not, store a default rule set.
     *
     * @param studentData the student data object
     * @return {@code true} if process succeeded; {@code false} on error
     * @throws SQLException if an error occurs reading data
     */
    private boolean determineStudentRuleSet(final StudentData studentData) throws SQLException {

        final Map<String, RawPacingStructure> ruleSets = new TreeMap<>();
        boolean success = true;

        final SystemData sysData = studentData.getSystemData();

        for (final RawStcourse stcourse : this.registrations) {
            // Placement-credit, forfeit registrations, and incompletes are not considered
            if (stcourse.iDeadlineDt != null
                    // FIXME remove once getRegistrationData omits AP credit records
                    || "OT".equals(stcourse.instrnType) || "G".equals(stcourse.openStatus)) {
                continue;
            }

            final SiteDataCfgCourse coursedata;

            if ("Y".equals(stcourse.iInProgress) && stcourse.iTermKey != null) {
                coursedata = this.owner.siteCourseData.getCourse(sysData, stcourse.course, stcourse.sect,
                        stcourse.iTermKey);
            } else {
                coursedata = this.owner.siteCourseData.getCourse(sysData, stcourse.course, stcourse.sect,
                        this.active.term);
            }

            final RawPacingStructure ruleSet = coursedata.pacingStructure;
            if (ruleSet != null) {
                ruleSets.put(ruleSet.pacingStructure, ruleSet);
            }
        }

        if (ruleSets.size() == 1) {
            final List<RawPacingStructure> set = new ArrayList<>(ruleSets.values());
            final RawPacingStructure newPacing = set.get(0);

            this.owner.siteStudentData.setStudentPacingStructure(newPacing);

            final RawStudent student = this.owner.siteStudentData.getStudent();
            if (student.pacingStructure == null) {
                // Update the rule set ID in the database
                final Cache cache = studentData.getCache();
                RawStudentLogic.updatePacingStructure(cache, student.stuId, newPacing.pacingStructure);
                student.pacingStructure = newPacing.pacingStructure;
            }
        }

        if (this.owner.siteStudentData.getStudentPacingStructure() == null) {
            final TermRec activeTerm = sysData.getActiveTerm();

            // Query for the default rule set
            final RawPacingStructure record = sysData.getPacingStructure(RawPacingStructure.DEF_PACING_STRUCTURE,
                activeTerm.term);

            if (record == null) {
                this.owner.setError("Unable to query for default rule set " + RawPacingStructure.DEF_PACING_STRUCTURE);
                success = false;
            } else {
                this.owner.siteStudentData.setStudentPacingStructure(record);
            }
        }

        return success;
    }

    /**
     * Determines the student's pace track and updates/verifies the student term record.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean determinePaceTrack(final StudentData studentData) throws SQLException {

        final boolean success;

        final int pace = PaceTrackLogic.determinePace(this.paceRegistrations);
        String track = PaceTrackLogic.determinePaceTrack(this.paceRegistrations);

        // If no rules matched, assign the default track based on the student's rule set
        if (track == null) {
            final RawPacingStructure pacingstructure = this.owner.siteStudentData.getStudentPacingStructure();
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
            final RawStterm stTerm = this.owner.siteMilestoneData.getStudentTerm(key);

            if (stTerm == null) {
                // No record exists - create one and store in database (if we have data to insert)

                if (pace > 0) {
                    final TermRec term = this.active;
                    final RawStterm model =
                            new RawStterm(term.term, this.owner.siteStudentData.getStudent().stuId,
                                    Integer.valueOf(pace), track, first, null, null, null);

                    final Cache cache = studentData.getCache();
                    RawSttermLogic.INSTANCE.insert(cache, model);
                    studentData.forgetStudentTerm();

                    // Now, refresh the milestone object's data, so it's current
                    this.owner.siteMilestoneData.preload(studentData);
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

                    final Cache cache = studentData.getCache();
                    success = RawSttermLogic.updatePaceTrackFirstCourse(cache, stTerm.stuId, stTerm.termKey, pace, t,
                            first);
                    studentData.forgetStudentTerm();
                }
        }

        return success;
    }
}
