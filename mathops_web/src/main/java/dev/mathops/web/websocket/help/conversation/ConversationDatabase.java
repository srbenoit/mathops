package dev.mathops.web.websocket.help.conversation;

import dev.mathops.core.EPath;
import dev.mathops.core.PathList;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.web.websocket.help.StudentKey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Data model for the set of all conversations.
 *
 * <p>
 * Data is stored in flat files, organized by term (directory), then student ID (subdirectory), and finally by message
 * number (one flat file with metadata, one with message content).
 *
 * <pre>
 * /[data]/conversations   [base directory for conversations]
 *   /888888888            [conversations owned by student 888888888]
 *     convlist.meta       [conversation list metadata, json format]
 *     /1                  [conversation number 1]
 *       conv.meta         [conversation metadata, json format]
 *       1.meta            [message 1 metadata, json format]
 *       1.content         [message 1 content, MIME/multipart format]
 *       2.meta            [message 2 metadata, json format]
 *       2.content         [message 2 content, MIME/multipart format]
 *       etc.
 * </pre>
 * <p>
 * Conversation list metadata file (convlist.meta) format:
 *
 * <pre>
 * {
 *   "studentId": "888888888",
 *   "firstName": "First name",
 *   "lastName": "Last name"
 * }
 * </pre>
 * <p>
 * Conversation metadata file (conv.meta) format:
 *
 * <pre>
 * {
 *  "subject": "The Subject Line, with quotes escaped"
 * }
 * </pre>
 * <p>
 * Message metadata file format:
 *
 * <pre>
 * {
 *   "whenCreated": "ISO-8601 date/time string",
 *   "byStudentId": "student ID",
 *   "byStudentName": "screen name, with quotes escaped",
 *   "state" : "state code",
 *   "whenRead": "ISO-8601 date/time string, absent if null"
 * }
 * </pre>
 */
public final class ConversationDatabase {

    /** The directory where conversations are stored. */
    private final File dir;

    /**
     * Constructs a new {@code ConversationDatabase}.
     */
    public ConversationDatabase() {

        final File dataPath = PathList.getInstance().get(EPath.CUR_DATA_PATH);

        File actualDir = null;

        final File curDir = new File(dataPath, "conversations");
        if (curDir.exists() && curDir.isDirectory()) {
            actualDir = curDir;
        }

        this.dir = actualDir == null ? curDir : actualDir;
    }

    /**
     * Loads all conversation information from the database. Called the first time conversation data is needed.
     *
     * @return the loaded container of all conversations
     */
    public ConversationsContainer load() {

        final ConversationsContainer all = new ConversationsContainer();

        final File[] stuIdDirs = this.dir.listFiles();

        if (stuIdDirs != null) {
            for (final File file : stuIdDirs) {
                if (file.isDirectory()) {
                    loadStudentConversationList(file, all);
                } else {
                    Log.warning("Unexpected file '", file.getName(), "' in ", this.dir.getAbsolutePath());
                }
            }
        }

        return all;
    }

    /**
     * Loads and validates the conversation list metadata and (if valid) creates a student conversation list and scans
     * all conversation subdirectories.
     *
     * @param stuIdDir the student ID directory
     * @param all      the conversation container to which to add loaded student conversation lists.
     */
    private static void loadStudentConversationList(final File stuIdDir, final ConversationsContainer all) {

        final String meta = FileLoader.loadFileAsString(new File(stuIdDir, "convlist.meta"), false);

        if (meta == null) {
            Log.warning("Unable to load 'convlist.meta' from ", stuIdDir.getAbsolutePath(), " - ignoring directory");
        } else {
            try {
                final StudentConversationList list = StudentConversationList.parse(all, stuIdDir.getName(), meta);
                all.loadStudentConversationList(list);

                final File[] conversationDirs = stuIdDir.listFiles();
                if (conversationDirs != null) {
                    for (final File file : conversationDirs) {
                        if (file.isDirectory()) {
                            try {
                                loadConversation(Integer.parseInt(file.getName()), file, list);
                            } catch (final NumberFormatException ex) {
                                Log.warning("Can't parse conversation number for '", file.getName(), "' in ",
                                        stuIdDir.getAbsolutePath(), ex);
                            }
                        } else {
                            Log.warning("Unexpected file '", file.getName(), "' in ", stuIdDir.getAbsolutePath());
                        }
                    }
                }
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse 'convlist.meta' from ", stuIdDir.getAbsolutePath(), ex);
            }
        }
    }

