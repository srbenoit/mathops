package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "IM2" opcode.
 */
public final class IM2 implements IOpcode {

    /**
     * Constructs a new {@code IM2}.
     */
    public IM2() {

        // No action
    }

    /**
     * Opcode IM2.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "IM2" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setIMode(2);

        return 8;
    }
}
