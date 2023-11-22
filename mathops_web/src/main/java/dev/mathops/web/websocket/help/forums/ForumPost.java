package dev.mathops.web.websocket.help.forums;

import dev.mathops.core.CoreConstants;
import dev.mathops.web.websocket.help.StudentKey;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A forum post.
 *
 * <p>
 * All changes to the post data MUST be synchronized on the forum list that owns the forum, and MUST notify the owning
 * forum of the change.
 */
public final class ForumPost {

    /** The forum to which the post belongs. */
    private final Forum forum;

    /** The fixed post number within that forum, monotonically increasing over time. */
    private final int postNumber;

    /** The post number of the (earlier) post that is parent to this post, null if none. */
    private final Integer parentPostNumber;

    /** The date/time when the post was created. */
    private final LocalDateTime whenPosted;

    /** The author. */
    private final StudentKey author;

    /** The post state. */
    private EPostState state;

    /**
     * The date/time when the post was read by a learning assistant (same as post time for posts created by learning
     * assistants - null if not yet read by learning assistant).
     */
    private LocalDateTime whenRead;

    /** The post content - null until lazily loaded. */
    private String content;

    /**
     * Constructs a new {@code ForumPost}.
     *
     * @param theForum            the forum to which this post belongs
     * @param thePostNumber       the post number within the forum
     * @param theParentPostNumber the post number of the (earlier) post that is parent to this post, null if none
     * @param theWhenPosted       the date/time when the post was created
     * @param theAuthor           the author
     * @param theState            the initial post state
     */
    ForumPost(final Forum theForum, final int thePostNumber, final Integer theParentPostNumber,
              final LocalDateTime theWhenPosted, final StudentKey theAuthor, final EPostState theState) {

        if (theForum == null || theWhenPosted == null || theAuthor == null) {
            throw new IllegalArgumentException("Invalid arguments to construction of forum post");
        }

        this.forum = theForum;
        this.postNumber = thePostNumber;
        this.parentPostNumber = theParentPostNumber;
        this.whenPosted = theWhenPosted;
        this.author = theAuthor;
        this.state = theState;
    }

    /**
     * Gets the post state.
     *
     * @return the state
     */
    public EPostState getState() {

        synchronized (this.forum.owner) {
            return this.state;
        }
    }

    /**
     * Sets the post state.
     *
     * @param theState the new state
     */
    public void setState(final EPostState theState) {

        if (theState == null) {
            throw new IllegalArgumentException("Post state may not be null");
        }

        synchronized (this.forum.owner) {
            final EPostState oldState = this.state;
            this.state = theState;
            this.forum.postUpdated(this, oldState);
        }
    }

//    /**
//     * Gets the (server-local) date/time when the post was first read by a learning assistant.
//     *
//     * @return the read date/time (null if not yet read)
//     */
//    public LocalDateTime getWhenRead() {
//
//        synchronized (this.forum.owner) {
//            return this.whenRead;
//        }
//    }

//    /**
//     * Sets the (server-local) date/time when the post was first read by a learning assistant.
//     *
//     * @param theWhenRead the new read date/time (null to indicate unread)
//     */
//    public void setWhenRead(final LocalDateTime theWhenRead) {
//
//        synchronized (this.forum.owner) {
//            this.whenRead = theWhenRead;
//            this.forum.postUpdated(this, this.state);
//        }
//    }

    /**
     * Gets the post content.
     *
     * @return the content
     */
    public String getContent() {

        synchronized (this.forum.owner) {
            if (this.content == null) {
                ForumDatabase.loadPostContent(this);
            }

            return this.content;
        }
    }

    /**
     * Sets the post content.
     *
     * @param theContent the new content
     */
    public void setContent(final String theContent) {

        synchronized (this.forum.owner) {
            this.content = Objects.requireNonNullElse(theContent, CoreConstants.EMPTY);
            this.forum.postContentUpdated(this);
        }
    }
}
