package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "in_reg_c" opcode.
 */
public final class InRegC implements IOpcode {

    /**
     * Constructs a new {@code InRegC}.
     */
    public InRegC() {

        // No action
    }

    /**
     * Opcode in_reg_c.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "in_reg_c" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 3) & 0x07;
        cpu.deviceInput(cpu.getC());

        switch (test) {
            case 0x00:
                cpu.setB(cpu.getBus());
                break;
            case 0x01:
                cpu.setC(cpu.getBus());
                break;
            case 0x02:
                cpu.setD(cpu.getBus());
                break;
            case 0x03:
                cpu.setE(cpu.getBus());
                break;
            case 0x04:
                cpu.setH(cpu.getBus());
                break;
            case 0x05:
                cpu.setL(cpu.getBus());
                break;
            case 0x06:
                break;
            default:
                cpu.setA(cpu.getBus());
                break;
        }

        cpu.setF(CPU.signchk(cpu.getBus()) + CPU.zerochk(cpu.getBus()) + CPU.x5chk(cpu.getBus())
                + CPU.x3chk(cpu.getBus()) + CPU.parity(cpu.getBus())
                + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return 12;
    }
}
