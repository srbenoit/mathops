package dev.mathops.app.simplewizard;

import javax.swing.JPanel;
import java.awt.Component;

/**
 * A base descriptor class used to reference a {@code Component} panel for the {@code Wizard}, as well as provide
 * general rules as to how the panel should behave.
 */
public class WizardPanelDescriptor {

    /**
     * Identifier returned by {@code getNextPanelDescriptor} to indicate that this is the last panel and the text of the
     * 'Next' button should change to 'Finish'.
     */
    public static final String FINISH = "FINISH";

    /** A default identifier for panels. */
    private static final String DEFAULT_PANEL_IDENTIFIER = //
            "defaultPanelIdentifier";

    /** The wizard. */
    private Wizard wizard;

    /** The target panel. */
    private Component targetPanel;

    /** The panel identifier. */
    private String panelIdentifier;

    /**
     * Default constructor. The id and the Component panel must be set separately.
     */
    public WizardPanelDescriptor() {

        this.panelIdentifier = DEFAULT_PANEL_IDENTIFIER;
        this.targetPanel = new JPanel();
    }

    /**
     * Constructor which accepts both the {@code Object} identifier and a reference to the {@code Component} class which
     * makes up the panel.
     *
     * @param id    object-based identifier
     * @param panel a class which extends {@code Component} that will be inserted as a panel into the wizard dialog
     */
    public WizardPanelDescriptor(final String id, final Component panel) {

        this.panelIdentifier = id;
        this.targetPanel = panel;
    }

    /**
     * Returns to {@code Component} that serves as the actual panel.
     *
     * @return a reference to the {@code Component} that serves as the panel
     */
    public final Component getPanelComponent() {

        return this.targetPanel;
    }

    /**
     * Sets the panel's component as a class that extends {@code Component}.
     *
     * @param panel {@code Component} which serves as the wizard panel
     */
    protected final void setPanelComponent(final Component panel) {

        this.targetPanel = panel;
    }

    /**
     * Sets the {@code String} identifier for this panel. The identifier must be unique from all the other identifiers
     * in the panel.
     *
     * @param id {@code String} identifier for this panel
     */
    protected final void setPanelDescriptorIdentifier(final String id) {

        this.panelIdentifier = id;
    }

    /**
     * Set the wizard that the panel belongs to.
     *
     * @param wiz the owning wizard
     */
    final void setWizard(final Wizard wiz) {

        this.wizard = wiz;
    }

    /**
     * Returns a reference to the {@code Wizard} component.
     *
     * @return the {@code Wizard} class hosting this descriptor
     */
    protected final Wizard getWizard() {

        return this.wizard;
    }

    /**
     * Override this class to provide the {@code String} identifier of the panel that the user should traverse to when
     * the Next button is pressed. Note that this method is only called when the button is actually pressed, so that the
     * panel can change the next panel's identifier dynamically at runtime if necessary. Return {@code null} if the
     * button should be disabled. Return {@code FinishIdentfier} if the button text should change to 'Finish' and the
     * dialog should end.
     *
     * @return a {@code String} identifier
     */
    public String getNextPanelDescriptor() {

        return null;
    }

    /**
     * Override this class to provide the {@code String} identifier of the panel that the user should traverse to when
     * the Back button is pressed. Note that this method is only called when the button is actually pressed, so that the
     * panel can change the previous panel's identifier dynamically at runtime if necessary. Return {@code null} if the
     * button should be disabled.
     *
     * @return a {@code String} identifier
     */
    public String getBackPanelDescriptor() {

        return null;
    }

    /**
     * Override this method to provide functionality that will be performed just before the panel is to be displayed.
     */
    public void aboutToDisplayPanel() {

        // No action
    }

    /**
     * Override this method to perform functionality when the panel itself is displayed.
     */
    void displayingPanel() {

        // No action
    }

    /**
     * Override this method to perform functionality just before the panel is to be hidden.
     */
    void aboutToHidePanel() {

        // No action
    }
}
