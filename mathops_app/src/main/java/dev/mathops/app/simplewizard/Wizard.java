package dev.mathops.app.simplewizard;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * This class implements a basic wizard dialog, where the programmer can insert one or more Components to act as panels.
 * These panels can be navigated through arbitrarily using the 'Next' or 'Back' buttons, or the dialog itself can be
 * closed using the 'Cancel' button. Note that even though the dialog uses a CardLayout manager, the order of the panels
 * is not linear. Each panel determines at runtime what its next and previous panel will be.
 */
public class Wizard extends WindowAdapter implements PropertyChangeListener {

    /** Indicates that the 'Finish' button was pressed to close the dialog. */
    public static final int FINISH_RETURN_CODE = 0;

    /**
     * Indicates that the 'Cancel' button was pressed to close the dialog, or the user pressed the close box in the
     * corner of the window.
     */
    static final int CANCEL_RETURN_CODE = 1;

    /** The action command for the 'Next' button. */
    static final String NEXT_BUTTON_ACTION_COMMAND = "NextCmd";

    /** The action command for the 'Back' button. */
    static final String BACK_BUTTON_ACTION_COMMAND = "BackCmd";

    /** The action command for the 'Cancel' button. */
    static final String CANCEL_BUTTON_ACTION_COMMAND = "CancelCmd";

    /** Indicates that the dialog closed due to an internal error. */
    private static final int ERROR_RETURN_CODE = 2;

    /** The model that controls the wizard data. */
    private final WizardModel wizardModel;

    /** The Controller to manage the wizard GUI. */
    private WizardController wizardController;

    /** The wizard dialog. */
    private final JDialog wizardDialog;

    /** The panel that contains the wizard panels. */
    private JPanel cardPanel;

    /** The card panel layout to flip between wizard panels. */
    private CardLayout cardLayout;

    /** The back button. */
    private JButton backButton;

    /** The next/finish button. */
    private JButton nextButton;

    /** The cancel button. */
    private JButton cancelButton;

    /** The return code, indicating how the wizard was closed. */
    private int returnCode;

    /**
     * Default constructor. This method creates a new {@code WizardModel} object and passes it into the overloaded
     * constructor.
     *
     * @param noCancel {@code true} to disable the cancel button
     */
    public Wizard(final boolean noCancel) {

        this(null, noCancel);
    }

    /**
     * This method accepts a {@code Frame} object as the {@code JDialog}'s parent.
     *
     * @param owner    the {@code Frame} object that is the owner of the {@code JDialog}
     * @param noCancel {@code true} to disable the cancel button
     */
    public Wizard(final Frame owner, final boolean noCancel) {
        super();

        this.wizardModel = new WizardModel();
        this.wizardDialog = new JDialog(owner);
        this.wizardDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        this.wizardDialog.setAlwaysOnTop(true);
        initComponents(noCancel);
    }

    /**
     * Returns an instance of the {@code JDialog} that this class created. This is useful in the event that you want to
     * change any of the {@code JDialog} parameters manually.
     *
     * @return the {@code JDialog} instance that this class created
     */
    public final JDialog getDialog() {

        return this.wizardDialog;
    }

    /**
     * Convenience method that displays a modal wizard dialog and blocks until the dialog has completed.
     *
     * @return indicates how the dialog was closed (compare this value against the RETURN_CODE constants at the
     *         beginning of the class)
     */
    public final int showModalDialog() {

        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension size;
        final Dimension screen;

        this.wizardDialog.setModal(true);
        this.wizardDialog.pack();

        // Center the dialog on the screen
        size = this.wizardDialog.getSize();
        screen = tk.getScreenSize();
        this.wizardDialog.setLocation((screen.width - size.width) / 2,
                (screen.height - size.height) / 2);

        this.wizardDialog.setVisible(true);

        return this.returnCode;
    }

