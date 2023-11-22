package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_mem16_a_reverse_info" opcode.
 */
public final class LdMem16AReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdMem16AReverseInfo}.
     */
    public LdMem16AReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_mem16_a_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_mem16_a_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        int address = cpu.getMemoryContext().memRead(cpu.getPC()) << 8;
        address |= cpu.getMemoryContext().memRead(cpu.getPC() + 1);

        cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(address));
    }
}
