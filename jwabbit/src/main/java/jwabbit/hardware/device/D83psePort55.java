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
 * The port 85 (0x55) device for the TI 83pse hardware.
 */
public final class D83psePort55 extends AbstractDevice {

    /** WABBITEMU SOURCE: hardware/83psehw.c, "USB_LINE_INTERRUPT_MASK" macro. */
    private static final int USB_LINE_INTERRUPT_MASK = 1 << 2;

    /** WABBITEMU SOURCE: hardware/83psehw.c, "USB_PROTOCOL_INTERRUPT_MASK" macro. */
    private static final int USB_PROTOCOL_INTERRUPT_MASK = 1 << 4;

    /** The USB to which this device interfaces. */
    private USB usb;

    /**
     * Constructs a new {@code Device83psePort55}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort55(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port55_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(1 | (1 << 1) | (1 << 3));

            if (!this.usb.isLineInterrupt()) {
                cpu.setBus(cpu.getBus() + USB_LINE_INTERRUPT_MASK);
            }
            if (!this.usb.isProtocolInterrupt()) {
                cpu.setBus(cpu.getBus() + USB_PROTOCOL_INTERRUPT_MASK);
            }

            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
