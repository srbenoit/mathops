package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;

/**
 * The port 6 device for the TI 81 hardware.
 */
public final class D81Port06 extends AbstractDevice {

    /**
     * Constructs a new {@code Device81Port06}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D81Port06(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/81hw.c, "port6" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(0x00);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
