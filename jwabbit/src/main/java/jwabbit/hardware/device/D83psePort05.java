package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;

/**
 * The port 5 device for the TI 83pse hardware.
 */
public final class D83psePort05 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort05}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort05(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port5_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(cpu.getMemoryContext().getBanks()[3].getPage());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.getMemoryContext().changePage(3,
                    (cpu.getBus() & 0x7f) % cpu.getMemoryContext().getRam().getPages(), true);
            cpu.setOutput(false);
        }
    }
}
