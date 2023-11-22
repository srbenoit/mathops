package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Hw83pse;
import jwabbit.hardware.USB;

/**
 * The port 77 (0x4D) device for the TI 83pse hardware.
 */
public final class D83psePort4D extends AbstractDevice {

    /** The USB to which this device interfaces. */
    private USB usb;

    /**
     * Constructs a new {@code Device83psePort4D}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort4D(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port4D_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus((int) this.usb.getUSBLineState());

            if ((this.usb.getPort54() & (1 << 2)) != 0 && (this.usb.getPort54() & (1 << 6)) != 0
                    && (this.usb.getPort4C() & (1 << 3)) != 0
                    && (this.usb.getUSBLineState() & (long) Hw83pse.VBUS_HIGH_MASK) != 0L) {
                cpu.setBus(cpu.getBus() | ((1 << 1) & ~1));
            } else {
                cpu.setBus(cpu.getBus() | (1 & ~(1 << 1)));
            }

            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);
        }
    }
}
