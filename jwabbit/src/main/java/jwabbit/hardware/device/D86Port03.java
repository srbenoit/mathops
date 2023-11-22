package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.PIOContext;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.STDINT;

/**
 * The port 3 device for the TI 86 hardware.
 */
public final class D86Port03 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device86Port03}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port03(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/86hw.c, "port3" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final PIOContext pio = cpu.getPIOContext();

        if (cpu.isInput()) {
            int result = 0;
            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                result += 4;
            }
            if (pio.getLcd().isActive()) {
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
            pio.getLcd().setActive((cpu.getBus() & 0x08) != 0);

            if ((cpu.getBus() & 0x01) == 0) {
                this.stdint.setOnLatch(false);
            }

            this.stdint.setIntactive(cpu.getBus());
            cpu.setOutput(false);
        }

        if ((this.stdint.getIntactive() & 0x04) == 0 && pio.getLcd().isActive()) {
            if ((cpu.getTimerContext().getElapsed() - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                cpu.setInterrupt(true);

                while ((cpu.getTimerContext().getElapsed()
                        - this.stdint.getLastchk1()) > this.stdint.getTimermax1()) {
                    this.stdint.setLastchk1(this.stdint.getLastchk1() + this.stdint.getTimermax1());
                }
            }
        }

        if ((this.stdint.getIntactive() & 0x01) != 0
                && (pio.getKeypad().getOnPressed() & HardwareConstants.KEY_VALUE_MASK) != 0
                && (this.stdint.getOnBackup() & HardwareConstants.KEY_VALUE_MASK) == 0) {
            this.stdint.setOnLatch(true);
        }
        this.stdint.setOnBackup(pio.getKeypad().getOnPressed());
        if (this.stdint.isOnLatch()) {
            cpu.setInterrupt(true);
        }
    }
}
