package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_mem16_hlf_reverse_info" opcode.
 */
public final class LdMem16HLfReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdMem16HLfReverseInfo}.
     */
    public LdMem16HLfReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_mem16_hlf_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_mem16_hlf_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        int reg = cpu.getMemoryContext().memRead(cpu.getPC() - 1);
        reg |= cpu.getMemoryContext().memRead(cpu.getPC() - 2) << 8;

        cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(reg));
        --reg;
        cpu.getPrevInstruction().setData2Lo(cpu.getMemoryContext().memRead(reg));
    }
}
