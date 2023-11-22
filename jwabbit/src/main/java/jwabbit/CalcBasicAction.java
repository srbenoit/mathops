package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A basic calculator action with no associated data.
 */
public class CalcBasicAction implements ICalcAction {

    /** The type of action. */
    private final ECalcAction type;

    /**
     * Constructs a new {@code CalcBasicAction}.
     *
     * @param theType the type of action
     */
    public CalcBasicAction(final ECalcAction theType) {

        // Make sure it's not an action type that requires a specialized action class
        if (theType == ECalcAction.KEY_PRESSED || theType == ECalcAction.KEY_RELEASED
                || theType == ECalcAction.KEY_KEYPRESSED || theType == ECalcAction.KEY_KEYRELEASED
                || theType == ECalcAction.STEP_N) {
            throw new IllegalArgumentException(
                    "Invalid action for a basic action object: " + theType.name());
        }

        this.type = theType;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return this.type;
    }
}
