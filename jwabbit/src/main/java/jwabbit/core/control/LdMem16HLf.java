package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_mem16_hlf" opcode.
 */
public final class LdMem16HLf implements IOpcode {

    /**
     * Constructs a new {@code LdMem16HLf}.
     */
    public LdMem16HLf() {

        // No action
    }

    /**
     * Opcode ld_mem16_hlf.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_mem16_hlf" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int reg = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        reg = (reg | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        if (cpu.getPrefix() == 0) {
            cpu.cpuMemWrite(reg, cpu.getL());
            ++reg;
            cpu.cpuMemWrite(reg, cpu.getH());
            return 16;
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.cpuMemWrite(reg, cpu.getIXL());
            ++reg;
            cpu.cpuMemWrite(reg, cpu.getIXH());
            return 20;
        } else {
            cpu.cpuMemWrite(reg, cpu.getIYL());
            ++reg;
            cpu.cpuMemWrite(reg, cpu.getIYH());
            return 20;
        }
    }
}
