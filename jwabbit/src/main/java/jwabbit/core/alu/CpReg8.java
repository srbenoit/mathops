package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "cp_reg8" opcode.
 */
public final class CpReg8 implements IOpcode {

    /**
     * Constructs a new {@code CpReg8}.
     */
    public CpReg8() {

        // No action
    }

    /**
     * Opcode cp_reg8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "cp_reg8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg;
        int time = 4;

        switch (cpu.getBus() & 0x07) {
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
                if (cpu.getPrefix() == 0) {
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
                if (cpu.getPrefix() == 0) {
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

        final int result = cpu.getA() - reg;
        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(reg)
                + CPU.hcsubchk(cpu.getA(), reg, 0) + CPU.x3chk(reg)
                + CPU.vchksub(cpu.getA(), reg, result) + CPU.SUB_INSTR + CPU.carrychk(result));

        return time;
    }
}
