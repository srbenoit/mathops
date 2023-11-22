package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.log.LoggedObject;

/**
 * WABBITEMU SOURCE: utilities/var.h, "ROM" struct.
 */
public final class ROM {

    /** The 4-byte size. */
    private int size;

    /** The 32-byte version. */
    private String version = "";

    /** The data. */
    private int[] data;

    /**
     * Constructs a new {@code ROM}.
     */
    public ROM() {

        super();
    }

    /**
     * Gets the size.
     *
     * @return the size
     */
    public int getSize() {

        return this.size;
    }

    /**
     * Sets the size.
     *
     * @param theSize the size
     */
    public void setSize(final int theSize) {

        if (theSize < 0) {
            LoggedObject.LOG.warning("Invalid size in ROM: " + theSize, new IllegalArgumentException());
        }

        this.size = theSize;
    }

    /**
     * Gets the data array.
     *
     * @return the data array
     */
    public int[] getData() {

        return this.data;
    }

    /**
     * Sets the data array.
     *
     * @param theData the data array
     */
    public void setData(final int[] theData) {

        this.data = theData;
    }

    /**
     * Gets the version string.
     *
     * @return the version string
     */
    public String getVersion() {

        return this.version;
    }

    /**
     * Sets the version string.
     *
     * @param theVersion the version string
     */
    public void setVersion(final String theVersion) {

        if (theVersion.length() > 32) {
            LoggedObject.LOG.warning("Invalid version string in ROM: ", theVersion, new IllegalArgumentException());
        }

        this.version = theVersion;
    }
}
