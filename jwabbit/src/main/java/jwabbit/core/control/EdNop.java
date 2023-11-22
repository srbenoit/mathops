package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ednop" opcode.
 */
public final class EdNop implements IOpcode {

    /**
     * Constructs a new {@code EdNop}.
     */
    public EdNop() {

        // No action
    }

    /**
     * Opcode ednop.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ednop" function.
     */
    @Override
    public int exec(final CPU cpu) {

        return 8;
    }
}
