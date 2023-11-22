package dev.mathops.web.websocket.help.queue;

import dev.mathops.web.websocket.help.StudentKey;

/**
 * Information on an administrator who is online and monitoring the queue.
 */
public final class LiveHelpOnlineAdministrator {

    /** Administrator student information. */
    final StudentKey student;

    /** The current web socket connection from the administrator. */
    private HelpQueueWebSocket webSocket;

    /**
     * Constructs a new {@code LiveHelpOnlineAdministrator}.
     *
     * @param theStudent the student information
     * @throws IllegalArgumentException if the student information is null
     */
    LiveHelpOnlineAdministrator(final StudentKey theStudent) throws IllegalArgumentException {

        if (theStudent == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }

        this.student = theStudent;
    }

    /**
     * Gets the web socket connection to the administrator.
     *
     * @return the web socket
     */
    HelpQueueWebSocket getWebSocket() {

        return this.webSocket;
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
     * @return true if {@code o} is a {@code LiveHelpOnlineAdministrator} for the same administrator; false otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final LiveHelpOnlineAdministrator admin) {
            equal = admin.student.equals(this.student);
        } else {
            equal = false;
        }

        return equal;
    }
}
