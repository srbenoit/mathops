package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "exx" opcode.
 */
public final class Exx implements IOpcode {

    /**
     * Constructs a new {@code Exx}.
     */
    public Exx() {

        // No action
    }

    /**
     * Opcode exx.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "exx" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int reg;

        reg = cpu.getHL();
        cpu.setHL(cpu.getHLprime());
        cpu.setHLprime(reg);

        reg = cpu.getBC();
        cpu.setBC(cpu.getBCprime());
        cpu.setBCprime(reg);

        reg = cpu.getDE();
        cpu.setDE(cpu.getDEprime());
        cpu.setDEprime(reg);

        return 4;
    }
}
