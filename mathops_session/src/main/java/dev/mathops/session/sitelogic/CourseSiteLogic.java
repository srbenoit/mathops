package dev.mathops.session.sitelogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.data.SiteData;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * A container for all logic that supports generation of a web page within a course website. The input is a session
 * (which contains the context/web-site, effective user ID, role, language, presence, and time offset). From that data,
 * this object queries all relevant database objects, performs all relevant business logic, and builds all data and
 * messages needed by the web page generation code to produce the web page.
 * <p>
 * Web page code should be performing NO logic - just layout and formatting of the information gathered here.
 */
public final class CourseSiteLogic {

    /** Course IDs in context. */
    public static final List<String> COURSE_IDS = Arrays.asList(RawRecordConstants.M117, RawRecordConstants.M118,
            RawRecordConstants.M124, RawRecordConstants.M125, RawRecordConstants.M126, RawRecordConstants.MATH117,
            RawRecordConstants.MATH118, RawRecordConstants.MATH124, RawRecordConstants.MATH125,
            RawRecordConstants.MATH126);

    /** The data cache. */
    private final Cache cache;

    /** The site profile. */
    private final WebSiteProfile siteProfile;

    /** The session info. */
    public final ImmutableSessionInfo sessionInfo;

    /** The error message if loading data failed. */
    private String error;

    /** The course site data. */
    public SiteData data = null;

    /** Course-related logic. */
    public CourseSiteLogicCourse course = null;

    /**
     * Constructs a new {@code CourseSiteLogic}.
     *
     * @param theCache       the data cache
     * @param theSiteProfile the site profile
     * @param theSessionInfo the session info
     */
    public CourseSiteLogic(final Cache theCache, final WebSiteProfile theSiteProfile,
                           final ImmutableSessionInfo theSessionInfo) {

        this.cache = theCache;
        this.siteProfile = theSiteProfile;
        this.sessionInfo = theSessionInfo;
    }

    /**
     * Gets the site profile under which the logic gathered data.
     *
     * @return the site profile
     */
    private WebSiteProfile getSiteProfile() {

        return this.siteProfile;
    }

    /**
     * Sets the error message in the event of a failure.
     *
     * @param theError the error message
     */
    private void setError(final String theError) {

        Log.warning(theError);
        this.error = theError;
    }

    /**
     * Tests whether there was an error in logic processing.
     *
     * @return {@code true} if there was an error; {@code false} if not
     */
    public boolean isError() {

        return this.error != null;
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
     * Gathers all data. If any error occurs, the appropriate error code and message are set.
     *
     * @return {@code true} if successful; {@code false} if any failure (in which case error message is set)
     */
    public boolean gatherData() {

        final DbProfile profile = getSiteProfile().dbProfile;

        final ZonedDateTime now = this.sessionInfo.getNow();
        final SiteData theData = new SiteData(profile, now);

        // First, do all database queries we'll need, so we get as close to a consistent image of the data as we can.
        final boolean success = theData.load(this.cache, this.sessionInfo);
        this.data = theData;

        if (success) {
            try {
                this.course = new CourseSiteLogicCourse(this.cache, this, theData, this.sessionInfo);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        } else {
            final String err = theData.getError();
            setError(err);
        }

        return success;
    }
}
