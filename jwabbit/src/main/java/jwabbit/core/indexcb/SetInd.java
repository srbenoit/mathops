package jwabbit.core.indexcb;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IIndexOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "set_ind" opcode.
 */
public final class SetInd implements IIndexOpcode {

    /**
     * Constructs a new {@code SetInd}.
     */
    public SetInd() {

        super();
    }

    /**
     * Opcode set_ind.
     *
     * <p>
     * WABBITEMU SOURCE: core/indexcb.c, "set_ind" function.
     */
    @Override
    public int exec(final CPU cpu, final byte index) {

        int reg;
        final int bit = (1 << ((cpu.getBus() >> 3) & 0x07)) & 0xFF;
        final int save = cpu.getBus() & 0x07;

        if (cpu.getPrefix() == JWCoreConstants.IX_PREFIX) {
            reg = cpu.cpuMemRead(cpu.getIX() + (int) index);
            cpu.cpuMemWrite(cpu.getIX() + (int) index, reg | bit);
        } else {
            reg = cpu.cpuMemRead(cpu.getIY() + (int) index);
            cpu.cpuMemWrite(cpu.getIY() + (int) index, reg | bit);
        }

        reg |= bit;
        switch (save) {
            case 0:
                cpu.setB(reg);
                break;
            case 1:
                cpu.setC(reg);
                break;
            case 2:
                cpu.setD(reg);
                break;
            case 3:
                cpu.setE(reg);
                break;
            case 4:
                cpu.setH(reg);
                break;
            case 5:
                cpu.setL(reg);
                break;
            case 7:
                cpu.setA(reg);
                break;
            default:
                break;
        }

        return 23;
    }
}
