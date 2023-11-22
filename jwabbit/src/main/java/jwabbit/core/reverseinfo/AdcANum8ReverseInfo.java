package jwabbit.core.reverseinfo;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IRevOpcode;

/**
 * The "adc_a_num8_reverse_info" opcode.
 */
public final class AdcANum8ReverseInfo implements IRevOpcode {

    /**
     * Constructs a new {@code AdcANum8ReverseInfo}.
     */
    public AdcANum8ReverseInfo() {

        // No action
    }

    /**
     * Opcode adc_a_num8_reverse_info.
     *
     * <p>
     * WABBITEMU SOURCE: core/reverse_info.c, "adc_a_num8_reverse_info" function.
     */
    @Override
    public void exec(final CPU cpu) {

        cpu.getPrevInstruction().setData1Lo(cpu.getA());
    }
}
