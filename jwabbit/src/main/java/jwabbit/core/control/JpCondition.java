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
 * The "jp_condition" opcode.
 */
public final class JpCondition implements IOpcode {

    /**
     * Constructs a new {@code JpCondition}.
     */
    public JpCondition() {

        // No action
    }

    /**
     * Opcode jp_condition.
     *
     * <p>
     * WABBITEMU SOURCE: core/control.c, "jp_condition" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int condition = (cpu.getBus() >> 3) & 0x07;
        int address = cpu.cpuMemRead(cpu.getPC()) & 0xFFFF;
        cpu.addPC(1);
        address = (address | (cpu.cpuMemRead(cpu.getPC()) << 8)) & 0xFFFF;
        cpu.addPC(1);

        switch (condition) {
            case 0x00:
                if ((JWCoreConstants.ZERO_MASK & cpu.getF()) == 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x01:
                if ((JWCoreConstants.ZERO_MASK & cpu.getF()) != 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x02:
                if ((JWCoreConstants.CARRY_MASK & cpu.getF()) == 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x03:
                if ((JWCoreConstants.CARRY_MASK & cpu.getF()) != 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x04:
                if ((JWCoreConstants.PV_MASK & cpu.getF()) == 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x05:
                if ((JWCoreConstants.PV_MASK & cpu.getF()) != 0) {
                    cpu.setPC(address);
                }
                break;
            case 0x06:
                if ((JWCoreConstants.SIGN_MASK & cpu.getF()) == 0) {
                    cpu.setPC(address);
                }
                break;
            default:
                if ((JWCoreConstants.SIGN_MASK & cpu.getF()) != 0) {
                    cpu.setPC(address);
                }
                break;
        }

        return 10;
    }
}
