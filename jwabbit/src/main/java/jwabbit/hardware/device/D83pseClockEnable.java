package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Clock;

/**
 * The clock enable device for the TI 83pse hardware.
 */
public final class D83pseClockEnable extends AbstractDevice {

    /** The CLOCK to which this device interfaces. */
    private Clock clock;

    /**
     * Constructs a new {@code Device83pseClockEnable}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pseClockEnable(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.clock = null;
    }

    /**
     * Gets the CLOCK to which this device interfaces.
     *
     * @return the CLOCK
     */
    public Clock getClock() {

        return this.clock;
    }

    /**
     * Sets the CLOCK to which this device interfaces.
     *
     * @param theClock the CLOCK
     */
    public void setClock(final Clock theClock) {

        this.clock = theClock;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "clock_enable" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.clock.getEnable() & 0x03);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            if ((this.clock.getEnable() & 0x02) == 0 && (cpu.getBus() & 0x02) == 2) {
                this.clock.setBase(this.clock.getSet());
            }
            if ((this.clock.getEnable() & 0x01) == 0 && (cpu.getBus() & 0x01) == 1) {
                this.clock.setLasttime(cpu.getTimerContext().getElapsed());
            }
            if ((this.clock.getEnable() & 0x01) == 1 && (cpu.getBus() & 0x01) == 0) {
                this.clock.setBase(this.clock.getBase()
                        + (long) (cpu.getTimerContext().getElapsed() - this.clock.getLasttime()));
            }
            this.clock.setEnable(cpu.getBus() & 0x03);

            cpu.setOutput(false);
        }
    }
}
