package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_hlf_mem16" opcode.
 */
public final class LdHLfMem16 implements IOpcode {

    /**
     * Constructs a new {@code LdHLfMem16}.
     */
    public LdHLfMem16() {

        // No action
    }

    /**
     * Opcode ld_hlf_mem16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_hlf_mem16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int mem = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        mem = (mem | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        int reg = cpu.cpuMemRead(mem) & 0xFFFF;
        reg = (reg | (cpu.cpuMemRead(mem + 1) << 8)) & 0xFFFF;

        if (cpu.getPrefix() == 0) {
            cpu.setHL(reg);
            return 16;
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.setIX(reg);
            return 20;
        } else {
            cpu.setIY(reg);
            return 20;
        }
    }
}
