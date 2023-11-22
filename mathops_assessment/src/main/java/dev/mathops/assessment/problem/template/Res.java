package dev.mathops.assessment.problem.template;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by AbstractProblemMultipleChoiceBase

    /** A resource key. */
    static final String CHOICES_LBL = key(index++);

    /** A resource key. */
    static final String CHOICES_ABOVE_COR = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {CHOICES_LBL, "Choices"},
            {CHOICES_ABOVE_COR, "CHOICE ABOVE IS CORRECT"},

            //
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
}
