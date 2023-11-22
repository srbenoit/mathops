package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.gui.ILcdListener;

import java.util.Arrays;

/**
 * WABBITEMU SOURCE: hardware/colorlcd.h, "ColorLCD" struct.
 */
public final class ColorLCD extends AbstractLCDBase {

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "REAL_LCD" macro. */
    public static final boolean REAL_LCD = false;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    public static final int ENTRY_MODE_REG = 0x03;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    public static final int GRAM_REG = 0x22;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "EIGHTEEN_BIT_MASK" macro. */
    public static final int EIGHTEEN_BIT_MASK = 1 << 14;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DRIVER_CODE_REG = 0x00;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DRIVER_OUTPUT_CONTROL1_REG = 0x01;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DRIVER_OUTPUT_CONTROL1_MASK = 0x0500;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int ENTRY_MODE_MASK = 0xD0B8;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DATA_FORMAT_16BIT_REG = 0x05;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DATA_FORMAT_16BIT_MASK = 0x0003;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL1_REG = 0x07;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL1_MASK = 0x313B;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL2_REG = 0x08;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL2_MASK = 0xFFFF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL3_REG = 0x09;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL3_MASK = 0x073F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL4_REG = 0x0A;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DISPLAY_CONTROL4_MASK = 0x000F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int RGB_DISPLAY_INTERFACE_CONTROL1_REG = 0x0C;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int RGB_DISPLAY_INTERFACE_CONTROL1_MASK = 0x7133;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int FRAME_MARKER_REG = 0x0D;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int FRAME_MARKER_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int RGB_DISPLAY_INTERFACE_CONTROL2_REG = 0x0F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int RGB_DISPLAY_INTERFACE_CONTROL2_MASK = 0x001B;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL1_REG = 0x10;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL1_MASK = 0x17F3;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL2_REG = 0x11;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL2_MASK = 0x0777;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL3_REG = 0x12;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL3_MASK = 0x008F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL4_REG = 0x13;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL4_MASK = 0x1F00;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int CUR_Y_REG = 0x20;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int CUR_Y_MASK = 0x00FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int CUR_X_REG = 0x21;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int CUR_X_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL7_REG = 0x29;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int POWER_CONTROL7_MASK = 0x003F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int FRAME_RATE_COLOR_CONTROL_REG = 0x2B;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int FRAME_RATE_COLOR_CONTROL_MASK = 0x000F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL1_REG = 0x30;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL1_MASK = 0x0707;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL2_REG = 0x31;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL3_REG = 0x32;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL4_REG = 0x35;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL5_REG = 0x36;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL5_MASK = 0x1F0F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL6_REG = 0x37;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL7_REG = 0x38;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL8_REG = 0x39;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL9_REG = 0x3C;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GAMMA_CONTROL10_REG = 0x3D;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_HORZ_START_REG = 0x50;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_HORZ_START_MASK = 0x00FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_HORZ_END_REG = 0x51;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_HORZ_END_MASK = 0x00FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_VERT_START_REG = 0x52;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_VERT_START_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_VERT_END_REG = 0x53;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int WINDOW_VERT_END_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GATE_SCAN_CONTROL_REG = 0x60;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int GATE_SCAN_CONTROL_MASK = 0xBF3F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int BASE_IMAGE_DISPLAY_CONTROL_REG = 0x61;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int BASE_IMAGE_DISPLAY_CONTROL_MASK = 0x0007;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int VERTICAL_SCROLL_CONTROL_REG = 0x6A;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int VERTICAL_SCROLL_CONTROL_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_DISPLAY_POSITION_REG = 0x80;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_DISPLAY_POSITION_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_START_LINE_REG = 0x81;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_START_LINE_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_END_LINE_REG = 0x82;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE1_END_LINE_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_DISPLAY_POSITION_REG = 0x83;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_DISPLAY_POSITION_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_START_LINE_REG = 0x84;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_START_LINE_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_END_LINE_REG = 0x85;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PARTIAL_IMAGE2_END_LINE_MASK = 0x01FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL1_REG = 0x90;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL1_MASK = 0x031F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL2_REG = 0x92;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL2_MASK = 0x0700;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL4_REG = 0x95;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL4_MASK = 0x0300;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL5_REG = 0x97;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int PANEL_INTERFACE_CONTROL5_MASK = 0x0F00;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_VCM_PROGRAMMING_CONTROL_REG = 0xA1;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_VCM_PROGRAMMING_CONTROL_MASK = 0x083F;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_VCM_STATUS_AND_ENABLE_REG = 0xA2;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_VCM_STATUS_AND_ENABLE_MASK = 0xFF01;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_PROGRAMMING_ID_KEY_REG = 0xA5;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int OTP_PROGRAMMING_ID_KEY_MASK = 0xFFFF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DEEP_STAND_BY_MODE_CONTROL_REG = 0xE6;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR_LCD_COMMAND" enum. */
    private static final int DEEP_STAND_BY_MODE_CONTROL_MASK = 0x0001;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "DRIVER_CODE_VER" macro. */
    private static final int DRIVER_CODE_VER = 0x9335;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "FLIP_COLS_MASK" macro. */
    private static final int FLIP_COLS_MASK = 1 << 8;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "INTERLACED_MASK" macro. */
    private static final int INTERLACED_MASK = 1 << 10;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "CUR_DIR_MASK" macro. */
    private static final int CUR_DIR_MASK = 1 << 3;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "ROW_INC_MASK" macro. */
    private static final int ROW_INC_MASK = 1 << 4;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COL_INC_MASK" macro. */
    private static final int COL_INC_MASK = 1 << 5;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "ORG_MASK" macro. */
    private static final int ORG_MASK = 1 << 7;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "BGR_MASK" macro. */
    private static final int BGR_MASK = 1 << 12;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "UNPACKED_MASK" macro. */
    private static final int UNPACKED_MASK = 1 << 15;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "TRI_MASK" macro. */
    public static final int TRI_MASK = EIGHTEEN_BIT_MASK | UNPACKED_MASK;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "DISPLAY_ON_MASK" macro. */
    private static final int DISPLAY_ON_MASK = 1 | (1 << 1);

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "COLOR8_MASK" macro. */
    private static final int COLOR8_MASK = 1 << 3;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "BASEE_MASK" macro. */
    private static final int BASEE_MASK = 1 << 8;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "SHOW_PARTIAL1_MASK" macro. */
    private static final int SHOW_PARTIAL1_MASK = 1 << 12;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "SHOW_PARTIAL2_MASK" macro. */
    private static final int SHOW_PARTIAL2_MASK = 1 << 13;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "FRAME_RATE_MASK" macro. */
    private static final int FRAME_RATE_MASK = 15;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "BASE_START_MASK" macro. */
    private static final int BASE_START_MASK = 63;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "BASE_NLINES_MASK" macro. */
    private static final int BASE_NLINES_MASK = 16128;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "GATE_SCAN_DIR_MASK" macro. */
    private static final int GATE_SCAN_DIR_MASK = 1 << 15;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "LEVEL_INVERT_MASK" macro. */
    private static final int LEVEL_INVERT_MASK = 1;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "SCROLL_ENABLED_MASK" macro. */
    private static final int SCROLL_ENABLED_MASK = 1 << 1;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "NDL_MASK" macro. */
    private static final int NDL_MASK = 1 << 2;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P1_POS_MASK" macro. */
    private static final int P1_POS_MASK = 0x1FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P1_START_MASK" macro. */
    private static final int P1_START_MASK = 0x1FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P1_END_MASK" macro. */
    private static final int P1_END_MASK = 0x1FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P2_POS_MASK" macro. */
    private static final int P2_POS_MASK = 0x1FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P2_START_MASK" macro. */
    private static final int P2_START_MASK = 0x1FF;

