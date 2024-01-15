package dev.mathops.app.teststation;

import dev.mathops.core.log.Log;
import dev.mathops.font.BundledFontManager;

import javax.swing.JDesktopPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.io.Serial;

/**
 * A background panel that can show the testing center name and testing station number.
 */
public final class BackgroundPane extends JDesktopPane implements MouseListener, KeyListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2678069115856307338L;


    /** Key command to view the log. */
    private static final char[] LOG_VIEW_CMD = "show the log".toCharArray();

    /** Color in which to paint the center name and station number. */
    private Color color;

    /** Highlight color. */
    private Color highlight;

    /** Shadow color. */
    private Color shadow;

    /** Testing center name. */
    private String centerName;

    /** Testing station number. */
    private String stationNumber;

    /** Status message. */
    private String status;

    /** Font in which to draw the center name. */
    private Font nameFont;

    /** Font in which to draw the version number. */
    private Font versionFont;

    /** Font in which to draw the station number. */
    private Font numberFont;

    /** Font in which to draw the status. */
    private Font statusFont;

    /** Frame number. */
    private int frame;

    /** The typed key command. */
    private final char[] keyCommand;

    /** The position in the typed key command. */
    private int keyPos;

    /**
     * Back door shutdown method tracking (click in upper left, drag to lower right, click in upper right, drag to lower
     * left will restart the application).
     */
    private int mousing;

    /**
     * Constructs a new {@code BackgroundPane}.
     */
    public BackgroundPane() {

        super();

        updateColor();

        setFocusable(true);
        setFocusCycleRoot(true);
        setRequestFocusEnabled(true);

        addMouseListener(this);
        addKeyListener(this);

        this.keyCommand = new char[12];
        this.keyPos = 0;
    }

    /**
     * Advances the frame.
     */
    public void advance() {

        this.frame++;

        if ((this.frame % 5) == 0) {
            repaint();
        }

        requestFocus();
    }

    /**
     * Overrides the method to set the background color, so the text color is also updated.
     *
     * @param bg the new background color
     */
    @Override
    public void setBackground(final Color bg) {
        synchronized (this) {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            super.setBackground(bg);
            updateColor();
        }
    }

    /**
     * Updates the color in which to paint the text, based on the current background color.
     */
    private void updateColor() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Generate a slightly lighter color than the background
        final Color bg = getBackground();
        int r = bg.getRed();
        int g = bg.getGreen();
        int b = bg.getBlue();
        r = r + ((255 - r) / 10);
        g = g + ((255 - g) / 10);
        b = b + ((255 - b) / 10);
        this.color = new Color(r, g, b);
        r = r + ((255 - r) / 10);
        g = g + ((255 - g) / 10);
        b = b + ((255 - b) / 10);
        this.highlight = new Color(r, g, b);

        r = bg.getRed();
        g = bg.getGreen();
        b = bg.getBlue();
        r = (r << 3) / 10;
        g = (g << 3) / 10;
        b = (b << 3) / 10;
        this.shadow = new Color(r, g, b);
    }

    /**
     * Sets the name of the testing center.
     *
     * @param theCenterName the name of the testing center
     */
    public void setCenterName(final String theCenterName) {
        synchronized (this) {

            this.centerName = theCenterName;
            this.nameFont = null;
        }
    }

    /**
     * Sets the station number to display in the window.
     *
     * @param theStationNumber the station number
     */
    public void setStationNumber(final String theStationNumber) {
        synchronized (this) {

            this.stationNumber = theStationNumber;
            this.numberFont = null;
        }
    }

    /**
     * Sets the status text to display in the window.
     *
     * @param theStatus the status text
     */
    public void setStatus(final String theStatus) {
        synchronized (this) {

            this.status = theStatus;
            this.statusFont = null;
        }
    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {
        synchronized (this) {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());

            final Graphics2D g2d = (Graphics2D) g;
            final RenderingHints orig = g2d.getRenderingHints();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            final Dimension size = getSize();
            int pts;
            final int[] heightDesc = new int[2];

            if (this.centerName != null) {
                final int h = size.height / 4;

                if (this.nameFont == null) {

                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 400) {
                        this.nameFont =
                                bfm.getFont(BundledFontManager.SERIF, (double) pts, Font.BOLD | Font.ITALIC);

                        maxHeightDescent(g2d, this.nameFont, this.centerName, heightDesc);
                        final FontMetrics fm = g.getFontMetrics(this.nameFont);

                        if (((double) fm.stringWidth(this.centerName) >= (double) size.width * 0.9)
                                || ((double) heightDesc[0] >= (double) h * 0.9)) {
                            break;
                        }

                        pts += 4;
                    }
                }

                g.setFont(this.nameFont);

                maxHeightDescent(g2d, this.nameFont, this.centerName, heightDesc);
                final FontMetrics fm = g.getFontMetrics();
                final int x = (int) ((double) size.width - fm.getStringBounds(this.centerName, g).getWidth()) / 2;
                final int y = size.height / 8 + heightDesc[0] / 2 - heightDesc[1];

                g.setColor(this.highlight);
                g.drawString(this.centerName, x - 2, y - 2);
                g.setColor(this.shadow);
                g.drawString(this.centerName, x + 2, y + 2);
                g.setColor(this.color);
                g.drawString(this.centerName, x, y);
            }

            if (this.stationNumber != null) {
                final int h = size.height / 2;

                if (this.numberFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 1000) {
                        this.numberFont = bfm.getFont("Martin_Vogels_Symbole", (double) pts, Font.BOLD);

                        maxHeightDescent(g2d, this.numberFont, this.stationNumber, heightDesc);
                        final FontMetrics fm = g.getFontMetrics(this.numberFont);

                        if ((double) fm.stringWidth(this.stationNumber) >= (double) size.width * 0.9
                                || ((double) heightDesc[0] >= (double) h * 0.9)) {
                            break;
                        }

                        pts += 10;
                    }
                }

                g.setFont(this.numberFont);

                maxHeightDescent(g2d, this.numberFont, this.stationNumber, heightDesc);
                final FontMetrics fm = g.getFontMetrics(this.numberFont);
                final int x = (size.width - fm.stringWidth(this.stationNumber)) / 2;
                final int y = size.height / 2 + heightDesc[0] / 2 - heightDesc[1];

                g.setColor(this.highlight);
                g.drawString(this.stationNumber, x - 2, y - 2);
                g.setColor(this.shadow);
                g.drawString(this.stationNumber, x + 2, y + 2);
                g.setColor(this.color);
                g.drawString(this.stationNumber, x, y);
            }

            if (this.status != null) {
                final int h = size.height / 4;

                if (this.statusFont == null) {
                    final BundledFontManager bfm = BundledFontManager.getInstance();
                    pts = 10;

                    while (pts < 400) {
                        this.statusFont = bfm.getFont("SANS", (double) pts, Font.PLAIN);

                        maxHeightDescent(g2d, this.statusFont, this.status, heightDesc);
                        final FontMetrics fm = g.getFontMetrics(this.statusFont);

                        if (((double) fm.stringWidth(this.status) >= (double) size.width * 0.9)
                                || ((double) heightDesc[0] >= (double) h * 0.5)) {
                            break;
                        }

                        pts += 4;
                    }
                }

                g.setFont(this.statusFont);

                maxHeightDescent(g2d, this.statusFont, this.status, heightDesc);
                final FontMetrics fm = g.getFontMetrics();
                final int x = (int) ((double) size.width - fm.getStringBounds(this.status, g).getWidth()) / 2;
                final int y = size.height * 7 / 8 + heightDesc[0] / 2 - heightDesc[1];

                g.setColor(this.highlight);
                g.drawString(this.status, x - 2, y - 2);
                g.setColor(this.shadow);
                g.drawString(this.status, x + 2, y + 2);
                g.setColor(this.color);
                g.drawString(this.status, x, y);
            }

            g.setColor(this.highlight);

            if (this.versionFont == null) {
                final BundledFontManager bfm = BundledFontManager.getInstance();
                this.versionFont = bfm.getFont("SERIF", 14.0, Font.BOLD | Font.ITALIC);
            }

            g.setFont(this.versionFont);

            final FontMetrics fm = g.getFontMetrics();
            final int y = size.height - 5 - fm.getDescent();
            g.drawString(TestStationApp.VERSION, 5, y);

            if (this.keyPos > 0) {
                g.setColor(this.shadow);
                final String cmd = new String(this.keyCommand, 0, this.keyPos);
                final int x = size.width - 5 - fm.stringWidth(cmd);
                g.drawString(cmd, x, y);
            }

            // Draw some tractor-treads to show that the frames are being updated when station appears
            // locked up
            g.setColor(this.color);
            g.drawLine(0, 0, size.width, 0);
            g.drawLine(0, 4, size.width, 4);

            for (int x = (this.frame / 5) % 5; x < size.width; x++) {
                g.setColor(this.highlight);
                g.drawLine(x, 1, x, 3);
                x++;
                g.setColor(this.color);
                g.drawLine(x, 1, x, 3);
                x++;
                g.drawLine(x, 1, x, 3);
                x++;
                g.setColor(this.shadow);
                g.drawLine(x, 1, x, 3);
                x++;
            }

            g2d.setRenderingHints(orig);
        }
    }

    /**
     * Finds the maximum height of any glyph in a string.
     *
     * @param g2d           the {@code Graphics2D} to which the string will be drawn
     * @param font          the font
     * @param str           the string to be drawn
     * @param heightDescent a 2-integer array that will receive the maximum height of any glyph and the maximum descent
     *                      of any glyph
     */
    private static void maxHeightDescent(final Graphics2D g2d, final Font font, final String str,
                                         final int[] heightDescent) {

        double maxHeight;
        double maxDesc;

        final GlyphVector vec = font.createGlyphVector(g2d.getFontRenderContext(), str);

        final Rectangle2D bounds = vec.getGlyphMetrics(0).getBounds2D();

        maxHeight = bounds.getHeight();
        maxDesc = bounds.getMaxY();
        final int numGlyphs = vec.getNumGlyphs();
        for (int i = 0; i < numGlyphs; ++i) {
            maxHeight = Math.max(maxHeight, bounds.getHeight());
            maxDesc = Math.max(maxDesc, bounds.getMaxY());
        }

        heightDescent[0] = (int) maxHeight;
        heightDescent[1] = (int) maxDesc;
    }

    /**
     * Placeholder implementation of the {@code MouseListener} interface.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    /**
     * Placeholder implementation of the {@code MouseListener} interface.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Placeholder implementation of the {@code MouseListener} interface.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }

    /**
     * Placeholder implementation of the {@code MouseListener} interface.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        final int x = e.getX();
        final int y = e.getY();

        switch (this.mousing) {

            case 0:
                if ((x < 50) && (y < 50)) { // Upper left
                    this.mousing++;
                }
                break;

            case 2:
                if ((x > (getWidth() - 50)) && (y < 50)) { // Upper right
                    this.mousing++;
                } else {
                    this.mousing = 0;
                }
                break;

            default:
                this.mousing = 0; // Screwed up, so start over.
                break;
        }
    }

    /**
     * Placeholder implementation of the {@code MouseListener} interface.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        final int x = e.getX();
        final int y = e.getY();

        switch (this.mousing) {

            case 1:
                if ((x > (getWidth() - 50)) && (y > (getHeight() - 50))) {// Lower right
                    this.mousing++;
                } else {
                    this.mousing = 0;
                }
                break;

            case 3:
                if ((x < 50) && (y > (getHeight() - 50))) { // Lower left
                    // Exit sequence has been completed. background pane is installed in a JPanel that
                    // is the content pane of the app frame.
                    getRootPane().getParent().setVisible(false);
                    this.mousing = 0;
                } else {
                    this.mousing = 0;
                }
                break;

            default:
                this.mousing = 0; // Screwed up, so start over.
                break;
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

        final char chr = e.getKeyChar();

        this.keyCommand[this.keyPos] = chr;
        ++this.keyPos;

        boolean match = true;
        for (int i = 0; i < this.keyPos; ++i) {
            if (this.keyCommand[i] != LOG_VIEW_CMD[i]) {
                match = false;
                break;
            }
        }

        if (match) {
            if (this.keyPos == LOG_VIEW_CMD.length) {
                this.keyPos = 0;

                // COMMAND RECOGNIZED - SHOW THE LOG
                final LogViewPanel dialog = new LogViewPanel(this);
                final Runnable adder = new PanelAdder(this, dialog);
                adder.run();
            }
        } else {
            this.keyPos = 0;
        }

        repaint();
    }

    /**
     * Handles key released events.
     *
     * @param e the key event
     */
    @Override
    public void keyReleased(final KeyEvent e) {

        // No action
    }

    ///**
    // * Updates the UI for the component.
    // */
    // @Override public void updateUI() {
    //
    // if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
    // final UIDefaults map = new UIDefaults();
    // final Painter<JComponent> painter = new Painter<JComponent>() {
    //
    // @Override public void paint(final Graphics2D grx, final JComponent comp,
    // final int width, final int height) {
    //
    // // fill using normal desktop color
    // grx.setColor(comp.getBackground());
    // grx.fillRect(0, 0, width, height);
    // }
    // };
    // map.put("DesktopPane[Enabled].backgroundPainter", painter);
    // putClientProperty("Nimbus.Overrides", map);
    // }
    //
    // super.updateUI();
    // }
}
