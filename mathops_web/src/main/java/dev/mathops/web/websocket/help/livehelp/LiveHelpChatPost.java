package dev.mathops.web.websocket.help.livehelp;

import dev.mathops.web.websocket.help.StudentKey;

/**
 * A post in a live help chat stream.
 */
public final class LiveHelpChatPost {

    /** A unique ID per post to prevent page-refresh posts from duplicating messages. */
    private final String id;

    /** The timestamp on the post. */
    private final long timestamp;

    /** The type of post (determines how to interpret {@code content}). */
    private final EChatPostType type;

    /** True for posts from student, false for posts from learning assistant. */
    private final boolean fromStudent;

    /** The author. */
    private final StudentKey author;

    /** The content of the post. */
    private final String content;

    /**
     * Constructs a new {@code LiveHelpChatPost} with text content (used for TEXT and HTML post types).
     *
     * @param theId         a unique ID per post to prevent page-refresh posts from duplicating messages
     * @param theType       the type of post
     * @param isFromStudent true for posts from student, false for posts from learning assistant
     * @param theAuthor     the author information
     * @param theContent    the content of the post
     */
    LiveHelpChatPost(final String theId, final EChatPostType theType, final boolean isFromStudent,
                     final StudentKey theAuthor, final String theContent) {

        if (theId == null) {
            throw new IllegalArgumentException("Post ID may not be null");
        }
        if (theType == null) {
            throw new IllegalArgumentException("Post type may not be null");
        }
        if (theAuthor == null) {
            throw new IllegalArgumentException("Author may not be null");
        }
        if (theContent == null) {
            throw new IllegalArgumentException("Post text content may not be null");
        }

        this.id = theId;
        this.timestamp = System.currentTimeMillis();
        this.type = theType;
        this.fromStudent = isFromStudent;
        this.author = theAuthor;
        this.content = theContent;
    }
}
