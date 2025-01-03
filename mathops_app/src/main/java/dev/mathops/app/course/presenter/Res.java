package dev.mathops.app.course.presenter;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    // Resources used by multiple classes

    /** A resource key. */
    static final String DIR_CHOOSER_TITLE = key(1);

    /** A resource key. */
    static final String PRESENTER_TITLE = key(2);

    /** A resource key. */
    static final String ADD_PRESENTATION_BTN_LBL = key(3);

    /** A resource key. */
    static final String NEW_PRES_DIALOG_TITLE = key(4);

    /** A resource key. */
    static final String PRES_FILE_TYPE = key(5);


    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {DIR_CHOOSER_TITLE, "Select course directory",},
            {PRESENTER_TITLE, "Presenter ({0})",},
            {ADD_PRESENTATION_BTN_LBL, "Add Presentation...",},
            {NEW_PRES_DIALOG_TITLE, "Create new Presentation",},
            {PRES_FILE_TYPE, "MathOps Presentation",},
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
