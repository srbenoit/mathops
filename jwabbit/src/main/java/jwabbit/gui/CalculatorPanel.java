package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.CalcBasicAction;
import jwabbit.CalcCopyTextAction;
import jwabbit.CalcKeyKeyPressAction;
import jwabbit.CalcKeyKeyReleaseAction;
import jwabbit.CalcKeyPressAction;
import jwabbit.CalcKeyReleaseAction;
import jwabbit.CalcSetProfileAction;
import jwabbit.CalcThread;
import jwabbit.ECalcAction;
import jwabbit.Launcher;
import jwabbit.core.EnumCalcModel;
import jwabbit.hardware.KeyName;
import jwabbit.hardware.KeyNames;
import jwabbit.iface.Calc;
import jwabbit.iface.CalcState;
import jwabbit.iface.EnumKeypadState;
import jwabbit.log.LoggedPanel;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * A panel that serves as the content pane of a {@code MainFrame} and displays the skin of the current calculator (if
 * skin is enabled) or nothing (if skin disabled). This panel processes mouse clicks and keystrokes. A mouse press on a
 * button causes that button to be pressed, while a mouse press anywhere else can allow the window to be dragged.
 */
public final class CalculatorPanel extends LoggedPanel implements Runnable, KeyListener, Transferable,
        DragSourceListener, DragGestureListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7578114461407508806L;

    /** A zero-length integer array. */
    private static final int[] ZERO_LEN_INT_ARR = new int[0];

    /** A zero-length key name array. */
    private static final KeyName[] ZERO_LEN_KEYNAME_ARR = new KeyName[0];

    /** Allow the screen area to be dragged as an image. */
    private final DragSource dragSource;

    /** The owning {@code CalcUI}. */
    private final CalcUI calcUi;

    /** The group of the key pressed until the mouse is released. */
    private int pressedGroup;

    /** The bit of the key pressed until the mouse is released. */
    private int pressedBit;

    /** Object on which to synchronize LCD update. */
    private final Object synch;

    /** The LCD active state. */
    private boolean lcdActive;

    /** The LCD contrast. */
    private int lcdContrast;

    /** The LCD screen data, as of the last update from the calculator thread. */
    private int[] lcdData;

    /** Flag to allow toggling of visibility of calculator. */
    private boolean showing;

    /** The alpha composite used to darken keys that are pressed. */
    private final Composite alpha;

    /**
     * Constructs a new {@code CalculatorPanel}.
     *
     * @param theOwner the owning {@code CalcUI}
     */
    CalculatorPanel(final CalcUI theOwner) {

        super();
        setLayout(null);

        this.synch = new Object();
        this.calcUi = theOwner;
        this.pressedGroup = -1;
        this.pressedBit = -1;
        this.lcdActive = false;
        this.lcdData = ZERO_LEN_INT_ARR;
        this.showing = true;

        if (this.calcUi.isSkinEnabled() && this.calcUi.getRectSkin() != null) {
            setPreferredSize(new Dimension(this.calcUi.getRectSkin().width, this.calcUi.getRectSkin().height));
        } else if (this.calcUi.getLCDRect() != null) {
            setPreferredSize(new Dimension(this.calcUi.getLCDRect().width, this.calcUi.getLCDRect().height));
        }

        this.alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);

        run();

        setFocusable(true);
        addMouseListener(this);
        addKeyListener(this);

        // Handler for drag & drop transfers.
        final TransferHandler xfer = new XferHandler(this);
        setTransferHandler(xfer);

        getDropTarget().setActive(false);

        this.dragSource = new DragSource();
        this.dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
    }

    /**
     * Creates a new calculator, its associated CalcUI and thread, and generates its panel.
     *
     * @return the panel instance
     */
    public static CalculatorPanel getInstance() {

        final CalcUI ui = Gui.createCalcRegisterEvents(false);

        return ui == null ? null : ui.getCalculatorPanel();
    }

    /**
     * Gets the owning {@code CalcUI}.
     *
     * @return the {@code CalcUI}.
     */
    public CalcUI getCalcUI() {

        return this.calcUi;
    }

    /**
     * Called when the LCD is updated.
     *
     * @param active   the LCD active state
     * @param contrast the LCD contrast
     * @param data     the LCD image data
     */
    void updateLcd(final boolean active, final int contrast, final int[] data) {

        // Called from the calculator thread, so synchronize access
        synchronized (this.synch) {
            this.lcdActive = active;
            this.lcdContrast = contrast;
            if (this.lcdData.length == data.length) {
                System.arraycopy(data, 0, this.lcdData, 0, data.length);
            } else {
                this.lcdData = data.clone();
            }

            repaint();
        }
    }

    /**
     * Called when the calculator changes state and the UI needs to update to reflect the new state.
     *
     * <p>
     * This is implemented as a "run" method, so it can be called on the AWT event thread using SwingUtilities if we're
     * not already in that thread.
     */
    @Override
    public void run() {

        final Rectangle bounds;

        if (this.calcUi != null) {
            if (this.lcdData.length == 0) {

                final BitmapInfo info = this.calcUi.getModel().ordinal() >= EnumCalcModel.TI_84PCSE.ordinal()
                        ? Gui.COLORBI : Gui.BI;
                final int size = info.getWidth() * info.getHeight() * (info.getBitCount() + 7) / 8;
                this.lcdData = new int[size];
            }

            if (this.calcUi.isSkinEnabled()) {
                bounds = this.calcUi.getRectSkin();
            } else {
                bounds = this.calcUi.getLCDRect();
            }

            // For either skin or LCD, use (x,y) as left/top margin, and assume an equal right/bottom margin
            setPreferredSize(new Dimension(bounds.width, bounds.height));
            setSize(getPreferredSize());

            // during construction of this panel, owner will not yet have main frame, so check for that
            if (this.calcUi.getMainFrame() != null) {

                final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                final Point loc = this.calcUi.getMainFrame().getLocation();
                final Dimension size = this.calcUi.getMainFrame().getSize();

                final GraphicsConfiguration gc = getGraphicsConfiguration();
                if (gc == null) {
                    // Panel not yet added to container
                    this.calcUi.setResizing(0);
                } else {
                    final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
                    final boolean isOnRight = loc.x + size.width == screen.width - insets.right;
                    final boolean isOnBottom = loc.y + size.height == screen.height - insets.bottom;

                    this.calcUi.setResizing(1 + (isOnRight ? 2 : 0) + (isOnBottom ? 4 : 0));
                }
                if (this.calcUi.getMainFrame() != null) {
                    this.calcUi.getMainFrame().pack();
                }
            }
        }
    }

    /**
     * Override "setSize" to print values.
     */
    @Override
    public void setSize(final int width, final int height) {

        super.setSize(width, height);
    }

    /**
     * Toggle the visibility of the calculator.
     *
     * @param show true to show, false to hide
     */
    public void showCalculator(final boolean show) {

        this.showing = show;

        // See how large we can make the calculator...
        BufferedImage skin = this.calcUi.getSkin();
        final Dimension size = getSize();
        final int targetWidth = size.height * skin.getWidth() / skin.getHeight();
        this.calcUi.resizeSkin(targetWidth, size.height);

//         LOG.info("Size: " + size);
//         LOG.info("Target size: " + targetWidth + "x" + size.height);
//         LOG.info("Skin rect size: " + skin.getWidth() + "x" + skin.getHeight());

        skin = this.calcUi.getRenderedSkin();

        final Dimension newSize = new Dimension(skin.getWidth(), skin.getHeight());

//        LOG.info("Rendered skin size: " + newSize);

        setPreferredSize(newSize);
        setMinimumSize(newSize);
        setSize(newSize);

        final Container toplevel = getTopLevelAncestor();
        if (toplevel instanceof final JFrame frm) {
            frm.pack();
        }

        repaint();
    }

    /**
     * Draws the panel.
     *
     * @param g the {@code Graphics} to which to paint
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        if (this.calcUi == null) {

            final String str = "Calculator emulator unavailable";
            final FontMetrics met = g.getFontMetrics();
            final Rectangle2D bounds = met.getStringBounds(str, g);

            g.drawString(str, 5, 5 + (int) bounds.getMaxY());
        } else if (this.showing) {
            final Rectangle bounds = this.calcUi.getRectSkin();
            final BufferedImage offscreen = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            final Graphics grx = offscreen.getGraphics();

            BufferedImage skin = this.calcUi.getRenderedSkin();
            if (skin == null) {
                skin = this.calcUi.getSkin();
            }

            if (skin == null) {
                renderLCD(grx, true);
            } else {
                grx.drawImage(skin, bounds.x, bounds.y, bounds.width, bounds.height, null);
                final Rectangle rect = getBounds();
                if (rect.width < bounds.width || rect.height < bounds.height) {
                    setPreferredSize(new Dimension(bounds.width, bounds.height));
                    invalidate();
                    if (this.calcUi.getMainFrame() != null) {
                        this.calcUi.getMainFrame().pack();
                    }
                }
                renderLCD(grx, false);

                // Darken any disabled keys
                final Calc calc = Launcher.getCalc(this.calcUi.getSlot());
                final String[] disabled = calc.getDisabledKeys();
                if (disabled != null) {
                    final KeyName[] names = switch (this.calcUi.getModel()) {
                        case TI_83P, TI_83PSE, TI_84P, TI_84PSE, TI_84PCSE -> KeyNames.KEY_NAMES_83P_83PSE_84PSE;
                        default -> ZERO_LEN_KEYNAME_ARR;
                    };

                    final EnumKeypadState state = CalcState.getKeypadState(calc);
                    for (final String s : disabled) {
                        for (final KeyName name : names) {
                            if (name.getState() == state && name.getName().equals(s)) {
                                // Render overlay twice to darken more than just pressed key
                                renderKeyOverlay(grx, name.getGroup(), name.getBit());
                                renderKeyOverlay(grx, name.getGroup(), name.getBit());
                            }
                        }
                    }
                }

                if (this.pressedGroup != -1) {
                    renderKeyOverlay(grx, this.pressedGroup, this.pressedBit);
                }
            }

            g.drawImage(offscreen, 0, 0, null);
        }
    }

    /**
     * Test the availability of the calculator.
     *
     * @return true if the calculator is showing, false otherwise
     */
    public boolean isCalculatorAvailable() {

        return this.calcUi != null;
    }

    /**
     * Get the last answer displayed on the calculator. This blocks the calling thread until the calculator thread posts
     * the answer, so this should NOT be called from the AWT event thread. To copy an answer to a Swing field, use
     * {@code copyAns}, which can be called from the AWT thread safely.
     *
     * @return the answer
     */
    public String getAns() {

        String ans = null;

        if (this.calcUi != null) {
            final int slot = this.calcUi.getSlot();
            final CalcThread thread = Launcher.getCalcThread(slot);
            thread.clearLastAnswer();
            thread.enqueueAction(new CalcBasicAction(ECalcAction.GET_LAST_ANSWER));

            for (int i = 0; i < 30 && ans == null; ++i) {
                try {
                    Thread.sleep(20L);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                ans = thread.getLastAnswer();
            }
        }

        return ans;
    }

    /**
     * Retrieves the current answer and copies it into a text field. This method is safe to call from the AWT event
     * thread. It will make the request of the calculator, store the field, and when the answer is retrieved, it will be
     * copied into the target field in the AWT thread by SwingUtilities.invokeLater.
     *
     * @param field the field to which to copy the answer
     */
    public void copyAns(final JTextComponent field) {

        if (this.calcUi != null) {
            final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
            thread.clearLastAnswer();
            thread.enqueueAction(new CalcCopyTextAction(field));
        }
    }

    /**
     * Sets the profile for the calculator.
     *
     * @param profileName the profile name
     */
    public void setProfile(final String profileName) {

        if (this.calcUi != null) {
            final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
            thread.enqueueAction(new CalcSetProfileAction(profileName));
        }
    }

    /**
     * Generates the scaled rectangle surrounding a key.
     *
     * @param group the key group
     * @param bit   the key bit
     * @return the scaled rectangle
     */
    private Rectangle scaledKeyRect(final int group, final int bit) {

        final Rectangle result;

        if (this.calcUi == null) {
            result = new Rectangle();
        } else {
            final Rectangle rect = this.calcUi.getKeyRect(group, bit);
            final double scale = this.calcUi.getSkinScale();

            result = new Rectangle((int) ((double) rect.x * scale), (int) ((double) rect.y * scale),
                    (int) ((double) rect.width * scale), (int) ((double) rect.height * scale));
        }

        return result;
    }

    /**
     * Paints the panel.
     *
     * <p>
     * WABBITEMU SOURCE: gui/guilcd.c, "PaintLCD" function.
     *
     * @param grx        the {@code Graphics} to which to paint
     * @param adjustSize true to adjust the window size if too small for the LCD
     */
    private void renderLCD(final Graphics grx, final boolean adjustSize) {

        // get data that might change based on updates from the calculator thread
        final boolean active;
        final int contrast;
        final int[] data;
        synchronized (this.synch) {
            active = this.lcdActive;
            contrast = this.lcdContrast;
            data = this.lcdData;
        }

        final Rectangle rc = this.calcUi.getLCDRect();
        final double scale = this.calcUi.getSkinScale();

        final BitmapInfo info = Gui.getLCDColorPalette(this.calcUi.getModel(), active, contrast);
        final int clientScaleHeight = (int) Math.round(scale * (double) rc.height);
        final int clientScaleWidth = (int) Math.round(scale * (double) rc.width);

        final BufferedImage lcdBitmap;
        final int h = info.getHeight();
        final int w = info.getWidth();

        if (info.getBitCount() == 24) {

            lcdBitmap = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            if (data.length >= w * info.getHeight() * 3) {
                int index = 0;
                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        final int rgb = (data[index] & 0x000000FF) + ((data[index + 1] << 8) & 0x0000FF00)
                                + ((data[index + 2] << 16) & 0x00FF0000) + 0xFF000000;
                        lcdBitmap.setRGB(x, y, rgb);
                        index += 3;
                    }
                }
            } else {
                LOG.warning("LCD screen data too small for color LCD " + data.length + "/" + (w * h * 3));
            }
        } else {
            lcdBitmap = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            if (data.length >= w * h) {
                int index = 0;

                for (int y = 0; y < h; ++y) {
                    for (int x = 0; x < w; ++x) {
                        final int colorIndex = data[index] & 0x00FF;
                        final int rgb = info.getColor(colorIndex).getRGB();
                        lcdBitmap.setRGB(x, y, rgb);
                        ++index;
                    }
                }
            } else {
                LOG.warning("LCD screen data too small for grayscale LCD, " + data.length);
            }
        }

        BufferedImage bitmap = lcdBitmap;

        if (clientScaleWidth != w || clientScaleHeight != h) {
            final BufferedImage scaledBitmap =
                    new BufferedImage(clientScaleWidth, clientScaleHeight, BufferedImage.TYPE_INT_ARGB);

            final int unscaledWidth = h * rc.width / rc.height;

            final Graphics scaledGraphics = scaledBitmap.getGraphics();

            // Smooth the color LCD's paint
            if (info.getBitCount() == 24 && scaledGraphics instanceof final Graphics2D g2d) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }

            scaledGraphics.drawImage(lcdBitmap, 0, 0, clientScaleWidth, clientScaleHeight, 0, 0, unscaledWidth, h,
                    null);
            bitmap = scaledBitmap;
        }

        grx.drawImage(bitmap, (int) Math.round(scale * (double) rc.x), (int) Math.round(scale * (double) rc.y), null);

        if (adjustSize) {
            final Rectangle rect = getBounds();
            if (rect.width < rc.width || rect.height < rc.height) {
                setPreferredSize(new Dimension(rc.width, rc.height));
                invalidate();
                this.calcUi.getMainFrame().pack();
            }
        }
    }

    /**
     * Renders a darkened overlay on a key.
     *
     * @param group the key group
     * @param bit   the key bit
     * @param grx   the {@code Graphics} to which to paint
     */
    private void renderKeyOverlay(final Graphics grx, final int group, final int bit) {

        final Rectangle rect = scaledKeyRect(group, bit);
        final BufferedImage img = this.calcUi.getRenderedKeymap();

        if (grx instanceof final Graphics2D g2d) {
            final Composite orig = g2d.getComposite();
            g2d.setComposite(this.alpha);
            g2d.drawImage(img, rect.x, rect.y, rect.x + rect.width, rect.y + rect.height, rect.x, rect.y,
                    rect.x + rect.width, rect.y + rect.height, null);
            g2d.setComposite(orig);
        }
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
        if (thread == null) {
            return;
        }

        if (this.calcUi.isSkinEnabled()) {
            // Scale the (x, y) coordinates down to coordinates in the original (unscaled) skin image
            final BufferedImage scaled = this.calcUi.getRenderedSkin();
            final BufferedImage unscaled = this.calcUi.getSkin();
            final BufferedImage keymap = this.calcUi.getKeymap();

            if (scaled != null && unscaled != null && keymap != null) {
                final double scale = (double) unscaled.getHeight() / (double) scaled.getHeight();

                final int unscaledX = e.getX() - this.calcUi.getRectSkin().x;
                final int unscaledY = e.getY() - this.calcUi.getRectSkin().y;

                final int scaledX = (int) Math.round((double) unscaledX * scale);
                final int scaledY = (int) Math.round((double) unscaledY * scale);

                if (scaledX < keymap.getWidth() && scaledY < keymap.getHeight()) {

                    // Get the color of the pixel in the keymap at the click point
                    final int color = keymap.getRGB(scaledX, scaledY);

                    if ((color & 0x00FF0000) == 0) {
                        // not red screen or white backdrop, so must be a key
                        final int group = (color >> 12) & 0x0F;
                        final int bit = (color >> 4) & 0x0F;

                        // See if the selected key is disabled
                        boolean hitKeyDisabled = false;
                        final Calc calc = Launcher.getCalc(this.calcUi.getSlot());
                        final String[] disabled = calc.getDisabledKeys();
                        if (disabled != null) {
                            final KeyName[] names = switch (this.calcUi.getModel()) {
                                case TI_83P, TI_83PSE, TI_84P, TI_84PSE, TI_84PCSE ->
                                        KeyNames.KEY_NAMES_83P_83PSE_84PSE;
                                default -> ZERO_LEN_KEYNAME_ARR;
                            };

                            final EnumKeypadState state = CalcState.getKeypadState(calc);
                            outer:
                            for (final String s : disabled) {
                                for (final KeyName name : names) {
                                    if (name.getState() == state && name.getName().equals(s)
                                            && name.getGroup() == group && name.getBit() == bit) {

                                        hitKeyDisabled = true;
                                        break outer;
                                    }
                                }
                            }
                        }

                        if (!hitKeyDisabled) {
                            this.pressedGroup = group;
                            this.pressedBit = bit;

                            repaint(scaledKeyRect(this.pressedGroup, this.pressedBit));

                            thread.enqueueAction(new CalcKeyPressAction(group, bit));
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked when a mouse button has been released on a component.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
        if (thread == null) {
            return;
        }

        if (this.pressedGroup != -1) {
            thread.enqueueAction(new CalcKeyReleaseAction(this.pressedGroup, this.pressedBit));
            final int oldGroup = this.pressedGroup;
            final int oldBit = this.pressedBit;
            this.pressedGroup = -1;
            this.pressedBit = -1;
            repaint(scaledKeyRect(oldGroup, oldBit));
        }
    }

    /**
     * Handles key typed events.
     *
     * @param e the key event
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        // No action
    }

    /**
     * Handles key pressed events.
     *
     * @param e the key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {

        final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
        if (thread == null) {
            return;
        }

        thread.enqueueAction(new CalcKeyKeyPressAction(e.getKeyCode(), e.getKeyLocation()));
    }

    /**
     * Handles key released events.
     *
     * @param e the key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {

        final CalcThread thread = Launcher.getCalcThread(this.calcUi.getSlot());
        if (thread == null) {
            return;
        }

        thread.enqueueAction(new CalcKeyKeyReleaseAction(e.getKeyCode(), e.getKeyLocation()));
    }

    /**
     * Gets the list of transfer flavors supported for dragging from this object.
     *
     * @return the array of flavors (just the image flavor)
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {

        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    /**
     * Tests whether a data flavor is supported.
     *
     * @param flavor the flavor to test
     * @return true if flavor supported; false if not
     */
    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {

        return flavor.equals(DataFlavor.imageFlavor);
    }

    /**
     * Gets the transfer data in a particular flavor.
     *
     * @param flavor the flavor
     * @return the transfer data
     */
    @Override
    public Object getTransferData(final DataFlavor flavor) {

        if (!SwingUtilities.isEventDispatchThread()) {
            LOG.warning("getTransferData called, not in AWT event thread!!!");
        }

        final Object result;

        if (flavor.equals(DataFlavor.imageFlavor)) {
            final Rectangle rc = this.calcUi.getLCDRect();
            final double scale = this.calcUi.getSkinScale();

            final BufferedImage img = new BufferedImage((int) Math.round(scale * (double) rc.width),
                    (int) Math.round(scale * (double) rc.height), BufferedImage.TYPE_INT_ARGB);

            final int x = (int) Math.round(scale * (double) rc.x);
            final int y = (int) Math.round(scale * (double) rc.y);

            final Graphics grx = img.getGraphics();
            if (grx instanceof final Graphics2D g2d) {
                g2d.translate(-x, -y);
                renderLCD(g2d, false);
            }
            result = img;
        } else {
            result = null;
        }

        return result;
    }

    /**
     * Called when a drag enters the object.
     *
     * @param dsde the drag source drop event
     */
    @Override
    public void dragEnter(final DragSourceDragEvent dsde) {

        // No action
    }

    /**
     * Called when a drag passes over the object.
     *
     * @param dsde the drag source drop event
     */
    @Override
    public void dragOver(final DragSourceDragEvent dsde) {

        // No action
    }

    /**
     * Called when the drag action changes.
     *
     * @param dsde the drag source drop event
     */
    @Override
    public void dropActionChanged(final DragSourceDragEvent dsde) {

        // No action
    }

    /**
     * Called when a drag exits the object.
     *
     * @param dse the drag source event
     */
    @Override
    public void dragExit(final DragSourceEvent dse) {

        // No action
    }

    /**
     * Called when the drag finishes.
     *
     * @param dsde the drag source drop event
     */
    @Override
    public void dragDropEnd(final DragSourceDropEvent dsde) {

        repaint();
    }

    /**
     * Called when the drag starts.
     *
     * @param dge the drag gesture event
     */
    @Override
    public void dragGestureRecognized(final DragGestureEvent dge) {

        // Make sure the dragging started in the LCD screen area
        final Point where = dge.getDragOrigin();
        final Rectangle rc = this.calcUi.getLCDRect();
        final double scale = this.calcUi.getSkinScale();

        final int left = (int) ((double) rc.x * scale);
        final int top = (int) ((double) rc.y * scale);
        final int width = (int) ((double) rc.width * scale);
        final int height = (int) ((double) rc.height * scale);

        if (where.x >= left && where.x <= (left + width) && where.y >= top && where.y <= (top + height)) {
            this.dragSource.startDrag(dge, DragSource.DefaultCopyDrop, this, this);
        }
    }

    /**
     * A transfer handler.
     */
    private static final class XferHandler extends TransferHandler {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -9021422445782720426L;

        /** The source. */
        private final CalculatorPanel source;

        /**
         * Constructs a new {@code XferHandler}.
         *
         * @param theSource the source
         */
        XferHandler(final CalculatorPanel theSource) {

            super();

            this.source = theSource;
        }

        /**
         * Returns the type of transfer actions supported by the source.
         *
         * @param c the component holding the data to be transferred
         * @return {@code COPY}
         */
        @Override
        public int getSourceActions(final JComponent c) {

            return COPY_OR_MOVE;
        }

        /**
         * Creates a transferable.
         *
         * @param c the component
         */
        @Override
        public Transferable createTransferable(final JComponent c) {

            return this.source;
        }

        /**
         * This method is invoked after the export is complete. When the action is a MOVE, the data needs to be removed
         * from the source after the transfer is complete � this method is where any necessary cleanup occurs.
         *
         * @param source the source component
         * @param data   the data
         */
        @Override
        public void exportDone(final JComponent source, final Transferable data, final int action) {

            // No action
        }
    }
}
