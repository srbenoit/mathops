package dev.mathops.app.database.eos;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel that displays headers and lists of steps, where headers can be expanded and collapsed.  Steps can be selected
 * but only one may be selected at a time.
 */
public final class StepList extends JPanel {

    /** The icon image for "expand" buttons. */
    private final Image expandImg;

    /** The icon image for "collapse" buttons. */
    private final Image collapseImg;

    /** Map from section header text to the section panel for that section. */
    private final Map<String, Section> sectionPanels;

    /** The currently selected step. */
    private AbstractStep selected;

    /**
     * Constructs a new {@code StepList}.
     *
     * @param theExpandImg   the icon image for "expand" buttons (null if unable to load)
     * @param theCollapseImg the icon image for "collapse" buttons (null if unable to load)
     */
    StepList(final Image theExpandImg, final Image theCollapseImg) {

        super(new StackedBorderLayout());

        this.sectionPanels = new HashMap<>(10);
        this.expandImg = theExpandImg;
        this.collapseImg = theCollapseImg;
    }

    /**
     * Adds a step.  If the section heading does not yet exist, it is created.
     *
     * @param sectionHeading the section heading under which to add the step
     * @param step           the step
     */
    void addStep(final String sectionHeading, final AbstractStep step) {

        Section section = this.sectionPanels.get(sectionHeading);
        if (section == null) {
            section = new Section(sectionHeading, this.expandImg, this.collapseImg);
            this.sectionPanels.put(sectionHeading, section);
            add(section, StackedBorderLayout.NORTH);
        }

        section.addContents(step);
    }

    /**
     * Called when a step is selected.  This de-selects any step that was previously selected, and selects the new
     * step.
     *
     * @param step the step being selected (null to deselect all steps)
     */
    void select(final AbstractStep step) {

        if (this.selected != null) {
            this.selected.setSelected(false);
        }

        if (step != null) {
            step.setSelected(true);
        }

        this.selected = step;
    }

    /**
     * Collapses a section.
     *
     * @param heading the section heading
     */
    void collapseSection(final String heading) {

        final Section sect = this.sectionPanels.get(heading);
        if (sect != null) {
            sect.collapse();
        }
    }
}
