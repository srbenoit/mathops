package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Runs an ED opcode.
 */
final class CPUEDOpcodeRun implements IOpcode {

    /**
     * Constructs a new {@code CPUEDOpcodeRun}.
     */
    CPUEDOpcodeRun() {

        // No action
    }

    /**
     * Runs an ED opcode.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_ED_opcode_run" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.cpuOpcodeFetch();
        return OpTable.edTab[cpu.getBus()].exec(cpu);
    }
}
