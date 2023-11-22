package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "srl_reg_reverse_info" opcode.
 */
public final class SrlRegReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code SrlRegReverseInfo}.
     */
    public SrlRegReverseInfo() {

        // No action
    }

    /**
     * Opcode srl_reg_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "srl_reg_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        switch (cpu.getBus() & 0x07) {
            case 0x00:
                cpu.getPrevInstruction().setData1Lo(cpu.getB());
                break;
            case 0x01:
                cpu.getPrevInstruction().setData1Lo(cpu.getC());
                break;
            case 0x02:
                cpu.getPrevInstruction().setData1Lo(cpu.getD());
                break;
            case 0x03:
                cpu.getPrevInstruction().setData1Lo(cpu.getE());
                break;
            case 0x04:
                cpu.getPrevInstruction().setData1Lo(cpu.getH());
                break;
            case 0x05:
                cpu.getPrevInstruction().setData1Lo(cpu.getL());
                break;
            case 0x06:
                cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(cpu.getHL()));
                break;
            default:
                cpu.getPrevInstruction().setData1Lo(cpu.getA());
                break;
        }
    }
}
