package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_sp_num16_reverse_info" opcode.
 */
public final class LdSPNum16ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdSPNum16ReverseInfo}.
     */
    public LdSPNum16ReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_sp_num16_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_sp_num16_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        cpu.getPrevInstruction().setData1(cpu.getSP());
    }
}
