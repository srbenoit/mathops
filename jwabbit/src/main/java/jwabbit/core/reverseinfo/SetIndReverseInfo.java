package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevIndexOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "set_ind_reverse_info" opcode.
 */
public final class SetIndReverseInfo implements IRevIndexOpcode {

    /**
     * Constructs a new {@code SetIndReverseInfo}.
     */
    public SetIndReverseInfo() {

        // No action
    }

    /**
     * Opcode set_ind_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "set_ind_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu, final byte index) {

        if (cpu.getPrefix() == JWCoreConstants.IX_PREFIX) {
            cpu.getPrevInstruction().setData2Lo(cpu.getMemoryContext().memRead(cpu.getIX() + (int) index));
        } else {
            cpu.getPrevInstruction().setData2Lo(cpu.getMemoryContext().memRead(cpu.getIY() + (int) index));
        }
    }
}
