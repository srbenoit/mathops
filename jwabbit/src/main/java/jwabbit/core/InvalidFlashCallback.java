package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Globals;

/**
 * WABBITEMU SOURCE: interface/calc.c: "invalid_flash_callback" function.
 */
public final class InvalidFlashCallback implements ICpuCallback {

    /**
     * Constructs a new {@code InvalidFlashCallback}.
     */
    public InvalidFlashCallback() {

        // No action
    }

    /**
     * Executes the callback.
     *
     * @param cpu the CPU
     */
    @Override
    public void exec(final CPU cpu) {

        if (!Globals.get().isBreakOnInvalidFlash()) {
            return;
        }

        cpu.getOwningCalc().getBreakpointCallback().exec(cpu.getOwningCalc());
    }
}
