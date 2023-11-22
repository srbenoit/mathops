package jwabbit.core.control;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "ld_mem16_reg16" opcode.
 */
public final class LdMem16Reg16 implements IOpcode {

    /**
     * Constructs a new {@code LdMem16Reg16}.
     */
    public LdMem16Reg16() {

        // No action
    }

    /**
     * Opcode ld_mem16_reg16.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ld_mem16_reg16" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int test = (cpu.getBus() >> 4) & 0x03;

        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);
        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        final int result = switch (test) {
            case 0 -> cpu.getBC();
            case 1 -> cpu.getDE();
            case 2 -> cpu.getHL();
            case 3 -> cpu.getSP();
            default -> 0;
        };

        cpu.cpuMemWrite(address, result & 0xFF);
        ++address;
        cpu.cpuMemWrite(address, result >> 8);

        return 20;
    }
}
