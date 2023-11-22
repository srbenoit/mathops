package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

/**
 * A descriptor for the second panel in the test wizard.
 */
class ResultsPanel5Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "NOT_CLEARED_PANEL";

    /** The panel. */
    private final ResultsPanel5 panel5;

    /**
     * Construct a new {@code ResultsPanel5Descriptor}.
     */
    ResultsPanel5Descriptor() {
        super();

        this.panel5 = new ResultsPanel5();

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

        return FINISH;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return ResultsPanel1Descriptor.IDENTIFIER;
    }
}
