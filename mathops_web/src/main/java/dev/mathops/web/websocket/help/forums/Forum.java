package dev.mathops.web.websocket.help.forums;

import dev.mathops.web.websocket.help.StudentKey;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * State for an individual forum.
 *
 * <p>
 * A forum post from a student must eventually be resolved. It is resolved if it is answered by a learning assistant, or
 * marked by a learning assistant as needing no further answer (either it was not a question, like a "Thanks!" post, or
 * it was adequately answered by another student's post). Course assistants will be able to see all student posts that
 * have not been resolved.
 *
 * <p>
 * Course assistants can also "star" student posts that give particularly good answers, as a recognition of the posting
 * student, and as a flag that the response is a good reference for other participants.
 *
 * <p>
 * Any participant can edit or delete their own posts. If a post is deleted after being responded to, a "deleted post"
 * placeholder will remain to "own" the responses.
 */
public final class Forum {

    /** The owning forum list. */
    public final ForumList owner;

    /** The forum title. */
    public final String title;

    /** The posts in the forum (where post content is loaded lazily). */
    private final List<ForumPost> posts;

    /** The last post number used. */
    private int lastPostNumber;

    /** The number of undeleted posts. */
    private int numUndeleted;

    /** The number of posts that have not been read by a course assistant. */
    private int numUnread;

    /**
     * Constructs a new {@code Forum}.
     *
     * @param theOwner the owning forum list
     * @param theTitle the forum title
     */
    Forum(final ForumList theOwner, final String theTitle) {

        this.owner = theOwner;
        this.title = theTitle;
        this.posts = new ArrayList<>(10);
    }

    /**
     * Gets the total number of posts.
     *
     * @return the total number of posts
     */
    public int getTotalPosts() {

        synchronized (this.owner) {
            return this.posts.size();
        }
    }

    /**
     * Retrieves a specific post from a forum.
     *
     * @param index the index of the post
     * @return the post
     */
    public ForumPost getPost(final int index) {

        synchronized (this.owner) {
            return this.posts.get(index);
        }
    }

    /**
     * Gets the number of undeleted posts.
     *
     * @return the number of undeleted posts
     */
    int getNumUndeleted() {

        synchronized (this.owner) {
            return this.numUndeleted;
        }
    }

    /**
     * Gets the number of unread posts.
     *
     * @return the number of unread posts
     */
    int getNumUnread() {

        synchronized (this.owner) {
            return this.numUnread;
        }
    }

    /**
     * Adds a post to this forum.
     *
     * @param theParentPostNumber the post number of the parent post; null if starting a new thread
     * @param theAuthor           the author information
     * @param theState            the initial message state
     * @return the generated message
     */
    public ForumPost addPost(final Integer theParentPostNumber, final StudentKey theAuthor,
                             final EPostState theState) {

        synchronized (this.owner) {
            ++this.lastPostNumber;

            final ForumPost post = new ForumPost(this, this.lastPostNumber, theParentPostNumber,
                    LocalDateTime.now(), theAuthor, theState);

            this.posts.add(post);

            if (theState == EPostState.UNREAD) {
                ++this.numUnread;
            }
            if (theState != EPostState.DELETED) {
                ++this.numUndeleted;
            }

            ForumDatabase.storePost(post);

            this.owner.postAdded(post);

            return post;
        }
    }

    /**
     * Called when the content of a post has been updated.
     *
     * @param thePost the post whose content changed
     */
    void postContentUpdated(final ForumPost thePost) {

        synchronized (this.owner) {
            ForumDatabase.storePostContent(thePost);
            this.owner.postContentUpdated(thePost);
        }
    }

    /**
     * Called when the state and/or when-read timestamp of a post has been updated.
     *
     * @param thePost  the post whose state or when-read timestamp changed
     * @param oldState the old state
     */
    void postUpdated(final ForumPost thePost, final EPostState oldState) {

        synchronized (this.owner) {
            if (oldState == EPostState.UNREAD && thePost.getState() != EPostState.UNREAD) {
                --this.numUnread;
            } else if (oldState != EPostState.UNREAD && thePost.getState() == EPostState.UNREAD) {
                ++this.numUnread;
            }

            if (oldState == EPostState.DELETED && thePost.getState() != EPostState.DELETED) {
                ++this.numUndeleted;
            } else if (oldState != EPostState.DELETED && thePost.getState() == EPostState.DELETED) {
                --this.numUndeleted;
            }

            ForumDatabase.updatePost(thePost);
            this.owner.postUpdated(thePost);
        }
    }
}
