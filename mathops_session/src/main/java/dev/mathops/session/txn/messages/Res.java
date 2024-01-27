package dev.mathops.session.txn.messages;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by AbstractMessageBase

    /** A resource key. */
    static final String BAD_MESSAGE_TYPE = key(index++);

    /** A resource key. */
    static final String NO_CLOSING_TAG = key(index++);

    /** A resource key. */
    static final String CANT_EXTRACT_INTEGER = key(index++);

    /** A resource key. */
    static final String CANT_EXTRACT_DOUBLE = key(index++);

    // Used by GetExamReply, GetPastExamReply

    /** A resource key. */
    static final String MISSING_PROBLEM = key(index++);

    /** A resource key. */
    static final String CANT_PARSE_PROBLEM = key(index++);

    /** A resource key. */
    static final String NO_SELECTED_LIST = key(index++);

    // Used by GetHomeworkReply

    /** A resource key. */
    static final String BAD_MIN_MOVEON = key(index++);

    /** A resource key. */
    static final String BAD_MIN_MASTERY = key(index++);

    /** A resource key. */
    static final String NO_SELECTED_HW_LIST = key(index++);

    // Used by GetReviewExamReply

    /** A resource key. */
    static final String BAD_MASTERY = key(index++);

    /** A resource key. */
    static final String NOT_ALL_SEL_IN_XML = key(index++);

    /** A resource key. */
    static final String SELECTED_WAS_NULL = key(index++);

    // Used by MachineSetupReply

    /** A resource key. */
    static final String BAD_SETUP_RESULT = key(index++);

    /** A resource key. */
    static final String NONINT_SETUP_RESULT = key(index++);

    // Used by MachineSetupRequest

    /** A resource key. */
    static final String BAD_CENTER_ID = key(index++);

    /** A resource key. */
    static final String NONINT_CENTER_ID = key(index++);

    // Used by MessageFactory

    /** A resource key. */
    static final String NULL_XML_MSG = key(index++);

    /** A resource key. */
    static final String MSG_NO_START_OPEN = key(index++);

    /** A resource key. */
    static final String MSG_NO_START_CLOSE = key(index++);

    /** A resource key. */
    static final String MSG_EMPTY_START_TAG = key(index++);

    /** A resource key. */
    static final String BAD_MSG_TAG = key(index++);

    /** A resource key. */
    static final String MSG_PARSE_ERROR = key(index++);

    // Used by SubmitHomeworkRequest

    /** A resource key. */
    static final String NOT_ALL_SEL_HW_INCLUDED = key(index++);

    /** A resource key. */
    static final String CANT_PARSE_SEL_HW = key(index++);

    /** A resource key. */
    static final String NO_SEL_HW_LIST = key(index++);

    /** A resource key. */
    static final String BAD_SUBMIT_HW_SCORE = key(index++);

    /** A resource key. */
    static final String BAD_ANSWER_OBJ = key(index++);

    // Used by TestingStationStatusReply

    /** A resource key. */
    static final String CANT_PARSE_UNIT = key(index++);

    // Used by UpdateExamReply

    /** A resource key. */
    static final String CANT_PARSE_STATUS = key(index++);

    /** A resource key. */
    static final String BAD_SCORE = key(index++);

    /** A resource key. */
    static final String BAD_SCORE_ENTRY = key(index++);

    /** A resource key. */
    static final String BAD_GRADE_ENTRY = key(index++);

    /** A resource key. */
    static final String BAD_MISSED_QUESTION = key(index++);

    /** A resource key. */
    static final String BAD_MISSED_ENTRY = key(index++);

    // Used by UpdateExamRequest

    /** A resource key. */
    static final String BAD_REALIZED = key(index++);

    /** A resource key. */
    static final String BAD_CUR_SECTION = key(index++);

    /** A resource key. */
    static final String BAD_CUR_PROBLEM = key(index++);

    /** A resource key. */
    static final String BAD_PRESENTED = key(index++);

    /** A resource key. */
    static final String BAD_UPDATED = key(index++);

    /** A resource key. */
    static final String BAD_NUM_ANSWERS = key(index++);

    /** A resource key. */
    static final String BAD_ANSWER = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {BAD_MESSAGE_TYPE, "XML is not a {0} message.",},
            {NO_CLOSING_TAG, "XML has no closing {0} tag.",},
            {CANT_EXTRACT_INTEGER, "Failed to extract integer",},
            {CANT_EXTRACT_DOUBLE, "Failed to extract double",},

            {MISSING_PROBLEM, "Exam {0} did not include problem {1} in section {2}",},
            {CANT_PARSE_PROBLEM, "Unable to parse selected exam problem.",},
            {NO_SELECTED_LIST, "No list of selected problems in realized exam.",},

            {BAD_MIN_MOVEON, "Invalid minimum move-on score",},
            {BAD_MIN_MASTERY, "Invalid minimum mastery score",},
            {NO_SELECTED_HW_LIST, "No list of problems in realized homework.",},

            {BAD_MASTERY, "Invalid mastery score.",},
            {NOT_ALL_SEL_IN_XML, "Not all selected problems included in exam XML",},
            {SELECTED_WAS_NULL, "Selected problem {0} in section {1} was null",},

            {BAD_SETUP_RESULT, "Invalid machine setup result code: {0}",},
            {NONINT_SETUP_RESULT, "Non-integer machine setup result code: {0}",},

            {BAD_CENTER_ID, "Invalid testing center ID: {0}",},
            {NONINT_CENTER_ID, "Non-integer testing center ID: {0}",},

            {NULL_XML_MSG, "Attempt to parse null XML message",},
            {MSG_NO_START_OPEN, "XML message contained no start tag '<': {0}",},
            {MSG_NO_START_CLOSE, "XML message contained no start tag '>': {0}",},
            {MSG_EMPTY_START_TAG, "XML message contained emnpty start tag: {0}",},
            {BAD_MSG_TAG, "Unrecognized message tag: {0}",},
            {MSG_PARSE_ERROR, "Error parsing message: {0}",},

            {NOT_ALL_SEL_HW_INCLUDED, "Not all selected problems included in homework submission for assignment {0}",},
            {CANT_PARSE_SEL_HW, "Unable to parse selected homework problem for assignment {0}",},
            {NO_SEL_HW_LIST, "No list of selected problems in realized homework assignment {0}",},
            {BAD_SUBMIT_HW_SCORE, "Invalid score in homework submission: ''{0}''",},
            {BAD_ANSWER_OBJ, "Answer array contains a {0}",},

            {CANT_PARSE_UNIT, "Failed to parse unit",},

            {CANT_PARSE_STATUS, "Failed to parse status",},
            {BAD_SCORE, "Invalid score: {0}",},
            {BAD_SCORE_ENTRY, "Invalid score entry: {0}",},
            {BAD_GRADE_ENTRY, "Invalid grade entry: {0}",},
            {BAD_MISSED_QUESTION, "Invalid missed question: ''{0}''",},
            {BAD_MISSED_ENTRY, "Invalid missed entry: ''{0}''",},

            {BAD_REALIZED, "Invalid realized",},
            {BAD_CUR_SECTION, "Invalid cur-section",},
            {BAD_CUR_PROBLEM, "Invalid cur-problem",},
            {BAD_PRESENTED, "Invalid presented",},
            {BAD_UPDATED, "Invalid updated",},
            {BAD_NUM_ANSWERS, "Invalid num-answers",},
            {BAD_ANSWER, "Answer array contains a {0}",},

    };

    /** The singleton instance. */
    private static final Res instance = new Res();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Res() {

        super(Locale.US, EN_US);
    }

    /**
     * Gets the message with a specified key using the current locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    static String get(final String key) {

        return instance.getMsg(key);
    }

    /**
     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format that message pattern with
     * a collection of arguments.
     *
     * @param key       the message key
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    static String fmt(final String key, final Object... arguments) {

        return instance.formatMsg(key, arguments);
    }
}
