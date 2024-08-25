package dev.mathops.session;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

/**
 * A mutable container of information on a single active login session, used internally to avoid construction of new
 * objects when fields change value.
 */
public final class LiveSessionInfo {

    /** The tag for the XML representation of a live session. */
    static final String XML_TAG = "live-session";

    /** The timeout duration (2 hours), in milliseconds. */
    private static final long TIMEOUT = (long) (2 * 60 * 60 * 1000);

    /** Object on which to synchronize tag creation. */
    private static final Object SYNCH = new Object();

    /** The most recently generated tag. */
    private static long lastTag = 0L;

    /** The login session ID (unique per session, long but not guessable). */
    final String loginSessionId;

    /** The login session tag (unique per session - short and monotone but guessable). */
    final long loginSessionTag;

    /** The type of authentication used in the login process. */
    private final String authType;

    /** The date/time when the session was established. */
    private Instant established;

    /** The ID of the logged-in user. */
    private String userId = null;

    /** The first name of the logged-in user. */
    private String firstName = null;

    /** The last name of the logged-in user. */
    private String lastName = null;

    /** The screen name of the logged-in user. */
    private String screenName = null;

    /** The date/time when the session had its last activity. */
    private Instant lastActivity;

    /** The date/time when the session will time out if no activity occurs. */
    private Instant timeout;

    /** The role. */
    private ERole role;

    /** The ID of the effective user. */
    private String actAsUserId = null;

    /** The first name of the effective user. */
    private String actAsFirstName = null;

    /** The last name of the effective user. */
    private String actAsLastName = null;

    /** The screen name of the effective user. */
    private String actAsScreenName = null;

    /** The effective role. */
    private ERole actAsRole = null;

    /** The time offset to apply to this session. */
    private long timeOffset = 0L;

    /**
     * Constructs a new {@code LiveSessionInfo}. The user ID, screen name, role, and presence are not set, as these
     * objects are created before login is completed. The establishment time and the last activity time are both set to
     * the current time, and the last action is set to "session established".
     *
     * @param theSessionId the session ID
     * @param theAuthType  the type of authentication used to create this session
     * @param theRole      the role
     */
    public LiveSessionInfo(final String theSessionId, final String theAuthType, final ERole theRole) {

        if (theSessionId == null) {
            Log.warning(new IllegalArgumentException("Session ID was null"));
        }

        final LocalDateTime today = LocalDateTime.now();

        final int yy = today.getYear() % 100;
        final int mm = today.getMonthValue() - 1;
        final int dd = today.getDayOfMonth() - 1;
        final int field1 = yy * (12 * 31) + mm * 31 + dd;

        final int hr = today.getHour();
        final int mn = today.getMinute();
        final int sc = today.getSecond();
        final int field2 = hr * (60 * 60) + mn * 60 + sc;

        final int ms = today.get(ChronoField.MILLI_OF_SECOND);
        long tag = ((long) field1 * 10000L + (long) field2) * 10000L + (long) ms * 10L;
        synchronized (SYNCH) {
            if (tag <= lastTag) {
                tag = lastTag + 1L;
            }
            lastTag = tag;
        }

        final Instant now = Instant.now();

        this.loginSessionId = theSessionId;
        this.loginSessionTag = tag;
        this.authType = theAuthType;
        this.established = now;
        this.lastActivity = this.established;
        this.timeout = now.plusMillis(TIMEOUT);
        this.role = theRole;
    }

    /**
     * Constructs a new {@code LiveSessionInfo}. The user ID, screen name, role, and presence are not set, as these
     * objects are created before login is completed. The establishment time and the last activity time are both set to
     * the current time, and the last action is set to "session established".
     *
     * @param theSessionId  the session ID
     * @param theSessionTag the session tag
     * @param theAuthType   the type of authentication used to create this session
     * @param theRole       the role
     */
    LiveSessionInfo(final String theSessionId, final long theSessionTag, final String theAuthType,
                    final ERole theRole) {

        final Instant now = Instant.now();

        this.loginSessionId = theSessionId;
        this.loginSessionTag = theSessionTag;
        this.authType = theAuthType;
        this.established = Instant.now();
        this.lastActivity = this.established;
        this.timeout = now.plusMillis(TIMEOUT);
        this.role = theRole;
        this.timeOffset = 0L;
    }

