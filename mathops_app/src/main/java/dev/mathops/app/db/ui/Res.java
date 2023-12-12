package dev.mathops.app.db.ui;

import dev.mathops.core.res.ResBundle;

import javax.swing.JButton;
import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by MainWindow

    /** A resource key. */
    static final String MAIN_WINDOW_TITLE = key(index++);

    /** A resource key. */
    static final String SERVER_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String PROFILE_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String CONTEXT_TAB_TITLE = key(index++);

    /** A resource key. */
    static final String LOAD_ACTIVE_BTN = key(index++);

    /** A resource key. */
    static final String SAVE_ACTIVE_BTN = key(index++);

    /** A resource key. */
    static final String LOAD_NAMED_BTN = key(index++);

    /** A resource key. */
    static final String SAVE_NAMED_BTN = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {MAIN_WINDOW_TITLE, "Database Management",},
            {SERVER_TAB_TITLE, "Database Servers",},
            {PROFILE_TAB_TITLE, "Data Profiles",},
            {CONTEXT_TAB_TITLE, "Web and Code Contexts",},

            {LOAD_ACTIVE_BTN, "Load Active Configuration",},
            {SAVE_ACTIVE_BTN, "Save as Active Configuration",},
            {LOAD_NAMED_BTN, "Load Named Configuration...",},
            {SAVE_NAMED_BTN, "Save As Named Configuration...",},


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
