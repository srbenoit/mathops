package dev.mathops.app.exam;

import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.exam.IExamSessionListener;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import dev.mathops.font.FontSpec;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * A generalized bottom bar panel for use in testing and homework applications. This panel contains a submission button
 * with a label that can be set, which fires an event to an ActionListener when pressed. It can read its font, color and
 * layout settings from a standardized skin definition.
 */
final class BottomBarPanel extends JPanel implements ActionListener, IExamSessionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8862893709269185709L;

    // Skin settings used to customize the display.

    /** The background color, as read from the skin. */
    private Color backgroundColor;

    /** The font to use for the button. */
    private Font buttonFont;

    // Data used internally by the class

    /** The button to submit the exam for grading. */
    private JButton[] buttons;

    /** The button to make text larger. */
    private JButton larger;

    /** The button to make text smaller. */
    private JButton smaller;

    /** The palette of available colors. */
    private final JToggleButton[] palette;

    /** The base size for the font to use for the button. */
    private float buttonFontBaseSize;

    /** A list of timer listeners to notify when the button is pressed. */
    private List<ActionListener> listeners;

    /** The size adjustment. */
    private int sizeAdjustment;

    /**
     * Constructs a new {@code BottomBarPanel}.
     *
     * @param theExamSession the exam session
     * @param practice       {@code true} if this is a practice assignment; {@code false} otherwise
     * @param skin           the properties object containing the skin settings
     */
    BottomBarPanel(final ExamSession theExamSession, final boolean practice, final Properties skin) {

        super();

        final String[] labels = {null};

        final boolean showingAnswers = theExamSession.getState().showAnswers || theExamSession.getState().showSolutions;

        if (showingAnswers) {
            labels[0] = skin.getProperty("bottom-bar-lbl-show-answers");
        }

        if (labels[0] == null && practice) {
            labels[0] = skin.getProperty("bottom-bar-lbl-practice");
        }

        if (labels[0] == null) {
            labels[0] = skin.getProperty("bottom-bar-lbl");
        }

        final String[] commands = {"Grade"};

        this.palette = new JToggleButton[4];
        construct(labels, commands, skin);

        theExamSession.addListener(this);
    }

    /**
     * Constructs a new {@code BottomBarPanel}.
     *
     * @param theButtonLabels   the text labels for the buttons
     * @param theButtonCommands the action commands for the buttons
     * @param skin              the properties object containing the skin settings
     */
    private void construct(final String[] theButtonLabels, final String[] theButtonCommands, final Properties skin) {

        final BundledFontManager fonts = BundledFontManager.getInstance();
        final FontSpec spec = new FontSpec();

        this.listeners = new ArrayList<>(4);

        // Load the skin
        String prop = skin.getProperty("bottom-bar-background-color");
        this.backgroundColor = prop != null ? ColorNames.getColor(prop) : Color.WHITE;

        prop = skin.getProperty("bottom-bar-button-font");
        spec.fontName = prop != null ? prop : BundledFontManager.SANS;

        prop = skin.getProperty("bottom-bar-button-size");

        this.buttonFontBaseSize = 0.0f;

        try {
            this.buttonFontBaseSize = prop != null ? Float.parseFloat(prop) : 20.0f;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            this.buttonFontBaseSize = 20.0f;
        }

        spec.fontSize = (double) this.buttonFontBaseSize;

        prop = skin.getProperty("bottom-bar-button-style");
        spec.fontStyle = Font.PLAIN;

        if (prop != null) {
            if (prop.contains("italic") || prop.contains("ITALIC")) {
                spec.fontStyle = spec.fontStyle | Font.ITALIC;
            }
            if (prop.contains("bold") || prop.contains("BOLD")) {
                spec.fontStyle = spec.fontStyle | Font.BOLD;
            }
        }

        this.buttonFont = fonts.getFont(spec);

        prop = skin.getProperty("bottom-bar-border-size");

        int borderSize;
        try {
            borderSize = prop != null ? Integer.parseInt(prop) : 1;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            borderSize = 1;
        }

        prop = skin.getProperty("bottom-bar-border-inset");

        int borderInset;
        try {
            borderInset = prop != null ? Integer.parseInt(prop) : 1;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            borderInset = 1;
        }

        prop = skin.getProperty("bottom-bar-border-color");

        final String color = prop != null ? prop : "gray";
        final Color borderColor = ColorNames.getColor(color);

        prop = skin.getProperty("bottom-bar-padding-size");

        int padding;
        try {
            padding = prop != null ? Integer.parseInt(prop) : 5;
        } catch (final NumberFormatException ex) {
            Log.warning(ex);
            padding = 5;
        }

        // Construct the GUI, making sure it's done in the AWT thread.
        final Runnable builder = new BottomBarPanelBuilder(this, borderSize, borderColor, borderInset, padding,
                theButtonLabels, theButtonCommands);

        if (SwingUtilities.isEventDispatchThread()) {
            builder.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(builder);
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        }

        if (this.buttons != null) {
            for (final JButton button : this.buttons) {
                button.addActionListener(this);
            }
        }

        this.larger.addActionListener(this);
        this.smaller.addActionListener(this);

        for (final JToggleButton jToggleButton : this.palette) {
            jToggleButton.addActionListener(this);
        }
    }

    /**
     * Enables or disables the panel.
     *
     * @param enabled {@code true} to enable; {@code false} to disable
     */
    @Override
    public void setEnabled(final boolean enabled) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        super.setEnabled(enabled);

        for (final JButton button : this.buttons) {
            button.setEnabled(enabled);
        }
    }

    /**
     * Sets the button label.
     *
     * @param command the command of the button whose text is being updated
     * @param label   text to install in the button label
     */
    void setButtonLabel(final String command, final String label) {

        SwingUtilities.invokeLater(new BottomBarPanelButtonUpdater(this, command, label));
    }

    /**
     * Adds a listener to receive action events when the button is pressed.
     *
     * @param listener the listener to receive events
     */
    void addActionListener(final ActionListener listener) {

        this.listeners.add(listener);
    }

    /**
     * Handler for clicks on the submission button.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final String cmd = e.getActionCommand();

        if (!"Larger".equals(cmd) && !"Smaller".equals(cmd) && !"color-white".equals(cmd)
                && !"color-gold".equals(cmd) && !"color-purple".equals(cmd) && !"color-blue".equals(cmd)) {
            // Disable the buttons to prevent multiple clicks
            for (final JButton button : this.buttons) {
                button.setEnabled(false);
            }
        }

        // Call all registered listeners
        for (final ActionListener listener : this.listeners) {
            listener.actionPerformed(e);
        }
    }

    /**
     * Make the window render larger, up to some limit.
     */
    void larger() {

        if (this.sizeAdjustment < 5) {
            ++this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Make the window render smaller, down to some limit.
     */
    void smaller() {

        if (this.sizeAdjustment > -3) {
            --this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    Color getBackgroundColor() {

        return this.backgroundColor;
    }

//    /**
//     * Sets the background color.
//     *
//     * @param theBackgroundColor the new background color
//     */
//    public void setBackgroundColor(final Color theBackgroundColor) {
//
//        this.backgroundColor = theBackgroundColor;
//    }

    /**
     * Gets the button font.
     *
     * @return the button font
     */
    Font getButtonFont() {

        return this.buttonFont;
    }

//    /**
//     * Sets the button font.
//     *
//     * @param theButtonFont the new button font
//     */
//    public void setButtonFont(final Font theButtonFont) {
//
//        this.buttonFont = theButtonFont;
//    }

    /**
     * Gets the button list.
     *
     * @return the button list
     */
    public JButton[] getButtons() {

        return this.buttons;
    }

    /**
     * Sets the button list.
     *
     * @param theButtons the new button list
     */
    public void setButtons(final JButton[] theButtons) {

        this.buttons = theButtons;
    }

    /**
     * Gets the larger button.
     *
     * @return the larger button
     */
    public JButton getLarger() {

        return this.larger;
    }

    /**
     * Sets the larger button.
     *
     * @param theLarger the new larger button
     */
    public void setLarger(final JButton theLarger) {

        this.larger = theLarger;
    }

    /**
     * Gets the smaller button.
     *
     * @return the smaller button
     */
    public JButton getSmaller() {

        return this.smaller;
    }

    /**
     * Sets the smaller button.
     *
     * @param theSmaller the new smaller button
     */
    public void setSmaller(final JButton theSmaller) {

        this.smaller = theSmaller;
    }

    /**
     * Gets the palette buttons.
     *
     * @return the palette buttons
     */
    public JToggleButton[] getPalette() {

        return this.palette;
    }

//    /**
//     * Gets the button font base size.
//     *
//     * @return the button font base size
//     */
//    public float getButtonFontBaseSize() {
//
//        return this.buttonFontBaseSize;
//    }

//    /**
//     * Sets the button font base size.
//     *
//     * @param theButtonFontBaseSize the new button font base size
//     */
//    public void setButtonFontBaseSize(final float theButtonFontBaseSize) {
//
//        this.buttonFontBaseSize = theButtonFontBaseSize;
//    }

//    /**
//     * Gets the showing answers flag.
//     *
//     * @return the showing answers flag
//     */
//    public boolean isShowingAnswers() {
//
//        return this.showingAnswers;
//    }

//    /**
//     * Sets the showing answers flag.
//     *
//     * @param theShowingAnswers the new showing answers flag
//     */
//    public void setShowingAnswers(final boolean theShowingAnswers) {
//
//        this.showingAnswers = theShowingAnswers;
//    }

//    /**
//     * Gets the size adjustment.
//     *
//     * @return the size adjustment
//     */
//    public int getSizeAdjustment() {
//
//        return this.sizeAdjustment;
//    }

//    /**
//     * Sets the size adjustment.
//     *
//     * @param theSizeAdjustment the new size adjustment
//     */
//    public void setSizeAdjustment(final int theSizeAdjustment) {
//
//        this.sizeAdjustment = theSizeAdjustment;
//    }

    /**
     * Updates the fonts for buttons and labels based on an updated size factor.
     */
    private void updateFonts() {

        final float fontFactor = (float) StrictMath.pow(2.5, (double) this.sizeAdjustment / 4.0);
        final float buttonFontSize = this.buttonFontBaseSize * fontFactor;

        for (final JButton button : this.buttons) {
            button.setFont(button.getFont().deriveFont(buttonFontSize));
        }

        this.larger.setFont(this.larger.getFont().deriveFont(buttonFontSize));
        this.smaller.setFont(this.smaller.getFont().deriveFont(buttonFontSize));

        revalidate();
    }

    /**
     * Called when the state of the exam session changes.
     */
    @Override
    public void examSessionStateChanged(final EExamSessionState newState) {

        // final boolean newShowingAnswers = newState.isShowAnswers() ||
        // newState.showSolutions;

        // if (newShowingAnswers) {
        // if (!this.showingAnswers) {
        // // TODO:
        // }
        // } else {
        // if (this.showingAnswers) {
        // // TODO:
        // }
        // }
    }
}

/**
 * Configures the bottom bar panel from within the AWT event thread.
 */
final class BottomBarPanelBuilder implements Runnable {

    /** The panel to be configured. */
    private final BottomBarPanel panel;

    /** The size of border to add around the panel. */
    private final int border;

    /** The amount by which to inset the border. */
    private final int inset;

    /** The border color. */
    private final Color color;

    /** The amount of padding around the interior objects. */
    private final int padding;

    /** The button label strings. */
    private final String[] labels;

    /** The button action commands. */
    private final String[] commands;

    /**
     * Constructs a new {@code BottomBarPanelBuilder}.
     *
     * @param thePanel       the panel to be configured
     * @param borderSize     the size of border to add around the panel
     * @param borderColor    the color for the border
     * @param borderInset    the amount by which to inset the border
     * @param thePadding     the amount of padding around interior objects
     * @param buttonLabels   the text labels for the buttons
     * @param buttonCommands the action commands for the buttons
     */
    BottomBarPanelBuilder(final BottomBarPanel thePanel, final int borderSize, final Color borderColor,
                          final int borderInset, final int thePadding, final String[] buttonLabels,
                          final String[] buttonCommands) {

        this.panel = thePanel;
        this.border = borderSize;
        this.inset = borderInset;
        this.color = borderColor;
        this.padding = thePadding;
        this.labels = buttonLabels;
        this.commands = buttonCommands;
    }

    /**
     * Runnable method to perform the updates in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Set up the panel itself
        this.panel.setLayout(new BorderLayout(0, 0));
        this.panel.setBackground(this.panel.getBackgroundColor());

        if (this.inset > 0) {

            if (this.border > 0) {
                this.panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(this.inset, this.inset, this.inset, this.inset),
                        BorderFactory.createLineBorder(this.color, this.border)));
            } else {
                this.panel.setBorder(BorderFactory.createEmptyBorder(this.inset, this.inset, this.inset, this.inset));
            }
        } else if (this.border > 0) {
            this.panel.setBorder(BorderFactory.createLineBorder(this.color, this.border));
        }

        // Set up the interior panel with padding space
        final JPanel thePanel = new JPanel(new BorderLayout(this.padding, 0));
        thePanel.setBackground(this.panel.getBackgroundColor());

        if (this.padding > 0) {
            thePanel.setBorder(BorderFactory.createEmptyBorder(this.padding, this.padding, this.padding, this.padding));
        }

        // Build the buttons.
        final JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inner.setBackground(this.panel.getBackgroundColor());

        final int len = this.labels.length;
        this.panel.setButtons(new JButton[len]);

        int inx = 0;

        while (inx < len) {
            final JButton button = new JButton(this.labels[inx]);
            button.setFont(this.panel.getButtonFont());
            button.setActionCommand(this.commands[inx]);
            button.setDefaultCapable(false);
            button.setFocusPainted(false);
            inner.add(button);
            this.panel.getButtons()[inx] = button;

            ++inx;
        }

        thePanel.add(inner, BorderLayout.CENTER);
        this.panel.add(thePanel, BorderLayout.CENTER);

        BufferedImage zoomIn = null;
        BufferedImage zoomOut = null;

        try (final InputStream in1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("edu/colostate/math/app/exam/zoom-in-3.png")) {
            if (in1 != null) {
                zoomIn = ImageIO.read(in1);
            }
        } catch (final IOException ex) {
            zoomIn = null;
        }

        try (final InputStream in1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("edu/colostate/math/app/exam/zoom-out-3.png")) {
            if (in1 != null) {
                zoomOut = ImageIO.read(in1);
            }
        } catch (final IOException ex) {
            zoomOut = null;
        }

        // Make a panel with "larger/smaller" buttons
        final JPanel east = new JPanel(new BorderLayout());
        final JPanel controls = new JPanel(new FlowLayout());
        east.setBackground(this.panel.getBackgroundColor());
        controls.setBackground(this.panel.getBackgroundColor());
        if (zoomIn == null) {
            this.panel.setLarger(new JButton("+"));
        } else {
            this.panel.setLarger(new JButton(new ImageIcon(zoomIn)));
        }
        this.panel.getLarger().setActionCommand("Larger");

        if (zoomOut == null) {
            this.panel.setSmaller(new JButton(CoreConstants.DASH));
        } else {
            this.panel.setSmaller(new JButton(new ImageIcon(zoomOut)));
        }
        this.panel.getSmaller().setActionCommand("Smaller");

        this.panel.getPalette()[0] = new JToggleButton("A");
        this.panel.getPalette()[0].setFont(new Font("Serif", Font.BOLD, 20));
        this.panel.getPalette()[0].setMargin(new Insets(0, 0, 0, 0));
        this.panel.getPalette()[0].setForeground(Color.DARK_GRAY);
        this.panel.getPalette()[0].setBackground(ColorNames.getColor("white"));
        this.panel.getPalette()[0].setActionCommand("color-white");

        this.panel.getPalette()[1] = new JToggleButton("A");
        this.panel.getPalette()[1].setFont(new Font("Serif", Font.BOLD, 20));
        this.panel.getPalette()[1].setMargin(new Insets(0, 0, 0, 0));
        this.panel.getPalette()[1].setForeground(Color.DARK_GRAY);
        this.panel.getPalette()[1].setBackground(ColorNames.getColor("gold"));
        this.panel.getPalette()[1].setActionCommand("color-gold");

        this.panel.getPalette()[2] = new JToggleButton("A");
        this.panel.getPalette()[2].setFont(new Font("Serif", Font.BOLD, 20));
        this.panel.getPalette()[2].setMargin(new Insets(0, 0, 0, 0));
        this.panel.getPalette()[2].setForeground(Color.DARK_GRAY);
        this.panel.getPalette()[2].setBackground(ColorNames.getColor("MediumPurple"));
        this.panel.getPalette()[2].setActionCommand("color-purple");

        this.panel.getPalette()[3] = new JToggleButton("A");
        this.panel.getPalette()[3].setFont(new Font("Serif", Font.BOLD, 20));
        this.panel.getPalette()[3].setMargin(new Insets(0, 0, 0, 0));
        this.panel.getPalette()[3].setForeground(Color.DARK_GRAY);
        this.panel.getPalette()[3].setBackground(ColorNames.getColor("MediumTurquoise"));
        this.panel.getPalette()[3].setActionCommand("color-blue");

        final ButtonGroup group = new ButtonGroup();
        group.add(this.panel.getPalette()[0]);
        group.add(this.panel.getPalette()[1]);
        group.add(this.panel.getPalette()[2]);
        group.add(this.panel.getPalette()[3]);

        this.panel.getPalette()[0].setSelected(true);
        group.setSelected(this.panel.getPalette()[0].getModel(), true);

        // FIXME: Uncomment to add palette buttons
        // controls.add(this.panel.getPalette()[0]);
        // controls.add(this.panel.getPalette()[1]);
        // controls.add(this.panel.getPalette()[2]);
        // controls.add(this.panel.getPalette()[3]);
        // controls.add(new JLabel(CoreConstants.SPC));

        controls.add(this.panel.getLarger());
        controls.add(this.panel.getSmaller());
        east.add(controls, BorderLayout.PAGE_END);
        this.panel.add(east, BorderLayout.LINE_END);
    }
}

/**
 * Updates the button label of the panel from within the AWT thread.
 */
final class BottomBarPanelButtonUpdater implements Runnable {

    /** The panel to be updated. */
    private final BottomBarPanel panel;

    /** The command whose button should have its label changed. */
    private final String command;

    /** The button label string. */
    private final String buttonString;

    /**
     * Constructs a new {@code BottomBarPanelButtonUpdater}.
     *
     * @param thePanel        the panel to be configured
     * @param theCommand      the command whose button should have its label changed
     * @param theButtonString the button label string
     */
    BottomBarPanelButtonUpdater(final BottomBarPanel thePanel, final String theCommand, final String theButtonString) {

        this.panel = thePanel;
        this.command = theCommand;
        this.buttonString = theButtonString;
    }

    /**
     * Updates in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final JButton[] buttons = this.panel.getButtons();
        if (this.buttonString != null && this.command != null && buttons != null) {
            for (final JButton button : buttons) {
                if (this.command.equals(button.getActionCommand())) {
                    button.setText(this.buttonString);
                }
            }
        }
    }
}
