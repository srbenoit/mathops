package dev.mathops.session;

import dev.mathops.core.EPath;
import dev.mathops.core.PathList;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.login.LocalLoginProcessor;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A cache of active sessions, maintained by the session manager. The cache stores three types of sessions: normal
 * sessions, development sessions (which do not time out), and testing station sessions.
 *
 * <p>
 * The session cache manages the only live copy of session records. All changes to session records MUST take place
 * within this cache. Immutable copies of session records can be obtained with appropriate permissions.
 */
public class SessionCache {

    /** An anonymous session ID for access while not logged in. */
    public static final String ANONYMOUS_SESSION = "AnonymousSession";

    /** A session ID to be used by testing stations. */
    public static final String TEST_SESSION_ID = "TestingStationID";

    /** The user ID of a built-in administrative user. */
    @Deprecated
    private static final String ADMIN_USER_ID = "111223333";

    /** The Shibboleth timeout duration (5 hours). */
    private static final long SHIBBOLETH_TIMEOUT = (long) (5 * 60 * 60 * 1000);

    /** A typical number of sessions. */
    private static final int TYP_NUM_SESSIONS = 50;

    /** Error text to indicate an invalid session. */
    private static final String INVALID_ERR = "Invalid session ";

    /** Error message. */
    private static final String NO_AUTH_ACT_AS = "Not authorized to act as that user";

    /** Error message. */
    private static final String TIMED_OUT = "Session has timed out";

    /** Administrator first name. */
    private static final String ADMIN_FIRST = "STEVE";

    /** Administrator last name. */
    private static final String ADMIN_LAST = "PROD-BAERREAL";

    /** Administrator screen name. */
    private static final String ADMIN_SCREEN = "STEVE PROD-BAERREAL";

    /** Object on which to synchronize access to the session maps. */
    private final Object synch;

    /** A map from secure session ID to session information. */
    private final Map<String, LiveSessionInfo> userSessions;

    /** The testing center sessions (max one per context). */
    private final ImmutableSessionInfo testingSession;

    /** Logged out shibboleth session cookies with log out time. */
    private final Map<String, Long> loggedOutSessions;

    /** List of users authorized to act as system administrators. */
    private final List<String> sysadmins;

    /**
     * Private constructor to prevent instantiation.
     */
    SessionCache() {

        this.synch = new Object();
        this.userSessions = new HashMap<>(TYP_NUM_SESSIONS);

        // Support testing session ID in testing transaction context
        final LiveSessionInfo liveTesting = new LiveSessionInfo(TEST_SESSION_ID, LocalLoginProcessor.TYPE, ERole.GUEST);
        liveTesting.setUserInfo(ADMIN_USER_ID, ADMIN_FIRST, ADMIN_LAST, ADMIN_SCREEN);
        this.testingSession = new ImmutableSessionInfo(liveTesting);

        // Support anonymous session ID
        final LiveSessionInfo liveAnon = new LiveSessionInfo(ANONYMOUS_SESSION, LocalLoginProcessor.TYPE, ERole.GUEST);
        this.userSessions.put(ANONYMOUS_SESSION, liveAnon);

        this.loggedOutSessions = new HashMap<>(50);

        this.sysadmins = new ArrayList<>(4);
        final File file = new File(PathList.getInstance().get(EPath.CFG_PATH), "sysadmins");
        final String[] lines = FileLoader.loadFileAsLines(file, false);
        if (lines != null) {
            for (final String line : lines) {
                if (line.length() == 9) {
                    Log.info("User ", line,
                            " authorized to access system administrator functions.");
                    this.sysadmins.add(line);
                }
            }
        }

        Log.info("Session cache initialized");
    }

    /**
     * Tests whether a user ID is authorized to access system administrator functions.
     *
     * @param userId the user id
     * @return true if authorized to access sysadmin functions
     */
    public final boolean isSysadmin(final String userId) {

        return this.sysadmins.contains(userId);
    }

    /**
     * Adds a new user session.
     *
     * @param sess the user session to add
     * @return the immutable session information
     */
    final ImmutableSessionInfo addUserSession(final LiveSessionInfo sess) {

        synchronized (this.synch) {
            this.userSessions.put(sess.loginSessionId, sess);
            return sess.makeImmutable();
        }
    }

    /**
     * Removes a user session with a particular session ID, if such a session exists.
     *
     * @param sessionId the session ID to remove
     */
    final void removeUserSession(final String sessionId) {

        synchronized (this.synch) {
            if (!ANONYMOUS_SESSION.equals(sessionId)) {
                this.userSessions.remove(sessionId);
            }
        }
    }

