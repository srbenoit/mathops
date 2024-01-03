package dev.mathops.app.placement;

import dev.mathops.app.AppFileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.Serial;
import java.util.Properties;

/**
 * A simple dialog to tell the user that the operation in progress may take a while and to please be patient.
 */
class PleaseWait extends JFrame {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5903920624166365263L;

    /** The layered pane. */
    private transient JLayeredPane layered;

    /**
     * Construct a new {@code PleaseWait} panel.
     */
    PleaseWait() {

        super();

        final Properties res = new DefaultPleaseWaitSkin();

        try {
            SwingUtilities.invokeAndWait(new PleaseWaitGUIBuilder(this, res));
        } catch (final Exception ex) {
            Log.warning(ex);
        }
    }

    /**
     * Present the dialog centered in the desktop, and set the cursor to the WAIT cursor.
     */
    final void showPopup() {

        try {
            SwingUtilities.invokeAndWait(new ShowPleaseWait(this));
        } catch (final Exception e) {
            Log.warning(e);
        }
    }

    /**
     * Close the Please Wait popup.
     */
    final void closePopup() {

        try {
            SwingUtilities.invokeAndWait(new ClosePleaseWait(this));
        } catch (final Exception e) {
            Log.warning(e);
        }
    }

    /**
     * Center the internal frame within its owning desktop.
     */
    final void centerInDesktop() {

        final Dimension size = getSize();

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
    }

    /**
     * Examine a set of standard properties settings and configure the frame. This includes setting the frame size,
     * building the border, and setting the background color or image.
     *
     * @param res the properties settings governing GUI look
     */
    void setupFrame(final Properties res) {

        setUndecorated(true);

        final JPanel content = new JPanel(null);
        setContentPane(content);

        // Set the title, if any
        setTitle(res.getProperty("panel-title"));

        // Setup the internal frame look.
        content.setBorder(BorderFactory.createLineBorder(Color.black, 2));

        this.layered = new JLayeredPane();
        content.add(this.layered);

        // Configure the frame size
        final int width = getInt(res, "panel-width", 300);
        final int height = getInt(res, "panel-height", 300);
        this.layered.setPreferredSize(new Dimension(width + 4, height + 4));
        this.layered.setLocation(2, 2);
        this.layered.setSize(width, height);
        content.setSize(width + 4, height + 4);
        setSize(width + 4, height + 4);

        // Configure the frame border. Possible styles are "line" (in which case
        // "panel-border-color" and "panel-border-size" are used), "..."
        String prop = res.getProperty("panel-border-style");

        if ("line".equals(prop)) {
            this.layered.setBorder(BorderFactory.createLineBorder(//
                    getColor(res, "panel-border-color", "black"),
                    getInt(res, "panel-border-size", 1)));
        }

        // Finally, set the background color or image.
        prop = res.getProperty("panel-background-image");
        final Image img =
                prop == null ? null : AppFileLoader.loadFileAsImage(PleaseWait.class, prop, true);

        this.layered.setOpaque(true);

        if (img == null) {
            // Set a simple background color for the panel
            this.layered.setBackground(//
                    getColor(res, "panel-background-color", "gray80"));
        } else {
            // Install the image as the panel background
            final Icon icon = new ImageIcon(img);
            final JLabel lbl = new JLabel(icon);
            this.layered.setLayer(lbl, JLayeredPane.DEFAULT_LAYER.intValue());
            this.layered.add(lbl);
            lbl.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        }

        // Allow the window to catch key events.
        setFocusable(true);
    }

    /**
     * Create and place a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res  the resource properties that store GUI settings
     * @param name the name of the label, used as a prefix to contain the relevant resource settings
     */
    void createSingleLabel(final Properties res, final String name) {

        configureSingleLabel(res, new JLabel(), name);
    }

