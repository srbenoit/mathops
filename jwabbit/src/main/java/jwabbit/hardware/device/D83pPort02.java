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
 * The port 2 device for the TI 83p hardware.
 */
public final class D83pPort02 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83pPort02}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort02(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port2" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus((cpu.getPIOContext().getModel() == EnumCalcModel.TI_73 ? 0x39 : 0x3B)
                    | (cpu.getMemoryContext().isFlashLocked() ? 0 : 4));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
