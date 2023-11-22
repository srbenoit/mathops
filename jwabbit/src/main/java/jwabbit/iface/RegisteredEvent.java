package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.gui.CalcUI;

/**
 * SOURCE: interface/core.h, "registered_event" struct.
 */
final class RegisteredEvent {

    /** The event type. */
    private EnumEventType type;

    /** The event callback. */
    private IEventCallback callback;

    /** The calculator UI. */
    private CalcUI calcUI;

    /**
     * Constructs a new {@code RegisteredEvent}.
     */
    RegisteredEvent() {

        this.type = null;
        this.callback = null;
        this.calcUI = null;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public EnumEventType getType() {

        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param theType the type
     */
    public void setType(final EnumEventType theType) {

        this.type = theType;
    }

    /**
     * Gets the callback.
     *
     * @return the callback
     */
    public IEventCallback getCallback() {

        return this.callback;
    }

    /**
     * Sets the callback.
     *
     * @param theCallback the callback
     */
    public void setCallback(final IEventCallback theCallback) {

        this.callback = theCallback;
    }

    /**
     * Gets the calculator UI.
     *
     * @return the calculator UI
     */
    CalcUI getCalcUI() {

        return this.calcUI;
    }

    /**
     * Sets the calculator UI.
     *
     * @param theCalcUI the calculator UI
     */
    void setCalcUI(final CalcUI theCalcUI) {

        this.calcUI = theCalcUI;
    }
}
