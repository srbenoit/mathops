package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.WideAddr;

/**
 * WABBITEMU SOURCE: utilities/breakpoint.h, "breakpoint" struct.
 */
public final class Breakpoint {

    /** The label. */
    private String label;

    /** The type (bitwise OR of NORMAL_BREAK, MEM_WRITE_BREAK, MEM_READ_BREAK). */
    private int type;

    /** Active flag. */
    private boolean active;

    /**
     * Constructs a new {@code Breakpoint}.
     */
    public Breakpoint() {

        this.label = null;
        this.type = 0;
        this.active = false;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {

        return this.label;
    }

    /**
     * Sets the label.
     *
     * @param theLabel the label
     */
    public void setLabel(final String theLabel) {

        this.label = theLabel;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public int getType() {

        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param theType the type
     */
    public void setType(final int theType) {

        this.type = theType;
    }

    /**
     * Sets the address.
     *
     * @param theWideAddr the address
     */
    public void setWaddr(final WideAddr theWideAddr) {

        // No action
    }

    /**
     * Sets the end address.
     *
     * @param theEndAddr the end address
     */
    public void setEndAddr(final int theEndAddr) {

        // No action
    }

    /**
     * Tests whether the breakpoint is active.
     *
     * @return true if active
     */
    public boolean isActive() {

        return this.active;
    }

    /**
     * Sets the flag indicating the breakpoint is active.
     *
     * @param isActive true to make the breakpoint active
     */
    public void setActive(final boolean isActive) {

        this.active = isActive;
    }
}
