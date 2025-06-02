package dev.mathops.web.host.nibbler;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Used by Page

    /** A resource key. */
    static final String LOGO_TEXT_ONLY = key(index++);

    /** A resource key. */
    static final String ROLE_ADMIN = key(index++);

    /** A resource key. */
    static final String ROLE_ADVISER = key(index++);

    /** A resource key. */
    static final String ROLE_ACT_AS = key(index++);

    /** A resource key. */
    static final String ADVISER_ACT_AS = key(index++);

    /** A resource key. */
    static final String ROLE_STOP_ACTING = key(index++);

    /** A resource key. */
    static final String ROLE_BECOME = key(index++);

    /** A resource key. */
    static final String ROLE_DETAILS = key(index++);

    /** A resource key. */
    static final String FOOTER_CONTACT = key(index++);

    /** A resource key. */
    static final String FOOTER_OEO = key(index++);

    /** A resource key. */
    static final String FOOTER_PRIVACY = key(index++);

    /** A resource key. */
    static final String FOOTER_DISCLAIMER = key(index++);

    /** A resource key. */
    static final String FOOTER_COPYRIGHT = key(index++);

    /** A resource key. */
    static final String SKIP_TO_MAIN = key(index++);

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

    // Used by WebMidController

    /** A resource key. */
    static final String INFO = key(index++);

    /** A resource key. */
    static final String CANT_CREATE_DIR = key(index++);

    /** A resource key. */
    static final String BAD_DIR = key(index++);

    /** A resource key. */
    static final String STARTING = key(index++);

    /** A resource key. */
    static final String STARTED = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {
            {LOGO_TEXT_ONLY, "Colorado State University"},
            {ROLE_ADMIN, "Role: Administrator"},
            {ROLE_ADVISER, "Role: Adviser"},
            {ROLE_ACT_AS, "Act as:"},
            {ADVISER_ACT_AS, "Act as student:"},
            {ROLE_STOP_ACTING, "Stop acting"},
            {ROLE_BECOME, "Become:"},
            {ROLE_DETAILS, "Details"},
            {FOOTER_CONTACT, "Contact CSU"},
            {FOOTER_OEO, "Equal Opportunity"},
            {FOOTER_PRIVACY, "Privacy Statement"},
            {FOOTER_DISCLAIMER, "Disclaimer"},
            {FOOTER_COPYRIGHT, "&copy; 2024 Colorado State University, &nbsp; Fort Collins, Colorado &nbsp; 80523 USA"},
            {SKIP_TO_MAIN, "Skip to main content"},

            {SERVLET_INIT, "Testing Front Controller servlet initializing",},
            {SERVLET_INITIALIZED, "Testing Front Controller servlet initialized",},
            {SERVLET_TERMINATED, "Testing Front Controller servlet terminated",},
            {SERVLET_TITLE, "Testing Front Controller servlet",},
            {BUILD_DATETIME, "Testing Front Controller servlet built: {0}",},
            {BAD_SCHEME, "Unsupported scheme: {0}",},

            {INFO, "Testing Mid Controller 1.0"},
            {CANT_CREATE_DIR, "Unable to create ''{0}'' directory"},
            {BAD_DIR, "Invalid directory ''{0}''"},
            {STARTING, "{0} starting with base directory ''{1}''"},
            {STARTED, "{0} initialized"},
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