    /**
     * Returns the current model of the wizard dialog.
     *
     * @return a {@code WizardModel} instance, which serves as the model for the wizard dialog
     */
    public final WizardModel getModel() {

        return this.wizardModel;
    }

    /**
     * Add a {@code Component} as a panel for the wizard dialog by registering its {@code WizardPanelDescriptor} object.
     * Each panel is identified by a unique {@code Object}-based identifier (often a {@code String}), which can be used
     * by the {@code setCurrentPanel} method to display the panel at runtime.
     *
     * @param id    a {@code String} identifier used to identify the {@code WizardPanelDescriptor} object
     * @param panel the {@code WizardPanelDescriptor} object which contains helpful information about the panel
     */
    public final void registerWizardPanel(final String id, final WizardPanelDescriptor panel) {

        // add the incoming panel to our JPanel display that is managed by the CardLayout layout
        // manager
        this.cardPanel.add(panel.getPanelComponent(), id);

        // set a callback to the current wizard
        panel.setWizard(this);

        // place a reference to it in the model
        this.wizardModel.registerPanel(id, panel);
    }

    /**
     * Displays the panel identified by the object passed in. This is the same {@code Object} identifier used when
     * registering the panel.
     *
     * @param id the {@code Object} identifier of the panel to be displayed
     */
    public final void setCurrentPanel(final String id) {

        final WizardPanelDescriptor oldPanelDescriptor;

        // Get the hashtable reference to the panel that should be displayed. If the identifier
        // passed in is null, then close the dialog.
        if (id == null) {
            close(ERROR_RETURN_CODE);
            return;
        }

        oldPanelDescriptor = this.wizardModel.getCurrentPanelDescriptor();

        if (oldPanelDescriptor != null) {
            oldPanelDescriptor.aboutToHidePanel();
        }

        this.wizardModel.setCurrentPanel(id);
        this.wizardModel.getCurrentPanelDescriptor().aboutToDisplayPanel();

        // Show the panel in the dialog.
        this.cardLayout.show(this.cardPanel, id);
        this.wizardModel.getCurrentPanelDescriptor().displayingPanel();
    }

    /**
     * Method used to listen for property change events from the model and update the dialog's graphical components as
     * necessary.
     *
     * @param evt {@code PropertyChangeEvent} passed from the model to signal that one of its properties has changed
     *            value
     */
    @Override
    public final void propertyChange(final PropertyChangeEvent evt) {

        final String prop = evt.getPropertyName();
        final Object value = evt.getNewValue();
        final String valueStr = value.toString();

        switch (prop) {
            case WizardModel.CURRENT_PANEL_DESCRIPTOR_PROPERTY -> this.wizardController.resetButtonsToPanelRules();
            case WizardModel.NEXT_FINISH_BUTTON_TEXT_PROPERTY -> this.nextButton.setText(valueStr);
            case WizardModel.BACK_BUTTON_TEXT_PROPERTY -> this.backButton.setText(valueStr);
            case WizardModel.CANCEL_BUTTON_TEXT_PROPERTY -> {
                if (this.cancelButton != null) {
                    this.cancelButton.setText(valueStr);
                }
            }
            case WizardModel.NEXT_FINISH_BUTTON_ENABLED_PROPERTY ->
                    this.nextButton.setEnabled(((Boolean) value).booleanValue());
            case WizardModel.BACK_BUTTON_ENABLED_PROPERTY -> {
                this.backButton.setEnabled(((Boolean) value).booleanValue());
                this.backButton.setVisible(((Boolean) value).booleanValue());
            }
            case WizardModel.CANCEL_BUTTON_ENABLED_PROPERTY -> {
                if (this.cancelButton != null) {
                    this.cancelButton.setEnabled(((Boolean) value).booleanValue());
                }
            }
            case WizardModel.NEXT_FINISH_BUTTON_ICON_PROPERTY -> this.nextButton.setIcon((Icon) value);
            case WizardModel.BACK_BUTTON_ICON_PROPERTY -> this.backButton.setIcon((Icon) value);
            case WizardModel.CANCEL_BUTTON_ICON_PROPERTY -> {
                if (this.cancelButton != null) {
                    this.cancelButton.setIcon((Icon) value);
                }
            }
        }
    }

