package dev.mathops.session;

import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.login.IAuthenticationMethod;
import dev.mathops.session.login.ILoginProcessor;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * The available session management methods.
 */
public interface ISessionManager {

    /** The length of session IDs (roughly 6 bits per character). */
    int SESSION_ID_LEN = 16;

    /**
     * Gets a list of the available authentication methods, including their name, display name, and the fields required
     * by the method to authenticate a user.
     *
     * @return an array of the configured authentication methods
     */
    IAuthenticationMethod[] getAuthenticationMethods();

    /**
     * Attempts to authenticate a user and create a new session.
     *
     * @param cache          the data cache
     * @param authMethod     the authentication method to use (one of the values returned by
     *                       {@code getAuthenticationMethods})
     * @param fieldValues    the values of the various fields required by the selected authentication method
     * @param doLiveRegCheck true to include a live registration check in the process
     * @return the result of the login attempt
     * @throws SQLException if there was an error accessing the database
     */
    SessionResult login(Cache cache, IAuthenticationMethod authMethod,
                        Map<String, String> fieldValues, boolean doLiveRegCheck) throws SQLException;

    /**
     * Identifies the desired login processor by its type name.
     *
     * @param typeName the type name
     * @return the matching {@code ILoginProcessor}, or {@code null} if none matches
     */
    ILoginProcessor identifyProcessor(String typeName);

    /**
     * Logs out a session, removing it from the list of active sessions.
     *
     * @param secSessionId the ID of the session to log out
     */
    void logout(String secSessionId);

    /**
     * Tests whether a session ID is valid, updates the last access timestamp, timeout, and last activity associated
     * with the session.
     *
     * @param sessionId the ID of the session to validate
     * @return the result of the validation, which will contain the information for the validated session on success; an
     *         error message on failure
     */
    SessionResult validate(String sessionId);

    /**
     * Sets the effective user ID for the session. A user acting under the ADMINISTRATOR role can set this value to any
     * user ID in the system. A user acting under the STAFF role can set this value to the ID of any student in the
     * system (any user with only the STUDENT role). A user acting under the INSTRUCTOR role can set this value to any
     * students enrolled any of the instructor's courses.
     *
     * @param cache        the data cache
     * @param secSessionId the ID of the session whose effective user ID to attempt to change
     * @param userId       the desired effective user ID (could be {@code null})
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     * @throws SQLException if there is an error accessing the database
     */
    SessionResult setEffectiveUserId(Cache cache, String secSessionId, String userId)
            throws SQLException;

    /**
     * Sets the user ID for the session. Restrictions on this method are as for setting the effective user ID, but this
     * method completely converts the login session to that of the target user, preventing subsequent changes to the
     * effective user ID (unless the target user's role permits).
     *
     * @param cache        the data cache
     * @param secSessionId the ID of the session whose user ID to attempt to change
     * @param userId       the desired user ID (could be {@code null})
     * @param newRole      the new role
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     * @throws SQLException if there is an error accessing the database
     */
    SessionResult setUserId(Cache cache, String secSessionId, String userId, ERole newRole)
            throws SQLException;

    /**
     * Sets the time offset for the session. Restrictions on this method are as for setting the effective user ID. The
     * time offset is used when determining the current time, allowing a session to show what a user would see in the
     * past or future.
     *
     * @param secSessionId the ID of the session whose user ID to attempt to change
     * @param timeOffset   the desired time offset for the session
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     */
    SessionResult setTimeOffset(String secSessionId, long timeOffset);

    /**
     * When called by a user acting under the ADMINISTRATOR role, retrieves the list of active login sessions.
     *
     * @param secSessionId the session ID of the user making the request (must be acting under the ADMINISTRATOR role)
     * @return the list of active sessions
     */
    List<ImmutableSessionInfo> listActiveSessions(String secSessionId);

    /**
     * Gets the immutable form of a user session record with a particular session ID.
     *
     * @param sessionId the session ID for which to query
     * @return the session record
     */
    ImmutableSessionInfo getUserSession(String sessionId);

    /**
     * Writes a persistent record of all current sessions to a file in a specified directory (any existing sessions file
     * in that directory is overwritten). This is intended to be called from the {@code destroy} method of an
     * authentication servlet to allow sessions to survive a redeployment of that servlet (but not a shutdown of the
     * server, since that would destroy SSL session IDs).
     *
     * @param dir the directory in which to persist the active sessions
     */
    void persist(File dir);

    /**
     * Loads a set of active sessions previously written by {@code persist}.
     *
     * @param dir the directory in which persisted sessions were written
     */
    void load(File dir);
}
