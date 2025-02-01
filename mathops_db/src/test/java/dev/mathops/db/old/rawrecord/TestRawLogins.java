package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawLogins} class.
 */
final class TestRawLogins {

    /** A field name. */
    private static final String TEST_USER_ID = "888888888";

    /** A field name. */
    private static final String TEST_USER_TYPE = "STU";

    /** A field name. */
    private static final String TEST_USER_NAME = "jdoe";

    /** A field name. */
    private static final String TEST_STORED_KEY = "ABCDEFGH01234567";

    /** A field name. */
    private static final String TEST_SERVER_KEY = "ZYXWVUTS98765432";

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_CREATED = LocalDateTime.of(2021, 1, 2, 3, 4, 5);

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_EXPIRES = LocalDateTime.of(2022, 2, 3, 4, 5, 6);

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_LAST_LOGIN = LocalDateTime.of(2023, 3, 4, 5, 6, 7);

    /** A field name. */
    private static final String TEST_FORCE_PW_CHANGE = "N";

    /** A field name. */
    private static final String TEST_EMAIL = "jdoe@colostate.edu";

    /** A field name. */
    private static final String TEST_SALT = "SALTSALTSALT";

    /** A field name. */
    private static final Integer TEST_NBR_INVALID_ATMPTS = Integer.valueOf(3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "user_id=888888888",
            "user_type=STU",
            "user_name=jdoe",
            "stored_key=ABCDEFGH01234567",
            "server_key=ZYXWVUTS98765432",
            "dtime_created=2021-01-02T03:04:05",
            "dtime_expires=2022-02-03T04:05:06",
            "dtime_last_login=2023-03-04T05:06:07",
            "force_pw_change=N",
            "email=jdoe@colostate.edu",
            "salt=SALTSALTSALT",
            "nbr_invalid_atmpts=3");

    /**
     * Constructs a new {@code IvtRawLogins}.
     */
    TestRawLogins() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawLogins obj =
                new RawLogins(TEST_USER_ID, TEST_USER_TYPE, TEST_USER_NAME, TEST_STORED_KEY,
                        TEST_SERVER_KEY, TEST_DTIME_CREATED, TEST_DTIME_EXPIRES, TEST_DTIME_LAST_LOGIN,
                        TEST_FORCE_PW_CHANGE, TEST_EMAIL, TEST_SALT, TEST_NBR_INVALID_ATMPTS);

        assertEquals(TEST_USER_ID, obj.userId, "Invalid user_id value after constructor");
        assertEquals(TEST_USER_TYPE, obj.userType, "Invalid user_type value after constructor");
        assertEquals(TEST_USER_NAME, obj.userName, "Invalid user_name value after constructor");
        assertEquals(TEST_STORED_KEY, obj.storedKey, "Invalid stored_key value after constructor");
        assertEquals(TEST_SERVER_KEY, obj.serverKey, "Invalid server_key value after constructor");
        assertEquals(TEST_DTIME_CREATED, obj.dtimeCreated, "Invalid dtime_created value after constructor");
        assertEquals(TEST_DTIME_EXPIRES, obj.dtimeExpires, "Invalid dtime_expires value after constructor");
        assertEquals(TEST_DTIME_LAST_LOGIN, obj.dtimeLastLogin, "Invalid dtime_last_login value after constructor");
        assertEquals(TEST_FORCE_PW_CHANGE, obj.forcePwChange, "Invalid force_pw_change value after constructor");
        assertEquals(TEST_EMAIL, obj.email, "Invalid email value after constructor");
        assertEquals(TEST_SALT, obj.salt, "Invalid salt value after constructor");
        assertEquals(TEST_NBR_INVALID_ATMPTS, obj.nbrInvalidAtmpts,
                "Invalid nbr_invalid_atmpts value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawLogins obj =
                new RawLogins(TEST_USER_ID, TEST_USER_TYPE, TEST_USER_NAME, TEST_STORED_KEY,
                        TEST_SERVER_KEY, TEST_DTIME_CREATED, TEST_DTIME_EXPIRES, TEST_DTIME_LAST_LOGIN,
                        TEST_FORCE_PW_CHANGE, TEST_EMAIL, TEST_SALT, TEST_NBR_INVALID_ATMPTS);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
