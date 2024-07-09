package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawUserClearance} class.
 */
final class TestRawUserClearance {

    /** A field name. */
    private static final String TEST_LOGIN = "a";

    /** A field name. */
    private static final String TEST_CLEAR_FUNCTION = "b";

    /** A field name. */
    private static final Integer TEST_CLEAR_TYPE = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_CLEAR_PASSWD = "c";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "login=a",
            "clear_function=b",
            "clear_type=2",
            "clear_passwd=c");

    /**
     * Constructs a new {@code IvtRawUserClearance}.
     */
    TestRawUserClearance() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawUserClearance obj = new RawUserClearance(TEST_LOGIN, TEST_CLEAR_FUNCTION,
                TEST_CLEAR_TYPE, TEST_CLEAR_PASSWD);

        assertEquals(TEST_LOGIN, obj.login, "Invalid login value after constructor");
        assertEquals(TEST_CLEAR_FUNCTION, obj.clearFunction, "Invalid clear_function value after constructor");
        assertEquals(TEST_CLEAR_TYPE, obj.clearType, "Invalid clear_type value after constructor");
        assertEquals(TEST_CLEAR_PASSWD, obj.clearPasswd, "Invalid clear_passwd value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawUserClearance obj = new RawUserClearance(TEST_LOGIN, TEST_CLEAR_FUNCTION,
                TEST_CLEAR_TYPE, TEST_CLEAR_PASSWD);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
