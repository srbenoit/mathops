package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ex_af_afp" opcode.
 */
public final class ExAFAFp implements IOpcode {

    /**
     * Constructs a new {@code ExAFAFp}.
     */
    public ExAFAFp() {

        // No action
    }

    /**
     * Opcode ex_af_afp.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ex_af_afp" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.getAF();
        cpu.setAF(cpu.getAFprime());
        cpu.setAFprime(reg);

        return 4;
    }
}
