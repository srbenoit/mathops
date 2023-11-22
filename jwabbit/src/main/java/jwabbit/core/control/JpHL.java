package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "jp_hl" opcode.
 */
public final class JpHL implements IOpcode {

    /**
     * Constructs a new {@code JpHL}.
     */
    public JpHL() {

        // No action
    }

    /**
     * Opcode jp_hl.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "jp_hl" function.
     */
    @Override
    public int exec(final CPU cpu) {

        if (cpu.getPrefix() == 0) {
            cpu.setPC(cpu.getHL());
            return 4;
        } else if (cpu.getPrefix() == 0xDD) {
            cpu.setPC(cpu.getIX());
            return 8;
        } else {
            cpu.setPC(cpu.getIY());
            return 8;
        }
    }
}
