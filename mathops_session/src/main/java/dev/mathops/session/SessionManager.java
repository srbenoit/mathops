package dev.mathops.session;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogBase;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.login.IAuthenticationMethod;
import dev.mathops.session.login.ILoginProcessor;
import dev.mathops.session.login.LocalLoginProcessor;
import dev.mathops.session.login.LoginResult;
import dev.mathops.session.login.ShibbolethLoginProcessor;
import dev.mathops.session.login.TestStudentLoginProcessor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * A singleton class that manages the set of active user sessions.
 */
public final class SessionManager extends SessionCache implements ISessionManager {

    /** The name of the session ID cookie. */
    public static final String SESSION_ID_COOKIE = "blssessionid";

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "live_sessions.xml";

    /** A common integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The singleton instance. */
    private static SessionManager instance;

    /** The list of installed login processors. */
    private final ILoginProcessor[] processors;

    /**
     * Private constructor to prevent instantiation.
     */
    private SessionManager() {

        super();

        this.processors = new ILoginProcessor[]{new LocalLoginProcessor(),
                new ShibbolethLoginProcessor(), new TestStudentLoginProcessor()};
    }

    /**
     * Gets the singleton {@code SessionManager} instance.
     *
     * @return the instance
     */
    public static SessionManager getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (instance == null) {
                instance = new SessionManager();
            }

