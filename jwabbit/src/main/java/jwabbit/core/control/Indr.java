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
 * The "indr" opcode.
 */
public final class Indr implements IOpcode {

    /**
     * Constructs a new {@code Indr}.
     */
    public Indr() {

        // No action
    }

    /**
     * Opcode indr.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "indr" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.deviceInput(cpu.getC());
        final int result = cpu.getBus();

        cpu.cpuMemWrite(cpu.getHL(), cpu.getBus());

        cpu.addB(-1);
        cpu.addHL(-1);
        final int tmp = result + ((cpu.getC() - 1) & 0xFF);

        cpu.setF(CPU.signchk(cpu.getB()) + CPU.zerochk(cpu.getB()) + CPU.x5chk(cpu.getB())
                + CPU.dohc(tmp > 0xFF) + CPU.x3chk(cpu.getB()) + CPU.parity((tmp & 7) ^ cpu.getB())
                + (((result & 0x80) != 0) ? JWCoreConstants.N_MASK : 0) + CPU.carry(tmp > 0xFF));

        if (cpu.getB() != 0) {
            cpu.addPC(-2);
            return 21;
        }

        return 16;
    }
}
