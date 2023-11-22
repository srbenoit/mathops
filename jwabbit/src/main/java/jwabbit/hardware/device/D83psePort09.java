package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Hw83pse;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;

/**
 * The port 9 device for the TI 83pse hardware.
 */
public final class D83psePort09 extends AbstractDevice {

    /** The LinkAssist to which this device interfaces. */
    private LinkAssist linkAssist;

    /**
     * Constructs a new {@code Device83psePort09}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort09(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port9_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final Link link = cpu.getPIOContext().getLink();

        if (!cpu.isInput() && !cpu.isOutput()) {
            if ((this.linkAssist.getLinkEnable() & 0x80) == 0) {

                if (this.linkAssist.isSending()) {
                    this.linkAssist.setReady(false);
                    switch (Hw83pse.linkRead(cpu) & 0x03) {
                        case 0:
                            // Bit acknowledged
                            if (link.getHost() != 0) {
                                this.linkAssist.setBit(this.linkAssist.getBit() + 1);
                            }
                            link.setHost(0);
                            break;
                        case 3:
                            if (this.linkAssist.getBit() >= 8) {
                                this.linkAssist.setSending(false);
                                this.linkAssist.setReady(true);
                                this.linkAssist.setBit(0);
                            } else if ((this.linkAssist.getOut()
                                    & (1 << this.linkAssist.getBit())) != 0) {
                                link.setHost(2);
                            } else {
                                link.setHost(1);
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    if (Hw83pse.linkRead(cpu) != 3) {
                        this.linkAssist.setReady(false);
                    }

                    switch (Hw83pse.linkRead(cpu) & 0x03) {
                        case 1:
                            if (this.linkAssist.getBit() < 8) {
                                if (!this.linkAssist.isReceiving()) {
                                    this.linkAssist.setBit(0);
                                }
                                this.linkAssist.setReceiving(true);
                                if (link.getHost() == 0) {
                                    this.linkAssist
                                            .setWorking((this.linkAssist.getWorking() >> 1) + 128);
                                    this.linkAssist.setBit(this.linkAssist.getBit() + 1);
                                    link.setHost(1);
                                } else {
                                    link.setHost(0);
                                }
                            } else if (this.linkAssist.getBit() == 8) {
                                link.setHost(0);
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
                                link.setHost(0);
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
                            break;
                    }
                }
                if ((this.linkAssist.getLinkEnable() & 0x01) != 0 && this.linkAssist.isRead()) {
                    cpu.setInterrupt(true);
                }
                if ((this.linkAssist.getLinkEnable() & 0x02) != 0 && this.linkAssist.isReady()) {
                    cpu.setInterrupt(true);
                }
                if ((this.linkAssist.getLinkEnable() & 0x04) != 0 && this.linkAssist.isError()) {
                    cpu.setInterrupt(true);
                }
            }
        }

        if (cpu.isInput()) {
            cpu.setBus(0x00);
            if ((this.linkAssist.getLinkEnable() & 0x80) == 0) {
                if ((this.linkAssist.getLinkEnable() & 0x01) != 0 && this.linkAssist.isRead()) {
                    cpu.setBus(cpu.getBus() + 1);
                }
                if ((this.linkAssist.getLinkEnable() & 0x02) != 0 && this.linkAssist.isReady()) {
                    cpu.setBus(cpu.getBus() + 2);
                }
                if ((this.linkAssist.getLinkEnable() & 0x04) != 0 && this.linkAssist.isError()) {
                    cpu.setBus(cpu.getBus() + 4);
                }
                if (this.linkAssist.isReceiving()) {
                    cpu.setBus(cpu.getBus() + 8);
                }
                if (this.linkAssist.isRead()) {
                    cpu.setBus(cpu.getBus() + 16);
                }
                if (this.linkAssist.isReady()) {
                    cpu.setBus(cpu.getBus() + 32);
                }
                if (this.linkAssist.isError()) {
                    cpu.setBus(cpu.getBus() + 64);
                }
                if (this.linkAssist.isSending()) {
                    cpu.setBus(cpu.getBus() + 128);
                }
            }
            cpu.setInput(false);
            this.linkAssist.setError(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
