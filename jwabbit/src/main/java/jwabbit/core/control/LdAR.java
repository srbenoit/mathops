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
 * The "ld_a_r" opcode.
 */
public final class LdAR implements IOpcode {

    /**
     * Constructs a new {@code LdAR}.
     */
    public LdAR() {

        // No action
    }

    /**
     * Opcode ld_a_r.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_a_r" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setA(cpu.getR());

        cpu.setF(CPU.signchk(cpu.getA()) + CPU.zerochk(cpu.getA()) + CPU.x5chk(cpu.getA())
                + CPU.x3chk(cpu.getA()) + CPU.doparity(cpu.isIff2())
                + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return 9;
    }
}
