package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.BankState;
import jwabbit.core.CPU;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;

/**
 * The port 6 device for the TI 86 hardware.
 */
public final class D86Port06 extends AbstractDevice {

    /**
     * Constructs a new {@code Device86Port06}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port06(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/86hw.c, "port6" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();
        final BankState[] banks = mc.getBanks();

        if (cpu.isInput()) {
            cpu.setBus(((banks[2].isRam() ? 1 : 0) << 6) + banks[2].getPage());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            banks[2].setRam(((cpu.getBus() >> 6) & 1) != 0);
            if (banks[2].isRam()) {
                banks[2].setPage(cpu.getBus() & 0x07);
                banks[2].setMem(mc.getRam());
                banks[2].setAddr(banks[2].getPage() * Memory.PAGE_SIZE);
                banks[2].setReadOnly(false);
                banks[2].setNoExec(false);
            } else {
                banks[2].setPage(cpu.getBus() & 0x0f);
                banks[2].setMem(mc.getFlash());
                banks[2].setAddr(banks[2].getPage() * Memory.PAGE_SIZE);
                banks[2].setReadOnly(true);
                banks[2].setNoExec(false);
                if (banks[2].getPage() == 0x1f) {
                    banks[2].setReadOnly(true);
                }
            }
            cpu.setOutput(false);
        }
    }
}
