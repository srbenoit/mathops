package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawRemoteMpe} class.
 */
final class TestRawRemoteMpe {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022);

    /** A field name. */
    private static final TermKey TEST_APLN_TERM = new TermKey(ETermName.SPRING, 2023);

    /** A field name. */
    private static final String TEST_COURSE = "M 100P";

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_END_DT = LocalDate.of(2023, 2, 3);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "apln_term=Spring, 2023",
            "course=M 100P",
            "start_dt=2022-01-02",
            "end_dt=2023-02-03");

    /**
     * Constructs a new {@code IvtRawRemoteMpe }.
     */
    TestRawRemoteMpe() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawRemoteMpe obj = new RawRemoteMpe(TEST_TERM, TEST_APLN_TERM, TEST_COURSE, TEST_START_DT, TEST_END_DT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_APLN_TERM, obj.aplnTerm, "Invalid apln_term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after constructor");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid end_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawRemoteMpe obj =
                new RawRemoteMpe(TEST_TERM, TEST_APLN_TERM, TEST_COURSE, TEST_START_DT, TEST_END_DT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0099() {

        final RawRemoteMpe obj = RawRemoteMpe.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_APLN_TERM, obj.aplnTerm, "Invalid apln_term value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after deserialization");
        assertEquals(TEST_END_DT, obj.endDt, "Invalid end_dt value after deserialization");
    }
}
