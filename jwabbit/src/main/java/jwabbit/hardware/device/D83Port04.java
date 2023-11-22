package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.IDevice;
import jwabbit.hardware.Hw83;
import jwabbit.hardware.Link;
import jwabbit.hardware.STDINT;

/**
 * The port 4 device for the TI 83 hardware.
 */
public final class D83Port04 extends AbstractDevice {

    /** The STDINT to which this device interfaces. */
    private STDINT stdint;

    /**
     * Constructs a new {@code Device83Port04}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83Port04(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.stdint = null;
    }

    /**
     * Gets the STDINT to which this device interfaces.
     *
     * @return the STDINT
     */
    public STDINT getStdint() {

        return this.stdint;
    }

    /**
     * Sets the STDINT to which this device interfaces.
     *
     * @param theStdint the STDINT
     */
    public void setStdint(final STDINT theStdint) {

        this.stdint = theStdint;
    }

    /**
     * WABBITEMU SOURCE: hardware/83hw.c, "port04_83" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final IDevice dev = cpu.getPIOContext().getDevice(0x00);
        Link link = null;
        if (dev instanceof D83Port00) {
            final D83Port00 dev00 = (D83Port00) dev;
            link = dev00.getLink();
        } else if (dev instanceof D82Port00) {
            final D82Port00 dev00 = (D82Port00) dev;
            link = dev00.getLink();
        }

        if (link != null) {
            if (cpu.isInput()) {
                final int bus = ((link.getHost() & 0x03) | (link.getClient()[0] & 0x03))
                        ^ 0x03 + ((link.getHost() & 0x03) << 2) + this.stdint.getXy();
                cpu.setBus(bus);
                cpu.setInput(false);
            } else if (cpu.isOutput()) {
                final int freq = (cpu.getBus() & 6) >> 1;
                this.stdint.setTimermax1(this.stdint.getFreq(freq));
                this.stdint.setTimermax2(this.stdint.getFreq(freq) / 2.0);
                this.stdint.setLastchk2(this.stdint.getLastchk1() + (this.stdint.getFreq(freq) / 4.0));

                cpu.getMemoryContext().setBootmapped((cpu.getBus() & 1) == 1);
                Hw83.setpage83(cpu);
                cpu.setOutput(false);
            }
        }
    }
}
