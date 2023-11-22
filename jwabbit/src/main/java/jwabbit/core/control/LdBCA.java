package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_bc_a" opcode.
 */
public final class LdBCA implements IOpcode {

    /**
     * Constructs a new {@code LdBCA}.
     */
    public LdBCA() {

        // No action
    }

    /**
     * Opcode ld_bc_a.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_bc_a" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.cpuMemWrite(cpu.getBC(), cpu.getA());

        return 7;
    }
}
