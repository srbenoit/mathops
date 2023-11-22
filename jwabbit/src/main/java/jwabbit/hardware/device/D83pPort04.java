package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.MemoryContext;
import jwabbit.hardware.STDINT;

/**
 * The port 4 device for the TI 83p hardware.
 */
public final class D83pPort04 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device83pPort04}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort04(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port4" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            int result = 0;
            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                result += 2;
            }
            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk2()) > this.stdint.getTimermax2()) {
                result += 4;
            }

            if (this.stdint.isOnLatch()) {
                result += 1;
            }
            if (cpu.getPIOContext().getKeypad().getOnPressed() == 0) {
                result += 8;
            }

            cpu.setBus(result);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            final int freq = (cpu.getBus() & 6) >> 1;
            this.stdint.setTimermax1(this.stdint.getFreq(freq));
            this.stdint.setTimermax2(this.stdint.getFreq(freq) / 2.0);
            this.stdint.setLastchk2(this.stdint.getLastchk1() + (this.stdint.getFreq(freq) / 4.0));

            final MemoryContext mc = cpu.getMemoryContext();
            if ((cpu.getBus() & 1) == 0) {
                mc.setBootmapped(false);
                mc.activateNormalBanks();
            } else {
                mc.setBootmapped(true);
                mc.activateBootmapBanks();
                mc.updateBootmapPages();
            }
            cpu.setOutput(false);
        }
    }
}
