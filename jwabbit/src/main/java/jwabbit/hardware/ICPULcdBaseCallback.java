package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;

/**
 * A callback that takes a CPU and an LCDBase as arguments.
 */
@FunctionalInterface
public interface ICPULcdBaseCallback {

    /**
     * Executes the callback.
     *
     * @param cpu     the CPU
     * @param lcdBase the LCDBase
     */
    void exec(CPU cpu, AbstractLCDBase lcdBase);
}
