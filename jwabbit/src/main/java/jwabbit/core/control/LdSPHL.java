package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_sp_hl" opcode.
 */
public final class LdSPHL implements IOpcode {

    /**
     * Constructs a new {@code LdSPHL}.
     */
    public LdSPHL() {

        // No action
    }

    /**
     * Opcode ld_sp_hl.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_sp_hl" function.
     */
    @Override
    public int exec(final CPU cpu) {

        if (cpu.getPrefix() == 0) {
            cpu.setSP(cpu.getHL());
            return 6;
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.setSP(cpu.getIX());
            return 10;
        } else {
            cpu.setSP(cpu.getIY());
            return 10;
        }
    }
}
