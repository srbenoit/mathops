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
 * The "cpl" opcode.
 */
public final class Cpl implements IOpcode {

    /**
     * Constructs a new {@code Cpl}.
     */
    public Cpl() {

        // No action
    }

    /**
     * Opcode cpl.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "cpl" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int result = (~cpu.getA()) & 0xFF;

        cpu.setF(cpu
                .unaffect(JWCoreConstants.SIGN_MASK + JWCoreConstants.ZERO_MASK
                        + JWCoreConstants.PV_MASK + JWCoreConstants.CARRY_MASK)
                + CPU.x5chk(result) + JWCoreConstants.HC_MASK + CPU.x3chk(result)
                + JWCoreConstants.N_MASK);
        cpu.setA(result);

        return 4;
    }
}
