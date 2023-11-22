package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ex_sp_hl" opcode.
 */
public final class ExSPHL implements IOpcode {

    /**
     * Constructs a new {@code ExSPHL}.
     */
    public ExSPHL() {

        // No action
    }

    /**
     * Opcode ex_sp_hl.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ex_sp_hl" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int reg = cpu.cpuMemRead(cpu.getSP()) & 0xFFFF;
        reg = (reg | (cpu.cpuMemRead(cpu.getSP() + 1) << 8)) & 0xFFFF;

        if (cpu.getPrefix() == 0) {
            cpu.cpuMemWrite(cpu.getSP() + 1, cpu.getH());
            cpu.cpuMemWrite(cpu.getSP(), cpu.getL());
            cpu.setHL(reg);

            return 19;
        }

        if (cpu.getPrefix() == 0xDD) {
            cpu.cpuMemWrite(cpu.getSP() + 1, cpu.getIXH());
            cpu.cpuMemWrite(cpu.getSP(), cpu.getIXL());
            cpu.setIX(reg);
        } else {
            cpu.cpuMemWrite(cpu.getSP() + 1, cpu.getIYH());
            cpu.cpuMemWrite(cpu.getSP(), cpu.getIYL());
            cpu.setIY(reg);
        }

        return 23;
    }
}
