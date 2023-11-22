package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_r_r" opcode.
 */
public final class LdRR implements IOpcode {

    /**
     * Constructs a new {@code LdRR}.
     */
    public LdRR() {

        // No action
    }

    /**
     * Opcode ld_r_r.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_r_r" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 3) & 0x07;
        final int test2 = cpu.getBus() & 0x07;
        int time = 4;

        final int reg;
        switch (test2) {
            case 0x00:
                reg = cpu.getB();
                break;
            case 0x01:
                reg = cpu.getC();
                break;
            case 0x02:
                reg = cpu.getD();
                break;
            case 0x03:
                reg = cpu.getE();
                break;
            case 0x04:
                if (cpu.getPrefix() != 0 && test == 6) {
                    reg = cpu.getH();
                } else if (cpu.getPrefix() == 0) {
                    reg = cpu.getH();
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIXH();
                    time += 4;
                } else {
                    reg = cpu.getIYH();
                    time += 4;
                }
                break;
            case 0x05:
                if (cpu.getPrefix() != 0 && test == 6) {
                    reg = cpu.getL();
                } else if (cpu.getPrefix() == 0) {
                    reg = cpu.getL();
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIXL();
                    time += 4;
                } else {
                    reg = cpu.getIYL();
                    time += 4;
                }
                break;
            case 0x06:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.cpuMemRead(cpu.getHL());
                    time += 3;
                } else {
                    final byte offset = (byte) cpu.cpuMemRead(cpu.getPC());
                    cpu.addPC(1);
                    if (cpu.getPrefix() == 0xDD) {
                        reg = cpu.cpuMemRead(cpu.getIX() + (int) offset);
                    } else {
                        reg = cpu.cpuMemRead(cpu.getIY() + (int) offset);
                    }
                    time += 15;
                }
                break;
            default:
                reg = cpu.getA();
                break;
        }

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
                if (cpu.getPrefix() != 0 && test2 == 6) {
                    cpu.setH(reg);
                } else if (cpu.getPrefix() == 0) {
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
                if (cpu.getPrefix() != 0 && test2 == 6) {
                    cpu.setL(reg);
                } else if (cpu.getPrefix() == 0) {
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
                    final byte offset = (byte) cpu.cpuMemRead(cpu.getPC());
                    cpu.addPC(1);
                    if (cpu.getPrefix() == 0xDD) {
                        cpu.cpuMemWrite(cpu.getIX() + (int) offset, reg);
                    } else {
                        cpu.cpuMemWrite(cpu.getIY() + (int) offset, reg);
                    }
                    time += 15;
                }
                break;
            default:
                cpu.setA(reg);
                break;
        }

        return time;
    }
}
