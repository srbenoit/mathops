package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_a_mem16" opcode.
 */
public final class LdAMem16 implements IOpcode {

    /**
     * Constructs a new {@code LdAMem16}.
     */
    public LdAMem16() {

        // No action
    }

    /**
     * Opcode ld_a_mem16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_a_mem16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        cpu.setA(cpu.cpuMemRead(address));

        return 13;
    }
}
