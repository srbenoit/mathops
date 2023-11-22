package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_r_num8" opcode.
 */
public final class LdRNum8 implements IOpcode {

    /**
     * Constructs a new {@code LdRNum8}.
     */
    public LdRNum8() {

        // No action
    }

    /**
     * Opcode ld_r_num8.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_r_num8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 3) & 0x07;

        int time = 7;
        int reg = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        switch (test) {
            case 0x00:
                cpu.setB(reg);
                break;
            case 0x01:
                cpu.setC(reg);
                break;
            case 0x02:
                cpu.setD(reg);
                break;
            case 0x03:
                cpu.setE(reg);
                break;
            case 0x04:
                if (cpu.getPrefix() == 0) {
                    cpu.setH(reg);
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.setIXH(reg);
                    time += 4;
                } else {
                    cpu.setIYH(reg);
                    time += 4;
                }
                break;
            case 0x05:
                if (cpu.getPrefix() == 0) {
                    cpu.setL(reg);
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.setIXL(reg);
                    time += 4;
                } else {
                    cpu.setIYL(reg);
                    time += 4;
                }
                break;
            case 0x06:
                if (cpu.getPrefix() == 0) {
                    cpu.cpuMemWrite(cpu.getHL(), reg);
                    time += 3;
                } else {
                    final byte offset = (byte) reg;
                    reg = cpu.cpuMemRead(cpu.getPC());
                    cpu.addPC(1);
                    if (cpu.getPrefix() == 0xDD) {
                        cpu.cpuMemWrite(cpu.getIX() + (int) offset, reg);
                    } else {
                        cpu.cpuMemWrite(cpu.getIY() + (int) offset, reg);
                    }
                    time += 12;
                }
                break;
            default:
                cpu.setA(reg);
                break;
        }

        return time;
    }
}
