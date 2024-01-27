package dev.mathops.app;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

/**
 * An internal frame popup dialog that displays a message and waits a click on its OK button or either an ESC or ENTER
 * key press.
 */
public final class PopupPanel extends InternalPanelBase implements ActionListener {

    /** Style code to include an OK button only. */
    public static final int STYLE_OK = 1;

    /** Style code to include a YES and a NO button, YES is default. */
    public static final int STYLE_YES_NO = 2;

    /** Style code to include a YES and a NO button, NO is default. */
    public static final int STYLE_NO_YES = 3;

    /** Style code to include a text entry field, and OK/CANCEL buttons. */
    public static final int STYLE_OK_CANCEL = 4;

    /** Style code to build a popup with no buttons. */
    public static final int STYLE_NO_BUTTONS = 5;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6413281846228869018L;

    /** A label to display a data entry field. */
    private JTextField field;

    /** The name of the button the user clicks to close the panel. */
    private String buttonClicked;

    /**
     * Constructs a new {@code PopupPanel}, creating the user interface from the skin settings.
     *
     * @param theResOwner       the object that is creating the dialog. Skin resources are loaded from the PleaseWaitSkin path
     *                    relative to this object's position in the source tree
     * @param theMessage1 line 1 of the message to display in the panel
     * @param theWarn     the optional warning
     * @param theMessage2 line 2 of the message to display in the panel
     * @param style       either STYLE_OK or STYLE_YES_NO
     */
    public PopupPanel(final Object theResOwner, final String theMessage1, final String theWarn,
                      final String theMessage2, final int style) {

        super(theResOwner, null);

        final Properties res = new DefaultPopupPanelSkin();

        buildUI(res, theMessage1, theWarn, theMessage2, style);
        pack();
    }

    /**
     * Displays a popup message on the screen and waits for the user to acknowledge it.
     *
     * @param owner    the object that is creating the dialog. Skin resources are loaded from the PleaseWaitSkin path
     *                 relative to this object's position in the source tree
     * @param parent   the panel that will contain this popup
     * @param message1 line 1 of the message to display
     * @param warn     optional warning to show below line 1
     * @param message2 line 3 of the message to display
     * @param style    one of the style constants in this class
     * @return the name of the button the user clicked on to close the panel
     */
    public static String showPopupMessage(final Object owner, final JPanel parent,
                                          final String message1, final String warn, final String message2,
                                          final int style) {

        Log.info("WARN = ", warn);

        final PanelBuilder builder =
                new PanelBuilder(owner, parent, message1, warn, message2, style);

        try {
            SwingUtilities.invokeAndWait(builder);
        } catch (final InterruptedException | InvocationTargetException ex) {
            Log.warning(ex);
        }

        final PopupPanel panel = builder.getPanel();
        final String cmd = panel.waitForClose();

        final Runnable killer = new PanelKiller(panel, builder.getFrame(), parent);

        try {
            SwingUtilities.invokeAndWait(killer);
        } catch (final InterruptedException | InvocationTargetException ex) {
            Log.warning(ex);
        }

        return cmd;
    }

    /**
     * Constructs the user interface using a specific set of properties.
     *
     * @param res         the properties settings governing GUI look
     * @param theMessage1 line 1 of the message to display in the panel
     * @param theWarn     the optional warning
     * @param theMessage2 line 2 of the message to display in the panel
     * @param style       either STYLE_OK or STYLE_YES_NO
     */
    private void buildUI(final Properties res, final String theMessage1, final String theWarn,
                         final String theMessage2, final int style) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Set the border for the frame, which may be decorated or undecorated,
        // and set the frame size and background (color or image)
        setupFrame(res);

        // Create the text label
        final JLabel message1 = createSingleLabel(res, "message1");
        message1.setText(theMessage1 == null ? CoreConstants.EMPTY : theMessage1);

        final JLabel warn;
        if (theWarn != null) {
            warn = createSingleLabel(res, "warn");
            warn.setText(theWarn);
        }

        final JLabel message2 = createSingleLabel(res, "message2");
        message2.setText(theMessage2 == null ? CoreConstants.EMPTY : theMessage2);

        if (style == STYLE_OK_CANCEL) {
            message2.setVisible(false);
            this.field = new JTextField(10);
            this.field.setLocation(message2.getLocation());
            this.field.setSize(message2.getSize());
            this.field.setFont(message2.getFont());
            this.field.setForeground(message2.getForeground());
        }

