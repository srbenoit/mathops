package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.JWCoreConstants;
import jwabbit.hardware.Hw83pse;

/**
 * The port 32 (0x20) device for the TI 83pse hardware.
 */
public final class D83psePort20 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort20}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort20(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        // No action
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port20_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(cpu.getCPUSpeed());
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            int val = cpu.getBus() & 3;
            if (cpu.getTimerContext().getTimerVersion() == 0 && val > 1) {
                val = 1;
            }
            switch (val) {
                case 1:
                    cpu.getTimerContext().setFreq(JWCoreConstants.MHZ_15);
                    break;
                case 2:
                    cpu.getTimerContext().setFreq(JWCoreConstants.MHZ_20);
                    break;
                case 3:
                    cpu.getTimerContext().setFreq(JWCoreConstants.MHZ_25);
                    break;
                default:
                    cpu.getTimerContext().setFreq(JWCoreConstants.MHZ_6);
                    break;
            }
            Hw83pse.updateDelays(cpu, cpu.getPIOContext().getSeAux().getDelay());
            cpu.setOutput(false);
        }
    }
}
