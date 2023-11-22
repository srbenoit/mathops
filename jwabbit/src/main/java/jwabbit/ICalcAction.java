package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * An action that can be queued to a calculator. Implementing classes may contain data associated with the action.
 */
@FunctionalInterface
public interface ICalcAction {

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    ECalcAction getType();
}
