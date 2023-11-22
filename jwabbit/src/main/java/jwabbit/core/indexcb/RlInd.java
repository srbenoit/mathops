package jwabbit.core.indexcb;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IIndexOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "rl_ind" opcode.
 */
public final class RlInd implements IIndexOpcode {

    /**
     * Constructs a new {@code RlInd}.
     */
    public RlInd() {

        super();
    }

    /**
     * Opcode rl_ind.
     *
     * <p>
     * WABBITEMU SOURCE: core/indexcb.c, "rl_ind" function.
     */
    @Override
    public int exec(final CPU cpu, final byte index) {

        int result;
        final int carry;
        final int save = cpu.getBus() & 0x07;

        if (cpu.getPrefix() == JWCoreConstants.IX_PREFIX) {
            result = cpu.cpuMemRead(cpu.getIX() + (int) index);
            carry = (result >> 7) & 1;
            result = (result << 1) + (cpu.getF() & 1);
            cpu.cpuMemWrite(cpu.getIX() + (int) index, result);
        } else {
            result = cpu.cpuMemRead(cpu.getIY() + (int) index);
            carry = (result >> 7) & 1;
            result = (result << 1) + (cpu.getF() & 1);
            cpu.cpuMemWrite(cpu.getIY() + (int) index, result);
        }

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result) + CPU.x3chk(result)
                + CPU.parity(result) + carry);

        switch (save) {
            case 0:
                cpu.setB(result);
                break;
            case 1:
                cpu.setC(result);
                break;
            case 2:
                cpu.setD(result);
                break;
            case 3:
                cpu.setE(result);
                break;
            case 4:
                cpu.setH(result);
                break;
            case 5:
                cpu.setL(result);
                break;
            case 7:
                cpu.setA(result);
                break;
            default:
                break;
        }

        return 23;
    }
}
