package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.ICpuCallback;

/**
 * WABBITEMU SOURCE: hardware/lcd.c, "LCD_free" function.
 */
class LCDFree implements ICpuCallback {

    /**
     * Constructs a new {@code LCDFree}.
     */
    LCDFree() {

        // No action
    }

    /**
     * Executes the callback.
     *
     * @param cpu the CPU
     */
    @Override
    public final void exec(final CPU cpu) {

        cpu.getPIOContext().setLcd(null);
    }
}