            return instance;
        }
    }

    /**
     * Gets a list of the available authentication methods, including their name, display name, and the fields required
     * by the method to authenticate a user.
     *
     * @return an array of the configured authentication methods
     */
    @Override
    public IAuthenticationMethod[] getAuthenticationMethods() {

        return this.processors.clone();
    }

    /**
     * Identifies the desired login processor by its type name.
     *
     * @param typeName the type name
     * @return the matching {@code ILoginProcessor}, or {@code null} if none matches
     */
    @Override
    public ILoginProcessor identifyProcessor(final String typeName) {

        ILoginProcessor result = null;

        for (final ILoginProcessor proc : this.processors) {
            if (proc.getType().equals(typeName)) {
                result = proc;
                break;
            }
        }

        return result;
    }

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
    @Override
    public SessionResult login(final Cache cache, final IAuthenticationMethod authMethod,
                               final Map<String, String> fieldValues, final boolean doLiveRegCheck)
            throws SQLException {

        if (authMethod == null || fieldValues == null) {
            Log.warning("Invalid arguments to login()");
            throw new IllegalArgumentException("Invalid arguments to login()");
        }

        Log.info("Login attempt using ", authMethod.getDisplayName());

        clearTimedOut();

        final SessionResult result;

        // Identify the login processor
        if (authMethod instanceof final ILoginProcessor proc) {
            final String sessionId = newSessionId();
            final LoginResult res = proc.login(cache, sessionId, fieldValues, doLiveRegCheck);
            final LiveSessionInfo live = res.session;

            if (res.error == null && live != null) {
                result = new SessionResult(addUserSession(live));
                LogBase.setSessionInfo(live.loginSessionId, live.getUserId());
                Log.info("Login succeeded: ", result.session.screenName);
            } else {
                Log.warning("Login failed: ", res.error);
                result = new SessionResult(res.error);
            }
        } else {
            result = new SessionResult("Invalid authentication method");
        }

        return result;
    }

    /**
     * Logs out a session, removing it from the list of active sessions.
     *
     * @param secSessionId the ID of the session to log out
     */
    @Override
    public void logout(final String secSessionId) {

        if (secSessionId == null) {
            Log.warning("Invalid arguments to logout()");
            throw new IllegalArgumentException("Invalid arguments to logout()");
        }

        final ImmutableSessionInfo info = getUserSession(secSessionId);
        if (info != null) {
            Log.info(info.screenName, " logging out session '", secSessionId, "'");
            removeUserSession(secSessionId);
        }
    }

    /**
     * Sets the effective user ID for the session. A user acting under the ADMINISTRATOR role can set this value to any
     * user ID in the system. A user acting under the STAFF role can set this value to the ID of any student in the
     * system (any user with only the STUDENT role). A user acting under the INSTRUCTOR role can set this value to any
     * students enrolled any of the instructor's courses.
     *
     * @param cache       the data cache
     * @param secSessionId   the ID of the session whose effective user ID to attempt to change
     * @param userId the desired effective user ID (could be {@code null})
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public SessionResult setEffectiveUserId(final Cache cache, final String secSessionId,
                                            final String userId) throws SQLException {

        if (secSessionId == null) {
            Log.warning("Invalid arguments to setEffectiveUserId()");
            throw new IllegalArgumentException("Invalid arguments to setEffectiveUserId()");
        }

        final SessionResult result;

        if ("AACTUTOR".equals(userId)) {
            result = setSessionActAs(secSessionId, userId, "AAC Tutor", ERole.STUDENT);
        } else {
            // Query the user, get screen name, return error if not found. Then test role of the
            // target user and see that the requester has needed permission.
            final RawStudent student = RawStudentLogic.query(cache, userId, false);

            if (student == null) {
                result = new SessionResult("Invalid user ID");
            } else {
                result = setSessionActAs(secSessionId, userId, student.getScreenName(), ERole.STUDENT);
            }
        }

        return result;
    }

    /**
     * Sets the user ID for the session. Restrictions on this method are as for setting the effective user ID, but this
     * method completely converts the login session to that of the target user, preventing subsequent changes to the
     * effective user ID (unless the target user's role permits). This method also resets the time offset of the session
     * to zero.
     *
     * @param cache        the data cache
     * @param secSessionId the ID of the session whose user ID to attempt to change
     * @param userId       the desired user ID (could be {@code null})
     * @param newRole      the new role
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public SessionResult setUserId(final Cache cache, final String secSessionId,
                                   final String userId, final ERole newRole) throws SQLException {

        if (secSessionId == null) {
            Log.warning("Invalid arguments to setUserId()");
            throw new IllegalArgumentException("Invalid arguments to setUserId()");
        }

        final SessionResult result;

        // Query the user, get screen name, return error if not found. Then test role of the
        // target user and see that the requester has needed permission.
        final RawStudent student = RawStudentLogic.query(cache, userId, true);

        if (student == null) {
            result = new SessionResult("Invalid user ID");
        } else {
            result = setSessionUser(secSessionId, userId, student.firstName, student.lastName,
                    student.getScreenName(), newRole);
        }

        return result;
    }

    /**
     * Sets the effective user ID for the session. A user acting under the ADMINISTRATOR role can set this value to any
     * user ID in the system. A user acting under the STAFF role can set this value to the ID of any student in the
     * system (any user with only the STUDENT role). A user acting under the INSTRUCTOR role can set this value to any
     * students enrolled any of the instructor's courses.
     *
     * @param sessionId the ID of the session whose effective user ID to attempt to change
     * @param actAsRole the desired effective role (could be {@code null})
     * @return the result of the user ID selection, which will contain the updated login session information on success;
     *         an error message on failure
     */
    public SessionResult setEffectiveRole(final String sessionId, final ERole actAsRole) {

        if (sessionId == null) {
            Log.warning("Invalid arguments to setEffectiveRole()");
            throw new IllegalArgumentException("Invalid arguments to setEffectiveRole()");
        }

        final ImmutableSessionInfo existing = getUserSession(sessionId);

        final SessionResult result;

        if (existing == null) {
            result = new SessionResult("Session not found");
        } else {
            final ERole effRole = actAsRole == existing.role ? null : actAsRole;
            result = setSessionActAs(sessionId, existing.actAsUserId, existing.actAsScreenName, effRole);
        }

        return result;
    }

    /**
     * When called by a user acting under the ADMINISTRATOR role, retrieves the list of active login sessions.
     *
     * @param secSessionId the session ID of the user making the request (must be acting under the ADMINISTRATOR role)
     * @return the list of active sessions, or {@code null} if the provided session ID is not valid or is not acting
     *         under the ADMINISTRATOR role
     */
    @Override
    public List<ImmutableSessionInfo> listActiveSessions(final String secSessionId) {

        return listUserSessions(secSessionId);
    }

    /**
     * Writes a persistent record of all current sessions to a file in a specified directory (any existing sessions file
     * in that directory is overwritten). This is intended to be called from the {@code destroy} method of an
     * authentication servlet to allow sessions to survive a new deployment of that servlet (but not a shutdown of the
     * server, since that would destroy SSL session IDs).
     *
     * @param dir the directory in which to persist the active sessions
     */
    @Override
    public void persist(final File dir) {

        final HtmlBuilder xml = new HtmlBuilder(1000);
        try {
            Log.info("Session manager persisting to " + dir.getAbsolutePath());
            persistToXml(xml);
            Log.info("Session manager generated XML");
        } catch (final RuntimeException ex) {
            Log.warning(ex);
        }

        if (dir.exists() || dir.mkdirs()) {
            final File target = new File(dir, PERSIST_FILENAME);

            try (final FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8)) {
                Log.info("Session manager persisting to " + target.getAbsolutePath());
                writer.write(xml.toString());
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        } else {
            Log.warning("Unable to create directory ", dir.getAbsolutePath());
        }
    }

    /**
     * Loads a set of active sessions previously written by {@code persist}, and deletes that session file so old
     * sessions cannot be re-used later.
     *
     * @param dir the directory in which persisted sessions were written
     */
    @Override
    public void load(final File dir) {

        final File target = new File(dir, PERSIST_FILENAME);

        Log.info("Restoring sessions from ", target.getAbsolutePath());

        if (target.exists()) {
            final String xml = FileLoader.loadFileAsString(target, true);

            try {
                final XmlContent content = new XmlContent(xml, true, false);
                final List<INode> nodes = content.getNodes();
                if (nodes != null) {
                    for (final INode node : nodes) {
                        if (node instanceof final EmptyElement elem) {

                            if (LiveSessionInfo.XML_TAG.equals(elem.getTagName())) {
                                parseSession(elem);
                            }
                        }
                    }
                }
            } catch (final ParsingException | DateTimeParseException | IllegalArgumentException ex) {
                Log.warning(ex);
            }

            if (!target.delete()) {
                Log.warning("Failed to delete ", target.getAbsolutePath());
            }
        }
    }

    /**
     * Parses a single session node and adds the live session object if valid.
     *
     * @param elem the element to parse
     * @throws DateTimeParseException if the session could not be parsed
     */
    private void parseSession(final IElement elem) throws DateTimeParseException {

        final String sessionId = elem.getStringAttr("id");

        if (sessionId == null) {
            Log.warning("Live session record with null 'id'");
        } else {
            final String tagStr = elem.getStringAttr("tag");
            long tag;
            try {
                tag = Long.parseLong(tagStr);
            } catch (final NullPointerException | NumberFormatException ex) {
                Log.warning(ex);
                tag = 0L;
            }

            final String authType = elem.getStringAttr("auth-type");

            final String role = elem.getStringAttr("role", null);
            final LiveSessionInfo sess =
                    new LiveSessionInfo(sessionId, tag, authType, ERole.fromAbbrev(role));

            final String userId = elem.getStringAttr("user-id");
            final String firstName = elem.getStringAttr("first-name");
            final String lastName = elem.getStringAttr("last-name");
            final String screenName = elem.getStringAttr("screen-name");
            sess.setUserInfo(userId, firstName, lastName, screenName);

            try {
                final Instant est = //
                        Instant.parse(elem.getStringAttr("established"));
                final Instant lastAct = //
                        Instant.parse(elem.getStringAttr("last-activity"));
                final Instant timeout = //
                        Instant.parse(elem.getStringAttr("timeout"));

                Integer timeOffset;
                try {
                    timeOffset = elem.getIntegerAttr("time-offset", ZERO);
                } catch (final ParsingException ex) {
                    timeOffset = ZERO;
                }
                final String actAsUser = elem.getStringAttr("act-as-user");
                final String actAsFirstName = elem.getStringAttr("act-as-first-name");
                final String actAsLastName = elem.getStringAttr("act-as-last-name");
                String actAsScreenName = elem.getStringAttr("act-as-screen-name");
                if (actAsScreenName == null) {
                    actAsScreenName = elem.getStringAttr("act-as-name");
                }
                final String actAsRole = elem.getStringAttr("act-as-role");

                sess.restoreState(est, lastAct, timeout, timeOffset, actAsUser, actAsFirstName,
                        actAsLastName, actAsScreenName, actAsRole);
            } catch (final DateTimeParseException ex) {
                Log.warning(ex);
            }

            if (!sess.isTimedOut()) {
                Log.info("Adding session '", sess.loginSessionId,
                        "' from file load, times out ", sess.getTimeout().toString());
                addUserSession(sess);
            }
        }
    }

    /**
     * Generates a new random session ID.
     *
     * @return the generated session ID
     */
    private static String newSessionId() {

        return CoreConstants.newId(SESSION_ID_LEN);
    }
}
