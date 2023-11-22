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
 * The port 7 device for the TI 83pse hardware.
 */
public final class D83psePort07 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort07}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort07(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port7_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();

        if (cpu.isInput()) {
            cpu.setBus(((mc.getBanks()[2].isRam() ? 1 : 0) << 7) + (mc.getBanks()[2].getPage() & 0x7F));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.getMemoryContext().setPort07(cpu.getBus());
            updateBank2(cpu);
            cpu.setOutput(false);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "update_bank_2" function.
     *
     * @param cpu the CPU
     */
    static void updateBank2(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();
        final boolean ram = ((mc.getPort07() >> 7) & 1) != 0;
        if (ram) {
            final int page = mc.getPort07() & (mc.getRam().getPages() - 1);
            mc.changePage(2, page, ram);
        } else {
            final int page = (mc.getPort07() & 0x7F) | (mc.getPort0F() << 7);
            mc.changePage(2, page & (mc.getFlash().getPages() - 1), ram);
        }
    }
}
