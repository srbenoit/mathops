package dev.mathops.app.webstart;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    // Resources used by multiple files

    /** A resource key. */
    static final String CANT_LOAD_FONT = key(index++);

    /** A resource key. */
    static final String APP_NOT_INSTALL = key(index++);

    /** A resource key. */
    static final String LAUNCH_NOT_INSTALL = key(index++);

    // Resources used by PreLaunch

    /** A resource key. */
    static final String LAUNCH_DIR_NOEXIST = key(index++);

    /** A resource key. */
    static final String LAUNCH_UPD_DIR_NOEXIST = key(index++);

    /** A resource key. */
    static final String UPDATE_LAUNCH_XML_NOEXIST = key(index++);

    /** A resource key. */
    static final String UPDATE_LAUNCH_XML_BAD = key(index++);

    /** A resource key. */
    static final String NO_LAUNCH_UPD_NEEDED = key(index++);

    /** A resource key. */
    static final String UPDATER_FAILED = key(index++);

    /** A resource key. */
    static final String UPDATER_EXIT = key(index++);

    // Resources used by Launch

    /** A resource key. */
    static final String LAUNCH_TITLE = key(index++);

    /** A resource key. */
    static final String CANT_START_APP = key(index++);

    /** A resource key. */
    static final String LAUNCH_FAILED = key(index++);

    /** A resource key. */
    static final String EXEC_APP = key(index++);

    /** A resource key. */
    static final String LAUNCH_EXIT = key(index++);

    // Resources used by Updater
    /** A resource key. */
    static final String UPDATER_TITLE = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {

            {CANT_LOAD_FONT, "Failed to load font",},
            {APP_NOT_INSTALL, "Application not installed",},
            {LAUNCH_NOT_INSTALL, "Launcher not installed",},

            {LAUNCH_DIR_NOEXIST, "Launcher directory does not exist",},
            {LAUNCH_UPD_DIR_NOEXIST, "Launcher update directory does not exist",},
            {UPDATE_LAUNCH_XML_NOEXIST, "update/launch.xml file does not exist",},
            {UPDATE_LAUNCH_XML_BAD, "Unable to parse update/launch.xml file",},
            {NO_LAUNCH_UPD_NEEDED, "Launcher version unchanged - skipping update",},
            {UPDATER_FAILED, "Updater failed",},
            {UPDATER_EXIT, "Updater exiting",},

            {LAUNCH_TITLE, "Launch",},
            {CANT_START_APP, "Failed to start application process",},
            {LAUNCH_FAILED, "Launcher failed",},
            {EXEC_APP, "Executing application",},
            {LAUNCH_EXIT, "Launcher exiting",},

            {UPDATER_TITLE, "Software Updater",},

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
