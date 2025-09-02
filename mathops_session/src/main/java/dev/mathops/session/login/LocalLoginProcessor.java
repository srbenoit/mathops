package dev.mathops.session.login;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.field.ERole;
import dev.mathops.db.logic.ELiveRefreshes;
import dev.mathops.db.schema.legacy.impl.RawLoginsLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawLogins;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Map;

/**
 * A JavaBean that accepts a username and password and processes a login against the local logins table.
 */
public final class LocalLoginProcessor implements ILoginProcessor {

    /** The type name for this processor. */
    public static final String TYPE = "Local";

    /** The name of the username field. */
    private static final String USERNAME = "username";

    /** The name of the password field. */
    private static final String PASSWORD = "password";

    /** Characters to use in hex representation. */
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    /** Mask for lowest order four bits. */
    private static final int FOUR_BIT_MASK = 0x0F;

    /** Error message. */
    private static final String ERR_MSG = "Invalid Login";

    /** Error message. */
    private static final String STU_INFO_ERR = "Failed to look up student information";

    /**
     * Construct a new {@code LocalLoginProcessor}.
     */
    public LocalLoginProcessor() {

        // No action
    }

    /**
     * Gets a type name (safe for use as an HTTP request parameter) for the login authentication method.
     *
     * @return the type name
     */
    @Override
    public String getType() {

        return TYPE;
    }

    /**
     * Gets a display name for the login authentication method (the user may choose from several available methods).
     *
     * @return the display name
     */
    @Override
    public String getDisplayName() {

        return "Local Database";
    }

    /**
     * Gets a list of the fields that the login form should display.
     *
     * @return the array of fields
     */
    @Override
    public LoginFormField[] getLoginFormFields() {

        return new LoginFormField[]{new LoginFormField(ELoginFieldType.TEXT),
                new LoginFormField(ELoginFieldType.PASSWORD),};
    }

    /**
     * Attempts to authenticate a user based on responses to the login fields and create a login session.
     *
     * @param cache          the data cache
     * @param secSessionId   the ID of the new session
     * @param fieldValues    the values provided by the user in the login field
     * @param liveRefreshes the live refresh policy
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed) @ throws SQLException if there is an error accessing the
     *         database
     */
    @Override
    public LoginResult login(final Cache cache, final String secSessionId, final Map<String, String> fieldValues,
                             final ELiveRefreshes liveRefreshes) throws SQLException {

        final String username = fieldValues.get(USERNAME);
        final LoginResult result;

        if (username == null) {
            result = new LoginResult("No username provided");
        } else {
            final String password = fieldValues.get(PASSWORD);

            if (password == null) {
                result = new LoginResult("No password provided");
            } else {
                result =
                        processRequest(cache, new LocalLoginAttempt(secSessionId, username, password));
            }
        }

        return result;
    }

    /**
     * Queries the local logins table for a record with the provided username, validates the provided password, and
     * creates the login information.
     *
     * @param cache   the data cache
     * @param attempt the login attempt
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed)
     * @throws SQLException if there is an error accessing the database
     */
    private static LoginResult processRequest(final Cache cache, final LocalLoginAttempt attempt)
            throws SQLException {

        final RawLogins login = RawLoginsLogic.query(cache, attempt.username);

        final LoginResult result;
        if (login == null) {
            Log.warning("No local login record with username '", attempt.username, "'");
            result = new LoginResult(ERR_MSG);
        } else {
            result = validateLogin(cache, login, attempt);
        }

        return result;
    }

    /**
     * Tests whether the login record will allow a login, and if so, moves on to testing the supplied password.
     *
     * @param cache   the data cache
     * @param login   the login record
     * @param attempt the login attempt
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed)
     * @throws SQLException if there is an error accessing the database
     */
    private static LoginResult validateLogin(final Cache cache, final RawLogins login,
                                             final LocalLoginAttempt attempt) throws SQLException {

        final LoginResult result;

        if (isInPast(login.dtimeExpires)) {
            Log.warning("Login is expired");
            result = new LoginResult("Login has expired");
        } else {
            result = checkPassword(cache, login, attempt);
        }

        return result;
    }

    /**
     * Tests whether a date is in the past.
     *
     * @param when the date to test
     * @return {@code true} if the date is not {@code null} and represents a date in the past; {@code false} otherwise
     */
    private static boolean isInPast(final ChronoLocalDateTime<LocalDate> when) {

        return when != null && when.isBefore(LocalDateTime.now());
    }

