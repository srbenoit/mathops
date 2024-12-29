package dev.mathops.font;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * An application to render all glyphs for a font, and a specified point size. The glyph's numeric value is shown below
 * each glyph.
 */
public final class GlyphViewer implements ActionListener, ViewerInt {

    /** The menu items for the font sizes. */
    private static final String[] SIZES = {"10 point", "12 point",
            "14 point", "16 point", "18 point",
            "20 point", "24 point", "30 point",
            "48 point",};

    /** The initial font size. */
    private static final int FONT_SIZE = 16;

    /** The scroll bar unit increment. */
    private static final int SCROLL_INCREMENT = 36;

    /** Estimated number of submenus. */
    private static final int NUM_SUBMENUS = 20;

    /** Command string. */
    private static final String EXPORT = "Export";

    /** Command string. */
    private static final String TOGGLE = "ToggleBounds";

    /** The viewer frame. */
    private final JFrame frame;

    /** The map of font names to menu names. */
    private final Properties nameMap;

    /** The font manager from which to gather fonts. */
    private final BundledFontManager mgr;

    /** The font size menu items. */
    private JMenuItem[] actions;

    /** A scroll pane in which the grid of glyphs is rendered. */
    private JScrollPane scroll;

    /** The point size to use when rendering fonts. */
    private int size;

    /** The name of the font whose glyphs are being viewed. */
    private String name;

    /** The glyph panel used to render the fonts. */
    private GlyphPanel panel;

    /** The named submenus created so far. */
    private final Map<String, JMenu> submenus;

    /**
     * Constructs a new {@code GlyphViewer}.
     *
     * @param manager the font manager from which to retrieve fonts
     */
    private GlyphViewer(final BundledFontManager manager) {

        super();

        this.actions = null;
        this.scroll = null;
        this.size = 0;
        this.name = null;
        this.panel = null;

        this.frame = new JFrame("Glyph Viewer");
        this.mgr = manager;
        this.submenus = new HashMap<>(NUM_SUBMENUS);
        this.nameMap = new Properties();

        loadNameMap();
        buildUI();
    }

    /**
     * Loads the map from font names to menu names.
     */
    private void loadNameMap() {

        try (final InputStream input = FileLoader.openInputStream(GlyphViewer.class,
                "edu/colostate/math/app/font/fontnames.properties", true)) {
            this.nameMap.load(input);
        } catch (final IOException ex) {
            Log.warning(//
                    "Failed to load edu/colostate/math/font/fontnames.properties");
            this.nameMap.clear();
        }
    }

    /**
     * Constructs the user interface, which consists of a menu dropdown of the available fonts, a menu dropdown of point
     * sizes, and a panel that shows the selected font's glyphs in the selected size, with the character codes below
     * each glyph.
     */
    private void buildUI() {

        final JMenuBar menuBar = new JMenuBar();

        menuBar.add(makeFontsMenu());
        menuBar.add(makeSizesMenu());
        menuBar.add(makeActionsMenu());

        this.frame.setJMenuBar(menuBar);

        this.panel = new GlyphPanel(this);
        this.scroll = new JScrollPane(this.panel);
        this.scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        this.scroll.setWheelScrollingEnabled(true);
        this.frame.setContentPane(this.scroll);

        // Center on screen.
        this.frame.pack();

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame.setLocation((screen.width - this.frame.getWidth()) / 2,
                (screen.height - this.frame.getHeight()) / 2);
        this.frame.setVisible(true);
    }

    /**
     * Builds the font menu.
     *
     * @return the font menu
     */
    private JMenu makeFontsMenu() {

        final JMenu menu = new JMenu("Fonts");
        final String[] names = this.mgr.fontNames();
        final ButtonGroup nameGrp = new ButtonGroup();

        for (final String s : names) {
            addFontToMenu(menu, s, nameGrp);
        }

        return menu;
    }

