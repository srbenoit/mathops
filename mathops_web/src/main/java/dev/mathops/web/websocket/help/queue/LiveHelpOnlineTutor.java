package dev.mathops.web.websocket.help.queue;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.websocket.help.StudentKey;

/**
 * Information on a tutor who is online and available to help students.
 */
final class LiveHelpOnlineTutor {

    /** Student information. */
    final StudentKey student;

    /** The current web socket connection from the tutor. */
    private HelpQueueWebSocket webSocket;

    /**
     * Constructs a new {@code LiveHelpOnlineTutor}.
     *
     * @param theStudent the student information
     * @throws IllegalArgumentException if the student information is null
     */
    LiveHelpOnlineTutor(final StudentKey theStudent) throws IllegalArgumentException {

        if (theStudent == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }

        this.student = theStudent;
    }

    /**
     * Gets the web socket connection to the tutor.
     *
     * @return the web socket
     */
    HelpQueueWebSocket getWebSocket() {

        return this.webSocket;
    }

    /**
     * Sets the web socket connection to the tutor.
     *
     * @param theWebSocket the web socket
     */
    void setWebSocket(final HelpQueueWebSocket theWebSocket) {

        this.webSocket = theWebSocket;
    }

    /**
     * Serializes the entry in JSON format to a {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    void toJSON(final HtmlBuilder htm) {

        htm.add("{\"stu\": \"", this.student.studentId,
                "\", \"name\": \"", this.student.screenName, "\"}");
    }

    /**
     * Gets a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.student.hashCode();
    }

    /**
     * Tests this object for equality to another.
     *
     * @param obj the other object
     * @return true if {@code o} is a {@code LiveHelpOnlineTutor} for the same tutor; false otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final LiveHelpOnlineTutor tutor) {
            equal = tutor.student.equals(this.student);
        } else {
            equal = false;
        }

        return equal;
    }
}
