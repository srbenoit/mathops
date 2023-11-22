package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.LCD;

/**
 * The port 0 device for the TI 86 hardware.
 */
public final class D86Port00 extends AbstractDevice {

    /** The LCD to which this device interfaces. */
    private LCD lcd;

    /**
     * Constructs a new {@code Device86Port00}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port00(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.lcd = null;
    }

    /**
     * Gets the LCD to which this device interfaces.
     *
     * @return the LCD
     */
    public LCD getLcd() {

        return this.lcd;
    }

    /**
     * Sets the LCD to which this device interfaces.
     *
     * @param theLcd the LCD
     */
    public void setLcd(final LCD theLcd) {

        this.lcd = theLcd;
    }

    /**
     * WABBITEMU SOURCE: hardware/86hw.c, "port0" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(0);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            this.lcd.setScreenAddr(0x100 * ((cpu.getBus() % 0x40) + 0xC0));

            final D86Port10 port10 = new D86Port10(getDevIndex());
            port10.setLcd(this.lcd);
            port10.runCode(cpu);

            cpu.setOutput(false);

            this.lcd.getDataCallback().exec(cpu, this.lcd);
        }
    }
}
