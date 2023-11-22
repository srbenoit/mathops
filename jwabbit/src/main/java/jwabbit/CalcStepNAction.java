package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action to request that the calculator executes some number of steps. While executing, other queued actions will
 * continue to be processed.
 */
public class CalcStepNAction implements ICalcAction {

    /** The number of steps. */
    private final int numSteps;

    /**
     * Constructs a new {@code CalcStepNAction}.
     *
     * @param theNumSteps the number of steps
     */
    public CalcStepNAction(final int theNumSteps) {

        if (theNumSteps <= 0) {
            throw new IllegalArgumentException("Invalid number of steps: " + theNumSteps);
        }

        this.numSteps = theNumSteps;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return ECalcAction.STEP_N;
    }

    /**
     * Gets the number of steps to execute.
     *
     * @return the number of steps
     */
    final int getNumSteps() {

        return this.numSteps;
    }
}