        buildButton(res, style);
    }

    /**
     * Builds a button and installs it in the root pane.
     *
     * @param res   the properties settings governing GUI look
     * @param style the dialog style
     */
    private void buildButton(final Properties res, final int style) {

        switch (style) {

            case STYLE_OK:
                final JButton okBtn = new JButton();
                okBtn.setActionCommand("Ok");
                okBtn.addActionListener(this);
                configureButton(res, okBtn, "ok");

                getRootPane().setDefaultButton(okBtn);
                break;

            case STYLE_YES_NO:
            case STYLE_NO_YES:
                final JButton yesBtn = new JButton();
                yesBtn.setActionCommand("Yes");
                yesBtn.addActionListener(this);
                configureButton(res, yesBtn, "yes");

                final JButton noBtn = new JButton();
                noBtn.setActionCommand("No");
                noBtn.addActionListener(this);
                configureButton(res, noBtn, "no");

                getRootPane().setDefaultButton((style == STYLE_YES_NO) ? yesBtn : noBtn);
                break;

            case STYLE_OK_CANCEL:
                final JButton okBtn2 = new JButton();
                okBtn2.setActionCommand("Ok");
                okBtn2.addActionListener(this);
                configureButton(res, okBtn2, "ok2");

                final JButton cancelBtn = new JButton();
                cancelBtn.setActionCommand("Cancel");
                cancelBtn.addActionListener(this);
                configureButton(res, cancelBtn, "cancel");

                getRootPane().setDefaultButton(okBtn2);
                break;

            default:
                break;
        }
    }

    /**
     * Waits for the panel to be closed.
     *
     * @return the name of the button clicked ('OK', 'Yes', or 'No')
     */
    private String waitForClose() {

        while (this.isVisible()) {

            try {
                Thread.sleep(100L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        final String result;

        if (this.field != null && "Ok".equals(this.buttonClicked)) {

            // If clicking on "OK" when a field is active, return field value.
            result = this.field.getText();
        } else {
            result = this.buttonClicked;
        }

        return result;
    }

    /**
     * Handler for action events generated by pressing the Login or Exit buttons.
     *
     * @param e the {@code ActionEvent} to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        this.buttonClicked = e.getActionCommand();
        setVisible(false);
    }
}

/**
 * Generates the user interface for a popup panel.
 */
class PanelBuilder implements Runnable {

    /** The object that is creating this panel. */
    private final Object owner;

    /** The popup panel being managed. */
    private PopupPanel panel;

    /** The owning panel, if any. */
    private final JPanel parent;

    /** A constructed owning frame, if no owner was supplied. */
    private JFrame frame;

    /** Line 1 of the message to display. */
    private final String message1;

    /** Optional warning to show below line 1. */
    private final String warn;

    /** Line 2 of the message to display. */
    private final String message2;

    /** The panel style. */
    private final int style;

    /**
     * Constructs a new {@code PanelBuilder}.
     *
     * @param theOwner    the object that is creating the dialog. Skin resources are loaded from the PleaseWaitSkin path
     *                    relative to this object's position in the source tree
     * @param theParent   the panel to which this panel should be added, of any
     * @param theMessage1 line 1 of the message to display
     * @param theWarn     the optional warning
     * @param theMessage2 line 2 of the message to display
     * @param theStyle    one of the style constants in this class
     */
    PanelBuilder(final Object theOwner, final JPanel theParent, final String theMessage1,
                 final String theWarn, final String theMessage2, final int theStyle) {

        this.owner = theOwner;
        this.parent = theParent;
        this.message1 = theMessage1;
        this.warn = theWarn;
        this.message2 = theMessage2;
        this.style = theStyle;
    }

    /**
     * Gets the frame constructed if no parent was supplied.
     *
     * @return the constructed frame parent
     */
    public JFrame getFrame() {

        return this.frame;
    }

    /**
     * Gets the popup panel that the class constructed.
     *
     * @return the generated panel
     */
    public PopupPanel getPanel() {

        return this.panel;
    }

    /**
     * Runnable method to perform all Swing manipulations in the AWT thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel =
                new PopupPanel(this.owner, this.message1, this.warn, this.message2, this.style);

        if (this.parent == null) {
            this.frame = new JFrame("Error");
            this.frame.setContentPane(this.panel);
            this.frame.pack();

            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            final Dimension size = this.frame.getSize();

            this.frame.setLocation((screen.width - size.width) / 2,
                    (screen.height - size.height) / 2);
        } else {
            this.parent.add(this.panel);
            this.panel.centerInDesktop();
        }

        this.panel.setVisible(true);
    }
}

/**
 * Removes a panel and disposes of it.
 */
class PanelKiller implements Runnable {

    /** The panel in which the panel to be killed is installed. */
    private final JPanel owner;

    /** A constructed frame in which the panel to be killed is installed. */
    private final JFrame frame;

    /** The panel to be killed. */
    private final PopupPanel panel;

    /**
     * Constructs a new {@code PanelKiller}.
     *
     * @param thePanel the label to be killed
     * @param theFrame a constructed frame in which the panel to be killed is installed
     * @param theOwner the panel in which the panel to be killed is installed
     */
    PanelKiller(final PopupPanel thePanel, final JFrame theFrame, final JPanel theOwner) {

        this.owner = theOwner;
        this.frame = theFrame;
        this.panel = thePanel;
    }

    /**
     * Runnable method to perform all Swing manipulations in the AWT thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.owner == null) {
            this.frame.setVisible(false);
            this.panel.dispose();
            this.frame.dispose();
        } else {
            this.panel.dispose();
            this.owner.remove(this.panel);
        }
    }
}

/**
 * A resource bundle class that contains the default settings for the panel.
 */
class DefaultPopupPanelSkin extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4108926475327590064L;

    /** A common property value. */
    private static final String RED = "red";

    /** A common property value. */
    private static final String GRAY_70 = "gray70";

    /** A common property value. */
    private static final String BTN_WIDTH = "120";

    /** A common property value. */
    private static final String TOP_Y = "150";

    /** A common property value. */
    private static final String TRUE = "true";

    /** A common property value. */
    private static final String BLACK = "black";

    /** A common property value. */
    private static final String PLAIN = "PLAIN";

    /** A common property value. */
    private static final String BOLD = "BOLD";

    /** A common property value. */
    private static final String SANS = "SANS";

    /** A common property value. */
    private static final String CENTER = "CENTER";

    /** Default settings. */
    private static final String[][] CONTENTS = {//
            {"panel-title", "Checkin Station"},
            {"panel-width", "790"},
            {"panel-height", "220"},
            {"panel-border-style", "line"},
            {"panel-border-size", "1"},
            {"panel-border-color", BLACK},
            {"panel-background-color", "MistyRose"},

            {"ok-text", "Ok"},
            {"ok-top", TOP_Y},
            {"ok-left", "345"},
            {"ok-width", "100"},
            {"ok-foreground", BLACK},
            {"ok-background", GRAY_70},
            {"ok-font-name", SANS},
            {"ok-font-size", "20"},
            {"ok-font-style", PLAIN},
            {"ok-border", TRUE},

            {"yes-text", "Yes"},
            {"yes-top", TOP_Y},
            {"yes-left", "265"},
            {"yes-width", BTN_WIDTH},
            {"yes-foreground", BLACK},
            {"yes-background", GRAY_70},
            {"yes-font-name", SANS},
            {"yes-font-size", "20"},
            {"yes-font-style", PLAIN},
            {"yes-border", TRUE},

            {"no-text", "No"},
            {"no-top", TOP_Y},
            {"no-left", "405"},
            {"no-width", BTN_WIDTH},
            {"no-foreground", BLACK},
            {"no-background", GRAY_70},
            {"no-font-name", SANS},
            {"no-font-size", "20"},
            {"no-font-style", PLAIN},
            {"no-border", TRUE},

            {"ok2-text", "Ok"},
            {"ok2-top", TOP_Y},
            {"ok2-left", "265"},
            {"ok2-width", BTN_WIDTH},
            {"ok2-foreground", BLACK},
            {"ok2-background", GRAY_70},
            {"ok2-font-name", SANS},
            {"ok2-font-size", "20"},
            {"ok2-font-style", PLAIN},
            {"ok2-border", TRUE},

            {"cancel-text", "Cancel"},
            {"cancel-top", TOP_Y},
            {"cancel-left", "405"},
            {"cancel-width", BTN_WIDTH},
            {"cancel-foreground", BLACK},
            {"cancel-background", GRAY_70},
            {"cancel-font-name", SANS},
            {"cancel-font-size", "20"},
            {"cancel-font-style", PLAIN},
            {"cancel-border", TRUE},

            {"message1-text", CoreConstants.SPC},
            {"message1-top", "30"},
            {"message1-left", "0"},
            {"message1-width", "790"},
            {"message1-color", BLACK},
            {"message1-font-name", SANS},
            {"message1-font-size", "30"},
            {"message1-font-style", PLAIN},
            {"message1-alignment", CENTER},

            {"warn-text", CoreConstants.SPC},
            {"warn-top", "72"},
            {"warn-left", "0"},
            {"warn-width", "790"},
            {"warn-color", RED},
            {"warn-font-name", SANS},
            {"warn-font-size", "24"},
            {"warn-font-style", BOLD},
            {"warn-alignment", CENTER},

            {"message2-text", CoreConstants.SPC},
            {"message2-top", "100"},
            {"message2-left", "0"},
            {"message2-width", "790"},
            {"message2-color", BLACK},
            {"message2-font-name", SANS},
            {"message2-font-size", "24"},
            {"message2-font-style", PLAIN},
            {"message2-alignment", CENTER},};

    /**
     * Constructs a new {@code DefaultPopupPanelSkin} properties object.
     */
    DefaultPopupPanelSkin() {

        super();

        for (final String[] content : CONTENTS) {
            setProperty(content[0], content[1]);
        }
    }
}
