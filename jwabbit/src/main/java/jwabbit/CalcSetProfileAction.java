package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action representing a profile being set.
 */
public class CalcSetProfileAction implements ICalcAction {

    /** The name of the profile to set. */
    private final String profile;

    /**
     * Constructs a new {@code CalcSetProfileAction}.
     *
     * @param theProfile the profile to set
     */
    public CalcSetProfileAction(final String theProfile) {

        this.profile = theProfile;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return ECalcAction.SET_PROFILE;
    }

    /**
     * Gets the name of the profile to set.
     *
     * @return the profile name
     */
    public final String getProfile() {

        return this.profile;
    }
}
