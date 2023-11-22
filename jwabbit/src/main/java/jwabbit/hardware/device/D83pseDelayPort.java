package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Delay;
import jwabbit.hardware.Hw83pse;

/**
 * The DELAY device for the TI 83pse hardware.
 */
public final class D83pseDelayPort extends AbstractDevice {

    /** The delay. */
    private Delay delay;

    /**
     * Constructs a new {@code DeviceDelayPorts}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pseDelayPort(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.delay = null;
    }

    /**
     * Gets the DELAY.
     *
     * @return theDELAY
     */
    public Delay getDelay() {

        return this.delay;
    }

    /**
     * Sets the DELAY.
     *
     * @param theDelay the DELAY
     */
    public void setDelay(final Delay theDelay) {

        this.delay = theDelay;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "delay_ports" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.delay.getReg(getDevIndex() - 0x29));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.delay.setReg(getDevIndex() - 0x29, cpu.getBus());
            cpu.setOutput(false);
            Hw83pse.updateDelays(cpu, this.delay);
        }
    }
}
