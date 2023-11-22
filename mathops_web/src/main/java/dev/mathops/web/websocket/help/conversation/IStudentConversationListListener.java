package dev.mathops.web.websocket.help.conversation;

/**
 * A list that will receive notification when the list of conversations changes.
 */
interface IStudentConversationListListener {

    /**
     * Called when a conversation has been added to the list.
     *
     * @param theConversation the conversation that was added
     */
    void conversationAdded(Conversation theConversation);

    /**
     * Called when the counts of tracked messages changes in a conversation.
     *
     * @param conversation the conversation
     */
    void conversationChanged(Conversation conversation);

    /**
     * Called when the subject of a conversation is changed.
     *
     * @param conversation the conversation
     */
    void subjectChanged(Conversation conversation);

    /**
     * Called when a message is added to a conversation.
     *
     * @param theMessage the message that was added
     */
    void messageAdded(ConversationMessage theMessage);

    /**
     * Called when the state of a message has been updated. Should be called from within a block synchronized on this
     * object.
     *
     * @param theMessage the message whose state changed
     */
    void messageStateUpdated(ConversationMessage theMessage);

    /**
     * Called when the "when read" timestamp of a message has been updated. Should be called from within a block
     * synchronized on this object.
     *
     * @param theMessage the message whose "when read" timestamp changed
     */
    void messageWhenReadUpdated(ConversationMessage theMessage);
}
