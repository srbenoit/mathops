package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "add_hl_reg16_reverse_info" opcode.
 */
public final class AddHLReg16ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code AddHLReg16ReverseInfo}.
     */
    public AddHLReg16ReverseInfo() {

        // No action
    }

    /**
     * Opcode add_hl_reg16_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "add_hl_reg16_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        if (cpu.getPrefix() == 0) {
            cpu.getPrevInstruction().setData1(cpu.getHL());
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.getPrevInstruction().setData1(cpu.getIX());
        } else {
            cpu.getPrevInstruction().setData1(cpu.getIY());
        }
    }
}
