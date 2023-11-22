package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.ColorLCD;
import jwabbit.hardware.ICPULcdBaseCallback;

/**
 * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_command" function.
 */
public final class DColorLCDCommand extends AbstractDevice implements ICPULcdBaseCallback {

    /** The LCD to which this device interfaces. */
    private ColorLCD lcd;

    /**
     * Constructs a new {@code DeviceColorLCDCommand}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DColorLCDCommand(final int theDevIndex) {

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
    public ColorLCD getLcd() {

        return this.lcd;
    }

    /**
     * Sets the LCD to which this device interfaces.
     *
     * @param theLcd the LCD
     */
    public void setLcd(final ColorLCD theLcd) {

        this.lcd = theLcd;
    }

    /**
     * Runs the device code.
     *
     * <p>
     * SOURCE: hardware/83psehw.c, "port0E_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        exec(cpu, this.lcd);
    }

    /**
     * Executes the callback.
     *
     * @param cpu     the CPU
     * @param lcdBase the LCDBase
     */
    @Override
    public void exec(final CPU cpu, final AbstractLCDBase lcdBase) {

        if (lcdBase instanceof final ColorLCD theLcd) {
            if (cpu.isOutput()) {
                theLcd.setCurrentRegister(cpu.getBus() | (theLcd.getCurrentRegister() << 8));
                theLcd.setReadStep(0);
                theLcd.setWriteStep(0);
                cpu.setOutput(false);
            } else if (cpu.isInput()) {
                cpu.setBus(0);
                cpu.setInput(false);
            }
        }
    }
}
