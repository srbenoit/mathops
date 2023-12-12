package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawETextCourse} class.
 */
final class TestRawEtextCourse {

    /** A field name. */
    private static final String TEST_ETEXT_ID = "PACe";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "etext_id=PACe",
            "course=M 117");

    /**
     * Constructs a new {@code IvtRawETextCourse}.
     */
    TestRawEtextCourse() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawEtextCourse obj = new RawEtextCourse(TEST_ETEXT_ID, TEST_COURSE);

        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawEtextCourse obj = new RawEtextCourse(TEST_ETEXT_ID, TEST_COURSE);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawEtextCourse obj = RawEtextCourse.parse(EXPECT_SER99);

        assertEquals(TEST_ETEXT_ID, obj.etextId, "Invalid etext_id value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
    }
}
