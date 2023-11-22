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
 * The port 91 (0x5B) device for the TI 83pse hardware.
 */
public final class D83psePort5B extends AbstractDevice {

    /** The USB to which this device interfaces. */
    private USB usb;

    /**
     * Constructs a new {@code Device83psePort5B}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort5B(final int theDevIndex) {

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
     * Runs the device code.
     *
     * <p>
     * SOURCE: hardware/83psehw.c, "port5B_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(0x00);
            if (this.usb.isProtocolInterruptEnabled()) {
                cpu.setBus(cpu.getBus() + 1);
            }
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.usb.setProtocolInterruptEnabled((cpu.getBus() & 1) != 0);
            cpu.setOutput(false);
        }
    }
}
