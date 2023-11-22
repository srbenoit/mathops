package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "dec_reg8" opcode.
 */
public final class DecReg8 implements IOpcode {

    /**
     * Constructs a new {@code DecReg8}.
     */
    public DecReg8() {

        // No action
    }

    /**
     * Opcode dec_reg8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "dec_reg8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int result;
        final int reg;
        int time = 4;

        switch ((cpu.getBus() >> 3) & 0x07) {
            case 0x00:
                reg = cpu.getB();
                cpu.addB(-1);
                result = cpu.getB();
                break;
            case 0x01:
                reg = cpu.getC();
                cpu.addC(-1);
                result = cpu.getC();
                break;
            case 0x02:
                reg = cpu.getD();
                cpu.addD(-1);
                result = cpu.getD();
                break;
            case 0x03:
                reg = cpu.getE();
                cpu.addE(-1);
                result = cpu.getE();
                break;
            case 0x04:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.getH();
                    cpu.addH(-1);
                    result = cpu.getH();
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIXH();
                    cpu.addIXH(-1);
                    result = cpu.getIXH();
                    time += 4;
                } else {
                    reg = cpu.getIYH();
                    cpu.addIYH(-1);
                    result = cpu.getIYH();
                    time += 4;
                }
                break;
            case 0x05:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.getL();
                    cpu.addL(-1);
                    result = cpu.getL();
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIXL();
                    cpu.addIXL(-1);
                    result = cpu.getIXL();
                    time += 4;
                } else {
                    reg = cpu.getIYL();
                    cpu.addIYL(-1);
                    result = cpu.getIYL();
                    time += 4;
                }
                break;
            case 0x06:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.cpuMemRead(cpu.getHL());
                    result = reg;
                    --result;
                    cpu.cpuMemWrite(cpu.getHL(), result);
                    time += 7;
                } else {
                    final byte offset = (byte) cpu.cpuMemRead(cpu.getPC());
                    cpu.addPC(1);
                    if (cpu.getPrefix() == 0xDD) {
                        reg = cpu.cpuMemRead(cpu.getIX() + (int) offset);
                        result = reg;
                        --result;
                        cpu.cpuMemWrite(cpu.getIX() + (int) offset, result);
                    } else {
                        reg = cpu.cpuMemRead(cpu.getIY() + (int) offset);
                        result = reg;
                        --result;
                        cpu.cpuMemWrite(cpu.getIY() + (int) offset, result);
                    }
                    time += 19 - 4;
                }
                break;
            default:
                reg = cpu.getA();
                cpu.addA(-1);
                result = cpu.getA();
                break;
        }

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result)
                + CPU.hcsubchk(reg, 1, 0) + CPU.x3chk(result) + CPU.vchksub(reg, 1, result)
                + CPU.SUB_INSTR + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return time;
    }
}