    /**
     * Builds the font sizes menu.
     *
     * @return the sizes menu
     */
    private JMenu makeSizesMenu() {

        final int count = SIZES.length;

        final JMenuItem[] fontSizes = new JMenuItem[count];
        final JMenu menu = new JMenu("Sizes");
        final ButtonGroup sizeGrp = new ButtonGroup();

        for (int i = 0; i < count; ++i) {
            fontSizes[i] = new JRadioButtonMenuItem(SIZES[i], false);
            fontSizes[i].addActionListener(this);
            menu.add(fontSizes[i]);
            sizeGrp.add(fontSizes[i]);
        }

        fontSizes[3].setSelected(true);
        this.size = FONT_SIZE;

        return menu;
    }

    /**
     * Builds the font actions menu.
     *
     * @return the actions menu
     */
    private JMenu makeActionsMenu() {

        final JMenu menu = new JMenu("Actions");

        this.actions = new JMenuItem[2];
        this.actions[0] = new JMenuItem("Export As Image...");
        this.actions[0].setActionCommand(EXPORT);
        this.actions[0].addActionListener(this);
        this.actions[0].setEnabled(false);
        menu.add(this.actions[0]);
        this.actions[1] = new JMenuItem("Toggle bounds boxes");
        this.actions[1].setActionCommand(TOGGLE);
        this.actions[1].addActionListener(this);
        menu.add(this.actions[1]);

        return menu;
    }

    /**
     * Create the menu item for a single font.
     *
     * @param menu     the menu to which to add the font item
     * @param fontName the name of the font to add
     * @param grp      the button group for font names
     */
    private void addFontToMenu(final JMenu menu, final String fontName, final ButtonGroup grp) {

        final JMenuItem menuItem = new JRadioButtonMenuItem(fontName, false);

        grp.add(menuItem);
        menuItem.addActionListener(this);

        JMenu submenu = menu;
        String menuName = fontName;

        for (final String key : this.nameMap.stringPropertyNames()) {
            if (fontName.startsWith(key)) {
                menuName = this.nameMap.getProperty(key);

                // See if we already have this menu
                submenu = this.submenus.get(menuName);

                break;
            }
        }

        if (menu != null && submenu == null) {
            submenu = new JMenu(menuName);
            this.submenus.put(menuName, submenu);
            menu.add(submenu);
        }

        if (submenu != null) {
            submenu.add(menuItem);
        }
    }

    /**
     * Handler for menu item selections.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (cmd.endsWith(" point")) {
            final String num = cmd.substring(0, cmd.length() - 6);
            try {
                final int pickedSize = Integer.parseInt(num);

                if (this.size != pickedSize) {
                    this.size = pickedSize;

                    if (this.name != null) {
                        rebuildFont();
                    }
                }
            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse point size");
            }
        } else if (EXPORT.equals(cmd)) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            final File file = new File(chooser.getCurrentDirectory(), //
                    this.name + ".png");
            chooser.setSelectedFile(file);

            if (chooser.showSaveDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
                this.panel.export(chooser.getSelectedFile());
            }
        } else if (TOGGLE.equals(cmd)) {
            this.panel.setDrawBoundsBoxes(!this.panel.isDrawingBoundsBoxes());
        } else {
            this.name = cmd;

            if (this.size != 0) {
                rebuildFont();
            }
        }
    }

    /**
     * Regenerates the font and glyphs display.
     */
    private void rebuildFont() {

        final Font font = this.mgr.getFont(this.name, this.size, Font.PLAIN);

        this.panel.setTheFont(font);

        // Enable export as image
        this.actions[0].setEnabled(true);
    }

    /**
     * Tells the scroll pane that something inside it has changed.
     *
     * @param jump the vertical size of boxes
     */
    @Override
    public void updateScroller(final int jump) {

        this.scroll.getVerticalScrollBar().setUnitIncrement(jump);
        this.scroll.revalidate();

        this.frame.repaint();
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final GlyphViewer viewer = new GlyphViewer(BundledFontManager.getInstance());

        viewer.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
