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
 * The "jr_condition" opcode.
 */
public final class JrCondition implements IOpcode {

    /**
     * Constructs a new {@code JrCondition}.
     */
    public JrCondition() {

        // No action
    }

    /**
     * Opcode jr_condition.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "jr_condition" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int condition = (cpu.getBus() >> 3) & 0x03;

        final byte reg = (byte) cpu.cpuMemRead(cpu.getPC());
        cpu.addPC(1);

        int time = 7;

        switch (condition) {
            case 0x00:
                if ((JWCoreConstants.ZERO_MASK & cpu.getF()) == 0) {
                    cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);
                    time += 5;
                }
                break;
            case 0x01:
                if ((JWCoreConstants.ZERO_MASK & cpu.getF()) != 0) {
                    cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);
                    time += 5;
                }
                break;
            case 0x02:
                if ((JWCoreConstants.CARRY_MASK & cpu.getF()) == 0) {
                    cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);
                    time += 5;
                }
                break;
            default:
                if ((JWCoreConstants.CARRY_MASK & cpu.getF()) != 0) {
                    cpu.setPC((cpu.getPC() & 0xFFFF) + (int) reg);
                    time += 5;
                }
                break;
        }

        return time;
    }
}
