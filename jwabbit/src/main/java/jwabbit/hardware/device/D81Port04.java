package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.PIOContext;
import jwabbit.hardware.STDINT;

/**
 * The port 4 device for the TI 81 hardware.
 */
public final class D81Port04 extends AbstractDevice {

    /**
     * Constructs a new {@code Device81Port04}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D81Port04(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

    }

    /**
     * WABBITEMU SOURCE: hardware/81hw.c, "port4" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(0x00);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            final int freq = (cpu.getBus() >> 1) & 0x03;

            final PIOContext pio = cpu.getPIOContext();
            final STDINT stdint = pio.getStdint();

            stdint.setTimermax1(stdint.getFreq(freq));
            stdint.setLastchk1(cpu.getTimerContext().getElapsed());
            final int lcdMode = (cpu.getBus() >> 3) & 0x03;
            if (lcdMode == 0) {
                pio.getLcd().setWidth(80);
            } else {
                pio.getLcd().setWidth(32 * lcdMode + 64);
            }

            cpu.setOutput(false);
        }
    }
}
