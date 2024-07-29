package dev.mathops.web.site.html.pastla;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.Attribute;
import dev.mathops.commons.parser.xml.CData;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.WebSiteProfile;

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
 * A singleton storage class that keeps a map from session ID to a map from exam ID to past LTA session.
 */
public final class PastLtaSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "past_lta_sessions.xml";

    /** The singleton instance. */
    private static final PastLtaSessionStore INSTANCE = new PastLtaSessionStore();

    /** Map from session ID to that session's map from XML path to past exam session. */
    private final Map<String, Map<String, PastLtaSession>> activePastExams;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private PastLtaSessionStore() {

        this.activePastExams = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static PastLtaSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves a copy of the map of active past LTA sessions.
     *
     * @return the past LTA session map (outer key is session ID, inner key is XML path)
     */
    public Map<String, Map<String, PastLtaSession>> getPastLtaSessions() {

        synchronized (this.activePastExams) {
            return new HashMap<>(this.activePastExams);
        }
    }

    /**
     * Gets the active past LTA session for an exam ID, if any.
     *
     * @param sessionId the session ID
     * @param xml       the path for the exam XML
     * @return the review LTA session; {@code null} if none
     */
    public PastLtaSession getPastLtaSession(final String sessionId, final String xml) {

        synchronized (this.activePastExams) {
            final Map<String, PastLtaSession> sessionMap = this.activePastExams.get(sessionId);

            return sessionMap == null ? null : sessionMap.get(xml);
        }
    }

    /**
     * Sets the active past LTA session for an XML path.
     *
     * @param theSession the session
     */
    public void setPastLtaSession(final PastLtaSession theSession) {

        synchronized (this.activePastExams) {
            if (!theSession.isTimedOut()) {
                final Map<String, PastLtaSession> sessionMap =
                        this.activePastExams.computeIfAbsent(theSession.sessionId, s -> new HashMap<>(2));

                sessionMap.put(theSession.xmlFilename, theSession);
            }
        }
    }

    /**
     * Removes the active past LTA session for an XML path.
     *
     * @param sessionId the session ID
     * @param xml       the xml path
     */
    void removePastLtaSession(final String sessionId, final String xml) {

        synchronized (this.activePastExams) {
            final Map<String, PastLtaSession> sessionMap = this.activePastExams.get(sessionId);
            if (sessionMap != null) {
                sessionMap.remove(xml);

                if (sessionMap.isEmpty()) {
                    this.activePastExams.remove(sessionId);
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

        synchronized (this.activePastExams) {
            final HtmlBuilder xml = new HtmlBuilder(1000);
            try {
                Log.info("Past LTA session store persisting to " + dir.getAbsolutePath());

                for (final Map<String, PastLtaSession> maps : this.activePastExams.values()) {
                    for (final PastLtaSession session : maps.values()) {
                        if (session.isTimedOut()) {
                            Log.info("  Skipping timed out session ", session.sessionId);
                            continue;
                        }
                        Log.info("  Appending session ", session.sessionId);
                        session.appendXml(xml);
                    }
                }

                Log.info("Past LTA session store generated XML");
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }

            if (dir.exists() || dir.mkdirs()) {
                final File target = new File(dir, PERSIST_FILENAME);

                try (final FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8)) {
                    Log.info("  Persisting to " + target.getAbsolutePath());
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

        synchronized (this.activePastExams) {
            final File target = new File(dir, PERSIST_FILENAME);

            Log.info("Restoring past LTA sessions from ", target.getAbsolutePath());

            if (target.exists()) {
                final String xml = FileLoader.loadFileAsString(target, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setPastLtaSession(parseSession(cache, xml, (NonemptyElement) node));
                                } catch (final IllegalArgumentException ex) {
                                    Log.warning(ex);
                                }
                            }
                        }
                    }
                } catch (final ParsingException | DateTimeParseException | IllegalArgumentException ex) {
                    Log.warning(ex);
                }

                final File bak = new File(dir, PERSIST_FILENAME + ".bak");
                if (bak.exists()) {
                    if (!bak.delete()) {
                        Log.warning("Failed to delete ", bak.getAbsolutePath());
                    }
                }
                if (!target.renameTo(bak)) {
                    Log.warning("Failed to rename ", target.getAbsolutePath());
                }

                // target.delete();
            }
        }
    }

    /**
     * Parses a past exam session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static PastLtaSession parseSession(final Cache cache, final String xml, final NonemptyElement elem)
            throws IllegalArgumentException, SQLException {

        String host = null;
        String path = null;
        String session = null;
        String xmlpath = null;
        String student = null;
        String state = null;
        String error = null;
        String item = null;
        String redirect = null;
        String timeout = null;
        ExamObj exam = null;

        if ("past-lta-session".equals(elem.getTagName())) {
            for (final INode node : elem.getChildrenAsList()) {
                if (node instanceof final NonemptyElement child) {
                    final String tag = child.getTagName();

                    if (child.getNumChildren() == 1 && child.getChild(0) instanceof CData) {
                        final String content = ((CData) child.getChild(0)).content;

                        if ("host".equals(tag)) {
                            host = content;
                        } else if ("path".equals(tag)) {
                            path = content;
                        } else if ("session".equals(tag)) {
                            session = content;
                        } else if ("xml".equals(tag)) {
                            xmlpath = content;
                        } else if ("student".equals(tag)) {
                            student = content;
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("error".equals(tag)) {
                            error = content;
                        } else if ("cur-item".equals(tag)) {
                            item = content;
                        } else if ("timeout".equals(tag)) {
                            timeout = content;
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
                                    final AbstractProblemTemplate selected =
                                            ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                                    examProb.setSelectedProblem(selected);
                                } catch (final ParsingException ex) {
                                    Log.warning(ex);
                                    throw new IllegalArgumentException("Unable to parse possible problem", ex);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Expected 'past-lta-session', found '" + elem.getTagName() + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'session'");
        }
        if (xmlpath == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'xml'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'student'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'timeout'");
        }
        if (exam == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'exam'");
        }
        if (item == null) {
            throw new IllegalArgumentException("'past-lta-session' was missing 'item'");
        }

        final WebSiteProfile siteProfile = ContextMap.getDefaultInstance().getWebSiteProfile(host, path);

        final PastLtaSession sess = new PastLtaSession(cache, siteProfile, session, xmlpath, student, redirect,
                EPastLtaState.valueOf(state), error, Integer.parseInt(item), Long.parseLong(timeout), exam);

        Log.info("Restoring past LTA session for ", student, CoreConstants.SLASH, xmlpath, CoreConstants.SLASH, state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     */
    public void purgeExpired() {

        synchronized (this.activePastExams) {
            final Iterator<Map.Entry<String, Map<String, PastLtaSession>>> outerIter =
                    this.activePastExams.entrySet().iterator();

            while (outerIter.hasNext()) {
                final Map<String, PastLtaSession> inner = outerIter.next().getValue();

                final Iterator<Map.Entry<String, PastLtaSession>> innerIter = inner.entrySet().iterator();

                while (innerIter.hasNext()) {
                    final PastLtaSession sess = innerIter.next().getValue();
                    if (sess.isTimedOut()) {
                        Log.info("Purging expired HTML past exam session " + sess.sessionId);
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