    /**
     * Validates the supplied password against the login record from the database.
     *
     * @param cache   the data cache
     * @param login   the login record
     * @param attempt the login attempt
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed)
     * @throws SQLException if there is an error accessing the database
     */
    private static LoginResult checkPassword(final Cache cache, final RawLogins login,
                                             final LocalLoginAttempt attempt) throws SQLException {

        LoginResult result;

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");

            final String salt = login.salt;

            if (salt == null) {
                Log.warning("No password salt for user '", attempt.username,
                        "'");
                failedLogin(cache, login);
                result = new LoginResult(ERR_MSG);
            } else {
                final byte[] pwdBytes = attempt.password.getBytes(StandardCharsets.UTF_8);
                final byte[] hash = digest.digest(pwdBytes);

                final byte[] toHash;

                final byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
                toHash = new byte[saltBytes.length + hash.length];
                System.arraycopy(saltBytes, 0, toHash, 0, saltBytes.length);
                System.arraycopy(hash, 0, toHash, saltBytes.length, hash.length);

                final byte[] bytes = digest.digest(toHash);
                final HtmlBuilder actual = new HtmlBuilder(bytes.length << 1);
                for (final int byt : bytes) {
                    actual.add(HEX[byt >> 4 & FOUR_BIT_MASK]);
                    actual.add(HEX[byt & FOUR_BIT_MASK]);
                }
                final String hashStr = actual.toString();

                if (hashStr.equals(login.storedKey)) {
                    result = successfulLogin(cache, login, attempt);
                } else {
                    Log.warning("Invalid password for user '",
                            attempt.username, "'");
                    failedLogin(cache, login);
                    result = new LoginResult(ERR_MSG);
                }
            }
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning(ex);
            result = new LoginResult(ERR_MSG);
        }

        return result;
    }

    /**
     * Handles a successful login, updating the login record and generating the login session information.
     *
     * @param cache   the data cache
     * @param login   the login record
     * @param attempt the login attempt
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed)
     * @throws SQLException if there is an error accessing the database
     */
    private static LoginResult successfulLogin(final Cache cache, final RawLogins login,
                                               final LocalLoginAttempt attempt) throws SQLException {

        final Integer fails = login.nbrInvalidAtmpts;

        if (fails != null && fails.intValue() > 0) {
            RawLoginsLogic.updatePasswordFails(cache, login.userName, Integer.valueOf(0));
        }

        RawLoginsLogic.updateLastLoginTime(cache, login);

        final RawStudent student = RawStudentLogic.query(cache, login.userId, true);
        final LoginResult result;

        if (student == null) {
            // The user may be a "pseudo-student" guest login like "CALC".
            result = checkPseudoStudent(attempt, login.userId);
        } else {
            final ERole role = ERole.fromAbbrev(login.userType);

            if (role == null) {
                Log.warning("Invalid role '", login.userType, "' configured for user ", attempt.username);
                result = new LoginResult(ERR_MSG);
            } else {
                final String studentId = attempt.student.stuId;
                final String first = attempt.student.firstName;
                final String last = attempt.student.lastName;
                final String screen = attempt.student.getScreenName();

                final LiveSessionInfo live = new LiveSessionInfo(attempt.sessionId, TYPE, role);
                live.setUserInfo(studentId, first, last, screen);

                result = new LoginResult(live);
            }
        }

        return result;
    }

    /**
     * Test whether the student trying to log in is one of the "pseudo" guest students who can access materials
     * anonymously, as in some of the tutorial and review sites.
     *
     * @param attempt   the login attempt
     * @param studentId the student ID to test
     * @return the generated {@code LoginResult} for the login attempt
     */
    private static LoginResult checkPseudoStudent(final LocalLoginAttempt attempt, final String studentId) {

        final String trimmed = studentId.trim();
        final LoginResult result;

        switch (trimmed) {
            case "GUEST", "AACTUTOR", "ETEXT" -> {
                final LiveSessionInfo live = new LiveSessionInfo(attempt.sessionId, TYPE, ERole.GUEST);
                live.setUserInfo(trimmed, "Guest", "User", "Guest");
                result = new LoginResult(live);
            }
            default -> result = new LoginResult(STU_INFO_ERR);
        }

        return result;
    }

    /**
     * Handles a failed login, updating the login record to reflect the failed attempt, and possibly disabling the
     * login.
     *
     * @param cache the data cache
     * @param login the login record
     * @throws SQLException if there is an error accessing the database
     */
    private static void failedLogin(final Cache cache, final RawLogins login) throws SQLException {

        final Integer fails = login.nbrInvalidAtmpts;
        final int newFails = fails == null ? 1 : fails.intValue() + 1;

        RawLoginsLogic.updatePasswordFails(cache, login.userName, Integer.valueOf(newFails));
    }
}
