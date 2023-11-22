package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "neg" opcode.
 */
public final class Neg implements IOpcode {

    /**
     * Constructs a new {@code Neg}.
     */
    public Neg() {

        // No action
    }

    /**
     * ED opcode neg.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "neg" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int result = -cpu.getA();

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result)
                + CPU.hcsubchk(0, cpu.getA(), 0) + CPU.x3chk(result)
                + CPU.vchksub(0, cpu.getA(), result) + CPU.SUB_INSTR + CPU.carrychk(result));

        cpu.setA(result);

        return 8;
    }
}
