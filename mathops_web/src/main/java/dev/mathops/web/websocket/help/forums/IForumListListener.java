package dev.mathops.web.websocket.help.forums;

/**
 * A list that will receive notification when the list of fora changes.
 */
public interface IForumListListener {

    /**
     * Called when a forum has been added to the list.
     *
     * @param theForum the forum that was added
     */
    void forumAdded(Forum theForum);

    /**
     * Called when the title of a forum is changed.
     *
     * @param forum the forum
     */
    void titleChanged(Forum forum);

    /**
     * Called when a post is added to a forum.
     *
     * @param thePost the post that was added
     */
    void postAdded(ForumPost thePost);

    /**
     * Called when the content of a post has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param thePost the post whose content changed
     */
    void postContentUpdated(ForumPost thePost);

    /**
     * Called when the state of a post has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param thePost the post whose state changed
     */
    void postUpdated(ForumPost thePost);

    /**
     * Called when the "when read" timestamp of a post has been updated. Should be called from within a block
     * synchronized on this object.
     *
     * @param thePost the post whose "when read" timestamp changed
     */
    void postWhenReadUpdated(ForumPost thePost);
}
