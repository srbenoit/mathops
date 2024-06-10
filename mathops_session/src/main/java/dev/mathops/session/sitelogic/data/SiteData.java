package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.StudentData;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * A container for the data used by site logic objects. This object and its child objects store all data relating to a
 * single student.
 */
public final class SiteData {

    /** A zero-length array used in construction of other arrays. */
    private static final String[] ZERO_LEN_STRING_ARR = new String[0];

    /** The student data object. */
    private final StudentData studentData;

    /** The courses to include. */
    private final String[] courses;

    /** The date/time to consider "now". */
    public final ZonedDateTime now;

    /** Data relating to student. */
    public final SiteDataStudent siteStudentData;

    /** Data relating to registrations. */
    public final SiteDataRegistration siteRegistrationData;

    /** Data relating to milestones. */
    public final SiteDataMilestone siteMilestoneData;

    /** Data relating to courses in which the student is enrolled or visiting. */
    public final SiteDataCourse siteCourseData;

    /** Data relating to student activity in courses. */
    public final SiteDataActivity siteActivityData;

    /** Data relating to student course status, including scores. */
    public final SiteDataStatus siteStatusData;

    /** The error message if loading data failed. */
    private String error = null;

    /**
     * Constructs a new {@code SiteData}.
     *
     * @param thStudentData the student data object
     * @param theNow        the date/time to consider now
     * @param theCourses    the courses to include
     */
    public SiteData(final StudentData thStudentData, final ZonedDateTime theNow, final String... theCourses) {

        this.now = theNow;
        this.courses = theCourses == null ? ZERO_LEN_STRING_ARR : theCourses.clone();
        this.studentData = thStudentData;

        this.siteStudentData = new SiteDataStudent(this);
        this.siteRegistrationData = new SiteDataRegistration(this);
        this.siteMilestoneData = new SiteDataMilestone(this);
        this.siteCourseData = new SiteDataCourse(this);
        this.siteActivityData = new SiteDataActivity(this);
        this.siteStatusData = new SiteDataStatus(this);
    }

    /**
     * Gets the student data object.
     *
     * @return the student data object
     */
    public StudentData getStudentData() {

        return this.studentData;
    }

    /**
     * Gets the list of courses to include.
     *
     * @return the array of courses
     */
    public String[] getCourses() {

        return this.courses.clone();
    }

    /**
     * Gets the error message in the event of a failure.
     *
     * @return the error message
     */
    public String getError() {

        return this.error;
    }

    /**
     * Sets the error message in the event of a failure.
     *
     * @param theError the error message
     */
    public void setError(final String theError) {

        final IllegalArgumentException ex = new IllegalArgumentException("SiteData error");
        final StackTraceElement[] stack = ex.getStackTrace();

        if (stack.length > 1) {
            final String fileName = stack[1].getFileName();
            final int lineNum = stack[1].getLineNumber();
            final String lineNumStr = Integer.toString(lineNum);
            Log.warning("SiteData error: ", theError, " (", fileName, CoreConstants.COLON, lineNumStr, ")", ex);
        } else {
            Log.warning("SiteData error: ", theError, ex);
        }

        this.error = theError;
    }

    /**
     * Loads all database data relevant to a session's effective user ID within the session's context, but does not use
     * the database cache objects.
     *
     * @param session the session info
     * @return {@code true} if success; {@code false} on any error
     */
    public boolean load(final ImmutableSessionInfo session) {

        final long start = System.currentTimeMillis();

        boolean success;
        try {
            success = loadData(session);
        } catch (final SQLException ex) {
            setError("Exception while querying data");
            Log.warning("Failed to query course site data", ex);
            success = false;
        }

        if (success) {
            final long end = System.currentTimeMillis();
            Log.info("SiteData took " + (end - start) + " ms. to gather: " + session.getEffectiveUserId()
                    + CoreConstants.SPC + session.getEffectiveScreenName());
        }

        return success;
    }

    /**
     * Queries all database data relevant to a session's effective user ID within the session's context.
     *
     * @param session the session info
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadData(final ImmutableSessionInfo session) throws SQLException {

        // NOTE: The "live reg" query is not done here - this logic will be called on each page refresh, and live
        // registration updates happen only on login


        // final long t0 = System.currentTimeMillis();

        this.siteStudentData.loadData(this.studentData);
        // final long t1 = System.currentTimeMillis();

        final boolean b2 = this.siteMilestoneData.preload(this.studentData);
        // final long t2 = System.currentTimeMillis();

        final boolean b3 = this.siteRegistrationData.loadData(this.studentData, session);
        // final long t3 = System.currentTimeMillis();

        final boolean b4 = this.siteMilestoneData.loadData(this.studentData);
        // final long t4 = System.currentTimeMillis();

        final boolean b5 = this.siteActivityData.loadData(this.studentData);
        // final long t5 = System.currentTimeMillis();

        final boolean b6 = this.siteStatusData.loadData(this.studentData);
        // final long t6 = System.currentTimeMillis();

        // Log.info(" Student data: " + (t1 - t0));
        // Log.info(" Milestone PRE data: " + (t2 - t1));
        // Log.info(" Registration data: " + (t3 - t2));
        // Log.info(" Milestone data: " + (t4 - t3));
        // Log.info(" Activity data: " + (t5 - t4));
        // Log.info(" Status data: " + (t6 - t5));

        return b2 && b3 && b4 && b5 && b6;
    }
}
