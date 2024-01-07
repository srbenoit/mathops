package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A descriptor for the sixth panel in the test wizard.
 */
class SurveyPanel6Descriptor extends WizardPanelDescriptor implements ActionListener {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "COLLEGE_COURSES_PANEL";

    /**
     * Construct a new {@code SurveyPanel6Descriptor}.
     */
    SurveyPanel6Descriptor() {
        super();

        final SurveyPanel6 panel6 = new SurveyPanel6();
        panel6.addActionListener(this);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel6);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return FINISH;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return SurveyPanel5Descriptor.IDENTIFIER;
    }

    /**
     * Called before the panel is displayed.
     */
    @Override
    public void aboutToDisplayPanel() {

        getWizard().setNextFinishButtonEnabled(true);
    }

    /**
     * Handle actions on the panel.
     *
     * @param e the {@code ActionEvent} that occurred
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        // No action
    }
}
