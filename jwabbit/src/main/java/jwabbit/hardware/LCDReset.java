package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.ICpuCallback;

/**
 * WABBITEMU SOURCE: hardware/lcd.c, "LCD_reset" function.
 */
final class LCDReset implements ICpuCallback {

    /**
     * Constructs a new {@code LCDReset}.
     */
    LCDReset() {

        super();
    }

    /**
     * Executes the callback.
     *
     * @param cpu the CPU
     */
    @Override
    public void exec(final CPU cpu) {

        final AbstractLCDBase lcdBase = cpu.getPIOContext().getLcd();

        if (lcdBase instanceof final LCD lcd) {
            lcd.setActive(false);
            lcd.setWordLen(8);
            lcd.setCursorMode(HardwareConstants.Y_UP);
            lcd.setX(0);
            lcd.setY(0);
            lcd.setZ(0);
            lcd.setContrast(32);
            lcd.setLastRead(0);

            for (int i = 0; i < HardwareConstants.DISPLAY_SIZE; ++i) {
                lcd.setDisplay(i, 0);
            }

            lcd.setFront(0);

            for (int i = 0; i < HardwareConstants.LCD_MAX_SHADES; ++i) {
                for (int j = 0; j < HardwareConstants.DISPLAY_SIZE; ++j) {
                    lcd.setQueue(i, j, 0);
                }
            }
        }
    }
}
