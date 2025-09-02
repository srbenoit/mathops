package dev.mathops.app.ops.snapin.messaging.tosend;

import dev.mathops.db.schema.legacy.rec.RawStcourse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A subpopulation of a population that are all in the same section.
 */
public final class PopulationSection {

    /** A map from student ID to registration list for all students in the subpopulation. */
    public final Map<String, List<RawStcourse>> students;

    /** A map from student ID to the message due to be sent to that student. */
    final Map<String, MessageToSend> messagesDue;

    /**
     * Constructs a new {@code PopulationSection}.
     */
    PopulationSection() {

        this.students = new HashMap<>(200);
        this.messagesDue = new HashMap<>(100);
    }

    /**
     * Tests whether the population is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {

        return this.students.isEmpty();
    }

    /**
     * Counts the total number of students in the population.
     *
     * @return the total number of students
     */
    public int countStudents() {

        return this.students.size();
    }

    /**
     * Counts the total number of messages due to be sent.
     *
     * @return the total number of messages
     */
    public int countMessages() {

        return this.messagesDue.size();
    }

    /**
     * Attempts to locate and remove a particular message.
     *
     * @param msg the message to remove
     * @return true if the message was found and removed
     */
    public boolean remove(final MessageToSend msg) {

        final String stuId = msg.context.student.stuId;

        return this.messagesDue.remove(stuId) != null;
    }
}
