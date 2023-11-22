package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "set" opcode.
 */
public final class Set implements IOpcode {

    /**
     * Constructs a new {@code Set}.
     */
    public Set() {

        // No action
    }

    /**
     * CB opcode set num,reg.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "set" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int bit = (1 << ((cpu.getBus() >> 3) & 0x7)) & 0xFF;
        int time = 8;

        cpu.getTimerContext().tcAdd(8L);

        switch (cpu.getBus() & 0x07) {
            case 0x00:
                cpu.setB(cpu.getB() | bit);
                break;
            case 0x01:
                cpu.setC(cpu.getC() | bit);
                break;
            case 0x02:
                cpu.setD(cpu.getD() | bit);
                break;
            case 0x03:
                cpu.setE(cpu.getE() | bit);
                break;
            case 0x04:
                cpu.setH(cpu.getH() | bit);
                break;
            case 0x05:
                cpu.setL(cpu.getL() | bit);
                break;
            case 0x06:
                final int reg = cpu.cpuMemRead(cpu.getHL());
                time += 7;
                cpu.cpuMemWrite(cpu.getHL(), reg | bit);
                break;
            default:
                cpu.setA(cpu.getA() | bit);
                break;
        }

        return time;
    }
}
