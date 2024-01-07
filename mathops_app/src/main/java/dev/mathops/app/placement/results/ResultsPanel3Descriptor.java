package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.util.SortedSet;

/**
 * A descriptor for the second panel in the test wizard.
 */
class ResultsPanel3Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "PLACED_OUT_PANEL";

    /** The list of courses for which credit was earned. */
    private final SortedSet<String> credit;

    /** The list of courses the student is cleared to take. */
    private final SortedSet<String> cleared;

    /**
     * Construct a new {@code ResultsPanel3Descriptor}.
     *
     * @param theCredit  the list of courses for which credit was earned
     * @param thePlaced  the list of courses the student placed out of
     * @param theCleared the list of courses the student is cleared to take
     */
    ResultsPanel3Descriptor(final SortedSet<String> theCredit, final SortedSet<String> thePlaced,
                            final SortedSet<String> theCleared) {
        super();

        this.credit = theCredit;
        this.cleared = theCleared;

        final ResultsPanel3 panel3 = new ResultsPanel3(thePlaced);

        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel3);
    }

    /**
     * Get the descriptor for the next panel.
     *
     * @return the next panel descriptor
     */
    @Override
    public String getNextPanelDescriptor() {

        return (this.cleared.isEmpty()) ? FINISH : ResultsPanel4Descriptor.IDENTIFIER;
    }

    /**
     * Get the descriptor for the prior panel.
     *
     * @return the prior panel descriptor
     */
    @Override
    public String getBackPanelDescriptor() {

        return (this.credit.isEmpty()) ? ResultsPanel1Descriptor.IDENTIFIER : ResultsPanel2Descriptor.IDENTIFIER;
    }
}
