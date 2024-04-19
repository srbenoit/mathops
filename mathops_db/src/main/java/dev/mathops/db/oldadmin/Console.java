package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * A console panel that shows text (perhaps inverted) and az cursor.
 */
public final class Console extends JPanel implements Runnable {

    /** Margin on left and right. */
    private static final int LEFT_RIGHT_MARGIN = 5;

    /** Margin on top and bottom. */
    private static final int TOP_BOTTOM_MARGIN = 5;

    /** The desired font size. */
    private static final float FONT_SIZE = 12.0f;

    /** The fallback font size. */
    private static final int FALLBACK_FONT_SIZE = 13;

    /** The cursor color. */
    private static final Color CURSOR_COLOR = new Color(50, 205, 50);

    /** The number of columns. */
    private final int numColumns;

    /** The number of lines. */
    private final int numLines;

    /** The cursor location. */
    private final Point cursor;

    /** The labels that display the characters in [y][x] order. */
    private final JLabel[][] labels;

    /** The characters in [y][x] order. */
    private final char[][] characters;

    /** A flag indicating whether each character is reversed. */
    private final boolean[][] reversed;

    /**
     * Constructs a new {@code Console}.
     *
     * @param theNumColumns the number of columns of text
     * @param theNumLines   the number of lines of text
     */
    Console(final int theNumColumns, final int theNumLines) {

        super(new GridLayout(theNumLines, theNumColumns));

        setFocusable(true);

        this.numColumns = theNumColumns;
        this.numLines = theNumLines;
        this.cursor = new Point(-1, -1);

        setBackground(Color.BLACK);
        final Border edgeBorder = BorderFactory.createEmptyBorder(TOP_BOTTOM_MARGIN, LEFT_RIGHT_MARGIN,
                TOP_BOTTOM_MARGIN, LEFT_RIGHT_MARGIN);
        setBorder(edgeBorder);

        final byte[] fontBytes = FileLoader.loadFileAsBytes(Console.class, "JetBrainsMono.ttf", true);
        Font font;
        if (fontBytes == null) {
            font = new Font(Font.MONOSPACED, Font.PLAIN, FALLBACK_FONT_SIZE);
        } else {
            try (final ByteArrayInputStream in = new ByteArrayInputStream(fontBytes)) {
                final Font onePoint = Font.createFont(Font.TRUETYPE_FONT, in);
                font = onePoint.deriveFont(FONT_SIZE);
            } catch (final IOException | FontFormatException ex) {
                font = new Font(Font.MONOSPACED, Font.PLAIN, FALLBACK_FONT_SIZE);
            }
        }

        this.labels = new JLabel[theNumLines][theNumColumns];
        for (int line = 0; line < theNumLines; ++line) {
            for (int col = 0; col < theNumColumns; ++col) {
                final JLabel lbl = new JLabel(CoreConstants.SPC);
                lbl.setBackground(Color.BLACK);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(font);
                lbl.setOpaque(true);
                add(lbl);
                this.labels[line][col] = lbl;
            }
        }

        this.characters = new char[theNumLines][theNumColumns];
        this.reversed = new boolean[theNumLines][theNumColumns];
    }

    /**
     * Clears all characters to spaces.
     */
    public void clear() {
        for (int line = 0; line < this.numLines; ++line) {
            for (int col = 0; col < this.numColumns; ++col) {
                this.characters[line][col] = ' ';
                this.reversed[line][col] = false;
            }
        }
    }

    /**
     * Sets the cursor location.  If the (x, y) point is outside the screen bounds, no cursor is drawn.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    void setCursor(final int x, final int y) {

        this.cursor.setLocation(x, y);
    }

    /**
     * Gets the cursor location.
     *
     * @return the cursor.
     */
    Point getCursorPoint() {

        return this.cursor;
    }

    /**
     * Prints a string at a position.
     *
     * @param str the string to print
     * @param x   the x coordinate
     * @param y   the y coordinate
     */
    public void print(final String str, final int x, final int y) {

        if (y >= 0 && y < this.numLines) {
            final char[] row = this.characters[y];
            final char[] chars = str.toCharArray();
            for (int i = 0; i < chars.length; ++i) {
                final int pos = x + i;
                if (pos >= 0 && pos < this.numColumns) {
                    row[pos] = chars[i];
                }
            }
        }
    }

    /**
     * Reverses some number of characters starting at a given position.
     *
     * @param x   the x coordinate
     * @param y   the y coordinate
     * @param len the number of characters to reverse
     */
    void reverse(final int x, final int y, final int len) {

        if (y >= 0 && y < this.numLines) {
            final boolean[] row = this.reversed[y];
            for (int i = 0; i < len; ++i) {
                final int pos = x + i;
                if (pos >= 0 && pos < this.numColumns) {
                    row[pos] = true;
                }
            }
        }
    }

    /**
     * Updates all labels with the appropriate characters.
     */
    public void commit() {

        SwingUtilities.invokeLater(this);
    }

    /**
     * Updates labels.  This should be called on the AWT event thread.
     */
    public void run() {

        setVisible(false);

        final int cx = this.cursor.x;
        final int cy = this.cursor.y;

        for (int line = 0; line < this.numLines; ++line) {
            for (int col = 0; col < this.numColumns; ++col) {
                final JLabel lbl = this.labels[line][col];

                if (line == cy && col == cx) {
                    lbl.setBackground(CURSOR_COLOR);
                } else {
                    lbl.setBackground(this.reversed[line][col] ? Color.WHITE : Color.BLACK);
                }
                lbl.setForeground(this.reversed[line][col] ? Color.BLACK : Color.WHITE);

                final String str = Character.toString(this.characters[line][col]);

                final String existing = lbl.getText();
                if (!str.equals(existing)) {
                    lbl.setText(str);
                }

                if (!lbl.isPreferredSizeSet()) {
                    final Dimension size = lbl.getPreferredSize();
                    final Dimension exist = lbl.getPreferredSize();
                    if (!exist.equals(size)) {
                        lbl.setPreferredSize(size);
                    }
                }
            }
        }

        setVisible(true);
        requestFocus();
    }
}
