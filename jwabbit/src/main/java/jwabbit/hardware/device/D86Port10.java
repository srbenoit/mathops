package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.BankState;
import jwabbit.core.CPU;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.LCD;

/**
 * The port 16 (0x10) device for the TI 86 hardware.
 */
public final class D86Port10 extends AbstractDevice {

    /** The LCD to which this device interfaces. */
    private LCD lcd;

    /**
     * Constructs a new {@code Device86Port10}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D86Port10(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/86hw.c, "port10" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final int screenAddr = this.lcd.getScreenAddr();
        final BankState bank = cpu.getMemoryContext().getBanks()[BankState.mcBank(screenAddr)];
        final int addr = bank.getAddr();
        final int base = BankState.mcBase(screenAddr);

        for (int i = 0; i < HardwareConstants.DISPLAY_SIZE; ++i) {
            this.lcd.setDisplay(i, bank.getMem().get(addr + base + i));
        }
    }
}
