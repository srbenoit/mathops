package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "pop_reg16" opcode.
 */
public final class PopReg16 implements IOpcode {

    /**
     * Constructs a new {@code PopReg16}.
     */
    public PopReg16() {

        // No action
    }

    /**
     * Opcode pop_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "pop_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int oldBus = cpu.getBus();
        int reg = cpu.cpuMemRead(cpu.getSP()) & 0xFFFF;
        cpu.addSP(1);

        reg = (reg | (cpu.cpuMemRead(cpu.getSP()) << 8)) & 0xFFFF;
        cpu.addSP(1);

        int time = 10;

        switch ((oldBus >> 4) & 0x03) {
            case 0:
                cpu.setBC(reg);
                break;
            case 1:
                cpu.setDE(reg);
                break;
            case 2:
                if (cpu.getPrefix() == 0) {
                    cpu.setHL(reg);
                } else if (cpu.getPrefix() == 0xDD) {
                    cpu.setIX(reg);
                    time += 4;
                } else {
                    cpu.setIY(reg);
                    time += 4;
                }
                break;
            default:
                cpu.setAF(reg);
                break;
        }

        return time;
    }
}
