package dev.mathops.app.exam;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.MessageFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

/**
 * Harvests exam data stored in server directories and generates a CSV file.
 */
public enum Harvester {
    ;

    /**
     * Do the actual harvesting.
     */
    private static void harvest() {

        final File impback = new File("/impback");
        final File[] dirsBaK = impback.listFiles();
        if (dirsBaK != null) {
            for (final File dir : dirsBaK) {
                if (dir.getName().startsWith("student")) {
                    processSemester(dir);
                }
            }
        }

        final File impcurrent = new File("/imp/data");
        final File[] dirsCurrent = impcurrent.listFiles();
        if (dirsCurrent != null) {
            for (final File dir : dirsCurrent) {
                if (dir.getName().startsWith("student")) {
                    processSemester(dir);
                }
            }
        }
    }

    /**
     * Processes a single semester's data.
     *
     * @param dir the directory with semester data
     */
    private static void processSemester(final File dir) {

        Log.info("PROCESSING: " + dir.getAbsolutePath());

        final File target = new File(dir, "exam_data.csv");
        try (final FileWriter fw = new FileWriter(target, StandardCharsets.UTF_8); //
             final BufferedWriter out = new BufferedWriter(fw)) {

            out.write("Serial,Student,Course,Unit,Version,Proctored,Remote,Item\r\n");

            final File[] list = dir.listFiles();
            if (list != null) {
                for (final File file : list) {
                    Log.finest(CoreConstants.DOT);
                    if ("888888888".equals(file.getName())) {
                        continue;
                    }

                    if (file.getName().startsWith("8")) {
                        processStudent(file, out);
                    }
                }
                Log.fine(CoreConstants.EMPTY);
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

    }

    /**
     * Processes a single student's folder within a single semester's data.
     *
     * @param dir the directory with student data
     * @param out the writer to which to write the output
     */
    private static void processStudent(final File dir, final Writer out) {

        final String studentId = dir.getName();

        final File[] exams = new File(dir, "exams").listFiles();
        if (exams != null) {
            for (final File exam : exams) {
                processExam(studentId, exam, out);
            }
        }
    }

    /**
     * Processes a single exam output folder.
     *
     * @param studentId the student ID
     * @param examDir   the exam folder
     * @param out       the writer to which to write the output
     */
    private static void processExam(final String studentId, final File examDir, final Writer out) {

        final HtmlBuilder htm = new HtmlBuilder(100);

        final ExamObj exam = readExam(examDir);
        if (exam != null) {
            final String course = exam.course;
            final String unit = exam.courseUnit;
            final String version = exam.examVersion;
            final boolean proctored = exam.proctored;
            final boolean remote = exam.remote;
            String serial = examDir.getName();

            if (serial.endsWith(")")) {
                final int start = serial.indexOf('(');
                if (start >= 0) {
                    serial = serial.substring(start, serial.length() - 1);
                }
            }

            for (int s = 0; s < exam.getNumSections(); ++s) {
                final ExamSection es = exam.getSection(s);
                for (int p = 0; p < es.getNumProblems(); ++p) {
                    final ExamProblem ep = es.getPresentedProblem(p);
                    if (ep != null) {
                        final AbstractProblemTemplate sel = ep.getSelectedProblem();
                        if (sel != null && sel.ref != null) {
                            final String ref = sel.ref;

                            htm.addln(serial, CoreConstants.COMMA, studentId, CoreConstants.COMMA,
                                    course, CoreConstants.COMMA, unit, CoreConstants.COMMA, version,
                                    CoreConstants.COMMA, proctored //
                                            ? "Y" : "N",
                                    CoreConstants.COMMA, remote ? "Y" : "N",
                                    CoreConstants.COMMA, ref);
                            try {
                                out.write(htm.toString());
                            } catch (final IOException ex) {
                                Log.warning(ex);
                            }
                            htm.reset();
                        }
                    }
                }
            }
        }
    }

    /**
     * Reads and parses the compressed exam file.
     *
     * @param examDir the exam directory
     * @return the parsed exam; {@code null} on any error
     */
    private static ExamObj readExam(final File examDir) {

        ExamObj exam = null;

        final File file = new File(examDir, ExamWriter.EXAM_FILE);
        byte[] result = null;

        if (file.exists()) {
            try (final InputStream in = new GZIPInputStream(new FileInputStream(file));
                 final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                final byte[] buffer = new byte[1024];
                int len = in.read(buffer);

                while (len > 0) {
                    baos.write(buffer, 0, len);
                    len = in.read(buffer);
                }
                result = baos.toByteArray();
            } catch (final IOException ex) {
                Log.warning("Failed to read exam file ", file.getAbsolutePath(), ex);
            }
        } else {
            // Old directories have HTML as well as xml
            final File[] files = examDir.listFiles();
            boolean found = false;
            for (final File f : files) {
                if (f.getName().endsWith(".xml.Z")) {
                    try (final InputStream in = new GZIPInputStream(new FileInputStream(f));
                         final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                        final byte[] buffer = new byte[1024];
                        int len = in.read(buffer);

                        while (len > 0) {
                            baos.write(buffer, 0, len);
                            len = in.read(buffer);
                        }
                        result = baos.toByteArray();
                    } catch (final IOException ex) {
                        Log.warning("Failed to read exam file ",
                                file.getAbsolutePath(), ex);
                    }
                    found = true;
                    break;
                }
            }

            if (!found) {
                Log.warning("Can't find exam file in ", examDir.getAbsolutePath());
            }
        }

        if (result != null) {
            final AbstractMessageBase msg = MessageFactory
                    .parseMessage(new String(result, StandardCharsets.UTF_8).toCharArray());

            if (msg instanceof GetExamReply) {
                exam = ((GetExamReply) msg).presentedExam;

            } else {
                Log.warning("Parsed object is ", msg.getClass().getName(),
                        " rather than GetExamReply");
            }
        }

        return exam;
    }

    /**
     * Runs the program.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        harvest();
    }
}
