package dev.mathops.db.logic;

import dev.mathops.db.old.rawrecord.RawStudent;

import java.util.HashMap;
import java.util.Map;

/**
 * A container for data associated with a single view of a webpage or a web-like application, in which there is a
 * logged-in user who may or may not be acting as another user, and who may be viewing information on one or more
 * students, all of whose data is relevant.
 */
public final class WebViewData {

    /** The data cache. */
    private final Cache cache;

    /** The single system data instance shared by all student data instances. */
    private final SystemData systemData;

    /** Student data for the logged-in user; null when there is no logged in user */
    private StudentData loggedInUser;

    /** Student data for the user as whom the logged-in user is acting; null if they are not acting. */
    private StudentData actAsUser;

    /** A map from student ID to student data container for "students of interest" in context. */
    private final Map<String, StudentData> studentData;

    /**
     * Constructs a new {@code WebViewData}
     *
     * @param theCache the data cache
     */
    public WebViewData(final Cache theCache) {

        this.cache = theCache;
        this.systemData = new SystemData(theCache);
        this.studentData = new HashMap<>(4);
    }

    /**
     * Gets the data cache.
     *
     * @return the data cache
     */
    public Cache getCache() {

        return this.cache;
    }

    /**
     * Gets the system data object.
     *
     * @return the system data object
     */
    public SystemData getSystemData() {

        return this.systemData;
    }

    /**
     * Sets the student data object for the logged-in user.
     *
     * @param studentId the student ID of the new logged-in user
     * @return the student data object for the logged-in user
     */
    public StudentData setLoggedInUser(final String studentId) {

        if (this.loggedInUser == null || !this.loggedInUser.getStudentId().equals(studentId)) {
            this.loggedInUser = new StudentData(this.cache, this.systemData, studentId, ELiveRefreshes.IF_MISSING);
        }

        return this.loggedInUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.  This also clears the "act as"
     * user, if present.
     *
     * @param newActAsUser the student data for the new "acting-as" user
     */
    public void setLoggedInUser(final StudentData newActAsUser) {

        this.loggedInUser = newActAsUser;
        this.actAsUser = null;
    }

    /**
     * Gets the student data object for the logged-in user.
     *
     * @return the student data object for the logged-in user
     */
    public StudentData getLoggedInUser() {

        return this.loggedInUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.
     *
     * @param studentId the student ID of the new "acting-as" user
     * @return the student data object for the "acting-as" user
     */
    public StudentData setActAsUser(final String studentId) {

        if (this.actAsUser == null || !this.actAsUser.getStudentId().equals(studentId)) {
            this.actAsUser = new StudentData(this.cache, this.systemData, studentId, ELiveRefreshes.IF_MISSING);
        }

        return this.actAsUser;
    }

    /**
     * Sets the student data object for the user as whom the logged-in user is acting.
     *
     * @param newActAsUser the student data for the new "acting-as" user
     */
    public void setActAsUser(final StudentData newActAsUser) {

        this.actAsUser = newActAsUser;
    }

    /**
     * Gets the student data object for the user as whom the logged-in user is acting.
     *
     * @return the student data object for the user as whom is being acted
     */
    public StudentData getActAsUser() {

        return this.actAsUser;
    }

    /**
     * Gets the student data object for the "effective" user, which is either the logged in user, or the user as whom
     * that logged-in user is currently acting if they are acting.
     *
     * @return the student data object for the effective user
     */
    public StudentData getEffectiveUser() {

        return this.actAsUser == null ? this.loggedInUser : this.actAsUser;
    }

    /**
     * Gets the data object for a student with a specified ID, creating a new {@code StudentData} object for that
     * student if one does not already exist.
     *
     * @param studentId the student ID
     * @return the student data object for the effective user
     */
    public StudentData getStudent(final String studentId) {

        return this.studentData.computeIfAbsent(studentId,
                key -> new StudentData(this.cache, this.systemData, key, ELiveRefreshes.IF_MISSING));
    }

    /**
     * Gets the data object for a student with a specified student record, creating a new {@code StudentData} object for
     * that student if one does not already exist.
     *
     * @param studentRecord the student record
     * @return the student data object for the effective user
     */
    public StudentData getStudent(final RawStudent studentRecord) {

        final String studentId = studentRecord.stuId;

        return this.studentData.computeIfAbsent(studentId,
                key -> new StudentData(this.cache, this.systemData, studentRecord));
    }
}
