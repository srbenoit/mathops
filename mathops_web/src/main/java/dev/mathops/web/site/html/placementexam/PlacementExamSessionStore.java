package dev.mathops.web.site.html.placementexam;

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
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.WebSiteProfile;

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
public final class PlacementExamSessionStore {

    /** The filename to which to persist sessions. */
    private static final String PERSIST_FILENAME = "placement_exam_sessions.xml";

    /** The singleton instance. */
    private static final PlacementExamSessionStore INSTANCE = new PlacementExamSessionStore();

    /** Map from student ID to exam session (one placement session active per student at a time). */
    private final Map<String, PlacementExamSession> studentPlacementExams;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private PlacementExamSessionStore() {

        this.studentPlacementExams = new HashMap<>(100);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static PlacementExamSessionStore getInstance() {

        return INSTANCE;
    }

    /**
     * Retrieves the map of active placement exam sessions. This is the live map which can be manipulated.
     *
     * <p>
     * Any changes to the returned may MUST be done within a block synchronized on the map.
     *
     * @return the placement exam session map (key is student ID)
     */
    public Map<String, PlacementExamSession> getPlacementExamSessions() {

        return this.studentPlacementExams;
    }

    /**
     * Gets the active placement exam session for a student, if any.
     *
     * @param studentId the student ID
     * @return the placement exam session; {@code null} if none
     */
    public PlacementExamSession getPlacementExamSessionForStudent(final String studentId) {

        synchronized (this.studentPlacementExams) {
            return this.studentPlacementExams.get(studentId);
        }
    }

    /**
     * Sets the active placement exam session for an exam ID.
     *
     * @param theSession the session
     */
    public void setPlacementExamSession(final PlacementExamSession theSession) {

        synchronized (this.studentPlacementExams) {
            if (!theSession.isTimedOut()
                    && !this.studentPlacementExams.containsKey(theSession.studentId)) {
                this.studentPlacementExams.put(theSession.studentId, theSession);
            }
        }
    }

    /**
     * Removes the placement exam session for a student.
     *
     * @param studentId the student ID
     */
    void removePlacementExamSessionForStudent(final String studentId) {

        synchronized (this.studentPlacementExams) {
            this.studentPlacementExams.remove(studentId);
        }
    }

    /**
     * Persists the session store on server shutdown. The persisted session states can be restored on server restart.
     *
     * @param dir the directory in which to persist the active sessions
     */
    public void persist(final File dir) {

        synchronized (this.studentPlacementExams) {
            final HtmlBuilder xml = new HtmlBuilder(1000);
            try {
                Log.info("Placement exam session store persisting to " + dir.getAbsolutePath());

                for (final PlacementExamSession session : this.studentPlacementExams.values()) {
                    if (session.isTimedOut()) {
                        Log.info("  Skipping timed out session ", session.sessionId);
                        continue;
                    }
                    Log.info("  Appending session ", session.sessionId);
                    session.appendXml(xml);
                }

                Log.info("Placement exam session store generated XML");
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

        synchronized (this.studentPlacementExams) {
            final File target = new File(dir, PERSIST_FILENAME);

            Log.info("Restoring placement exam sessions from ",
                    target.getAbsolutePath());

            if (target.exists()) {
                final String xml = FileLoader.loadFileAsString(target, true);

                try {
                    final XmlContent content = new XmlContent(xml, false, false);
                    final List<INode> nodes = content.getNodes();

                    if (nodes != null) {
                        for (final INode node : nodes) {
                            if (node instanceof NonemptyElement) {
                                try {
                                    setPlacementExamSession(parseSession(cache, xml, (NonemptyElement) node));
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
     * Parses a placement exam session from its XML element.
     *
     * @param cache the data cache
     * @param xml   the source XML
     * @param elem  the XML element
     * @return the parsed session
     * @throws IllegalArgumentException if the XML could not be parsed
     * @throws SQLException             if there is an error accessing the database
     */
    private static PlacementExamSession parseSession(final Cache cache, final String xml,
                                                     final NonemptyElement elem) throws IllegalArgumentException,
            SQLException {

        String host = null;
        String path = null;
        String session = null;
        String student = null;
        String proctored = null;
        String assign = null;
        String score = null;
        String mastery = null;
        String state = null;
        String profile = null;
        String sect = null;
        String item = null;
        String error = null;
        boolean started = false;
        String redirect = null;
        String timeout = null;
        String purge = null;
        ExamObj exam = null;

        final String tagName = elem.getTagName();

        if ("placement-exam-session".equals(tagName)) {
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
                        } else if ("proctored".equals(tag)) {
                            proctored = content;
                        } else if ("exam-id".equals(tag)) {
                            assign = content;
                        } else if ("score".equals(tag)) {
                            score = content;
                        } else if ("mastery".equals(tag)) {
                            mastery = content;
                        } else if ("state".equals(tag)) {
                            state = content;
                        } else if ("profile".equals(tag)) {
                            profile = content;
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
                                    throw new IllegalArgumentException( "Unable to parse possible problem", ex);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Expected 'placement-exam-session', found '"
                    + tagName + "'");
        }

        if (host == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'host'");
        }
        if (path == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'path'");
        }
        if (session == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'session'");
        }
        if (student == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'student'");
        }
        if (proctored == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'proctored'");
        }
        if (assign == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'assign-id'");
        }
        if (state == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'state'");
        }
        if (redirect == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'redirect'");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'timeout'");
        }
        if (purge == null) {
            // throw new IllegalArgumentException("'placement-exam-session' was missing 'purge'");
            purge = Long.toString(Long.parseLong(timeout) + 600000L);
        }
        if (exam == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'exam'");
        }
        if (profile == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'profile'");
        }
        if (sect == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'sect'");
        }
        if (item == null) {
            throw new IllegalArgumentException("'placement-exam-session' was missing 'item'");
        }

        final WebSiteProfile siteProfile =
                ContextMap.getDefaultInstance().getWebSiteProfile(host, path);
        final Integer scoreInt = score == null ? null : Integer.valueOf(score);
        final Integer minMastery = mastery == null ? null : Integer.valueOf(mastery);

        final PlacementExamSession sess = new PlacementExamSession(cache, siteProfile, session,
                student, Boolean.parseBoolean(proctored), assign, redirect,
                EPlacementExamState.valueOf(state), scoreInt, minMastery, started,
                Integer.parseInt(profile), Integer.parseInt(sect), Integer.parseInt(item),
                Long.parseLong(timeout), Long.parseLong(purge), exam, error);

        Log.info("Restoring placement exam session for ", student,
                CoreConstants.SLASH, assign, CoreConstants.SLASH, state);

        return sess;
    }

    /**
     * Purges all expired sessions from the store. Called periodically from the servlet container.
     *
     * @param cache the data cache
     */
    public void purgeExpired(final Cache cache) {

        synchronized (this.studentPlacementExams) {
            final Iterator<Map.Entry<String, PlacementExamSession>> iter =
                    this.studentPlacementExams.entrySet().iterator();

            while (iter.hasNext()) {
                final PlacementExamSession sess = iter.next().getValue();

                if (sess.isPurgable()) {
                    try {
                        if (sess.getState() == EPlacementExamState.ITEM_NN
                                || sess.getState() == EPlacementExamState.SUBMIT_NN) {
                            // Force-submit
                            sess.scoreAndRecordCompletion(cache, ZonedDateTime.now());
                        } else {
                            sess.writeExamRecovery(cache);
                        }
                    } catch (final SQLException ex) {
                        Log.warning(ex);
                    }

                    Log.info("Purging expired HTML placement exam session " + sess.sessionId);
                    iter.remove();
                }
            }
        }
    }
}
