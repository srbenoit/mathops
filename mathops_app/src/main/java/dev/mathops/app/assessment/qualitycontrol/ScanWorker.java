package dev.mathops.app.assessment.qualitycontrol;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.exam.ExamFactory;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemTemplateFactory;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.parser.xml.XmlContentError;

import javax.swing.SwingWorker;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The worker that perform the scan
 */
final class ScanWorker extends SwingWorker<String, ProgressUpdate> {

    /** The owning window to update with progress. */
    private final MainWindow owner;

    /** The parser mode./ */
    private final EParserMode parserMode;

    /**
     * Constructs a new {@code ScanWorker}.
     *
     * @param theOwner      the owning window to update with progress
     * @param theParserMode the parser mode
     */
    ScanWorker(final MainWindow theOwner, final EParserMode theParserMode) {
        super();

        this.owner = theOwner;
        this.parserMode = theParserMode;
    }

    /**
     * Performs the scan on a background thread.
     */
    @Override
    protected String doInBackground() {

        final HtmlBuilder report = new HtmlBuilder(1000);
        report.sH(2).add("Quality Control Report").eH(2);

        final File dir = this.owner.getLibraryDir();
        final String dirPath = dir.getAbsolutePath();
        report.add("Scanning <span style='color:blue;'>", dirPath, "</span>").br().addln();

        final String reportStr = report.toString();
        publish(new ProgressUpdate(0.0f, "Determining number of files to scan", reportStr));

        final List<File> problemFiles = new ArrayList<>(1000);
        final List<File> examFiles = new ArrayList<>(200);
        final List<File> homeworkFiles = new ArrayList<>(200);

        final int total = count(problemFiles, examFiles, homeworkFiles, dir);
        final int numProblems = problemFiles.size();
        final int numExams = examFiles.size();
        final int numHomeworks = homeworkFiles.size();

        // Recursively scan, testing as we go.
        final String numProblemsStr = Integer.toString(numProblems);
        final String numExamsStr = Integer.toString(numExams);
        final String numHomeworksStr = Integer.toString(numHomeworks);
        report.add("&bull; Found ", numProblemsStr, " problems, ", numExamsStr, " exams, and ", numHomeworksStr, " " +
                "homeworks.").br().addln();

        try {
            final float pct1 = 100.0f * (float) numProblems / (float) total;
            final float pct2 = 100.0f * (float) (numProblems + numExams) / (float) total;

            if (!isCancelled()) {
                report.sH(3).add("Scanning Problems").eH(3);
                final String reportText = report.toString();
                publish(new ProgressUpdate(0.5f, "Scanning Problems...", reportText));
                final int dirLen = dirPath.length();
                scanProblems(report, dirLen, problemFiles, pct1);
            }

            if (!isCancelled()) {
                report.sH(3).add("Scanning Exams").eH(3);
                final String reportText = report.toString();
                publish(new ProgressUpdate(pct1, "Scanning Exams...", reportText));
                scanExams(report, examFiles, pct1, pct2);
            }

            if (!isCancelled()) {
                report.sH(3).add("Scanning Homeworks").eH(3);
                final String reportText = report.toString();
                publish(new ProgressUpdate(pct2, "Scanning Homework...", reportText));
                scanHomeworks(report, homeworkFiles, pct2);
            }
        } catch (final RuntimeException ex) {
            Log.warning(ex);
        }

        final String reportText = report.toString();
        publish(new ProgressUpdate(100.0f, "Scan Finished", reportText));

        return null;
    }

    /**
     * Scans all problems, updating the displayed report after each.
     *
     * @param report       the report being constructed
     * @param dirLen       the length of the directory path being scanned
     * @param problemFiles the problem files to scan
     * @param endPct       the ending completion percentage
     */
    private void scanProblems(final HtmlBuilder report, final int dirLen, final Collection<? extends File> problemFiles,
                              final float endPct) {

        final float step = (endPct - 0.5f) / (float) problemFiles.size();
        float pct = 0.5f;

        for (final File file : problemFiles) {
            if (isCancelled()) {
                break;
            }
            scanProblem(report, dirLen, file);
            pct += step;
            publish(new ProgressUpdate(pct, "Scanning Problems...", report.toString()));
        }
    }

