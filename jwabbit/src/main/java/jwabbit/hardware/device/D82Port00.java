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
 * The port 0 device for the TI 82 hardware.
 */
public final class D82Port00 extends AbstractDevice {

    /** The Link to which this device interfaces. */
    private Link link;

    /**
     * Constructs a new {@code Device82Port00}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D82Port00(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83hw.c, "port00_82" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(((this.link.getHost() & 0x03)) << 2);
            cpu.setBus(cpu.getBus() + (((this.link.getHost() & 0x03) | (this.link.getClient()[0] & 0x03)) ^ 0x03));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            if ((this.link.getHost() & 0x01) != ((cpu.getBus() & 0x04) >> 2)) {
                cpu.flippedLeft((cpu.getBus() & 0x04) >> 2);
            }
            if ((this.link.getHost() & 0x02) != ((cpu.getBus() & 0x08) >> 2)) {
                cpu.flippedRight((cpu.getBus() & 0x08) >> 3);
            }

            this.link.setHost((cpu.getBus() & 0x0C) >> 2);
            cpu.setOutput(false);
        }

        if (this.link.getAudio().isInit() && this.link.getAudio().isEnabled()) {
            cpu.nextSample();
        }

    }
}
