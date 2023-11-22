package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.ICpuCallback;

/**
 * WABBITEMU SOURCE: hardware/lcd.h, "LCDBase" struct.
 */
public class AbstractLCDBase {

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_DPE = 0x02;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_DPE_MASK = 0xFE;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_DPE_DATA = 0x01;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_86E = 0x00;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_86E_MASK = 0xFE;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_86E_DATA = 0x01;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_UDE = 0x04;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_UDE_MASK = 0xFC;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_UDE_DATA = 0x03;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SYE = 0x20;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SYE_MASK = 0xE0;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SYE_DATA = 0x1F;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SZE = 0x40;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SZE_MASK = 0xC0;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SZE_DATA = 0x3F;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SXE = 0x80;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SXE_MASK = 0xC0;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SXE_DATA = 0x3F;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SCE = 0xC0;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SCE_MASK = 0xC0;

    /** WABBITEMU SOURCE: hardware/lcd.c, "_CRD_COMMAND" enum. */
    public static final int CRD_SCE_DATA = 0x3F;

    /** Port 10 function. */
    private ICPULcdBaseCallback command;

    /** Port 11 function. */
    private ICPULcdBaseCallback data;

    /** TRUE = on, FALSE = off. */
    private boolean active;

    /** LCD cursor x. */
    private int x;

    /** LCD cursor y. */
    private int y;

    /** LCD cursor z. */
    private int z;

    /** The contrast. */
    private int contrast;

    /** Y_UP, Y_DOWN, X_UP, X_DOWN. */
    private int cursorMode;

    /** The width. */
    private int width;

    /** Display width. */
    private int displayWidth;

    /** The height. */
    private int height;

    /** User frames per second. */
    private double ufpsLast;

    /** Used to determine freq. of writes to the LCD. */
    private double writeAvg;

    /** Used to determine freq. of writes to the LCD. */
    private double writeLast;

    /** The last lcd update in seconds. */
    private double time;

    /** The Timer_c->tstate of the last write. */
    private long lastTstate;

    /** Last AVI frame. */
    private double lastAviFrame;

    /** Bytes per pixel. */
    private int bytesPerPixel;

    /**
     * Constructs a new {@code LCDBase}.
     */
    AbstractLCDBase() {

        super();

        this.command = null;
        this.data = null;
        this.active = false;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.contrast = 0;
        this.cursorMode = HardwareConstants.X_DOWN;
        this.width = 0;
        this.displayWidth = 0;
        this.height = 0;
        this.ufpsLast = 0.0;
        this.writeAvg = 0.0;
        this.writeLast = 0.0;
        this.time = 0.0;
        this.lastTstate = 0L;
        this.lastAviFrame = 0.0;
        this.bytesPerPixel = 0;
    }

    /**
     * Sets the free callback.
     *
     * @param callback the callback
     */
    final void setFreeCallback(final ICpuCallback callback) {

    }

    /**
     * Sets the reset callback.
     *
     * @param callback the callback
     */
    final void setResetCallback(final ICpuCallback callback) {

    }

    /**
     * Sets the command callback.
     *
     * @param callback the callback
     */
    final void setCommandCallback(final ICPULcdBaseCallback callback) {

        this.command = callback;
    }

    /**
     * Gets the command callback.
     *
     * @return the callback
     */
    public final ICPULcdBaseCallback getCommandCallback() {

        return this.command;
    }

    /**
     * Sets the data callback.
     *
     * @param callback the callback
     */
    final void setDataCallback(final ICPULcdBaseCallback callback) {

        this.data = callback;
    }

    /**
     * Gets the data callback.
     *
     * @return the callback
     */
    public final ICPULcdBaseCallback getDataCallback() {

        return this.data;
    }

    /**
     * Sets the active flag.
     *
     * @param isActive true if active
     */
    public final void setActive(final boolean isActive) {

        this.active = isActive;
    }

    /**
     * Tests whether the LCD is active.
     *
     * @return true if active
     */
    public final boolean isActive() {

        return this.active;
    }

    /**
     * Sets the cursor X position.
     *
     * @param theX the X position
     */
    public final void setX(final int theX) {

        this.x = theX;
    }

    /**
     * Gets the cursor X position.
     *
     * @return the X position
     */
    public final int getX() {

        return this.x;
    }

    /**
     * Sets the cursor Y position.
     *
     * @param theY the Y position
     */
    public final void setY(final int theY) {

        this.y = theY;
    }

    /**
     * Gets the cursor Y position.
     *
     * @return the Y position
     */
    public final int getY() {

        return this.y;
    }

    /**
     * Sets the cursor Z position.
     *
     * @param theZ the Z position
     */
    public final void setZ(final int theZ) {

        this.z = theZ;
    }

    /**
     * Gets the cursor Z position.
     *
     * @return the Z position
     */
    public final int getZ() {

        return this.z;
    }

