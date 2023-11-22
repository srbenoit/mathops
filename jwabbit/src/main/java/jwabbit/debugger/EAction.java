package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Actions and their corresponding action commands.
 */
enum EAction {

    /** Resets the currently selected calculator. */
    resetCalc("reset"),

    /** Exits the debugger. */
    exit("exit"),

    /** Adds a new memory view (mem/ram/flash). */
    addMemoryView("addmemview"),

    /** Removes the currently active memory view (mem/ram/flash). */
    removeMemoryView("remmemview"),

    /** Adds a new disassembly view. */
    addDisasmView("adddisview"),

    /** Removes the currently active disassembly view. */
    removeDisasmView("remdisview"),

    /** Puts the currently selected calculator in a running state. */
    runCalc("run"),

    /** Puts the currently selected calculator in a stopped state. */
    stopCalc("stop"),

    /** Opens a popup to enter a time for which to run the currently selected calculator. */
    runCalcTimed("runtimed"),

    /** Executes one instruction in the currently selected calculator. */
    stepCalc("step"),

    /** Executes N instructions in the currently selected calculator. */
    stepnCalc("stepn"),

    /** Executes until the PC is on instruction after the current instruction. */
    stepOverCalc("stepover"),

    /** Executes until a return has been executed. */
    stepOutCalc("stepout"),

    /** Dumps memory. */
    dumpMem("dumpmem"),

    /** Runs the profiler. */
    profile("profile"),

    /** Runs the code counter. */
    codeCount("codecount"),

    /** Toggles an execution breakpoint at the current selection. */
    toggleExecutionBreak("togglebreak"),

    /** Toggles a general memory breakpoint at the current selection. */
    toggleMemoryBreak("togglemembreak"),

    /** Toggles a memory write breakpoint at the current selection. */
    toggleMemoryWriteBreak("togglememwrite"),

    /** Toggles a memory read breakpoint at the current selection. */
    toggleMemoryReadBreak("togglememread"),

    /** Opens a popup to go to (set the PC to) a specified address. */
    gotoAddress("goto");

    /** The action command. */
    private final String cmd;

    /**
     * Constructs a new {@code EActions}.
     *
     * @param theCmd the action command
     */
    EAction(final String theCmd) {

        this.cmd = theCmd;
    }

    /**
     * Gets the action command.
     *
     * @return the action command
     */
    public String getCmd() {

        return this.cmd;
    }
}
