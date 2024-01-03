package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.Launcher;
import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.ColorLCD;
import jwabbit.hardware.HardwareConstants;
import jwabbit.hardware.LCD;
import jwabbit.iface.Calc;
import jwabbit.iface.EnumGifDispState;
import jwabbit.log.LoggedObject;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The UI for a single calculator, which may include several frames.
 *
 * <p>
 * WABBITEMU SOURCE: gui/gui.h, "MainWindow" struct.
 */
public final class CalcUI extends WindowAdapter implements ComponentListener, ILcdListener {

    /** A faceplate color. */
    public static final Color CHARCOAL = new Color(0x2e, 0x2e, 0x2e);

    /** A faceplate color. */
    public static final Color GRAY = new Color(0x83, 0x85, 0x87);

    /** A faceplate color. */
    public static final Color SILVER = new Color(0xd1, 0xd3, 0xd5);

    /** A faceplate color. */
    public static final Color PINK = new Color(0xea, 0xa7, 0xbd);

    /** A faceplate color. */
    public static final Color BRICKRED = new Color(0xa8, 0x3e, 0x27);

    /** A faceplate color. */
    public static final Color DARKRED = new Color(0x63, 0x1f, 0x21);

    /** A faceplate color. */
    public static final Color RED = new Color(0x88, 0x00, 0x03);

    /** A faceplate color. */
    public static final Color BLUE = new Color(0x00, 0x40, 0x96);

    /** A faceplate color. */
    public static final Color DARKBLUE = new Color(0x05, 0x2b, 0x55);

    /** A faceplate color. */
    public static final Color LIGHTBLUE = new Color(0x97, 0xaf, 0xce);

    /** A faceplate color. */
    public static final Color PURPLE = new Color(0x4b, 0x32, 0x65);

    /** A faceplate color. */
    public static final Color TAN = new Color(0xaf, 0x97, 0x4b);

    /** A faceplate color. */
    public static final Color BROWN = new Color(0x6a, 0x50, 0x31);

    /** A faceplate color. */
    public static final Color ORANGE = new Color(0xdf, 0x83, 0x3d);

    /** A faceplate color. */
    public static final Color GREEN = new Color(0x20, 0x86, 0x52);

    /** A faceplate color. */
    public static final Color YELLOW = new Color(0xff, 0xf9, 0xbd);

    /** Snap distance for screen edges. */
    private static final int SNAP_DIST = 10;

    /** The slot with which this UI is associated. */
    private final int slot;

    /** The calculator model. */
    private final EnumCalcModel model;

    /** The skin panel. */
    private CalculatorPanel calculatorPanel;

    /** The main frame that shows the calculator skin and hosts the menu bar (can be null). */
    private MainFrame mainFrame;

    /** The frame that shows the LCD without the surrounding skin (can be null). */
    private JFrame detachedLcdFrame;

    /** The frame that shows the teacher view (can be null). */
    private JFrame teacherViewFrame;

    /** The frame that shows the list of keys (can be null). */
    private JFrame keyListDialog;

    /** The frame that shows the list of variables (can be null). */
    private JFrame varListDialog;

    /** The debugger attached to this calculator (can be null). */
    private JFrame debugger;

    /** The rendered skin image, scaled to the current window size. */
    private BufferedImage renderedSkinImage;

    /** The rendered key map image, scaled to the current window size. */
    private BufferedImage renderedKeymapImage;

    /** The unscaled skin image. */
    private BufferedImage skinImage;

    /** The unscaled skin mask. */
    private BufferedImage skinMask;

    /** The unscaled key map image. */
    private BufferedImage keymapImage;

    /** The scaled skin rectangle in the displayed window. */
    private final Rectangle skinRect;

    /** The LCD rectangle, relative to the unscaled skin and keymap. */
    private final Rectangle lcdRect;

    /** The rectangle for each key, by group then bit, relative to the unscaled skin and keymap. */
    private final Rectangle[][] keyRects;

    /** The color of the faceplate (used when building the rendered skin). */
    private Color faceplateColor;

    /** The scale applied to the skin and keymap image when rendering. */
    private int scale;

    /** The default scale for the skin (from preferences). */
    private double defaultSkinScale;

    /** The skin scale. */
    private double skinScale;

    /** True to show a cutout skin (no window border, transparent background). */
    private boolean cutout;

    /** True if skin is enabled; false if not. */
    private boolean skinEnabled;

    /** Flag indicating a custom skin is in use. */
    private boolean customSkin;

    /** GIF display state. */
    private EnumGifDispState gifDisplayState;

    /** Current AVI. */
    private AviFile currentAvi;

    /** Recording flag. */
    private boolean recording;

    /** TIOS debug flag used by the disassembler. */
    private boolean tiosDebug;

    /** Flags indicating we're resizing and which edges of the screen window lies on. */
    private int resizing;

