package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "rst" opcode.
 */
public final class Rst implements IOpcode {

    /**
     * Constructs a new {@code Rst}.
     */
    public Rst() {

        // No action
    }

    /**
     * Opcode rst.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "rst" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.getBus() & 0x38;

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), (cpu.getPC() >> 8) & 0xFF);

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), cpu.getPC() & 0xFF);

        cpu.setPC(reg);

        return 11;
    }
}
