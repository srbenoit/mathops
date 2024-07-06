package dev.mathops.app.simplewizard;

import javax.swing.Icon;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * The model for the {@code Wizard} component, which tracks the text, icons, and enabled state of each of the buttons,
 * as well as the current panel that is displayed. Note that the model, in its current form, is not intended to be
 * sub-classed.
 */
public class WizardModel {

    /** Identification string for the current panel. */
    static final String CURRENT_PANEL_DESCRIPTOR_PROPERTY = //
            "currentPanelDescriptorProperty";

    /** Property identification String for the Back button's text. */
    static final String BACK_BUTTON_TEXT_PROPERTY = //
            "backButtonTextProperty";

    /** Property identification String for the Back button's icon. */
    static final String BACK_BUTTON_ICON_PROPERTY = //
            "backButtonIconProperty";

    /** Property identification String for the Back button's enabled state. */
    static final String BACK_BUTTON_ENABLED_PROPERTY = //
            "backButtonEnabledProperty";

    /** Property identification String for the Next button's text. */
    static final String NEXT_FINISH_BUTTON_TEXT_PROPERTY = //
            "nextButtonTextProperty";

    /** Property identification String for the Next button's icon. */
    static final String NEXT_FINISH_BUTTON_ICON_PROPERTY = //
            "nextButtonIconProperty";

    /** Property identification String for the Next button's enabled state. */
    static final String NEXT_FINISH_BUTTON_ENABLED_PROPERTY = //
            "nextButtonEnabledProperty";

    /** Property identification String for the Cancel button's text. */
    static final String CANCEL_BUTTON_TEXT_PROPERTY = //
            "cancelButtonTextProperty";

    /** Property identification String for the Cancel button's icon. */
    static final String CANCEL_BUTTON_ICON_PROPERTY = //
            "cancelButtonIconProperty";

    /** Property identification String for the Cancel button's enabled state. */
    static final String CANCEL_BUTTON_ENABLED_PROPERTY = //
            "cancelButtonEnabledProperty";

    /** The current panel. */
    private WizardPanelDescriptor currentPanel;

    /** A map from ID to panel descriptor. */
    private final Map<String, WizardPanelDescriptor> panelHashmap;

    /** A map to button text. */
    private final Map<String, String> buttonTextHashmap;

    /** A map to icon. */
    private final Map<String, Icon> buttonIconHashmap;

    /** A map to button enabled state. */
    private final Map<String, Boolean> buttonEnabledHashmap;

    /** A property change support object. */
    private final PropertyChangeSupport propertyChangeSupport;

    /**
     * Default constructor.
     */
    WizardModel() {

        this.panelHashmap = new HashMap<>(10);

        this.buttonTextHashmap = new HashMap<>(10);
        this.buttonIconHashmap = new HashMap<>(10);
        this.buttonEnabledHashmap = new HashMap<>(10);

        this.propertyChangeSupport = new PropertyChangeSupport(this);

    }

    /**
     * Gets the currently displayed {@code WizardPanelDescriptor}.
     *
     * @return the currently displayed {@code WizardPanelDescriptor}
     */
    WizardPanelDescriptor getCurrentPanelDescriptor() {

        return this.currentPanel;
    }

    /**
     * Registers the {@code WizardPanelDescriptor} in the model using the Object-identifier specified.
     *
     * @param id         object identifier
     * @param descriptor {@code WizardPanelDescriptor} that describes the panel
     */
    void registerPanel(final String id, final WizardPanelDescriptor descriptor) {

        // Place a reference to it in a hashtable so we can access it later
        // when it is about to be displayed.
        this.panelHashmap.put(id, descriptor);
    }

    /**
     * Sets the current panel to that identified by the {@code String} passed in.
     *
     * @param id object panel identifier
     */
    final void setCurrentPanel(final String id) {

        final WizardPanelDescriptor nextPanel;
        final WizardPanelDescriptor oldPanel;

        // First, get the hashtable reference to the panel that should be displayed.
        nextPanel = this.panelHashmap.get(id);

        // If we couldn't find the panel that should be displayed, return false.
        if (nextPanel == null) {
            throw new WizardPanelNotFoundException();
        }

        oldPanel = this.currentPanel;
        this.currentPanel = nextPanel;

        if (oldPanel != this.currentPanel) {
            firePropertyChange(CURRENT_PANEL_DESCRIPTOR_PROPERTY, oldPanel, this.currentPanel);
        }
    }

    /**
     * Get the text of the Back button.
     *
     * @return the back button text
     */
    private String getBackButtonText() {

        return this.buttonTextHashmap.get(BACK_BUTTON_TEXT_PROPERTY);
    }

