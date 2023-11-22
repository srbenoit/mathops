package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.PIOContext;
import jwabbit.hardware.STDINT;
import jwabbit.log.LoggedObject;

/**
 * The port 2 device for the TI 83pse hardware.
 */
public final class D83psePort02 extends AbstractDevice {

    /**
     * Constructs a new {@code Device83psePort02}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePort02(final int theDevIndex) {

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
     * WABBITEMU SOURCE: hardware/83psehw.c, "port2_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            final boolean lcdBusy = isLCDBusy(cpu);
            final PIOContext pio = cpu.getPIOContext();

            cpu.setBus((pio.getModel().ordinal() >= EnumCalcModel.TI_84P.ordinal() ? 0xE1 : 0xC1)
                    | (cpu.getMemoryContext().isFlashLocked() ? 0 : 4) | (lcdBusy ? 0 : 2));
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            cpu.setOutput(false);

            final STDINT stdint = cpu.getPIOContext().getStdint();
            if ((cpu.getBus() & 1) == 0) {
                stdint.setOnLatch(false);
            }

            if ((cpu.getBus() & (1 << 1)) == 0) {
                while ((cpu.getTimerContext().getElapsed() - stdint.getLastchk1()) > stdint.getTimermax1()) {
                    stdint.setLastchk1(stdint.getLastchk1() + stdint.getTimermax1());
                }
            }

            if ((cpu.getBus() & (1 << 2)) == 0) {
                while ((cpu.getTimerContext().getElapsed() - stdint.getLastchk2()) > stdint.getTimermax2()) {
                    stdint.setLastchk2(stdint.getLastchk2() + stdint.getTimermax2());
                }
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "IsLCDBusy" function.
     *
     * @param cpu the CPU
     * @return true if busy
     */
    private static boolean isLCDBusy(final CPU cpu) {

        int lcdWaitTStates;

        switch (cpu.getCPUSpeed()) {
            case 0:
                return false;
            case 1:
                lcdWaitTStates = cpu.getPIOContext().getSeAux().getDelay().getLcdWait() & 3;
                break;
            case 2:
                lcdWaitTStates = (cpu.getPIOContext().getSeAux().getDelay().getLcdWait() >> 2) & 7;
                break;
            case 3:
                lcdWaitTStates = (cpu.getPIOContext().getSeAux().getDelay().getLcdWait() >> 5) & 7;
                break;
            default:
                LoggedObject.LOG.warning("Invalid CPU speed");
                return false;
        }

        lcdWaitTStates = (lcdWaitTStates << 6) + 48;
        return !((cpu.getTimerContext().getTStates()
                - cpu.getPIOContext().getLcd().getLastTState()) > (long) lcdWaitTStates);
    }
}
