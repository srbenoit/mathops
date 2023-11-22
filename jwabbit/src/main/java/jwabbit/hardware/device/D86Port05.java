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
import jwabbit.hardware.STDINT;

/**
 * The port 5 device for the TI 86 hardware.
 */
public final class D86Port05 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device86Port05}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port05(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.stdint = null;
    }

    /**
     * Gets the STDINT to which this device interfaces.
     *
     * @return the STDINT
     */
    public STDINT getStdint() {

        return this.stdint;
    }

    /**
     * Sets the STDINT to which this device interfaces.
     *
     * @param theStdint the STDINT
     */
    public void setStdint(final STDINT theStdint) {

        this.stdint = theStdint;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "port5" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final MemoryContext mc = cpu.getMemoryContext();
        final BankState[] banks = mc.getBanks();

        if (cpu.isInput()) {
            cpu.setBus(((banks[1].isRam() ? 1 : 0) << 6) + banks[1].getPage());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            banks[1].setRam(((cpu.getBus() >> 6) & 1) != 0);
            if (banks[1].isRam()) {
                banks[1].setPage((cpu.getBus() & 0x1f) % mc.getRam().getPages());
                banks[1].setMem(mc.getRam());
                banks[1].setAddr(banks[1].getPage() * Memory.PAGE_SIZE);
                banks[1].setReadOnly(false);
                banks[1].setNoExec(false);
            } else {
                banks[1].setPage((cpu.getBus() & 0x1f) % mc.getFlash().getPages());
                banks[1].setMem(mc.getFlash());
                banks[1].setAddr(banks[1].getPage() * Memory.PAGE_SIZE);
                banks[1].setReadOnly(true);
                banks[1].setNoExec(false);
                if (banks[1].getPage() == 0x1f) {
                    banks[1].setReadOnly(true);
                }
            }
            cpu.setOutput(false);
        }
    }
}
