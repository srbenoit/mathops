package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "inc_reg16" opcode.
 */
public final class IncReg16 implements IOpcode {

    /**
     * Constructs a new {@code IncReg16}.
     */
    public IncReg16() {

        // No action
    }

    /**
     * Opcode inc_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "inc_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        int time = 6;

        switch ((cpu.getBus() >> 4) & 0x03) {
            case 0x00:
                cpu.addBC(1);
                break;
            case 0x01:
                cpu.addDE(1);
                break;
            case 0x02:
                if (cpu.getPrefix() == 0) {
                    cpu.addHL(1);
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.addIX(1);
                    time += 4;
                } else {
                    cpu.addIY(1);
                    time += 4;
                }
                break;
            default:
                cpu.addSP(1);
                break;
        }

        return time;
    }
}
