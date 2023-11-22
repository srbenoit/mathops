package dev.mathops.font;

import dev.mathops.core.log.Log;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.IntBuffer;
import java.util.Iterator;

/**
 * A panel that renders the glyphs of a font.
 */
final class GlyphPanel extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3511066856984634391L;

    /** Default width of panel. */
    private static final int DEFAULT_WIDTH = 1024;

    /** Default height of panel. */
    private static final int DEFAULT_HEIGHT = 480;

    /** The upper limit of code points. */
    private static final int MAX_CODE_POINT = 0x110000;

    /** The owning frame. */
    private final ViewerInt ownerFrame;

    /** The font to be rendered. */
    private Font font;

    /** A font render context based on a one-by-one buffered image. */
    private final FontRenderContext context;

    /** An off-screen image that is rendered to. */
    private BufferedImage offScreen;

    /** The width of the box for each glyph. */
    private int boxWidth;

    /** The height of the box for each glyph. */
    private int boxHeight;

    /** Flag to control whether bounds are drawn around characters. */
    private boolean drawBoxes;

    /** Flag indicating off-screen image needs to be repainted. */
    private boolean dirty;

    /**
     * Constructs a new {@code GlyphPanel}.
     *
     * @param owner the viewer that owns this panel
     */
    GlyphPanel(final ViewerInt owner) {

        super();

        this.ownerFrame = owner;
        this.font = null;
        this.offScreen = null;
        this.boxWidth = 0;
        this.boxHeight = 0;
        this.drawBoxes = false;
        this.dirty = false;

        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        setBackground(Color.white);

        final BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        this.context = ((Graphics2D) img.getGraphics()).getFontRenderContext();
    }

    /**
     * Sets the font that the panel will render.
     *
     * @param theFont the font
     */
    void setTheFont(final Font theFont) {

        this.font = theFont;
        this.dirty = true;
        repaint();
    }

    /**
     * Sets the flag controlling whether bounds are drawn.
     *
     * @param drawBoundsBoxes {@code true} to draw bounds boxes; {@code false} otherwise
     */
    void setDrawBoundsBoxes(final boolean drawBoundsBoxes) {

        this.drawBoxes = drawBoundsBoxes;
        this.dirty = true;
    }

    /**
     * Gets the state of the flag controlling whether bounds are drawn.
     *
     * @return {@code true} if bounding boxes are being drawn; {@code false} otherwise
     */
    boolean isDrawingBoundsBoxes() {

        return this.drawBoxes;
    }

    /**
     * Redraws the panel. If this is the first time paint has been called since the font was changed, the off-screen
     * glyph image is created.
     *
     * @param g the {@code Graphics} object to render to
     */
    @Override
    public void paint(final Graphics g) {

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (this.dirty) {

            if (this.offScreen != null) {
                this.offScreen.flush();
            }

            if ((this.font != null) && (g instanceof Graphics2D)) {
                buildOffScreen((Graphics2D) g);
                this.dirty = false;
            }
        }

        g.drawImage(this.offScreen, 0, 0, this);
    }

    /**
     * Creates an off-screen image with the glyph renderings.
     *
     * @param grx the {@code Graphics2D} object to render to
     */
    private void buildOffScreen(final Graphics2D grx) {

        // Get the code points the font supports
        final int[] codePoints = getFontSupportedCodePoints(this.font);

        // Create a font to be used for labeling
        final Font labelFont = new Font("Dialog", Font.PLAIN, 9);
        final FontMetrics fmLabel = grx.getFontMetrics(labelFont);
        final FontMetrics fmFont = grx.getFontMetrics(this.font);

        // Find box width based on max size of label or glyph
        this.boxWidth = fmLabel.stringWidth("119999");

        if (this.boxWidth < (int) Math.ceil(fmFont.getMaxCharBounds(grx).getWidth())) {
            this.boxWidth = (int) Math.ceil(fmFont.getMaxCharBounds(grx).getWidth());
        }

        // add a pixel per box for left border
        ++this.boxWidth;

        // Using box width, and assuming right border, find boxes per row
        final int columns = (DEFAULT_WIDTH - 1) / this.boxWidth;

        // Compute total width of image, including borders, add a pixel for right border
        final int imgW = (this.boxWidth * columns) + 1;

        // Determine the number of full rows
        int rows = codePoints.length / columns;

        // add a partial row if needed
        if ((rows * columns) < codePoints.length) {
            ++rows;
        }

        // Compute box height. Note we don't need descent on label since digits don't extend below
        // baseline, but we add 1 pixel below, along with 1 pixel for an interior border.
        this.boxHeight = fmFont.getHeight() + fmLabel.getAscent();
        // Add top border and border between glyph and
        this.boxHeight += 2;

        // label - add a pixel for bottom border
        int imgH = (this.boxHeight * rows) + 1;

        // Add space for a line for the font name
        imgH += fmFont.getHeight() + fmFont.getLeading();

        final Graphics2D g2d = createOffScreen(imgW, imgH);
        drawGrid(g2d, fmFont);
        g2d.setFont(labelFont);
        drawLabels(g2d, codePoints, fmFont, fmLabel);

        makeImage(g2d, codePoints, fmFont);

        setPreferredSize(new Dimension(imgW, imgH));
        this.ownerFrame.updateScroller(this.boxHeight);
    }

    /**
     * Creates the off-screen image and build its graphics object.
     *
     * @param imgW the image width
     * @param imgH the image height
     * @return the {@code Graphics2D} for the image
     */
    private Graphics2D createOffScreen(final int imgW, final int imgH) {

        this.offScreen = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);

        final Graphics2D g2d = (Graphics2D) (this.offScreen.getGraphics());
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, imgW, imgH);

        return g2d;
    }

    /**
     * Draws the grid on the image.
     *
     * @param g2d    the {@code Graphics2D} to which to draw
     * @param fmFont the metrics of the font being rendered
     */
    private void drawGrid(final Graphics2D g2d, final FontMetrics fmFont) {

        final int rows = this.offScreen.getHeight() / this.boxHeight;
        final int columns = this.offScreen.getWidth() / this.boxWidth;
        final int yPos = fmFont.getHeight();

        // Draw grid
        for (int i = 0; i <= rows; ++i) {
            g2d.setColor(Color.lightGray);
            g2d.fillRect(0, yPos + (i * this.boxHeight) + fmFont.getHeight(),
                    this.offScreen.getWidth(), this.boxHeight - fmFont.getHeight());
            g2d.setColor(Color.black);
            g2d.drawLine(0, yPos + (i * this.boxHeight), this.offScreen.getWidth(),
                    yPos + (i * this.boxHeight));
            g2d.setColor(Color.gray);
            g2d.drawLine(0, yPos + (i * this.boxHeight) + fmFont.getHeight(),
                    this.offScreen.getWidth(), yPos + (i * this.boxHeight) + fmFont.getHeight());
        }

        g2d.setColor(Color.BLACK);

        for (int i = 0; i <= columns; ++i) {
            g2d.drawLine(i * this.boxWidth, yPos, i * this.boxWidth,
                    yPos + this.offScreen.getHeight() - fmFont.getHeight() - fmFont.getLeading() - 1);
        }
    }

    /**
     * Draws the labels on the image.
     *
     * @param g2d        the {@code Graphics2D} to which to draw
     * @param codePoints an array of the Unicode code points the font supports
     * @param fmFont     the metrics of the font being rendered
     * @param fmLabel    the metrics of the label font
     */
    private void drawLabels(final Graphics2D g2d, final int[] codePoints, final FontMetrics fmFont,
                            final FontMetrics fmLabel) {

        int yPos = fmFont.getHeight();
        int xPos = 0;

        for (final int codePoint : codePoints) {
            final String str = Integer.toHexString(codePoint);
            g2d.drawString(str, xPos + 1 + ((this.boxWidth - fmLabel.stringWidth(str)) / 2),
                    yPos + fmFont.getHeight() + fmLabel.getAscent());

            xPos += this.boxWidth;

            if (xPos >= (this.offScreen.getWidth() - 1)) {
                xPos = 0;
                yPos += this.boxHeight;
            }
        }
    }

    /**
     * Draws the image with a grid with labels and all the glyphs.
     *
     * @param g2d        the {@code Graphics} to which to draw
     * @param codePoints an array of the Unicode code points the font supports
     * @param fmFont     the metrics for the font being displayed
     */
    private void makeImage(final Graphics2D g2d, final int[] codePoints, final FontMetrics fmFont) {

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the line of example text
        g2d.setFont(this.font);
        g2d.setColor(Color.black);

        final String str1 = this.font.getName() + ", " + this.font.getSize()
                + " point ";
        g2d.drawString(str1, 5, fmFont.getAscent() + (fmFont.getLeading() / 2));

        int xPos = 5 + fmFont.stringWidth(str1);
        g2d.setFont(this.font.deriveFont(Font.BOLD));

        final String str2 = " Bold, ";
        g2d.drawString(str2, xPos, fmFont.getAscent() + (fmFont.getLeading() / 2));
        xPos += g2d.getFontMetrics().stringWidth(str2);
        g2d.setFont(this.font.deriveFont(Font.ITALIC));

        final String str3 = " Italic";
        g2d.drawString(str3, xPos, fmFont.getAscent() + (fmFont.getLeading() / 2));
//        xPos += g2d.getFontMetrics().stringWidth(str3);

        g2d.setFont(this.font);

        // Draw glyphs
        xPos = 0;

        int yPos = fmFont.getHeight();

        g2d.setFont(this.font);

        final char[] char1 = {'a'};
        final char[] char2 = {'a', 'b'};

        for (final int codePoint : codePoints) {

            final GlyphVector vec;

            if (Character.isSupplementaryCodePoint(codePoint)) {
                Character.toChars(codePoint, char2, 0);
                vec = this.font.createGlyphVector(this.context, char2);
            } else {
                char1[0] = (char) codePoint;
                vec = this.font.createGlyphVector(this.context, char1);
            }

            final Shape shape = vec.getGlyphOutline(0);
            final int pixX = xPos + 1 + ((this.boxWidth - shape.getBounds().width) / 2);
            final int pixY = yPos + fmFont.getAscent();
            final Shape shape2 = vec.getGlyphOutline(0, (float) (pixX - shape.getBounds().x), (float) pixY);

            // Draw a glyph bounds box around the character, if configured
            if (this.drawBoxes) {
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.draw(shape2.getBounds());
                g2d.setColor(Color.BLACK);
            }

            // Draw the character
            g2d.fill(shape2);

            xPos += this.boxWidth;

            if (xPos >= (this.offScreen.getWidth() - 1)) {
                xPos = 0;
                yPos += this.boxHeight;
            }
        }
    }

    /**
     * Gets the list of characters that a font supports.
     *
     * @param fnt the font
     * @return an array of the Unicode code points the font supports
     */
    private static int[] getFontSupportedCodePoints(final Font fnt) {

        final int count = fnt.getNumGlyphs();
        final IntBuffer buf = IntBuffer.allocate(2 * count);

        // Scan all Unicode code points for those the font can process
        for (int codePoint = 0; codePoint < MAX_CODE_POINT; ++codePoint) {

            if (fnt.canDisplay(codePoint)) {
                buf.put(codePoint);
            }
        }

        final int[] glyphCodes = new int[buf.position()];
        System.arraycopy(buf.array(), 0, glyphCodes, 0, buf.position());

        return glyphCodes;
    }

    /**
     * Exports the image as a JPEG file.
     *
     * @param target the file to write to
     */
    void export(final File target) {

        final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");

        if (iter.hasNext()) {
            final ImageWriter writer = iter.next();

            try (final FileImageOutputStream fios = new FileImageOutputStream(target)) {
                writer.setOutput(fios);
                writer.write(this.offScreen);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }
    }
}
