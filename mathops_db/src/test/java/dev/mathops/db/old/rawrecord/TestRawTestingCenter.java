package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

/**
 * Tests for the {@code RawTestingCenter} class.
 */
final class TestRawTestingCenter {

    /** A field name. */
    private static final String TEST_TESTING_CENTER_ID = "TC1";

    /** A field name. */
    private static final String TEST_TC_NAME = "Main";

    /** A field name. */
    private static final String TEST_ADDRES_1 = "A1";

    /** A field name. */
    private static final String TEST_ADDRES_2 = "A2";

    /** A field name. */
    private static final String TEST_ADDRES_3 = "A3";

    /** A field name. */
    private static final String TEST_CITY = "CC";

    /** A field name. */
    private static final String TEST_STATE = "SS";

    /** A field name. */
    private static final String TEST_ZIP_CODE = "ZZ";

    /** A field name. */
    private static final String TEST_ACTIVE = "Y";

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_CREATED = LocalDateTime.of(2001, 2, 3, 4, 5, 6);

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_APPROVED = LocalDateTime.of(2002, 3, 4, 5, 6, 7);

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_DENIED = LocalDateTime.of(2003, 4, 5, 6, 7, 8);

    /** A field name. */
    private static final LocalDateTime TEST_DTIME_REVOKED = LocalDateTime.of(2004, 5, 6, 7, 8, 9);

    /** A field name. */
    private static final String TEST_IS_REMOTE = "N";

    /** A field name. */
    private static final String TEST_IS_PROCTORED = "Q";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "testing_center_id=TC1",
            "tc_name=Main",
            "addres_1=A1",
            "addres_2=A2",
            "addres_3=A3",
            "city=CC",
            "state=SS",
            "zip_code=ZZ",
            "active=Y",
            "dtime_created=2001-02-03T04:05:06",
            "dtime_approved=2002-03-04T05:06:07",
            "dtime_denied=2003-04-05T06:07:08",
            "dtime_revoked=2004-05-06T07:08:09",
            "is_remote=N",
            "is_proctored=Q");

    /**
     * Constructs a new {@code IvtRawTestingCenter}.
     */
    TestRawTestingCenter() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawTestingCenter obj = new RawTestingCenter(TEST_TESTING_CENTER_ID, TEST_TC_NAME,
                TEST_ADDRES_1, TEST_ADDRES_2, TEST_ADDRES_3, TEST_CITY, TEST_STATE, TEST_ZIP_CODE,
                TEST_ACTIVE, TEST_DTIME_CREATED, TEST_DTIME_APPROVED, TEST_DTIME_DENIED,
                TEST_DTIME_REVOKED, TEST_IS_REMOTE, TEST_IS_PROCTORED);

        assertEquals(TEST_TESTING_CENTER_ID, obj.testingCenterId, "Invalid testing_center_id value after constructor");
        assertEquals(TEST_TC_NAME, obj.tcName, "Invalid tc_name value after constructor");
        assertEquals(TEST_ADDRES_1, obj.addres1, "Invalid addres1 value after constructor");
        assertEquals(TEST_ADDRES_2, obj.addres2, "Invalid addres2 value after constructor");
        assertEquals(TEST_ADDRES_3, obj.addres3, "Invalid addres3 value after constructor");
        assertEquals(TEST_CITY, obj.city, "Invalid city value after constructor");
        assertEquals(TEST_STATE, obj.state, "Invalid state value after constructor");
        assertEquals(TEST_ZIP_CODE, obj.zipCode, "Invalid zip_code value after constructor");
        assertEquals(TEST_ACTIVE, obj.active, "Invalid active value after constructor");
        assertEquals(TEST_DTIME_CREATED, obj.dtimeCreated, "Invalid dtime_created value after constructor");
        assertEquals(TEST_DTIME_APPROVED, obj.dtimeApproved, "Invalid dtime_approved value after constructor");
        assertEquals(TEST_DTIME_DENIED, obj.dtimeDenied, "Invalid dtime_denied value after constructor");
        assertEquals(TEST_DTIME_REVOKED, obj.dtimeRevoked, "Invalid dtime_revoked value after constructor");
        assertEquals(TEST_IS_REMOTE, obj.isRemote, "Invalid is_remote value after constructor");
        assertEquals(TEST_IS_PROCTORED, obj.isProctored, "Invalid is_proctored value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawTestingCenter obj = new RawTestingCenter(TEST_TESTING_CENTER_ID, TEST_TC_NAME,
                TEST_ADDRES_1, TEST_ADDRES_2, TEST_ADDRES_3, TEST_CITY, TEST_STATE, TEST_ZIP_CODE,
                TEST_ACTIVE, TEST_DTIME_CREATED, TEST_DTIME_APPROVED, TEST_DTIME_DENIED,
                TEST_DTIME_REVOKED, TEST_IS_REMOTE, TEST_IS_PROCTORED);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
