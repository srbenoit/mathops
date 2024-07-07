package dev.mathops.assessment;

import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemDummyTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.db.Contexts;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A singleton class that processes requests for instructional materials (exams, problems and homework assignments), and
 * caches them for later retrievals. A background thread scans a directory and caches any exam, problem and homework
 * files found, and periodically rescans that directory for files that have changed since the last scan.
 */
public final class InstructionalCache implements InstructionalCacheInt {

    /** The singleton instance. */
    private static InstructionalCache instance;

    /** Object on which to synchronize member variable access. */
    private final Object synch;

    /** The file under which to search for instructional data files. */
    private final File base;

    /** A cache of exam objects. */
    private final Map<String, String> examFiles;

    /** A cache of problem objects. */
    private final Map<String, String> problemFiles;

    /** A cache of exam objects. */
    private final Map<String, ExamObj> examCache;

    /** A cache of problem objects. */
    private final Map<String, AbstractProblemTemplate> problemCache;

    /** A cache of timestamps on files that have been loaded. */
    private final Map<String, Long> fileTimestamps;

    /**
     * Constructs a new {@code InstructionalCache}.
     *
     * @param theBase the file under which to search for instructional data files
     */
    private InstructionalCache(final File theBase) {

        this.base = theBase;
        this.synch = new Object();

        this.examFiles = new HashMap<>(100);
        this.problemFiles = new HashMap<>(500);

        this.examCache = new HashMap<>(100);
        this.problemCache = new HashMap<>(500);
        this.fileTimestamps = new HashMap<>(600);
    }

    /**
     * Get an instance of the {@code InstructionalCache} that reads from a specified directory.
     *
     * @param instruction the instruction directory
     * @return the instance
     */
    public static InstructionalCache getInstance(final File instruction) {

        return new InstructionalCache(instruction);
    }

    /**
     * Get the singleton instance of the {@code InstructionalCache}.
     *
     * @return the instance
     */
    public static InstructionalCache getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (instance == null) {
                final PathList paths = PathList.getInstance();
                File file = paths.get(EPath.CUR_DATA_PATH);

                if (!file.exists()) {
                    file = paths.get(EPath.SOURCE_1_PATH);

                    if (!file.exists()) {
                        file = paths.get(EPath.SOURCE_2_PATH);

                        if (!file.exists()) {
                            file = paths.get(EPath.SOURCE_3_PATH);
                        }
                    }
                }

                file = new File(file, "instruction");
                Log.info("Instructional cache reading from ", file.getAbsolutePath());

                instance = new InstructionalCache(file);
//                instance.rescan();
            }

