package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.PIOContext;
import jwabbit.hardware.Hw83p;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;
import jwabbit.log.LoggedObject;

/**
 * The port 5 device for the TI 83p hardware.
 */
public final class D83pPort05 extends AbstractDevice {

    /** The LinkAssist to which this device interfaces. */
    private LinkAssist linkAssist;

    /**
     * Constructs a new {@code Device83pPort05}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83pPort05(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83phw.c, "port5" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final PIOContext pio = cpu.getPIOContext();
        final Link link = pio.getLink();

        if (!cpu.isInput() && !cpu.isOutput()) {
            if ((this.linkAssist.getLinkEnable() & 0x04) != 0) {
                if (Hw83p.linkRead(cpu) != 3) {
                    this.linkAssist.setReady(false);
                }

                switch (Hw83p.linkRead(cpu) & 0x03) {
                    case 1:
                        if (this.linkAssist.getBit() < 8) {
                            if (!this.linkAssist.isReceiving()) {
                                this.linkAssist.setBit(0);
                            }
                            this.linkAssist.setReceiving(true);
                            if (link.getHost() == 0) {
                                this.linkAssist.setWorking((this.linkAssist.getWorking() >> 1) + 128);
                                this.linkAssist.setBit(this.linkAssist.getBit() + 1);
                                link.setHost(1);
                            } else {
                                link.setHost(0);
                            }
                        } else if (this.linkAssist.getBit() == 8) {
                            if (link.getHost() != 0) {
                                link.setHost(0);
                            }
                            if (!this.linkAssist.isRead()) {
                                this.linkAssist.setReceiving(false);
                                this.linkAssist.setRead(true);
                                this.linkAssist.setIn(this.linkAssist.getWorking());
                                this.linkAssist.setBit(0);
                            }
                        }
                        break;

                    case 2:
                        if (this.linkAssist.getBit() < 8) {
                            if (!this.linkAssist.isReceiving()) {
                                this.linkAssist.setBit(0);
                            }
                            this.linkAssist.setReceiving(true);
                            if (link.getHost() == 0) {
                                this.linkAssist.setWorking(this.linkAssist.getWorking() >> 1);
                                this.linkAssist.setBit(this.linkAssist.getBit() + 1);
                                link.setHost(2);
                            } else {
                                link.setHost(0);
                            }
                        } else if (this.linkAssist.getBit() == 8) {
                            if (link.getHost() != 0) {
                                link.setHost(0);
                            }
                            if (!this.linkAssist.isRead()) {
                                this.linkAssist.setReceiving(false);
                                this.linkAssist.setRead(true);
                                this.linkAssist.setIn(this.linkAssist.getWorking());
                                this.linkAssist.setBit(0);
                            }
                        }
                        break;

                    case 3:
                        if (this.linkAssist.getBit() >= 8 && !this.linkAssist.isRead()) {
                            this.linkAssist.setReceiving(false);
                            this.linkAssist.setRead(true);
                            this.linkAssist.setIn(this.linkAssist.getWorking());
                            this.linkAssist.setBit(0);
                        } else if (this.linkAssist.getBit() == 0) {
                            this.linkAssist.setReady(true);
                        }
                        break;

                    default:
                        LoggedObject.LOG.warning("Unhandled case");
                        break;
                }
            }
        }

        if (cpu.isInput()) {
            this.linkAssist.setRead(false);
            cpu.setBus(this.linkAssist.getIn());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.getMemoryContext().setProtectedPageSet(cpu.getBus() & 0x07);
            cpu.setOutput(false);
        }
    }
}
