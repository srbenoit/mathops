package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "halt" opcode.
 */
public final class Halt implements IOpcode {

    /**
     * Constructs a new {@code Halt}.
     */
    public Halt() {

        // No action
    }

    /**
     * Opcode halt.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "halt" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setHalt(true);

        return 4;
    }
}