    /**
     * Sets the contrast.
     *
     * @param theContrast the contrast
     */
    public final void setContrast(final int theContrast) {

        this.contrast = theContrast;
    }

    /**
     * Gets the contrast.
     *
     * @return the contrast
     */
    public final int getContrast() {

        return this.contrast;
    }

    /**
     * Sets the cursor mode.
     *
     * @param theMode the cursor mode (Y_UP, Y_DOWN, X_UP, X_DOWN)
     */
    public final void setCursorMode(final int theMode) {

        this.cursorMode = theMode;
    }

    /**
     * Gets the cursor mode.
     *
     * @return the cursor mode (Y_UP, Y_DOWN, X_UP, X_DOWN)
     */
    public final int getCursorMode() {

        return this.cursorMode;
    }

    /**
     * Sets the LCD width.
     *
     * @param theWidth the width
     */
    public final void setWidth(final int theWidth) {

        this.width = theWidth;
    }

    /**
     * Gets the LCD width.
     *
     * @return the width
     */
    public final int getWidth() {

        return this.width;
    }

    /**
     * Sets the display width.
     *
     * @param theWidth the width
     */
    final void setDisplayWidth(final int theWidth) {

        this.displayWidth = theWidth;
    }

    /**
     * Gets the display width.
     *
     * @return the width
     */
    public final int getDisplayWidth() {

        return this.displayWidth;
    }

    /**
     * Sets the LCD height.
     *
     * @param theHeight the height
     */
    final void setHeight(final int theHeight) {

        this.height = theHeight;
    }

    /**
     * Gets the LCD height.
     *
     * @return the height
     */
    public final int getHeight() {

        return this.height;
    }

    /**
     * Sets the user frames per second.
     *
     * @param theUfps the frames per second
     */
    public final void setUfps(final double theUfps) {

    }

    /**
     * Sets the last user frames per second.
     *
     * @param theUfpsLast the last frames per second
     */
    public final void setUfpsLast(final double theUfpsLast) {

        this.ufpsLast = theUfpsLast;
    }

    /**
     * Gets the last user frames per second.
     *
     * @return the last frames per second
     */
    public final double getUfpsLast() {

        return this.ufpsLast;
    }

    /**
     * Sets the average write.
     *
     * @param theWriteAvg average write
     */
    public final void setWriteAvg(final double theWriteAvg) {

        this.writeAvg = theWriteAvg;
    }

    /**
     * Gets the average write.
     *
     * @return the average write
     */
    public final double getWriteAvg() {

        return this.writeAvg;
    }

    /**
     * Sets the last write.
     *
     * @param theWriteLast last write
     */
    public final void setWriteLast(final double theWriteLast) {

        this.writeLast = theWriteLast;
    }

    /**
     * Gets the last write.
     *
     * @return the last write
     */
    public final double getWriteLast() {

        return this.writeLast;
    }

    /**
     * Sets the last LCD update in seconds.
     *
     * @param theTime the time
     */
    public final void setTime(final double theTime) {

        this.time = theTime;
    }

    /**
     * Gets the last LCD update in seconds.
     *
     * @return the time
     */
    public final double getTime() {

        return this.time;
    }

    /**
     * Sets the last write timer state.
     *
     * @param theLastTState last write timer state
     */
    public final void setLastTState(final long theLastTState) {

        this.lastTstate = theLastTState;
    }

    /**
     * Gets the last write timer state.
     *
     * @return the last write timer state
     */
    public final long getLastTState() {

        return this.lastTstate;
    }

    /**
     * Sets the last GIF frame.
     *
     * @param theLastGIFFrame the last GIF frame
     */
    public final void setLastGifFrame(final double theLastGIFFrame) {

    }

    /**
     * Sets the last AVI frame.
     *
     * @param theLastAviFrame the last AVI frame
     */
    public final void setLastAviFrame(final double theLastAviFrame) {

        this.lastAviFrame = theLastAviFrame;
    }

    /**
     * Gets the last AVI frame.
     *
     * @return the last AVI frame
     */
    public final double getLastAviFrame() {

        return this.lastAviFrame;
    }

    /**
     * Sets the LCD bytes per pixel.
     *
     * @param theBytesPerPixel the bytes per pixel
     */
    final void setBytesPerPixel(final int theBytesPerPixel) {

        this.bytesPerPixel = theBytesPerPixel;
    }

    /**
     * Gets the LCD bytes per pixel.
     *
     * @return the the bytes per pixel
     */
    public final int getBytesPerPixel() {

        return this.bytesPerPixel;
    }

    /**
     * WABBITEMU SOURCE: hardware/lcd.c, "TRUCOLOR" macro.
     *
     * @param color the color
     * @param bits  the bits
     * @return the true color
     */
    static int truColor(final int color, final int bits) {

        return color * (0xFF / ((1 << bits) - 1));
    }
}
