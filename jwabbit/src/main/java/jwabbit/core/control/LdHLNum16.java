package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_hl_num16" opcode.
 */
public final class LdHLNum16 implements IOpcode {

    /**
     * Constructs a new {@code LdHLNum16}.
     */
    public LdHLNum16() {

        // No action
    }

    /**
     * Opcode ld_hl_num16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_hl_num16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int reg = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        reg = (reg | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        if (cpu.getPrefix() == 0) {
            cpu.setHL(reg);
            return 10;
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.setIX(reg);
            return 14;
        } else {
            cpu.setIY(reg);
            return 14;
        }
    }
}
