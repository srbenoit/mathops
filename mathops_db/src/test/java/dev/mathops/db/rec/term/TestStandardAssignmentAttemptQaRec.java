package dev.mathops.db.rec.term;

import dev.mathops.db.rec.RecBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code StandardAssignmentAttemptQaRec} class.
 */
public final class TestStandardAssignmentAttemptQaRec {

    /** A field name. */
    private static final Integer TEST_SERIAL_NBR = Integer.valueOf(12345);

    /** A field name. */
    private static final Integer TEST_QUESTION_NBR = Integer.valueOf(8);

    /** A field name. */
    private static final Integer TEST_POINTS = Integer.valueOf(2);

    /** A field name. */
    private static final String TEST_ITEM_ID = "I1.2.3";

    /**
     * The expected String serialization of a test record.
     */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "serial_nbr=12345",
            "question_nbr=8",
            "points=2",
            "item_id=I1.2.3");

    /**
     * Constructs a new {@code TestStandardAssignmentAttemptQaRec}.
     */
    TestStandardAssignmentAttemptQaRec() {

        // No action
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final StandardAssignmentAttemptQaRec obj = new StandardAssignmentAttemptQaRec(TEST_SERIAL_NBR,
                TEST_QUESTION_NBR, TEST_POINTS, TEST_ITEM_ID);

        assertEquals(TEST_SERIAL_NBR, obj.serialNbr, "Invalid serial number value after constructor");
        assertEquals(TEST_QUESTION_NBR, obj.questionNbr, "Invalid question number value after constructor");
        assertEquals(TEST_POINTS, obj.points, "Invalid points  value after constructor");
        assertEquals(TEST_ITEM_ID, obj.itemId, "Invalid item ID value after constructor");
    }

    /**
     * Test case.
     */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final StandardAssignmentAttemptQaRec obj = new StandardAssignmentAttemptQaRec(TEST_SERIAL_NBR,
                TEST_QUESTION_NBR, TEST_POINTS, TEST_ITEM_ID);

        final String ser = obj.toString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }
}