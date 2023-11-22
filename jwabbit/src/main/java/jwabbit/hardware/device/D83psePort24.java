package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.MemoryContext;

/**
 * The port 36 (0x24) device for the TI 83pse hardware.
 */
public final class D83psePort24 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort24}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort24(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port24_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();

        if (cpu.isInput()) {
            cpu.setInput(false);
            cpu.setBus(mc.getPort24());
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
            mc.setPort24(cpu.getBus());
            mc.getFlash().setUpper((mc.getFlash().getUpper() & 0x00FF) | (cpu.getBus() & (1 << 1) << 7));
            mc.getFlash().setLower((mc.getFlash().getLower() & 0x00FF) | (cpu.getBus() & 1 << 8));
        }
    }
}
