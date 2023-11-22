package dev.mathops.web.websocket.help.forums;

/**
 * Manager for database storage and retrieval of forum information.
 */
public enum ForumDatabase {
    ;

    /**
     * Loads all forums information from the database. Called the first time forums data is needed.
     *
     * @param target the target to which to add loaded forums (this object should be empty and should have no registered
     *               listeners at the time this call is made)
     */
    public static void load(final ForumList target) {

        final Forum forum117 = new Forum(target, "MATH 117");
        target.addForum(forum117);

        final Forum forum118 = new Forum(target, "MATH 118");
        target.addForum(forum118);

        final Forum forum124 = new Forum(target, "MATH 124");
        target.addForum(forum124);

        final Forum forum125 = new Forum(target, "MATH 125");
        target.addForum(forum125);

        final Forum forum126 = new Forum(target, "MATH 126");
        target.addForum(forum126);
    }

    /**
     * Attempts to lazily load the content of a forum post.
     *
     * @param post the post (whose content should be null on entry)
     */
    static void loadPostContent(final ForumPost post) {

        // TODO:
    }

    /**
     * Stores the content of a post.
     *
     * @param post the post
     */
    static void storePostContent(final ForumPost post) {

        // TODO:
    }

    /**
     * Stores a post. This call does not store the post content - just the non-content fields.
     *
     * @param post the post
     */
    static void storePost(final ForumPost post) {

        // TODO:
    }

    /**
     * Updates the stored post state and/or when-read timestamp of a message. This call does not store changes to the
     * post content.
     *
     * @param post the post
     */
    static void updatePost(final ForumPost post) {

        // TODO:
    }
}
