package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: utilities/breakpoint.h, "breakpoint_condition" struct.
 */
class BreakpointCondition {

    /** The breakpoint type. */
    private EnumConditionalBreakpointType type;

    /** The data. */
    private ICondition data;

    /**
     * Constructs a new {@code BreakpointCondition}.
     */
    BreakpointCondition() {

        // No action
    }

    /**
     * Gets the breakpoint type.
     *
     * @return the type
     */
    public EnumConditionalBreakpointType getType() {

        return this.type;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public ICondition getData() {

        return this.data;
    }
}
