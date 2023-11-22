package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;

/**
 * A listener that is notified when it may retrieve calculator state.
 */
public interface ICalcStateListener {

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    void calcState(Calc theCalc);

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    void enableControls(boolean enable);
}
