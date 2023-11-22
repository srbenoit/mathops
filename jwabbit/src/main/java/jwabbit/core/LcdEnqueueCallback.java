package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.EnumEventType;

/**
 * WABBITEMU SOURCE: interface/calc.c: "lcd_enqueue_callback" function.
 */
public final class LcdEnqueueCallback implements ICpuCallback {

    /**
     * Constructs a new {@code LcdEnqueueCallback}.
     */
    public LcdEnqueueCallback() {

        // No action
    }

    /**
     * Executes the callback.
     *
     * @param cpu the CPU
     */
    @Override
    public void exec(final CPU cpu) {

        cpu.getOwningCalc().notifyEvent(EnumEventType.LCD_ENQUEUE_EVENT);
    }
}
