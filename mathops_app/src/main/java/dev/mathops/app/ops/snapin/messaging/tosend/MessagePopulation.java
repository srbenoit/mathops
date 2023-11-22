package dev.mathops.app.ops.snapin.messaging.tosend;

/**
 * A population for messaging.
 */
public final class MessagePopulation {

    /** The section number. */
    private final String section;

    /** The students. */
    public final PopulationSection population;

    /**
     * Constructs a new {@code MessagePopulation}.
     *
     * @param theSection    the section number
     * @param thePopulation the population
     */
    public MessagePopulation(final String theSection, final PopulationSection thePopulation) {

        this.section = theSection;
        this.population = thePopulation;
    }

    /**
     * Attempts to locate and remove a particular message.
     *
     * @param msg the message to remove
     * @return true if the message was found and removed
     */
    public boolean remove(final MessageToSend msg) {

        return this.population.remove(msg);
    }

    /**
     * Generates the string representation of the population.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "Sect. " + this.section + " (" + this.population.countStudents() + " students, "
                + this.population.countMessages() + " messages)";
    }
}
