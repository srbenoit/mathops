package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.ColorLCD;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.SEAux;

/**
 * The port 58 (0x3A) device for the TI 83pse hardware.
 */
public final class D83psePort3A extends AbstractDevice {

    /** The SE_AUX to which this device interfaces. */
    private SEAux seAux;

    /**
     * Constructs a new {@code Device83psePort3A}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort3A(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.seAux = null;
    }

    /**
     * Sets the SE_AUX to which this device interfaces.
     *
     * @param theSeAux the SE_AUX
     */
    public void setSeAux(final SEAux theSeAux) {

        this.seAux = theSeAux;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port3A_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final ColorLCD lcd;

        final AbstractLCDBase lcdBase = cpu.getPIOContext().getLcd();
        if (lcdBase instanceof ColorLCD) {
            lcd = (ColorLCD) lcdBase;
        } else {
            // LOG.warning("83pse device 3A called with LCD that is not color");
            lcd = null;
        }

        if (cpu.isInput()) {
            cpu.setBus(this.seAux.getGpio());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            if (cpu.getPIOContext().getModel() == EnumCalcModel.TI_84PCSE) {
                // check if bit 5 flipped
                if ((cpu.getBus() & 0x20) != (this.seAux.getGpio() & 0x20) && lcd != null) {
                    if ((cpu.getBus() & 0x20) == 0) {
                        lcd.setBacklightOffElapsed(cpu.getTimerContext().getElapsed());
                    } else {
                        if (lcd.isBacklightActive()) {
                            lcd.setContrast(lcd.getContrast() + 1);
                            lcd.setContrast(lcd.getContrast() % HardwareConstants.MAX_BACKLIGHT_LEVEL);
                        } else {
                            lcd.setBacklightActive(true);
                            // brightest backlight
                            lcd.setContrast(0);
                        }

                        lcd.setBacklightOffElapsed(0.0);
                    }
                }
            }

            this.seAux.setGpio(cpu.getBus());
            cpu.setOutput(false);
        }

        if (cpu.getPIOContext().getModel() == EnumCalcModel.TI_84PCSE && lcd != null) {
            if (lcd.isBacklightActive() && lcd.getBacklightOffElapsed() > Double.MIN_VALUE
                    && ((cpu.getTimerContext().getElapsed() - lcd.getBacklightOffElapsed()
                    - HardwareConstants.BACKLIGHT_OFF_DELAY) > Double.MIN_VALUE)) {
                lcd.setBacklightActive(false);
            }
        }
    }
}