            return instance;
        }
    }

    /**
     * Retrieves the exam based on a {@code Reference}.
     *
     * @param ref the {@code Reference} to the exam to load
     * @return the loaded {@code ExamObj} object on success, or {@code null}
     */
    public static ExamObj getExam(final String ref) {

        final ExamObj exam = getInstance().retrieveExam(ref);

        return exam == null ? null : exam.deepCopy();
    }

    /**
     * Gets the list of scanned exam references.
     *
     * @return the exam references
     */
    public List<String> getExamFileRefs() {

        return new ArrayList<>(this.examFiles.keySet());
    }

    /**
     * Retrieves the exam based on a {@code Reference}.
     *
     * @param ref the reference of the exam to load
     * @return the loaded {@code ExamObj} object on success, or an empty {@code ExamObj} object containing the set of
     *         errors encountered on failure
     */
    private ExamObj retrieveExam(final String ref) {

        synchronized (this.synch) {
            ExamObj exam = this.examCache.get(ref);

            if (exam == null) {
                final File file = FactoryBase.getRefSourceFile(this.base, ref);

                final String xml = FileLoader.loadFileAsString(file, true);
                if (xml != null) {
                    final String path = file.getAbsolutePath();

                    try {
                        final XmlContent content = new XmlContent(xml, false, false);
                        exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                        if (exam == null || exam.ref == null) {
                            Log.warning("FAILED TO LOAD exam", ref);
                        } else {
                            final String test;

                            if (path.indexOf('\\') == -1) {
                                test = path.replace(Contexts.ROOT_PATH, CoreConstants.DOT);
                            } else {
                                test = path.replace("\\", CoreConstants.DOT);
                            }

                            if (!test.endsWith(exam.ref + ".xml")) {
                                Log.warning("Origin: ", test, ", ref: ", exam.ref);
                            }

                            Log.info("Caching exam ", exam.ref);

                            this.examCache.put(ref, exam);
                            this.fileTimestamps.put(path, Long.valueOf(file.lastModified()));
                        }
                    } catch (final ParsingException ex) {
                        Log.warning("Failed to parse exam from ", path, ex);
                    }
                }
            }

            if (exam != null) {
                // Now, load the exam with its problems, so it can be realized
                final int numSect = exam.getNumSections();

                for (int i = 0; i < numSect; i++) {
                    final ExamSection esect = exam.getSection(i);

                    final int numProb = esect.getNumProblems();

                    for (int j = 0; j < numProb; j++) {
                        final ExamProblem eprob = esect.getProblem(j);

                        final int count = eprob.getNumProblems();

                        for (int k = 0; k < count; k++) {
                            final String pref = eprob.getProblem(k).id;

                            if (pref == null) {
                                Log.warning("Preferred problem (" + k + " of " + count + ") is null");
                                throw new IllegalArgumentException("The " + (k + 1) + "th choice for problem "
                                        + (j + 1) + " on section " + (i + 1) + " of exam " + exam.examVersion
                                        + " had a null identifier reference");
                            }

                            final String actualRef;
                            if (pref.startsWith(exam.refRoot + ".")) {
                                actualRef = pref;
                            } else {
                                actualRef = exam.refRoot + "." + pref;
                            }

                            final AbstractProblemTemplate prob = retrieveProblem(actualRef);

                            if (prob == null) {
                                Log.warning("Exam ", ref, " problem ",
                                        pref, " not found");
                            } else {
                                eprob.setProblem(k, prob);
                            }
                        }
                    }
                }
            }

            return exam;
        }
    }

    /**
     * Retrieves a problem based on a {@code Reference}.
     *
     * @param ref the {@code Reference} to the exam to load
     * @return the loaded {@code Problem} object on success, or {@code null} on failure
     */
    public AbstractProblemTemplate retrieveProblem(final String ref) {

         Log.info("Retrieve problem: ", ref);

        synchronized (this.synch) {
            AbstractProblemTemplate problem;

            if (ref == null) {
                Log.warning("Reference with null ref");
                problem = null;
            } else {
                problem = this.problemCache.get(ref);

                if (problem == null) {
                    final File file = FactoryBase.getRefSourceFile(this.base, ref);

                    final String xml = FileLoader.loadFileAsString(file, true);
                    if (xml != null) {
                        final String path = file.getAbsolutePath();

                        try {
                            final XmlContent content = new XmlContent(xml, false, false);
                            problem = ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                            if (problem.id == null) {
                                Log.warning("No ref in retrieved problem ", ref);
                            } else {
                                Log.info("Caching problem ", problem.id);
                                this.problemCache.put(ref, problem);
                                this.fileTimestamps.put(path, Long.valueOf(file.lastModified()));
                            }
                        } catch (final ParsingException ex) {
                            Log.warning("Failed to parse problem from ", path, ex);
                        }
                    }
                }
            }

            return problem;
        }
    }

    /**
     * Gets the list of scanned problem references.
     *
     * @return the problem references
     */
    public List<String> getProblemFileRefs() {

        return new ArrayList<>(this.problemFiles.keySet());
    }
    /**
     * Retrieves a problem based on a {@code Reference}.
     *
     * @param ref the {@code Reference} to the problem to load
     * @return the loaded {@code Problem}
     */
    public static AbstractProblemTemplate getProblem(final String ref) {

        final AbstractProblemTemplate prob = getInstance().retrieveProblem(ref);
        if (prob == null) {
            Log.warning("No problem ", ref);
        }
        return prob == null ? null : prob.deepCopy();
    }

    /**
     * Retrieves a problem based on a {@code Reference}.
     *
     * @param ref the {@code Reference} to the problem to load
     * @return the loaded {@code Problem}
     */
    public static File getProblemSource(final String ref) {

        return FactoryBase.getRefSourceFile(getInstance().base, ref);
    }

    /**
     * Forgets a problem, so it will be reloaded from source.
     *
     * @param ref the {@code Reference} to the problem to forget
     */
    public void forgetProblem(final String ref) {

        this.problemCache.remove(ref);
    }

    /**
     * Retrieves a problem based on a {@code Reference}.
     *
     * @param baseDir the directory under which to search for the problem
     * @param ref     the {@code Reference} to the problem to load
     * @return the loaded {@code Problem} object on success, or an empty {@code Problem} object containing the set of
     *         errors encountered on failure
     */
    public static AbstractProblemTemplate getProblem(final File baseDir, final String ref) {

        AbstractProblemTemplate problem = null;

        final File file = FactoryBase.getRefSourceFile(baseDir, ref);

        final String xml = FileLoader.loadFileAsString(file, true);
        if (xml != null) {
            final String path = file.getAbsolutePath();

            try {
                final XmlContent content = new XmlContent(xml, false, false);
                problem = ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse problem from ", path, ex);
            }
        }

        if (problem == null) {
            problem = new ProblemDummyTemplate();
        }

        return problem;
    }

    /**
     * Indicates that the cache should rescan its data directory for updated files.
     */
    @Override
    public void rescan() {

        synchronized (this.synch) {
            Log.info("InstructionalCache Scan Scanning " + this.base.getAbsolutePath());

            scan(this.base);

            Log.info("InstructionalCache Scan Complete " + this.examFiles.size() + " exams and "
                    + this.problemFiles.size() + " problems");
        }
    }

    /**
     * Scans a directory recursively, descending into subdirectories and testing each XML file we find.
     *
     * @param dir the directory to scan
     */
    private void scan(final File dir) {

        final String path = dir.getAbsolutePath();
        final File[] list = dir.listFiles();

        if (list != null) {
            Arrays.sort(list);

            for (final File file : list) {
                if (file.isDirectory()) {
                    scan(file);
                }

                if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {
                    boolean changed = true;

                    synchronized (this.fileTimestamps) {
                        final Long time = this.fileTimestamps.get(file.getAbsolutePath());

                        if (time != null) {
                            if (time.longValue() == file.lastModified()) {
                                changed = false;
                            } else {
                                Log.info(file.getAbsolutePath(), " had timestamp ", time, " and now has timestamp ",
                                        Long.toString(file.lastModified()));
                            }
                        }
                    }

                    if (path.contains("/exams/") || path.endsWith("/exams") || path.contains("\\exams\\") ||
                            path.endsWith("\\exams") || path.contains("/homework/") || path.endsWith("/homework") ||
                            path.contains("\\homework\\") || path.endsWith("\\homework")) {

                        if (changed) {
                            processExam(file);
                        }
                    } else if (path.contains("/problems/") || path.endsWith("/problems") ||
                            path.contains("\\problems\\") || path.endsWith("\\problems") ||
                            path.contains("m130problems")) {

                        if (changed) {
                            processProblem(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes a single exam file.
     *
     * @param f the file
     */
    private void processExam(final File f) {

        final String xml = FileLoader.loadFileAsString(f, true);
        if (xml != null) {
            final String path = f.getAbsolutePath();

            try {
                final XmlContent content = new XmlContent(xml, false, false);
                final ExamObj exam = ExamFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                if (exam.ref == null) {
                    Log.warning("Failed to load exam: " + path);
                } else {
                    final String test;

                    if (path.indexOf('\\') == -1) {
                        test = path.replace(Contexts.ROOT_PATH, CoreConstants.DOT);
                    } else {
                        test = path.replace("\\", CoreConstants.DOT);
                    }

                    if (!test.endsWith(exam.ref + ".xml")) {
                        Log.warning("Origin: ", test, ", ref-base: ", exam.ref);
                    }

                    synchronized (this.fileTimestamps) {

                        if (this.examCache.get(exam.ref) != null) {
                            Log.warning("Scanning ", path, " Duplicate exam identifier: ",
                                    this.examCache.get(exam.ref).ref);
                        }

                        final String existing = this.examFiles.get(exam.ref);
                        if (existing == null) {
                            this.examFiles.put(exam.ref, path);
                        } else if (!existing.equals(path)) {
                            Log.warning("Same exam reference path in ", existing, " and ", path);
                        }

                        this.examCache.put(exam.ref, exam);
                        this.fileTimestamps.put(path, Long.valueOf(f.lastModified()));
                    }
                }
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse exam from ", path, ex);
            }
        }
    }

    /**
     * Processes a single problem file.
     *
     * @param f the file
     */
    private void processProblem(final File f) {

        final String xml = FileLoader.loadFileAsString(f, true);
        if (xml != null) {
            final String path = f.getAbsolutePath();

            try {
                final XmlContent content = new XmlContent(xml, false, false);
                final AbstractProblemTemplate prob =
                        ProblemTemplateFactory.load(content, EParserMode.ALLOW_DEPRECATED);

                if (prob.id == null) {
                    Log.warning("Failed to load problem: " + path);

                } else {
                    synchronized (this.fileTimestamps) {
                        if (this.problemCache.get(prob.id) != null) {
                            Log.warning("Scanning ", path, " Duplicate problem identifier: ",
                                    this.problemCache.get(prob.id).id);
                        }

                        final String existing = this.problemFiles.get(prob.id);
                        if (existing == null) {
                            Log.info("Scanned ", path);
                            this.problemFiles.put(prob.id, path);
                        } else if (!existing.equals(path)) {
                            Log.warning("Same problem reference path in ", existing, " and ", path);
                        }

                        this.problemCache.put(prob.id, prob);
                        this.fileTimestamps.put(path, Long.valueOf(f.lastModified()));
                    }
                }
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse exam from ", path, ex);
            }
        }
    }

    /**
     * Main method for testing.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final DecimalFormat df = new DecimalFormat("###,###");
        final InstructionalCache cache = getInstance();

        final Runtime rt = Runtime.getRuntime();

        final long start = rt.totalMemory() - rt.freeMemory();

        cache.rescan();

        final long end = rt.totalMemory() - rt.freeMemory();

        Log.info("Detected that first scan is complete... (", df.format(end - start), ")");

        // for (String ref : cache.getExamFileRefs()) {
        // Log.info("Exam file at: ", ref);
        // }
        //
        // for (String ref : cache.getProblemFileRefs()) {
        // Log.info("Problem file at: ", ref);
        // }
    }
}
