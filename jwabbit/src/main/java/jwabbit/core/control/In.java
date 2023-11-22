package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "in" opcode.
 */
public final class In implements IOpcode {

    /**
     * Constructs a new {@code In}.
     */
    public In() {

        // No action
    }

    /**
     * Opcode in.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "in" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int port = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        cpu.deviceInput(port);
        cpu.setA(cpu.getBus());

        return 11;
    }
}
