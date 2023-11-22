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
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.ICPULcdBaseCallback;
import jwabbit.hardware.LCD;
import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: hardware/lcd.c, "LCD_data" function.
 */
public final class DLCDData extends AbstractDevice implements ICPULcdBaseCallback {

    /** The LCD to which this device interfaces. */
    private AbstractLCDBase lcd;

    /**
     * Constructs a new {@code DeviceLCDData}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DLCDData(final int theDevIndex) {

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
    public AbstractLCDBase getLcd() {

        return this.lcd;
    }

    /**
     * Sets the LCD to which this device interfaces.
     *
     * @param theLcd the LCD
     */
    public void setLcd(final AbstractLCDBase theLcd) {

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

        if (lcdBase instanceof final LCD theLcd) {

            if (cpu.getPIOContext().getModel().ordinal() >= EnumCalcModel.TI_83P.ordinal()
                    && (long) theLcd.getLcdDelay() > (cpu.getTimerContext().getTStates() - theLcd.getLastTState())
                    && (cpu.isInput() || cpu.isOutput())) {

                cpu.setOutput(false);
                cpu.setInput(false);
                return;
            }

            // Get a pointer to the byte referenced by the CRD cursor
            int shift = 0;
            final int cursorIndex;

            if (theLcd.getWordLen() == 0) {
                final int newY = theLcd.getY() * 6;
                shift = 10 - (newY % 8);
                cursorIndex = LCD.lcdOffset(newY / 8, theLcd.getX(), 0);
            } else {
                cursorIndex = LCD.lcdOffset(theLcd.getY(), theLcd.getX(), 0);
            }

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

                    if (theLcd.getMode() == HardwareConstants.MODE_PERFECT_GRAY) {
                        theLcd.enqueue(cpu);
                        theLcd.setTime(cpu.getTimerContext().getElapsed());
                    }
                }

                if (theLcd.getMode() == HardwareConstants.MODE_GAME_GRAY && theLcd.getX() == 0 && theLcd.getY() == 0) {
                    theLcd.enqueue(cpu);
                    theLcd.setTime(cpu.getTimerContext().getElapsed());
                }

                // Set the cursor based on the word mode
                if (theLcd.getWordLen() == 0) {
                    final int data = (cpu.getBus() << shift) & 0x0000FFFF;
                    final int mask = (~(0x003F << shift)) & 0x0000FFFF;

                    theLcd.setDisplay(cursorIndex, (theLcd.getDisplay(cursorIndex) & (mask >> 8)) | (data >> 8));
                    theLcd.setDisplay(cursorIndex + 1,
                            (theLcd.getDisplay(cursorIndex + 1) & (mask & 0xFF)) | (data & 0xFF));
                } else {
                    theLcd.setDisplay(cursorIndex, cpu.getBus());
                }

                theLcd.advanceCursor();
                cpu.setOutput(false);
            } else if (cpu.isInput()) {
                cpu.setBus(theLcd.getLastRead() & 0x00FF);

                if (theLcd.getWordLen() == 0) {
                    int lastRead = theLcd.getDisplay(cursorIndex) << 8 | theLcd.getDisplay(cursorIndex + 1);
                    lastRead >>= shift;
                    lastRead &= 0x3F;
                    theLcd.setLastRead(lastRead);
                } else {
                    theLcd.setLastRead(theLcd.getDisplay(cursorIndex));
                }

                theLcd.advanceCursor();
                cpu.setInput(false);
            }

            // Make sure timers are valid
            if (theLcd.getTime() > cpu.getTimerContext().getElapsed()) {
                theLcd.setTime(cpu.getTimerContext().getElapsed());
            } else if (cpu.getTimerContext().getElapsed()
                    - theLcd.getTime() > (2.0 / (double) HardwareConstants.STEADY_FREQ_MIN)) {
                theLcd.setTime(cpu.getTimerContext().getElapsed() - (2.0 / (double) HardwareConstants.STEADY_FREQ_MIN));
            }

            // Perfect gray mode should time out too in case the screen update rate is too slow for
            // proper grayscale (essentially a fallback on steady freq)
            if (theLcd.getMode() == HardwareConstants.MODE_PERFECT_GRAY
                    || theLcd.getMode() == HardwareConstants.MODE_GAME_GRAY) {
                if ((cpu.getTimerContext().getElapsed() - theLcd.getTime()) >=
                        (1.0 / (double) HardwareConstants.STEADY_FREQ_MIN)) {
                    theLcd.enqueue(cpu);
                    theLcd.setTime(theLcd.getTime() + (1.0 / (double) HardwareConstants.STEADY_FREQ_MIN));
                }
            } else if (theLcd.getMode() == HardwareConstants.MODE_STEADY) {
                if ((cpu.getTimerContext().getElapsed() - theLcd.getTime()) >= theLcd.getSteadyFrame()) {
                    theLcd.enqueue(cpu);
                    theLcd.setTime(theLcd.getTime() + theLcd.getSteadyFrame());
                }
            }
        } else {
            LoggedObject.LOG.warning("LCD Data called with non LCD");
        }
    }
}
