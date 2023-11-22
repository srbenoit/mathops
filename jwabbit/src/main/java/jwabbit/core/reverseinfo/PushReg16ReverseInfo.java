package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "push_reg16_reverse_info" opcode.
 */
public final class PushReg16ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code PushReg16ReverseInfo}.
     */
    public PushReg16ReverseInfo() {

        // No action
    }

    /**
     * Opcode push_reg16_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "push_reg16_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(cpu.getSP()));
        cpu.getPrevInstruction().setData1Hi(cpu.getMemoryContext().memRead(cpu.getSP() + 1));
    }
}
