package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "di" opcode.
 */
public final class Di implements IOpcode {

    /**
     * Constructs a new {@code Di}.
     */
    public Di() {

        // No action
    }

    /**
     * Opcode di.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "di" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setIff1(false);
        cpu.setIff2(false);

        return 4;
    }
}
