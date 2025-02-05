package dev.mathops.web.site.html.challengeexam;

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
 * A singleton storage class that keeps a map from session ID to a map from exam ID to unit exam session.
 */
public final class ChallengeExamSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "challenge_exam_sessions.xml";

    /** The singleton instance. */
    private static final ChallengeExamSessionStore INSTANCE = new ChallengeExamSessionStore();

    /** Map from student ID to exam session (one challenge session active per student at a time). */
    private final Map<String, ChallengeExamSession> studentChallengeExams;

    /** Map from student ID to one-time challenge code. */
    private final Map<String, String> oneTimeChallengeCodes;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ChallengeExamSessionStore() {

        this.studentChallengeExams = new HashMap<>(100);
        this.oneTimeChallengeCodes = new HashMap<>(10);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static ChallengeExamSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Gets the active challenge exam session for a student, if any.
     *
     * @param studentId the student ID
     * @return the challenge exam session; {@code null} if none
     */
    public ChallengeExamSession getChallengeExamSessionForStudent(final String studentId) {

        // Called only from the LTI page where a student takes a challenge exam via Teams

        synchronized (this.studentChallengeExams) {
            return this.studentChallengeExams.get(studentId);
        }
    }

    /**
     * Sets the active challenge exam session for an exam ID.
     *
     * @param theSession the session
     * @return {@code true} if the session was installed; {@code false} if installing the session would mean there were
     *         two active sessions for the same student (or the session being installed has timed out and was ignored)
     */
    public boolean setChallengeExamSession(final ChallengeExamSession theSession) {

        boolean result = false;

        // Called only from the LTI page where a student takes a challenge exam via Teams, and from
        // the challenge exam session store when restoring after a restart

        synchronized (this.studentChallengeExams) {
            if (!theSession.isTimedOut()
                && !this.studentChallengeExams.containsKey(theSession.studentId)) {
                this.studentChallengeExams.put(theSession.studentId, theSession);
                result = true;
            }
        }

        return result;
    }

    /**
     * Removes the challenge exam session for a student.
     *
     * @param studentId the student ID
     */
    void removeChallengeExamSessionForStudent(final String studentId) {

        // Called by the challenge exam session when the session ends

        synchronized (this.studentChallengeExams) {
            this.studentChallengeExams.remove(studentId);
        }
    }

    /**
     * Persists the session store on server shutdown. The persisted session states can be restored on server restart.
     *
     * @param dir the directory in which to persist the active sessions
     */
    public void persist(final File dir) {

        // Called by the front controller when the app server is being shut down

        synchronized (this.studentChallengeExams) {
            final HtmlBuilder xml = new HtmlBuilder(1000);
            try {
                Log.info("Challenge exam session store persisting to " + dir.getAbsolutePath());

                for (final ChallengeExamSession session : this.studentChallengeExams.values()) {
                    if (session.isTimedOut()) {
                        Log.info("  Skipping timed out session ", session.sessionId);
                        continue;
                    }
                    Log.info("  Appending session ", session.sessionId);
                    session.appendXml(xml);
                }

                Log.info("Challenge exam session store generated XML");
            } catch (final Exception ex) {
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

        // Called by the web mid-controller when the app server is being started up

        synchronized (this.studentChallengeExams) {
            final File target = new File(dir, PERSIST_FILENAME);

            Log.info("Restoring challenge exam sessions from ", target.getAbsolutePath());

            if (target.exists()) {
                final String xml = FileLoader.loadFileAsString(target, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setChallengeExamSession(parseSession(cache, xml, (NonemptyElement) node));
                                } catch (final IllegalArgumentException ex) {
                                    Log.warning(ex);
                                }
                            } else if (node instanceof CData) {
                                final String data = ((CData) node).content.trim();
                                if (!data.isEmpty()) {
                                    Log.warning("  Node is CData with content: ", data);
                                }
                            } else {
                                Log.warning("  Node is ", node.getClass().getName());
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
     * Parses a challenge exam session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static ChallengeExamSession parseSession(final Cache cache, final String xml,
                                                     final NonemptyElement elem) throws IllegalArgumentException,
            SQLException {

        String host = null;
        String path = null;
        String session = null;
        String student = null;
        String assign = null;
        String score = null;
        String state = null;
        String sect = null;
        String item = null;
        String error = null;
        boolean started = false;
        String redirect = null;
        String timeout = null;
        String purge = null;
        ExamObj exam = null;

        if ("challenge-exam-session".equals(elem.getTagName())) {
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
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("cur-sect".equals(tag)) {
                            sect = content;
                        } else if ("cur-item".equals(tag)) {
                            item = content;
                        } else if ("error".equals(tag)) {
                            error = content;
                        } else if ("timeout".equals(tag)) {
                            timeout = content;
                        } else if ("purge".equals(tag)) {
                            purge = content;
                        } else if ("redirect".equals(tag)) {
                            redirect = XmlEscaper.unescape(xml.substring(child.getTagSpan().getEnd(),
                                    child.getClosingTagSpan().getStart()));
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
                            throw new IllegalArgumentException(//
                                    "Invalid section number in problem: "
                                    + sectAttr.value);
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
                            while (!problems.isEmpty()) {
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
            throw new IllegalArgumentException("Expected 'challenge-exam-session', found '" + elem.getTagName() + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'session'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'student'");
        }
        if (assign == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'assign-id'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'timeout'");
        }
        if (purge == null) {
            // throw new IllegalArgumentException("'challenge-exam-session' was missing 'purge'");
            purge = Long.toString(Long.parseLong(timeout) + 600000L);
        }
        if (exam == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'exam'");
        }
        if (sect == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'sect'");
        }
        if (item == null) {
            throw new IllegalArgumentException("'challenge-exam-session' was missing 'item'");
        }

        final Site siteProfile = DatabaseConfig.getDefault().getSite(host, path);
        final Integer scoreInt = score == null ? null : Integer.valueOf(score);

        final ChallengeExamSession sess = new ChallengeExamSession(cache, siteProfile, session, student, assign,
                redirect, EChallengeExamState.valueOf(state), scoreInt, started, Integer.parseInt(sect),
                Integer.parseInt(item), Long.parseLong(timeout), Long.parseLong(purge), exam, error);

        Log.info("Restoring challenge exam session for ", student, CoreConstants.SLASH, assign, CoreConstants.SLASH,
                state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     *
     * @param cache the data cache
     */
    public void purgeExpired(final Cache cache) {

        // Called by the web mid-controller every 10 minutes

        synchronized (this.studentChallengeExams) {
            final Iterator<Map.Entry<String, ChallengeExamSession>> iterator =
                    this.studentChallengeExams.entrySet().iterator();

            while (iterator.hasNext()) {
                final ChallengeExamSession session = iterator.next().getValue();

                if (session.isPurgable()) {
                    Log.info("Purging expired HTML challenge exam session " + session.sessionId);
                    try {
                        if (session.getState() == EChallengeExamState.ITEM_NN
                            || session.getState() == EChallengeExamState.SUBMIT_NN) {
                            // Force-submit
                            session.scoreAndRecordCompletion(cache);
                        } else {
                            session.writeExamRecovery(cache);
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                    }

                    iterator.remove();
                }
            }
        }
    }

    /**
     * Creates and stores the one-time code to run a challenge exam for a student.
     *
     * @param studentId the student ID
     * @return the code
     */
    public String createOneTimeChallengeCode(final String studentId) {

        // Called from the pages in the Office and Proctor sections of the admin website when
        // starting a Teams-based challenge exam

        final String code = CoreConstants.newId(20);

        this.oneTimeChallengeCodes.put(studentId, code);

        return code;
    }

    /**
     * Validates a one-time challenge code, but leaves it in the set of valid codes.
     *
     * @param studentId the student ID
     * @param code      the code to verify
     * @return true if the code was verified (and deleted); false if the code was not verified (in which case no codes
     *         are deleted, since this could have been a typo and the proctor will try again)
     */
    public boolean validateOneTimeChallengeCodeNoDelete(final String studentId, final String code) {

        // Called from the web pages that run a challenge exam

        final String existing = this.oneTimeChallengeCodes.get(studentId);
        final boolean valid;

        if (existing == null) {
            valid = false;
        } else {
            valid = existing.equals(code);
        }

        return valid;
    }

    /**
     * Validates a one-time challenge code and deletes it from the set of valid codes (used when the exam actually
     * starts).
     *
     * @param studentId the student ID
     * @param code      the code to verify
     * @return true if the code was verified (and deleted); false if the code was not verified (in which case no codes
     *         are deleted, since this could have been a typo and the proctor will try again)
     */
    public boolean validateAndDeleteOneTimeChallengeCode(final String studentId, final String code) {

        // Called from the web pages that run a challenge exam

        final boolean valid = validateOneTimeChallengeCodeNoDelete(studentId, code);

        if (valid) {
            this.oneTimeChallengeCodes.remove(studentId);
        }

        return valid;
    }
}
