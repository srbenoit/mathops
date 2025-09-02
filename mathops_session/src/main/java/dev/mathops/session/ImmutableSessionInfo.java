package dev.mathops.session;

import dev.mathops.db.field.ERole;

import java.time.Instant;
import java.time.ZonedDateTime;

/**
 * An immutable container of information on a single active login session. This is the version returned to users - a
 * mutable object is used internally to support efficient updates to dynamic fields like the last activity, last action,
 * and timeout.
 */
public final class ImmutableSessionInfo {

    /** The login session ID. */
    public final String loginSessionId;

    /** The login session tag (unique per session - short and monotone but guessable). */
    public final long loginSessionTag;

    /** The ID of the logged-in user. */
    public final String userId;

    /** The first name of the logged-in user. */
    private final String firstName;

    /** The last name of the logged-in user. */
    public final String lastName;

    /** The screen name of the logged-in user. */
    public final String screenName;

    /** The currently selected role. */
    public final ERole role;

    /** The date/time when the session had its last activity. */
    public final Instant lastActivity;

    /** The date/time when the session will time out if no activity occurs. */
    private final Instant timeout;

    /** The ID of the effective user. */
    public final String actAsUserId;

    /** The first name of the effective user. */
    private final String actAsFirstName;

    /** The last name of the effective user. */
    private final String actAsLastName;

    /** The screen name of the effective user. */
    final String actAsScreenName;

    /** The effective role. */
    private final ERole actAsRole;

    /** The time offset to apply to this session. */
    public final long timeOffset;

    /**
     * Constructs a new {@code ImmutableSessionInfo} from a {@code LiveSessionInfo}.
     *
     * @param live the {@code LiveSessionInfo} from which to copy
     */
    public ImmutableSessionInfo(final LiveSessionInfo live) {

        this.loginSessionId = live.loginSessionId;
        this.loginSessionTag = live.loginSessionTag;

        this.userId = live.getUserId();
        this.firstName = live.getFirstName();
        this.lastName = live.getLastName();
        this.screenName = live.getScreenName();
        this.role = live.getRole();

        this.lastActivity = live.getLastActivity();
        this.timeout = live.getTimeout();

        this.actAsUserId = live.getActAsUserId();
        this.actAsFirstName = live.getActAsFirstName();
        this.actAsLastName = live.getActAsLastName();
        this.actAsScreenName = live.getActAsScreenName();
        this.actAsRole = live.getActAsRole();

        this.timeOffset = live.getTimeOffset();
    }

    /**
     * Gets the effective user ID, which is the "act as" user ID if one is set, or the user ID if not.
     *
     * @return the effective user ID
     */
    public String getEffectiveUserId() {

        return (this.actAsUserId == null) ? this.userId : this.actAsUserId;
    }

    /**
     * Gets the effective first name, which is the "act as" first name if one is set, or the first name if not.
     *
     * @return the effective first name
     */
    public String getEffectiveFirstName() {

        return (this.actAsFirstName == null) ? this.firstName : this.actAsFirstName;
    }

    /**
     * Gets the effective last name, which is the "act as" last name if one is set, or the last name if not.
     *
     * @return the effective last name
     */
    public String getEffectiveLastName() {

        return (this.actAsLastName == null) ? this.lastName : this.actAsLastName;
    }

    /**
     * Gets the effective screen name, which is the "act as" screen name if one is set, or the screen name if not.
     *
     * @return the effective screen name
     */
    public String getEffectiveScreenName() {

        return (this.actAsScreenName == null) ? this.screenName : this.actAsScreenName;
    }

    /**
     * Gets the effective role, which is the "act as" role if one is set, or the role if not.
     *
     * @return the effective role
     */
    public ERole getEffectiveRole() {

        return (this.actAsRole == null) ? this.role : this.actAsRole;
    }

    /**
     * Gets the current date/time as seen by the session.
     *
     * @return a {@code ZonedDateTime} representing the current date/time adjusted by the session time offset (the
     *         current date/time as the session should see it)
     */
    public ZonedDateTime getNow() {

        return ZonedDateTime.now().plusNanos(this.timeOffset * 1000000L);
    }

    /**
     * Gets the duration until this session will be timed out and purged if no activity is detected.
     *
     * @return the duration, in milliseconds
     */
    public long getTimeUntilPurge() {

        return this.timeout.toEpochMilli() - System.currentTimeMillis();
    }
}
