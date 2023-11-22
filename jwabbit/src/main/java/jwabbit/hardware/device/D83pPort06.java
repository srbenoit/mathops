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
 * The port 6 device for the TI 83p hardware.
 */
public final class D83pPort06 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83pPort06}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort06(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port6" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();

        if (cpu.isInput()) {
            cpu.setBus(((mc.getBanks()[1].isRam() ? 1 : 0) << 6) + mc.getBanks()[1].getPage());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            final boolean ram = ((cpu.getBus() >> 6) & 1) != 0;
            if (ram) {
                mc.changePage(1, (cpu.getBus() & 0x1f) % mc.getRam().getPages(), ram);
            } else {
                mc.changePage(1, (cpu.getBus() & 0x1f) % mc.getFlash().getPages(), ram);
            }
            cpu.setOutput(false);
        }
    }
}
