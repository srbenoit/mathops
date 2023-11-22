package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.SEAux;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;

/**
 * Event sync calc clock.
 */
class EventSyncCalcClock implements IEventCallback {

    /**
     * Constructs a new {@code EventSyncCalcClock}.
     */
    EventSyncCalcClock() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "sync_calc_clock" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        if (calc.getModel().ordinal() < EnumCalcModel.TI_84P.ordinal()) {
            return;
        }

        final SEAux seAux = calc.getCPU().getPIOContext().getSeAux();
        if (seAux == null) {
            return;
        }

        final long result = System.currentTimeMillis() * 1000L;

        seAux.getClock().setSet(result);
        seAux.getClock().setBase(result);
    }
}
