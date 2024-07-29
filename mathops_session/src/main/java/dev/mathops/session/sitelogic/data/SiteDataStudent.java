package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
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
import dev.mathops.session.ImmutableSessionInfo;

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

//    /**
//     * Gets the student e-texts.
//     *
//     * @return the student e-texts
//     */
//    public List<RawStetext> getStudentETexts() {
//
//        return new ArrayList<>(this.studentETexts);
//    }

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
     * @param cache          the data cache
     * @param theSessionInfo the session info
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    boolean loadData(final Cache cache, final ImmutableSessionInfo theSessionInfo) throws SQLException {

        final String studentId = theSessionInfo.getEffectiveUserId();

        return loadStudent(cache, studentId)
                && loadStudentETexts(cache, studentId)
                && loadSpecialStudent(cache, studentId)
                && loadStudentHolds(cache, studentId)
                && loadStudentPlacement(cache, studentId)
                && loadStudentElm(cache, studentId)
                && loadStudentPlacementCredit(cache, studentId)
                && loadStudentOTCredit(cache, studentId);

        // TODO: Get score coupons (or move that to the scores object)?
        // TODO: Make a student preferences object?
        // TODO: Make a student messages object?
    }

    /**
     * Loads the student record.
     *
     * @param cache     the data cache
     * @param studentId the student ID
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudent(final Cache cache, final String studentId) throws SQLException {

        final boolean success;

        // Test for special IDs that create synthetic student records
        this.student = makeSyntheticStudent(studentId);

        if (this.student == null) {
            this.student = RawStudentLogic.query(cache, studentId, false);

            if (this.student == null) {
                this.owner.setError("Unable to query student record");
                success = false;
            } else {
                success = true;
            }
        } else {
            success = true;
        }

        return success;
    }

    /**
     * Constructs a synthetic student record for a set of "special" recognized student IDs.
     *
     * @param studentId the student ID
     * @return the synthetic record (defaults to a "Guest" record if the student ID is not a recognized special ID)
     */
    private static RawStudent makeSyntheticStudent(final String studentId) {

        // Detect special IDs and generate synthetic student records, marking as not-real

        return switch (studentId) {
            case "GUEST" -> RawStudentLogic.makeFakeStudent(studentId, CoreConstants.EMPTY, "Guest");
            case "AACTUTOR", "ETEXT" -> RawStudentLogic.makeFakeStudent(studentId, CoreConstants.EMPTY, "Tutor");
            case "BOOKSTORE" -> RawStudentLogic.makeFakeStudent(studentId, CoreConstants.EMPTY, "Bookstore Staff");
            case null, default -> null;
        };
    }

    /**
     * Loads all e-texts owned by the student.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudentETexts(final Cache cache, final String studentId) throws SQLException {

        final SystemData systemData = cache.getSystemData();

        this.studentETexts = RawStetextLogic.queryByStudent(cache, studentId);

        final LocalDate today = this.owner.now.toLocalDate();
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

        return true;
    }

    /**
     * Loads all special categories for which the student is configured.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadSpecialStudent(final Cache cache, final String studentId) throws SQLException {

        final List<RawSpecialStus> allSpecial = RawSpecialStusLogic.queryByStudent(cache, studentId);
        if (allSpecial.isEmpty()) {
            this.specialStudents = allSpecial;
        } else {
            this.specialStudents = new ArrayList<>(allSpecial.size());
            final LocalDate today = this.owner.now.toLocalDate();

            for (final RawSpecialStus special : allSpecial) {
                // CSpecialStudent
                if ((special.startDt != null && special.startDt.isAfter(today))
                        || (special.endDt != null && special.endDt.isBefore(today))) {
                    continue;
                }
                this.specialStudents.add(special);
            }
        }

        return true;
    }

    /**
     * Loads all holds currently applied to a student's account.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudentHolds(final Cache cache, final String studentId) throws SQLException {

        this.studentHolds = RawAdminHoldLogic.queryByStudent(cache, studentId);

        return true;
    }

    /**
     * Loads all placement attempts for a student.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudentPlacement(final Cache cache, final String studentId) throws SQLException {

        this.studentPlacement = RawStmpeLogic.queryLegalByStudent(cache, studentId);

        return true;
    }

    /**
     * Loads all ELM attempts for a student.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadStudentElm(final Cache cache, final String studentId) throws SQLException {

        this.studentElm = RawStexamLogic.queryByStudentCourse(cache, studentId, RawRecordConstants.M100T, false);

        return true;
    }

    /**
     * Loads all placement credit for a student.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadStudentPlacementCredit(final Cache cache, final String studentId) throws SQLException {

        this.studentPlacementCredit = RawMpeCreditLogic.queryByStudent(cache, studentId);

        return true;
    }

    /**
     * Loads all OT credit for a student.
     *
     * @param cache     the data cache
     * @param studentId the ID of the student
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if there is an error accessing the database
     */
    private boolean loadStudentOTCredit(final Cache cache, final String studentId) throws SQLException {

        this.studentOTCredit = RawStcourseLogic.queryCreditByExam(cache, studentId);

        return true;
    }
}
