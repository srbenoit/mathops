package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "push_reg16" opcode.
 */
public final class PushReg16 implements IOpcode {

    /**
     * Constructs a new {@code PushReg16}.
     */
    public PushReg16() {

        super();
    }

    /**
     * Opcode push_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "push_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int reg;
        int time = 11;

        switch ((cpu.getBus() >> 4) & 0x03) {
            case 0:
                reg = cpu.getBC() & 0xFFFF;
                break;
            case 1:
                reg = cpu.getDE() & 0xFFFF;
                break;
            case 2:
                if (cpu.getPrefix() == 0) {
                    reg = cpu.getHL() & 0xFFFF;
                } else if (cpu.getPrefix() == 0xDD) {
                    reg = cpu.getIX() & 0xFFFF;
                    time += 4;
                } else {
                    reg = cpu.getIY() & 0xFFFF;
                    time += 4;
                }
                break;
            default:
                reg = cpu.getAF() & 0xFFFF;
                break;
        }

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), reg >> 8);

        cpu.addSP(-1);
        cpu.cpuMemWrite(cpu.getSP(), reg & 0xFF);

        return time;
    }
}
