package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_mem16_reg16_reverse_info" opcode.
 */
public final class LdMem16Reg16ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdMem16Reg16ReverseInfo}.
     */
    public LdMem16Reg16ReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_mem16_reg16_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_mem16_reg16_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        int address = cpu.getMemoryContext().memRead(cpu.getPC()) & 0x0000FFFF;
        address = (address | (cpu.getMemoryContext().memRead(cpu.getPC() + 1) << 8)) & 0x0000FFFF;

        int data1 = cpu.getMemoryContext().memRead(address);
        data1 |= cpu.getMemoryContext().memRead(address + 1) << 8;

        cpu.getPrevInstruction().setData1(data1);
    }
}
