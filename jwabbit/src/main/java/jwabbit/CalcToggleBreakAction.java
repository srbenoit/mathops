package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.WideAddr;

/**
 * An action representing toggling of a breakpoint.
 */
public final class CalcToggleBreakAction implements ICalcAction {

    /** The address. */
    private final WideAddr addr;

    /** The breakpoint type. */
    private final int type;

    /**
     * Constructs a new {@code CalcToggleBreakAction}.
     *
     * @param theAddr the address
     * @param theType the breakpoint type
     */
    public CalcToggleBreakAction(final WideAddr theAddr, final int theType) {

        this.addr = theAddr;
        this.type = theType;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public ECalcAction getType() {

        return ECalcAction.BREAKPOINT_TOGGLE;
    }

    /**
     * Gets the address.
     *
     * @return the address
     */
    public WideAddr getAddress() {

        return this.addr;
    }

    /**
     * Gets the breakpoint type.
     *
     * @return the type
     */
    int getBreakpointType() {

        return this.type;
    }
}
