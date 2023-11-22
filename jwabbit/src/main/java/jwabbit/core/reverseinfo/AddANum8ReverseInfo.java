package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "add_a_num8_reverse_info" opcode.
 */
public final class AddANum8ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code AddANum8ReverseInfo}.
     */
    public AddANum8ReverseInfo() {

        // No action
    }

    /**
     * Opcode add_a_num8_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "add_a_num8_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        cpu.getPrevInstruction().setData1Lo(cpu.getA());
    }
}
