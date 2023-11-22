package dev.mathops.web.websocket.help.conversation;

/**
 * A list that will receive notification when the list of conversations changes.
 */
@FunctionalInterface
interface IConversationsContainerListener {

    /**
     * Called when a student conversation list has been added.
     *
     * @param theList the conversation list that was added
     */
    void studentConversationListAdded(StudentConversationList theList);
}
