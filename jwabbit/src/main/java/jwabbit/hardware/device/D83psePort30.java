package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Hw83pse;
import jwabbit.hardware.Timer;
import jwabbit.hardware.XTAL;

/**
 * The port 48 (0x30) device for the TI 83pse hardware.
 */
public final class D83psePort30 extends AbstractDevice {

    /** The XTAL to which this device interfaces. */
    private XTAL xtal;

    /**
     * Constructs a new {@code Device83psePort30}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort30(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.xtal = null;
    }

    /**
     * Sets the XTAL to which this device interfaces.
     *
     * @param theXtal the XTAL
     */
    public void setXtal(final XTAL theXtal) {

        this.xtal = theXtal;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port30_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final Timer timer = this.xtal.getTimer((getDevIndex() - 0x30) / 3);

        if (cpu.isInput()) {
            cpu.setBus(timer.getClock());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            timer.setClock(cpu.getBus());
            timer.setActive(false);
            timer.setGenerate(false);
            switch ((timer.getClock() & 0xC0) >> 6) {
                case 0x00:
                    timer.setDivisor(0.0);
                    break;
                case 0x01:
                    switch (timer.getClock() & 0x07) {
                        case 0x00:
                            timer.setDivisor(3.0);
                            break;
                        case 0x01:
                            timer.setDivisor(32.0);
                            break;
                        case 0x02:
                            timer.setDivisor(327.000);
                            break;
                        case 0x03:
                            timer.setDivisor(3276.00);
                            break;
                        case 0x04:
                            timer.setDivisor(1.0);
                            break;
                        case 0x05:
                            timer.setDivisor(16.0);
                            break;
                        case 0x06:
                            timer.setDivisor(256.0);
                            break;
                        case 0x07:
                            timer.setDivisor(4096.0);
                            break;
                        default:
                            break;
                    }
                    break;
                case 0x02:
                case 0x03:
                    int mask = 0x20;
                    timer.setDivisor(64.0);
                    for (int i = 0; i < 6; ++i) {
                        if ((timer.getClock() & mask) != 0) {
                            break;
                        }
                        mask = mask >> 1;
                        timer.setDivisor(timer.getDivisor() / 2.0);
                    }
                    break;
                default:
                    break;
            }
            Hw83pse.modTimer(cpu, this.xtal);
            cpu.setOutput(false);
        }
    }
}
