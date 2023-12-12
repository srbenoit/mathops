package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawPlcFee} class.
 */
final class TestRawPrereq {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final String TEST_COURSE = "M 126";

    /** A field name. */
    private static final String TEST_PREREQUISITE = RawRecordConstants.M125;

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "course=M 126",
            "prerequisite=M 125");

    /**
     * Constructs a new {@code IvtRawPrereq}.
     */
    TestRawPrereq() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawPrereq obj = new RawPrereq(TEST_TERM, TEST_COURSE, TEST_PREREQUISITE);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");

        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");

        assertEquals(TEST_PREREQUISITE, obj.prerequisite, "Invalid prerequisite value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawPrereq obj = new RawPrereq(TEST_TERM, TEST_COURSE, TEST_PREREQUISITE);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawPrereq obj = RawPrereq.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_PREREQUISITE, obj.prerequisite, "Invalid prerequisite value after deserialization");
    }
}
