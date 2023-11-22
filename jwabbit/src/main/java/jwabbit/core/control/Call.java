package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "call" opcode.
 */
public final class Call implements IOpcode {

    /**
     * Constructs a new {@code Call}.
     */
    public Call() {

        // No action
    }

    /**
     * Opcode call.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "call" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), cpu.getPC() >> 8);

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), cpu.getPC() & 0xFF);

        cpu.setPC(address);

        return 17;
    }
}
