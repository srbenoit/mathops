package dev.mathops.assessment.formula.edit;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

/**
 * A rendered visual of a cursor step. If the selection range crosses a rendered box, the box is filled with a selection
 * highlight before drawing content, and all content is drawn in a selected text color. Otherwise, content is drawn in a
 * normal text color.
 *
 * <p>
 * The cursor is drawn at the boundaries between rendered boxes. A box may be "cursor-phobic" meaning when the cursor is
 * to the left of the box, it should cling to the right edge of the preceding rendered box, and if the cursor is to the
 * right of the box, it should cling to the left edge of the next rendered box (examples would be a division line in a
 * fraction, or a "box" that represents the cursor step from the end of the base to the start of an exponent).
 */
public final class RenderedBox extends AbstractFEDrawable {

    /** The selection highlight color. */
    private static final Color SELECTION_HIGHLIGHT_COLOR = new Color(60, 60, 240);

    /** The selection highlight color. */
    private static final Color TEXT_COLOR = new Color(30, 30, 30);

    /** The selection highlight color. */
    private static final Color SELECTED_TEXT_COLOR = Color.WHITE;

    /** The selection highlight color. */
    public static final Color CURSOR_COLOR = Color.BLACK;

    /** The test this box displays. */
    private final String text;

    /** True if this is a "cursor-phobic" box. */
    private boolean cursorPhobic;

    /**
     * Constructs a new {@code RenderedBox}.
     *
     * @param theText the text this box will display
     */
    public RenderedBox(final String theText) {

        super();

        this.text = theText;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {

        return this.text;
    }

    /**
     * Sets the flag that indicates whether this box is "cursor-phobic".
     *
     * @param isCursorPhobic {@code true} if this box is "cursor-phobic"; false if not
     */
    public void setCursorPhobic(final boolean isCursorPhobic) {

        this.cursorPhobic = isCursorPhobic;
    }

    /**
     * Tests whether this box is "cursor-phobic".
     *
     * @return {@code true} if this box is "cursor-phobic"; false if not
     */
    public boolean isCursorPhobic() {

        return this.cursorPhobic;
    }

    /**
     * Lays out the box, which sets the bounding rectangle and advance width.
     *
     * @param g2d the {@code Graphics2D} from which a font render context may be obtained
     */
    public void layout(final Graphics2D g2d) {

        final Font font = getFont();

        final FontRenderContext frc = g2d.getFontRenderContext();
        final FontMetrics metrics = g2d.getFontMetrics(font);
        final LineMetrics lineMetrics = font.getLineMetrics(this.text, frc);

        final int ascent = Math.round(lineMetrics.getAscent());
        final int descent = Math.round(lineMetrics.getDescent());

        final int adv = metrics.stringWidth(this.text);
        setAdvance(adv);
        getOrigin().setLocation(0, 0);
        getBounds().setBounds(0, -ascent, adv, ascent + descent);
        setCenterAscent(Math.round(lineMetrics.getBaselineOffsets()[Font.CENTER_BASELINE]));
    }

    /**
     * Moves this object and all subordinate objects. Used during layout.
     *
     * @param dx the x offset
     * @param dy the y offset
     */
    public void translate(final int dx, final int dy) {

        getOrigin().translate(dx, dy);
    }

    /**
     * Lays out the box, which sets the bounding rectangle and advance width.
     *
     * @param g2d      the {@code Graphics2D} from which a font render context may be obtained
     * @param selected true if this box is selected
     */
    public void render(final Graphics2D g2d, final boolean selected) {

        final Point origin = getOrigin();

        if (selected) {
            g2d.setColor(SELECTION_HIGHLIGHT_COLOR);
            g2d.translate(origin.x, origin.y);
            g2d.fill(getBounds());
            g2d.translate(-origin.x, -origin.y);
            g2d.setColor(SELECTED_TEXT_COLOR);
        } else {
            g2d.setColor(TEXT_COLOR);
        }

        g2d.setFont(getFont());
        g2d.drawString(this.text, origin.x, origin.y);
    }
}
