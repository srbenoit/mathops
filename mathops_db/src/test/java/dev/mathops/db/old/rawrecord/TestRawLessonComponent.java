package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawLessonComponent} class.
 */
final class TestRawLessonComponent {

    /** A field name. */
    private static final String TEST_LESSON_ID = "L1.2.3";

    /** A field name. */
    private static final Integer TEST_SEQ_NBR = Integer.valueOf(987);

    /** A field name. */
    private static final String TEST_TYPE = "A";

    /** A field name. */
    private static final String TEST_XML_DATA = "<xml></xml>";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "lesson_id=L1.2.3",
            "seq_nbr=987",
            "type=A",
            "xml_data=<xml></xml>");

    /**
     * Constructs a new {@code IvtRawLessonComponent}.
     */
    TestRawLessonComponent() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawLessonComponent obj =
                new RawLessonComponent(TEST_LESSON_ID, TEST_SEQ_NBR, TEST_TYPE, TEST_XML_DATA);

        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after constructor");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after constructor");
        assertEquals(TEST_TYPE, obj.type, "Invalid type value after constructor");
        assertEquals(TEST_XML_DATA, obj.xmlData, "Invalid xml_data value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawLessonComponent obj =
                new RawLessonComponent(TEST_LESSON_ID, TEST_SEQ_NBR, TEST_TYPE, TEST_XML_DATA);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawLessonComponent obj = RawLessonComponent.parse(EXPECT_SER99);

        assertEquals(TEST_LESSON_ID, obj.lessonId, "Invalid lesson_id value after deserialization");
        assertEquals(TEST_SEQ_NBR, obj.seqNbr, "Invalid seq_nbr value after deserialization");
        assertEquals(TEST_TYPE, obj.type, "Invalid type value after deserialization");
        assertEquals(TEST_XML_DATA, obj.xmlData, "Invalid xml_data value after deserialization");
    }
}
