package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.USB;

/**
 * The port 84 (0x54) device for the TI 83pse hardware.
 */
public final class D83psePort54 extends AbstractDevice {

    /** The USB to which this device interfaces. */
    private USB usb;

    /**
     * Constructs a new {@code Device83psePort54}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort54(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.usb = null;
    }

    /**
     * Gets the USB to which this device interfaces.
     *
     * @return the USB
     */
    public USB getUsb() {

        return this.usb;
    }

    /**
     * Sets the USB to which this device interfaces.
     *
     * @param theUsb the USB
     */
    public void setUsb(final USB theUsb) {

        this.usb = theUsb;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port54_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.usb.getPort54() & (1 | (1 << 1) | (1 << 2) | (1 << 6) | (1 << 7)));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
            this.usb.setPort54(cpu.getBus() & (1 | (1 << 1) | (1 << 2) | (1 << 6) | (1 << 7)));
            this.usb.setUSBPowered((cpu.getBus() & (1 << 1)) == 0);
        }
    }
}
