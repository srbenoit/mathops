package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ei" opcode.
 */
public final class Ei implements IOpcode {

    /**
     * Constructs a new {@code Ei}.
     */
    public Ei() {

        // No action
    }

    /**
     * Opcode ei.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ei" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setIff1(true);
        cpu.setIff2(true);
        cpu.setEiBlock(true);

        return 4;
    }
}
