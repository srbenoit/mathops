package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A descriptor for the third panel in the test wizard.
 */
class SurveyPanel3Descriptor extends WizardPanelDescriptor implements ActionListener {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "CLASS_HISTORY_PANEL";

    /** The panel. */
    private final SurveyPanel3 panel3;

    /**
     * Construct a new {@code SurveyPanel3Descriptor}.
     */
    SurveyPanel3Descriptor() {
        super();

        this.panel3 = new SurveyPanel3();
        this.panel3.addActionListener(this);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(this.panel3);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return SurveyPanel4Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return SurveyPanel2Descriptor.IDENTIFIER;
    }

    /**
     * Called before the panel is displayed.
     */
    @Override
    public void aboutToDisplayPanel() {

        setNextButtonAccordingToRadioBoxes();
    }

    /**
     * Handle actions on the panel.
     *
     * @param e the {@code ActionEvent} that occurred
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        setNextButtonAccordingToRadioBoxes();
    }

    /**
     * Set the state of the "Next" button based on the radio boxes.
     */
    private void setNextButtonAccordingToRadioBoxes() {

        final boolean ok = (this.panel3.getQ1RadioButtonSelected() != null)
                && (this.panel3.getQ2RadioButtonSelected() != null);

        getWizard().setNextFinishButtonEnabled(ok);
    }
}
