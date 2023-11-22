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
 * The clock read device for the TI 83pse hardware.
 */
public final class D83pseClockRead extends AbstractDevice {

    /** The CLOCK to which this device interfaces. */
    private Clock clock;

    /**
     * Constructs a new {@code Device83pseClockRead}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pseClockRead(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "clock_read" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            final long time;
            if ((this.clock.getEnable() & 0x01) == 0) {
                time = this.clock.getBase();
            } else {
                time = (long) ((double) this.clock.getBase() + cpu.getTimerContext().getElapsed()
                        - this.clock.getLasttime());
            }

            final int shift = (getDevIndex() - 0x45) << 3;
            cpu.setBus((int) ((time >> shift) & 0x00FFL));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
