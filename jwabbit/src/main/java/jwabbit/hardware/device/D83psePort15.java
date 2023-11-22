package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;

/**
 * The port 21 (0x15) device for the TI 83pse hardware.
 */
public final class D83psePort15 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort15}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort15(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port15_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            if (cpu.getPIOContext().getModel() == EnumCalcModel.TI_83PSE) {
                cpu.setBus(0x33);
            } else if (cpu.getPIOContext().getModel() == EnumCalcModel.TI_84PCSE) {
                cpu.setBus(0x45);
            } else if (cpu.getMemoryContext().getRam().getVersion() == 0) {
                cpu.setBus(0x44);
            } else {
                cpu.setBus(0x55);
            }

            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
