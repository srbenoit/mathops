package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.EnumEventType;
import jwabbit.iface.IBreakpointCallback;

/**
 * WABBITEMU SOURCE: interface/calc.c, "calc_debug_callback" function.
 */
public final class CalcDebugCallback implements IBreakpointCallback {

    /**
     * Constructs a new {@code CalcDebugCallback}.
     */
    public CalcDebugCallback() {

        // No action
    }

    /**
     * Execute the callback.
     *
     * @param calc the calculator
     */
    @Override
    public void exec(final Calc calc) {

        calc.notifyEvent(EnumEventType.BREAKPOINT_EVENT);
    }
}
