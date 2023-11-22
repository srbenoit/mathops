package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "out_reg" opcode.
 */
public final class OutReg implements IOpcode {

    /**
     * Constructs a new {@code OutReg}.
     */
    public OutReg() {

        // No action
    }

    /**
     * Opcode out_reg.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "out_reg" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 3) & 0x07;

        switch (test) {
            case 0x00:
                cpu.setBus(cpu.getB());
                break;
            case 0x01:
                cpu.setBus(cpu.getC());
                break;
            case 0x02:
                cpu.setBus(cpu.getD());
                break;
            case 0x03:
                cpu.setBus(cpu.getE());
                break;
            case 0x04:
                cpu.setBus(cpu.getH());
                break;
            case 0x05:
                cpu.setBus(cpu.getL());
                break;
            case 0x06:
                cpu.setBus(0xFF);
                break;
            default:
                cpu.setBus(cpu.getA());
                break;
        }

        cpu.deviceOutput(cpu.getC());

        return 12;
    }
}