    /** WABBITEMU SOURCE: hardware/colorlcd.c, "P2_END_MASK" macro. */
    private static final int P2_END_MASK = 0x1FF;

    /** The current register. */
    private int currentRegister;

    /** A uint_8 array. */
    private final int[] display;

    /** A uint_8 array. */
    private final int[] queuedImage;

    /** A uint_16 array. */
    private final int[] registers;

    /** Register breakpoints. */
    private final boolean[] registerBreakpoint;

    /** Front. */
    private int front;

    /** Last draw. */
    private double lastDraw;

    /** Draw gate. */
    private int drawGate;

    /** Line time. */
    private double lineTime;

    /** Drawing. */
    private boolean drawing;

    /** Panic mode. */
    private boolean panicMode;

    /** Read buffer. */
    private int readBuffer;

    /** Write buffer. */
    private int writeBuffer;

    /** Read step. */
    private int readStep;

    /** Write step. */
    private int writeStep;

    /** Frame rate. */
    private int frameRate;

    /** Front porch. */
    private int frontPorch;

    /** Back porch. */
    private int backPorch;

    /** Display lines. */
    private int displayLines;

    /** Clocks per line. */
    private int clocksPerLine;

    /** Clock divider. */
    private int clockDivider;

    /** Backlight active flag. */
    private boolean backlightActive;

    /** Backlight off elapsed time. */
    private double backlightOffElapsed;

    /** Listener to be notified when LCD update changes. */
    private ILcdListener listener;

