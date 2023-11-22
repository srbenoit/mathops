package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;

/**
 * The port 0 device for the TI 83p hardware.
 */
public final class D83pPort00 extends AbstractDevice {

    /** The LinkAssist to which this device interfaces. */
    private LinkAssist linkAssist;

    /**
     * Constructs a new {@code Device83pPort00}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort00(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port0" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final Link link = cpu.getPIOContext().getLink();

        if (cpu.isInput()) {
            link.setHasChanged(false);
            link.setChangedTime(0L);

            cpu.setBus((((link.getHost() & 0x03) | (link.getClient()[0] & 0x03)) ^ 0x03)
                    + (this.linkAssist.getLinkEnable() & (1 << 2)));
            if (this.linkAssist.isRead()) {
                cpu.setBus(cpu.getBus() + (1 << 3));
            }
            if (this.linkAssist.isReceiving()) {
                cpu.setBus(cpu.getBus() + (1 << 6));
            }
            cpu.setBus(cpu.getBus() + (cpu.getLinkWrite() << 4));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            if ((link.getHost() & 0x01) != (cpu.getBus() & 0x01)) {
                cpu.flippedLeft(cpu.getBus() & 0x01);
            }
            if ((link.getHost() & 0x02) != (cpu.getBus() & 0x02)) {
                cpu.flippedRight((cpu.getBus() & 0x02) >> 1);
            }

            this.linkAssist.setLinkEnable(cpu.getBus() & (1 << 2));
            link.setHost(cpu.getBus() & 0x03);
            cpu.setLinkWrite(link.getHost());
            cpu.setOutput(false);
        }

        if (link.getAudio().isInit() && link.getAudio().isEnabled()) {
            cpu.nextSample();
        }
    }
}
