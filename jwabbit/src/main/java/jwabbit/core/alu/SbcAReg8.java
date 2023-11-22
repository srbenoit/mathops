package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "sbc_a_reg8" opcode.
 */
public final class SbcAReg8 implements IOpcode {

    /** The subtract logic. */
    private final SubAReg sub;

    /**
     * Constructs a new {@code SbcAReg8}.
     */
    public SbcAReg8() {

        this.sub = new SubAReg();
    }

    /**
     * Opcode sbc_a_reg8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "sbc_a_reg8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        return this.sub.exec(cpu, (byte) (cpu.getF() & 1));
    }
}
