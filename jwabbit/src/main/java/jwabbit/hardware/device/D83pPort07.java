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
 * The port 7 device for the TI 83p hardware.
 */
public final class D83pPort07 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83pPort07}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort07(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port7" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();

        if (cpu.isInput()) {
            cpu.setBus(((mc.getBanks()[2].isRam() ? 1 : 0) << 6) + mc.getBanks()[2].getPage());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            final boolean ram = ((cpu.getBus() >> 6) & 1) != 0;
            if (ram) {
                mc.changePage(2, (cpu.getBus() & 0x1f) % mc.getRam().getPages(), ram);
            } else {
                mc.changePage(2, (cpu.getBus() & 0x1f) % mc.getFlash().getPages(), ram);
            }
            cpu.setOutput(false);
        }
    }
}
