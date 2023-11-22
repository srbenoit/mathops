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
 * The "ret_condition" opcode.
 */
public final class RetCondition implements IOpcode {

    /**
     * Constructs a new {@code RetCondition}.
     */
    public RetCondition() {

        // No action
    }

    /**
     * Opcode ret_condition.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "ret_condition" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final boolean succeed = switch ((cpu.getBus() >> 3) & 0x07) {
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
            cpu.setPC(cpu.cpuMemRead(cpu.getSP()));
            cpu.addSP(1);

            cpu.setPC(cpu.getPC() | (cpu.cpuMemRead(cpu.getSP()) << 8));
            cpu.addSP(1);

            return 11;
        }

        return 5;
    }
}
