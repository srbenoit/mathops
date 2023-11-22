package dev.mathops.app;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.ColorNames;
import dev.mathops.font.BundledFontManager;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * This class is a base class for internal frames that exist within the blocking window. It extends JInternalFrame to
 * add support for loading a set of "skin" preferences from a standard Properties file.
 */
public class InternalPanelBase extends JInternalFrame {

    /** The layer in {@code JLayeredPane} for backdrop images. */
    private static final int BACKDROP_LAYER = 0;

    /** The layer in {@code JLayeredPane} for label drop-shadows. */
    private static final int SHADOW_LAYER = BACKDROP_LAYER + 1;

    /** The layer in {@code JLayeredPane} for normal components. */
    private static final int MAIN_LAYER = BACKDROP_LAYER + 2;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4537398222182357290L;

    /** The object that is creating this panel. */
    private Object resOwner;

    /** The name of the logged in user. */
    private final String username;

    /** The set of listeners registered to receive action events. */
    private final List<ActionListener> listeners;

    /** The content pane for the internal frame. */
    private JPanel content;

    /** A unique number to assign to each action event sent. */
    private int eventId = 1;

    /**
     * Constructs a new {@code InternalPanelBase}.
     *
     * @param theResOwner the object that is creating this panel; to be used as the base for resource loading
     * @param theUsername the name of the logged in user
     */
    public InternalPanelBase(final Object theResOwner, final String theUsername) {

        super();

        this.resOwner = theResOwner;
        this.username = theUsername;
        this.listeners = new ArrayList<>(1);
    }

    /**
     * Sets the object that is creating this panel; to be used as the base for resource loading.
     *
     * @param theResOwner the resource owner
     */
    protected final void setResOwner(final Object theResOwner) {

        this.resOwner = theResOwner;
    }

    /**
     * Centers the internal frame within its owning desktop.
     */
    public final void centerInDesktop() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Container parent = getParent();
        final Dimension screen = parent.getSize();
        final Dimension size = getSize();

        setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
    }

    /**
     * Makes an internal frame full-screen.
     */
    public void makeFullscreen() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final JDesktopPane desk = getDesktopPane();
        final Dimension screen = desk.getSize();

        desk.getDesktopManager().maximizeFrame(this);

        final int borderWidth = (getSize().width - getContentPane().getSize().width) / 2;
        final int titleWidth = getSize().height - getContentPane().getSize().height - borderWidth;

        final Dimension size = new Dimension(screen.width + (borderWidth << 1),
                screen.height + titleWidth + borderWidth);

        desk.getDesktopManager().setBoundsForFrame(this, -borderWidth, -titleWidth, size.width, size.height);

        setPreferredSize(size);
    }

    /**
     * Add an action listener that will be notified whenever the panel fires an action. Each subclass will specify a set
     * of actions it will fire. These can be used to detect when the user accepts or cancels a dialog, makes a
     * selection, and so on. Typically, a client program that uses a subclass will create the object and register as a
     * listener before showing the object, then enter a "wait". In the action performed method the waiting thread is
     * interrupted, and processing continues.
     *
     * @param listener the action listener to register
     */
    public final void addActionListener(final ActionListener listener) {

        // Protect modifications to the listeners list from thread interference
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    /**
     * Sends an action command to all registered action listeners.
     *
     * @param command the command to send
     */
    protected final void fireAction(final String command) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final ActionEvent event = new ActionEvent(this, this.eventId, command);
        ++this.eventId;

        // Protect modifications to listeners list from thread interference
        synchronized (this.listeners) {
            for (final ActionListener listener : this.listeners) {
                listener.actionPerformed(event);
            }
        }
    }

    /**
     * Examines a set of standard properties settings and configure the frame. This includes setting the frame size,
     * building the border, and setting the background color or image.
     *
     * @param res the properties settings governing GUI look
     */
    public void setupFrame(final Properties res) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Set the title, if any
        setTitle(res.getProperty("panel-title"));

        // Setup the internal frame look.
        setBorder(BorderFactory.createLineBorder(Color.black, 1));

        // Generate the content panel, with no layout manager
        this.content = new JPanel();
        this.content.setLayout(null);
        setContentPane(this.content);

        // Configure the frame size
        final int width = getInt(res, "panel-width", 300);
        final int height = getInt(res, "panel-height", 200);
        this.content.setPreferredSize(new Dimension(width, height));
        this.content.setLocation(0, 0);
        setSize(width, height);

        // Configure the frame border. Possible styles are "line" (in which case
        // "panel-border-color" and "panel-border-size" are used), "..."
        String prop = res.getProperty("panel-border-style") == null ? null : res.getProperty("panel-border-style");

        if ("line".equals(prop)) {
            this.content.setBorder(BorderFactory.createLineBorder(getColor(res, "panel-border-color", "black"),
                    getInt(res, "panel-border-size", 1)));
        }

        // Finally, set the background color or image.
        prop = res.getProperty("panel-background-image") == null ? null : res.getProperty("panel-background-image");

        final Image img = (prop == null) ? null : FileLoader.loadFileAsImage(this.resOwner.getClass(), prop, true);

        if (img == null) {
            // Set a simple background color for the panel
            this.content.setOpaque(true);
            this.content.setBackground(getColor(res, "panel-background-color", "gray80"));
        } else {
            // Install the image as the panel background
            this.content.setOpaque(false);

            final Icon icon = new ImageIcon(img);
            final JLabel lbl = new JLabel(icon);
            getLayeredPane().setLayer(lbl, BACKDROP_LAYER);
            getLayeredPane().add(lbl);
            lbl.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        }

        // Allow the window to catch key events.
        setFocusable(true);
    }

    /**
     * Creates and place a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res  the resource properties that store GUI settings
     * @param name the name of the label, used as a prefix to obtain the relevant resource settings
     * @return the created label
     */
    public final JLabel createSingleLabel(final Properties res, final String name) {

        final JLabel label = new JLabel();

        configureSingleLabel(res, label, name);

        return label;
    }

    /**
     * Creates and place a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res          the resource properties that store GUI settings
     * @param name         the name of the label, used as a prefix to obtain the relevant resource settings
     * @param overrideText the text for the label, overriding that in the skin
     * @return the created label
     */
    public JLabel createSingleLabel(final Properties res, final String name, final String overrideText) {

        final JLabel label = new JLabel();

        configureSingleLabel(res, label, name, overrideText);

        return label;
    }

    /**
     * Places a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res  the resource properties that store GUI settings
     * @param lbl  the label to be configured
     * @param name the name of the label, used as a prefix to obtain the relevant resource settings
     */
    protected final void configureSingleLabel(final Properties res, final JLabel lbl, final String name) {

        // Get the label text from the skin
        final String prop = res.getProperty(name + "-text");

        if (prop != null) {
            configureSingleLabel(res, lbl, name, prop);
        }
    }

    /**
     * Places a single label on the panel, applying all the style settings from the resource properties.
     *
     * @param res          the resource properties that store GUI settings
     * @param lbl          the label to be configured
     * @param name         the name of the label, used as a prefix to obtain the relevant resource settings
     * @param overrideText the text for the label, overriding that in the skin
     */
    private void configureSingleLabel(final Properties res, final JLabel lbl, final String name,
                                      final String overrideText) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Get font and color settings
        final Font font = getFont(res, name);
        final Color color = getColor(res, name + "-color", "black");
        final Color shadow = res.getProperty(name + "-shadow-color") == null
                ? null : getColor(res, name + "-shadow-color", "white");

        // Determine the text alignment
        final int align = getTextAlign(res, name);

        // Replace substitution strings in the test with user info, if known
        final String resText = res.getProperty(name + "-text");
        final String theText = resText == null || CoreConstants.EMPTY.equals(resText) ? overrideText : resText;
        final String text = (this.username == null) ? theText : theText.replace("$USERNAME", this.username);

        // Set text only if value in res is not empty (otherwise, we leave existing text there,
        // allowing caller to preset text)
        if (text != null && !text.isEmpty()) {
            lbl.setText(text);
        }

        lbl.setOpaque(false);
        lbl.setForeground(color);
        lbl.setFont(font);
        lbl.setHorizontalAlignment(align);
        final Dimension dim = lbl.getPreferredSize();

        getLayeredPane().setLayer(lbl, MAIN_LAYER);
        getLayeredPane().add(lbl);

        final int width = getInt(res, name + "-width", dim.width);
        final int x = getInt(res, name + "-left", 0);
        final int y = getInt(res, name + "-top", 0);

        lbl.setBounds(x, y, width, dim.height);
        lbl.setLocation(x, y);

        if (shadow != null) {
            final int offset = getInt(res, name + "-shadow-offset", 0);
            final JLabel shad = new JLabel(text == null ? CoreConstants.EMPTY : text);
            shad.setOpaque(false);
            shad.setForeground(shadow);
            shad.setFont(font);
            shad.setHorizontalAlignment(align);
            getLayeredPane().setLayer(shad, SHADOW_LAYER);
            getLayeredPane().add(shad);
            shad.setBounds(x + offset, y + offset, width, shad.getPreferredSize().height);
        }
    }

    /**
     * Gets the test alignment property.
     *
     * @param res  the resource properties that store GUI settings
     * @param name the name of the label, used as a prefix to obtain the relevant resource settings
     * @return the text alignment
     */
    private static int getTextAlign(final Properties res, final String name) {

        String prop = res.getProperty(name + "-alignment");
        final int align;

        if (prop == null) {
            align = SwingConstants.LEFT;
        } else {
            prop = prop.toUpperCase(Locale.ROOT);
            align = switch (prop) {
                case "CENTER" -> SwingConstants.CENTER;
                case "RIGHT" -> SwingConstants.RIGHT;
                default -> SwingConstants.LEFT;
            };
        }

        return align;
    }

    /**
     * Given a button and a prefix in the properties list, configures the button with the properties.
     *
     * @param res    the properties object with panel settings
     * @param btn    the button to be configured
     * @param prefix the prefix for the settings in the properties object
     */
    public void configureButton(final Properties res, final AbstractButton btn,
                                final String prefix) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Font font = getFont(res, prefix);
        final Color fg = getColor(res, prefix + "-foreground", "white");
        final Color bg = getColor(res, prefix + "-background", "black");

        btn.setFocusPainted(false);
        btn.setFont(font);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setText(res.getProperty(prefix + "-text"));

        // Set the border
        final String prop = res.getProperty(prefix + "-border");

        if ("FALSE".equalsIgnoreCase(prop)) {
            btn.setOpaque(false);
            btn.setBorder(null);
        }

        getLayeredPane().setLayer(btn, MAIN_LAYER);
        getLayeredPane().add(btn);
        final Dimension dim = btn.getPreferredSize();

        int width = getInt(res, prefix + "-width", 0);
        if (width == 0) {
            width = dim.width;
        }

        final int x = getInt(res, prefix + "-left", 0);
        final int y = getInt(res, prefix + "-top", 0);
        btn.setBounds(x, y, width, dim.height);
    }

    /**
     * Constructs a new {@code Font} object based on settings in the properties object.
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
            } catch (final NumberFormatException e) {
                Log.severe("Unparseable float value for '", prefix, "-font-size' in properties: ", prop, e);
            }
        }

        int style = Font.PLAIN;
        prop = res.getProperty(prefix + "-font-style");

        if (prop != null) {
            prop = prop.toUpperCase(Locale.ROOT);

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
            // Provide fallback font
            font = fonts.getFont(BundledFontManager.SANS, 12.0, Font.PLAIN);
            if (font == null) {
                font = new Font("Dialog", Font.PLAIN, 12);
            }
        }

        return font;
    }

    /**
     * Constructs a new {@code Color} object based on settings in the properties object. If the color name is not
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

    /**
     * Retrieves an integer value from the properties object. If the value name is not specified in the properties
     * object, a supplied default value is used.
     *
     * @param res  the properties object that holds GUI settings
     * @param name the name of the setting with the value
     * @param def  the default value
     * @return the retrieved value
     */
    private static int getInt(final Properties res, final String name, final int def) {

        final String prop = res.getProperty(name);
        int value = def;

        if (prop != null) {
            try {
                value = Integer.parseInt(prop);
            } catch (final NumberFormatException ex) {
                Log.severe("Unparseable int value for '", name, "' in properties: ", prop, ex);
            }
        }

        return value;
    }
}
