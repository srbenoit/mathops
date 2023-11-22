package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.Launcher;
import jwabbit.debugger.Debugger;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;
import jwabbit.log.LoggedObject;

import javax.swing.SwingUtilities;

/**
 * Event GUI debug.
 */
class EventGuiDebug implements IEventCallback {

    /**
     * Constructs a new {@code EventGuiDebug}.
     */
    EventGuiDebug() {

        super();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "gui_debug" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        if (theCalcUI == null) {
            return;
        }

        final int slot = theCalcUI.getSlot();
        final Debugger debugger = Launcher.getDebugger(slot);
        if (debugger != null) {
            debugger.show();
        } else {
            LoggedObject.LOG.info("Creating new debugger for slot " + theCalcUI.getSlot());
            final Debugger newDebug = new Debugger(theCalcUI.getSlot());
            Launcher.setDebugger(slot, newDebug);
            SwingUtilities.invokeLater(newDebug);
        }

        Launcher.calcSetRunning(calc, false);
        Launcher.calcPauseLinked();
    }
}
