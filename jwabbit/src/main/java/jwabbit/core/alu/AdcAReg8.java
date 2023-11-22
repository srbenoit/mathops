package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "adc_a_reg8" opcode.
 */
public final class AdcAReg8 implements IOpcode {

    /** The add logic. */
    private final AddAReg add;

    /**
     * Constructs a new {@code AdcAReg8}.
     */
    public AdcAReg8() {

        this.add = new AddAReg();
    }

    /**
     * Opcode adc_a_reg8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "adc_a_reg8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        return this.add.exec(cpu, (byte) (cpu.getF() & 1));
    }
}
