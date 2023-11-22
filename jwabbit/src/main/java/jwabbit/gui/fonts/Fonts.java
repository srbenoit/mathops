package jwabbit.gui.fonts;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.awt.Font;

/**
 * Loads the stock fonts for cross-platform consistency and good small-print rendering.
 */
public final class Fonts {

    /** Object on which to synchronize creation of font instances. */
    private static final Object INSTANCE_SYNCH = new Object();

    /** The sans font. */
    private static Font sansFont;

    /** The monospace font. */
    private static Font monoFont;

    /**
     * private constructor to prevent direct instantiation.
     */
    private Fonts() {

        super();
    }

    /**
     * Retrieves the sans-serif font (a one-point plain version from which other sizes and styles may be derived).
     *
     * @return the font
     */
    public static Font getSans() {

        synchronized (INSTANCE_SYNCH) {

            if (sansFont == null) {
                sansFont = new Font("Arial", Font.PLAIN, 1);
            }
        }

        return sansFont;
    }

    /**
     * Retrieves the monospace font (a one-point plain version from which other sizes and styles may be derived).
     *
     * @return the font
     */
    public static Font getMono() {

        synchronized (INSTANCE_SYNCH) {

            if (monoFont == null) {
                monoFont = new Font("Lucida Console", Font.PLAIN, 1);
            }
        }

        return monoFont;
    }
}
