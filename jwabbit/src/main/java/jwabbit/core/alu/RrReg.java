package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "rr_reg" opcode.
 */
public final class RrReg implements IOpcode {

    /**
     * Constructs a new {@code RrReg}.
     */
    public RrReg() {

        // No action
    }

    /**
     * CB opcode rr_reg.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "rr_reg" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int result;
        final int carry;
        int time = 8;

        switch (cpu.getBus() & 0x07) {
            case 0x00:
                carry = cpu.getB() & 1;
                result = ((cpu.getB() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setB(result);
                break;
            case 0x01:
                carry = cpu.getC() & 1;
                result = ((cpu.getC() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setC(result);
                break;
            case 0x02:
                carry = cpu.getD() & 1;
                result = ((cpu.getD() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setD(result);
                break;
            case 0x03:
                carry = cpu.getE() & 1;
                result = ((cpu.getE() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setE(result);
                break;
            case 0x04:
                carry = cpu.getH() & 1;
                result = ((cpu.getH() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setH(result);
                break;
            case 0x05:
                carry = cpu.getL() & 1;
                result = ((cpu.getL() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setL(result);
                break;
            case 0x06:
                result = cpu.cpuMemRead(cpu.getHL());
                carry = result & 1;
                result = ((result >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                time += 7;
                cpu.cpuMemWrite(cpu.getHL(), result);
                break;
            default:
                carry = cpu.getA() & 1;
                result = ((cpu.getA() >> 1) + ((cpu.getF() & 1) << 7)) & 0xFF;
                cpu.setA(result);
                break;
        }

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result) + CPU.x3chk(result)
                + CPU.parity(result) + carry);

        return time;
    }
}
