package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_reg16_mem16_reverse_info" opcode.
 */
public final class LdReg16Mem16ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdReg16Mem16ReverseInfo}.
     */
    public LdReg16Mem16ReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_reg16_mem16_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_reg16_mem16_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        switch ((cpu.getBus() >> 4) & 0x03) {
            case 0:
                cpu.getPrevInstruction().setData1(cpu.getBC());
                break;
            case 1:
                cpu.getPrevInstruction().setData1(cpu.getDE());
                break;
            case 2:
                cpu.getPrevInstruction().setData1(cpu.getHL());
                break;
            case 3:
                cpu.getPrevInstruction().setData1(cpu.getSP());
                break;
            default:
                break;
        }
    }
}
