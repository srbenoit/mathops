package dev.mathops.web.site.html.unitexam;

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
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A singleton storage class that keeps a map from session ID to a map from exam ID to unit exam session.
 */
public final class UnitExamSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "unit_exam_sessions.xml";

    /** The singleton instance. */
    private static final UnitExamSessionStore INSTANCE = new UnitExamSessionStore();

    /** Map from session ID to that session's map from exam ID to exam session. */
    private final Map<String, Map<String, UnitExamSession>> unitExamSessions;

    /** Map from exam code to the corresponding session ID. */
    private final Map<String, String> examCodeToSessionId;

    /** Map from student ID to the corresponding session ID. */
    private final Map<String, String> studentIdToSessionId;

    /** Map from session ID to the corresponding exam code. */
    private final Map<String, String> sessionIdToExamCode;

    /** Last timestamp when exam codes were tested for expiry. */
    private long nextTimeoutScan;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private UnitExamSessionStore() {

        this.unitExamSessions = new HashMap<>(100);

        this.studentIdToSessionId = new HashMap<>(200);
        this.examCodeToSessionId = new HashMap<>(200);
        this.sessionIdToExamCode = new HashMap<>(200);
        this.nextTimeoutScan = 0L;
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static UnitExamSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Generates a new exam code for a session and associates it with the session.
     *
     * @param session the session
     * @return the exam code
     */
    public String makeExamCode(final ImmutableSessionInfo session) {

        final String sessionId = session.loginSessionId;

        String examCode;

        synchronized (this.examCodeToSessionId) {

            // See if there is already a code, use it if found
            examCode = this.sessionIdToExamCode.get(sessionId);

            if (examCode == null) {
                // Needs a new code, take opportunity to clean expired sessions if needed
                if (System.currentTimeMillis() > this.nextTimeoutScan) {
                    final SessionManager sm = SessionManager.getInstance();

                    final Iterator<Map.Entry<String, String>> iter = this.examCodeToSessionId.entrySet().iterator();

                    while (iter.hasNext()) {
                        final Map.Entry<String, String> entry = iter.next();
                        final String testId = entry.getValue();

                        if (sm.getUserSession(testId) == null) {
                            iter.remove();
                            this.sessionIdToExamCode.remove(testId);
                        }
                    }
                    this.nextTimeoutScan = System.currentTimeMillis() + 120000L;
                }

                // Generate and store the new exam code
                do {
                    examCode = CoreConstants.newId(6);
                } while (this.examCodeToSessionId.containsKey(examCode));
                Log.info("Storing exam code '", examCode, "' for session ", sessionId);
                this.examCodeToSessionId.put(examCode, sessionId);
                this.sessionIdToExamCode.put(sessionId, examCode);
                this.studentIdToSessionId.put(session.getEffectiveUserId(), sessionId);
            }
        }

        return examCode;
    }

    ///**
    // * Looks up an exam code, returning the associated login session ID if found.
    // *
    // * @param code the exam code
    // * @return the login session ID
    // */
    // public String lookupExamCode(final String code) {
    //
    // synchronized (this.examCodeToSessionId) {
    // final String sid = this.examCodeToSessionId.get(code);
    // Log.info("Looking up exam code '", code,
    // "' returned ", sid);
    // return sid;
    // }
    // }

    /**
     * Looks up a student ID, returning the associated login session ID if found.
     *
     * @param studentId the student ID
     * @return the login session ID
     */
    public String lookupStudent(final String studentId) {

        synchronized (this.studentIdToSessionId) {
            final String sid = this.studentIdToSessionId.get(studentId);
            Log.info("Looking up student '", studentId, "' returned ", sid);
            return sid;
        }
    }

    /**
     * Retrieves the map of active unit exam sessions. This is the live map which can be manipulated.
     *
     * <p>
     * Any changes to the returned may MUST be done within a block synchronized on the map.
     *
     * @return the unit exam session map (outer key is session ID, inner key is exam ID)
     */
    public Map<String, Map<String, UnitExamSession>> getUnitExamSessions() {

        return this.unitExamSessions;
    }

    /**
     * Gets the active unit exam session for an exam ID, if any.
     *
     * @param sessionId the session ID
     * @param examId    the exam ID
     * @return the unit exam session; {@code null} if none
     */
    public UnitExamSession getUnitExamSession(final String sessionId, final String examId) {

        synchronized (this.unitExamSessions) {
            final Map<String, UnitExamSession> sessionMap = this.unitExamSessions.get(sessionId);

            // if (sessionMap != null) {
            // Log.info("Query - session map for ", sessionId,
            // " found, testing for ", examId);
            // }

            return sessionMap == null ? null : sessionMap.get(examId);
        }
    }

    /**
     * Sets the active unit exam session for an exam ID.
     *
     * @param theSession the session
     */
    public void setUnitExamSession(final UnitExamSession theSession) {

        synchronized (this.unitExamSessions) {
            if (!theSession.isTimedOut()) {
                final Map<String, UnitExamSession> sessionMap =
                        this.unitExamSessions.computeIfAbsent(theSession.sessionId, s -> new HashMap<>(2));
                sessionMap.put(theSession.version, theSession);
            }
        }
    }

    /**
     * Removes the active unit exam session for an assignment ID.
     *
     * @param sessionId the session ID
     * @param examId    the exam ID
     */
    void removeUnitExamSession(final String sessionId, final String examId) {

        synchronized (this.unitExamSessions) {
            final Map<String, UnitExamSession> sessionMap = this.unitExamSessions.get(sessionId);
            if (sessionMap != null) {
                sessionMap.remove(examId);
                if (sessionMap.isEmpty()) {
                    this.unitExamSessions.remove(sessionId);
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

        synchronized (this.unitExamSessions) {
            final HtmlBuilder xml = new HtmlBuilder(1000);
            try {
                Log.info("Unit exam session store persisting to " + dir.getAbsolutePath());

                for (final Map<String, UnitExamSession> maps : this.unitExamSessions.values()) {
                    for (final UnitExamSession session : maps.values()) {
                        if (session.isTimedOut()) {
                            Log.info("  Skipping timed out session ", session.sessionId);
                            continue;
                        }
                        Log.info("  Appending session ", session.sessionId);
                        session.appendXml(xml);
                    }
                }

                Log.info("Unit exam session store generated XML");
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

        synchronized (this.unitExamSessions) {
            final File target = new File(dir, PERSIST_FILENAME);

            Log.info("Restoring unit exam sessions from ", target.getAbsolutePath());

            if (target.exists()) {
                final String xml = FileLoader.loadFileAsString(target, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setUnitExamSession(parseSession(cache, xml, (NonemptyElement) node));
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
     * Parses a unit exam session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static UnitExamSession parseSession(final Cache cache, final String xml, final NonemptyElement elem)
            throws IllegalArgumentException, SQLException {

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
        boolean started = false;
        String redirect = null;
        String timeout = null;
        String startInstructions = null;
        ExamObj exam = null;

        if ("unit-exam-session".equals(elem.getTagName())) {
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
                        } else if ("startInstructions".equals(tag)) {
                            startInstructions = content;
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
            throw new IllegalArgumentException("Expected 'unit-exam-session', found '" + elem.getTagName() + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'session'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'student'");
        }
        if (assign == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'assign-id'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'timeout'");
        }
        if (startInstructions == null) {
            Log.warning("'unit-exam-session' was missing 'startInstructions'");
            startInstructions = "0";
        }
        if (exam == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'exam'");
        }
        if (item == null) {
            throw new IllegalArgumentException("'unit-exam-session' was missing 'item'");
        }

        final Site siteProfile = DatabaseConfig.getDefault().getSite(host, path);

        final Integer scoreInt = score == null ? null : Integer.valueOf(score);
        final Integer minMastery = mastery == null ? null : Integer.valueOf(mastery);

        final UnitExamSession sess = new UnitExamSession(cache, siteProfile, session, student, assign, redirect,
                EUnitExamState.valueOf(state), scoreInt, minMastery, started, Integer.parseInt(item),
                Long.parseLong(timeout), Long.parseLong(startInstructions), exam, error);

        Log.info("Restoring unit exam session for ", student, CoreConstants.SLASH, assign, CoreConstants.SLASH, state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     *
     * @param cache the data cache
     */
    public void purgeExpired(final Cache cache) {

        synchronized (this.unitExamSessions) {
            final Iterator<Map.Entry<String, Map<String, UnitExamSession>>> outerIter =
                    this.unitExamSessions.entrySet().iterator();

            while (outerIter.hasNext()) {
                final Map<String, UnitExamSession> inner = outerIter.next().getValue();

                final Iterator<Map.Entry<String, UnitExamSession>> innerIter = inner.entrySet().iterator();

                while (innerIter.hasNext()) {
                    final UnitExamSession sess = innerIter.next().getValue();

                    if (sess.isPurgable()) {
                        try {
                            if (sess.getState() == EUnitExamState.ITEM_NN
                                || sess.getState() == EUnitExamState.SUBMIT_NN) {
                                // Force-submit
                                sess.scoreAndRecordCompletion(cache, ZonedDateTime.now());
                            } else {
                                sess.writeExamRecovery(cache);
                            }
                        } catch (final SQLException ex) {
                            Log.warning(ex);
                        }

                        Log.info("Purging expired HTML unit exam session " + sess.sessionId);
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
