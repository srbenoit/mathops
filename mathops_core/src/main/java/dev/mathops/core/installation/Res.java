package dev.mathops.core.installation;

import dev.mathops.core.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by Installation

    /** A resource key. */
    static final String CFG_FILE_NONEXIST = key(index++);

    /** A resource key. */
    static final String CANT_READ_CFG_FILE = key(index++);

    /** A resource key. */
    static final String CANT_CREATE_DIR = key(index++);

    /** A resource key. */
    static final String PATH_IS_NOT_DIR = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {CFG_FILE_NONEXIST, "Configuration file ''{0}'' does not exist",},
            {CANT_READ_CFG_FILE, "Unable to load configuration file ''{0}''",},
            {CANT_CREATE_DIR, "Unable to create diretory ''{0}''",},
            {PATH_IS_NOT_DIR, "Path ''{0}'' exists but is not a directory",},

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
