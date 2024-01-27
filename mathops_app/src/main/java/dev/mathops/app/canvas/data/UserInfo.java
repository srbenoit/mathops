package dev.mathops.app.canvas.data;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.json.JSONObject;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * User information gathered during login from the JSON response to a query to {@code /api/v1/users/self}. The format of
 * the response is shown below.
 *
 * <pre>
 * {
 *   "id":1,                                  // The ID of the user.
 *   "name":"Steve Benoit",                   // The name of the user.
 *   "sortable_name":"Benoit, Steve",         // The name of the user that is should be used for
 *                                            //  sorting groups of users, such as in the grade book.
 *   "last_name":null,                        // The last name of the user.
 *   "first_name":null,                       // The first name of the user.
 *   "created_at":"2022-06-29T12:33:41-06:00",// The creation date
 *   "short_name":"Steve Benoit",             // A short name the user has selected, for use in
 *                                            //  conversations or other less formal places through
 *                                            //  the site.
 *   "sis_user_id":null,                      // The SIS ID associated with the user. This field is
 *                                            //  only included if the user came from a SIS import
 *                                            //  and has permissions to view SIS information.
 *   "sis_import_id":null,                    // The id of the SIS import. This field is only
 *                                            //  included if the user came from a SIS import and
 *                                            //  has permissions to manage SIS information.
 *   "integration_id":null,                   // The integration_id associated with the user. This
 *                                            //  field is only included if the user came from a
 *                                            //  SIS import and has permissions to view SIS
 *                                            //  information.
 *   "login_id":"steve.benoit@colostate.edu", // The unique login id for the user. This is what the
 *                                            //  user uses to log in to Canvas.
 *   "avatar_url":null,                       // If avatars are enabled, this field will be included
 *                                            //  and contain a url to retrieve the user's avatar.
 *   "avatar_state":null,                     // Optional: If avatars are enabled and caller is
 *                                            //  admin, this field can be requested and will
 *                                            //  contain the current state of the user's avatar.
 *   "enrollments":null,                      // Optional: This field can be requested with certain
 *                                            //  API calls, and will return a list of the user's
 *                                            //  active enrollments.
 *   "email":"steve.benoit@colostate.edu",    // Optional: This field can be requested with
 *                                            //  certain API calls, and will return the user's
 *                                            //  primary email address.
 *   "locale":null,                           // Optional: This field can be requested with certain
 *                                            //  API calls, and will return the users locale in
 *                                            //  RFC 5646 format.
 *   "last_login":null,                       // Optional: This field is only returned in certain
 *                                            //  API calls, and will return a timestamp
 *                                            //  representing the last time the user logged in to
 *                                            //  canvas.
 *   "time_zone":null,                        // Optional: This field is only returned in certain
 *                                            //  API calls, and will return the IANA time zone name
 *                                            //  of the user's preferred timezone.
 *   "bio":null,                              // Optional: The user's bio.
 *   "effective_locale":"en",                 // Returned only for /api/v1/users/:id.  Holds the
 *                                            //  effective locale
 *   "permissions":{                          // Returned only for /api/v1/users/:id.  Holds a non-
 *       "can_update_name":true,              //  comprehensive list of permissions for the user
 *       "can_update_avatar":false,
 *       "limit_parent_app_web_access":false}
 * }
 * </pre>
 */
public final class UserInfo {

    /** The user ID. */
    private final Long userId;

    /** The username. */
    private final String name;

    /** The user last name. */
    private final String lastName;

    /** The user first name. */
    private final String firstName;

    /** The date/time the user was created. */
    private final ZonedDateTime createdAt;

    /** The user ID. */
    private final String sortableName;

    /** The user ID. */
    private final String shortName;

    /** The SIS user ID. */
    private final String sisUserId;

    /** The SIS import ID. */
    private final Long sisImportId;

    /** The integration ID. */
    private final String integrationId;

    /** The user ID. */
    private final String loginId;

    /** The avatar URL. */
    private final String avatarUrl;

    /** The avatar URL. */
    private final String avatarState;

    ///** The user's enrollments. */
    // public final List<Enrollment> enrollments;

    /** The user ID. */
    private final String email;

    /** The locale. */
    private final String locale;

    /** The date/time the user last logged in. */
    private final ZonedDateTime lastLogin;

    /** The user's time zone. */
    private final ZoneId timeZone;

    /** The user's bio. */
    private final String bio;

    /** The effective locale. */
    private final String effectiveLocale;

    /**
     * Constructs a new {@code UserInfo} from the JSON response from the server.
     *
     * @param json the parsed JSON response
     */
    public UserInfo(final JSONObject json) {

        final Double userIdDouble = json.getNumberProperty("id");
        if (userIdDouble == null) {
            this.userId = null;
        } else {
            this.userId = Long.valueOf(userIdDouble.longValue());
        }
        this.name = json.getStringProperty("name");
        this.sortableName = json.getStringProperty("sortable_name");
        this.lastName = json.getStringProperty("last_name");
        this.firstName = json.getStringProperty("first_name");

        final String creationDateStr = json.getStringProperty("created_at");
        if (creationDateStr == null) {
            this.createdAt = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(creationDateStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", creationDateStr, ex);
            }
            this.createdAt = parsed;
        }

        this.shortName = json.getStringProperty("short_name");
        this.sisUserId = json.getStringProperty("sis_user_id");

        final Double sisImportIdDouble = json.getNumberProperty("sis_import_id");
        if (sisImportIdDouble == null) {
            this.sisImportId = null;
        } else {
            this.sisImportId = Long.valueOf(sisImportIdDouble.longValue());
        }

        this.integrationId = json.getStringProperty("integration_id");
        this.loginId = json.getStringProperty("login_id");
        this.avatarUrl = json.getStringProperty("avatar_url");
        this.avatarState = json.getStringProperty("avatar_state");

        // final Object enrollmentsObj = json.getProperty("enrollments");
        // if (enrollmentsObj == null) {
        // this.enrollments = null;
        // } else {
        // this.enrollments = new ArrayList<>();
        //
        // // TODO: Extract child objects from enrollmentsObj (an array)
        // }

        this.email = json.getStringProperty("email");
        this.locale = json.getStringProperty("locale");

        final String lastLoginStr = json.getStringProperty("last_login");
        if (lastLoginStr == null) {
            this.lastLogin = null;
        } else {
            ZonedDateTime parsed = null;
            try {
                parsed = ZonedDateTime.parse(lastLoginStr);
            } catch (final DateTimeParseException ex) {
                Log.warning("Invalid date/time value: ", lastLoginStr, ex);
            }
            this.lastLogin = parsed;
        }

        final String timeZoneStr = json.getStringProperty("time_zone");
        if (timeZoneStr == null) {
            this.timeZone = null;
        } else {
            ZoneId parsed = null;
            try {
                parsed = ZoneId.of(timeZoneStr);
            } catch (final DateTimeException ex) {
                Log.warning("Invalid time zone value: ", lastLoginStr, ex);
            }
            this.timeZone = parsed;
        }

        this.bio = json.getStringProperty("bio");
        this.effectiveLocale = json.getStringProperty("effective_locale");
    }

    /**
     * Gets the user's display name (the short name if present; otherwise the full name)
     *
     * @return the display name
     */
    public String getDisplayName() {

        return this.shortName == null ? this.name : this.shortName;
    }
}
