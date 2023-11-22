package dev.mathops.web.site.help;

import java.time.LocalDateTime;

/**
 * Logic to support online live help.
 */
public enum HelpLogic {
    ;

    /**
     * Tests whether live help is available right now.
     *
     * @return true if online learning assistants are available now
     */
    public static boolean isHelpOpenNow() {

        return true;
    }

    /**
     * Gets the date/time when online learning assistants will next be available.
     *
     * @return the date/time
     */
    public static LocalDateTime getWhenNextAvailable() {

        return LocalDateTime.now().plusHours(1L);
    }
}
