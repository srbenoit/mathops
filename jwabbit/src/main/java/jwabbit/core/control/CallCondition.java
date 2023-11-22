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
 * The "call_condition" opcode.
 */
public final class CallCondition implements IOpcode {

    /**
     * Constructs a new {@code CallCondition}.
     */
    public CallCondition() {

        // No action
    }

    /**
     * Opcode call_condition.java.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "call_condition" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int condition = (cpu.getBus() >> 3) & 0x07;

        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);

        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        final boolean succeed = switch (condition) {
            case 0 -> (cpu.getF() & JWCoreConstants.ZERO_MASK) == 0;
            case 1 -> (cpu.getF() & JWCoreConstants.ZERO_MASK) != 0;
            case 2 -> (cpu.getF() & JWCoreConstants.CARRY_MASK) == 0;
            case 3 -> (cpu.getF() & JWCoreConstants.CARRY_MASK) != 0;
            case 4 -> (cpu.getF() & JWCoreConstants.PV_MASK) == 0;
            case 5 -> (cpu.getF() & JWCoreConstants.PV_MASK) != 0;
            case 6 -> (cpu.getF() & JWCoreConstants.SIGN_MASK) == 0;
            default -> (cpu.getF() & JWCoreConstants.SIGN_MASK) != 0;
        };

        if (succeed) {
            cpu.addSP(-1);
            cpu.cpuMemWrite(cpu.getSP(), cpu.getPC() >> 8);

            cpu.addSP(-1);
            cpu.cpuMemWrite(cpu.getSP(), cpu.getPC() & 0xFF);

            cpu.setPC(address);

            return 17;
        }

        return 10;
    }
}
