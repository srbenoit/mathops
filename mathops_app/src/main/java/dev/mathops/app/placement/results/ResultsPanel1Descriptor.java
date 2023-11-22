package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

/**
 * A descriptor for the first panel in the placement results wizard.
 */
class ResultsPanel1Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "INTRODUCTION_PANEL";

    /**
     * Construct a new {@code ResultsPanel1Descriptor}.
     */
    ResultsPanel1Descriptor() {

        super(IDENTIFIER, new ResultsPanel1());
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

        return null;
    }
}
