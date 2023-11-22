package dev.mathops.app.placement.survey;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

/**
 * A descriptor for the first panel in the test wizard.
 */
class SurveyPanel1Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "INTRODUCTION_PANEL";

    /**
     * Construct a new {@code SurveyPanel1Descriptor}.
     *
     * @param examTitle the exam title
     */
    SurveyPanel1Descriptor(final String examTitle) {

        super(IDENTIFIER, new SurveyPanel1(examTitle));
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return SurveyPanel2Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return null;
    }
}
