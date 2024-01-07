package dev.mathops.web.site.html.reviewexam;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.Attribute;
import dev.mathops.core.parser.xml.CData;
import dev.mathops.core.parser.xml.EmptyElement;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.db.old.Cache;
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
 * A singleton storage class that keeps a map from session ID to a map from exam ID to review exam session.
 */
public final class ReviewExamSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "review_exam_sessions.xml";

    /** The singleton instance. */
    private static final ReviewExamSessionStore INSTANCE = new ReviewExamSessionStore();

    /** Map from session ID to that session's map from exam ID to exam session. */
    private final Map<String, Map<String, ReviewExamSession>> activeReviewExams;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ReviewExamSessionStore() {

        this.activeReviewExams = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static ReviewExamSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves the map of active review exam sessions. This is the live map which can be manipulated.
     *
     * <p>
     * Any changes to the returned may MUST be done within a block synchronized on the map.
     *
     * @return the review exam session map (outer key is session ID, inner key is exam ID)
     */
    public Map<String, Map<String, ReviewExamSession>> getReviewExamSessions() {

        return this.activeReviewExams;
    }

    /**
     * Gets the active review exam session for an exam ID, if any.
     *
     * @param sessionId the session ID
     * @param examId    the exam ID
     * @return the review exam session; {@code null} if none
     */
    public ReviewExamSession getReviewExamSession(final String sessionId, final String examId) {

        synchronized (this.activeReviewExams) {
            final Map<String, ReviewExamSession> sessionMap = this.activeReviewExams.get(sessionId);

            return sessionMap == null ? null : sessionMap.get(examId);
        }
    }

    /**
     * Sets the active review exam session for an exam ID.
     *
     * @param theSession the session
     */
    public void setReviewExamSession(final ReviewExamSession theSession) {

        synchronized (this.activeReviewExams) {
            if (!theSession.isTimedOut()) {
                final Map<String, ReviewExamSession> sessionMap =
                        this.activeReviewExams.computeIfAbsent(theSession.sessionId, s -> new HashMap<>(2));
                sessionMap.put(theSession.version, theSession);
            }
        }
    }

    /**
     * Removes the active review exam session for an assignment ID.
     *
     * @param sessionId the session ID
     * @param examId    the exam ID
     */
    void removeReviewExamSession(final String sessionId, final String examId) {

        synchronized (this.activeReviewExams) {
            final Map<String, ReviewExamSession> sessionMap = this.activeReviewExams.get(sessionId);
            if (sessionMap != null) {
                sessionMap.remove(examId);
                if (sessionMap.isEmpty()) {
                    this.activeReviewExams.remove(sessionId);
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

        final HtmlBuilder xml = new HtmlBuilder(1000);

        synchronized (this.activeReviewExams) {
            try {
                Log.info("Review exam session store persisting to "
                        + dir.getAbsolutePath());

                for (final Map<String, ReviewExamSession> maps : this.activeReviewExams.values()) {
                    for (final ReviewExamSession session : maps.values()) {
                        if (session.isTimedOut()) {
                            Log.info("  Skiping timed out session ", session.sessionId);
                            continue;
                        }
                        Log.info("  Appending session ", session.sessionId);
                        session.appendXml(xml);
                    }
                }

                Log.info("Review exam session store generated XML");
            } catch (final RuntimeException ex) {
                Log.warning(ex);
            }
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

        synchronized (this.activeReviewExams) {
            final File target = new File(dir, PERSIST_FILENAME);

            Log.info("Restoring review exam sessions from ", target.getAbsolutePath());

            if (target.exists()) {
                final String xml = FileLoader.loadFileAsString(target, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setReviewExamSession(parseSession(cache, xml, (NonemptyElement) node));
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
     * Parses a review exam session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static ReviewExamSession parseSession(final Cache cache, final String xml,
                                                  final NonemptyElement elem) throws IllegalArgumentException,
            SQLException {

        String host = null;
        String path = null;
        String session = null;
        String student = null;
        String assign = null;
        String score = null;
        String mastery = null;
        String state = null;
        String item = null;
        String error = null;
        boolean practice = false;
        boolean started = false;
        String redirect = null;
        String timeout = null;
        ExamObj exam = null;

        if ("review-exam-session".equals(elem.getTagName())) {
            for (final INode node : elem.getChildrenAsList()) {
                if (node instanceof EmptyElement) {
                    final IElement child = (IElement) node;
                    final String tag = child.getTagName();
                    if ("practice".equals(tag)) {
                        practice = true;
                    } else if ("started".equals(tag)) {
                        started = true;
                    }
                } else if (node instanceof NonemptyElement) {
                    final NonemptyElement child = (NonemptyElement) node;
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
                        } else if ("exam-id".equals(tag)) {
                            assign = content;
                        } else if ("score".equals(tag)) {
                            score = content;
                        } else if ("mastery".equals(tag)) {
                            mastery = content;
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("cur-item".equals(tag)) {
                            item = content;
                        } else if ("error".equals(tag)) {
                            error = content;
                        } else if ("timeout".equals(tag)) {
                            timeout = content;
                        } else if ("redirect".equals(tag)) {
                            redirect = XmlEscaper.unescape(content);
                        }
                    } else if ("redirect".equals(tag)) {
                        final int count = child.getNumChildren();
                        Log.warning("Redirect has " + count + " children");
                        for (int i = 0; i < count; ++i) {
                            Log.warning("Redirect child " + i + " is " + child.getChild(i).getClass().getName());
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
                            throw new IllegalArgumentException("Invalid problem number in problem: "  + probAttr.value);
                        }

                        final List<INode> problems = child.getChildrenAsList();
                        final ExamProblem examProb = examSect.getProblem(probNum);

                        if (examProb != null) {
                            NonemptyElement problemElem = null;
                            while (!problems.isEmpty()) { // Problems list changes within loop
                                final INode problemNode = problems.remove(0);
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
                                    throw new IllegalArgumentException( "Unable to parse possible problem");
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Expected 'review-exam-session', found '" + elem.getTagName() + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'session'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'student'");
        }
        if (assign == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'assign-id'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'timeout'");
        }
        if (exam == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'exam'");
        }
        if (item == null) {
            throw new IllegalArgumentException("'review-exam-session' was missing 'item'");
        }

        final WebSiteProfile siteProfile = ContextMap.getDefaultInstance().getWebSiteProfile(host, path);
        final Integer scoreInt = score == null ? null : Integer.valueOf(score);
        final Integer minMastery = mastery == null ? null : Integer.valueOf(mastery);

        final ReviewExamSession sess = new ReviewExamSession(cache, siteProfile, session, student,
                assign, practice, redirect, EReviewExamState.valueOf(state), scoreInt, minMastery,
                started, Integer.parseInt(item), Long.parseLong(timeout), exam, error);

        Log.info("Restoring review exam session for ", student, CoreConstants.SLASH, assign, CoreConstants.SLASH,
                state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     *
     * @param cache the data cache
     */
    public void purgeExpired(final Cache cache) {

        synchronized (this.activeReviewExams) {
            final Iterator<Map.Entry<String, Map<String, ReviewExamSession>>> outerIter =
                    this.activeReviewExams.entrySet().iterator();

            while (outerIter.hasNext()) {
                final Map<String, ReviewExamSession> inner = outerIter.next().getValue();

                final Iterator<Map.Entry<String, ReviewExamSession>> innerIter =
                        inner.entrySet().iterator();

                while (innerIter.hasNext()) {
                    final ReviewExamSession sess = innerIter.next().getValue();
                    synchronized (sess) {
                        if (sess.isTimedOut()) {
                            final EReviewExamState state = sess.getState();

                            if (state == EReviewExamState.ITEM_NN
                                    || state == EReviewExamState.SUBMIT_NN) {
                                Log.info("Review exam was started but abandoned - auto-submit it");

                                try {
                                    sess.forceSubmit(cache, null);
                                } catch (final SQLException ex) {
                                    Log.warning(ex);
                                }
                            } else {
                                Log.info("Purging expired HTML review exam session "
                                        + sess.sessionId);
                            }
                            innerIter.remove();
                        }
                    }
                }

                if (inner.isEmpty()) {
                    outerIter.remove();
                }
            }
        }
    }
}
