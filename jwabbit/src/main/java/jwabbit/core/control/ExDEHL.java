package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ex_de_hl" opcode.
 */
public final class ExDEHL implements IOpcode {

    /**
     * Constructs a new {@code ExDEHL}.
     */
    public ExDEHL() {

        // No action
    }

    /**
     * Opcode ex_de_hl.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ex_de_hl" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.getHL();
        cpu.setHL(cpu.getDE());
        cpu.setDE(reg);

        return 4;
    }
}
