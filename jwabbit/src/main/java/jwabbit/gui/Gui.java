package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.FileLoader;
import jwabbit.Launcher;
import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.KEYPROG;
import jwabbit.iface.Calc;
import jwabbit.iface.EnumEventType;
import jwabbit.log.LoggedObject;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * The main application class.
 *
 * <p>
 * SOURCE: gui/guiapp.h, "WabbitemuApp" class.
 */
public final class Gui {

    /** WABBITEMU SOURCE: gui/gui.c, "gif_anim_advance" global. */
    static boolean gifAnimAdvance;

    /** WABBITEMU SOURCE: gui/gui.c, "silent_mode" global. */
    static boolean silentMode;

    /** WABBITEMU SOURCE: gui/gui.h, "SKIN_WIDTH" macro. */
    static final int SKIN_WIDTH = 350;

    /** WABBITEMU SOURCE: gui/gui.h, "SKIN_HEIGHT" macro. */
    static final int SKIN_HEIGHT = 725;

    /** WABBITEMU SOURCE: gui/gui.c, "keygrps" global array. */
    static final KEYPROG[] KEYGRPS;

    /** WABBITEMU SOURCE: gui/gui.c, "keysti83" global array. */
    static final KEYPROG[] KEYSTI83;

    /** WABBITEMU SOURCE: gui/gui.c, "keysti86" global array. */
    static final KEYPROG[] KEYSTI86;

    /** Bitmap information. */
    static final BitmapInfo BI;

    /** Bitmap information. */
    static final BitmapInfo COLORBI;

    /** WABBITEMU SOURCE: gui/guilcd.h, "MAX_SHADES" macro. */
    private static final int MAX_SHADES = 255;

    /** Bitmap information. */
    private static final BitmapInfo CONTRASTBI;

    static {
        KEYGRPS = new KEYPROG[256];
        KEYSTI83 = new KEYPROG[256];
        KEYSTI86 = new KEYPROG[256];

        BI = new BitmapInfo(MAX_SHADES + 1);
        BI.setWidth(128);
        BI.setHeight(64);
        BI.setBitCount(8);
        BI.setClrUsed(MAX_SHADES + 1);
        BI.setClrImportant(MAX_SHADES + 1);

        for (int i = 0; i <= MAX_SHADES; ++i) {
            BI.setColor(i, new Color(0x9E * (256 - i) / 255 & 0x00FF, 0xAB * (256 - i) / 255 & 0x00FF,
                    0x88 * (256 - i) / 255 & 0x00FF));
        }

        CONTRASTBI = new BitmapInfo(MAX_SHADES + 1);
        CONTRASTBI.setWidth(128);
        CONTRASTBI.setHeight(64);
        CONTRASTBI.setBitCount(8);
        CONTRASTBI.setClrUsed(MAX_SHADES + 1);
        CONTRASTBI.setClrImportant(MAX_SHADES + 1);

        for (int i = 0; i <= MAX_SHADES; ++i) {
            CONTRASTBI.setColor(i, new Color(0x9E * (256 - i) / 255 & 0x00FF, 0xAB * (256 - i) / 255 & 0x00FF,
                    0x20 - 0x88 * (256 - i) / 255 & 0x00FF));
        }

        COLORBI = new BitmapInfo(0);
        COLORBI.setWidth(HardwareConstants.COLOR_LCD_WIDTH);
        COLORBI.setHeight(HardwareConstants.COLOR_LCD_HEIGHT);
        COLORBI.setBitCount(24);
        COLORBI.setClrUsed(0);
        COLORBI.setClrImportant(0);
    }

