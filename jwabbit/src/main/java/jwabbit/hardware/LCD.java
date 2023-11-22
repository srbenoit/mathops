package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.EnumCalcModel;
import jwabbit.core.JWCoreConstants;
import jwabbit.gui.ILcdListener;

import java.util.Arrays;

/**
 * WABBITEMU SOURCE: hardware/lcd.h, "LCD" struct.
 */
public final class LCD extends AbstractLCDBase {

    /** WABBITEMU SOURCE: hardware/lcd.c, "NORMAL_DELAY" macro. */
    private static final int NORMAL_DELAY = 60;

    /** WABBITEMU SOURCE: hardware/lcd.c, "BASE_LEVEL_83P" macro. */
    private static final int BASE_LEVEL_83P = 24;

    /** WABBITEMU SOURCE: hardware/lcd.c, "BASE_LEVEL_82" macro. */
    private static final int BASE_LEVEL_82 = 30;

    /** Listener to be notified when LCD update changes. */
    private ILcdListener listener;

    /** Word length. */
    private int wordLen;

    /** Delay in timer states required to write. */
    private int lcdDelay;

    /** Buffer previous read. */
    private int lastRead;

    /** Used in lcd level to handle contrast. */
    private int baseLevel;

    /** LCD display memory. */
    private final int[] display;

    /** Front. */
    private int front;

    /** Holds previous buffers for grey. */
    private final int[][] queue;

    /** Number of shades of grey. */
    private int shades;

    /** Mode of LCD rendering. */
    private int mode;

    /** Length of a steady frame in seconds. */
    private double steadyFrame;

    /** Memory mapped screen address. */
    private int screenAddr;

    /**
     * Constructs a new {@code LCD}.
     */
    private LCD() {

        super();

        this.listener = null;

        this.wordLen = 0;
        this.lcdDelay = 0;
        this.lastRead = 0;
        this.baseLevel = 0;
        this.display = new int[HardwareConstants.DISPLAY_SIZE];
        this.front = 0;
        this.queue = new int[HardwareConstants.LCD_MAX_SHADES][HardwareConstants.DISPLAY_SIZE];
        this.shades = 0;
        this.mode = 0;
        this.steadyFrame = 0.0;
        this.screenAddr = 0;
    }

    /**
     * Sets the listener to notify when the LCD is updated.
     *
     * @param theListener the listener
     */
    public void setListener(final ILcdListener theListener) {

        this.listener = theListener;
    }

    /**
     * Sets the word length.
     *
     * @param theWordLen the word length
     */
    public void setWordLen(final int theWordLen) {

        this.wordLen = theWordLen;
    }

    /**
     * Gets the word length.
     *
     * @return the word length
     */
    public int getWordLen() {

        return this.wordLen;
    }

    /**
     * Sets the LCD write delay.
     *
     * @param theLcdDelay theLcdDelay the LCD delay
     */
    public void setLcdDelay(final int theLcdDelay) {

        this.lcdDelay = theLcdDelay;
    }

    /**
     * Gets the LCD write delay.
     *
     * @return the the LCD delay
     */
    public int getLcdDelay() {

        return this.lcdDelay;
    }

    /**
     * Sets the last read.
     *
     * @param theLastRead the last read
     */
    public void setLastRead(final int theLastRead) {

        this.lastRead = theLastRead;
    }

    /**
     * Gets the last read.
     *
     * @return the last read
     */
    public int getLastRead() {

        return this.lastRead;
    }

    /**
     * Sets the base level.
     *
     * @param theBaseLevel the base level
     */
    public void setBaseLevel(final int theBaseLevel) {

        this.baseLevel = theBaseLevel;
    }

    /**
     * Gets the base level.
     *
     * @return the base level
     */
    public int getBaseLevel() {

        return this.baseLevel;
    }

    /**
     * Sets a value in the display array.
     *
     * @param index the index
     * @param value the value to set
     */
    public void setDisplay(final int index, final int value) {

        this.display[index] = value;
    }

    /**
     * Gets a value in the display array.
     *
     * @param index the index
     * @return the value
     */
    public int getDisplay(final int index) {

        return this.display[index];
    }

    /**
     * Gets the display array.
     *
     * @return the value
     */
    public int[] getDisplay() {

        return this.display;
    }

    /**
     * Sets the front.
     *
     * @param theFront the front
     */
    public void setFront(final int theFront) {

        this.front = theFront;
    }

    /**
     * Gets the front.
     *
     * @return the front
     */
    public int getFront() {

        return this.front;
    }

    /**
     * Sets a queue pixel.
     *
     * @param row   the row
     * @param col   the column
     * @param value the pixel
     */
    public void setQueue(final int row, final int col, final int value) {

        this.queue[row][col] = value;
    }