    /**
     * Place a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res  the resource properties that store GUI settings
     * @param lbl  the label to be configured
     * @param name the name of the label, used as a prefix to contain the relevant resource settings
     */
    private void configureSingleLabel(final Properties res, final JLabel lbl, final String name) {

        // Get font and color settings
        final Font font = getFont(res, name);

        Color shadow = null;
        if (res.getProperty(name + "-shadow-color") != null) {
            shadow = getColor(res, name + "-shadow-color", "white");
        }

        // Determine the x,y position and width
        final int x = getInt(res, name + "-left", 0);
        final int y = getInt(res, name + "-top", 0);
        int width = getInt(res, name + "-width", 0);

        // Determine the shadow offset and alpha
        int offset = 0;
        if (shadow != null) {
            offset = getInt(res, name + "-shadow-offset", 0);
        }

        // Determine the text alignment
        String prop = res.getProperty(name + "-alignment");

        int align = SwingConstants.LEFT;
        if (prop != null) {
            prop = prop.toUpperCase();

            align = switch (prop) {
                case "LEFT" -> SwingConstants.LEFT;
                case "CENTER" -> SwingConstants.CENTER;
                case "RIGHT" -> SwingConstants.RIGHT;
                default -> align;
            };
        }

        // Get the text itself, and build & place the label, placing the shadow first if present
        prop = res.getProperty(name + "-text");

        if (prop == null) {
            // No text, so no point adding anything
            return;
        }

        if (shadow != null && !prop.isEmpty()) {
            final JLabel shad = new JLabel(prop);
            shad.setOpaque(false);
            shad.setForeground(shadow);
            shad.setFont(font);
            shad.setHorizontalAlignment(align);
            this.layered.setLayer(shad, JLayeredPane.DEFAULT_LAYER.intValue() + 1);
            this.layered.add(shad);
            final Dimension dim = shad.getPreferredSize();
            shad.setBounds(x + offset, y + offset, width, dim.height);
        }

        // Set text only if value in res is not empty (otherwise, we leave existing text there,
        // allowing caller to preset text)
        if (!prop.isEmpty()) {
            lbl.setText(prop);
        }

        lbl.setOpaque(false);
        final Color color = getColor(res, name + "-color", "black");
        lbl.setForeground(color);
        lbl.setFont(font);
        lbl.setHorizontalAlignment(align);
        final Dimension dim = lbl.getPreferredSize();

        this.layered.setLayer(lbl, JLayeredPane.DEFAULT_LAYER.intValue() + 2);
        this.layered.add(lbl);

        if (width == 0) {
            width = dim.width;
        }

        lbl.setBounds(x, y, width, dim.height);
    }

    /**
     * Retrieve an integer value from the properties object. If the value name is not specified in the properties
     * object, a supplied default value is used.
     *
     * @param res  the properties object that holds GUI settings
     * @param name the name of the setting with the value
     * @param def  the default value
     * @return the retrieved value
     */
    private static int getInt(final Properties res, final String name, final int def) {

        int value = def;

        final String prop = res.getProperty(name);

        if (prop != null) {
            try {
                value = Integer.parseInt(prop);
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
            }
        }

        return value;
    }

    /**
     * Construct a new {@code Font} object based on settings in the properties object.
     *
     * @param res    the properties object that holds GUI settings
     * @param prefix the prefix of the properties to extract
     * @return the generated font
     */
    private static Font getFont(final Properties res, final String prefix) {

        final BundledFontManager fonts = BundledFontManager.getInstance();

        // Construct the font from settings
        String prop = res.getProperty(prefix + "-font-size");
        double size = 12.0;
        if (prop != null) {
            try {
                size = Double.parseDouble(prop);
            } catch (final NumberFormatException ex) {
                Log.warning("invalid float property: ", prop, ex);
            }
        }

        prop = res.getProperty(prefix + "-font-style");
        int style = Font.PLAIN;
        if (prop != null) {
            prop = prop.toUpperCase();

            if (prop.contains("BOLD")) {
                style |= Font.BOLD;
            }

            if (prop.contains("ITALIC")) {
                style |= Font.ITALIC;
            }
        }

        prop = res.getProperty(prefix + "-font-name");
        if (prop == null) {
            prop = "SANS";
        }

        Font font = fonts.getFont(prop, size, style);
        if (font == null) {

            // Provide a default font in case of emergency.
            font = fonts.getFont(BundledFontManager.SANS, 12.0, Font.PLAIN);

            if (font == null) {
                font = new Font("Dialog", Font.PLAIN, 12);
            }
        }

        return font;
    }

