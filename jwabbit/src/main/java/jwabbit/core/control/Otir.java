package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;
import jwabbit.core.JWCoreConstants;

/**
 * The "otir" opcode.
 */
public final class Otir implements IOpcode {

    /**
     * Constructs a new {@code Otir}.
     */
    public Otir() {

        // No action
    }

    /**
     * Opcode otir.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "otir" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int result = cpu.cpuMemRead(cpu.getHL());
        cpu.deviceOutput(cpu.getC());

        cpu.addB(-1);
        cpu.addHL(1);
        final int tmp = result + cpu.getL();

        cpu.setF(CPU.signchk(cpu.getB()) + CPU.zerochk(cpu.getB()) + CPU.x5chk(cpu.getB())
                + CPU.dohc(tmp > 0xFF) + CPU.x3chk(cpu.getB()) + CPU.parity((tmp & 0x07) ^ cpu.getB())
                + (((result & 0x80) != 0) ? JWCoreConstants.N_MASK : 0) + CPU.carry(tmp > 0xFF));

        if (cpu.getB() != 0) {
            cpu.addPC(-2);
            return 21;
        }

        return 16;
    }
}
