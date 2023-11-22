package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: utilities/sound.h, "SAMPLE" struct.
 */
public final class SAMPLE {

    /** Left channel. */
    private int left;

    /** Right channel. */
    private int right;

    /**
     * Constructs a new {@code SAMPLE}.
     */
    SAMPLE() {

        this.left = 0;
        this.right = 0;
    }

    /**
     * Gets the left channel.
     *
     * @return the left channel value
     */
    public int getLeft() {

        return this.left;
    }

    /**
     * Sets the left channel.
     *
     * @param theLeft the left channel value
     */
    public void setLeft(final int theLeft) {

        this.left = theLeft;
    }

    /**
     * Sets the right channel.
     *
     * @param theRight the right channel value
     */
    public void setRight(final int theRight) {

        this.right = theRight;
    }

    /**
     * Gets the right channel.
     *
     * @return the right channel value
     */
    public int getRight() {

        return this.right;
    }
}
