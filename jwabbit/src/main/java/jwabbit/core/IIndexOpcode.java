package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An opcode that takes a CPU argument and in integer index.
 */
@FunctionalInterface
public interface IIndexOpcode {

    /**
     * Executes the opcode.
     *
     * @param cpu   the CPU on which to execute the opcode
     * @param index the index
     * @return the elapsed time
     */
    int exec(CPU cpu, byte index);
}
