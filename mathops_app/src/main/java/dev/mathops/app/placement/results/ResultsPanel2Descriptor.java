package dev.mathops.app.placement.results;

import dev.mathops.app.simplewizard.WizardPanelDescriptor;

import java.util.SortedSet;

/**
 * A descriptor for the second panel in the test wizard.
 */
class ResultsPanel2Descriptor extends WizardPanelDescriptor {

    /** The identifier for this panel. */
    static final String IDENTIFIER = "PRE_EXAM_PREP_PANEL";

    /** The panel. */
    private final ResultsPanel2 panel2;

    /** The list of courses the student placed out of. */
    private final SortedSet<String> placed;

    /** The list of courses the student is cleared to take. */
    private final SortedSet<String> cleared;

    /**
     * Construct a new {@code ResultsPanel2Descriptor}.
     *
     * @param theCredit  the list of courses for which credit was earned
     * @param thePlaced  the list of courses the student placed out of
     * @param theCleared the list of courses the student is cleared to take
     */
    ResultsPanel2Descriptor(final SortedSet<String> theCredit, final SortedSet<String> thePlaced,
                            final SortedSet<String> theCleared) {
        super();

        this.placed = thePlaced;
        this.cleared = theCleared;

        this.panel2 = new ResultsPanel2(theCredit);

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

        final String result;

        if (!this.placed.isEmpty()) {
            result = ResultsPanel3Descriptor.IDENTIFIER;
        } else if (!this.cleared.isEmpty()) {
            result = ResultsPanel4Descriptor.IDENTIFIER;
        } else {
            result = FINISH;
        }

        return result;
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
