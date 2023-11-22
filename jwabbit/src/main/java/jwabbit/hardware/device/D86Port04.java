package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.STDINT;

/**
 * The port 4 device for the TI 86 hardware.
 */
public final class D86Port04 extends AbstractDevice {

    /** The data value. */
    private int data;

    /**
     * Constructs a new {@code Device86Port04}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port04(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.data = 0;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public int getData() {

        return this.data;
    }

    /**
     * Sets the data.
     *
     * @param theData the data
     */
    public void setData(final int theData) {

        this.data = theData;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "port4" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(1);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.data = cpu.getBus();
            final int freq = (this.data >> 1) & 0x03;
            final STDINT stdint = cpu.getPIOContext().getStdint();
            stdint.setTimermax1(stdint.getFreq(freq));
            stdint.setLastchk1(cpu.getTimerContext().getElapsed());
            final int lcdMode = (this.data >> 3) & 0x03;
            if (lcdMode == 0) {
                cpu.getPIOContext().getLcd().setWidth(80);
            } else {
                cpu.getPIOContext().getLcd().setWidth(32 * lcdMode + 64);
            }

            cpu.setOutput(false);
        }
    }
}