    /**
     * Sets the number of shades of grey.
     *
     * @param theShades the number of shades of grey
     */
    public void setShades(final int theShades) {

        this.shades = theShades;
    }

    /**
     * Gets the number of shades of grey.
     *
     * @return the number of shades of grey
     */
    public int getShades() {

        return this.shades;
    }

    /**
     * Sets the LCD rendering mode.
     *
     * @param theMode the mode
     */
    public void setMode(final int theMode) {

        this.mode = theMode;
    }

    /**
     * Gets the LCD rendering mode.
     *
     * @return the mode
     */
    public int getMode() {

        return this.mode;
    }

    /**
     * Sets the length of a steady frame in seconds.
     *
     * @param theSteadyFrame the length
     */
    public void setSteadyFrame(final double theSteadyFrame) {

        this.steadyFrame = theSteadyFrame;
    }

    /**
     * Gets the length of a steady frame in seconds.
     *
     * @return the length
     */
    public double getSteadyFrame() {

        return this.steadyFrame;
    }

    /**
     * Sets the LCD screen address.
     *
     * @param theScreenAddr the screen address
     */
    public void setScreenAddr(final int theScreenAddr) {

        this.screenAddr = theScreenAddr;
    }

    /**
     * Gets the LCD screen address.
     *
     * @return the screen address
     */
    public int getScreenAddr() {

        return this.screenAddr;
    }

    /**
     * WABBITEMU SOURCE: hardware/lcd.c, "LCD_OFFSET" macro.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the offset
     */
    public static int lcdOffset(final int x, final int y, final int z) {

        return (((y + z) % HardwareConstants.LCD_HEIGHT) * HardwareConstants.LCD_MEM_WIDTH)
                + (x % HardwareConstants.LCD_MEM_WIDTH);
    }

    /**
     * WABBITEMU SOURCE: hardware/lcd.c, "set_model_baselevel" function.
     *
     * @param model the model
     */
    public void setModelBaselevel(final EnumCalcModel model) {

        switch (model) {
            case TI_82:
                this.baseLevel = BASE_LEVEL_82;
                break;

            case TI_83:
            case TI_73:
            case TI_83P:
            case TI_83PSE:
            case TI_84P:
            case TI_84PSE:
                this.baseLevel = BASE_LEVEL_83P;
                break;

            case INVALID_MODEL:
            case TI_81:
            case TI_84PCSE:
            case TI_85:
            case TI_86:
            default:
                this.baseLevel = 0;
                break;
        }
    }

    /**
     * Constructs and initializes an LCD for a given CPU.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.c, "LCD_init" function.
     *
     * @param cpu   the CPU
     * @param model the model
     * @return the constructed LCD
     */
    static LCD lcdInit(final CPU cpu, final EnumCalcModel model) {

        final LCD lcd = new LCD();

        lcd.setFreeCallback(new LCDFree());
        lcd.setResetCallback(new LCDReset());

        // NOTE: command and data callbacks are set on device initialization

        lcd.setBytesPerPixel(1);

        lcd.setModelBaselevel(model);
        lcd.setHeight(64);
        lcd.setWidth(128);

        if (model == EnumCalcModel.TI_86 || model == EnumCalcModel.TI_85) {
            lcd.setDisplayWidth(128);
        } else {
            lcd.setDisplayWidth(96);
        }

        lcd.shades = HardwareConstants.LCD_DEFAULT_SHADES;
        lcd.mode = HardwareConstants.MODE_PERFECT_GRAY;
        lcd.steadyFrame = 1.0 / (double) JWCoreConstants.FPS;
        lcd.lcdDelay = NORMAL_DELAY;

        if (lcd.shades > HardwareConstants.LCD_MAX_SHADES) {
            lcd.shades = HardwareConstants.LCD_MAX_SHADES;
        } else if (lcd.shades == 0) {
            lcd.shades = HardwareConstants.LCD_DEFAULT_SHADES;
        }

        lcd.setTime(cpu.getTimerContext().getElapsed());
        lcd.setUfpsLast(cpu.getTimerContext().getElapsed());
        lcd.setUfps(0.0);
        lcd.setLastGifFrame(cpu.getTimerContext().getElapsed());
        lcd.setLastAviFrame(cpu.getTimerContext().getElapsed());
        lcd.setWriteAvg(0.0);
        lcd.setWriteLast(cpu.getTimerContext().getElapsed());

        return lcd;
    }

