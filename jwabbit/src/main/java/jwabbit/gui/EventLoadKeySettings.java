package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.KEYPROG;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;

/**
 * Event load key settings.
 */
class EventLoadKeySettings implements IEventCallback {

    /**
     * Constructs a new {@code EventLoadKeySettings}.
     */
    EventLoadKeySettings() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "load_key_settings" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        final KEYPROG[] keys = calc.getModel() == EnumCalcModel.TI_86 || calc.getModel() == EnumCalcModel.TI_85
                ? Gui.KEYSTI86 : Gui.KEYSTI83;

        System.arraycopy(keys, 0, Gui.KEYGRPS, 0, Gui.KEYGRPS.length);
    }
}
