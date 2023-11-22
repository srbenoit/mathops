package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A descriptor for the fifth panel in the test wizard.
 */
class SurveyPanel5Descriptor extends WizardPanelDescriptor implements ActionListener {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "HS_COURSES_PANEL_2";

    /** The panel. */
    private final SurveyPanel5 panel5;

    /**
     * Construct a new {@code SurveyPanel5Descriptor}.
     */
    SurveyPanel5Descriptor() {
        super();

        this.panel5 = new SurveyPanel5();
        this.panel5.addActionListener(this);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(this.panel5);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return SurveyPanel6Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return SurveyPanel4Descriptor.IDENTIFIER;
    }

    /**
     * Called before the panel is displayed.
     */
    @Override
    public void aboutToDisplayPanel() {

        getWizard().setNextFinishButtonEnabled(true);
        this.panel5.updateCheckboxes();
    }

    /**
     * Handle actions on the panel.
     *
     * @param e the {@code ActionEvent} that occurred
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        this.panel5.updateCheckboxes();
    }
}
