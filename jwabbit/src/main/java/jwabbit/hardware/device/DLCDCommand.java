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
import jwabbit.hardware.ICPULcdBaseCallback;
import jwabbit.hardware.LCD;
import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: hardware/lcd.c, "LCD_command" function.
 */
public final class DLCDCommand extends AbstractDevice implements ICPULcdBaseCallback {

    /** The LCD to which this device interfaces. */
    private AbstractLCDBase lcd;

    /**
     * Constructs a new {@code DeviceLCDCommand}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DLCDCommand(final int theDevIndex) {

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
                    && (long) theLcd.getLcdDelay() > (cpu.getTimerContext().getTStates() - theLcd.getLastTState())) {

                cpu.setOutput(false);
                if (cpu.isInput()) {
                    cpu.setInput(false);
                    cpu.setBus(0x0080);
                }
            }

            if (cpu.isOutput()) {

                theLcd.setLastTState(cpu.getTimerContext().getTStates());

                // NOTE: the "clever macros" used in the original are a dog's breakfast.
                // Simple if logic is just as clear and far more portable!

                final int crdTest = cpu.getBus();

                if ((crdTest & (AbstractLCDBase.CRD_DPE_MASK)) == AbstractLCDBase.CRD_DPE) {
                    theLcd.setActive((crdTest & AbstractLCDBase.CRD_DPE_DATA) != 0);
                    theLcd.enqueue(cpu);
                }

                if ((crdTest & (AbstractLCDBase.CRD_86E_MASK)) == AbstractLCDBase.CRD_86E) {
                    theLcd.setWordLen(crdTest & AbstractLCDBase.CRD_86E_DATA);
                }

                if ((crdTest & (AbstractLCDBase.CRD_UDE_MASK)) == AbstractLCDBase.CRD_UDE) {
                    theLcd.setCursorMode(crdTest & AbstractLCDBase.CRD_UDE_DATA);
                }

                if ((crdTest & (AbstractLCDBase.CRD_SYE_MASK)) == AbstractLCDBase.CRD_SYE) {
                    theLcd.setY(crdTest & AbstractLCDBase.CRD_SYE_DATA);
                }

                if ((crdTest & (AbstractLCDBase.CRD_SZE_MASK)) == AbstractLCDBase.CRD_SZE) {
                    theLcd.setZ(crdTest & AbstractLCDBase.CRD_SZE_DATA);
                    theLcd.enqueue(cpu);
                }

                if ((crdTest & (AbstractLCDBase.CRD_SXE_MASK)) == AbstractLCDBase.CRD_SXE) {
                    theLcd.setX(crdTest & AbstractLCDBase.CRD_SXE_DATA);
                }

                if ((crdTest & (AbstractLCDBase.CRD_SCE_MASK)) == AbstractLCDBase.CRD_SCE) {
                    theLcd.setContrast(
                            (crdTest & AbstractLCDBase.CRD_SCE_DATA) - theLcd.getBaseLevel());
                }

                cpu.setOutput(false);
            } else if (cpu.isInput()) {
                cpu.setBus((theLcd.getWordLen() << 6) | ((theLcd.isActive() ? 1 : 0) << 5) | theLcd.getCursorMode());
                cpu.setInput(false);
            }
        } else {
            LoggedObject.LOG.warning("LCD command called on non-LCD");
        }
    }
}
