package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.util.SortedSet;

/**
 * A descriptor for the second panel in the test wizard.
 */
class ResultsPanel4Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "CLEARED_FOR_PANEL";

    /** The panel. */
    private final ResultsPanel4 panel4;

    /** The list of courses for which credit was earned. */
    private final SortedSet<String> credit;

    /** The list of courses the student placed out of. */
    private final SortedSet<String> placed;

    /**
     * Construct a new {@code ResultsPanel4Descriptor}.
     *
     * @param theCredit  the list of courses for which credit was earned
     * @param thePlaced  the list of courses the student placed out of
     * @param theCleared the list of courses the student is cleared to take
     */
    ResultsPanel4Descriptor(final SortedSet<String> theCredit, final SortedSet<String> thePlaced,
                            final SortedSet<String> theCleared) {
        super();

        this.credit = theCredit;
        this.placed = thePlaced;

        this.panel4 = new ResultsPanel4(theCleared);

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

        return FINISH;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        final String result;

        if (!this.placed.isEmpty()) {
            result = ResultsPanel3Descriptor.IDENTIFIER;
        } else if (!this.credit.isEmpty()) {
            result = ResultsPanel2Descriptor.IDENTIFIER;
        } else {
            result = ResultsPanel1Descriptor.IDENTIFIER;
        }

        return result;
    }
}
