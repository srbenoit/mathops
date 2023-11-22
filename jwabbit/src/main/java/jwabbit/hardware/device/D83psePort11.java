package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.IDevice;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.Delay;
import jwabbit.hardware.ICPULcdBaseCallback;
import jwabbit.hardware.SEAux;

/**
 * The port 17 (0x11) device for the TI 83pse hardware.
 */
public final class D83psePort11 extends AbstractDevice {

    /** The LCD to which this device interfaces. */
    private AbstractLCDBase lcd;

    /**
     * Constructs a new {@code Device83psePort11}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort11(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port11_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            addSEDelay(cpu, cpu.getPIOContext().getSeAux());
        } else if (cpu.isOutput()) {
            addSEDelay(cpu, cpu.getPIOContext().getSeAux());
        }

        final ICPULcdBaseCallback data = cpu.getPIOContext().getLcd().getDataCallback();
        ((IDevice) data).runCode(cpu);
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "Add_SE_Delay" function.
     *
     * @param cpu   the CPU
     * @param seAux the SE_AUX
     */
    private static void addSEDelay(final CPU cpu, final SEAux seAux) {

        final Delay delay = seAux.getDelay();
        final long extraTime = (long) (delay.getReg(cpu.getCPUSpeed()) >> 2);
        cpu.getTimerContext().tcAdd(extraTime);
    }
}
