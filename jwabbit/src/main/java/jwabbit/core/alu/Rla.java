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
 * The "rla" opcode.
 */
public final class Rla implements IOpcode {

    /**
     * Constructs a new {@code Rla}.
     */
    public Rla() {

        // No action
    }

    /**
     * Opcode rla.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "rla" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int result = ((cpu.getA() << 1) + (cpu.getF() & 1)) & 0xFF;

        cpu.setF(cpu.unaffect(
                JWCoreConstants.SIGN_MASK + JWCoreConstants.ZERO_MASK + JWCoreConstants.PV_MASK)
                + CPU.x5chk(result) + CPU.x3chk(result) + ((cpu.getA() >> 7) & 1));
        cpu.setA(result);

        return 4;
    }
}
