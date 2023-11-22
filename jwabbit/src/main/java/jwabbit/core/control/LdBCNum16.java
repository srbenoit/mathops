package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_bc_num16" opcode.
 */
public final class LdBCNum16 implements IOpcode {

    /**
     * Constructs a new {@code LdBCNum16}.
     */
    public LdBCNum16() {

        // No action
    }

    /**
     * Opcode ld_bc_num16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_bc_num16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setBC(cpu.cpuMemRead(cpu.getPC()));
        cpu.addPC(1);

        cpu.setBC(cpu.getBC() | (cpu.cpuMemRead(cpu.getPC()) << 8));
        cpu.addPC(1);

        return 10;
    }
}
