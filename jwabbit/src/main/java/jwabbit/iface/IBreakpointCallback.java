package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A breakpoint callback.
 */
@FunctionalInterface
public interface IBreakpointCallback {

    /**
     * Execute the callback.
     *
     * @param calc the calculator
     */
    void exec(Calc calc);
}
