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
 * The "cpdr" opcode.
 */
public final class Cpdr implements IOpcode {

    /**
     * Constructs a new {@code Cpdr}.
     */
    public Cpdr() {

        // No action
    }

    /**
     * ED opcode cpdr.
     *
     * <p>
     * Reads the value in the memory address in HL, compares that value to the value in A, decrements BC and HL, sets
     * flags based on the comparison, and advances CPU timer by 16 ticks, then if the PV flag is set and the ZERO flag
     * is not set, decrements the PC counter by 2 and advanced the CPU timer by 5 additional ticks.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "cpdr" function.
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

        if ((cpu.getF() & JWCoreConstants.PV_MASK) != 0
                && (cpu.getF() & JWCoreConstants.ZERO_MASK) == 0) {
            cpu.addPC(-2);
            return 21;
        }

        return 16;
    }
}