    /**
     * Set the text of the Back button.
     *
     * @param newText the new back button text
     */
    void setBackButtonText(final String newText) {

        final String oldText = getBackButtonText();

        if (!newText.equals(oldText)) {
            this.buttonTextHashmap.put(BACK_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(BACK_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    }

    /**
     * Get the text of the Next/Finish button.
     *
     * @return the next/finish button text
     */
    private String getNextFinishButtonText() {

        return this.buttonTextHashmap.get(NEXT_FINISH_BUTTON_TEXT_PROPERTY);
    }

    /**
     * Set the text of the Next/Finish button.
     *
     * @param newText the new next/finish button text
     */
    void setNextFinishButtonText(final String newText) {

        final String oldText = getNextFinishButtonText();

        if (!newText.equals(oldText)) {
            this.buttonTextHashmap.put(NEXT_FINISH_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(NEXT_FINISH_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    }

    /**
     * Get the text of the Cancel button.
     *
     * @return the cancel button text
     */
    private String getCancelButtonText() {

        return this.buttonTextHashmap.get(CANCEL_BUTTON_TEXT_PROPERTY);
    }

    /**
     * Set the text of the Cancel button.
     *
     * @param newText the new cancel button text
     */
    void setCancelButtonText(final String newText) {

        final String oldText = getCancelButtonText();

        if (!newText.equals(oldText)) {
            this.buttonTextHashmap.put(CANCEL_BUTTON_TEXT_PROPERTY, newText);
            firePropertyChange(CANCEL_BUTTON_TEXT_PROPERTY, oldText, newText);
        }
    }

    /**
     * Get the icon of the Back button.
     *
     * @return the back button icon
     */
    private Icon getBackButtonIcon() {

        return this.buttonIconHashmap.get(BACK_BUTTON_ICON_PROPERTY);
    }

    /**
     * Set the icon of the Back button.
     *
     * @param newIcon the new back button icon
     */
    void setBackButtonIcon(final Icon newIcon) {

        final Icon oldIcon = getBackButtonIcon();

        if (newIcon != null) {

            if (!newIcon.equals(oldIcon)) {
                this.buttonIconHashmap.put(BACK_BUTTON_ICON_PROPERTY, newIcon);
                firePropertyChange(BACK_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
            }
        }
    }

    /**
     * Get the icon of the Next/Finish button.
     *
     * @return the next/finish button icon
     */
    private Icon getNextFinishButtonIcon() {

        return this.buttonIconHashmap.get(NEXT_FINISH_BUTTON_ICON_PROPERTY);
    }

    /**
     * Set the icon of the Next/Finish button.
     *
     * @param newIcon the new next/finish button icon
     */
    public void setNextFinishButtonIcon(final Icon newIcon) {

        final Icon oldIcon = getNextFinishButtonIcon();

        if (newIcon != null) {

            if (!newIcon.equals(oldIcon)) {
                this.buttonIconHashmap.put(NEXT_FINISH_BUTTON_ICON_PROPERTY, newIcon);
                firePropertyChange(NEXT_FINISH_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
            }
        }
    }

    /**
     * Get the icon of the Cancel button.
     *
     * @return the cancel button icon
     */
    private Icon getCancelButtonIcon() {

        return this.buttonIconHashmap.get(CANCEL_BUTTON_ICON_PROPERTY);
    }

    /**
     * Set the icon of the Cancel button.
     *
     * @param newIcon the new cancel button icon
     */
    void setCancelButtonIcon(final Icon newIcon) {

        final Icon oldIcon = getCancelButtonIcon();

        if (newIcon != null) {

            if (!newIcon.equals(oldIcon)) {
                this.buttonIconHashmap.put(CANCEL_BUTTON_ICON_PROPERTY, newIcon);
                firePropertyChange(CANCEL_BUTTON_ICON_PROPERTY, oldIcon, newIcon);
            }
        }
    }

    /**
     * Get the enabled state of the Back button.
     *
     * @return the enabled state of the back button
     */
    Boolean getBackButtonEnabled() {

        return this.buttonEnabledHashmap.get(BACK_BUTTON_ENABLED_PROPERTY);
    }

    /**
     * Set the enabled state of the Back button.
     *
     * @param newValue the new enabled state of the back button
     */
    void setBackButtonEnabled(final Boolean newValue) {

        final Boolean oldValue = getBackButtonEnabled();

        if (!newValue.equals(oldValue)) {
            this.buttonEnabledHashmap.put(BACK_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(BACK_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }

    /**
     * Get the enabled state of the Next/Finish button.
     *
     * @return the enabled state of the next/finish button
     */
    Boolean getNextFinishButtonEnabled() {

        return this.buttonEnabledHashmap.get(NEXT_FINISH_BUTTON_ENABLED_PROPERTY);
    }

    /**
     * Set the enabled state of the Next/Finish button.
     *
     * @param newValue the new enabled state of the next/finish button
     */
    void setNextFinishButtonEnabled(final Boolean newValue) {

        final Boolean oldValue = getNextFinishButtonEnabled();

        if (!newValue.equals(oldValue)) {
            this.buttonEnabledHashmap.put(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(NEXT_FINISH_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }

    /**
     * Get the enabled state of the Cancel button.
     *
     * @return the enabled state of the cancel button
     */
    Boolean getCancelButtonEnabled() {

        return this.buttonEnabledHashmap.get(CANCEL_BUTTON_ENABLED_PROPERTY);
    }

    /**
     * Set the enabled state of the Cancel button.
     *
     * @param newValue the new enabled state of the cancel button
     */
    void setCancelButtonEnabled(final Boolean newValue) {

        final Boolean oldValue = getCancelButtonEnabled();

        if (!newValue.equals(oldValue)) {
            this.buttonEnabledHashmap.put(CANCEL_BUTTON_ENABLED_PROPERTY, newValue);
            firePropertyChange(CANCEL_BUTTON_ENABLED_PROPERTY, oldValue, newValue);
        }
    }

    /**
     * Register a property change listener.
     *
     * @param listener the listener to register
     */
    final void addPropertyChangeListener(final PropertyChangeListener listener) {

        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Fire a property change event to all registered listeners.
     *
     * @param propertyName the name of the property that changed
     * @param oldValue     the old value
     * @param newValue     the new value
     */
    private void firePropertyChange(final String propertyName, final Object oldValue,
                                    final Object newValue) {

        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
