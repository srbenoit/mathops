package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StudentPreferenceRec} class.
 */
public final class TestStudentPreferenceRec {

    /** A field name. */
    private static final String TEST_STUDENT_ID = "876543210";

    /** A field name. */
    private static final String TEST_PREF_KEY = "KEY001";

    /** A field name. */
    private static final Integer TEST_PREF_VALUE = Integer.valueOf(6);

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "student_id=876543210",
            "pref_key=KEY001",
            "pref_value=6");

    /**
     * Constructs a new {@code TestStudentPreferenceRec}.
     */
    TestStudentPreferenceRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StudentPreferenceRec obj = new StudentPreferenceRec(TEST_STUDENT_ID, TEST_PREF_KEY, TEST_PREF_VALUE);

        assertEquals(TEST_STUDENT_ID, obj.studentId, "Invalid student ID value after constructor");
        assertEquals(TEST_PREF_KEY, obj.prefKey, "Invalid pref key value after constructor");
        assertEquals(TEST_PREF_VALUE, obj.prefValue, "Invalid pref value value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StudentPreferenceRec obj = new StudentPreferenceRec(TEST_STUDENT_ID, TEST_PREF_KEY, TEST_PREF_VALUE);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}