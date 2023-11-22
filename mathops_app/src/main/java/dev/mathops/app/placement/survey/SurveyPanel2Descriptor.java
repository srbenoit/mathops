package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A descriptor for the second panel in the test wizard.
 */
class SurveyPanel2Descriptor extends WizardPanelDescriptor implements ActionListener {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "PRE_EXAM_PREP_PANEL";

    /** The panel. */
    private final SurveyPanel2 panel2;

    /**
     * Construct a new {@code SurveyPanel2Descriptor}.
     */
    SurveyPanel2Descriptor() {
        super();

        this.panel2 = new SurveyPanel2();
        this.panel2.addActionListener(this);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(this.panel2);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return SurveyPanel3Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return SurveyPanel1Descriptor.IDENTIFIER;
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
        this.panel2.setCheckboxEnabledState();
    }

    /**
     * Set the state of the "Next" button based on the radio boxes.
     */
    private void setNextButtonAccordingToRadioBoxes() {

        getWizard().setNextFinishButtonEnabled(this.panel2.getRadioButtonSelected() != null);
    }
}