    // * conv.meta [conversation metadata, json format]
    // * 1.meta [message 1 metadata, json format]
    // * 1.content [message 1 content, MIME/multipart format]
    // * 2.meta [message 2 metadata, json format]
    // * 2.content [message 2 content, MIME/multipart format]

    /**
     * Loads a single conversation and adds it to a student conversation list.
     *
     * @param convNumber the conversation number
     * @param convDir    the conversation directory
     * @param list       the list to which to add the loaded conversation
     */
    private static void loadConversation(final int convNumber, final File convDir, final StudentConversationList list) {

        final String meta = FileLoader.loadFileAsString(new File(convDir, "conv.meta"), false);

        if (meta == null) {
            Log.warning("Unable to load 'conv.meta' from ", convDir.getAbsolutePath());
        } else {
            try {
                final Conversation conv = Conversation.parse(list, convNumber, meta);
                list.loadConversation(conv);

                // Scan metadata for messages
                int num = 1;
                File msgMeta = new File(convDir, num + ".meta");
                while (msgMeta.exists()) {
                    if (!loadStudentMessage(msgMeta, conv, num)) {
                        break;
                    }
                    ++num;
                    msgMeta = new File(convDir, num + ".meta");
                }
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse 'conv.meta' from ", convDir.getAbsolutePath(), ex);
            }
        }
    }

    /**
     * Loads the metadata for a message that is part of a conversation.
     *
     * @param file          the metadata file (JSON format)
     * @param conv          the conversation to which to add the loaded message
     * @param messageNumber the message number
     * @return true if success; false if failed
     */
    private static boolean loadStudentMessage(final File file, final Conversation conv, final int messageNumber) {

        final String meta = FileLoader.loadFileAsString(file, false);
        boolean result = false;

        if (meta == null) {
            Log.warning("Unable to load ", file.getAbsolutePath());
        } else {
            try {
                final ConversationMessage msg = ConversationMessage.parse(conv, messageNumber, meta);
                conv.loadMessage(msg);
                result = true;
            } catch (final ParsingException ex) {
                Log.warning("Unable to parse ", file.getAbsolutePath(), ex);
            }
        }

        return result;
    }

    /**
     * Attempts to lazily load the content of a conversation message. This should be called when a message is to be
     * viewed, but its content field is null.
     *
     * @param message the message (whose content should be null on entry)
     */
    void loadMessageContent(final ConversationMessage message) {

        final Conversation conv = message.conversation;
        final StudentKey key = conv.owner.studentKey;
        final File stuDir = new File(this.dir, key.studentId);
        final File convDir = new File(stuDir, Integer.toString(conv.conversationNumber));

        if (convDir.exists()) {
            final File file = new File(convDir, message.messageNumber + ".content");

            final String content = FileLoader.loadFileAsString(file, true);
            if (content != null) {
                message.loadContent(content);
            }
        } else {
            Log.warning("Conversation directory does not exist: ", convDir.getAbsolutePath());
        }
    }

    /**
     * Attempts to write a student conversation list to the database. This call does not write conversations (if any)
     * that exist in the list. it simply creates the student directory and writes the metadata.
     *
     * <p>
     * When adding a new student conversation list, this method should be called first, and only if it succeeds should
     * the list be added to the {@code AllConversations} container.
     *
     * <p>
     * NOTE: there is no programmatic way to remove a student conversation list once added. To do so, the student
     * directory can be deleted manually and then the database reloaded.
     *
     * @param list the list to write
     * @return true if succeeded; false if not
     */
    boolean writeStudentMessageList(final StudentConversationList list) {

        final StudentKey key = list.studentKey;
        final File stuDir = new File(this.dir, key.studentId);

        boolean ok = false;
        if (stuDir.exists() || stuDir.mkdirs()) {
            final File file = new File(stuDir, "convlist.meta");

            try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(list.toMetadataJson());
                ok = true;
            } catch (final IOException ex) {
                Log.warning("Failed to write student conversation list metadata", ex);
            }
        } else {
            Log.warning("Failed to write student conversation list metadata");
        }

        return ok;
    }

