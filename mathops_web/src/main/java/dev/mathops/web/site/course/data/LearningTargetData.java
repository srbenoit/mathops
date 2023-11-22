package dev.mathops.web.site.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a Learning Target section of a course module.
 */
public final class LearningTargetData {

    /** The owning module. */
    public final ModuleData module;

    /** The unit number. */
    public final int unit;

    /** The objective number. */
    private final int objective;

    /** The learning target number, such as "1.1". */
    public final String targetNumber;

    /** The assignment ID. */
    public final String assignmentId;

    /** The main learning outcome, such as "I am never going to:" */
    public final String mainOutcome;

    /**
     * The list of subordinate outcomes, such as "give you up", "let you go", "run around and desert you", "make you
     * cry", "say goodbye", or "tell a lie and hurt you".
     */
    public final String[] subOutcomes;

    /** The list of example blocks. */
    public final List<ExampleBlock> exampleBlocks;

    /**
     * Constructs a new {@code LearningTargetData}.
     *
     * @param theModule       the owning module
     * @param theUnit         the unit number
     * @param theObjective    the objective number
     * @param theTargetNumber the learning target number, such as "1.1"
     * @param theAssignmentId the assignment ID
     * @param theMainOutcome  the main outcome, typically of the form "I can ..., including:"
     * @param theSuboutcomes  a list of sub-outcomes
     */
    LearningTargetData(final ModuleData theModule, final int theUnit, final int theObjective,
                       final String theTargetNumber, final String theAssignmentId, final String theMainOutcome,
                       final String... theSuboutcomes) {

        this.module = theModule;
        this.unit = theUnit;
        this.objective = theObjective;
        this.targetNumber = theTargetNumber;
        this.assignmentId = theAssignmentId;
        this.mainOutcome = theMainOutcome;
        this.subOutcomes = (theSuboutcomes != null && theSuboutcomes.length > 0) ? theSuboutcomes.clone() : null;

        this.exampleBlocks = new ArrayList<>(10);
    }

    /**
     * Creates and adds an example block.
     *
     * @param theTitle the title
     * @return the created example block
     */
    ExampleBlock addExBlock(final String theTitle) {

        final ExampleBlock block = new ExampleBlock(this.module.course, theTitle);

        this.exampleBlocks.add(block);

        return block;
    }
}
