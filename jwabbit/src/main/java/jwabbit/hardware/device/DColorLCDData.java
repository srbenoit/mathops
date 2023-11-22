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
import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_data" function.
 */
public final class DColorLCDData extends AbstractDevice implements ICPULcdBaseCallback {

    /** The LCD to which this device interfaces. */
    private ColorLCD lcd;

    /**
     * Constructs a new {@code DeviceColorLCDData}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DColorLCDData(final int theDevIndex) {

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

            final int regIndex = theLcd.getCurrentRegister() & 0x00FF;

            if (cpu.isOutput()) {
                // Run some sanity checks on the write vars
                if (theLcd.getWriteLast() > cpu.getTimerContext().getElapsed()) {
                    theLcd.setWriteLast(cpu.getTimerContext().getElapsed());
                }

                final double writeDelay = cpu.getTimerContext().getElapsed() - theLcd.getWriteLast();
                if (theLcd.getWriteAvg() == 0.0) {
                    theLcd.setWriteAvg(writeDelay);
                }
                theLcd.setWriteLast(cpu.getTimerContext().getElapsed());
                theLcd.setLastTState(cpu.getTimerContext().getTStates());

                // If there is a delay that is significantly longer than the average write delay,
                // we can assume a frame has just terminated, and you can push this complete frame
                // towards generating the final image.

                // If you are in steady mode, then this simply serves as an FPS calculator
                if (writeDelay < theLcd.getWriteAvg() * 100.0) {
                    theLcd.setWriteAvg((theLcd.getWriteAvg() * 0.90) + (writeDelay * 0.10));
                } else {
                    final double ufpsLength = cpu.getTimerContext().getElapsed() - theLcd.getUfpsLast();
                    theLcd.setUfps(1.0 / ufpsLength);
                    theLcd.setUfpsLast(cpu.getTimerContext().getElapsed());
                }

                theLcd.setWriteBuffer((theLcd.getWriteBuffer() << 8) | cpu.getBus());

                if (regIndex == ColorLCD.GRAM_REG) {
                    final int mode = theLcd.lcdRegMask(ColorLCD.ENTRY_MODE_REG, ColorLCD.TRI_MASK);
                    if ((mode & ColorLCD.EIGHTEEN_BIT_MASK) == 0) {
                        theLcd.setWriteStep(theLcd.getWriteStep() == 0 ? 1 : 0);
                        if (theLcd.getWriteStep() == 0) {
                            theLcd.writePixel16();
                        }
                    } else {
                        theLcd.setWriteStep(theLcd.getWriteStep() + 1);
                        if (theLcd.getWriteStep() >= 3) {
                            theLcd.setWriteStep(0);
                            theLcd.writePixel18();
                        }
                    }
                } else {
                    theLcd.setWriteStep(theLcd.getWriteStep() == 0 ? 1 : 0);
                    if (theLcd.getWriteStep() == 0) {
                        theLcd.colorLCDSetRegister(cpu, regIndex, theLcd.getWriteBuffer() & 0x0000FFFF);
                    }
                }

                final boolean isBreakpoint = theLcd.isRegisterBreakpoint(regIndex);
                if (isBreakpoint) {
                    cpu.getPIOContext().getBreakpointCallback().exec(cpu);
                }

                cpu.setOutput(false);
            } else if (cpu.isInput()) {
                if (regIndex == ColorLCD.GRAM_REG) {
                    // read from LCD mem
                    int pixel = theLcd.getReadBuffer();
                    cpu.setBus(pixel >> 16);

                    theLcd.setReadStep(theLcd.getReadStep() == 0 ? 1 : 0);
                    if (theLcd.getReadStep() == 0) {
                        pixel = theLcd.readPixel();
                        pixel = ((pixel & 0x3e0000) << 2) | ((pixel & 0x3f00) << 5)
                                | ((pixel & 0x3e) << 7);
                    } else {
                        pixel <<= 8;
                    }

                    theLcd.setReadBuffer(pixel);
                } else {
                    final int val = theLcd.lcdReg(regIndex) & 0x0000FFFF;
                    theLcd.setReadStep(theLcd.getReadStep() == 0 ? 1 : 0);
                    if (theLcd.getReadStep() == 0) {
                        cpu.setBus(val & 0x00FF);
                    } else {
                        cpu.setBus(val >> 8);
                    }
                }

                cpu.setInput(false);
            }

            // Make sure timers are valid
            if (theLcd.getTime() > cpu.getTimerContext().getElapsed()) {
                theLcd.setTime(cpu.getTimerContext().getElapsed());
            }

            if (((cpu.getTimerContext().getElapsed() - theLcd.getTime()) >= (1.0
                    / (double) theLcd.getFrameRate())) && !theLcd.isDrawing()) {
                theLcd.enqueue(cpu);
                theLcd.setTime(theLcd.getTime() + (1.0 / (double) theLcd.getFrameRate()));
            }

            if (ColorLCD.REAL_LCD) {
                while ((cpu.getTimerContext().getElapsed() - theLcd.getLastDraw()) >= theLcd.getLineTime()) {
                    theLcd.enqueue(cpu);
                }
            }
        } else {
            LoggedObject.LOG.warning("Color LCD Data called with grayscale LCD");
        }
    }
}