//    /**
//     * Attempts to write a conversation to the database. This call does not write messages (if any) that exist in the
//     * conversation. it simply creates the conversation directory and writes the metadata.
//     *
//     * <p>
//     * When adding a new conversation, this method should be called first, and only if it succeeds should the
//     * conversation be added to the {@code StudentConversationList}.
//     *
//     * <p>
//     * NOTE: there is no programmatic way to remove a conversation once added. To do so, the conversation directory
//     can
//     * be deleted manually and then the database reloaded.
//     *
//     * @param conv the conversation to write
//     * @return true if succeeded; false if not
//     */
//    boolean writeConversation(final Conversation conv) {
//
//        final StudentKey key = conv.owner.studentKey;
//        final File stuDir = new File(this.dir, key.studentId);
//        final File convDir = new File(stuDir, Integer.toString(conv.conversationNumber));
//
//        boolean ok = false;
//        if (convDir.exists() || convDir.mkdirs()) {
//            final File file = new File(stuDir, "conv.meta");
//
//            try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
//                fw.write(conv.toMetadataJson());
//                ok = true;
//            } catch (final IOException ex) {
//                Log.warning("Failed to write conversation metadata", ex);
//            }
//        } else {
//            Log.warning("Failed to write conversation metadata");
//        }
//
//        return ok;
//    }

    /**
     * Attempts to write a conversation message to the database. This call writes both the message metadata and the
     * message content.
     *
     * <p>
     * When adding a new conversation message, this method should be called first, and only if it succeeds should the
     * message be added to the {@code Conversation}.
     *
     * <p>
     * NOTE: there is no programmatic way to remove a message once added. To do so, the message files can be deleted
     * manually and then the database reloaded.
     *
     * @param message the message to write
     * @return true if succeeded; false if not
     */
    boolean writeMessage(final ConversationMessage message) {

        final Conversation conv = message.conversation;
        final StudentKey key = conv.owner.studentKey;
        final File stuDir = new File(this.dir, key.studentId);
        final File convDir = new File(stuDir, Integer.toString(conv.conversationNumber));

        boolean ok = false;
        if (convDir.exists()) {
            final File metaFile = new File(convDir, message.messageNumber + ".meta");

            try (final FileWriter fw1 = new FileWriter(metaFile, StandardCharsets.UTF_8)) {
                fw1.write(message.toMetadataJson());

                final File contentFile = new File(convDir, message.messageNumber + ".content");

                try (final FileWriter fw2 = new FileWriter(contentFile, StandardCharsets.UTF_8)) {
                    fw2.write(message.getContent());
                    ok = true;
                } catch (final IOException ex) {
                    Log.warning("Failed to write message content", ex);
                    if (!metaFile.delete()) {
                        Log.warning("Failed to delete meta file");
                    }
                }
            } catch (final IOException ex) {
                Log.warning("Failed to write message metadata", ex);
            }
        } else {
            Log.warning("Conversation directory does not exist: ", convDir.getAbsolutePath());
        }

        return ok;
    }

//    /**
//     * Writes the content of a message. This does not write the metadata. Used when the content of a
//     * message is edited.
//     *
//     * @param message the message whose content to write
//     * @return true if succeeded; false if not
//     */
//     public boolean writeMessageContent(final ConversationMessage message) {
//
//     final Conversation conv = message.conversation;
//     final StudentKey key = conv.owner.studentKey;
//     final File stuDir = new File(this.dir, key.studentId);
//     final File convDir = new File(stuDir, Integer.toString(conv.conversationNumber));
//
//     boolean ok = false;
//     if (convDir.exists() && convDir.isDirectory()) {
//     final File file = new File(convDir, //
//     message.messageNumber + ".content");
//
//     try (FileWriter fw = new FileWriter(file)) {
//     fw.write(message.getContent());
//     ok = true;
//     } catch (IOException ex) {
//     Log.warning("Failed to write message content", ex);
//     }
//     } else {
//     Log.warning("Conversation directory does not exist: ",
//     convDir.getAbsolutePath());
//     }
//
//     return ok;
//     }

    /**
     * Stores the metadata of a message. This call does not write the message content. Used when the metadata for a
     * message is edited.
     *
     * @param message the message whose metadata to write
     * @return true if succeeded; false if not
     */
    boolean writeMessageMetadata(final ConversationMessage message) {

        final Conversation conv = message.conversation;
        final StudentKey key = conv.owner.studentKey;
        final File stuDir = new File(this.dir, key.studentId);
        final File convDir = new File(stuDir, Integer.toString(conv.conversationNumber));

        boolean ok = false;
        if (convDir.exists() || convDir.mkdirs()) {
            final File file = new File(stuDir, "conv.meta");

            try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
                fw.write(conv.toMetadataJson());
                ok = true;
            } catch (final IOException ex) {
                Log.warning("Failed to write conversation metadata", ex);
            }
        } else {
            Log.warning("Conversation directory does not exist: ",
                    convDir.getAbsolutePath());
        }

        return ok;
    }
}
