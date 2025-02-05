package dev.mathops.session.sitelogic.data;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Profile;
import dev.mathops.session.ImmutableSessionInfo;

import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * A container for the data used by site logic objects. This object and its child objects store all data relating to a
 * single student.
 */
public final class SiteData {

    /** The database profile. */
    private final Profile profile;

    /** The date/time to consider "now". */
    public final ZonedDateTime now;

    /** Data relating to student. */
    public final SiteDataStudent studentData;

    /** Data relating to registrations. */
    public final SiteDataRegistration registrationData;

    /** Data relating to milestones. */
    public final SiteDataMilestone milestoneData;

    /** Data relating to courses in which the student is enrolled or visiting. */
    public final SiteDataCourse courseData;

    /** Data relating to student activity in courses. */
    public final SiteDataActivity activityData;

    /** Data relating to student course status, including scores. */
    public final SiteDataStatus statusData;

    /** The error message if loading data failed. */
    private String error = null;

    /**
     * Constructs a new {@code SiteData}.
     *
     * @param theprofile the database profile (host, path, and DbProfile)
     * @param theNow     the date/time to consider now
     */
    public SiteData(final Profile theprofile, final ZonedDateTime theNow) {

        this.now = theNow;
        this.profile = theprofile;

        this.studentData = new SiteDataStudent(this);
        this.registrationData = new SiteDataRegistration(this);
        this.milestoneData = new SiteDataMilestone(this);
        this.courseData = new SiteDataCourse(this);
        this.activityData = new SiteDataActivity(this);
        this.statusData = new SiteDataStatus(this);
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
     * <p>
     * TODO: support flags that govern which data to load, so this object may be used everywhere, including checkin
     *    and checkout, the admin site, etc.
     *
     * @param theCache the cache
     * @param session  the session info
     * @return {@code true} if success; {@code false} on any error
     */
    public boolean load(final Cache theCache, final ImmutableSessionInfo session) {

        final long start = System.currentTimeMillis();

        boolean success;
        try {
            success = loadData(theCache, session);
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
     * @param cache   the data cache
     * @param session the session info
     * @return {@code true} if success; {@code false} on any error
     * @throws SQLException if an error occurs reading data
     */
    private boolean loadData(final Cache cache, final ImmutableSessionInfo session)
            throws SQLException {

        // NOTE: The "live reg" query is not done here - this logic will be called on each page
        // refresh, and live registration updates happen only on login

        // final long t0 = System.currentTimeMillis();

//        this.contextData.loadData(cache, this.courses);
        // final long t1 = System.currentTimeMillis();

        final boolean b2 = this.studentData.loadData(cache, session);
        // final long t2 = System.currentTimeMillis();

        final boolean b3 = this.milestoneData.preload(cache);
        // final long t3 = System.currentTimeMillis();

        final boolean b4 = this.registrationData.loadData(cache, session);
        // final long t4 = System.currentTimeMillis();

        final boolean b5 = this.milestoneData.loadData(cache);
        // final long t5 = System.currentTimeMillis();

        final boolean b6 = this.activityData.loadData(cache);
        // final long t6 = System.currentTimeMillis();

        final boolean b7 = this.statusData.loadData(cache);
        // final long t7 = System.currentTimeMillis();

        // Log.info(" Context data: " + (t1 - t0));
        // Log.info(" Student data: " + (t2 - t1));
        // Log.info(" Milestone PRE data: " + (t3 - t2));
        // Log.info(" Registration data: " + (t4 - t3));
        // Log.info(" Milestone data: " + (t5 - t4));
        // Log.info(" Activity data: " + (t6 - t5));
        // Log.info(" Status data: " + (t7 - t6));

        return b2 && b3 && b4 && b5 && b6 && b7;
    }
}
