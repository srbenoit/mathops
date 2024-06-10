package dev.mathops.session.sitelogic.data;

import dev.mathops.db.logic.StudentData;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A container for the student-oriented data relating to a {@code SiteData} object.
 */
public final class SiteDataStudent {

    /** A zero-length array used when creating other arrays. */
    private static final String[] ZERO_LEN_STRING_ARR = new String[0];

    /** The data object that owns this object. */
    private final SiteData owner;

    /** The student record. */
    private RawStudent student;

    /** The student rule set record. */
    private RawPacingStructure studentPacingStructure;

    /** The student e-texts. */
    private List<RawStetext> studentETexts;

    /** The set of course IDs to which the student's active e-texts grant access. */
    private final Set<String> etextCourseIds;

    /** The special student categories that are currently active. */
    private List<RawSpecialStus> specialStudents;

    /** The student hold records. */
    private List<RawAdminHold> studentHolds;

    /** The student placement attempt records. */
    private List<RawStmpe> studentPlacement;

    /** The student ELM attempt records. */
    private List<RawStexam> studentElm;

    /** The student placement credit records. */
    private List<RawMpeCredit> studentPlacementCredit;

    /** The student placement credit records. */
    private List<RawStcourse> studentOTCredit;

    /**
     * Constructs a new {@code SiteDataStudent}.
     *
     * @param theOwner the data object that owns this object
     */
    SiteDataStudent(final SiteData theOwner) {

        this.owner = theOwner;

        this.student = null;
        this.studentPacingStructure = null;
        this.studentETexts = null;
        this.etextCourseIds = new TreeSet<>();
        this.specialStudents = null;
        this.studentHolds = null;
        this.studentPlacement = null;
        this.studentElm = null;
        this.studentPlacementCredit = null;
        this.studentOTCredit = null;
    }

    /**
     * Gets the student record.
     *
     * @return the student record
     */
    public RawStudent getStudent() {

        return this.student;
    }

    /**
     * Gets the student pacing structure.
     *
     * @return the student pacing structure
     */
    RawPacingStructure getStudentPacingStructure() {

        return this.studentPacingStructure;
    }

    /**
     * Sets the student pacing structure (called after the student registrations have been queried and verified for
     * consistency in their pacing structures).
     *
     * @param thePacingStructure the new student pacing structure
     */
    void setStudentPacingStructure(final RawPacingStructure thePacingStructure) {

        this.studentPacingStructure = thePacingStructure;
    }

    /**
     * Gets a list of all course IDs that the student has access to because they own an active e-text.
     *
     * @return the array of course IDs
     */
    String[] getEtextCourseIds() {

        return this.etextCourseIds.toArray(ZERO_LEN_STRING_ARR);
    }

    /**
     * Gets the special student categories.
     *
     * @return the special student categories
     */
    List<RawSpecialStus> getSpecialStudents() {

        return new ArrayList<>(this.specialStudents);
    }

    /**
     * Tests whether the student has any of a list of special types assigned and active at the current date/time.
     *
     * @param now   the current date
     * @param types a list of special types for which to test
     * @return {@code true} if the student is part of at least one of the listed types at the specified date/time
     */
    public boolean isSpecialType(final ZonedDateTime now, final String... types) {

        // TODO: Move this into logic

        boolean result = false;

        final LocalDate today = now.toLocalDate();

        if (this.specialStudents != null) {
            outer:
            for (final RawSpecialStus special : this.specialStudents) {
                final String type = special.stuType;

                for (final String s : types) {
                    if (type.equals(s)) {

                        final LocalDate start = special.startDt;
                        final LocalDate end = special.endDt;

                        if ((start == null || !today.isBefore(start)) && (end == null || !today.isAfter(end))) {
                            result = true;
                            break outer;
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Gets the student hold records.
     *
     * @return the student hold records
     */
    public List<RawAdminHold> getStudentHolds() {

        return new ArrayList<>(this.studentHolds);
    }

    /**
     * Gets the total number of lockout holds on the student's account.
     *
     * @return the number of lockout holds
     */
    public int getNumLockouts() {

        // TODO: Move this into logic.

        int count = 0;

        if (this.studentHolds != null) {
            for (final RawAdminHold hold : this.studentHolds) {
                if ("30".equals(hold.holdId)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * Gets the student placement attempt records.
     *
     * @return the student placement attempt records
     */
    public List<RawStmpe> getStudentPlacementAttempts() {

        return new ArrayList<>(this.studentPlacement);
    }

    /**
     * Gets the student ELM exam attempt records.
     *
     * @return the student placement attempt records
     */
    public List<RawStexam> getStudentElmAttempts() {

        return new ArrayList<>(this.studentElm);
    }

    /**
     * Gets the student placement credit records.
     *
     * @return the student placement credit records
     */
    public List<RawMpeCredit> getStudentPlacementCredit() {

        return new ArrayList<>(this.studentPlacementCredit);
    }

    /**
     * Gets the student OT credit records.
     *
     * @return the student OT credit records
     */
    public List<RawStcourse> getStudentOTCredit() {

        return new ArrayList<>(this.studentOTCredit);
    }

    /**
     * Tests whether a student has a particular hold.
     *
     * @param holdId the hold ID for which to test
     * @return {@code true} if the student has the requested hold on their account
     */
    public boolean hasHold(final String holdId) {

        // TODO: Move this into logic.

        boolean hasHold = false;

        if (this.studentHolds != null && holdId != null) {
            for (final RawAdminHold hold : this.studentHolds) {
                if (holdId.equals(hold.holdId)) {
                    hasHold = true;
                    break;
                }
            }
        }

        return hasHold;
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     * <p>
     * At the time this method is called; the {@code SiteData} object will have loaded the active term, all calendar
     * records, all pace track rules, and the {@code SiteDataContext} object.
     *
     * @param studentData the student data object
     * @throws SQLException if an error occurs reading data
     */
    void loadData(final StudentData studentData) throws SQLException {

        final LocalDate today = this.owner.now.toLocalDate();

        this.studentETexts = studentData.getStudentETexts();
        final SystemData systemData = studentData.getSystemData();

        for (final RawStetext stetext : this.studentETexts) {
            if (stetext.refundDt != null) {
                continue;
            }
            final LocalDate expiry = stetext.expirationDt == null ? null : stetext.expirationDt;

            if (expiry == null || !today.isAfter(expiry)) {
                final String etextId = stetext.etextId;

                final List<RawEtextCourse> courses = systemData.getETextCoursesByETextId(etextId);
                for (final RawEtextCourse crs : courses) {
                    this.etextCourseIds.add(crs.course);
                }
            }
        }

        this.specialStudents = studentData.getActiveSpecialCategories(today);
        this.studentHolds = studentData.getHolds();
        this.studentPlacement = studentData.getLegalPlacementAttempts();
        this.studentElm = studentData.getStudentExamsByCourseType(RawRecordConstants.M100T, false);
        this.studentPlacementCredit = studentData.getPlacementCredit();
        this.studentOTCredit = studentData.getCreditByExam();
    }

    /**
     * Loads all e-texts owned by the student.
     *
     * @param studentData the student data object
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudentETexts(final StudentData studentData) throws SQLException {


        return true;
    }
}
