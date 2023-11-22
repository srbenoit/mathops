package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Runs an IX/IY opcode.
 */
final class CPUIXYOpcodeRun implements IOpcode {

    /**
     * Constructs a new {@code CPUIXYOpcodeRun}.
     */
    CPUIXYOpcodeRun() {

        // No action
    }

    /**
     * Runs an IX/IY opcode.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_IXY_opcode_run" function.
     */
    @Override
    public int exec(final CPU cpu) {

        cpu.setPrefix(cpu.getBus());
        cpu.cpuOpcodeFetch();
        cpu.cpuOpcodeRun();
        cpu.setPrefix(0);

        return 0;
    }
}