    /**
     * Removes any timed out user sessions from the user session cache.
     */
    final void clearTimedOut() {

        final Instant now = Instant.now();
        final long timeout = System.currentTimeMillis() - SHIBBOLETH_TIMEOUT;

        synchronized (this.synch) {
            final Iterator<Map.Entry<String, LiveSessionInfo>> iter =
                    this.userSessions.entrySet().iterator();

            while (iter.hasNext()) {

                final Map.Entry<String, LiveSessionInfo> entry = iter.next();
                final LiveSessionInfo live = entry.getValue();

                if (live.loginSessionId.equals(ANONYMOUS_SESSION)) {
                    continue;
                }

                if (live.getTimeout().compareTo(now) < 0) {
                    Log.info("Session: ", entry.getValue().loginSessionId,
                            " timed out");
                    iter.remove();
                }
            }

            this.loggedOutSessions.entrySet().removeIf(entry -> entry.getValue().longValue() < timeout);
        }
    }

    /**
     * Writes all current session information to an XML stream for persisting as a file. This is used when a server is
     * to be restarted to store active sessions, allowing those sessions to be restored after a restart and avoid
     * interruption of user sessions.
     *
     * @param xml the XML stream to which to write
     */
    final void persistToXml(final HtmlBuilder xml) {

        synchronized (this.synch) {

            for (final LiveSessionInfo sess : this.userSessions.values()) {
                if (sess.isTimedOut() || ANONYMOUS_SESSION.equals(sess.loginSessionId)) {
                    continue;
                }
                sess.appendXml(xml);
            }
        }
    }

    /**
     * When called by a user acting under the ADMINISTRATOR role, retrieves the list of active login sessions.
     *
     * @param sessionId the session ID of the user making the request (must be acting under the ADMINISTRATOR role)
     * @return the list of active sessions, or {@code null} if the provided session ID is not valid or is not acting
     *         under the ADMINISTRATOR role
     */
    final List<ImmutableSessionInfo> listUserSessions(final String sessionId) {

        final List<ImmutableSessionInfo> result;

        synchronized (this.synch) {
            final LiveSessionInfo sess = this.userSessions.get(sessionId);

            if (sess == null) {
                result = null;
            } else if (sess.getRole().canActAs(ERole.ADMINISTRATOR)) {
                result = new ArrayList<>(this.userSessions.size());

                for (final LiveSessionInfo toCopy : this.userSessions.values()) {
                    result.add(toCopy.makeImmutable());
                }
            } else {
                result = null;
            }
        }

        return result;
    }

    /**
     * Gets the immutable form of a user session record with a particular session ID.
     *
     * @param sessionId the session ID for which to query
     * @return the session record
     */
    public final ImmutableSessionInfo getUserSession(final String sessionId) {

        synchronized (this.synch) {
            final LiveSessionInfo sess = this.userSessions.get(sessionId);

            return sess == null ? null : sess.makeImmutable();
        }
    }

    /**
     * Attempts to set the user under which an active login session will henceforth act. Only login sessions
     * acting under certain roles can set their effective users to users with certain roles:
     *
     * <ul>
     * <li>SUPERUSER can set any effective user or role.
     * <li>ADMINISTRATOR can set any effective user with any role that is not SUPERUSER or
     * ADMINISTRATOR.
     * <li>STAFF can set an effective user with INSTRUCTOR, PROCTOR, TUTOR, STUDENT, or GUEST role.
     * <li>INSTRUCTOR can set an effective user with TUTOR, STUDENT, or GUEST role.
     * </ul>
     *
     * @param sessionId       the ID of the session whose effective user to set
     * @param actAsUserId     the user ID of the effective user
     * @param actAsScreenName the screen name of the effective user
     * @param actAsRole       the effective user role
     * @return the session change result, with the updated immutable session info that was changed, or an error message
     *         on failure
     */
    final SessionResult setSessionActAs(final String sessionId, final String actAsUserId,
                                        final String actAsScreenName, final ERole actAsRole) {

        final SessionResult result;

        synchronized (this.synch) {
            final LiveSessionInfo sess = this.userSessions.get(sessionId);

            if (sess == null || sess.getRole() == null) {
                result = new SessionResult(INVALID_ERR + sessionId);
            } else if (sess.getRole().canActAs(actAsRole)) {
                sess.setActAsUserInfo(actAsUserId, actAsScreenName, actAsRole);
                result = new SessionResult(sess.makeImmutable());
            } else {
                result = new SessionResult(NO_AUTH_ACT_AS);
            }
        }

        return result;
    }

