package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "ld_r_num8_reverse_info" opcode.
 */
public final class LdRNum8ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code LdRNum8ReverseInfo}.
     */
    public LdRNum8ReverseInfo() {

        // No action
    }

    /**
     * Opcode ld_r_num8_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "ld_r_num8_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 3) & 0x07;

        switch (test) {
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
                if (cpu.getPrefix() == 0) {
                    cpu.getPrevInstruction().setData1Lo(cpu.getH());
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.getPrevInstruction().setData1Lo(cpu.getIXH());
                } else {
                    cpu.getPrevInstruction().setData1Lo(cpu.getIYH());
                }
                break;
            case 0x05:
                if (cpu.getPrefix() == 0) {
                    cpu.getPrevInstruction().setData1Lo(cpu.getL());
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.getPrevInstruction().setData1Lo(cpu.getIXL());
                } else {
                    cpu.getPrevInstruction().setData1Lo(cpu.getIYL());
                }
                break;
            case 0x06:
                if (cpu.getPrefix() == 0) {
                    cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(cpu.getHL()));
                } else {
                    final byte index = (byte) cpu.getMemoryContext().memRead(cpu.getPC());
                    if (cpu.getPrefix() == 0xDD) {
                        cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(cpu.getIX() + (int) index));
                    } else {
                        cpu.getPrevInstruction().setData1Lo(cpu.getMemoryContext().memRead(cpu.getIY() + (int) index));
                    }
                }
                break;
            default:
                cpu.getPrevInstruction().setData1Lo(cpu.getA());
                break;
        }
    }
}
