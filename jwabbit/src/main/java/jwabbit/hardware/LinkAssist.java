package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "LINKASSIST" struct.
 */
public final class LinkAssist {

    /** Link enable. */
    private int linkEnable;

    /** Link in. */
    private int in;

    /** Link out. */
    private int out;

    /** Working. */
    private int working;

    /** Receiving flag. */
    private boolean receiving;

    /** Read flag. */
    private boolean read;

    /** Ready flag. */
    private boolean ready;

    /** Error flag. */
    private boolean error;

    /** Sending flag. */
    private boolean sending;

    /** Last access time. */
    private double lastAccess;

    /** Bit. */
    private int bit;

    /**
     * Constructs a new {@code LinkAssist}.
     */
    public LinkAssist() {

        // No action
    }

    /**
     * Gets the link enable.
     *
     * @return the link enable
     */
    public int getLinkEnable() {

        return this.linkEnable;
    }

    /**
     * Sets the link enable.
     *
     * @param theLinkEnable the link enable
     */
    public void setLinkEnable(final int theLinkEnable) {

        this.linkEnable = theLinkEnable & 0x00FF;
    }

    /**
     * Gets the in value.
     *
     * @return the in value
     */
    public int getIn() {

        return this.in;
    }

    /**
     * Sets the in value.
     *
     * @param theIn the in value
     */
    public void setIn(final int theIn) {

        this.in = theIn & 0x00FF;
    }

    /**
     * Gets the out value.
     *
     * @return the out value
     */
    public int getOut() {

        return this.out;
    }

    /**
     * Sets the out value.
     *
     * @param theOut the out value
     */
    public void setOut(final int theOut) {

        this.out = theOut & 0x00FF;
    }

    /**
     * Gets the working value.
     *
     * @return the working value
     */
    public int getWorking() {

        return this.working;
    }

    /**
     * Sets the working value.
     *
     * @param theWorking the working value
     */
    public void setWorking(final int theWorking) {

        this.working = theWorking & 0x00FF;
    }

    /**
     * Gets the receiving flag.
     *
     * @return true if receiving
     */
    public boolean isReceiving() {

        return this.receiving;
    }

    /**
     * Sets the receiving flag.
     *
     * @param isReceiving true if receiving
     */
    public void setReceiving(final boolean isReceiving) {

        this.receiving = isReceiving;
    }

    /**
     * Gets the read flag.
     *
     * @return true if read
     */
    public boolean isRead() {

        return this.read;
    }

    /**
     * Sets the read flag.
     *
     * @param isRead true if read
     */
    public void setRead(final boolean isRead) {

        this.read = isRead;
    }

    /**
     * Gets the ready flag.
     *
     * @return true if ready
     */
    public boolean isReady() {

        return this.ready;
    }

    /**
     * Sets the ready flag.
     *
     * @param isReady true if ready
     */
    public void setReady(final boolean isReady) {

        this.ready = isReady;
    }

    /**
     * Gets the error flag.
     *
     * @return true if error
     */
    public boolean isError() {

        return this.error;
    }

    /**
     * Sets the error flag.
     *
     * @param isError true if error
     */
    public void setError(final boolean isError) {

        this.error = isError;
    }

    /**
     * Gets the sending flag.
     *
     * @return true if sending
     */
    public boolean isSending() {

        return this.sending;
    }

    /**
     * Sets the sending flag.
     *
     * @param isSending true if sending
     */
    public void setSending(final boolean isSending) {

        this.sending = isSending;
    }

    /**
     * Gets the last access.
     *
     * @return the last access
     */
    public double getLastAccess() {

        return this.lastAccess;
    }

    /**
     * Sets the last access.
     *
     * @param theLastAccess the last access
     */
    public void setLastAccess(final double theLastAccess) {

        this.lastAccess = theLastAccess;
    }

    /**
     * Gets the bit.
     *
     * @return the bit
     */
    public int getBit() {

        return this.bit;
    }

    /**
     * Sets the bit.
     *
     * @param theBit the bit
     */
    public void setBit(final int theBit) {

        this.bit = theBit;
    }
}
