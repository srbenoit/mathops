package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;

/**
 * Event update calculator running.
 */
class EventUpdateCalcRunning implements IEventCallback {

    /**
     * Constructs a new {@code EventLoadKeySettings}.
     */
    EventUpdateCalcRunning() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "update_calc_running" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

//        if (theCalcUI == null) {
//            return;
//        }

//        final JMenuBar hMenu = theCalcUI.getMainFrame().getJMenuBar();
//        if (hMenu == null) {
//            return;
//        }
//
//        // TODO: Make sure any changes occur in the AWT event thread!
//
//        // if (calc.isRunning()) {
//        // CheckMenuItem(GetSubMenu(hMenu, 2), IDM_CALC_PAUSE, MF_BYCOMMAND | MF_UNCHECKED);
//        // } else {
//        // CheckMenuItem(GetSubMenu(hMenu, 2), IDM_CALC_PAUSE, MF_BYCOMMAND | MF_CHECKED);
//        // }
    }
}