    /**
     * Sets the user ID and screen name of the logged-in user.
     *
     * @param theUserId     the user ID
     * @param theFirstName  the first name
     * @param theLastName   the last name
     * @param theScreenName the screen name
     */
    public void setUserInfo(final String theUserId, final String theFirstName,
                            final String theLastName, final String theScreenName) {

        if ("MPE".equals(theUserId)) {
            throw new IllegalArgumentException("Cannot act as 'MPE' user.");
        }

        this.userId = theUserId;
        this.firstName = theFirstName;
        this.lastName = theLastName;
        this.screenName = theScreenName;
    }

    /**
     * Sets the effective user ID under which this session will act.
     *
     * @param theUserId     the new effective user ID
     * @param theScreenName the new effective screen name
     * @param theRole       the new effective role
     */
    void setActAsUserInfo(final String theUserId, final String theScreenName, final ERole theRole) {

        if ("MPE".equals(theUserId)) {
            throw new IllegalArgumentException("Cannot act as 'MPE' user.");
        }

        if (theUserId != null && theUserId.equals(this.userId)) {
            this.actAsUserId = null;
            this.actAsScreenName = null;
        } else {
            this.actAsUserId = theUserId;
            this.actAsScreenName = theScreenName;
        }

        if (theRole != null && theRole == this.role) {
            this.actAsRole = null;
        } else {
            this.actAsRole = theRole;
        }
    }

    /**
     * Sets the time offset (milliseconds) to apply to this session, allowing it to present web pages as they would
     * appear at any point in the past or future.
     *
     * @param theTimeOffset the new time offset
     */
    void setTimeOffset(final long theTimeOffset) {

        this.timeOffset = theTimeOffset;
    }

    /**
     * Clears the effective user ID under which this session will act.
     */
    void clearActAsUserInfo() {

        this.actAsUserId = null;
        this.actAsScreenName = null;
        this.actAsRole = null;
    }

    /**
     * Sets the last action performed on the session, sets the last activity timestamp to the current time, and updates
     * the timeout.
     */
    void touch() {

        this.lastActivity = Instant.now();
        this.timeout = Instant.now().plusMillis(TIMEOUT);
    }

    /**
     * Sets the currently selected role.
     *
     * @param newRole the new role
     */
    public void setRole(final ERole newRole) {

        this.role = newRole;
    }

    /**
     * Gets the ID of the logged-in user.
     *
     * @return the user ID
     */
    public String getUserId() {

        return this.userId;
    }

    /**
     * Gets the first name of the logged-in user.
     *
     * @return the first name
     */
    public String getFirstName() {

        return this.firstName;
    }

    /**
     * Gets the last name of the logged-in user.
     *
     * @return the last name
     */
    public String getLastName() {

        return this.lastName;
    }

    /**
     * Gets the screen name of the logged-in user.
     *
     * @return the screen name
     */
    public String getScreenName() {

        return this.screenName;
    }

    /**
     * Gets the date/time when the session had its last activity.
     *
     * @return the last activity date/time
     */
    Instant getLastActivity() {

        return this.lastActivity;
    }

    /**
     * Gets the date/time when the session will time out if no activity occurs.
     *
     * @return the timeout
     */
    public Instant getTimeout() {

        return this.timeout;
    }

    /**
     * Gets the currently selected role.
     *
     * @return the role
     */
    public ERole getRole() {

        return this.role;
    }

    /**
     * Gets the ID of the effective user.
     *
     * @return the effective user ID
     */
    public String getActAsUserId() {

        return this.actAsUserId;
    }