    /**
     * Constructs a new {@code CalcUI}.
     *
     * @param theModel the calculator model
     * @param theSlot  the slot with which this UI is associated
     */
    private CalcUI(final EnumCalcModel theModel, final int theSlot) {

        super();

        this.model = theModel;
        this.slot = theSlot;

        // this.teacherViews = new TeacherViewScreen[
        // TeacherViewScreen.TEACHER_VIEW_ROWS][TeacherViewScreen.TEACHER_VIEW_COLS];

        this.skinRect = new Rectangle();
        this.lcdRect = new Rectangle();
        this.keyRects = new Rectangle[8][8];
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                this.keyRects[i][j] = new Rectangle();
            }
        }

        this.skinEnabled = Registry.asBoolean(Registry.queryWabbitKey("skin"));
    }

    /**
     * Gets the calculator model.
     *
     * @return the model
     */
    public EnumCalcModel getModel() {

        return this.model;
    }

    /**
     * Gets the slot with which this UI is associated.
     *
     * @return the slot
     */
    public int getSlot() {

        return this.slot;
    }

    /**
     * Gets the calculator panel.
     *
     * @return the calculator panel
     */
    CalculatorPanel getCalculatorPanel() {

        return this.calculatorPanel;
    }

    /**
     * Gets the main frame.
     *
     * @return the main frame
     */
    JFrame getMainFrame() {

        return this.mainFrame;
    }

    /**
     * Sets the main frame.
     *
     * @param theMainFrame the main frame
     */
    private void setMainFrame(final MainFrame theMainFrame) {

        this.mainFrame = theMainFrame;
    }

    /**
     * Gets the detached LCD frame.
     *
     * @return the detached LCD frame
     */
    JFrame getDetachedLCDFrame() {

        return this.detachedLcdFrame;
    }

    /**
     * Gets the debugger frame.
     *
     * @return the debugger frame
     */
    JFrame getDebugFrame() {

        return this.debugger;
    }

    /**
     * Gets the rendered skin image.
     *
     * @return the image
     */
    BufferedImage getRenderedSkin() {

        return this.renderedSkinImage;
    }

    /**
     * Gets the rendered key map image.
     *
     * @return the image
     */
    BufferedImage getRenderedKeymap() {

        return this.renderedKeymapImage;
    }

    /**
     * Gets the skin image.
     *
     * @return the image
     */
    public BufferedImage getSkin() {

        return this.skinImage;
    }

    /**
     * Gets the key map image.
     *
     * @return the image
     */
    BufferedImage getKeymap() {

        return this.keymapImage;
    }

    /**
     * Gets the skin rectangle.
     *
     * @return the skin rectangle
     */
    Rectangle getRectSkin() {

        return this.skinRect;
    }

    /**
     * Gets the LCD rectangle.
     *
     * @return the LCD rectangle
     */
    Rectangle getLCDRect() {

        return this.lcdRect;
    }

    /**
     * Gets a key rectangle.
     *
     * @param group the group
     * @param bit   the bit
     * @return the key rectangle
     */
    Rectangle getKeyRect(final int group, final int bit) {

        return this.keyRects[group][bit];
    }

    /**
     * Gets the scale.
     *
     * @return the scale
     */
    public int getScale() {

        return this.scale;
    }

    /**
     * Sets the scale.
     *
     * @param theScale scale
     */
    public void setScale(final int theScale) {

        this.scale = theScale;
    }

    /**
     * Gets the skin scale.
     *
     * @return the scale
     */
    double getSkinScale() {

        return this.skinScale;
    }

    /**
     * Tests whether skin is enabled.
     *
     * @return true if skin is enabled
     */
    boolean isSkinEnabled() {

        return this.skinEnabled;
    }

    /**
     * Gets the GIF display state.
     *
     * @return the display sate
     */
    EnumGifDispState getGifDispState() {

        return this.gifDisplayState;
    }

    /**
     * Tests whether recording in progress.
     *
     * @return true if recording
     */
    public boolean isRecording() {

        return this.recording;
    }

    /**
     * Sets the flag that indicates whether recording is progress.
     *
     * @param isRecording true if recording in progress
     */
    public void setRecording(final boolean isRecording) {

        this.recording = isRecording;
    }

    /**
     * Tests whether TIOS debug enabled.
     *
     * @return true if TIOS debug enabled
     */
    boolean isTIOSDebug() {

        return this.tiosDebug;
    }

    /**
     * Sets the flags that indicate we're resizing based on a skin change (not on user dragging of the window), and
     * which edges of the screen window is adjacent to.
     *
     * @param theResizing 0 if not is a resize based on skin change, or a bitwise OR of the following: 1 if we're
     *                    resizing based on a skin change, 2 if the window is on the right edge of the screen, and 4 if
     *                    the window is at the bottom edge of the screen
     */
    void setResizing(final int theResizing) {

        this.resizing = theResizing;
    }

    /**
     * Kills the Calculator UI, when the calculator is being closed.
     */
    public void die() {

        closeSubFrames();

        if (this.mainFrame != null) {
            this.mainFrame.setVisible(false);
            this.mainFrame.dispose();
        }
    }

    /**
     * Closes all frames except the main frame.
     */
    private void closeSubFrames() {

        if (this.debugger != null) {
            this.debugger.setVisible(false);
            this.debugger.dispose();
            this.debugger = null;
        }

        if (this.detachedLcdFrame != null) {
            this.detachedLcdFrame.setVisible(false);
            this.detachedLcdFrame.dispose();
            this.detachedLcdFrame = null;
        }

        if (this.keyListDialog != null) {
            this.keyListDialog.setVisible(false);
            this.keyListDialog.dispose();
            this.keyListDialog = null;
        }

        if (this.varListDialog != null) {
            this.varListDialog.setVisible(false);
            this.varListDialog.dispose();
            this.varListDialog = null;
        }

        if (this.teacherViewFrame != null) {
            this.teacherViewFrame.setVisible(false);
            this.teacherViewFrame.dispose();
            this.teacherViewFrame = null;
        }
    }

    /**
     * Called when the LCD is updated.
     *
     * @param active   the LCD active state
     * @param contrast the LCD contrast
     * @param data     the LCD image data
     */
    @Override
    public void updateLcd(final boolean active, final int contrast, final int[] data) {

        // For now, just send to calculator panel. Once detached LCD frame or teacher frame are
        // working, send to those as well.

        this.calculatorPanel.updateLcd(active, contrast, data);
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "GetStartPoint" function.
     *
     * @return the start point
     */
    private static Point getStartPoint() {

        final Object startXObj = Registry.queryWabbitKey("startX");
        final Object startYObj = Registry.queryWabbitKey("startY");

        final int startX = startXObj instanceof Integer ? ((Integer) startXObj).intValue() : -1;
        final int startY = startYObj instanceof Integer ? ((Integer) startYObj).intValue() : -1;

        final Rectangle desktop = new Rectangle();
        final Point topLeftPt = new Point(startX, startY);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        desktop.width = screen.width;
        desktop.height = screen.height;

        if (!desktop.contains(topLeftPt)) {
            // pt is not on the desktop
            topLeftPt.setLocation(-1, -1);
        }

        return topLeftPt;
    }

    /**
     * WABBITEMU SOURCE: gui/gui.c, "gui_frame" function.
     *
     * @param calc the calculator
     * @return the calculator UI
     */
    static CalcUI guiFrame(final Calc calc) {

        // Called after calculator construction but before calculator thread is created, so still
        // safe to access calculator

        final Rectangle rect = new Rectangle();

        // this is to do some checks on some bad registry settings we may have saved its also good
        // for multiple monitors, in case wabbit was on a monitor that no longer exists
        final Point startPoint = getStartPoint();

        final CalcUI theCalcUI = new CalcUI(calc.getModel(), calc.getSlot());

        if (theCalcUI.scale == 0) {
            theCalcUI.scale = 2;
        }

        if (theCalcUI.skinScale == 0.0) {
            theCalcUI.skinScale = 1.0;
        }

        if (theCalcUI.skinEnabled) {
            rect.setBounds(0, 0, Gui.SKIN_WIDTH, Gui.SKIN_HEIGHT);
        } else {
            rect.setBounds(0, 0, 96 * theCalcUI.scale, 64 * theCalcUI.scale);
        }

        if (SwingUtilities.isEventDispatchThread()) {
            constructUI(theCalcUI, true, startPoint);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    /** Run. */
                    @Override
                    public void run() {

                        constructUI(theCalcUI, true, startPoint);
                    }
                });
            } catch (final InvocationTargetException ex) {
                LoggedObject.LOG.warning("Failed to build calculator panel and main window", ex);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        if (theCalcUI.mainFrame == null) {
            return null;
        }

        calc.setSpeed(100);
        final AbstractLCDBase lcd = calc.getCPU().getPIOContext().getLcd();
        if (lcd instanceof final LCD lcdObject) {
            lcdObject.setListener(theCalcUI);
        } else if (lcd instanceof final ColorLCD colorLcdObject) {
            colorLcdObject.setListener(theCalcUI);
        }

        theCalcUI.refresh();
        theCalcUI.positionWindow();
        theCalcUI.mainFrame.setVisible(true);

        return theCalcUI;
    }

    /**
     * Constructs a calculator panel without the associated {@code MainFrame}.
     *
     * @param calc the calculator
     * @return the calculator UI
     */
    static CalcUI guiPanel(final Calc calc) {

        // Called after calculator construction but before calculator thread is created, so still
        // safe to access calculator

        final Rectangle rect = new Rectangle();

        // this is to do some checks on some bad registry settings we may have saved its also good
        // for multiple monitors, in case wabbit was on a monitor that no longer exists
        final Point startPoint = getStartPoint();

        final CalcUI theCalcUI = new CalcUI(calc.getModel(), calc.getSlot());

        if (theCalcUI.scale == 0) {
            theCalcUI.scale = 2;
        }

        if (theCalcUI.skinScale == 0.0) {
            theCalcUI.skinScale = 1.0;
        }

        if (theCalcUI.skinEnabled) {
            rect.setBounds(0, 0, Gui.SKIN_WIDTH, Gui.SKIN_HEIGHT);
        } else {
            rect.setBounds(0, 0, 96 * theCalcUI.scale, 64 * theCalcUI.scale);
        }

        if (SwingUtilities.isEventDispatchThread()) {
            constructUI(theCalcUI, false, startPoint);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    /** Run. */
                    @Override
                    public void run() {
                        constructUI(theCalcUI, false, startPoint);
                    }
                });
            } catch (final InvocationTargetException ex) {
                LoggedObject.LOG.warning("Failed to build calculator panel", ex);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        calc.setSpeed(100);
        final AbstractLCDBase lcd = calc.getCPU().getPIOContext().getLcd();
        if (lcd instanceof LCD) {
            ((LCD) lcd).setListener(theCalcUI);
        } else if (lcd instanceof ColorLCD) {
            ((ColorLCD) lcd).setListener(theCalcUI);
        }

        theCalcUI.refresh();

        return theCalcUI;
    }

    /**
     * Constructs the UI.
     *
     * @param theCalcUI  the CalcUI
     * @param buildFrame true to build the frame; false to build only the panel
     * @param startPoint the start point at which to place the main frame
     */
    private static void constructUI(final CalcUI theCalcUI, final boolean buildFrame, final Point startPoint) {

        final CalculatorPanel calcPanel = new CalculatorPanel(theCalcUI);
        theCalcUI.calculatorPanel = calcPanel;

        if (buildFrame) {
            theCalcUI.mainFrame = new MainFrame(theCalcUI, calcPanel);
            if (Math.abs(startPoint.getX() + 1.0) < 0.001) {
                theCalcUI.getMainFrame().setLocation(startPoint);
            }
        }
    }

    /**
     * Causes a repaint of the calculator panel.
     */
    public void repaint() {

        this.calculatorPanel.repaint();
    }

    /**
     * Called when the calculator changes state and the UI needs to update to reflect the new state.
     */
    private void refresh() {

        if (this.skinEnabled) {

            final String skinRes;
            final String maskRes;
            final String keymapRes;

            if (this.customSkin) {
                skinRes = (String) Registry.queryWabbitKey("custom_skin");
                maskRes = (String) Registry.queryWabbitKey("custom_skinmask");
                keymapRes = (String) Registry.queryWabbitKey("keymap_path");
            } else {
                // Set the skin based on the model
                switch (this.model) {
                    case TI_81:
                        skinRes = "ti-81.png";
                        maskRes = "";
                        keymapRes = "ti-81keymap.png";
                        break;
                    case TI_82:
                        skinRes = "ti-82.png";
                        maskRes = "";
                        keymapRes = "ti-82keymap.png";
                        break;
                    case TI_83:
                        skinRes = "ti-83.png";
                        maskRes = "";
                        keymapRes = "ti-83keymap.png";
                        break;
                    case TI_85:
                        skinRes = "ti-85.png";
                        maskRes = "";
                        keymapRes = "ti-85keymap.png";
                        break;
                    case TI_86:
                        skinRes = "ti-86.png";
                        maskRes = "";
                        keymapRes = "ti-86keymap.png";
                        break;
                    case TI_73:
                        skinRes = "ti-73.png";
                        maskRes = "";
                        keymapRes = "ti-83+keymap.png";
                        break;
                    case TI_83P:
                        skinRes = "ti-83+.png";
                        maskRes = "";
                        keymapRes = "ti-83+keymap.png";
                        break;
                    case TI_83PSE:
                        skinRes = "ti-83+se.png";
                        maskRes = "";
                        keymapRes = "ti-83+keymap.png";
                        break;
                    case TI_84P:
                        skinRes = "ti-84+.png";
                        maskRes = "";
                        keymapRes = "ti-84+sekeymap.png";
                        break;
                    case TI_84PSE:
                        skinRes = "ti-84+se.png";
                        maskRes = "ti-84+se_skinmask.png";
                        keymapRes = "ti-84+sekeymap.png";
                        break;
                    case TI_84PCSE:
                        skinRes = "ti-84+cse.png";
                        maskRes = "ti-84+cse_skinmask.png";
                        keymapRes = "ti-84+csekeymap.png";
                        break;

                    case INVALID_MODEL:
                    default:
                        skinRes = "";
                        maskRes = "";
                        keymapRes = "";
                        break;
                }
            }

            // For a custom skin, need to load from a different location, so "loadSkin" will not work.

            // Custom skin path.
            // Custom key map path.
            this.skinImage = Gui.loadSkin(skinRes);
            this.skinMask = Gui.loadSkin(maskRes);
            this.keymapImage = Gui.loadSkin(keymapRes);

            // Apply skin scale to generate a rendered skin
            if (this.skinImage == null || this.keymapImage == null) {
                LoggedObject.LOG.warning("Unable to load skin image - disabling skin");
                this.skinEnabled = false;
            } else {
                // Use the keymap to identify the location and size of the LCD and each of the keys in the skin
                int firstRow = -1;
                int lastRow = -1;
                int firstCol = -1;
                int lastCol = -1;
                final int keymapWidth = this.keymapImage.getWidth();
                final int keymapHeight = this.keymapImage.getHeight();
                for (int x = 0; x < keymapWidth; ++x) {
                    for (int y = 0; y < keymapHeight; ++y) {
                        final int rgb = this.keymapImage.getRGB(x, y);

                        // Pure red means LCD region
                        if ((rgb & 0x00FFFFFF) == 0x00FF0000) {
                            if (firstRow == -1) {
                                firstRow = y;
                            }
                            lastRow = Math.max(lastRow, y);
                            if (firstCol == -1) {
                                firstCol = x;
                            }
                            lastCol = Math.max(lastCol, x);
                        } else if ((rgb & 0x00FF0000) == 0) {
                            // Not red or white, so a key
                            final int group = (rgb >> 12) & 0x07;
                            final int bit = (rgb >> 4) & 0x07;
                            final Rectangle rect = this.keyRects[group][bit];
                            if (rect.x == 0) {
                                rect.x = x;
                            } else if (x < rect.x) {
                                rect.x = x;
                            }
                            if ((rect.x + rect.width) < x) {
                                rect.width = x - rect.x;
                            }
                            if (rect.y == 0) {
                                rect.y = y;
                            } else if (y < rect.y) {
                                rect.y = y;
                            }
                            if ((rect.y + rect.height) < y) {
                                rect.height = y - rect.y;
                            }
                        }
                    }
                }

                if (firstRow == -1) {
                    LoggedObject.LOG.warning("No red found in keymap!");
                    this.lcdRect.setBounds(0, 0, HardwareConstants.LCD_WIDTH, HardwareConstants.LCD_HEIGHT);
                } else {
                    this.lcdRect.setBounds(firstCol, firstRow, lastCol - firstCol + 1, lastRow - firstRow + 1);
                }

                // Basic sizing: half the skin image size...
                // resizeSkin(this.skinImage.getWidth() / 2, this.skinImage.getHeight() / 2);

                // More sophisticated: Actual calculator is about 7.5" tall, so make target about
                // 30% larger, so 7.5 * 96ppi * 1.3 = 936
                final double aspect = (double) this.skinImage.getWidth() / (double) this.skinImage.getHeight();
                resizeSkin((int) (936.0 * aspect), 936);
            }
        }

        if (!this.skinEnabled) {

            int lcdScale = 1;
            final Object obj = Registry.queryWabbitKey("screen_scale");
            if (obj instanceof Integer) {
                lcdScale = ((Integer) obj).intValue();
            }

            // Hide the skin and position the LCD image in the upper left corner
            this.lcdRect.setBounds(0, 0, HardwareConstants.LCD_WIDTH * lcdScale,
                    HardwareConstants.LCD_HEIGHT * lcdScale);
        }

        refreshCalcPanel();
    }

    /**
     * Called when the calculator changes state and the UI needs to update to reflect the new state.
     */
    private void refreshCalcPanel() {

        if (SwingUtilities.isEventDispatchThread()) {
            this.calculatorPanel.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(this.calculatorPanel);
            } catch (final InvocationTargetException ex) {
                LoggedObject.LOG.warning("Exception refreshing skin panel", ex);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Finds a suitable skin scale and size nearest a given target size, subject to the limits of desktop size and
     * insets. The key map image and LCD rectangle are scaled to match.
     *
     * @param targetWidth  the target width
     * @param targetHeight the target height
     */
    void resizeSkin(final int targetWidth, final int targetHeight) {

        // LOG.info("Resize skin to " + targetWidth + " x " + targetHeight);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        final int maxHeight;
        final int maxWidth;

        // Make sure size won't exceed screen dimensions, but allow 3% cutoff in case round-off
        // forces calculator to be smaller when it need not be.
        final GraphicsConfiguration gc = this.calculatorPanel.getGraphicsConfiguration();
        if (gc == null) {
            maxHeight = Math.min(targetHeight, screen.height) * 103 / 100;
            maxWidth = Math.min(targetWidth, screen.width) * 103 / 100;
        } else {
            final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            maxHeight =
                    Math.min(targetHeight, screen.height - insets.top - insets.bottom) * 103 / 100;
            maxWidth = Math.min(targetWidth, screen.width - insets.left - insets.right) * 103 / 100;
        }
        // LOG.info(" Max sizes " + maxWidth + " x " + maxHeight);

        // The unscaled skin image is sized so that the screen would be scaled by a factor of 4.

        // The only scales we permit are those that leave the screen an integer multiple of its
        // ordinary resolution, so 1/4, 2/4, 3/4, ..., 7/4, 8/4 (max possible)

        final int skinWidth = this.skinImage.getWidth();
        final int skinHeight = this.skinImage.getHeight();
        if (skinHeight << 1 <= maxHeight && skinWidth << 1 <= maxWidth) {
            this.skinScale = 2.0;
        } else if (skinHeight * 7 / 8 <= maxHeight && skinWidth * 7 / 8 <= maxWidth) {
            this.skinScale = 1.75;
        } else if (skinHeight * 3 / 2 <= maxHeight && skinWidth * 3 / 2 <= maxWidth) {
            this.skinScale = 1.5;
        } else if (skinHeight * 5 / 4 <= maxHeight && skinWidth * 5 / 4 <= maxWidth) {
            this.skinScale = 1.25;
        } else if (skinHeight <= maxHeight && skinWidth <= maxWidth) {
            this.skinScale = 1.0;
        } else if (skinHeight * 3 / 4 <= maxHeight && skinWidth * 3 / 4 <= maxWidth) {
            this.skinScale = 0.75;
        } else if (skinHeight * 3 / 5 <= maxHeight && skinWidth * 3 / 5 <= maxWidth) {
            this.skinScale = 0.6;
        } else if (skinHeight / 2 <= maxHeight && skinWidth / 2 <= maxWidth) {
            this.skinScale = 0.5;
        } else if (skinHeight * 2 / 5 <= maxHeight && skinWidth * 2 / 5 <= maxWidth) {
            this.skinScale = 0.4;
        } else if (skinHeight / 3 <= maxHeight && skinWidth / 3 <= maxWidth) {
            this.skinScale = 0.333333333333;
        } else {
            this.skinScale = 0.25;
        }

        // LOG.info("Scale = " + this.skinScale + " width = "
        // + (this.skinImage.getWidth() * this.skinScale) + " height = "
        // + (this.skinImage.getHeight() * this.skinScale));

        // Apply the face color as background anywhere the skin has nonzero alpha
        final Color faceColor;
        final Object colorInt = Registry.queryWabbitKey("faceplate_color");
        if (colorInt instanceof Integer) {
            faceColor = new Color(((Integer) colorInt).intValue());
        } else {
            faceColor = new Color(40, 40, 40);
        }

        if (Math.abs(this.skinScale - 1.0) < 0.001) {
            // No scaling
            this.renderedSkinImage = new BufferedImage(skinWidth, skinHeight, BufferedImage.TYPE_INT_ARGB);
            this.renderedKeymapImage = new BufferedImage(this.keymapImage.getWidth(), this.keymapImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            // If there is a skin mask, use it to apply the skin color
            if (this.skinMask != null) {
                for (int x = 0; x < skinWidth; ++x) {
                    for (int y = 0; y < skinHeight; ++y) {
                        final int argb = this.skinMask.getRGB(x, y);
                        if (((argb >> 24) & 0xFF) > 0x7F) {
                            this.renderedKeymapImage.setRGB(x, y, faceColor.getRGB());
                        }
                    }
                }
            }

            this.renderedSkinImage.getGraphics().drawImage(this.skinImage, 0, 0, null);
            this.renderedKeymapImage.getGraphics().drawImage(this.keymapImage, 0, 0, null);
        } else {
            final int newWidth = (int) Math.round((double) skinWidth * this.skinScale);
            final int newHeight = (int) Math.round((double) skinHeight * this.skinScale);

            if (this.renderedSkinImage == null || newWidth != this.renderedSkinImage.getWidth()) {
                this.renderedSkinImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                this.renderedKeymapImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

                // If there is a skin mask, use it to apply the skin color
                if (this.skinMask != null) {
                    final int faceRgb = faceColor.getRGB();

                    // Find the average alpha of the original skin...
                    long total = 0L;
                    int count = 0;
                    for (int x = 0; x < newWidth; ++x) {
                        for (int y = 0; y < newHeight; ++y) {
                            final int argb = this.skinMask.getRGB(x * this.skinMask.getWidth() / newWidth,
                                    y * this.skinMask.getHeight() / newHeight);

                            if (argb == 0xFFFFFFFF) {
                                total = total + (long) (this.skinImage.getRGB(x * skinWidth / newWidth,
                                        y * skinHeight / newHeight) >> 24) & 0x00FFL;
                                ++count;
                            }
                        }
                    }
                    final int average = (int) (total / (long) count);

                    // LOG.info("Average alpha = " + average);

                    for (int x = 0; x < newWidth; ++x) {
                        for (int y = 0; y < newHeight; ++y) {
                            final int argb = this.skinMask.getRGB(x * this.skinMask.getWidth() / newWidth,
                                    y * this.skinMask.getHeight() / newHeight);

                            if (argb == 0xFFFFFFFF) {
                                if (average > 190) {
                                    this.renderedSkinImage.setRGB(x, y, faceRgb);
                                } else {
                                    this.renderedSkinImage.setRGB(x, y, (average << 24) + (faceRgb & 0x00FFFFFF));
                                }
                            }
                        }
                    }
                }

                Graphics2D g2d = (Graphics2D) this.renderedSkinImage.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(this.skinImage, 0, 0, newWidth, newHeight, null);

                g2d = (Graphics2D) this.renderedKeymapImage.getGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(this.keymapImage, 0, 0, newWidth, newHeight, null);
            }
        }

        // In the rendered keymap, change 0x00FF**** pixels to clear, others to black
        final int keymapWidth = this.renderedKeymapImage.getWidth();
        final int keymapHeight = this.renderedKeymapImage.getHeight();
        for (int x = 0; x < keymapWidth; ++x) {
            for (int y = 0; y < keymapHeight; ++y) {
                final int rgb = this.renderedKeymapImage.getRGB(x, y);
                if ((rgb & 0x00FF0000) == 0x00FF0000) {
                    this.renderedKeymapImage.setRGB(x, y, 0x00FFFFFF);
                } else {
                    this.renderedKeymapImage.setRGB(x, y, 0xFF000000);
                }
            }
        }

        this.skinRect.setBounds(0, 0, this.renderedSkinImage.getWidth(), this.renderedSkinImage.getHeight());
    }

    /**
     * Called when main frame is opened.
     *
     * @param e the window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {

        positionWindow();
    }

    /**
     * Positions the window on the screen when it is created.
     */
    private void positionWindow() {

        if (this.mainFrame != null) {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension size = this.mainFrame.getSize();

            // Find bounds of all existing open calculators
            final int count = Launcher.getNumCalcs();
            final List<Rectangle> bounds = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                if (i != this.slot) {
                    final CalcUI ui = Launcher.getCalcUI(i);
                    if (ui != null) {
                        bounds.add(ui.getMainFrame().getBounds());
                    }
                }
            }

            if (bounds.isEmpty()) {
                // Place first calculator at configured start point
                final Point pt = getStartPoint();
                if (pt.x == -1 && pt.y == -1) {
                    this.mainFrame.setLocation(screen.width - size.width, 0);
                } else {
                    this.mainFrame.setLocation(pt);
                }
            } else {
                // Place subsequent calculators by tiling
                final Rectangle myBounds = new Rectangle(0, 0, size.width, size.height);
                boolean goodLoc = false;

                // Try to tile to the right of an existing calculator
                for (int i = 0; i < bounds.size() && !goodLoc; ++i) {
                    final Rectangle test = bounds.get(i);
                    myBounds.x = test.x + test.width + 1;
                    myBounds.y = test.y;

                    if (myBounds.x + myBounds.width < screen.width) {
                        goodLoc = true;
                        for (final Rectangle test2 : bounds) {
                            if (myBounds.intersects(test2)) {
                                goodLoc = false;
                                break;
                            }
                        }
                    }
                }

                // Try to tile to the left of an existing calculator
                for (int i = 0; i < bounds.size() && !goodLoc; ++i) {
                    final Rectangle test = bounds.get(i);
                    myBounds.x = test.x - size.width - 1;
                    myBounds.y = test.y;

                    if (myBounds.x >= 0) {
                        goodLoc = true;
                        for (final Rectangle test2 : bounds) {
                            if (myBounds.intersects(test2)) {
                                goodLoc = false;
                                break;
                            }
                        }
                    }
                }

                // Find any space where we don't overlap a calculator
                for (int i = 0; i < screen.width - size.width && !goodLoc; i += 8) {
                    myBounds.x = i;
                    for (int j = 0; j < screen.height - size.height && !goodLoc; j += 8) {
                        myBounds.y = j;

                        goodLoc = true;
                        for (final Rectangle test2 : bounds) {
                            if (myBounds.intersects(test2)) {
                                goodLoc = false;
                                break;
                            }
                        }
                    }
                }

                // Find any space where we don't exactly overlap a calculator, but partial overlap
                // OK
                for (int j = 16; j < screen.height - size.height && !goodLoc; j += 16) {
                    myBounds.y = j;
                    for (int i = 16; i < screen.width - size.width && !goodLoc; i += 16) {
                        myBounds.x = i;

                        goodLoc = true;
                        for (final Rectangle test2 : bounds) {
                            if (myBounds.x == test2.x && myBounds.y == test2.y) {
                                goodLoc = false;
                                break;
                            }
                        }
                    }
                }

                this.mainFrame.setLocation(myBounds.x, myBounds.y);
            }

            snapToEdges();
        }
    }

    /**
     * Snap the window to screen edges.
     */
    private void snapToEdges() {

        if (this.mainFrame != null) {
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            final Insets insets = Toolkit.getDefaultToolkit()
                    .getScreenInsets(this.mainFrame.getGraphicsConfiguration());

            final Dimension size = this.mainFrame.getSize();
            final Point loc = this.mainFrame.getLocation();

            // Snap to the left or right edge
            if (Math.abs(loc.x - insets.left) < SNAP_DIST) {
                if (loc.x != insets.left) {
                    this.mainFrame.setLocation(insets.left, loc.y);
                }
            } else if (Math.abs(loc.x + size.width - (screen.width - insets.right)) < SNAP_DIST) {
                if (loc.x + size.width != screen.width - insets.right) {
                    this.mainFrame.setLocation(screen.width - size.width - insets.right, loc.y);
                }
            }

            // Snap to the top or bottom edge
            if (Math.abs(loc.y - insets.top) < SNAP_DIST) {
                if (loc.y != insets.top) {
                    this.mainFrame.setLocation(loc.x, insets.top);
                }
            } else if (Math.abs(loc.y + size.height - (screen.height - insets.bottom)) < SNAP_DIST) {
                if (loc.y + size.height != screen.height - insets.bottom) {
                    this.mainFrame.setLocation(loc.x, screen.height - size.height - insets.bottom);
                }
            }
        }
    }

    /**
     * Called when a frame is closing.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        // If the main window is closing, close all associated frames first
        if (e.getSource() == this.mainFrame) {
            closeSubFrames();
        }

        // if (Boolean.TRUE.equals(Registry.queryWabbitKey("exit_save_state"))) {
        // // Save state
        // }

        // At a minimum, save last screen X, Y coordinates
    }

    /**
     * Called when main frame is iconified.
     *
     * @param e the window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {

        // When the main frame is iconified, we hide the LCD and small button frames if present
        if (e.getSource() == this.mainFrame) {

            if (this.keyListDialog != null) {
                this.keyListDialog.setVisible(false);
            }
            if (this.varListDialog != null) {
                this.varListDialog.setVisible(false);
            }
        }

        // Halt the calculator
    }

    /**
     * Called when main frame is deiconified.
     *
     * @param e the window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {

        // When the main frame is iconified, we hide the LCD and small button frames if present
        if (e.getSource() == this.mainFrame) {

            if (this.keyListDialog != null) {
                this.keyListDialog.setVisible(true);
            }
            if (this.varListDialog != null) {
                this.varListDialog.setVisible(true);
            }
        }

        // Un-halt the calculator
    }

    /**
     * Called when main frame is resized.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        if (this.mainFrame != null) {
            final int height = this.mainFrame.getContentPane().getHeight();
            final int width = this.mainFrame.getContentPane().getWidth();

            // Find the desired aspect ratio and snap to the nearest acceptable size
            if (this.skinEnabled) {
                if (this.skinImage != null && height != 0) {
                    resizeSkin(width, height);
                }
            }

            // If we're resizing, based on a skin change, and the window was at the right edge of
            // the screen before the change, position it at the right edge of the screen after the
            // change.
            if (this.resizing != 0) {

                final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                final Insets insets = Toolkit.getDefaultToolkit()
                        .getScreenInsets(this.mainFrame.getGraphicsConfiguration());

                final Point loc = this.mainFrame.getLocation();
                final Dimension size = this.mainFrame.getSize();

                if ((this.resizing & 0x02) == 0x02) {
                    // Keep window on the right edge
                    final int curRight = loc.x + size.width;
                    final int wantRight = screen.width - insets.right;
                    if (curRight != wantRight) {
                        this.mainFrame.setLocation(wantRight - size.width, loc.y);
                    }
                }

                if ((this.resizing & 0x04) == 0x04) {
                    // Keep window on the bottom edge
                    final int curBottom = loc.y + size.height;
                    final int wantBottom = screen.height - insets.bottom;
                    if (curBottom != wantBottom) {
                        this.mainFrame.setLocation(loc.x, wantBottom - size.height);
                    }
                }

                this.resizing = 0;
            }

            snapToEdges();
        }
    }

    /**
     * Called when main frame is moved.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        snapToEdges();
    }

    /**
     * Called when main frame is shown.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Called when main frame is hidden.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }
}
