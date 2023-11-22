package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action representing a key being pressed.
 */
public class CalcKeyPressAction implements ICalcAction {

    /** The key group. */
    private final int group;

    /** The key bit. */
    private final int bit;

    /**
     * Constructs a new {@code CalcKeyPressAction}.
     *
     * @param theGroup the key group
     * @param theBit   the key bit
     */
    public CalcKeyPressAction(final int theGroup, final int theBit) {

        this.group = theGroup;
        this.bit = theBit;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return ECalcAction.KEY_PRESSED;
    }

    /**
     * Gets the key group.
     *
     * @return the key group
     */
    public final int getGroup() {

        return this.group;
    }

    /**
     * Gets the key bit.
     *
     * @return the key bit
     */
    public final int getBit() {

        return this.bit;
    }
}
