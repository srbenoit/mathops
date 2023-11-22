package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "USB" struct.
 */
public final class USB {

    /** Mask to get only low-order 32 bits, leaving upper bits zero. */
    private static final long UINT32_MASK = 0x0FFFFFFFFL;

    /** Whether each line is low or high. */
    private long usbLineState;

    /** Whether interrupts have occurred. */
    private long usbEvents;

    /** Whether interrupts should be generated when USB lines change. */
    private long usbEventMask;

    /** The line interrupt flag. */
    private boolean lineInterrupt;

    /** The protocol interrupt flag. */
    private boolean protocolInterrupt;

    /** The interrupt enabled flag. */
    private boolean protocolInterruptEnabled;

    /** Current USB device address. */
    private long devAddress;

    /** The version. */
    private int version;

    /** The port 4a state. */
    private int port4A;

    /** The port 4c state. */
    private int port4C;

    /** The port 54 state. */
    private int port54;

    /**
     * Constructs a new {@code USB}.
     */
    public USB() {

        // No action
    }

    /**
     * Gets the USB line state.
     *
     * @return the line state
     */
    public long getUSBLineState() {

        return this.usbLineState;
    }

    /**
     * Sets the USB line state.
     *
     * @param theUSBLineState the line state
     */
    public void setUSBLineState(final long theUSBLineState) {

        this.usbLineState = theUSBLineState & UINT32_MASK;
    }

    /**
     * Gets the USB events.
     *
     * @return the events
     */
    public long getUSBEvents() {

        return this.usbEvents;
    }

    /**
     * Sets the USB events.
     *
     * @param theUSBEvents the events
     */
    public void setUSBEvents(final long theUSBEvents) {

        this.usbEvents = theUSBEvents & UINT32_MASK;
    }

    /**
     * Gets the USB event mask.
     *
     * @return the event mask
     */
    public long getUSBEventMask() {

        return this.usbEventMask;
    }

    /**
     * Sets the USB event mask.
     *
     * @param theUSBEventMask the event mask
     */
    public void setUSBEventMask(final long theUSBEventMask) {

        this.usbEventMask = theUSBEventMask & UINT32_MASK;
    }

    /**
     * Gets the line interrupt flag.
     *
     * @return true if interrupt
     */
    public boolean isLineInterrupt() {

        return this.lineInterrupt;
    }

    /**
     * Sets the line interrupt flag.
     *
     * @param isLineInterrupt true if interrupt
     */
    public void setLineInterrupt(final boolean isLineInterrupt) {

        this.lineInterrupt = isLineInterrupt;
    }

    /**
     * Gets the protocol interrupt flag.
     *
     * @return true if interrupt
     */
    public boolean isProtocolInterrupt() {

        return this.protocolInterrupt;
    }

    /**
     * Sets the protocol interrupt flag.
     *
     * @param isProtocolInterrupt true if interrupt
     */
    public void setProtocolInterrupt(final boolean isProtocolInterrupt) {

        this.protocolInterrupt = isProtocolInterrupt;
    }

    /**
     * Gets the protocol interrupt enabled flag.
     *
     * @return true if interrupt enabled
     */
    public boolean isProtocolInterruptEnabled() {

        return this.protocolInterruptEnabled;
    }

    /**
     * Sets the protocol interrupt enabled flag.
     *
     * @param isProtocolInterruptEnabled true if interrupt enabled
     */
    public void setProtocolInterruptEnabled(final boolean isProtocolInterruptEnabled) {

        this.protocolInterruptEnabled = isProtocolInterruptEnabled;
    }

    /**
     * Gets the device address.
     *
     * @return the device address
     */
    public long getDevAddress() {

        return this.devAddress;
    }

    /**
     * Sets the device address.
     *
     * @param theDevAddress the device address
     */
    public void setDevAddress(final long theDevAddress) {

        this.devAddress = theDevAddress & UINT32_MASK;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public int getVersion() {

        return this.version;
    }

    /**
     * Sets the version.
     *
     * @param theVersion the version
     */
    public void setVersion(final int theVersion) {

        this.version = theVersion;
    }

    /**
     * Sets the USB powered flag.
     *
     * @param isUSBPowered true if powered
     */
    public void setUSBPowered(final boolean isUSBPowered) {

        // No action
    }

    /**
     * Gets the port 4A value.
     *
     * @return the value
     */
    public int getPort4A() {

        return this.port4A;
    }

    /**
     * Sets the port 4A value.
     *
     * @param thePort4a the value
     */
    public void setPort4A(final int thePort4a) {

        this.port4A = thePort4a & 0x00FF;
    }

    /**
     * Gets the port 4C value.
     *
     * @return the value
     */
    public int getPort4C() {

        return this.port4C;
    }

    /**
     * Sets the port 4C value.
     *
     * @param thePort4c the value
     */
    public void setPort4C(final int thePort4c) {

        this.port4C = thePort4c & 0x00FF;
    }

    /**
     * Gets the port 54 value.
     *
     * @return the value
     */
    public int getPort54() {

        return this.port54;
    }

    /**
     * Sets the port 54 value.
     *
     * @param thePort54 the value
     */
    public void setPort54(final int thePort54) {

        this.port54 = thePort54 & 0x00FF;
    }
}
