package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Timer;
import jwabbit.hardware.XTAL;

/**
 * The port 49 (0x31) device for the TI 83pse hardware.
 */
public final class D83psePort31 extends AbstractDevice {

    /** The XTAL to which this device interfaces. */
    private XTAL xtal;

    /**
     * Constructs a new {@code Device83psePort31}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort31(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.xtal = null;
    }

    /**
     * Sets the XTAL to which this device interfaces.
     *
     * @param theXtal the XTAL
     */
    public void setXtal(final XTAL theXtal) {

        this.xtal = theXtal;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port31_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final Timer timer = this.xtal.getTimer((getDevIndex() - 0x30) / 3);

        if (cpu.isInput()) {
            cpu.setBus((timer.isLoop() ? 1 : 0) + (timer.isInterrupt() ? 2 : 0) + (timer.isUnderflow() ? 4 : 0));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            timer.setLoop((cpu.getBus() & 0x01) != 0);
            timer.setInterrupt((cpu.getBus() & 0x02) != 0);
            timer.setUnderflow(false);
            timer.setGenerate(false);
            cpu.setOutput(false);
        }
    }
}
