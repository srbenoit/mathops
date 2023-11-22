package dev.mathops.assessment.document.template;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by multiple classes

    /** A resource key. */
    static final String NOT_AWT_THREAD = key(index++);

    // Resources used by AbstractDocInput

    /** A resource key. */
    static final String INCONSISTENT_TYPE = key(index++);

    /** A resource key. */
    static final String BAD_DATA_TYPE = key(index++);

    // Resources used by various classes

    /** A resource key. */
    static final String BAD_ATTEMPT_TO_SET = key(index++);

    // Resources used by DocInputDoubleField

    /** A resource key. */
    static final String INVALID_NUMBER = key(index++);

    // Resources used by DocInputRadioButton

    /** A resource key. */
    static final String UNABLE_TO_PARSE = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {NOT_AWT_THREAD, "NOT THE AWT EVENT THREAD!",},

            {INCONSISTENT_TYPE, //
                    "Variable {0} has inconsistent type (has {1}, expected {2})",},
            {BAD_DATA_TYPE, "Invalid data type",},

            {BAD_ATTEMPT_TO_SET, "Attempt to set {0} from {1}",},

            {INVALID_NUMBER, "Invalid number",},

            {UNABLE_TO_PARSE, "Unable to parse: {0}",},

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