    /**
     * Construct a new {@code Color} object based on settings in the properties object. If the color name is not
     * specified in the properties object, a supplied default color name is used.
     *
     * @param res  the properties object that holds GUI settings
     * @param name the name of the setting with the color specification
     * @param def  the default color name
     * @return the generated color
     */
    private static Color getColor(final Properties res, final String name, final String def) {

        String prop = res.getProperty(name);

        if (prop == null) {
            prop = def;
        }

        return ColorNames.getColor(prop);
    }
}

/**
 * A class to construct the GUI in the AWT event thread.
 */
class PleaseWaitGUIBuilder implements Runnable {

    /** The owning panel. */
    private final PleaseWait owner;

    /** The resource properties from which to get GUI settings. */
    private final Properties res;

    /**
     * Construct a new {@code PleaseWaitGUIBuilder}.
     *
     * @param theOwner the owning panel
     * @param theRes   the resource properties from which to get GUI settings
     */
    PleaseWaitGUIBuilder(final PleaseWait theOwner, final Properties theRes) {

        this.owner = theOwner;
        this.res = theRes;
    }

    /**
     * Run method to construct the user interface, intended to be run in the AWT event dispatch thread.
     */
    @Override
    public void run() {

        // Set the border for the frame, which may be decorated or undecorated, and set the frame
        // size and background (color or image)
        this.owner.setupFrame(this.res);

        // Create all the text labels
        this.owner.createSingleLabel(this.res, "wait");
    }
}

/**
 * A class to display the dialog in the AWT event thread.
 */
class ShowPleaseWait implements Runnable {

    /** The please-wait panel to show. */
    private final PleaseWait panel;

    /**
     * Construct a new {@code ShowPleaseWait}.
     *
     * @param thePanel the panel to add to the desktop
     */
    ShowPleaseWait(final PleaseWait thePanel) {

        this.panel = thePanel;
    }

    /**
     * Runnable method to display the dialog; intended to be called in the AWT event thread.
     */
    @Override
    public void run() {

        this.panel.centerInDesktop();
        this.panel.setVisible(true);
        this.panel.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    }
}

/**
 * A class to close the dialog in the AWT event thread.
 */
class ClosePleaseWait implements Runnable {

    /** The please-wait panel to close. */
    private final PleaseWait panel;

    /**
     * Construct a new {@code ClosePleaseWait}.
     *
     * @param thePanel the panel to close
     */
    ClosePleaseWait(final PleaseWait thePanel) {

        this.panel = thePanel;
    }

    /**
     * Runnable method to close the dialog; intended to be called in the AWT event thread.
     */
    @Override
    public void run() {

        this.panel.setVisible(false);
        this.panel.dispose();
    }
}

/**
 * A resource bundle class that contains the default settings for the panel.
 */
class DefaultPleaseWaitSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6079418026570771151L;

    /** The default settings. */
    private static final String[][] CONTENTS = {//
            {"panel-title", "Operation in Progress"},
            {"panel-width", "350"},
            {"panel-height", "100"},
            {"panel-border-style", "line"},
            {"panel-border-size", "1"},
            {"panel-border-color", "black"},
            {"panel-background-color", "gray80"},
            {"wait-text",
                    "<HTML>This operation takes a few moments.<br>Please be patient...</HTML>"},
            {"wait-left", "15"},
            {"wait-top", "15"},
            {"wait-color", "black"},
            {"wait-font-name", "SANS"},
            {"wait-font-size", "16"},
            {"wait-font-style", "BOLD"},
            {"wait-alignment", "LEFT"}};

    /**
     * Constructs a new {@code DefaultPleaseWaitSkin} properties object.
     */
    DefaultPleaseWaitSkin() {

        super();

        for (final String[] content : CONTENTS) {
            setProperty(content[0], content[1]);
        }
    }
}
