/**
 * A web-socket service to manage a list of conversations, each with posts from students and course assistants.
 * Conversations are private between the student and course assistants but may include several course assistants, and is
 * available to course assistants who may be helping the student.
 *
 * <p>
 * It is assumed that conversations are reset for each class (each semester), but data for the entire current semester
 * is retained and visible to all course assistants.
 */
package dev.mathops.web.websocket.help.conversation;