    /**
     * Gets the first name of the effective user.
     *
     * @return the effective first name
     */
    String getActAsFirstName() {

        return this.actAsFirstName;
    }

    /**
     * Gets the last name of the effective user.
     *
     * @return the effective last name
     */
    String getActAsLastName() {

        return this.actAsLastName;
    }

    /**
     * Gets the screen name of the effective user.
     *
     * @return the effective screen name
     */
    String getActAsScreenName() {

        return this.actAsScreenName;
    }

    /**
     * Gets the role of the effective user.
     *
     * @return the effective role
     */
    ERole getActAsRole() {

        return this.actAsRole;
    }

    /**
     * Gets the time offset for this session.
     *
     * @return the time offset
     */
    long getTimeOffset() {

        return this.timeOffset;
    }

    /**
     * Tests whether the session has expired.
     *
     * @return {@code true} if the session has expired; {@code false} otherwise
     */
    public boolean isTimedOut() {

        return this.timeout.toEpochMilli() < System.currentTimeMillis();
    }

    /**
     * Generates an immutable representation of this session.
     *
     * @return the immutable representation
     */
    ImmutableSessionInfo makeImmutable() {

        return new ImmutableSessionInfo(this);
    }

    /**
     * Restores the timer and last action fields, to be used when loading persisted sessions from an XML file.
     *
     * @param theEstablished     the date/time the session was established
     * @param theLastActivity    the date/time of the last activity on the session
     * @param theTimeout         the date/time the session times out
     * @param theTimeOffset      the time offset of the session
     * @param theActAsUser       the ID of the user for whom the session is acting
     * @param theActAsFirstName  the first name of the user for whom the session is acting
     * @param theActAsLastName   the last name of the user for whom the session is acting
     * @param theActAsScreenName the screen name of the user for whom the session is acting
     * @param theActAsRole       the role of the user for whom the session is acting
     */
    void restoreState(final Instant theEstablished, final Instant theLastActivity, final Instant theTimeout,
                      final Integer theTimeOffset, final String theActAsUser, final String theActAsFirstName,
                      final String theActAsLastName, final String theActAsScreenName, final String theActAsRole) {

        this.established = theEstablished;
        this.lastActivity = theLastActivity;
        this.timeout = theTimeout;
        this.timeOffset = (long) theTimeOffset.intValue();

        this.actAsUserId = theActAsUser;
        this.actAsFirstName = theActAsFirstName;
        this.actAsLastName = theActAsLastName;
        this.actAsScreenName = theActAsScreenName;
        if (theActAsRole != null) {
            this.actAsRole = ERole.fromAbbrev(theActAsRole);
        }
    }

    /**
     * Appends the XML representation of this live session to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder xml) {

        xml.openElement(0, XML_TAG);
        xml.addAttribute("id", this.loginSessionId, 0);
        xml.addAttribute("tag", Long.toString(this.loginSessionTag), 0);
        xml.addAttribute("auth-type", this.authType, 0);
        xml.addAttribute("established", this.established, 2);

        xml.addAttribute("user-id", this.userId, 0);
        xml.addAttribute("first-name", this.firstName, 0);
        xml.addAttribute("last-name", this.lastName, 0);
        xml.addAttribute("screen-name", this.screenName, 0);

        xml.addAttribute("last-activity", this.lastActivity, 2);
        xml.addAttribute("timeout", this.timeout, 0);
        xml.addAttribute("role", this.role == null ? null : this.role.abbrev, 2);
        xml.addAttribute("time-offset", Long.toString(this.timeOffset), 0);

        xml.addAttribute("act-as-user", this.actAsUserId, 2);
        xml.addAttribute("act-as-first-name", this.actAsFirstName, 0);
        xml.addAttribute("act-as-last-name", this.actAsLastName, 0);
        xml.addAttribute("act-as-name", this.actAsScreenName, 0);
        xml.addAttribute("act-as-role", this.actAsRole == null ? null : this.actAsRole.abbrev, 0);

        xml.closeEmptyElement(true);
    }

}
