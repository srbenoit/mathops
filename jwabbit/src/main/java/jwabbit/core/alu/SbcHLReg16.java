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
 * The "sbc_hl_reg16" opcode.
 */
public final class SbcHLReg16 implements IOpcode {

    /**
     * Constructs a new {@code SbcHLReg16}.
     */
    public SbcHLReg16() {

        // No action
    }

    /**
     * ED opcode sbc_hl_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "sbc_hl_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg = switch ((cpu.getBus() >> 4) & 0x03) {
            case 0x00 -> cpu.getBC();
            case 0x01 -> cpu.getDE();
            case 0x02 -> cpu.getHL();
            default -> cpu.getSP();
        };

        final int result = cpu.getHL() - reg - (cpu.getF() & JWCoreConstants.CARRY_MASK);

        cpu.setF(CPU.signchk16(result) + CPU.zerochk16(result) + CPU.x5chk16(result)
                + CPU.hcsubchk16(cpu.getHL(), reg, cpu.getF() & JWCoreConstants.CARRY_MASK)
                + CPU.x3chk16(result) + CPU.vchksub16(cpu.getHL(), reg, result) + CPU.SUB_INSTR
                + CPU.carrychk16(result));

        cpu.setHL(result);

        return 15;
    }
}