    /**
     * Scans a single problem.
     *
     * @param report      the report being constructed
     * @param dirLen       the length of the directory path being scanned
     * @param problemFile the problem file to scan
     */
    private void scanProblem(final HtmlBuilder report, final int dirLen, final File problemFile) {

        final String filePath = problemFile.getAbsolutePath();
        final String relativePath = filePath.substring(dirLen);
        report.addln(relativePath).br();

        final String xml = FileLoader.loadFileAsString(problemFile, true);
        if (xml == null) {
            report.sSpan(null, "style='color:red;'").add("ERROR: Unable to read file.").eSpan().br().addln();
        } else {
            try {
                final XmlContent content = new XmlContent(xml, false, true);
                final AbstractProblemTemplate prob = ProblemTemplateFactory.load(content, this.parserMode);
                final List<XmlContentError> parseErrors = content.getAllErrors();

                if (!parseErrors.isEmpty()) {
                    report.sSpan(null, "style='color:red;'").add("WARNING: There were parser warnings:").eSpan().br()
                            .addln();
                    for (final XmlContentError err : parseErrors) {
                        report.add("&nbsp; &bull; ").sSpan(null, "style='color:blue;'").add(err).eSpan().br().addln();
                    }
                }
//                final long start = System.currentTimeMillis();
                QualityControlChecks.problemQualityChecks(report, problemFile, prob);
//                final long end = System.currentTimeMillis();
//                final long duration = end - start;
//                Log.info("Tests on ", prob.ref, " took " + duration + " ms.");
            } catch (final ParsingException ex) {
                report.sSpan(null, "style='color:red;'").add("ERROR: Exception while parsing file: ", ex.getMessage())
                        .eSpan().br().addln();
            }
        }
    }

    /**
     * Scans all exams.
     *
     * @param report    the report being constructed
     * @param examFiles the problem files to scan
     * @param startPct  the starting completion percentage
     * @param endPct    the ending completion percentage
     */
    private void scanExams(final HtmlBuilder report, final Collection<? extends File> examFiles,
                           final float startPct, final float endPct) {

        final float step = (endPct - startPct) / (float) examFiles.size();
        float pct = startPct;

        for (final File file : examFiles) {
            if (isCancelled()) {
                break;
            }
            scanExam(report, file);
            pct += step;
            publish(new ProgressUpdate(pct, "Scanning Exam...", report.toString()));
        }
    }

    /**
     * Scans a single exam.
     *
     * @param report   the report being constructed
     * @param examFile the exam file to scan
     */
    private void scanExam(final HtmlBuilder report, final File examFile) {

        report.addln(examFile.getAbsolutePath()).br();

        final String xml = FileLoader.loadFileAsString(examFile, true);
        if (xml == null) {
            report.sSpan(null, "style='color:red;'").add("ERROR: Unable to read file.").eSpan().br().addln();
        } else {
            try {
                final XmlContent content = new XmlContent(xml, false, true);
                final ExamObj exam = ExamFactory.load(content, this.parserMode);
                final List<XmlContentError> parseErrors = content.getAllErrors();

                if (exam == null) {
                    report.sSpan(null, "style='color:red;'").add("ERROR: Unable to parse file.").eSpan().br().addln();
                    for (final XmlContentError err : parseErrors) {
                        report.add("&nbsp; &bull; ", err).br().addln();
                    }
                } else {
                    if (!parseErrors.isEmpty()) {
                        report.sSpan(null, "style='color:red;'").add("WARNING: There were parser warnings:").eSpan()
                                .br().addln();
                        for (final XmlContentError err : parseErrors) {
                            report.add("&nbsp; &bull; ").sSpan(null, "style='color:blue;'").add(err).eSpan().br()
                                    .addln();
                        }
                    }

                    QualityControlChecks.examQualityChecks(report, examFile, exam);
                }
            } catch (final ParsingException ex) {
                report.sSpan(null, "style='color:red;'").add("ERROR: Exception while parsing file: ", ex.getMessage())
                        .eSpan().br().addln();
            }
        }
    }

    /**
     * Scans all homeworks.
     *
     * @param report        the report being constructed
     * @param homeworkFiles the homework files to scan
     * @param startPct      the starting completion percentage
     */
    private void scanHomeworks(final HtmlBuilder report, final Collection<? extends File> homeworkFiles,
                               final float startPct) {

        final float step = (100.0f - startPct) / (float) homeworkFiles.size();
        float pct = startPct;

        for (final File file : homeworkFiles) {
            if (isCancelled()) {
                break;
            }
            scanHomework(report, file);
            pct += step;
            publish(new ProgressUpdate(pct, "Scanning Homework Set...", report.toString()));
        }
    }

