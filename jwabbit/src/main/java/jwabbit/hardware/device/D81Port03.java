package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.STDINT;

/**
 * The port 3 device for the TI 81 hardware.
 */
public final class D81Port03 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device81Port03}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D81Port03(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/81hw.c, "port3" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            int result = 0;

            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint
                    .getTimermax1()) {
                result += 4;
            }

            if (cpu.getPIOContext().getLcd().isActive()) {
                result += 2;
            }

            if (this.stdint.isOnLatch()) {
                result += 1;
            } else {
                result += 8;
            }

            cpu.setBus(result);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.getPIOContext().getLcd().setActive((cpu.getBus() & 0x08) != 0);

            if ((cpu.getBus() & 0x01) == 0) {
                this.stdint.setOnLatch(false);
            }

            this.stdint.setIntactive(cpu.getBus() & ((1 << 2) | 1));
            cpu.setOutput(false);
        }

        if ((this.stdint.getIntactive() & 0x04) == 0 && cpu.getPIOContext().getLcd().isActive()) {
            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint
                    .getTimermax1()) {
                cpu.setInterrupt(true);
                while ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                    this.stdint.setLastchk1(this.stdint.getLastchk1() + this.stdint.getTimermax1());
                }
            }
        }

        if ((this.stdint.getIntactive() & 0x01) != 0
                && (cpu.getPIOContext().getKeypad().getOnPressed()
                & HardwareConstants.KEY_VALUE_MASK) != 0
                && (this.stdint.getOnBackup() & HardwareConstants.KEY_VALUE_MASK) == 0) {
            this.stdint.setOnLatch(true);
        }
        this.stdint.setOnBackup(cpu.getPIOContext().getKeypad().getOnPressed());
        if (this.stdint.isOnLatch()) {
            cpu.setInterrupt(true);
        }
    }
}
