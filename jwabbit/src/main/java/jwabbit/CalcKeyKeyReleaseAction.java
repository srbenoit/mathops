package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action representing a key being released.
 */
public class CalcKeyKeyReleaseAction implements ICalcAction {

    /** The virtual key code. */
    private final int vk;

    /** The key location. */
    private final int loc;

    /**
     * Constructs a new {@code CalcKeyKeyReleaseAction}.
     *
     * @param theVk  the virtual key code
     * @param theLoc the key location
     */
    public CalcKeyKeyReleaseAction(final int theVk, final int theLoc) {

        this.vk = theVk;
        this.loc = theLoc;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return ECalcAction.KEY_KEYRELEASED;
    }

    /**
     * Gets the virtual key code.
     *
     * @return the key code
     */
    final int getVk() {

        return this.vk;
    }

    /**
     * Gets the key location.
     *
     * @return the key location
     */
    final int getLoc() {

        return this.loc;
    }
}
