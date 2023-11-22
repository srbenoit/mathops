package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_de_a" opcode.
 */
public final class LdDEA implements IOpcode {

    /**
     * Constructs a new {@code LdDEA}.
     */
    public LdDEA() {

        // No action
    }

    /**
     * Opcode ld_de_a.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_de_a" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.cpuMemWrite(cpu.getDE(), cpu.getA());

        return 7;
    }
}
