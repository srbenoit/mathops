package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve
 * Benoit. This software is licensed under the GNU General Public License version 2 (GPLv2). See the
 * disclaimers or warranty and liability included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.JWCoreConstants;
import jwabbit.hardware.LCD;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;

/**
 * Event load settings.
 */
final class EventLoadSettings implements IEventCallback {

    /**
     * Constructs a new {@code EventLoadSettings}.
     */
    EventLoadSettings() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "load_settings" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public void exec(final Calc calc, final CalcUI theCalcUI) {

        if (calc.getModel().ordinal() < EnumCalcModel.TI_84PCSE.ordinal()) {
            final LCD lcd = (LCD) calc.getCPU().getPIOContext().getLcd();

            lcd.setShades(Registry.asInteger(Registry.queryWabbitKey("shades"), 6));
            lcd.setMode(Registry.asInteger(Registry.queryWabbitKey("lcd_mode"), 0));
            final int freq = Registry.asInteger(Registry.queryWabbitKey("lcd_freq"), JWCoreConstants.FPS);
            lcd.setSteadyFrame(1.0 / (double) freq);
            lcd.setLcdDelay(Registry.asInteger(Registry.queryWabbitKey("lcd_delay"), 60));
        }

        theCalcUI.getMainFrame().invalidate();
        theCalcUI.getMainFrame().repaint();

        if (theCalcUI.getDebugFrame() == null) {
            calc.setRunning(true);
            // } else {
            // SendMessage(theCalcUI.getMainFrame(), WM_CLOSE_DEBUGGER, 0, 0);
            // SendMessage(theCalcUI.getMainFrame(), WM_OPEN_DEBUGGER, 0, 0);
        }
    }
}
