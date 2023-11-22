package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ret" opcode.
 */
public final class Ret implements IOpcode {

    /**
     * Constructs a new {@code Ret}.
     */
    public Ret() {

        // No action
    }

    /**
     * Opcode ret.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ret" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setPC(cpu.cpuMemRead(cpu.getSP()));
        cpu.addSP(1);

        cpu.setPC(cpu.getPC() | (cpu.cpuMemRead(cpu.getSP()) << 8));
        cpu.addSP(1);

        return 10;
    }
}
