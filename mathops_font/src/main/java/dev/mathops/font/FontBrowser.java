package dev.mathops.font;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serial;
import java.util.Locale;

/**
 * A simple application allowing the user to select a TrueType (.TTF) or Type-1 (.PFA/.PFB) font file then displaying a
 * sample of that font's symbols.
 */
final class FontBrowser implements ActionListener {

    /** File menu label. */
    private static final String FILE_MENU = "File";

    /** File menu open command label. */
    private static final String OPEN_CMD = "Open";

    /** Font style. */
    private static final String PLAIN = "plain";

    /** Font style. */
    private static final String BOLD = "bold";

    /** Font style. */
    private static final String ITALIC = "italic";

    /** Font style. */
    private static final String BOLD_ITALIC = "bold italic";

    /** The frame. */
    private final JFrame frame;

    /** The content panel of the frame. */
    private final JPanel content;

    /** The currently loaded (one-point) font. */
    private Font font;

    /** The most recently opened font file's parent directory. */
    private File dir;

    /**
     * Creates a new {@code FontBrowser}.
     */
    private FontBrowser() {

        super();

        this.frame = new JFrame("Font Browser");

        final JMenuBar menuBar = new JMenuBar();
        final JMenu menu = new JMenu(FILE_MENU);
        final JMenuItem item = new JMenuItem(OPEN_CMD);

        this.content = new JPanel(new BorderLayout());
        this.frame.setContentPane(this.content);

        item.addActionListener(this);
        item.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        menu.add(item);
        menuBar.add(menu);
        this.frame.setJMenuBar(menuBar);

        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.font = new Font(Font.DIALOG, Font.PLAIN, 1);
        this.content.add(new FontPanel(this.font), BorderLayout.CENTER);
    }

    /**
     * Handles action events.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (OPEN_CMD.equals(cmd)) {
            final JFileChooser jfc = new JFileChooser();

            if (this.dir != null) {
                jfc.setCurrentDirectory(this.dir);
            }

            if (jfc.showOpenDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
                final File file = jfc.getSelectedFile();
                this.dir = file.getParentFile();

                final String name = file.getName().toLowerCase(Locale.US);

                try (final FileInputStream fis = new FileInputStream(file)) {

                    if (name.endsWith(".ttf") || name.endsWith(".otf")) {
                        this.font = Font.createFont(Font.TRUETYPE_FONT, fis);
                    } else {
                        this.font = Font.createFont(Font.TYPE1_FONT, fis);
                    }

                    this.content.removeAll();
                    this.content.add(new FontPanel(this.font), BorderLayout.CENTER);
                    this.frame.pack();

                    this.frame.setTitle(file.getAbsolutePath());
                } catch (final IOException | FontFormatException ex) {
                    Log.warning(ex);
                }
            }
        }
    }

    /**
     * Main method to execute the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        UIUtilities.packAndCenter(new FontBrowser().frame);
    }

    /**
     * A panel that displays the selected font in varying sizes.
     */
    private static final class FontPanel extends JPanel {

        /** Default panel width. */
        private static final int PANEL_WIDTH = 800;

        /** Default panel height. */
        private static final int PANEL_HEIGHT = 800;

        /** Default font size 1. */
        private static final int FONT_SIZE1 = 24;

        /** Default font size 2. */
        private static final int FONT_SIZE2 = 12;

        /** Default font size 3. */
        private static final int FONT_SIZE3 = 9;

        /** Default font size 4. */
        private static final int FONT_SIZE4 = 18;

        /** Uppercase letters. */
        private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        /** Lowercase letters. */
        private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";

        /** Numbers and punctuation. */
        private static final String PUNCT = //
                "1234567890`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -4052269402511508396L;

        /**
         * Construct a new {@code FontPanel}.
         *
         * @param fnt the font to display in the panel
         */
        FontPanel(final Font fnt) {

            super(new BorderLayout(5, 5));

            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

            if (fnt != null) {

                // Build the UI.
                final JPanel grid = new JPanel(new GridLayout(16, 1, 0, 0));
                add(grid, BorderLayout.PAGE_START);

                addFontSample(fnt, grid, Font.PLAIN, FONT_SIZE1, PLAIN);
                addFontSample(fnt, grid, Font.BOLD, FONT_SIZE1, BOLD);
                addFontSample(fnt, grid, Font.ITALIC, FONT_SIZE1, ITALIC);
                addFontSample(fnt, grid, Font.BOLD | Font.ITALIC, FONT_SIZE1, BOLD_ITALIC);

                addFontSample(fnt, grid, Font.PLAIN, FONT_SIZE2, PLAIN);
                addFontSample(fnt, grid, Font.BOLD, FONT_SIZE2, BOLD);
                addFontSample(fnt, grid, Font.ITALIC, FONT_SIZE2, ITALIC);
                addFontSample(fnt, grid, Font.BOLD | Font.ITALIC, FONT_SIZE2, BOLD_ITALIC);
                addCanon(fnt, grid, Font.BOLD | Font.ITALIC, FONT_SIZE2);

                addFontSample(fnt, grid, Font.PLAIN, FONT_SIZE3, PLAIN);
                addFontSample(fnt, grid, Font.BOLD, FONT_SIZE3, BOLD);
                addFontSample(fnt, grid, Font.ITALIC, FONT_SIZE3, ITALIC);
                addFontSample(fnt, grid, Font.BOLD | Font.ITALIC, FONT_SIZE3, BOLD_ITALIC);
                addCanon(fnt, grid, Font.BOLD | Font.ITALIC, FONT_SIZE3);

                final Font derived = fnt.deriveFont(Font.PLAIN, (float) FONT_SIZE4);
                final JTextArea area = new JTextArea("Type text here to try out the font.");
                area.setBorder(BorderFactory.createLoweredBevelBorder());
                area.setFont(derived);
                add(area, BorderLayout.CENTER);
            }
        }

        /**
         * Adds a font sample to the grid.
         *
         * @param fnt       the base font
         * @param grid      the grid to which to add the sample
         * @param style     the font style
         * @param size      the point size
         * @param styleName the text name of the font style
         */
        private static void addFontSample(final Font fnt, final JPanel grid, final int style,
                                          final int size, final String styleName) {

            final Font derived = fnt.deriveFont(style, (float) size);
            final JLabel lbl = new JLabel(
                    fnt.getFontName() + " in " + size + "-point " + styleName);
            lbl.setFont(derived);
            grid.add(lbl);
        }

        /**
         * Adds a sample of a canon of characters and symbols to the grid.
         *
         * @param fnt   the base font
         * @param grid  the grid to which to add the sample
         * @param style the font style
         * @param size  the point size
         */
        private static void addCanon(final Font fnt, final JPanel grid, final int style,
                                     final int size) {

            final Font derived = fnt.deriveFont(style, (float) size);

            final JLabel lbl = new JLabel(UPPERCASE + LOWERCASE);
            lbl.setFont(derived);
            grid.add(lbl);

            final JLabel lbl2 = new JLabel(PUNCT);
            lbl2.setFont(derived);
            grid.add(lbl2);
        }
    }
}
