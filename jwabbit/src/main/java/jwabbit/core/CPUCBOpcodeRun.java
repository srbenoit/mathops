package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Runs a CB opcode.
 */
final class CPUCBOpcodeRun implements IOpcode {

    /**
     * Constructs a new {@code CPUCBOpcodeRun}.
     */
    CPUCBOpcodeRun() {

        // No action
    }

    /**
     * Runs a CB opcode.
     *
     * <p>
     * WABBITEMU SOURCE: core/core.c: "CPU_CB_opcode_run" function.
     */
    @Override
    public int exec(final CPU cpu) {

        final int time;

        if (cpu.getPrefix() == 0) {
            cpu.cpuOpcodeFetch();
            time = OpTable.cbTab[cpu.getBus()].exec(cpu);
        } else {
            cpu.cpuMemRead(cpu.getPC());
            cpu.addPC(1);
            final byte offset = (byte) cpu.getBus();
            cpu.cpuOpcodeFetch();
            cpu.setR(((cpu.getR() - 1) & 0x7f) + (cpu.getR() & 0x80));
            time = OpTable.icbOpcode[cpu.getBus()].exec(cpu, offset);
        }

        return time;
    }
}
