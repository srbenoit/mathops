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
 * The port 0 device for the TI 83pse hardware.
 */
public final class D83psePort00 extends AbstractDevice {

    /** The Link to which this device interfaces. */
    private Link link;

    /**
     * Constructs a new {@code Device83psePort00}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort00(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port0_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            final int bus = ((this.link.getHost() & 0x03) | (this.link.getClient()[0] & 0x03)) ^ 0x03;
            cpu.setBus(bus + (cpu.getLinkWrite() << 4));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            if ((this.link.getHost() & 0x01) != (cpu.getBus() & 0x01)) {
                cpu.flippedLeft(cpu.getBus() & 0x01);
            }
            if ((this.link.getHost() & 0x02) != (cpu.getBus() & 0x02)) {
                cpu.flippedRight((cpu.getBus() & 0x02) >> 1);
            }

            this.link.setHost(cpu.getBus() & 0x03);
            cpu.setLinkWrite(this.link.getHost());
            cpu.setOutput(false);
        }

        if (this.link.getAudio().isInit() && this.link.getAudio().isEnabled()) {
            cpu.nextSample();
        }
    }
}
