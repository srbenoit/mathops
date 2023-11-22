package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "out" opcode.
 */
public final class Out implements IOpcode {

    /**
     * Constructs a new {@code Out}.
     */
    public Out() {

        // No action
    }

    /**
     * Opcode out.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "out" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int port = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        cpu.setBus(cpu.getA());

        cpu.deviceOutput(port);

        return 11;
    }
}
