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
 * The "add_hl_reg16" opcode.
 */
public final class AddHLReg16 implements IOpcode {

    /**
     * Constructs a new {@code AddHLReg16}.
     */
    public AddHLReg16() {

        // No action
    }

    /**
     * Opcode add_hl_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "add_hl_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg;
        int time = 11;

        switch ((cpu.getBus() >> 4) & 0x03) {
            case 0x00:
                reg = cpu.getBC();
                break;
            case 0x01:
                reg = cpu.getDE();
                break;
            case 0x02:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.getHL();
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIX();
                    time += 4;
                } else {
                    reg = cpu.getIY();
                    time += 4;
                }
                break;
            default:
                reg = cpu.getSP();
                break;
        }

        final int base;
        final int result;
        if (cpu.getPrefix() == 0) {
            base = cpu.getHL();
            result = base + reg;
            cpu.setHL(result);
        } else if (cpu.getPrefix() == 0xDD) {
            base = cpu.getIX();
            result = base + reg;
            cpu.setIX(result);
        } else {
            base = cpu.getIY();
            result = base + reg;
            cpu.setIY(result);
        }

        cpu.setF(cpu.unaffect(
                JWCoreConstants.SIGN_MASK + JWCoreConstants.ZERO_MASK + JWCoreConstants.PV_MASK)
                + CPU.x5chk16(result) + CPU.hcaddchk16(base, reg, 0) + CPU.x3chk16(result)
                + CPU.ADD_INSTR + CPU.carrychk16(base + reg));

        return time;
    }
}
