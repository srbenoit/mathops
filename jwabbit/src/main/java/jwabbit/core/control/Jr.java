package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "jr" opcode.
 */
public final class Jr implements IOpcode {

    /**
     * Constructs a new {@code Jr}.
     */
    public Jr() {

        // No action
    }

    /**
     * Opcode jr.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "jr" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final byte reg = (byte) cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);

        return 12;
    }
}