    /**
     * Mirrors the {@code WizardModel} method of the same name.
     *
     * @param newValue the new enabled status of the button
     */
    public final void setNextFinishButtonEnabled(final boolean newValue) {

        this.wizardModel.setNextFinishButtonEnabled(Boolean.valueOf(newValue));
    }

    /**
     * Closes the dialog and sets the return code to the integer parameter.
     *
     * @param code the return code
     */
    final void close(final int code) {

        this.returnCode = code;
        this.wizardDialog.dispose();
    }

    /**
     * This method initializes the components for the wizard dialog: it creates a {@code JDialog} as a
     * {@code CardLayout} panel surrounded by a small amount of space on each side, as well as three buttons at the
     * bottom.
     *
     * @param noCancel {@code true} to disable the cancel button
     */
    private void initComponents(final boolean noCancel) {

        final JPanel buttonPanel;
        final JSeparator separator;
        final Box buttonBox;

        this.wizardModel.addPropertyChangeListener(this);
        this.wizardController = new WizardController(this);

        this.wizardDialog.getContentPane().setLayout(new BorderLayout());
        this.wizardDialog.addWindowListener(this);

        // Create the outer wizard panel, which is responsible for three buttons:
        // Next, Back, and Cancel. It is also responsible a JPanel above them that
        // uses a CardLayout layout manager to display multiple panels in the same spot.

        buttonPanel = new JPanel();
        separator = new JSeparator();
        buttonBox = new Box(BoxLayout.X_AXIS);

        this.cardPanel = new JPanel();
        this.cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));

        this.cardLayout = new CardLayout();
        this.cardPanel.setLayout(this.cardLayout);

        this.backButton = new JButton();
        this.backButton.setActionCommand(BACK_BUTTON_ACTION_COMMAND);
        this.backButton.addActionListener(this.wizardController);

        this.nextButton = new JButton();
        this.nextButton.setActionCommand(NEXT_BUTTON_ACTION_COMMAND);
        this.nextButton.addActionListener(this.wizardController);

        if (!noCancel) {
            this.cancelButton = new JButton();
            this.cancelButton.setActionCommand(CANCEL_BUTTON_ACTION_COMMAND);
            this.cancelButton.addActionListener(this.wizardController);
        }

        // Create the buttons with a separator above them, then place them
        // on the east side of the panel with a small amount of space between
        // the back and the next button, and a larger amount of space between
        // the next button and the cancel button.
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(separator, BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10)));
        buttonBox.add(this.backButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(this.nextButton);

        if (!noCancel) {
            buttonBox.add(Box.createHorizontalStrut(30));
            buttonBox.add(this.cancelButton);
        }

        buttonPanel.add(buttonBox, BorderLayout.EAST);

        this.wizardDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        this.wizardDialog.getContentPane().add(this.cardPanel, BorderLayout.CENTER);
    }

    /**
     * If the user presses the close box on the dialog's window, treat it as a cancel.
     *
     * @param evt the event passed in from AWT
     */
    @Override
    public final void windowClosing(final WindowEvent evt) {

        this.returnCode = CANCEL_RETURN_CODE;
    }

    /**
     * Gets an image resource and returns it as an {@code ImageIcon}.
     *
     * @param name the name of the image to get
     * @return the {@code ImageIcon}
     */
    static ImageIcon getImage(final String name) {

        final URL url;
        ImageIcon icon = null;

        url = Wizard.class.getClassLoader().getResource(
                "dev.mathops.app.simplewizard." + name);

        if (url != null) {
            icon = new ImageIcon(url);
        }

        return icon;
    }
}
