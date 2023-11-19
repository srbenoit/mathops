package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawCuobjective} class.
 */
final class TestRawCuobjective {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022); // $NON-NLS-1$

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(3);

    /** A field name. */
    private static final Integer TEST_OBJECTIVE = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_LESSON_ID = "ALG_1.2";

    /** A field name. */
    private static final String TEST_LESSON_NBR = "3.1.1A";

    /** A field name. */
    private static final LocalDate TEST_START_DT = LocalDate.of(2023, 1, 2);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "course=M 117",
            "unit=3",
            "objective=1",
            "lesson_id=ALG_1.2",
            "lesson_nbr=3.1.1A",
            "start_dt=2023-01-02");

    /**
     * Constructs a new {@code IvtRawCuobjective}.
     */
    TestRawCuobjective() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawCuobjective obj = new RawCuobjective(TEST_TERM, TEST_COURSE, TEST_UNIT,
                TEST_OBJECTIVE, TEST_LESSON_ID, TEST_LESSON_NBR, TEST_START_DT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after constructor");
        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after constructor");
        assertEquals(TEST_LESSON_NBR, obj.lessonNbr, "Invalid lesson_nbr value after constructor");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawCuobjective obj = new RawCuobjective(TEST_TERM, TEST_COURSE, TEST_UNIT,
                TEST_OBJECTIVE, TEST_LESSON_ID, TEST_LESSON_NBR, TEST_START_DT);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawCuobjective obj = RawCuobjective.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after deserialization");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after deserialization");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after deserialization");
        assertEquals(TEST_OBJECTIVE, obj.objective, "Invalid objective value after deserialization");
        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after deserialization");
        assertEquals(TEST_LESSON_NBR, obj.lessonNbr, "Invalid lesson_nbr value after deserialization");
        assertEquals(TEST_START_DT, obj.startDt, "Invalid start_dt value after deserialization");
    }
}
