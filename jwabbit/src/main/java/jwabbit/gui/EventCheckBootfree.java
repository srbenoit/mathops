package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;
import jwabbit.core.Memory;
import jwabbit.gui.wizard.RomWizard;
import jwabbit.hardware.Link;
import jwabbit.iface.Calc;
import jwabbit.iface.IEventCallback;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.MFILE;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Event check boot free.
 */
class EventCheckBootfree implements IEventCallback {

    /** WABBITEMU SOURCE: gui/gui.c, "BOOTFREE_VER_MAJOR" macro. */
    private static final int BOOTFREE_VER_MAJOR = 11;

    /** WABBITEMU SOURCE: gui/gui.c, "BOOTFREE_VER_MINOR" macro. */
    private static final int BOOTFREE_VER_MINOR = 259;

    /**
     * Constructs a new {@code EventCheckBootfree}.
     */
    EventCheckBootfree() {

        super();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "check_bootfree_and_update" function.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    @Override
    public final void exec(final Calc calc, final CalcUI theCalcUI) {

        if (calc.getModel().ordinal() < EnumCalcModel.TI_73.ordinal()) {
            return;
        }

        final Memory mem = calc.getCPU().getMemoryContext().getFlash();
        int index = (mem.getPages() - 1) * Memory.PAGE_SIZE + 0x0F;

        if (mem.get(index) != '1') {
            // not using bootfree
            return;
        }

        if (mem.get(index + 1) == '.') {
            // using normal bootpage
            return;
        }

        int majorVer = 0;
        int minorVer = 0;
        for (; ; ) {
            if (mem.get(index) >= '0' && mem.get(index) <= '9') {
                majorVer *= 10;
                majorVer += mem.get(index) - (int) '0';
            } else {
                break;
            }
            ++index;
        }
        if (mem.get(index) == '.') {
            ++index;
            for (; ; ) {
                if (mem.get(index) >= '0' && mem.get(index) <= '9') {
                    minorVer *= 10;
                    minorVer += mem.get(index) - (int) '0';
                } else {
                    break;
                }
                ++index;
            }
        }

        if (((long) minorVer + ((long) majorVer) << 32) < ((long) BOOTFREE_VER_MINOR + ((long) BOOTFREE_VER_MAJOR) << 32)) {

            final String[] hexFile = new String[1];

            RomWizard.extractBootFree(calc.getModel(), hexFile);
            if (hexFile[0] != null) {
                try (final FileInputStream file = new FileInputStream(hexFile[0])) {
                    Link.writeboot(file, calc.getCPU().getMemoryContext(), -1);
                } catch (final IOException ex) {
                    LoggedObject.LOG.warning("Failed to write boot file");
                }
            }

            final MFILE mfile = MFILE.exportRom(calc.getRomPath(), calc);
            mfile.mclose();
        }
    }
}
