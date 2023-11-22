package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "ldd" opcode.
 */
public final class Ldd implements IOpcode {

    /**
     * Constructs a new {@code Ldd}.
     */
    public Ldd() {

        // No action
    }

    /**
     * Opcode ldd.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ldd" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.cpuMemRead(cpu.getHL());
        cpu.cpuMemWrite(cpu.getDE(), reg);

        final int tmp = cpu.getA() + reg;
        cpu.addBC(-1);
        cpu.addHL(-1);
        cpu.addDE(-1);

        cpu.setF(CPU.dox5((tmp & 2) != 0) + CPU.dox3((tmp & 8) != 0)
                + CPU.doparity(cpu.getBC() != 0) + cpu.unaffect(JWCoreConstants.SIGN_MASK
                + JWCoreConstants.ZERO_MASK + JWCoreConstants.CARRY_MASK));

        return 16;
    }
}