    /**
     * Constructs a new {@code Gui}.
     */
    public Gui() {

        super();
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "GetLCDColorPalette" function.
     *
     * @param model    the calculator model
     * @param active   the active state of the LCD
     * @param contrast the LCD contrast
     * @return the bitmap info
     */
    static BitmapInfo getLCDColorPalette(final EnumCalcModel model, final boolean active, final int contrast) {

        BitmapInfo info = model.ordinal() >= EnumCalcModel.TI_84PCSE.ordinal() ? COLORBI : BI;

        if (model.ordinal() <= EnumCalcModel.TI_84PSE.ordinal() && active
                && contrast > HardwareConstants.LCD_MAX_CONTRAST - 4) {
            info = CONTRASTBI;
        }

        return info;
    }

    /**
     * Creates a new Calc object then builds a CalcUI for it and registers all events with the calculator.
     *
     * <p>
     * WABBITEMU SOURCE: gui/gui.c, "create_calc_frame_register_events" function.
     *
     * @param buildFrame true to construct a {@code MainFrame} and install the calculator panel in the frame; false to
     *                   simply construct the calculator panel, so it can be hosted within another component or frame
     * @return the main window
     */
    static CalcUI createCalcRegisterEvents(final boolean buildFrame) {

        final Calc calc = new Calc();

        // Attempt to load a ROM into the new calculator (if this fails, no need to continue)
        final ParsedCmdArgs args = WabbitemuModule.MODULE.getParsedCmdArgs();

        // ROMs are special, we need to load them first before anything else
        boolean loadedRom = false;
        final int numRoms = args.getNumRomFiles();
        if (numRoms > 0) {
            for (int i = 0; i < numRoms; ++i) {
                // if you specify more than one rom file to be loaded, only the first is loaded
                final String romFile = args.getRomFile(i);
                LoggedObject.LOG.info("Loading from file ", romFile);
                if (calc.romLoad(romFile)) {
                    loadedRom = true;
                    break;
                }
            }
        }

        if (!loadedRom) {
            calc.setRomPath(Registry.asString(Registry.queryWabbitKey("rom_path")));
            final String romPath = calc.getRomPath();
            LoggedObject.LOG.info("Loading ROM: ", romPath);
            loadedRom = calc.romLoad(romPath);
        }

        CalcUI theCalcUI = null;

        if (loadedRom) {
            final int slot = Launcher.addCalc(calc, buildFrame);

            if (slot != -1) {
                theCalcUI = Launcher.getCalcUI(slot);
                registerEvents(calc, theCalcUI);
                Launcher.getCalcThread(slot).start();
            }
        }

        return theCalcUI;
    }

    /**
     * Registers events for a calculator.
     *
     * @param calc      the calculator
     * @param theCalcUI the calculator UI
     */
    public static void registerEvents(final Calc calc, final CalcUI theCalcUI) {

        calc.calcRegisterEvent(EnumEventType.LCD_ENQUEUE_EVENT, new EventGuiDraw(), theCalcUI);
        calc.calcRegisterEvent(EnumEventType.ROM_LOAD_EVENT, new EventLoadSettings(), theCalcUI);
        calc.calcRegisterEvent(EnumEventType.ROM_LOAD_EVENT, new EventLoadKeySettings(), null);
        calc.calcRegisterEvent(EnumEventType.ROM_LOAD_EVENT, new EventCheckBootfree(), null);
        calc.calcRegisterEvent(EnumEventType.ROM_LOAD_EVENT, new EventSyncCalcClock(), null);
        calc.calcRegisterEvent(EnumEventType.ROM_LOAD_EVENT, new EventCreateBCallLabels(), theCalcUI);
        calc.calcRegisterEvent(EnumEventType.ROM_RUNNING_EVENT, new EventUpdateCalcRunning(), theCalcUI);
        // calc.calcRegisterEvent(EnumEventType.BREAKPOINT_EVENT, new EventFireComBreakpoint(), theCalcUI);
        calc.calcRegisterEvent(EnumEventType.GIF_FRAME_EVENT, new EventHandleScreenshot(), null);
        calc.calcRegisterEvent(EnumEventType.AVI_VIDEO_FRAME_EVENT, new EventHandleAviVideoFrame(), theCalcUI);
        calc.calcRegisterEvent(EnumEventType.AVI_AUDIO_FRAME_EVENT, new EventHandleAviAudioFrame(), theCalcUI);

        if (!WabbitemuModule.MODULE.getParsedCmdArgs().isNoCreateCalc()) {
            calc.calcRegisterEvent(EnumEventType.BREAKPOINT_EVENT, new EventGuiDebug(), theCalcUI);
        }
    }

    /**
     * Loads the image.
     *
     * @param name the name of the file under "jwabbit/gui/images"
     * @return the loaded image; {@code null} if unable to load
     */
    public static BufferedImage loadImage(final String name) {

        return FileLoader.loadFileAsImage(Gui.class, "images/" + name, true);
    }

    /**
     * Loads the skin.
     *
     * @param name the name of the file under "jwabbit/gui/skins"
     * @return the loaded skin; {@code null} if unable to load
     */
    static BufferedImage loadSkin(final String name) {

        return FileLoader.loadFileAsImage(Gui.class, name, true);
    }
}
