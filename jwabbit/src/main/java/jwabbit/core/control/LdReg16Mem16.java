package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_reg16_mem16" opcode.
 */
public final class LdReg16Mem16 implements IOpcode {

    /**
     * Constructs a new {@code LdReg16Mem16}.
     */
    public LdReg16Mem16() {

        // No action
    }

    /**
     * Opcode ld_reg16_mem16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_reg16_mem16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 4) & 0x03;
        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);
        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        int result = cpu.cpuMemRead(address) & 0xFFFF;
        result = (result | (cpu.cpuMemRead(address + 1) << 8)) & 0xFFFF;

        switch (test) {
            case 0:
                cpu.setBC(result);
                break;
            case 1:
                cpu.setDE(result);
                break;
            case 2:
                cpu.setHL(result);
                break;
            default:
                cpu.setSP(result);
                break;
        }

        return 20;
    }
}
