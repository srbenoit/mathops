package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;

/**
 * The port 22 (0x16) device for the TI 83p hardware.
 */
public final class D83pPort16 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83pPort16}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort16(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port16" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            // NOTE: WABBITEMU code calls port 14 code, which does nothing if isInput() is true but
            // set input to false, so we skip that here
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            int offset = cpu.getMemoryContext().getProtectedPageSet();
            if (offset == 7) {
                offset = 3;
            }
            cpu.getMemoryContext().setProtectedPage(offset, cpu.getBus());
            cpu.setOutput(false);
        }
    }
}
