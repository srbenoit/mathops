package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;

/**
 * The port 38 (0x26) device for the TI 83pse hardware.
 */
public final class D83psePort26 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort26}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort26(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port26_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus((cpu.getMemoryContext().getRam().getUpper() - 0x3FF) / 0x400);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
            cpu.getMemoryContext().getRam().setUpper((cpu.getBus() << 10) + 0x3FF);
        }
    }
}
