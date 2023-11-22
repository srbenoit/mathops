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
 * The "bit" opcode.
 */
public final class Bit implements IOpcode {

    /**
     * Constructs a new {@code Bit}.
     */
    public Bit() {

        // No action
    }

    /**
     * CB opcode bit num,reg.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "bit" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int dbus = cpu.getBus();
        int time = 8;

        final int reg;
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
                reg = cpu.getH();
                break;
            case 0x05:
                reg = cpu.getL();
                break;
            case 0x06:
                reg = cpu.cpuMemRead(cpu.getHL());
                time += 4;
                break;
            default:
                reg = cpu.getA();
                break;
        }

        final int result = reg & (1 << ((dbus >> 3) & 0x07));

        final int xchk;
//        if ((dbus & 0x07) == 0x06) {
//            xchk = cpu.getH();
//        } else {
//            xchk = result;
//        }
        xchk = result;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(xchk) + JWCoreConstants.HC_MASK
                + CPU.x3chk(xchk) + CPU.parity(result) + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return time;
    }
}
