package dev.mathops.dbjobs.report.analytics.irdata;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String SOURCE_FILE_NOT_FOUND = key(index++);

    /** A resource key. */
    static final String CANT_READ_SOURCE_FILE = key(index++);

    /** A resource key. */
    static final String NUM_RECORDS_FOUND = key(index++);

    /** A resource key. */
    static final String NUM_STUDENTS_FOUND = key(index++);

    /** A resource key. */
    static final String NUM_TERMS_FOUND = key(index++);

    /** A resource key. */
    static final String BAD_FLAG_VALUE = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //

            {SOURCE_FILE_NOT_FOUND, "Source data file not found: {0}"},
            {CANT_READ_SOURCE_FILE, "Failed to read source data file"},
            {NUM_RECORDS_FOUND, "Input data file had {0} records"},
            {NUM_STUDENTS_FOUND, "Data for {0} distinct students found"},
            {NUM_TERMS_FOUND, "Data for {0} distinct enrollment terms found"},
            {BAD_FLAG_VALUE, "*** Line {0} has ''{1}'' in field {2} when expecting 1/0 flag"},

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

//    /**
//     * Gets the message with a specified key using the current locale.
//     *
//     * @param key the message key
//     * @param locale the desired {@code Locale}
//     * @return the best-matching message, an empty string if none is registered (never
//     *         {@code null})
//     */
//     static String get(final String key, final Locale locale) {
//
//         return instance.getMsg(key, locale);
//     }

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

//    /**
//     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format
//     * that message pattern with a collection of arguments.
//     *
//     * @param key the message key
//     * @param locale the desired {@code Locale}
//     * @param arguments the arguments, as for {@code MessageFormat}
//     * @return the formatted string (never {@code null})
//     */
//     static String fmt(final String key, final Locale locale, final Object... arguments) {
//
//        return instance.formatMsg(key, locale, arguments);
//     }
}