    /**
     * Constructs a new {@code ColorLCD}.
     */
    private ColorLCD() {

        super();

        this.display = new int[HardwareConstants.COLOR_LCD_DISPLAY_SIZE];
        this.queuedImage = new int[HardwareConstants.COLOR_LCD_DISPLAY_SIZE];
        this.registers = new int[0xFF];
        this.registerBreakpoint = new boolean[0xFF];

        this.listener = null;

        reset();
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
     * Resets a {@code ColorLCD}.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_LCDreset" function.
     */
    public void reset() {

        // Simulate a "memset(0)"
        setActive(false);
        setX(0);
        setY(0);
        setZ(0);
        setContrast(0);
        setCursorMode(HardwareConstants.X_DOWN);
        setWidth(0);
        setDisplayWidth(0);
        setHeight(0);
        setUfps(0.0);
        setUfpsLast(0.0);
        setWriteAvg(0.0);
        setWriteLast(0.0);
        setTime(0.0);
        setLastTState(0L);
        setLastGifFrame(0.0);
        setLastAviFrame(0.0);
        setBytesPerPixel(0);

        this.currentRegister = 0;
        Arrays.fill(this.display, 0);
        Arrays.fill(this.queuedImage, 0);
        Arrays.fill(this.registers, 0);
        Arrays.fill(this.registerBreakpoint, false);
        this.front = 0;
        this.lastDraw = 0.0;
        this.drawGate = 0;
        this.lineTime = 0.0;
        this.drawing = false;
        this.panicMode = false;
        this.readBuffer = 0;
        this.writeBuffer = 0;
        this.readStep = 0;
        this.writeStep = 0;
        this.frameRate = 0;
        this.frontPorch = 0;
        this.backPorch = 0;
        this.clocksPerLine = 0;
        this.clockDivider = 0;
        this.backlightActive = false;
        this.backlightOffElapsed = 0.0;

        // now set the state to the "reset" conditions
        setFreeCallback(new ColorLCDFree());
        setResetCallback(new ColorLCDReset());
        // setImageCallback(new ColorLCDImage());

        // NOTE: command and data callbacks are set on device initialization

        setWidth(HardwareConstants.COLOR_LCD_WIDTH);
        setDisplayWidth(HardwareConstants.COLOR_LCD_WIDTH);
        setHeight(HardwareConstants.COLOR_LCD_HEIGHT);

        this.displayLines = HardwareConstants.COLOR_LCD_WIDTH;
        this.frameRate = 69;
        this.backPorch = 2;
        this.frontPorch = 2;
        this.clocksPerLine = 16;
        this.clockDivider = 1;
        this.backlightActive = true;

        setLineTime();

        this.registers[DRIVER_CODE_REG] = DRIVER_CODE_VER;
        this.registers[DISPLAY_CONTROL2_REG] = 0x0202;
        this.registers[FRAME_RATE_COLOR_CONTROL_REG] = 0x000B;
        this.registers[GATE_SCAN_CONTROL_REG] = 0x2700;
        this.registers[PANEL_INTERFACE_CONTROL1_REG] = 0x0010;
        this.registers[PANEL_INTERFACE_CONTROL2_REG] = 0x0600;
        this.registers[PANEL_INTERFACE_CONTROL4_REG] = 0x0600;
        this.registers[PANEL_INTERFACE_CONTROL5_REG] = 0x0C00;

        setBytesPerPixel(3);
    }

    /**
     * Sets the current register.
     *
     * @param theCurrentRegister the current register
     */
    public void setCurrentRegister(final int theCurrentRegister) {

        this.currentRegister = theCurrentRegister;
    }

    /**
     * Gets the current register.
     *
     * @return the current register
     */
    public int getCurrentRegister() {

        return this.currentRegister;
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
     * Gets the queued image array.
     *
     * @return the queued image array
     */
    public int[] getQueuedImage() {

        return this.queuedImage.clone();
    }

    /**
     * Gets the register values array.
     *
     * @return the byte value array
     */
    public int[] getRegisters() {

        return this.registers;
    }

    /**
     * Gets a register breakpoint flag.
     *
     * @param reg the register
     * @return true if breakpoint
     */
    public boolean isRegisterBreakpoint(final int reg) {

        return this.registerBreakpoint[reg];
    }

    /**
     * Sets the front value.
     *
     * @param value the value
     */
    public void setFront(final int value) {

        this.front = value;
    }

    /**
     * Gets the front value.
     *
     * @return the value
     */
    public int getFront() {

        return this.front;
    }

    /**
     * Gets the last draw time.
     *
     * @return the last draw time
     */
    public double getLastDraw() {

        return this.lastDraw;
    }

    /**
     * Gets the line time.
     *
     * @return the line time
     */
    public double getLineTime() {

        return this.lineTime;
    }

    /**
     * Sets the drawing flag.
     *
     * @param isDrawing true if drawing
     */
    public void setDrawing(final boolean isDrawing) {

        this.drawing = isDrawing;
    }

    /**
     * Gets the drawing flag.
     *
     * @return true if drawing
     */
    public boolean isDrawing() {

        return this.drawing;
    }

    /**
     * Sets the read buffer value.
     *
     * @param theReadBuffer the value
     */
    public void setReadBuffer(final int theReadBuffer) {

        this.readBuffer = theReadBuffer;
    }

    /**
     * Gets the read buffer value.
     *
     * @return the value
     */
    public int getReadBuffer() {

        return this.readBuffer;
    }

    /**
     * Sets the write buffer value.
     *
     * @param theWriteBuffer the value
     */
    public void setWriteBuffer(final int theWriteBuffer) {

        this.writeBuffer = theWriteBuffer;
    }

    /**
     * Gets the write buffer value.
     *
     * @return the value
     */
    public int getWriteBuffer() {

        return this.writeBuffer;
    }

    /**
     * Sets the read step value.
     *
     * @param theReadStep the value
     */
    public void setReadStep(final int theReadStep) {

        this.readStep = theReadStep;
    }

    /**
     * Gets the read step value.
     *
     * @return the value
     */
    public int getReadStep() {

        return this.readStep;
    }

    /**
     * Sets the write step value.
     *
     * @param theWriteStep the value
     */
    public void setWriteStep(final int theWriteStep) {

        this.writeStep = theWriteStep;
    }

    /**
     * Gets the write step value.
     *
     * @return the value
     */
    public int getWriteStep() {

        return this.writeStep;
    }

    /**
     * Sets the frame rate value.
     *
     * @param theFrameRate the value
     */
    public void setFrameRate(final int theFrameRate) {

        this.frameRate = theFrameRate;
    }

    /**
     * Gets the frame rate value.
     *
     * @return the value
     */
    public int getFrameRate() {

        return this.frameRate;
    }

    /**
     * Sets the back-light active flag.
     *
     * @param isBacklightActive true if back-light is active
     */
    public void setBacklightActive(final boolean isBacklightActive) {

        this.backlightActive = isBacklightActive;
    }

    /**
     * Gets the back-light active flag.
     *
     * @return true if back-light is active
     */
    public boolean isBacklightActive() {

        return this.backlightActive;
    }

    /**
     * Sets the time when the back-light will turn off.
     *
     * @param theElapsed the elapsed time
     */
    public void setBacklightOffElapsed(final double theElapsed) {

        this.backlightOffElapsed = theElapsed;
    }

    /**
     * Gets the time when the back-light will turn off.
     *
     * @return the elapsed time
     */
    public double getBacklightOffElapsed() {

        return this.backlightOffElapsed;
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "PIXEL_OFFSET" macro.
     *
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the pixel offset
     */
    private static int pixelOffset(final int x, final int y) {

        return (y * HardwareConstants.COLOR_LCD_WIDTH + x) * HardwareConstants.COLOR_LCD_DEPTH;
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "LCD_REG" macro.
     *
     * @param reg the register
     * @return the register value
     */
    public int lcdReg(final int reg) {

        return this.registers[reg];
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "LCD_REG_MASK" macro.
     *
     * @param reg  the register
     * @param mask the mask to apply to the returned value
     * @return the masked register value
     */
    public int lcdRegMask(final int reg, final int mask) {

        return this.registers[reg] & mask;
    }

    /**
     * Constructs a new {@code ColorLCD} and initializes it for a calculator model.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_init" function.
     *
     * @return the initialized ColorLCD
     */
    static ColorLCD colorLcdInit() {

        return new ColorLCD();
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "set_line_time" function.
     */
    private void setLineTime() {

        long refreshTime = (long) this.frameRate * (long) (this.displayLines + this.frontPorch + this.backPorch)
                * (long) this.clocksPerLine;
        refreshTime = refreshTime / (long) this.clockDivider;
        this.lineTime = 1.0 / (double) refreshTime;
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "reset_y" function.
     *
     * @param mode the mode
     */
    private void resetY(final int mode) {

        if ((mode & ROW_INC_MASK) == 0) {
            setY(this.registers[WINDOW_HORZ_END_REG]);
        } else {
            setY(this.registers[WINDOW_HORZ_START_REG]);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "reset_x" function.
     *
     * @param mode the mode
     */
    private void resetX(final int mode) {

        if ((mode & COL_INC_MASK) == 0) {
            setX(this.registers[WINDOW_VERT_END_REG]);
        } else {
            setX(this.registers[WINDOW_VERT_START_REG]);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_set_register" function.
     *
     * @param cpu   the CPU
     * @param reg   the register
     * @param value the value
     */
    public void colorLCDSetRegister(final CPU cpu, final int reg, final int value) {

        final int ushortReg = reg & 0x0000FFFF;
        final int ushortValue = value & 0x0000FFFF;

        final int mode = lcdReg(ENTRY_MODE_REG) & 0x0000FFFF;

        switch (ushortReg) {
            case DRIVER_CODE_REG:
                break;

            case DRIVER_OUTPUT_CONTROL1_REG:
                this.registers[ushortReg] = ushortValue & DRIVER_OUTPUT_CONTROL1_MASK;
                break;

            case ENTRY_MODE_REG:
                this.registers[ushortReg] = ushortValue & ENTRY_MODE_MASK;
                if ((mode & ORG_MASK) != 0) {
                    resetX(ushortValue);
                    resetY(ushortValue);
                }
                break;

            case DATA_FORMAT_16BIT_REG:
                this.registers[ushortReg] = ushortValue & DATA_FORMAT_16BIT_MASK;
                break;

            case DISPLAY_CONTROL1_REG:
                this.registers[ushortReg] = ushortValue & DISPLAY_CONTROL1_MASK;
                setActive((ushortValue & DISPLAY_ON_MASK) != 0);
                // if active changed, we need to notify the change immediately
                enqueue(cpu);
                break;

            case DISPLAY_CONTROL2_REG:
                this.registers[ushortReg] = ushortValue & DISPLAY_CONTROL2_MASK;
                this.backPorch = ushortValue & 0x00FF;
                this.frontPorch = ushortValue >> 8;
                setLineTime();
                break;

            case DISPLAY_CONTROL3_REG:
                this.registers[ushortReg] = ushortValue & DISPLAY_CONTROL3_MASK;
                break;

            case DISPLAY_CONTROL4_REG:
                this.registers[ushortReg] = ushortValue & DISPLAY_CONTROL4_MASK;
                break;

            case RGB_DISPLAY_INTERFACE_CONTROL1_REG:
                this.registers[ushortReg] = ushortValue & RGB_DISPLAY_INTERFACE_CONTROL1_MASK;
                break;

            case FRAME_MARKER_REG:
                this.registers[ushortReg] = ushortValue & FRAME_MARKER_MASK;
                break;

            case RGB_DISPLAY_INTERFACE_CONTROL2_REG:
                this.registers[ushortReg] = ushortValue & RGB_DISPLAY_INTERFACE_CONTROL2_MASK;
                break;

            case POWER_CONTROL1_REG:
                this.registers[ushortReg] = ushortValue & POWER_CONTROL1_MASK;
                break;

            case POWER_CONTROL2_REG:
                this.registers[ushortReg] = ushortValue & POWER_CONTROL2_MASK;
                break;

            case POWER_CONTROL3_REG:
                this.registers[ushortReg] = ushortValue & POWER_CONTROL3_MASK;
                break;

            case POWER_CONTROL4_REG:
                this.registers[ushortReg] = ushortValue & POWER_CONTROL4_MASK;
                break;

            case CUR_X_REG:
            case CUR_Y_REG:
                this.registers[ushortReg] = ushortValue & (reg == CUR_X_REG ? CUR_X_MASK : CUR_Y_MASK);

                if ((mode & ORG_MASK) != 0) {
                    if (reg == CUR_Y_REG) {
                        resetY(mode);
                    } else {
                        resetX(mode);
                    }
                } else {
                    setX(lcdReg(CUR_X_REG));
                    setY(lcdReg(CUR_Y_REG));
                }
                break;

            case POWER_CONTROL7_REG:
                this.registers[ushortReg] = ushortValue & POWER_CONTROL7_MASK;
                break;

            case FRAME_RATE_COLOR_CONTROL_REG:
                this.registers[ushortReg] = ushortValue & FRAME_RATE_COLOR_CONTROL_MASK;
                this.panicMode = false;
                switch (ushortValue & FRAME_RATE_MASK) {
                    case 0:
                        this.frameRate = 31;
                        break;
                    case 1:
                        this.frameRate = 32;
                        break;
                    case 2:
                        this.frameRate = 34;
                        break;
                    case 3:
                        this.frameRate = 36;
                        break;
                    case 4:
                        this.frameRate = 39;
                        break;
                    case 5:
                        this.frameRate = 41;
                        break;
                    case 6:
                        // Original Wabbitemu: this.frameRate = 34;
                        // this.frameRate = 44;
                        this.frameRate = 34;
                        break;
                    case 7:
                        this.frameRate = 48;
                        break;
                    case 8:
                        this.frameRate = 52;
                        break;
                    case 9:
                        this.frameRate = 57;
                        break;
                    case 10:
                        this.frameRate = 62;
                        break;
                    case 11:
                        this.frameRate = 69;
                        break;
                    case 12:
                        this.frameRate = 78;
                        break;
                    case 13:
                        this.frameRate = 89;
                        break;
                    default:
                        this.panicMode = true;
                        break;
                }
                setLineTime();
                break;

            case GAMMA_CONTROL1_REG:
            case GAMMA_CONTROL2_REG:
            case GAMMA_CONTROL3_REG:
            case GAMMA_CONTROL4_REG:
            case GAMMA_CONTROL6_REG:
            case GAMMA_CONTROL7_REG:
            case GAMMA_CONTROL8_REG:
            case GAMMA_CONTROL9_REG:
                this.registers[ushortReg] = ushortValue & GAMMA_CONTROL1_MASK;
                break;

            case GAMMA_CONTROL5_REG:
            case GAMMA_CONTROL10_REG:
                this.registers[ushortReg] = ushortValue & GAMMA_CONTROL5_MASK;
                break;

            case WINDOW_HORZ_START_REG:
                this.registers[ushortReg] = ushortValue & WINDOW_HORZ_START_MASK;
                if ((mode & ORG_MASK) != 0 && (mode & COL_INC_MASK) != 0) {
                    setY(lcdReg(ushortReg));
                }
                break;

            case WINDOW_HORZ_END_REG:
                this.registers[ushortReg] = ushortValue & WINDOW_HORZ_END_MASK;
                if ((mode & ORG_MASK) != 0 && (mode & COL_INC_MASK) == 0) {
                    setY(this.registers[ushortReg]);
                }
                break;

            case WINDOW_VERT_START_REG:
                this.registers[ushortReg] = ushortValue & WINDOW_VERT_START_MASK;
                if ((mode & ORG_MASK) != 0 && (mode & ROW_INC_MASK) != 0) {
                    setX(lcdReg(ushortReg));
                }
                break;

            case WINDOW_VERT_END_REG:
                this.registers[ushortReg] = ushortValue & WINDOW_VERT_END_MASK;
                if ((mode & ORG_MASK) != 0 && (mode & ROW_INC_MASK) == 0) {
                    setX(lcdReg(ushortReg));
                }
                break;

            case GATE_SCAN_CONTROL_REG:
                this.registers[ushortReg] = ushortValue & GATE_SCAN_CONTROL_MASK;
                break;

            case BASE_IMAGE_DISPLAY_CONTROL_REG:
                this.registers[ushortReg] = ushortValue & BASE_IMAGE_DISPLAY_CONTROL_MASK;
                break;

            case VERTICAL_SCROLL_CONTROL_REG:
                setZ(ushortValue & VERTICAL_SCROLL_CONTROL_MASK);
                this.registers[ushortReg] = getZ();
                break;

            case PARTIAL_IMAGE1_DISPLAY_POSITION_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE1_DISPLAY_POSITION_MASK;
                break;

            case PARTIAL_IMAGE1_START_LINE_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE1_START_LINE_MASK;
                break;

            case PARTIAL_IMAGE1_END_LINE_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE1_END_LINE_MASK;
                break;

            case PARTIAL_IMAGE2_DISPLAY_POSITION_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE2_DISPLAY_POSITION_MASK;
                break;

            case PARTIAL_IMAGE2_START_LINE_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE2_START_LINE_MASK;
                break;

            case PARTIAL_IMAGE2_END_LINE_REG:
                this.registers[ushortReg] = ushortValue & PARTIAL_IMAGE2_END_LINE_MASK;
                break;

            case PANEL_INTERFACE_CONTROL1_REG:
                this.registers[ushortReg] = ushortValue & PANEL_INTERFACE_CONTROL1_MASK;
                this.clocksPerLine = ushortValue & 0x00FF;
                switch (ushortValue >> 8) {
                    case 0:
                        this.clockDivider = 1;
                        break;
                    case 1:
                        this.clockDivider = 2;
                        break;
                    case 2:
                        this.clockDivider = 4;
                        break;
                    case 3:
                        this.clockDivider = 8;
                        break;
                    default:
                        break;
                }
                setLineTime();
                break;

            case PANEL_INTERFACE_CONTROL2_REG:
                this.registers[ushortReg] = ushortValue & PANEL_INTERFACE_CONTROL2_MASK;
                break;

            case PANEL_INTERFACE_CONTROL4_REG:
                this.registers[ushortReg] = ushortValue & PANEL_INTERFACE_CONTROL4_MASK;
                break;

            case PANEL_INTERFACE_CONTROL5_REG:
                this.registers[ushortReg] = ushortValue & PANEL_INTERFACE_CONTROL5_MASK;
                break;

            case OTP_VCM_PROGRAMMING_CONTROL_REG:
                this.registers[ushortReg] = ushortValue & OTP_VCM_PROGRAMMING_CONTROL_MASK;
                break;

            case OTP_VCM_STATUS_AND_ENABLE_REG:
                this.registers[ushortReg] = ushortValue & OTP_VCM_STATUS_AND_ENABLE_MASK;
                break;

            case OTP_PROGRAMMING_ID_KEY_REG:
                this.registers[ushortReg] = ushortValue & OTP_PROGRAMMING_ID_KEY_MASK;
                break;

            case DEEP_STAND_BY_MODE_CONTROL_REG:
                this.registers[ushortReg] = ushortValue & DEEP_STAND_BY_MODE_CONTROL_MASK;
                break;

            default:
                this.registers[ushortReg] = ushortValue;
                break;
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_enqueue" function.
     *
     * @param cpu the CPU
     */
    public void enqueue(final CPU cpu) {

        if (REAL_LCD) {
            this.drawing = true;

            if (this.drawGate >= this.backPorch
                    && this.drawGate <= (HardwareConstants.COLOR_LCD_WIDTH + 1 - this.frontPorch
                    + this.backPorch)) {

                int offset = (this.drawGate - this.backPorch) * HardwareConstants.COLOR_LCD_DEPTH;
                for (int i = 0; i < HardwareConstants.COLOR_LCD_HEIGHT; ++i) {
                    this.queuedImage[offset] = this.display[offset];
                    this.queuedImage[offset + 1] = this.display[offset + 1];
                    this.queuedImage[offset + 2] = this.display[offset + 2];
                    offset += HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH;
                }
            }

            this.lastDraw = cpu.getTimerContext().getElapsed();
            ++this.drawGate;
            if (this.drawGate == HardwareConstants.COLOR_LCD_WIDTH + this.frontPorch
                    + this.backPorch) {
                this.drawGate = 0;
                this.drawing = false;
                if (cpu.getLcdEnqueueCallback() != null) {
                    cpu.getLcdEnqueueCallback().exec(cpu);
                }
                fireUpdate();
            }
        } else {
            System.arraycopy(this.display, 0, this.queuedImage, 0,
                    HardwareConstants.COLOR_LCD_DISPLAY_SIZE);
            if (cpu.getLcdEnqueueCallback() != null) {
                cpu.getLcdEnqueueCallback().exec(cpu);
            }
            fireUpdate();
        }
    }

    /**
     * Fires the update notification to the update listener, if one is registered.
     */
    private void fireUpdate() {

        if (this.listener != null) {
            this.listener.updateLcd(isActive(), getContrast(), updateImage());
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "read_pixel" function.
     *
     * @return the pixel value at the current (x, y) position
     */
    public int readPixel() {

        final int x = getX() % HardwareConstants.COLOR_LCD_WIDTH;
        final int y = getY() % HardwareConstants.COLOR_LCD_HEIGHT;
        final int pixelPtr = pixelOffset(x, y);
        final int pixel;

        if (lcdRegMask(ENTRY_MODE_REG, BGR_MASK) == 0) {
            pixel = (this.display[pixelPtr] << 16) | (this.display[pixelPtr + 1] << 8) | this.display[pixelPtr + 2];
        } else {
            pixel = (this.display[pixelPtr + 2] << 16) | (this.display[pixelPtr + 1] << 8) | this.display[pixelPtr];
        }

        return pixel;
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "write_pixel" function.
     *
     * @param red   the red component
     * @param green the green component
     * @param blue  the blue component
     */
    private void writePixel(final int red, final int green, final int blue) {

        final int x = getX();
        int y = getY();

        if (lcdRegMask(DRIVER_OUTPUT_CONTROL1_REG, FLIP_COLS_MASK) != 0) {
            y = HardwareConstants.COLOR_LCD_HEIGHT - y - 1;
        }

        // LOG.info("(" + x + ", " + y + ") --> [" + red + "," + green + "," + blue + "]");

        final int pixelPtr = pixelOffset(x, y);
        if (lcdRegMask(ENTRY_MODE_REG, BGR_MASK) == 0) {
            this.display[pixelPtr] = red & 0x003F;
            this.display[pixelPtr + 1] = green & 0x003F;
            this.display[pixelPtr + 2] = blue & 0x003F;
        } else {
            this.display[pixelPtr + 2] = red & 0x003F;
            this.display[pixelPtr + 1] = green & 0x003F;
            this.display[pixelPtr] = blue & 0x003F;
        }

        if (lcdRegMask(ENTRY_MODE_REG, CUR_DIR_MASK) == 0) {
            updateY(true);
        } else {
            updateX(true);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "write_pixel18" function.
     */
    public void writePixel18() {

        final int red;
        final int green;
        final int blue;

        if (lcdRegMask(ENTRY_MODE_REG, UNPACKED_MASK) == 0) {
            final int pixelVal = this.writeBuffer & 0x3ffff;
            red = (pixelVal >> 12) & 0x3F;
            green = (pixelVal >> 6) & 0x3F;
            blue = pixelVal & 0x3F;
        } else {
            final int pixelVal = this.writeBuffer & 0xfcfcfc;
            red = (pixelVal >> 18) & 0x3F;
            green = (pixelVal >> 10) & 0x3F;
            blue = (pixelVal >> 2) & 0x3F;
        }

        writePixel(red, green, blue);
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "write_pixel16" function.
     */
    public void writePixel16() {

        final int pixelVal = this.writeBuffer;
        final int redSignificantBit = (pixelVal & (1 << 15)) != 0 ? 1 : 0;
        final int blueSignificantBit = (pixelVal & (1 << 4)) != 0 ? 1 : 0;

        final int red = ((pixelVal >> 10) | redSignificantBit) & 0x3F;
        final int green = (pixelVal >> 5) & 0x3F;
        final int blue = ((pixelVal << 1) | blueSignificantBit) & 0x3F;

        writePixel(red, green, blue);
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "update_y" function.
     *
     * @param shouldUpdate true to update X as well
     */
    private void updateY(final boolean shouldUpdate) {

        if (lcdRegMask(ENTRY_MODE_REG, ROW_INC_MASK) == 0) {
            if (getY() > lcdReg(WINDOW_HORZ_START_REG)) {
                setY(getY() - 1);
                return;
            }

            // to bottom of the window
            setY(lcdReg(WINDOW_HORZ_END_REG));
        } else {
            if (getY() < lcdReg(WINDOW_HORZ_END_REG)) {
                setY(getY() + 1);
                return;
            }

            // back to top of the window
            setY(lcdReg(WINDOW_HORZ_START_REG));
        }

        if (shouldUpdate) {
            updateX(false);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "update_x" function.
     *
     * @param shouldUpdate true to update Y as well
     */
    private void updateX(final boolean shouldUpdate) {

        if (lcdRegMask(ENTRY_MODE_REG, COL_INC_MASK) == 0) {
            if (getX() > lcdReg(WINDOW_VERT_START_REG)) {
                setX(getX() - 1);
                return;
            }

            // to the end of the window
            setX(lcdReg(WINDOW_VERT_END_REG));
        } else {
            if (getX() < lcdReg(WINDOW_VERT_END_REG)) {
                setX(getX() + 1);
                return;
            }

            // back to the beginning of the window
            setX(lcdReg(WINDOW_VERT_START_REG));
        }

        if (shouldUpdate) {
            updateY(false);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "draw_row_floating" function.
     *
     * @param dest  the destination array
     * @param start the start address
     * @param size  the size
     */
    private static void drawRowFloating(final int[] dest, final int start, final int size) {

        Arrays.fill(dest, start, start + size, 0xFF);
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "draw_row_image" function.
     *
     * @param dest      the destination array
     * @param destStart the start address in the destination array
     * @param src       the source array
     * @param srcStart  the start address in the source array
     * @param size      the size
     */
    private void drawRowImage(final int[] dest, final int destStart, final int[] src, final int srcStart,
                              final int size) {

        final boolean levelInvert = lcdRegMask(BASE_IMAGE_DISPLAY_CONTROL_REG, LEVEL_INVERT_MASK) == 0;
        final boolean color8bit = lcdRegMask(DISPLAY_CONTROL1_REG, COLOR8_MASK) != 0;

        int alpha = 100;

        if (getContrast() == HardwareConstants.MAX_BACKLIGHT_LEVEL - 1) {
            alpha = 0;
        }

        final int alphaOverlay = 0;
        final int inverseAlpha = alpha;
        int r;
        int g;
        int b;
        final int bits = color8bit ? 1 : 6;

        if (levelInvert) {
            int redSrcIndex = 0;
            int greenSrcIndex = 1;
            int blueSrcIndex = 2;

            final boolean flipRows = lcdRegMask(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) != 0;

            if (flipRows) {
                redSrcIndex += size - 3;
                greenSrcIndex += size - 3;
                blueSrcIndex += size - 3;
                for (int i = 0; i < size; i += 3) {
                    r = src[srcStart + redSrcIndex - i] ^ 0x3f;
                    g = src[srcStart + greenSrcIndex - i] ^ 0x3f;
                    b = src[srcStart + blueSrcIndex - i] ^ 0x3f;
                    if (color8bit) {
                        r >>= 5;
                        g >>= 5;
                        b >>= 5;
                    }

                    dest[destStart + i] = (alphaOverlay + truColor(r, bits) * inverseAlpha / 100) & 0x00FF;
                    dest[destStart + i + 1] = (alphaOverlay + truColor(g, bits) * inverseAlpha / 100) & 0x00FF;
                    dest[destStart + i + 2] = (alphaOverlay + truColor(b, bits) * inverseAlpha / 100) & 0x00FF;
                }
            } else {
                for (int i = 0; i < size; i += 3) {
                    r = src[srcStart + redSrcIndex + i] ^ 0x3f;
                    g = src[srcStart + greenSrcIndex + i] ^ 0x3f;
                    b = src[srcStart + blueSrcIndex + i] ^ 0x3f;
                    if (color8bit) {
                        r >>= 5;
                        g >>= 5;
                        b >>= 5;
                    }

                    dest[destStart + i] = (alphaOverlay + truColor(r, bits) * inverseAlpha / 100) & 0x00FF;
                    dest[destStart + i + 1] = (alphaOverlay + truColor(g, bits) * inverseAlpha / 100) & 0x00FF;
                    dest[destStart + i + 2] = (alphaOverlay + truColor(b, bits) * inverseAlpha / 100) & 0x00FF;
                }
            }
        } else {
            for (int i = 0; i < size; ++i) {
                final int val = color8bit ? src[srcStart + i] >> 5 : src[srcStart + i];
                dest[destStart + i] = (alphaOverlay + truColor(val, bits) * inverseAlpha / 100) & 0x00FF;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "draw_partial_image" function.
     *
     * @param dest      the destination array
     * @param destStart the start address in the destination array
     * @param src       the source array
     * @param srcStart  the start address in the source array
     * @param offset    the offset
     * @param size      the size
     */
    private void drawPartialImage(final int[] dest, final int destStart, final int[] src,
                                  final int srcStart, final int offset, final int size) {

        int theOffset = offset;

        if (theOffset > HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH) {
            theOffset %= HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH;
        }

        if (theOffset + size > HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH) {
            final int rightMarginSize = (HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH)
                    - theOffset;
            final int leftMarginSize = size - rightMarginSize;
            final boolean flipRows = lcdRegMask(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) != 0;

            if (flipRows) {
                drawRowImage(dest, destStart + leftMarginSize, src, srcStart + theOffset, rightMarginSize);
                drawRowImage(dest, destStart, src, srcStart, leftMarginSize);
            } else {
                drawRowImage(dest, destStart, src, srcStart + theOffset, rightMarginSize);
                drawRowImage(dest, destStart + rightMarginSize, src, srcStart, leftMarginSize);
            }
        } else {
            drawRowImage(dest, destStart, src, srcStart + theOffset, size);
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "draw_nondisplay_area" function.
     *
     * @param dest      the destination array
     * @param destStart the start address in the destination array
     * @param size      the size
     * @param ndlColor  the color
     */
    private static void drawNondisplayArea(final int[] dest, final int destStart, final int size, final int ndlColor) {

        Arrays.fill(dest, destStart, destStart + size, ndlColor);
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "draw_row" function.
     *
     * @param dest         the destination array
     * @param destStart    the start address in the destination array
     * @param src          the source array
     * @param srcStart     the start address in the source array
     * @param startX       the start X
     * @param displayWidth the display width
     * @param imgpos1      the pos 1
     * @param imgoffs1     the offset 1
     * @param imgsize1     the size 1
     * @param imgpos2      the pos 2
     * @param imgoffs2     the offset 2
     * @param imgsize2     the size 2
     */
    private void drawRow(final int[] dest, final int destStart, final int[] src, final int srcStart,
                         final int startX, final int displayWidth, final int imgpos1, final int imgoffs1,
                         final int imgsize1, final int imgpos2, final int imgoffs2, final int imgsize2) {

        final int[] interlaceBuf = new int[HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH];

        final int nonDisplayAreaColor = lcdRegMask(BASE_IMAGE_DISPLAY_CONTROL_REG, NDL_MASK) != 0
                ? truColor(0x00, 6) : truColor(0x3F, 6);
        final boolean interlaceCols = lcdRegMask(DRIVER_OUTPUT_CONTROL1_REG, INTERLACED_MASK) != 0;

        final int[] optr = interlaceCols ? interlaceBuf : dest;
        int optrStart = interlaceCols ? 0 : destStart;

        if (startX != 0) {
            if (interlaceCols) {
                drawNondisplayArea(optr, optrStart, startX, 0);
            } else {
                drawRowFloating(optr, optrStart, startX);
            }
            optrStart += startX;
        }

        final int n = HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH - startX - displayWidth;
        if (imgsize1 != n && imgsize2 != n) {
            drawNondisplayArea(optr, optrStart, n, nonDisplayAreaColor);
        }

        if (imgsize1 != 0) {
            drawPartialImage(optr, optrStart + imgpos1, src, srcStart, imgoffs1, imgsize1);
        }

        if (imgsize2 != 0) {
            drawPartialImage(optr, optrStart + imgpos2, src, srcStart, imgoffs2, imgsize2);
        }

        optrStart += n;

        if (displayWidth != 0) {
            if (interlaceCols) {
                drawNondisplayArea(optr, optrStart, displayWidth, 0);
            } else {
                drawRowFloating(optr, optrStart, displayWidth);
            }
        }

        if (interlaceCols) {
            optrStart = 0;
            int destPos = 0;
            for (int i = 0; i < HardwareConstants.COLOR_LCD_WIDTH / 2; ++i, optrStart += 3) {
                dest[destStart + destPos] = optr[optrStart];
                ++destPos;
                dest[destStart + destPos] = optr[optrStart + 1];
                ++destPos;
                dest[destStart + destPos] = optr[optrStart + 2];
                ++destPos;
                dest[destStart + destPos] = optr[optrStart
                        + (HardwareConstants.COLOR_LCD_DEPTH * HardwareConstants.COLOR_LCD_WIDTH) / 2];
                ++destPos;
                dest[destStart + destPos] = optr[optrStart
                        + (HardwareConstants.COLOR_LCD_DEPTH * HardwareConstants.COLOR_LCD_WIDTH) / 2 + 1];
                ++destPos;
                dest[destStart + destPos] = optr[optrStart
                        + (HardwareConstants.COLOR_LCD_DEPTH * HardwareConstants.COLOR_LCD_WIDTH) / 2 + 2];
                ++destPos;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/colorlcd.c, "ColorLCD_Image" function.
     *
     * @return the image
     */
    private int[] updateImage() {

        final int[] buffer = new int[HardwareConstants.COLOR_LCD_DISPLAY_SIZE];

        int p1pos;
        final int p1start;
        final int p1end;
        int p1width;
        int p2pos;
        final int p2start;
        final int p2end;
        int p2width;

        if (!isActive() || !this.backlightActive) {
            return buffer;
        }

        if (this.panicMode) {
            for (int i = 0; i < HardwareConstants.COLOR_LCD_HEIGHT; ++i) {
                for (int j = 0; j < HardwareConstants.COLOR_LCD_WIDTH; j += 2) {
                    buffer[pixelOffset(j, i)] = 0xFF;
                    buffer[pixelOffset(j, i) + 1] = 0xFF;
                    buffer[pixelOffset(j, i) + 2] = 0xFF;
                }
            }
            return buffer;
        }

        int startX = lcdRegMask(GATE_SCAN_CONTROL_REG, BASE_START_MASK) << 3;
        int pixelWidth = ((lcdRegMask(GATE_SCAN_CONTROL_REG, BASE_NLINES_MASK) >> 8) + 1) << 3;

        if (startX > HardwareConstants.COLOR_LCD_WIDTH) {
            startX = HardwareConstants.COLOR_LCD_WIDTH;
        }

        if (pixelWidth > (HardwareConstants.COLOR_LCD_WIDTH - startX)) {
            pixelWidth = HardwareConstants.COLOR_LCD_WIDTH - startX;
        }

        int displayWidth = (HardwareConstants.COLOR_LCD_WIDTH - (startX + pixelWidth))
                * HardwareConstants.COLOR_LCD_DEPTH;
        startX *= HardwareConstants.COLOR_LCD_DEPTH;

        if (lcdRegMask(DISPLAY_CONTROL1_REG, BASEE_MASK) == 0) {
            if (lcdRegMask(DISPLAY_CONTROL1_REG, SHOW_PARTIAL1_MASK) == 0) {
                p1pos = 0;
                p1start = 0;
                p1width = 0;
            } else {
                p1pos = lcdRegMask(PARTIAL_IMAGE1_DISPLAY_POSITION_REG, P1_POS_MASK)
                        % HardwareConstants.COLOR_LCD_WIDTH;
                p1start = lcdRegMask(PARTIAL_IMAGE1_START_LINE_REG, P1_START_MASK) % HardwareConstants.COLOR_LCD_WIDTH;
                p1end = lcdRegMask(PARTIAL_IMAGE1_END_LINE_REG, P1_END_MASK) % HardwareConstants.COLOR_LCD_WIDTH;

                p1width = p1end + 1 - p1start;
                if (p1width < 0) {
                    p1width += HardwareConstants.COLOR_LCD_WIDTH;
                }

                if (p1pos > pixelWidth) {
                    p1pos = pixelWidth;
                }

                if (p1width > (pixelWidth - p1pos)) {
                    p1width = pixelWidth - p1pos;
                }
            }

            if (lcdRegMask(DISPLAY_CONTROL1_REG, SHOW_PARTIAL2_MASK) == 0) {
                p2pos = 0;
                p2start = 0;
                p2width = 0;
            } else {
                p2pos = lcdRegMask(PARTIAL_IMAGE2_DISPLAY_POSITION_REG, P2_POS_MASK)
                        % HardwareConstants.COLOR_LCD_WIDTH;
                p2start = lcdRegMask(PARTIAL_IMAGE2_START_LINE_REG, P2_START_MASK) % HardwareConstants.COLOR_LCD_WIDTH;
                p2end = lcdRegMask(PARTIAL_IMAGE2_END_LINE_REG, P2_END_MASK) % HardwareConstants.COLOR_LCD_WIDTH;

                p2width = p2end + 1 - p2start;
                if (p2width < 0) {
                    p2width += HardwareConstants.COLOR_LCD_WIDTH;
                }

                if (p2pos > pixelWidth) {
                    p2pos = pixelWidth;
                }

                if (p2width > (pixelWidth - p2pos)) {
                    p2width = pixelWidth - p2pos;
                }
            }
        } else {
            p2pos = 0;
            p2width = pixelWidth;
            if (lcdRegMask(BASE_IMAGE_DISPLAY_CONTROL_REG, SCROLL_ENABLED_MASK) == 0) {
                p2start = 0;
            } else {
                p2start = getZ();
            }

            p1pos = 0;
            p1start = 0;
            p1width = 0;
        }

        final boolean flipRows = lcdRegMask(GATE_SCAN_CONTROL_REG, GATE_SCAN_DIR_MASK) != 0;
        if (flipRows) {
            final int tmp = displayWidth;
            displayWidth = startX;
            startX = tmp;

            p1pos = HardwareConstants.COLOR_LCD_WIDTH - (p1pos + p1width);
            p2pos = HardwareConstants.COLOR_LCD_WIDTH - (p2pos + p2width);
        }

        int destIndex = 0;
        final int[] src = getQueuedImage();
        int srcIndex = 0;

        final int imgpos1 = p2pos * HardwareConstants.COLOR_LCD_DEPTH;
        final int imgoffs1 = p2start * HardwareConstants.COLOR_LCD_DEPTH;
        final int imgsize1 = p2width * HardwareConstants.COLOR_LCD_DEPTH;
        final int imgpos2 = p1pos * HardwareConstants.COLOR_LCD_DEPTH;
        final int imgoffs2 = p1start * HardwareConstants.COLOR_LCD_DEPTH;
        final int imgsize2 = p1width * HardwareConstants.COLOR_LCD_DEPTH;

        for (int i = 0; i < HardwareConstants.COLOR_LCD_HEIGHT; ++i) {
            drawRow(buffer, destIndex, src, srcIndex, startX, displayWidth, imgpos1, imgoffs1, imgsize1, imgpos2,
                    imgoffs2, imgsize2);

            destIndex += HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH;
            srcIndex += HardwareConstants.COLOR_LCD_WIDTH * HardwareConstants.COLOR_LCD_DEPTH;
        }

        return buffer;
    }
}
