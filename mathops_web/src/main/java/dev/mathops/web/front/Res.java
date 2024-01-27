package dev.mathops.web.front;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by BuildDateTime

    /** A resource key. */
    static final String CANT_GET_BUILD_DTIME = key(index++);

    // Resources used by ContextListener

    /** A resource key. */
    static final String CONTEXT_INITIALIZING = key(index++);

    /** A resource key. */
    static final String BASE_DIR = key(index++);

    /** A resource key. */
    static final String CFG_FILE = key(index++);

    /** A resource key. */
    static final String CONTEXT_INITIALIZED = key(index++);

    /** A resource key. */
    static final String REPORTS_ENABLED = key(index++);

    /** A resource key. */
    static final String CRON_TERMINATING = key(index++);

    /** A resource key. */
    static final String CONTEXT_DESTROYED = key(index++);

    // Resources used by FrontController

    /** A resource key. */
    static final String SERVLET_INIT = key(index++);

    /** A resource key. */
    static final String SERVLET_INITIALIZED = key(index++);

    /** A resource key. */
    static final String SERVLET_TERMINATED = key(index++);

    /** A resource key. */
    static final String SERVLET_TITLE = key(index++);

    /** A resource key. */
    static final String BUILD_DATETIME = key(index++);

    /** A resource key. */
    static final String BAD_SCHEME = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {CANT_GET_BUILD_DTIME, "Unable to read build date/time",},

            {CONTEXT_INITIALIZING, "Front Controller context initializing within ",},
            {BASE_DIR, "  Installation base dir: ",},
            {CFG_FILE, "  Installation cfg file: ",},
            {CONTEXT_INITIALIZED, "Front Controller context initialized",},
            {REPORTS_ENABLED, "Reporting cron task installed",},
            {CRON_TERMINATING, "cron service terminating",},
            {CONTEXT_DESTROYED, "Front Controller context destroyed",},

            {SERVLET_INIT, "Front Controller servlet initializing",},
            {SERVLET_INITIALIZED, "Front Controller servlet initialized",},
            {SERVLET_TERMINATED, "Front Controller servlet terminated",},
            {SERVLET_TITLE, "Front Controller servlet",},
            {BUILD_DATETIME, "Front Controller servlet built: {0}",},
            {BAD_SCHEME, "Unsupported scheme: {0}",},

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
