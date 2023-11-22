package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IIndexOpcode;

/**
 * The "add_a_num" opcode.
 */
final class AddANum implements IIndexOpcode {

    /**
     * Constructs a new {@code AddANum}.
     */
    AddANum() {

        // No action
    }

    /**
     * Opcode add_a_num.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "add_a_num" function.
     */
    @Override
    public int exec(final CPU cpu, final byte index) {

        final int reg = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        final int result = cpu.getA() + reg + (int) index;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result)
                + CPU.hcaddchk(cpu.getA(), reg, (int) index) + CPU.x3chk(result)
                + CPU.vchkadd(cpu.getA(), reg, result) + CPU.ADD_INSTR + CPU.carrychk(result));
        cpu.setA(result);

        return 7;
    }
}
