package dev.mathops.db.old.rawrecord;

import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawExam} class.
 */
final class TestRawExam {

    /** A field name. */
    private static final String TEST_VERSION = "174UE";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final Integer TEST_UNIT = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_VSN_EXPLT = "Q#)$(@";

    /** A field name. */
    private static final String TEST_TITLE = "Unit 4 Exam";

    /** A field name. */
    private static final String TEST_TREE_REF = "a.b.c.d";

    /** A field name. */
    private static final String TEST_EXAM_TYPE = "U";

    /** A field name. */
    private static final LocalDate TEST_ACTIVE_DT = LocalDate.of(2022, 1, 2);

    /** A field name. */
    private static final LocalDate TEST_PULL_DT = LocalDate.of(2023, 2, 3);

    /** A field name. */
    private static final String TEST_BUTTON_LABEL = "Unit 4 Exam";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "version=174UE",
            "course=M 117",
            "unit=4",
            "vsn_explt=Q#)$(@",
            "title=Unit 4 Exam",
            "tree_ref=a.b.c.d",
            "exam_type=U",
            "active_dt=2022-01-02",
            "pull_dt=2023-02-03",
            "button_label=Unit 4 Exam");

    /**
     * Constructs a new {@code IvtRawExam}.
     */
    TestRawExam() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawExam obj =
                new RawExam(TEST_VERSION, TEST_COURSE, TEST_UNIT, TEST_VSN_EXPLT, TEST_TITLE,
                        TEST_TREE_REF, TEST_EXAM_TYPE, TEST_ACTIVE_DT, TEST_PULL_DT, TEST_BUTTON_LABEL);

        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_UNIT, obj.unit, "Invalid unit value after constructor");
        assertEquals(TEST_VSN_EXPLT, obj.vsnExplt, "Invalid vsn_explt value after constructor");
        assertEquals(TEST_TITLE, obj.title, "Invalid title value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after constructor");
        assertEquals(TEST_EXAM_TYPE, obj.examType, "Invalid exam_type value after constructor");
        assertEquals(TEST_ACTIVE_DT, obj.activeDt, "Invalid active_dt value after constructor");
        assertEquals(TEST_PULL_DT, obj.pullDt, "Invalid pull_dt value after constructor");
        assertEquals(TEST_BUTTON_LABEL, obj.buttonLabel, "Invalid button_label value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawExam obj =
                new RawExam(TEST_VERSION, TEST_COURSE, TEST_UNIT, TEST_VSN_EXPLT, TEST_TITLE,
                        TEST_TREE_REF, TEST_EXAM_TYPE, TEST_ACTIVE_DT, TEST_PULL_DT, TEST_BUTTON_LABEL);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
