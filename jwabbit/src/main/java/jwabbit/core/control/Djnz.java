package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "djnz" opcode.
 */
public final class Djnz implements IOpcode {

    /**
     * Constructs a new {@code Djnz}.
     */
    public Djnz() {

        // No action
    }

    /**
     * Opcode djnz.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "djnz" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final byte reg = (byte) cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        cpu.addB(-1);
        if (cpu.getB() != 0) {
            cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);
            return 13;
        }

        return 8;
    }
}
