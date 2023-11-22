package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "sub_a_reg8" opcode.
 */
public final class SubAReg8 implements IOpcode {

    /** The subtract logic. */
    private final SubAReg sub;

    /**
     * Constructs a new {@code SubAReg8}.
     */
    public SubAReg8() {

        this.sub = new SubAReg();
    }

    /**
     * Opcode sub_a_reg8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "sub_a_reg8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        return this.sub.exec(cpu, (byte) 0);
    }
}
