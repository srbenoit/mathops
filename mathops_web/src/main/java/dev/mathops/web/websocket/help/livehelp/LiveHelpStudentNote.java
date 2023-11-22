package dev.mathops.web.websocket.help.livehelp;

import dev.mathops.web.websocket.help.StudentKey;

/**
 * A student node.
 */
public final class LiveHelpStudentNote {

    /** A unique ID per note to prevent page-refresh posts from duplicating notes. */
    private final String id;

    /** The timestamp when the note was added. */
    private final long timestamp;

    /** The author. */
    private final StudentKey author;

    /** The note content (HTML). */
    private final String content;

    /**
     * Constructs a new {@code LiveHelpStudentNote} with text content (used for TEXT and HTML post types).
     *
     * @param theId      a unique ID per post to prevent page-refresh posts from duplicating messages
     * @param theAuthor  the author information
     * @param theContent the HTML content of the note
     */
    LiveHelpStudentNote(final String theId, final StudentKey theAuthor, final String theContent) {

        if (theId == null) {
            throw new IllegalArgumentException("Post ID may not be null");
        }
        if (theAuthor == null) {
            throw new IllegalArgumentException("Author may not be null");
        }
        if (theContent == null) {
            throw new IllegalArgumentException("Note content may not be null");
        }

        this.id = theId;
        this.timestamp = System.currentTimeMillis();
        this.author = theAuthor;
        this.content = theContent;
    }
}
