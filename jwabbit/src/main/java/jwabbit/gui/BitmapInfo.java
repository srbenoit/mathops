package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.awt.Color;

/**
 * An analog of the Windows "BITMAPINFO" structure.
 */
final class BitmapInfo {

    /** Bitmap width, in pixels. */
    private int width;

    /** Bitmap height, in pixels. */
    private int height;

    /** The number of bits per pixel. */
    private int bitCount;

    /** Colors. */
    private final Color[] colors;

    /**
     * Constructs a new {@code BitmapInfo}.
     *
     * @param numColors the number of colors
     */
    BitmapInfo(final int numColors) {

        this.colors = new Color[numColors];
    }

    /**
     * Gets the image width.
     *
     * @return the image width
     */
    public int getWidth() {

        return this.width;
    }

    /**
     * Sets the image width.
     *
     * @param theWidth the image width
     */
    public void setWidth(final int theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the image height.
     *
     * @return the image height
     */
    public int getHeight() {

        return this.height;
    }

    /**
     * Sets the image height.
     *
     * @param theHeight the image height
     */
    public void setHeight(final int theHeight) {

        this.height = theHeight;
    }

    /**
     * Gets the bits per pixel.
     *
     * @return the bits per pixel
     */
    int getBitCount() {

        return this.bitCount;
    }

    /**
     * Sets the bits per pixel.
     *
     * @param theBitCount the bits per pixel
     */
    void setBitCount(final int theBitCount) {

        this.bitCount = theBitCount;
    }

    /**
     * Sets the number of colors used.
     *
     * @param theClrUsed the number of colors
     */
    void setClrUsed(final int theClrUsed) {

        // No action
    }

    /**
     * Sets the number of "important" colors.
     *
     * @param theClrImportant the number of colors
     */
    void setClrImportant(final int theClrImportant) {

        // No action
    }

    /**
     * Gets a color.
     *
     * @param index the index
     * @return the color
     */
    Color getColor(final int index) {

        return this.colors[index];
    }

    /**
     * Sets a color.
     *
     * @param index    the index
     * @param theColor the color
     */
    void setColor(final int index, final Color theColor) {

        this.colors[index] = theColor;
    }
}
