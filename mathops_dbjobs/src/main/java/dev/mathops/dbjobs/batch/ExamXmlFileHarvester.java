package dev.mathops.dbjobs.batch;

import dev.mathops.commons.log.Log;

import java.io.File;

/**
 * A class that scans the directories where XML records of exams are stored, and extracts small summary data for each
 * exam that includes the problem selected for each exam item. This should be run on the production web server where the
 * '/impback' folder exists.
 *
 * <p>
 * The results are stored in a file system structure with this layout
 *
 * <pre>
 * /target_path
 *     /Numan-2021-1Spring (one folder per semester)
 *         /888888888 (one folder per student)
 *             123456.xml (where 123456 is the serial number of the exam)
 * </pre>
 * <p>
 * The XML format of the summary data includes:
 *
 * <pre>
 * &lt;exam-record stu='888888888' serial='123456' version='18GAT'&gt;
 *   &lt;problem id='1' selected='math.m118.problems.gateway.2P1A'
 *   ...
 * &lt;/exam-record&gt;
 * </pre>
 */
final class ExamXmlFileHarvester {

    /** The top level in the source file directory. */
    private final File sourceTop;

    /** The top level in the destination file directory. */
    private final File destTop;

    /**
     * Constructs a new {@code ExamXmlFileHarvester}.
     */
    private ExamXmlFileHarvester() {

        this.sourceTop = new File("/impback");
        this.destTop = new File("/imp/data/summary");
    }

    /**
     * Performs data extraction.
     */
    private void run() {

        final File[] termDirectories = this.sourceTop.listFiles();
        if (termDirectories != null) {
            for (final File termDir : termDirectories) {
                final String name = termDir.getName();

                if (name.length() >= 11 && name.startsWith("student")) {
                    processTermDirectory(termDir);
                } else {
                    Log.warning("Skipping term directory: ", name);
                }
            }
        }
    }

    /**
     * Processes a term directory.
     *
     * @param termDir the term directory
     */
    private void processTermDirectory(final File termDir) {

        final String termDirName = termDir.getName();
        final String shortTerm = termDirName.substring(7);

        final StringBuilder filename = new StringBuilder(30);
        filename.append("Numan-20");
        filename.append(shortTerm, 2, 4);

        if (shortTerm.startsWith("SP")) {
            filename.append("-1Spring");
        } else if (shortTerm.startsWith("SM")) {
            filename.append("-2Summer");
        } else if (shortTerm.startsWith("FA")) {
            filename.append("-3Fall");
        } else {
            Log.warning("Invalid term designator: ", shortTerm);
            filename.append(shortTerm);
        }

        final File termTargetDir = new File(this.destTop, filename.toString());

        if (termTargetDir.exists() || termTargetDir.mkdirs()) {

            final File[] stuDirectories = termDir.listFiles();

            if (stuDirectories != null) {
                for (final File stuDir : stuDirectories) {
                    final String name = stuDir.getName();

                    if (name.length() == 9) {

                        if ("888888888".equals(name)
                                || "823251213".equals(name)
                                || "111223333".equals(name)) {

                            final String fullName = termDirName + "/" + name;
                            Log.warning("Skipping student directory: ", fullName);
                        } else {
                            final File stuTargetDir = new File(termTargetDir, name);

                            if (stuTargetDir.exists() || stuTargetDir.mkdirs()) {
                                final File stuExamsDir = new File(stuDir, "exams");

                                if (stuExamsDir.exists()) {
                                    processStudentDirectory(stuExamsDir, stuTargetDir);
                                } else {
                                    final String fullName = termDirName + "/" + name;
                                    Log.warning("Student directory ", fullName,
                                            " contained no 'exams' subdirectory");
                                }
                            } else {
                                Log.warning("Failed to create: ", stuTargetDir.getAbsolutePath());
                            }
                        }
                    } else {
                        final String fullName = termDirName + "/" + name;
                        Log.warning("Skipping student directory: ", fullName);
                    }
                }
            }
        } else {
            Log.warning("Failed to create: ", termTargetDir.getAbsolutePath());
        }
    }

    /**
     * Processes a student directory.
     *
     * @param stuExamsDir  the directory containing the student's exams
     * @param stuTargetDir the target directory in which to write summary files
     */
    private static void processStudentDirectory(final File stuExamsDir, final File stuTargetDir) {

        final File[] examFolders = stuExamsDir.listFiles();

        if (examFolders != null) {
            for (final File examFolder : examFolders) {
                final String folderName = examFolder.getName();

                if (!folderName.isEmpty() && folderName.charAt(0) == 'P') {
                    // Practice - skip
                    continue;
                }

                processExamFolder(examFolder);
            }
        }
    }

    /**
     * Processes a single exam folder.
     *
     * @param examFolder   the exam folder
     */
    private static void processExamFolder(final File examFolder) {

        final File examXml = new File(examFolder, "exam.xml.Z");
        final File answers = new File(examFolder, "answers.txt.Z");

        if (examXml.exists()) {
            if (answers.exists()) {

                // TODO:

            } else {
                Log.warning("Exam started but no answers found.  Folder contents:");
                final File[] files = examFolder.listFiles();
                if (files != null) {
                    for (final File file : files) {
                        Log.warning("    ", file.getName());
                    }
                }
            }
        } else {
            Log.warning("Exam file not found.  Folder contents:");
            final File[] files = examFolder.listFiles();
            if (files != null) {
                for (final File file : files) {
                    Log.warning("    ", file.getName());
                }
            }
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        new ExamXmlFileHarvester().run();
    }
}
