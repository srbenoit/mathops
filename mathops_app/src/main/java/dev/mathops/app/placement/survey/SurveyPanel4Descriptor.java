package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A descriptor for the fourth panel in the test wizard.
 */
class SurveyPanel4Descriptor extends WizardPanelDescriptor implements ActionListener {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "HS_COURSES_PANEL_1";

    /** The panel. */
    private final SurveyPanel4 panel4;

    /**
     * Construct a new {@code SurveyPanel4Descriptor}.
     */
    SurveyPanel4Descriptor() {
        super();

        this.panel4 = new SurveyPanel4();
        this.panel4.addActionListener(this);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(this.panel4);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return SurveyPanel5Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return SurveyPanel3Descriptor.IDENTIFIER;
    }

    /**
     * Called before the panel is displayed.
     */
    @Override
    public void aboutToDisplayPanel() {

        getWizard().setNextFinishButtonEnabled(true);
        this.panel4.updateCheckboxes();
    }

    /**
     * Handle actions on the panel.
     *
     * @param e the {@code ActionEvent} that occurred
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        this.panel4.updateCheckboxes();
    }
}
