package dev.mathops.app.adm.office;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by TopPanelOffice

    /** A resource key. */
    static final String PICK_STUDENT = key(index++);

    /** A resource key. */
    static final String STUDENT_DETAIL = key(index++);

    ///** A resource key. */
    //static final String PICK_POPULATION = key(index++);

    ///** A resource key. */
    //static final String POPULATION_DETAIL = key(index++);

    /** A resource key. */
    static final String VIEW_LOG = key(index++);

    // Used by LogWindow

    /** A resource key. */
    static final String LOG_TITLE = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {PICK_STUDENT, "Pick Student"},
            {STUDENT_DETAIL, "Student Detail"},
            //{PICK_POPULATION, "Pick Population"},
            //{POPULATION_DETAIL, "Population Detail"},
            {VIEW_LOG, "View Log"},

            {LOG_TITLE, "Log"},

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
