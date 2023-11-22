package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_a_de" opcode.
 */
public final class LdADE implements IOpcode {

    /**
     * Constructs a new {@code LdADE}.
     */
    public LdADE() {

        // No action
    }

    /**
     * Opcode ld_a_de.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_a_de" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setA(cpu.cpuMemRead(cpu.getDE()));

        return 7;
    }
}
