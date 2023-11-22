package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "xor_num8" opcode.
 */
public final class XorNum8 implements IOpcode {

    /**
     * Constructs a new {@code XorNum8}.
     */
    public XorNum8() {

        // No action
    }

    /**
     * Opcode xor_num8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "xor_num8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);
        final int result = cpu.getA() ^ reg;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result) + CPU.x3chk(result)
                + CPU.parity(result));
        cpu.setA(result);

        return 7;
    }
}
