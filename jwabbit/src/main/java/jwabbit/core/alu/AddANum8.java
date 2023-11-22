package jwabbit.core.alu;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.IOpcode;

/**
 * The "add_a_num8" opcode.
 */
public final class AddANum8 implements IOpcode {

    /** The add logic. */
    private final AddANum add;

    /**
     * Constructs a new {@code AddANum8}.
     */
    public AddANum8() {

        this.add = new AddANum();
    }

    /**
     * Opcode add_a_num8.
     *
     * <p>
     * WABBITEMU SOURCE: core/alu.c, "add_a_num8" function.
     */
    @Override
    public int exec(final CPU cpu) {

        return this.add.exec(cpu, (byte) 0);
    }
}
