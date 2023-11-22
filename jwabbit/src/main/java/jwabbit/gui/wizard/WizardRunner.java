package jwabbit.gui.wizard;

import jwabbit.Launcher;
import jwabbit.gui.CalcUI;
import jwabbit.gui.Gui;
import jwabbit.iface.Calc;

/**
 * A runner to launch and monitor a ROM wizard instance in its own thread.
 */
public final class WizardRunner extends Thread {

    /**
     * Constructs a new {@code WizardRunner}.
     */
    public WizardRunner() {

        super("ROM Wizard");
    }

    /**
     * Runs the ROM wizard in its own thread.
     */
    @Override
    public void run() {

        final Calc calc = new Calc();
        if (RomWizard.doWizardSheet(calc)) {

            final int slot = Launcher.addCalc(calc, true);
            if (slot != -1) {
                final CalcUI theCalcUI = Launcher.getCalcUI(slot);
                Gui.registerEvents(calc, theCalcUI);
                Launcher.getCalcThread(slot).start();
            }
        }
    }
}
