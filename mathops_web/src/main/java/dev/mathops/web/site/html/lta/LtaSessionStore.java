package dev.mathops.web.site.html.lta;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Site;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.Attribute;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.EmptyElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;
import dev.mathops.text.parser.xml.XmlEscaper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A singleton storage class that keeps a map from session ID to a map from assignment ID to LTA session.
 */
public final class LtaSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME_XML = "lta_sessions.xml";

    /** The singleton instance. */
    private static final LtaSessionStore INSTANCE = new LtaSessionStore();

    /** Map from session ID to that session's map from assignment ID to LTA session. */
    private final HashMap<String, HashMap<String, LtaSession>> activeLtas;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private LtaSessionStore() {

        this.activeLtas = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static LtaSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves a copy of the map of active homework sessions.
     *
     * @return the homework session map (outer key is session ID, inner key is exam ID)
     */
    public HashMap<String, HashMap<String, LtaSession>> getLtaSessions() {

        synchronized (this.activeLtas) {
            return new HashMap<>(this.activeLtas);
        }
    }

    /**
     * Gets the active learning target session for an assignment ID, if any.
     *
     * @param sessionId    the session ID
     * @param assignmentId the assignment ID
     * @return the homework session; {@code null} if none
     */
    public LtaSession getLtaSession(final String sessionId, final String assignmentId) {

        // Called by pages in course sites that present homework

        synchronized (this.activeLtas) {
            final Map<String, LtaSession> sessionMap = this.activeLtas.get(sessionId);

            return sessionMap == null ? null : sessionMap.get(assignmentId);
        }
    }

    /**
     * Sets the active learning target session for an assignment ID.
     *
     * @param theSession the session
     */
    public void setLtaSession(final LtaSession theSession) {

        // Called by pages in course sites that present homework

        synchronized (this.activeLtas) {
            if (!theSession.isTimedOut()) {
                final Map<String, LtaSession> sessionMap =
                        this.activeLtas.computeIfAbsent(theSession.sessionId, s -> new HashMap<>(2));

                sessionMap.put(theSession.version, theSession);
            }
        }
    }

    /**
     * Removes the active learning target session for an assignment ID.
     *
     * @param sessionId    the session ID
     * @param assignmentId the assignment ID
     */
    void removeLtaSession(final String sessionId, final String assignmentId) {

        // Called by the homework session when the session ends

        synchronized (this.activeLtas) {
            final Map<String, LtaSession> sessionMap = this.activeLtas.get(sessionId);
            if (sessionMap != null) {
                sessionMap.remove(assignmentId);
                if (sessionMap.isEmpty()) {
                    this.activeLtas.remove(sessionId);
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

        synchronized (this.activeLtas) {

            final HtmlBuilder xml = new HtmlBuilder(1000);
            try {
                Log.info("Learning target session store persisting to " + dir.getAbsolutePath());

                for (final Map<String, LtaSession> maps : this.activeLtas.values()) {
                    for (final LtaSession session : maps.values()) {
                        if (session.isTimedOut()) {
                            Log.info("  Skipping timed out session ", session.sessionId);
                            continue;
                        }
                        Log.info("  Appending session ", session.sessionId);
                        session.appendXml(xml);
                    }
                }

                Log.info("Learning target session store generated XML");
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }

            if (dir.exists() || dir.mkdirs()) {
                final File targetXml = new File(dir, PERSIST_FILENAME_XML);

                try (final FileWriter writer = new FileWriter(targetXml, StandardCharsets.UTF_8)) {
                    Log.info("  Persisting to " + targetXml.getAbsolutePath());
                    writer.write(xml.toString());
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
            } else {
                Log.warning("Unable to create directory ", dir.getAbsolutePath());
            }
        }
    }

    /**
     * Restores session states persisted with the {@code persist} method. States that have been idle longer than the
     * session idle timeout will be discarded. If the server has been down for longer than the idle timeout, this means
     * all sessions will be discarded.
     *
     * @param cache the data cache
     * @param dir   the directory from which to load the active sessions
     * @throws SQLException if there is an error accessing the database
     */
    public void restore(final Cache cache, final File dir) throws SQLException {

        // Called from the web mid-controller when the app server is starting

        synchronized (this.activeLtas) {

            final File sourceXml = new File(dir, PERSIST_FILENAME_XML);
            if (sourceXml.exists()) {
                Log.info("Restoring learning target sessions from ", sourceXml.getAbsolutePath());
                final String xml = FileLoader.loadFileAsString(sourceXml, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setLtaSession(parseSession(cache, xml, (NonemptyElement) node));
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
                        Log.warning("Failed to delete ", bakXml.getAbsolutePath());
                    }
                }
                if (!sourceXml.renameTo(bakXml)) {
                    Log.warning("Failed to rename ", sourceXml.getAbsolutePath());
                }

                // sourceXml.delete();
            }
        }
    }

    /**
     * Parses a homework session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static LtaSession parseSession(final Cache cache, final String xml,
                                           final NonemptyElement elem) throws IllegalArgumentException, SQLException {

        String host = null;
        String path = null;
        String session = null;
        String student = null;
        String assign = null;
        String state = null;
        String sect = null;
        String item = null;
        String mastery = null;
        String redirect = null;
        String timeout = null;
        String score = null;
        String error = null;
        boolean started = false;
        ExamObj exam = null;

        final String tagName = elem.getTagName();
        if ("lta-session".equals(tagName)) {
            for (final INode node : elem.getChildrenAsList()) {
                if (node instanceof final EmptyElement child) {
                    final String tag = child.getTagName();
                    if ("started".equals(tag)) {
                        started = true;
                    }
                } else if (node instanceof final NonemptyElement child) {
                    final String tag = child.getTagName();

                    if (child.getNumChildren() == 1 && child.getChild(0) instanceof CData) {
                        final String content = ((CData) child.getChild(0)).content;

                        if ("host".equals(tag)) {
                            host = content;
                        } else if ("path".equals(tag)) {
                            path = content;
                        } else if ("session".equals(tag)) {
                            session = content;
                        } else if ("student".equals(tag)) {
                            student = content;
                        } else if ("assign-id".equals(tag)) {
                            assign = content;
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("cur-sect".equals(tag)) {
                            sect = content;
                        } else if ("cur-item".equals(tag)) {
                            item = content;
                        } else if ("mastery".equals(tag)) {
                            mastery = content;
                        } else if ("timeout".equals(tag)) {
                            timeout = content;
                        } else if ("score".equals(tag)) {
                            score = content;
                        } else if ("error".equals(tag)) {
                            error = content;
                        } else if ("redirect".equals(tag)) {
                            redirect = XmlEscaper.unescape(content);
                        }
                    } else if ("exam".equals(tag)) {
                        final String examXml = xml.substring(child.getStart(), child.getClosingTagSpan().getEnd());
                        exam = ExamFactory.load(examXml, EParserMode.ALLOW_DEPRECATED);

                        // Clean out DummyProblems used to store references in the exam...
                        final int numSect = exam.getNumSections();
                        for (int i = 0; i < numSect; ++i) {
                            final ExamSection examSect = exam.getSection(i);
                            final int numProb = examSect.getNumProblems();
                            for (int j = 0; j < numProb; ++j) {
                                final ExamProblem examProb = examSect.getProblem(j);
                                examProb.clearProblems();
                            }
                        }

                    } else if ("problems".equals(tag) && exam != null) {
                        final Attribute sectAttr = child.getAttribute("sect");
                        final Attribute probAttr = child.getAttribute("prob");

                        if (sectAttr == null || probAttr == null) {
                            throw new IllegalArgumentException("Missing sect/prob attribute on 'problems'");
                        }
                        final int sectNum = Integer.parseInt(sectAttr.value);
                        final int probNum = Integer.parseInt(probAttr.value);

                        if (sectNum < 0 || sectNum >= exam.getNumSections()) {
                            throw new IllegalArgumentException("Invalid section number in problem: " + sectAttr.value);
                        }
                        if (probNum < 0) {
                            throw new IllegalArgumentException("Invalid problem number in problem: " + probAttr.value);
                        }

                        final ExamSection examSect = exam.getSection(sectNum);
                        if (probNum >= examSect.getNumProblems()) {
                            throw new IllegalArgumentException("Invalid problem number in problem: " + probAttr.value);
                        }

                        final List<INode> problems = child.getChildrenAsList();
                        final ExamProblem examProb = examSect.getProblem(probNum);

                        if (examProb != null) {
                            NonemptyElement problemElem = null;
                            while (!problems.isEmpty()) { // Problems list changes in loop
                                final INode problemNode = problems.removeFirst();
                                if (problemNode instanceof NonemptyElement) {
                                    problemElem = (NonemptyElement) problemNode;
                                    break;
                                }
                            }
                            if (problemElem != null) {
                                final String problemXml = xml.substring(problemElem.getStart(),
                                        problemElem.getClosingTagSpan().getEnd());
                                try {
                                    final XmlContent content = new XmlContent(problemXml, false, false);
                                    final AbstractProblemTemplate possible =
                                            ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                                    examProb.addProblem(possible);
                                } catch (final ParsingException ex) {
                                    Log.warning(ex);
                                    throw new IllegalArgumentException("Unable to parse possible problem");
                                }
                            }
                        }

                    } else if ("selected-problem".equals(tag) && exam != null) {
                        final Attribute sectAttr = child.getAttribute("sect");
                        final Attribute probAttr = child.getAttribute("prob");

                        if (sectAttr == null || probAttr == null) {
                            throw new IllegalArgumentException("Missing sect/prob attribute on 'problems'");
                        }
                        final int sectNum = Integer.parseInt(sectAttr.value);
                        final int probNum = Integer.parseInt(probAttr.value);

                        if (sectNum < 0 || sectNum >= exam.getNumSections()) {
                            throw new IllegalArgumentException("Invalid section number in problem: " + sectAttr.value);
                        }
                        if (probNum < 0) {
                            throw new IllegalArgumentException("Invalid problem number in problem: " + probAttr.value);
                        }

                        final ExamSection examSect = exam.getSection(sectNum);
                        if (probNum >= examSect.getNumProblems()) {
                            throw new IllegalArgumentException("Invalid problem number in problem: " + probAttr.value);
                        }

                        final List<INode> problems = child.getChildrenAsList();
                        final ExamProblem examProb = examSect.getProblem(probNum);

                        if (examProb != null) {
                            NonemptyElement problemElem = null;
                            while (!problems.isEmpty()) { // Problems list changes within loop
                                final INode problemNode = problems.removeFirst();
                                if (problemNode instanceof NonemptyElement) {
                                    problemElem = (NonemptyElement) problemNode;
                                    break;
                                }
                            }
                            if (problemElem != null) {
                                final String problemXml = xml.substring(problemElem.getStart(),
                                        problemElem.getClosingTagSpan().getEnd());
                                try {
                                    final XmlContent content = new XmlContent(problemXml, false, false);
                                    final AbstractProblemTemplate selected =
                                            ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                                    examProb.setSelectedProblem(selected);
                                } catch (final ParsingException ex) {
                                    Log.warning(ex);
                                    throw new IllegalArgumentException("Unable to parse possible problem");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Expected 'lta-session', found '" + tagName + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'session'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'student'");
        }
        if (assign == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'assign-id'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'timeout'");
        }
        if (exam == null) {
            throw new IllegalArgumentException("'lta-session' was missing 'exam'");
        }

        final Site siteProfile = DatabaseConfig.getDefault().getSite(host, path);
        final Integer minMastery = mastery == null ? null : Integer.valueOf(mastery);
        final int theSection = sect == null ? 0 : Integer.parseInt(sect);
        final int theItem = item == null ? 0 : Integer.parseInt(item);
        final Integer scoreInt = score == null ? null : Integer.valueOf(score);

        final LtaSession sess = new LtaSession(cache, siteProfile, session, student, assign, redirect,
                ELtaState.valueOf(state), theSection, theItem, minMastery, Long.parseLong(timeout), started, scoreInt,
                error, exam);

        Log.info("Restoring LTA session for ", student, CoreConstants.SLASH, assign, CoreConstants.SLASH, state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     */
    public void purgeExpired() {

        // Called by the web mid-controller every 10 minutes

        synchronized (this.activeLtas) {
            final Iterator<Map.Entry<String, HashMap<String, LtaSession>>> outerIter =
                    this.activeLtas.entrySet().iterator();

            while (outerIter.hasNext()) {
                final Map<String, LtaSession> inner = outerIter.next().getValue();

                final Iterator<Map.Entry<String, LtaSession>> innerIter = inner.entrySet().iterator();

                while (innerIter.hasNext()) {
                    final LtaSession sess = innerIter.next().getValue();
                    if (sess.isTimedOut()) {
                        Log.info("Purging expired HTML learning target assignment session " + sess.sessionId);
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
