package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Link;

/**
 * The port 7 device for the TI 86 hardware.
 */
public final class D86Port07 extends AbstractDevice {

    /** The Link to which this device interfaces. */
    private Link link;

    /**
     * Constructs a new {@code Device86Port07}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port07(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.link = null;
    }

    /**
     * Gets the Link to which this device interfaces.
     *
     * @return the Link
     */
    public Link getLink() {

        return this.link;
    }

    /**
     * Sets the Link to which this device interfaces.
     *
     * @param theLink the Link
     */
    public void setLink(final Link theLink) {

        this.link = theLink;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "port7" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(((this.link.getHost() & 0x03) | (this.link.getClient()[0] & 0x03)) ^ 0x03);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.link.setHost((cpu.getBus() >> 4) & 0x03);
            cpu.setOutput(false);
        }
    }
}