    /**
     * WABBITEMU SOURCE: hardware/lcd.c, "LCD_advance_cursor" function.
     */
    public void advanceCursor() {

        final int oldX = getX();
        final int oldY = getY();

        switch (getCursorMode()) {
            case HardwareConstants.X_UP:
                setX((oldX + 1) % HardwareConstants.LCD_HEIGHT);
                break;

            case HardwareConstants.X_DOWN:
                setX((oldX + HardwareConstants.LCD_HEIGHT - 1) % HardwareConstants.LCD_HEIGHT);
                break;

            case HardwareConstants.Y_UP:
                setY(oldY + 1);
                final int bound = this.wordLen != 0 ? 15 : 19;
                if (getY() >= bound) {
                    setY(0);
                }
                break;

            case HardwareConstants.Y_DOWN:
                if (oldY <= 0) {
                    setY(this.wordLen != 0 ? 14 : 18);
                } else {
                    setY(oldY - 1);
                }
                break;

            default:
                break;
        }
    }

    /**
     * Add a black and white LCD image to the LCD grayscale queue.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.c, "LCD_enqueue" function.
     *
     * @param cpu the CPU
     */
    public void enqueue(final CPU cpu) {

        if (this.front == 0) {
            this.front = this.shades;
        }
        this.front = this.front - 1;

        for (int i = 0; i < HardwareConstants.LCD_HEIGHT; ++i) {
            for (int j = 0; j < HardwareConstants.LCD_MEM_WIDTH; ++j) {
                this.queue[this.front][lcdOffset(j, i, HardwareConstants.LCD_HEIGHT - getZ())] =
                        this.display[lcdOffset(j, i, 0)];
            }
        }

        fireUpdate();
    }

    /**
     * Fires the update notification to the update listener, if one is registered.
     */
    public void fireUpdate() {

        if (this.listener != null) {
            this.listener.updateLcd(isActive(), getContrast(), updateImage());
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/lcd.c, "LCD_image" and "LCD_update_image" functions.
     *
     * @return the image
     */
    public int[] updateImage() {

        final int[] screen = new int[HardwareConstants.GRAY_DISPLAY_SIZE];

        if (!this.isActive()) {
            return screen;
        }

        int bits = 0;
        int n = this.shades;
        while (n > 0) {
            ++bits;
            n >>= 1;
        }

        int alpha;
        int contrastColor = 0x00FF;
        if (getContrast() < HardwareConstants.LCD_MID_CONTRAST) {
            alpha = 98 - ((getContrast() % HardwareConstants.LCD_MID_CONTRAST) * 100
                    / HardwareConstants.LCD_MID_CONTRAST);
            contrastColor = 0x00;
        } else {
            alpha = getContrast() % HardwareConstants.LCD_MID_CONTRAST;
            alpha = alpha * alpha / 3;
            if (alpha > 100) {
                alpha = 100;
            }
        }

        final int alphaOverlay = alpha * contrastColor / 100;
        final int inverseAlpha = 100 - alpha;

        for (int row = 0; row < HardwareConstants.LCD_HEIGHT; ++row) {
            for (int col = 0; col < HardwareConstants.LCD_MEM_WIDTH; ++col) {
                int p0 = 0;
                int p1 = 0;
                int p2 = 0;
                int p3 = 0;
                int p4 = 0;
                int p5 = 0;
                int p6 = 0;
                int p7 = 0;

                for (int i = 0; i < this.shades; ++i) {
                    int u = this.queue[i][row * HardwareConstants.LCD_MEM_WIDTH + col];
                    p7 += u & 1;
                    u >>= 1;
                    p6 += u & 1;
                    u >>= 1;
                    p5 += u & 1;
                    u >>= 1;
                    p4 += u & 1;
                    u >>= 1;
                    p3 += u & 1;
                    u >>= 1;
                    p2 += u & 1;
                    u >>= 1;
                    p1 += u & 1;
                    u >>= 1;
                    p0 += u & 1;
                }

                final int index = row * HardwareConstants.LCD_WIDTH + (col << 3);

                screen[index] = (alphaOverlay + truColor(p0, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 1] = (alphaOverlay + truColor(p1, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 2] = (alphaOverlay + truColor(p2, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 3] = (alphaOverlay + truColor(p3, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 4] = (alphaOverlay + truColor(p4, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 5] = (alphaOverlay + truColor(p5, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 6] = (alphaOverlay + truColor(p6, bits) * inverseAlpha / 100) & 0x00FF;
                screen[index + 7] = (alphaOverlay + truColor(p7, bits) * inverseAlpha / 100) & 0x00FF;
            }
        }

        return screen;
    }

    /**
     * Simulates the state of the LCD after a power reset.
     *
     * <p>
     * SOURCE: hardware/lcd.c, "LCD_reset" function.
     */
    public void reset() {

        setActive(false);
        this.wordLen = 8;
        setCursorMode(HardwareConstants.Y_UP);
        setX(0);
        setY(0);
        setZ(0);
        setContrast(32);
        this.lastRead = 0;
        this.front = 0;

        synchronized (this) {
            Arrays.fill(this.display, 0);
            for (final int[] ints : this.queue) {
                Arrays.fill(ints, 0);
            }
            fireUpdate();
        }
    }
}
