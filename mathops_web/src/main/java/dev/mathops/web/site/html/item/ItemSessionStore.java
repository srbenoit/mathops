package dev.mathops.web.site.html.item;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A singleton storage class that keeps a map from session ID to a map from GUID to item session.
 */
public final class ItemSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME_XML = "item_sessions.xml";

    /** The singleton instance. */
    private static final ItemSessionStore INSTANCE = new ItemSessionStore();

    /** Map from item student ID to a map from GUID to item session. */
    private final HashMap<String, HashMap<String, ItemSession>> activeItems;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ItemSessionStore() {

        this.activeItems = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static ItemSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves a copy of the map of active item sessions.
     *
     * @return the item session map (key is item session ID)
     */
    public Map<String, Map<String, ItemSession>> getItemSessions() {

        synchronized (this.activeItems) {
            return new HashMap<>(this.activeItems);
        }
    }

    /**
     * Gets the active item session for a student and GUID, if any
     *
     * @param studentId the student ID
     * @param guid      the GUID
     * @return the item session; {@code null} if none
     */
    public ItemSession getItemSession(final String studentId, final String guid) {

        synchronized (this.activeItems) {
            final Map<String, ItemSession> sessionMap = this.activeItems.get(studentId);

            return sessionMap == null ? null : sessionMap.get(guid);
        }
    }

    /**
     * Sets the active item session for a GUID.
     *
     * @param theSession the session
     */
    public void setItemSession(final ItemSession theSession) {

        // Called by pages in course sites that present homework

        synchronized (this.activeItems) {
            if (!theSession.isTimedOut()) {
                final Map<String, ItemSession> sessionMap =
                        this.activeItems.computeIfAbsent(theSession.studentId, s -> new HashMap<>(2));

                sessionMap.put(theSession.guid, theSession);
            }
        }
    }

    /**
     * Removes the active item student for a GUID.
     *
     * @param studentId the student ID
     * @param guid      the GUID
     */
    void removeItemSession(final String studentId, final String guid) {

        // Called by the homework session when the session ends

        synchronized (this.activeItems) {
            final Map<String, ItemSession> sessionMap = this.activeItems.get(studentId);
            if (sessionMap != null) {
                sessionMap.remove(guid);
                if (sessionMap.isEmpty()) {
                    this.activeItems.remove(studentId);
                }
            }
        }
    }

    /**
     * Persists the session store on server shutdown. The persisted session states can be restored on server restart.
     *
     * @param dir the directory in which to persist the active sessions
     */
    public void persist(final File dir) {

        // Called from the front controller when the app server is closing

        synchronized (this.activeItems) {

            final HtmlBuilder xml = new HtmlBuilder(1000);
            final String persistDirPath = dir.getAbsolutePath();

            try {
                Log.info("Item session store persisting to ", persistDirPath);

                for (final Map<String, ItemSession> maps : this.activeItems.values()) {
                    for (final ItemSession session : maps.values()) {
                        if (session.isTimedOut()) {
                            Log.info("  Skipping timed out session for ", session.studentId);
                            continue;
                        }
                        Log.info("  Appending session for ", session.studentId);
                        session.appendXml(xml);
                    }
                }

                Log.info("Item session store generated XML");
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }

            if (dir.exists() || dir.mkdirs()) {
                final File targetXml = new File(dir, PERSIST_FILENAME_XML);

                try (final FileWriter writer = new FileWriter(targetXml, StandardCharsets.UTF_8)) {
                    final String targetPath = targetXml.getAbsolutePath();
                    Log.info("  Persisting to ", targetPath);

                    final String xmlString = xml.toString();
                    writer.write(xmlString);
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
            } else {
                Log.warning("Unable to create directory ", persistDirPath);
            }
        }
    }

    /**
     * Restores session states persisted with the {@code persist} method. States that have been idle longer than the
     * session idle timeout will be discarded. If the server has been down for longer than the idle timeout, this means
     * all sessions will be discarded.
     *
     * @param dir the directory from which to load the active sessions
     */
    public void restore(final File dir) {

        // Called from the web mid-controller when the app server is starting

        synchronized (this.activeItems) {

            final File sourceXml = new File(dir, PERSIST_FILENAME_XML);
            if (sourceXml.exists()) {
                final String srcPath = sourceXml.getAbsolutePath();
                Log.info("Restoring item sessions from ", srcPath);
                final String xml = FileLoader.loadFileAsString(sourceXml, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof final NonemptyElement nonempty) {
                                try {
                                    final ItemSession session = parseSession(nonempty);
                                    setItemSession(session);
                                } catch (final IllegalArgumentException ex) {
                                    Log.warning(ex);
                                }
                            }
                        }
                    }
                } catch (final ParsingException | DateTimeParseException | IllegalArgumentException ex) {
                    Log.warning(ex);
                }

                final File bakXml = new File(dir, PERSIST_FILENAME_XML + ".bak");
                if (bakXml.exists()) {
                    if (!bakXml.delete()) {
                        final String bakPath = bakXml.getAbsolutePath();
                        Log.warning("Failed to delete ", bakPath);
                    }
                }
                if (!sourceXml.renameTo(bakXml)) {
                    Log.warning("Failed to rename ", srcPath);
                }

                // sourceXml.delete();
            }
        }
    }

    /**
     * Parses an item session from its XML element.
     *
     * @param elem the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     */
    private static ItemSession parseSession(final NonemptyElement elem) throws IllegalArgumentException {

        String studentId = null;
        String guid = null;
        String treeRef = null;
        String state = null;
        String timeout = null;
        AbstractProblemTemplate item = null;

        final String tagName = elem.getTagName();

        if ("item-session".equals(tagName)) {
            for (final INode node : elem.getChildrenAsList()) {
                if (node instanceof final NonemptyElement child) {
                    final String tag = child.getTagName();

                    if (child.getNumChildren() == 1 && child.getChild(0) instanceof CData) {
                        final String content = ((CData) child.getChild(0)).content;

                        if ("student".equals(tag)) {
                            studentId = content;
                        } else if ("guid".equals(tag)) {
                            guid = content;
                        } else if ("tree-ref".equals(tag)) {
                            treeRef = content;
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("timeout".equals(tag)) {
                            timeout = content;
                        }
                    } else if ("problem".equals(tag)) {
                        item = ProblemTemplateFactory.parseFromProblemElement(child, EParserMode.NORMAL);
                    } else if ("problem-multiple-choice".equals(tag)) {
                        item = ProblemTemplateFactory.parseFromProblemMultipleChoiceElement(child, EParserMode.NORMAL);
                    } else if ("problem-multiple-selection".equals(tag)) {
                        item = ProblemTemplateFactory.parseFromProblemMultipleSelectionElement(child,
                                EParserMode.NORMAL);
                    } else if ("problem-numeric".equals(tag)) {
                        item = ProblemTemplateFactory.parseFromProblemNumericElement(child, EParserMode.NORMAL);
                    } else if ("problem-embedded-input".equals(tag)) {
                        item = ProblemTemplateFactory.parseFromProblemEmbeddedInputElement(child, EParserMode.NORMAL);
                    } else if ("problem-auto-correct".equals(tag)) {
                        item = ProblemTemplateFactory.parseAutocorrectProblem();
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Expected 'item-session', found '" + tagName + "'");
        }

        if (studentId == null) {
            throw new IllegalArgumentException("'item-session' was missing 'student' attribute");
        }
        if (guid == null) {
            throw new IllegalArgumentException("'item-session' was missing 'guid' attribute");
        }
        if (treeRef == null) {
            throw new IllegalArgumentException("'item-session' was missing 'tree-ref' attribute");
        }
        if (state == null) {
            throw new IllegalArgumentException("'item-session' was missing 'state' attribute");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'item-session' was missing 'timeout' attribute");
        }
        if (item == null) {
            throw new IllegalArgumentException("'item-session' was missing 'problem' child element");
        }

        final EItemState stateObj = EItemState.valueOf(state);
        final long timeoutVal = Long.parseLong(timeout);

        final ItemSession session = new ItemSession(studentId, guid, treeRef, stateObj, timeoutVal, item);

        Log.info("Restoring item session for ", studentId, CoreConstants.SLASH, guid, CoreConstants.SLASH, state);

        return session;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     */
    public void purgeExpired() {

        // Called by the web mid-controller every 10 minutes

        synchronized (this.activeItems) {
            final Iterator<Map.Entry<String, HashMap<String, ItemSession>>> outerIter =
                    this.activeItems.entrySet().iterator();

            while (outerIter.hasNext()) {
                final Map<String, ItemSession> inner = outerIter.next().getValue();

                final Iterator<Map.Entry<String, ItemSession>> innerIter = inner.entrySet().iterator();

                while (innerIter.hasNext()) {
                    final ItemSession sess = innerIter.next().getValue();
                    if (sess.isTimedOut()) {
                        Log.info("Purging expired HTML item session for " + sess.studentId);
                        innerIter.remove();
                    }
                }

                if (inner.isEmpty()) {
                    outerIter.remove();
                }
            }
        }
    }
}