    /**
     * Scans a single homework.
     *
     * @param report       the report being constructed
     * @param homeworkFile the homework file to scan
     */
    private void scanHomework(final HtmlBuilder report, final File homeworkFile) {

        report.addln(homeworkFile.getAbsolutePath()).br();

        final String xml = FileLoader.loadFileAsString(homeworkFile, true);
        if (xml == null) {
            report.sSpan(null, "style='color:red;'").add("ERROR: Unable to read file.").eSpan().br().addln();
        } else {
            try {
                final XmlContent content = new XmlContent(xml, false, true);
                final ExamObj exam = ExamFactory.load(content, this.parserMode);
                final List<XmlContentError> parseErrors = content.getAllErrors();

                if (exam == null) {
                    report.sSpan(null, "style='color:red;'")
                            .add("ERROR: Unable to parse file.").eSpan().br().addln();
                    for (final XmlContentError err : parseErrors) {
                        report.add("&nbsp; &bull; ", err).br().addln();
                    }
                } else {
                    if (!parseErrors.isEmpty()) {
                        report.sSpan(null, "style='color:red;'")
                                .add("WARNING: There were parser warnings:").eSpan().br().addln();
                        for (final XmlContentError err : parseErrors) {
                            report.add("&nbsp; &bull; ").sSpan(null, "style='color:blue;'").add(err)
                                    .eSpan().br().addln();
                        }
                    }

                    QualityControlChecks.homeworkQualityChecks(report, homeworkFile, exam);
                }
            } catch (final ParsingException ex) {
                report.sSpan(null, "style='color:red;'").add("ERROR: Exception while parsing file: ", ex.getMessage())
                        .eSpan().br().addln();
            }
        }
    }

    /**
     * Called on the AWT event thread when one or more updates have been published.
     *
     * @param chunks the updates (only the most recent is of interest)
     */
    @Override
    protected void process(final List<ProgressUpdate> chunks) {

        final ProgressUpdate latest = chunks.get(chunks.size() - 1);
        this.owner.update(latest);
    }

    /**
     * Descend a directory recursively, counting the number of xml files we find.
     *
     * @param problems  a list to which to add all files identified as "problems"
     * @param exams     a list to which to add all files identified as "exams"
     * @param homeworks a list to which to add all files identified as "homeworks"
     * @param dir       the directory to scan
     * @return the number of matching XML files within the directory
     */
    private static int count(final List<? super File> problems, final List<? super File> exams,
                             final List<? super File> homeworks, final File dir) {

        final String path = dir.getAbsolutePath();
        final File[] list = dir.listFiles();

        final boolean isProblem = path.contains("/problems/")
                || path.endsWith("/problems")
                || path.contains("\\problems\\")
                || path.endsWith("\\problems");
        final boolean isExam = path.contains("/exams/")
                || path.endsWith("/exams")
                || path.contains("\\exams\\")
                || path.endsWith("\\exams");
        final boolean isHomework = path.contains("/homework/")
                || path.endsWith("/homework")
                || path.contains("\\homework\\")
                || path.endsWith("\\homework")
                || path.contains("/assignments/")
                || path.endsWith("/assignments")
                || path.contains("\\assignments\\");

        if (list != null) {
            for (final File file : list) {

                if (file.isDirectory()) {
                    count(problems, exams, homeworks, file);
                } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {

                    if (isProblem) {
                        problems.add(file);
                    } else if (isHomework) {
                        homeworks.add(file);
                    } else if (isExam) {
                        exams.add(file);
                    } else {
                        // File is not in a folder that indicates types - infer type from its contents
                        final String xml = FileLoader.loadFileAsString(file, true);

                        if (xml.contains("<problem ")) {
                            problems.add(file);
                        } else if (xml.contains("<exam ")) {
                            exams.add(file);
                        }
                    }
                }
            }
        }

        return problems.size() + exams.size() + homeworks.size();
    }

    /**
     * Called when the task is done.
     */
    @Override
    protected void done() {

        this.owner.workerDone();
    }
}
