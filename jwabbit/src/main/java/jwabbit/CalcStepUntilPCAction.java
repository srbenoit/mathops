package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action to request that the calculator executes steps until the PC equals some target. While executing, other
 * queued actions will continue to be processed, including new instances of this action, which will cancel the current
 * request and replace the target PC register value.
 */
class CalcStepUntilPCAction implements ICalcAction {

    /** The target PC. */
    private final int targetPC;

    /**
     * Constructs a new {@code CalcStepUntilPCAction}.
     *
     * @param theTargetPC the number of steps
     */
    CalcStepUntilPCAction(final int theTargetPC) {

        if (theTargetPC <= 0) {
            throw new IllegalArgumentException("Invalid target PC: " + theTargetPC);
        }

        this.targetPC = theTargetPC & 0x0000FFFF;
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
     * Gets the target PC.
     *
     * @return the target PC
     */
    final int getTargetPC() {

        return this.targetPC;
    }
}
