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
 * The "scf" opcode.
 */
public final class Scf implements IOpcode {

    /**
     * Constructs a new {@code Scf}.
     */
    public Scf() {

        // No action
    }

    /**
     * Opcode scf.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "scf" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setF(cpu.unaffect(
                JWCoreConstants.SIGN_MASK + JWCoreConstants.ZERO_MASK + JWCoreConstants.PV_MASK)
                + CPU.x5chk(cpu.getA()) + CPU.x3chk(cpu.getA()) + JWCoreConstants.CARRY_MASK);

        return 4;
    }
}
