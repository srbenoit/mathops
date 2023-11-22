package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "cp_num8" opcode.
 */
public final class CpNum8 implements IOpcode {

    /**
     * Constructs a new {@code CpNum8}.
     */
    public CpNum8() {

        // No action
    }

    /**
     * Opcode cp_num8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "cp_num8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);
        final int result = cpu.getA() - reg;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk(reg)
                + CPU.hcsubchk(cpu.getA(), reg, 0) + CPU.x3chk(reg)
                + CPU.vchksub(cpu.getA(), reg, result) + CPU.SUB_INSTR + CPU.carrychk(result));

        return 7;
    }
}
