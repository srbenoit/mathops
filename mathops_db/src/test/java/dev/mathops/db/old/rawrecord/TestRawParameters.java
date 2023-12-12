package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawParameters} class.
 */
final class TestRawParameters {

    /** A field name. */
    private static final String TEST_PGM_NAME = "FOO";

    /** A field name. */
    private static final String TEST_PARM1 = "p1";

    /** A field name. */
    private static final String TEST_PARM2 = "p2";

    /** A field name. */
    private static final String TEST_PARM3 = "p3";

    /** A field name. */
    private static final String TEST_PARM4 = "p4";

    /** A field name. */
    private static final String TEST_PARM5 = "p5";

    /** A field name. */
    private static final String TEST_PARM6 = "p6";

    /** A field name. */
    private static final String TEST_PARM7 = "p7";

    /** A field name. */
    private static final String TEST_PARM8 = "p8";

    /** A field name. */
    private static final String TEST_PARM9 = "p9";

    /** A field name. */
    private static final LocalDate TEST_PARM10 = LocalDate.of(2023, 1, 2);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "pgm_name=FOO",
            "parm1=p1",
            "parm2=p2",
            "parm3=p3",
            "parm4=p4",
            "parm5=p5",
            "parm6=p6",
            "parm7=p7",
            "parm8=p8",
            "parm9=p9",
            "parm10=2023-01-02");

    /**
     * Constructs a new {@code IvtRawParameters}.
     */
    TestRawParameters() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawParameters obj =
                new RawParameters(TEST_PGM_NAME, TEST_PARM1, TEST_PARM2, TEST_PARM3, TEST_PARM4,
                        TEST_PARM5, TEST_PARM6, TEST_PARM7, TEST_PARM8, TEST_PARM9, TEST_PARM10);

        assertEquals(TEST_PGM_NAME, obj.pgmName, "Invalid pgm_name value after constructor");
        assertEquals(TEST_PARM1, obj.parm1, "Invalid parm1 value after constructor");
        assertEquals(TEST_PARM2, obj.parm2, "Invalid parm2 value after constructor");
        assertEquals(TEST_PARM3, obj.parm3, "Invalid parm3 value after constructor");
        assertEquals(TEST_PARM4, obj.parm4, "Invalid parm4 value after constructor");
        assertEquals(TEST_PARM5, obj.parm5, "Invalid parm5 value after constructor");
        assertEquals(TEST_PARM6, obj.parm6, "Invalid parm6 value after constructor");
        assertEquals(TEST_PARM7, obj.parm7, "Invalid parm7 value after constructor");
        assertEquals(TEST_PARM8, obj.parm8, "Invalid parm8 value after constructor");
        assertEquals(TEST_PARM9, obj.parm9, "Invalid parm9 value after constructor");
        assertEquals(TEST_PARM10, obj.parm10, "Invalid parm10 value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawParameters obj =
                new RawParameters(TEST_PGM_NAME, TEST_PARM1, TEST_PARM2, TEST_PARM3, TEST_PARM4,
                        TEST_PARM5, TEST_PARM6, TEST_PARM7, TEST_PARM8, TEST_PARM9, TEST_PARM10);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawParameters obj = RawParameters.parse(EXPECT_SER99);

        assertEquals(TEST_PGM_NAME, obj.pgmName, "Invalid pgm_name value after deserialization");
        assertEquals(TEST_PARM1, obj.parm1, "Invalid parm1 value after deserialization");
        assertEquals(TEST_PARM2, obj.parm2, "Invalid parm2 value after deserialization");
        assertEquals(TEST_PARM3, obj.parm3, "Invalid parm3 value after deserialization");
        assertEquals(TEST_PARM4, obj.parm4, "Invalid parm4 value after deserialization");
        assertEquals(TEST_PARM5, obj.parm5, "Invalid parm5 value after deserialization");
        assertEquals(TEST_PARM6, obj.parm6, "Invalid parm6 value after deserialization");
        assertEquals(TEST_PARM7, obj.parm7, "Invalid parm7 value after deserialization");
        assertEquals(TEST_PARM8, obj.parm8, "Invalid parm8 value after deserialization");
        assertEquals(TEST_PARM9, obj.parm9, "Invalid parm9 value after deserialization");
        assertEquals(TEST_PARM10, obj.parm10, "Invalid parm10 value after deserialization");
    }
}
