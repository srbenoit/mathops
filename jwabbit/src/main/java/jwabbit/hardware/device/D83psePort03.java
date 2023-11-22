package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.STDINT;

/**
 * The port 3 device for the TI 83pse hardware.
 */
public final class D83psePort03 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device83psePort03}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort03(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port3_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.stdint.getIntactive());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {

            if (((this.stdint.getIntactive() & (1 << 3)) == 0) && (cpu.getBus() & (1 << 3)) != 0) {
                cpu.getPIOContext().getLcd().setActive(true);
            }

            if ((cpu.getBus() & 1) == 0) {
                this.stdint.setOnLatch(false);
            }

            this.stdint.setIntactive(cpu.getBus());
            cpu.setOutput(false);
        }

        if ((this.stdint.getIntactive() & (1 << 3)) == 0 && cpu.isHalt()
                && cpu.getPIOContext().getModel().ordinal() < EnumCalcModel.TI_84PCSE.ordinal()) {
            cpu.getPIOContext().getLcd().setActive(false);
        }

        if ((this.stdint.getIntactive() & (1 << 1)) == 0) {
            while ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                this.stdint.setLastchk1(this.stdint.getLastchk1() + this.stdint.getTimermax1());
            }
        } else if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
            cpu.setInterrupt(true);
        }

        if ((this.stdint.getIntactive() & (1 << 2)) == 0) {
            while ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk2()) > this.stdint.getTimermax2()) {
                this.stdint.setLastchk2(this.stdint.getLastchk2() + this.stdint.getTimermax2());
            }
        } else if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk2()) > this.stdint.getTimermax2()) {
            cpu.setInterrupt(true);
        }

        if ((this.stdint.getIntactive() & 1) != 0
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
