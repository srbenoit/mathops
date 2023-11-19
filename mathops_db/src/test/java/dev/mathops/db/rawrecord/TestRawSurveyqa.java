package dev.mathops.db.rawrecord;

import dev.mathops.db.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.RecBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@code RawSurveyqa} class.
 */
final class TestRawSurveyqa {

    /** A field name. */
    private static final TermKey TEST_TERM = new TermKey(ETermName.FALL, 2022); // $NON-NLS-1$

    /** A field name. */
    private static final String TEST_VERSION = "UOOOO";

    /** A field name. */
    private static final Integer TEST_SURVEY_NBR = Integer.valueOf(1);

    /** A field name. */
    private static final String TEST_QUESTION_DESC = "Favorite Color";

    /** A field name. */
    private static final String TEST_TYPE_QUESTION = "MC";

    /** A field name. */
    private static final String TEST_ANSWER = "B";

    /** A field name. */
    private static final String TEST_ANSWER_DESC = "Blue";

    /** A field name. */
    private static final String TEST_ANSWER_MEANING = "Blue is my favorite color";

    /** A field name. */
    private static final String TEST_MUST_ANSWER = "Y";

    /** A field name. */
    private static final String TEST_TREE_REF = "A.B.C";

    /** The expected String serialization of a test record. */
    private static final String EXPECT_SER99 = String.join(RecBase.DIVIDER,
            "term=Fall, 2022",
            "version=UOOOO",
            "survey_nbr=1",
            "question_desc=Favorite Color",
            "type_question=MC",
            "answer=B",
            "answer_desc=Blue",
            "answer_meaning=Blue is my favorite color",
            "must_answer=Y",
            "tree_ref=A.B.C");

    /**
     * Constructs a new {@code IvtRawSurveyqa}.
     */
    TestRawSurveyqa() {

        // No action
    }

    /** Test case. */
    @Test
    @DisplayName("Constructor")
    void test0001() {

        final RawSurveyqa obj = new RawSurveyqa(TEST_TERM, TEST_VERSION, TEST_SURVEY_NBR,
                TEST_QUESTION_DESC, TEST_TYPE_QUESTION, TEST_ANSWER, TEST_ANSWER_DESC,
                TEST_ANSWER_MEANING, TEST_MUST_ANSWER, TEST_TREE_REF);

        assertEquals(TEST_TERM, obj.termKey, "Invalid sttermd_id value after constructor");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after constructor");
        assertEquals(TEST_SURVEY_NBR, obj.surveyNbr, "Invalid survey_nbr value after constructor");
        assertEquals(TEST_QUESTION_DESC, obj.questionDesc, "Invalid question_desc value after constructor");
        assertEquals(TEST_TYPE_QUESTION, obj.typeQuestion, "Invalid type_question value after constructor");
        assertEquals(TEST_ANSWER, obj.answer, "Invalid answer value after constructor");
        assertEquals(TEST_ANSWER_DESC, obj.answerDesc, "Invalid answer_desc value after constructor");
        assertEquals(TEST_ANSWER_MEANING, obj.answerMeaning, "Invalid answer_meaning value after constructor");
        assertEquals(TEST_MUST_ANSWER, obj.mustAnswer, "Invalid must_answer value after constructor");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after constructor");
    }

    /** Test case. */
    @Test
    @DisplayName("string serialization")
    void test0098() {

        final RawSurveyqa obj = new RawSurveyqa(TEST_TERM, TEST_VERSION, TEST_SURVEY_NBR,
                TEST_QUESTION_DESC, TEST_TYPE_QUESTION, TEST_ANSWER, TEST_ANSWER_DESC,
                TEST_ANSWER_MEANING, TEST_MUST_ANSWER, TEST_TREE_REF);

        final String ser = obj.serializedString();

        assertEquals(EXPECT_SER99, ser, "Invalid serialized string");
    }

    /** Test case. */
    @Test
    @DisplayName("string deserialization")
    void test0099() {

        final RawSurveyqa obj = RawSurveyqa.parse(EXPECT_SER99);

        assertEquals(TEST_TERM, obj.termKey, "Invalid sttermd_id value after deserialization");
        assertEquals(TEST_VERSION, obj.version, "Invalid version value after deserialization");
        assertEquals(TEST_SURVEY_NBR, obj.surveyNbr, "Invalid survey_nbr value after deserialization");
        assertEquals(TEST_QUESTION_DESC, obj.questionDesc, "Invalid question_desc value after deserialization");
        assertEquals(TEST_TYPE_QUESTION, obj.typeQuestion, "Invalid type_question value after deserialization");
        assertEquals(TEST_ANSWER, obj.answer, "Invalid answer value after deserialization");
        assertEquals(TEST_ANSWER_DESC, obj.answerDesc, "Invalid answer_desc value after deserialization");
        assertEquals(TEST_ANSWER_MEANING, obj.answerMeaning, "Invalid answer_meaning value after deserialization");
        assertEquals(TEST_MUST_ANSWER, obj.mustAnswer, "Invalid must_answer value after deserialization");
        assertEquals(TEST_TREE_REF, obj.treeRef, "Invalid tree_ref value after deserialization");
    }
}
