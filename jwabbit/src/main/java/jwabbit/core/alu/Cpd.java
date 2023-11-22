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
 * The "cpd" opcode.
 */
public final class Cpd implements IOpcode {

    /**
     * Constructs a new {@code Cpd}.
     */
    public Cpd() {

        // No action
    }

    /**
     * ED opcode cpd.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "cpd" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = cpu.cpuMemRead(cpu.getHL());
        final int result = cpu.getA() - reg;

        cpu.addBC(-1);
        cpu.addHL(-1);

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result)
                + CPU.x5chk(reg - ((cpu.getF() & JWCoreConstants.HC_MASK) >> 4))
                + CPU.hcsubchk(cpu.getA(), reg, 0)
                + CPU.x3chk(reg - ((cpu.getF() & JWCoreConstants.HC_MASK) >> 4))
                + CPU.doparity(cpu.getBC() != 0) + CPU.SUB_INSTR
                + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return 16;
    }
}
