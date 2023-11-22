package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A listener for updates to the (grayscale) LCD. The calculator thread will install such a listener to be notified (on
 * the calculator thread) of updates to the LCD screen size or data. When called, the recipient should copy and store
 * the data as quickly as possible and return, storing the notification that the LCD should be re-rendered and drawn,
 * but not processing the repaint on the calling thread.
 */
@FunctionalInterface
public interface ILcdListener {

    /**
     * Called when the LCD is updated.
     *
     * @param active   the active state of the LCD
     * @param contrast the LCD contrast
     * @param data     the LCD image data
     */
    void updateLcd(boolean active, int contrast, int[] data);
}
