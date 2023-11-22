package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "rld" opcode.
 */
public final class Rld implements IOpcode {

    /**
     * Constructs a new {@code Rld}.
     */
    public Rld() {

        // No action
    }

    /**
     * ED opcode rld.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "rld" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int result = (cpu.cpuMemRead(cpu.getHL()) << 4) + (cpu.getA() & 0x0F);

        cpu.cpuMemWrite(cpu.getHL(), result & 0x00FF);

        cpu.setA((cpu.getA() & 0xF0) + ((result >> 8) & 0x0F));

        cpu.setF(CPU.signchk(cpu.getA()) + CPU.zerochk(cpu.getA()) + CPU.x5chk(cpu.getA())
                + CPU.x3chk(cpu.getA()) + CPU.parity(cpu.getA())
                + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return 18;
    }
}
