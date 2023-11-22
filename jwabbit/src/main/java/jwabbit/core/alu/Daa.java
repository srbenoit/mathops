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
 * The "daa" opcode.
 */
public final class Daa implements IOpcode {

    /**
     * Constructs a new {@code Daa}.
     */
    public Daa() {

        // No action
    }

    /**
     * Opcode daa.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "daa" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int result = cpu.getA();

        if ((cpu.getF() & JWCoreConstants.N_MASK) == 0) {
            if ((cpu.getF() & JWCoreConstants.HC_MASK) != 0 || (cpu.getA() & 0x0F) > 9) {
                result += 0x06;
            }
            if ((cpu.getF() & JWCoreConstants.CARRY_MASK) != 0 || (cpu.getA() > 0x99)) {
                result += 0x60;
            }
        } else {
            if ((cpu.getF() & JWCoreConstants.HC_MASK) != 0 || (cpu.getA() & 0x0F) > 9) {
                result -= 0x06;
            }
            if ((cpu.getF() & JWCoreConstants.CARRY_MASK) != 0 || (cpu.getA() > 0x99)) {
                result -= 0x60;
            }
        }

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result)
                + ((cpu.getA() & 0x10) ^ (result & 0x10)) + CPU.x3chk(result) + CPU.parity(result)
                + cpu.unaffect(JWCoreConstants.N_MASK) + ((cpu.getF() & JWCoreConstants.CARRY_MASK)
                | ((cpu.getA() > 0x99) ? JWCoreConstants.CARRY_MASK : 0)));
        cpu.setA(result);

        return 4;
    }
}
