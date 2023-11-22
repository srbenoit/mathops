package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IIndexOpcode;

/**
 * The "sub_a_num" opcode.
 */
final class SubANum implements IIndexOpcode {

    /**
     * Constructs a new {@code SubANum}.
     */
    SubANum() {

        // No action
    }

    /**
     * Opcode sub_a_num.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "sub_a_num" function.
     */
    @Override
    public int exec(final CPU cpu, final byte index) {

        final int reg = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);
        final int result = cpu.getA() - reg - (int) index;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(result)
                + CPU.hcsubchk(cpu.getA(), reg, (int) index) + CPU.x3chk(result)
                + CPU.vchksub(cpu.getA(), reg, result) + CPU.SUB_INSTR + CPU.carrychk(result));
        cpu.setA(result);

        return 7;
    }
}
