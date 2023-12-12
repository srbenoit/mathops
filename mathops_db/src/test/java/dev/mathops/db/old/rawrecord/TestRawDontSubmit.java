package dev.mathops.db.old.rawrecord;

import dev.mathops.core.CoreConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the {@code RawDontSubmit} class.
 */
final class TestRawDontSubmit {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER1 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            CoreConstants.EMPTY,
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER2 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            "course=M 117",
            CoreConstants.EMPTY);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER3 = String.join(RecBase.DIVIDER,
            CoreConstants.EMPTY,
            CoreConstants.EMPTY,
            "sect=001");

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER4 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "course=M 117",
            "sect=001");

    /**
     * Constructs a new {@code IvtRawDontSubmit}.
     */
    TestRawDontSubmit() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawDontSubmit obj = new RawDontSubmit(TEST_TERM, TEST_COURSE, TEST_SECT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term key value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0002() {

        final RawDontSubmit obj = new RawDontSubmit(TEST_TERM, null, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER1, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0003() {

        final RawDontSubmit obj = RawDontSubmit.parse(EXPECT_SER1);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term key value after deserialization");
        assertNull(obj.course, "Invalid course value after deserialization");
        assertNull(obj.sect, "Invalid sect value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0004() {

        final RawDontSubmit obj = new RawDontSubmit(null, TEST_COURSE, null);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER2, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0005() {

        final RawDontSubmit obj = RawDontSubmit.parse(EXPECT_SER2);

        assertNull(obj.termKey, "Invalid term key value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertNull(obj.sect, "Invalid sect value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0006() {

        final RawDontSubmit obj = new RawDontSubmit(null, null, TEST_SECT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER3, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0007() {

        final RawDontSubmit obj = RawDontSubmit.parse(EXPECT_SER3);

        assertNull(obj.termKey, "Invalid term key value after deserialization");
        assertNull(obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after deserialization");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0008() {

        final RawDontSubmit obj = new RawDontSubmit(TEST_TERM, TEST_COURSE, TEST_SECT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER4, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0009() {

        final RawDontSubmit obj = RawDontSubmit.parse(EXPECT_SER4);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term key value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after deserialization");
    }
}
