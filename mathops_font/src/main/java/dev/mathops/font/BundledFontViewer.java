package dev.mathops.font;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A frame that displays a tabbed pane with one tab per bundled font. Within each tab are the font's glyphs in various
 * sizes.
 */
final class BundledFontViewer extends JFrame {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4624550107266295327L;

    /** The font sizes to view. */
    private static final int[] SIZES = {10, 12, 14, 18, 24};

    /** Gap between entries. */
    private static final int GAP = 5;

    /** Background gray color level. */
    private static final int BG_GRAY = 220;

    /** White color level. */
    private static final int WHITE = 255;

    /** Window width. */
    private static final int PREF_WIDTH = 740;

    /** Window height. */
    private static final int PREF_HEIGHT = 500;

    /** Number of lines to spread glyphs across. */
    private static final int NUM_LINES = 5;

    /**
     * Constructs a new {@code BundledFontViewer}.
     */
    private BundledFontViewer() {

        super("Bundled Fonts Viewer");

        setBackground(new Color(BG_GRAY, BG_GRAY, WHITE));
    }

    /**
     * Generates the user interface, which consists of a tabbed pane where each tab is a different font, and within the
     * tab is a set of text boxes showing the font's glyphs in various sizes.
     *
     * @param mgr the font manager used to obtain the fonts
     */
    private void createUI(final BundledFontManager mgr) {

        final String[] names = mgr.fontNames();

        final JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM);
        tabs.setOpaque(true);

        tabs.setBackground(new Color(BG_GRAY, BG_GRAY, WHITE));
        tabs.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        tabs.setFont(new Font("Dialog", Font.PLAIN, SIZES[0]));
        setContentPane(tabs);

        final int count = names.length;
        final JPanel[] panes = new JPanel[count];

        for (int i = 0; i < count; ++i) {
            panes[i] = new JPanel();
            tabs.addTab(names[i], panes[i]);
            populateTab(mgr, names[i], panes[i]);
        }
    }

    /**
     * Generates the contents of a font tab, including text boxes that use the font at varying sizes.
     *
     * @param mgr  the font manager from which to retrieve fonts
     * @param name the name of the font
     * @param pane the panel in which to install the text boxes
     */
    private static void populateTab(final BundledFontManager mgr, final String name,
                                    final JPanel pane) {

        pane.setLayout(new GridLayout(SIZES.length, 1, GAP, GAP));
        pane.setBackground(new Color(BG_GRAY, BG_GRAY, BG_GRAY));
        pane.setBorder(BorderFactory.createEtchedBorder());

        final HtmlBuilder htm = new HtmlBuilder(500);

        for (final int size : SIZES) {
            final JTextArea area = new JTextArea();
            final Font fnt = mgr.getFont(name, size, Font.PLAIN);
            area.setFont(fnt);

            // Determine the font glyphs and set that in each text area.
            final int num = fnt.getNumGlyphs();
            int cnt = 0;
            char chr = 1;

            htm.reset();

            while ((cnt < num) && (chr < Character.MAX_VALUE)) {

                if (fnt.canDisplay(chr)) {
                    htm.add(chr);
                    ++cnt;

                    if ((cnt % ((num / NUM_LINES) + 1)) == 0) {
                        htm.addln();
                    }
                }

                ++chr;
            }

            area.setText(htm.toString());
            area.setWrapStyleWord(true);

            pane.add(area);
        }
    }

    /**
     * An implementation of main for testing.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final BundledFontViewer obj = new BundledFontViewer();
        final BundledFontManager mgr = BundledFontManager.getInstance();

        obj.createUI(mgr);
        obj.setDefaultCloseOperation(EXIT_ON_CLOSE);

        UIUtilities.packAndCenter(obj);
    }
}
