package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * Tests for the {@code RawStcourse} class.
 */
final class TestRawStcourse {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022); // $NON-NLS-1$

    /** A field name. */
    private static final String TEST_STU_ID = "888888888";

    /** A field name. */
    private static final String TEST_COURSE = "M 117";

    /** A field name. */
    private static final String TEST_SECT = "001";

    /** A field name. */
    private static final Integer TEST_PACE_ORDER = Integer.valueOf(4);

    /** A field name. */
    private static final String TEST_OPEN_STATUS = "A";

    /** A field name. */
    private static final String TEST_GRADING_OPTION = "B";

    /** A field name. */
    private static final String TEST_COMPLETED = "C";

    /** A field name. */
    private static final Integer TEST_SCORE = Integer.valueOf(57);

    /** A field name. */
    private static final String TEST_COURSE_GRADE = "D";

    /** A field name. */
    private static final String TEST_PREREQ_SATIS = "E";

    /** A field name. */
    private static final String TEST_INIT_CLASS_ROLL = "F";

    /** A field name. */
    private static final String TEST_STU_PROVIDED = "G";

    /** A field name. */
    private static final String TEST_FINAL_CLASS_ROLL = "H";

    /** A field name. */
    private static final String TEST_EXAM_PLACED = "I";

    /** A field name. */
    private static final Integer TEST_ZERO_UNIT = Integer.valueOf(1);

    /** A field name. */
    private static final Float TEST_TIMEOUT_FACTOR = Float.valueOf(1.5f);

    /** A field name. */
    private static final String TEST_FORFEIT_I = "J";

    /** A field name. */
    private static final String TEST_I_IN_PROGRESS = "K";

    /** A field name. */
    private static final String TEST_I_COUNTED = "L";

    /** A field name. */
    private static final String TEST_CTRL_TEST = "M";

    /** A field name. */
    private static final LocalDate TEST_DEFERRED_F_DT = LocalDate.of(2023, 1, 2);

    /** A field name. */
    private static final Integer TEST_BYPASS_TIMEOUT = Integer.valueOf(5);

    /** A field name. */
    private static final String TEST_INSTRN_TYPE = "O";

    /** A field name. */
    private static final String TEST_REGISTRATION_STATUS = "P";

    /** A field name. */
    private static final LocalDate TEST_LAST_CLASS_ROLL_DT = LocalDate.of(2021, 5, 6);

    /** A field name. */
    private static final TermKey TEST_I_TERM = new TermKey(ETermName.SPRING, 2023); // $NON-NLS-1$

    /** A field name. */
    private static final LocalDate TEST_I_DEADLINE_DT = LocalDate.of(2022, 3, 4);

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "stu_id=888888888",
            "course=M 117",
            "sect=001",
            "pace_order=4",
            "open_status=A",
            "grading_option=B",
            "completed=C",
            "score=57",
            "course_grade=D",
            "prereq_satis=E",
            "init_class_roll=F",
            "stu_provided=G",
            "final_class_roll=H",
            "exam_placed=I",
            "zero_unit=1",
            "timeout_factor=1.5",
            "forfeit_i=J",
            "i_in_progress=K",
            "i_counted=L",
            "ctrl_test=M",
            "deferred_f_dt=2023-01-02",
            "bypass_timeout=5",
            "instrn_type=O",
            "registration_status=P",
            "last_class_roll_dt=2021-05-06",
            "i_term=Spring, 2023",
            "i_deadline_dt=2022-03-04");

    /**
     * Constructs a new {@code IvtRawStcourse}.
     */
    TestRawStcourse() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawStcourse obj = new RawStcourse(TEST_TERM, TEST_STU_ID, TEST_COURSE, TEST_SECT,
                TEST_PACE_ORDER, TEST_OPEN_STATUS, TEST_GRADING_OPTION, TEST_COMPLETED, TEST_SCORE,
                TEST_COURSE_GRADE, TEST_PREREQ_SATIS, TEST_INIT_CLASS_ROLL, TEST_STU_PROVIDED,
                TEST_FINAL_CLASS_ROLL, TEST_EXAM_PLACED, TEST_ZERO_UNIT, TEST_TIMEOUT_FACTOR,
                TEST_FORFEIT_I, TEST_I_IN_PROGRESS, TEST_I_COUNTED, TEST_CTRL_TEST, TEST_DEFERRED_F_DT,
                TEST_BYPASS_TIMEOUT, TEST_INSTRN_TYPE, TEST_REGISTRATION_STATUS,
                TEST_LAST_CLASS_ROLL_DT, TEST_I_TERM, TEST_I_DEADLINE_DT);

        assertEquals(TEST_TERM, obj.termKey, "Invalid term value after constructor");
        assertEquals(TEST_STU_ID, obj.stuId, "Invalid term value after constructor");
        assertEquals(TEST_COURSE, obj.course, "Invalid course value after constructor");
        assertEquals(TEST_SECT, obj.sect, "Invalid sect value after constructor");
        assertEquals(TEST_PACE_ORDER, obj.paceOrder, "Invalid pace_order value after constructor");
        assertEquals(TEST_OPEN_STATUS, obj.openStatus, "Invalid open_status value after constructor");
        assertEquals(TEST_GRADING_OPTION, obj.gradingOption, "Invalid grading_option value after constructor");
        assertEquals(TEST_COMPLETED, obj.completed, "Invalid completed value after constructor");
        assertEquals(TEST_SCORE, obj.score, "Invalid score value after constructor");
        assertEquals(TEST_COURSE_GRADE, obj.courseGrade, "Invalid course_grade value after constructor");
        assertEquals(TEST_PREREQ_SATIS, obj.prereqSatis, "Invalid prereq_satis value after constructor");
        assertEquals(TEST_INIT_CLASS_ROLL, obj.initClassRoll, "Invalid init_class_roll value after constructor");
        assertEquals(TEST_STU_PROVIDED, obj.stuProvided, "Invalid stu_provided value after constructor");
        assertEquals(TEST_FINAL_CLASS_ROLL, obj.finalClassRoll, "Invalid final_class_roll value after constructor");
        assertEquals(TEST_EXAM_PLACED, obj.examPlaced, "Invalid exam_placed value after constructor");
        assertEquals(TEST_ZERO_UNIT, obj.zeroUnit, "Invalid zero_unit value after constructor");
        assertEquals(TEST_TIMEOUT_FACTOR, obj.timeoutFactor, "Invalid timeout_factor value after constructor");
        assertEquals(TEST_FORFEIT_I, obj.forfeitI, "Invalid forfeit_i value after constructor");
        assertEquals(TEST_I_IN_PROGRESS, obj.iInProgress, "Invalid i_in_progress value after constructor");
        assertEquals(TEST_I_COUNTED, obj.iCounted, "Invalid i_counted value after constructor");
        assertEquals(TEST_CTRL_TEST, obj.ctrlTest, "Invalid ctrl_test value after constructor");
        assertEquals(TEST_DEFERRED_F_DT, obj.deferredFDt, "Invalid deferred_f_dt value after constructor");
        assertEquals(TEST_BYPASS_TIMEOUT, obj.bypassTimeout, "Invalid bypass_timeout value after constructor");
        assertEquals(TEST_INSTRN_TYPE, obj.instrnType, "Invalid instrn_type value after constructor");
        assertEquals(TEST_REGISTRATION_STATUS, obj.registrationStatus,
                "Invalid registration_status value after constructor");
        assertEquals(TEST_LAST_CLASS_ROLL_DT, obj.lastClassRollDt,
                "Invalid last_class_roll_dt value after constructor");
        assertEquals(TEST_I_TERM, obj.iTermKey, "Invalid i_term value after constructor");
        assertEquals(TEST_I_DEADLINE_DT, obj.iDeadlineDt, "Invalid i_deadline_dt value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawStcourse obj = new RawStcourse(TEST_TERM, TEST_STU_ID, TEST_COURSE, TEST_SECT,
                TEST_PACE_ORDER, TEST_OPEN_STATUS, TEST_GRADING_OPTION, TEST_COMPLETED, TEST_SCORE,
                TEST_COURSE_GRADE, TEST_PREREQ_SATIS, TEST_INIT_CLASS_ROLL, TEST_STU_PROVIDED,
                TEST_FINAL_CLASS_ROLL, TEST_EXAM_PLACED, TEST_ZERO_UNIT, TEST_TIMEOUT_FACTOR,
                TEST_FORFEIT_I, TEST_I_IN_PROGRESS, TEST_I_COUNTED, TEST_CTRL_TEST, TEST_DEFERRED_F_DT,
                TEST_BYPASS_TIMEOUT, TEST_INSTRN_TYPE, TEST_REGISTRATION_STATUS,
                TEST_LAST_CLASS_ROLL_DT, TEST_I_TERM, TEST_I_DEADLINE_DT);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}
