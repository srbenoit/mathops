package dev.mathops.app.deploy;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources for the {@code edu.colostate.math.deploy} package.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String ADDING_FILES = key(index++);

    /** A resource key. */
    static final String JAR_WRITE_FAILED = key(index++);

    /** A resource key. */
    static final String READ_FAILED = key(index++);

    /** A resource key. */
    static final String FILES_COPIED = key(index++);

    /** A resource key. */
    static final String FILE_CREATED = key(index++);

    /** A resource key. */
    static final String FINISHED = key(index++);

    /** A resource key. */
    static final String DIR_NOT_FOUND = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {
            {ADDING_FILES, "Adding files from [{0}] project"},
            {JAR_WRITE_FAILED, "Failed to write jar file"},
            {READ_FAILED, "Failed to read file: {0}"},
            {FILES_COPIED, "File(s) copied"},
            {FILE_CREATED, "{0} created"},
            {FINISHED, "Finished"},

            {DIR_NOT_FOUND, "Directory {0} not found."},

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
