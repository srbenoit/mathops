package jwabbit.hardware;

/**
 * Constants used by the hardware package.
 */
public enum HardwareConstants {
    ;

    /** WABBITEMU SOURCE: hardware/lcd.h, "STEADY_FREQ_MIN" macro. */
    public static final int STEADY_FREQ_MIN = 30;

    /**
     * LCD height (large enough to encompass all z80 models).
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.h, "LCD_HEIGHT" macro.
     */
    public static final int LCD_HEIGHT = 64;

    /**
     * LCD width (large enough to encompass all z80 models).
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.h, "LCD_WIDTH" macro.
     */
    public static final int LCD_WIDTH = 128;

    /** WABBITEMU SOURCE: hardware/lcd.h, "LCD_MAX_CONTRAST" macro. */
    public static final int LCD_MAX_CONTRAST = 40;

    /** WABBITEMU SOURCE: hardware/lcd.h, "LCD_MID_CONTRAST" macro. */
    public static final int LCD_MID_CONTRAST = LCD_MAX_CONTRAST / 2;

    /**
     * Maximum shades the LCD will be able to render. Actual shades rendered is stored in LCD.shades.
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.h, "LCD_MAX_SHADES" macro.
     */
    public static final int LCD_MAX_SHADES = 12;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_CURSOR_MODE " enum. */
    public static final int X_DOWN = 0;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_CURSOR_MODE " enum. */
    public static final int X_UP = 1;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_CURSOR_MODE " enum. */
    public static final int Y_DOWN = 2;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_CURSOR_MODE " enum. */
    public static final int Y_UP = 3;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_MODE" enum. */
    public static final int MODE_PERFECT_GRAY = 0;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_MODE" enum. */
    public static final int MODE_STEADY = 1;

    /** WABBITEMU SOURCE: hardware/lcd.h, "_LCD_MODE" enum. */
    public static final int MODE_GAME_GRAY = 2;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "COLOR_LCD_WIDTH" macro. */
    public static final int COLOR_LCD_WIDTH = 320;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "COLOR_LCD_HEIGHT" macro. */
    public static final int COLOR_LCD_HEIGHT = 240;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "MAX_BACKLIGHT_LEVEL" macro. */
    public static final int MAX_BACKLIGHT_LEVEL = 32;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "BACKLIGHT_OFF_DELAY" macro. */
    public static final double BACKLIGHT_OFF_DELAY = 0.002;

    /** WABBITEMU SOURCE: hardware/keys.h, "KEY_VALUE_MASK" macro. */
    public static final int KEY_VALUE_MASK = 0x0F;

    /** WABBITEMU SOURCE: hardware/keys.h, "KEYGROUP_ON" macro. */
    public static final int KEYGROUP_ON = 0x05;

    /** WABBITEMU SOURCE: hardware/keys.h, "KEYBIT_ON" macro. */
    public static final int KEYBIT_ON = 0x00;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int REAL_OBJ = 0x00;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int LIST_OBJ = 0x01;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int MAT_OBJ = 0x02;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int STRNG_OBJ = 0x04;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int PROG_OBJ = 0x05;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int PROT_PROG_OBJ = 0x06;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int PICT_PBJ = 0x07;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int GDB_OBJ = 0x08;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int CPLX_OBJ = 0x0C;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int C_LIST_OBJ = 0x0D;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int APP_VAR_OBJ = 0x15;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int GROP_OBJ = 0x17;

    /** WABBITEMU SOURCE: hardware/link.h, "TI83POBJ" enum. */
    public static final int EQU_OBJ_2 = 0x23;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "COLOR_LCD_DEPTH" macro. */
    static final int COLOR_LCD_DEPTH = 3;

    /** WABBITEMU SOURCE: hardware/colorlcd.h, "COLOR_LCD_DISPLAY_SIZE" macro. */
    public static final int COLOR_LCD_DISPLAY_SIZE = COLOR_LCD_WIDTH * COLOR_LCD_HEIGHT * COLOR_LCD_DEPTH;

    /**
     * LCD memory width.
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.h, "LCD_MEM_WIDTH" macro.
     */
    static final int LCD_MEM_WIDTH = LCD_WIDTH / 8;

    /** WABBITEMU SOURCE: hardware/lcd.h, "DISPLAY_SIZE" macro. */
    public static final int DISPLAY_SIZE = LCD_MEM_WIDTH * LCD_HEIGHT;

    /** WABBITEMU SOURCE: hardware/lcd.h, "GRAY_DISPLAY_SIZE" macro. */
    static final int GRAY_DISPLAY_SIZE = DISPLAY_SIZE * 8;

    /**
     * Default number of shades the LCD will render.
     * <p>
     * WABBITEMU SOURCE: hardware/lcd.h, "LCD_DEFAULT_SHADES" macro.
     */
    static final int LCD_DEFAULT_SHADES = 6;

    /** WABBITEMU SOURCE: hardware/keys.h, "KEY_KEYBOARDPRESS" macro. */
    static final int KEY_KEYBOARDPRESS = 0x01;
}
