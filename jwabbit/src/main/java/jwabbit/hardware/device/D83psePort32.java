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
 * The port 50 (0x32) device for the TI 83pse hardware.
 */
public final class D83psePort32 extends AbstractDevice {

    /** The XTAL to which this device interfaces. */
    private XTAL xtal;

    /**
     * Constructs a new {@code Device83psePort32}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort32(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port32_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        final Timer timer = this.xtal.getTimer((getDevIndex() - 0x30) / 3);

        handleXtal(cpu);

        if (cpu.isInput()) {
            cpu.setBus(timer.getCount());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            timer.setCount(cpu.getBus());
            timer.setMax(cpu.getBus());
            if ((timer.getClock() & 0xC0) != 0) {
                timer.setActive(true);
            }
            timer.setLastTstates(cpu.getTimerContext().getTStates());
            timer.setLastTicks((double) this.xtal.getTicks());
            Hw83pse.modTimer(cpu, this.xtal);
            cpu.setOutput(false);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "handlextal" function.
     *
     * @param cpu the CPU
     */
    private void handleXtal(final CPU cpu) {

        Timer timer;

        this.xtal.setTicks((long) (cpu.getTimerContext().getElapsed() * 32768.0));
        this.xtal.setLastTime(cpu.getTimerContext().getElapsed());

        for (int i = 0; i < this.xtal.getNumTimers(); ++i) {

            timer = this.xtal.getTimer(i);
            if (timer.isActive()) {
                switch (((timer.getClock() & 0xC0) >> 6) & 0x03) {
                    case 1:
                        if ((timer.getLastTicks() + timer.getDivisor()) < (double) this.xtal.getTicks()) {
                            timer.setLastTicks(timer.getLastTicks() + timer.getDivisor());
                            timer.setCount(timer.getCount() - 1);
                            if (timer.getCount() == 0) {
                                if (!timer.isUnderflow()) {
                                    timer.setCount(timer.getMax());
                                    if (!timer.isLoop()) {
                                        timer.setActive(false);
                                    }
                                }
                                if (timer.isInterrupt()) {
                                    timer.setGenerate(true);
                                }
                                timer.setUnderflow(true);
                            }
                        }
                        break;
                    case 2:
                    case 3:
                        while (timer.getLastTstates() + (long) timer.getDivisor() < cpu
                                .getTimerContext().getTStates()) {

                            timer.setLastTstates(timer.getLastTstates() + (long) timer.getDivisor());
                            timer.setCount(timer.getCount() - 1);
                            if (timer.getCount() == 0) {
                                if (!timer.isUnderflow()) {
                                    timer.setCount(timer.getMax());
                                    if (!timer.isLoop()) {
                                        timer.setActive(false);
                                    }
                                }
                                if (timer.isInterrupt()) {
                                    timer.setGenerate(true);
                                }
                                timer.setUnderflow(true);
                            }
                        }
                        break;
                    case 0:
                    default:
                        break;
                }
            }

            if (timer.isGenerate() && !cpu.isHalt()) {
                cpu.setInterrupt(true);
            }
        }
    }
}