    /**
     * Attempts to set the user for an active login session. This also sets the time offset of the session to zero. Only
     * login sessions acting under certain roles can set their user IDs to users with certain roles:
     *
     * <ul>
     * <li>SUPERUSER can set any effective user or role.
     * <li>ADMINISTRATOR can set any effective user with any role that is not SUPERUSER or
     * ADMINISTRATOR.
     * <li>STAFF can set an effective user with INSTRUCTOR, PROCTOR, TUTOR, STUDENT, or GUEST role.
     * <li>INSTRUCTOR can set an effective user with TUTOR, STUDENT, or GUEST role.
     * </ul>
     *
     * @param sessionId     the ID of the session whose effective user to set
     * @param newUserId     the user ID of the effective user
     * @param newFirstName  the first name of the effective user
     * @param newLastName   the last name of the effective user
     * @param newScreenName the screen name of the effective user
     * @param newRole       the effective user role
     * @return the session change result, with the updated immutable session info that was changed, or an error message
     *         on failure
     */
    final SessionResult setSessionUser(final String sessionId, final String newUserId, final String newFirstName,
                                       final String newLastName, final String newScreenName, final ERole newRole) {

        final SessionResult result;

        synchronized (this.synch) {
            final LiveSessionInfo sess = this.userSessions.get(sessionId);

            if (sess == null || sess.getRole() == null) {
                result = new SessionResult(INVALID_ERR + sessionId);
            } else if (sess.getRole().canActAs(newRole)) {
                sess.setUserInfo(newUserId, newFirstName, newLastName, newScreenName);
                sess.setRole(newRole);
                sess.setTimeOffset(0L);
                sess.clearActAsUserInfo();
                result = new SessionResult(sess.makeImmutable());
            } else {
                result = new SessionResult(NO_AUTH_ACT_AS);
            }
        }

        return result;
    }

    /**
     * Sets the time offset of a user session.
     *
     * @param sessionId  the ID of the session whose time offset to set
     * @param timeOffset the new time offset
     * @return the session change result, with the updated immutable session info that was changed, or an error message
     *         on failure
     */
    public SessionResult setTimeOffset(final String sessionId, final long timeOffset) {

        final SessionResult result;

        synchronized (this.synch) {
            final LiveSessionInfo sess = this.userSessions.get(sessionId);

            if (sess == null) {
                result = new SessionResult(INVALID_ERR + sessionId);
            } else {
                sess.setTimeOffset(timeOffset);
                result = new SessionResult(sess.makeImmutable());
            }
        }

        return result;
    }

    /**
     * Tests whether a session ID is valid, updates the last access timestamp, timeout, and last activity associated
     * with the session.
     *
     * @param sessionId the ID of the session to validate
     * @return the result of the validation, which will contain the information for the validated session on success; an
     *         error message on failure
     */
    public final SessionResult validate(final String sessionId) {

        final SessionResult result;

        synchronized (this.synch) {
            if (sessionId == null) {
                result = new SessionResult(INVALID_ERR);
            } else {
                final LiveSessionInfo sess = this.userSessions.get(sessionId);

                if (sess == null) {
                    // Do this test here rather than above since it will be very rarely used, and
                    // we don't want the overhead of the test on EVERY valid session activity
                    result = checkForSpecialSession(sessionId);
                } else if (sess.isTimedOut()) {
                    result = new SessionResult(TIMED_OUT);
                } else {
                    sess.touch();
                    result = new SessionResult(sess.makeImmutable());
                }
            }
        }

        return result;
    }

    /**
     * Checks the session ID against a special session ID.
     *
     * @param sessionId the session ID to test
     * @return the result of the validation, which will contain the information for the validated session on success; an
     *         error message on failure
     */
    private SessionResult checkForSpecialSession(final String sessionId) {

        final SessionResult result;

        // Do this test here rather than above since it will be very rarely used, and
        // we don't want the overhead of the test on EVERY valid session activity
        if (TEST_SESSION_ID.equals(sessionId)) {
            final ImmutableSessionInfo info = this.testingSession;

            if (info == null) {
                Log.warning("No testing session");
                result = new SessionResult(INVALID_ERR + sessionId);
            } else {
                result = new SessionResult(info);
            }
        } else {
            Log.warning("Session ID '" + sessionId + "' not found");
            result = new SessionResult(INVALID_ERR + sessionId);
        }

        return result;
    }

    /**
     * Stores a logged-out session.
     *
     * @param value the logged-out session cookie value
     */
    public final void storeLoggedOutSession(final String value) {

        Log.info("Logout from session " + value);
        synchronized (this.synch) {
            this.loggedOutSessions.put(value, Long.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * Tests whether a session was previously logged out.
     *
     * @param value the session cookie value
     * @return {@code true} if that cookie value was previously logged out
     */
    public final boolean isLoggedOutSession(final String value) {

        synchronized (this.synch) {
            return this.loggedOutSessions.containsKey(value);
        }
    }
}
