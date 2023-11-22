package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;
import jwabbit.utilities.Gif;

/**
 * Event handle screenshot.
 */
class EventHandleScreenshot implements IEventCallback {

    /**
     * Constructs a new {@code EventHandleScreenshot}.
     */
    EventHandleScreenshot() {

        super();
    }

    /**
     * WABBITEMU SOURCE: utilities/screenshothandle.c, "handle_screenshot" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        Gif.get().handleScreenshot(calc);
    }
}
