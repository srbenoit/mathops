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
 * The "bit_ind" opcode.
 */
public final class BitInd implements IIndexOpcode {

    /**
     * Constructs a new {@code BitInd}.
     */
    public BitInd() {

        super();
    }

    /**
     * Opcode bit_ind.
     *
     * <p>
     * WABBITEMU SOURCE: core/indexcb.c, "bit_ind" function.
     */
    @Override
    public int exec(final CPU cpu, final byte index) {

        final int testMask = 1 << ((cpu.getBus() >> 3) & 0x07);

        final int address;
        if (cpu.getPrefix() == JWCoreConstants.IX_PREFIX) {
            address = (cpu.getIX() + (int) index) & 0xFFFF;
        } else {
            address = (cpu.getIY() + (int) index) & 0xFFFF;
        }

        final int reg = cpu.cpuMemRead(address);
        final int result = reg & testMask;

        cpu.setF(CPU.signchk(result) + CPU.zerochk(result) + CPU.x5chk16(address)
                + JWCoreConstants.HC_MASK + CPU.x3chk16(address) + CPU.parity(result)
                + cpu.unaffect(JWCoreConstants.CARRY_MASK));

        return 20;
    }
}
