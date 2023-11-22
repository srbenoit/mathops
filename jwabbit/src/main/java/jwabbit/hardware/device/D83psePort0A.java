package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.LinkAssist;

/**
 * The port 10 (0x0A) device for the TI 83pse hardware.
 */
public final class D83psePort0A extends AbstractDevice {

    /** The LinkAssist to which this device interfaces. */
    private LinkAssist linkAssist;

    /**
     * Constructs a new {@code Device83psePort0A}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort0A(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.linkAssist = null;
    }

    /**
     * Sets the LinkAssist to which this device interfaces.
     *
     * @param theLinkAssist the LinkAssist
     */
    public void setLinkAssist(final LinkAssist theLinkAssist) {

        this.linkAssist = theLinkAssist;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port0A_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.linkAssist.getIn());
            this.linkAssist.setRead(false);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.linkAssist.setRead(false);
            cpu.setOutput(false);
        }
    }
}
