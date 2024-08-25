package dev.mathops.session;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.MessageFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A class that can read and write data files in the student data directory.
 */
public final class ExamWriter {

    /** The compressed exam XML file. */
    public static final String EXAM_FILE = "exam.xml.Z";

    /** The exam answers file. */
    public static final String ANSWERS_FILE = "answers.txt.Z";

    /** The exam recovery file. */
    private static final String RECOVERY_FILE = "recovery.txt.Z";

    /** The path under which to read/write student data. */
    private final File dataPath;

    /**
     * Constructs a new {@code ExamWriter} object.
     */
    public ExamWriter() {

        this.dataPath = PathList.getInstance().get(EPath.CUR_DATA_PATH);
    }

    /**
     * Constructs the path under which an exam of a specified serial number will be stored.
     *
     * @param shortTermStr the short term string
     * @param studentId    the student ID
     * @param serial       the serial number (negative for practice exams)
     * @return the exam path (the exam directory name is the serial number, if positive, or "P" followed by the negated
     *         serial number, if negative)
     */
    public File makeExamPath(final String shortTermStr, final String studentId, final long serial) {

        final String fname = "student" + shortTermStr;

        final File examDir = new File(new File(new File(this.dataPath, fname), studentId), "exams");

        return new File(examDir, serial >= 0L ? Long.toString(serial) : "P" + (-serial));
    }

    /**
     * Constructs the path under which an exam of a specified serial number will be stored.
     *
     * @param shortTermStr the short term string
     * @param studentId    the student ID
     * @param serial       the serial number (negative for practice exams)
     * @return the exam path (the exam directory name is the serial number, if positive, or "P" followed by the negated
     *         serial number, if negative)
     */
    public static String makeWebExamPath(final String shortTermStr, final String studentId, final long serial) {

        final HtmlBuilder xml = new HtmlBuilder(50);

        xml.add("student", shortTermStr, CoreConstants.SLASH, studentId, "/exams/");
        if (serial >= 0L) {
            xml.add(Long.toString(serial));
        } else {
            xml.add("P", Long.toString(-serial));
        }

        return xml.toString();
    }

    /**
     * Writes the record of a presented exam for a student. This currently writes the XML representation of the exam
     * into a subdirectory maintained for the student.
     *
     * @param studentId the ID of the student under which to record the exam
     * @param term      the term under which to file the exam
     * @param exam      the exam that was presented
     * @param reply     the XML representation of the get exam reply
     * @return {@code true} if the record was successfully written
     */
    public boolean writePresentedExam(final String studentId, final TermRec term, final ExamObj exam,
                                      final String reply) {

        if (studentId == null) {
            Log.warning("No user ID provided when writing exam.");
            return false;
        } else if (term == null) {
            Log.warning("No term information provided when writing exam.");
            return false;
        } else if (exam == null) {
            Log.warning("No exam provided to write.");
            return false;
        } else if (reply == null) {
            Log.warning("No exam reply provided to write.");
            return false;
        }

        final int serial = exam.serialNumber.intValue();
        final File path = makeExamPath(term.term.shortString, studentId, (long) serial);

        if (path.exists()) {
            Log.warning("Path [", path.getAbsolutePath(),
                    "] already exists!  Cannot issue another exam in same second.");
            return false;
        }

        Log.info("Writing ", exam.examVersion, " exam to ", path.getAbsolutePath());

        boolean ok = path.mkdirs();

        if (ok) {
            final File file = new File(path, EXAM_FILE);

            try (final GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(file))) {
                gz.write(reply.getBytes(StandardCharsets.UTF_8));
                gz.finish();
            } catch (final Exception ex) {
                ok = false;
                Log.severe("Failed to write presented exam", ex);
            }
        } else {
            Log.severe("Failed to create directory to write presented exam", path);
        }

        return ok;
    }

    /**
     * Writes the record of a presented exam for a student. This currently writes the XML representation of the exam
     * into a subdirectory maintained for the student.
     *
     * @param studentId the ID of the student under which to record the exam
     * @param term      the term under which the exam is filed
     * @param serial    the serial number of the presented exam
     * @return the exam that was delivered to the student; null if not found
     */
    public ExamObj readPresentedExam(final String studentId, final TermRec term, final long serial) {

        // Parse the stored message
        ExamObj exam = null;

        if (studentId != null && term != null) {
            final File path = makeExamPath(term.term.shortString, studentId, serial);

            Log.info("Looking in ", path.getAbsolutePath(), " for exam data");

            // Copy the file into a byte array
            final File file = new File(path, EXAM_FILE);
            byte[] result = null;

            if (file.exists()) {
                try (final InputStream in = new GZIPInputStream(new FileInputStream(file));
                     final ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                    final byte[] buffer = new byte[1024];
                    int len = in.read(buffer);

                    while (len > 0) {
                        out.write(buffer, 0, len);
                        len = in.read(buffer);
                    }
                    result = out.toByteArray();
                } catch (final IOException ex) {
                    Log.warning("Failed to read exam file ", file.getAbsolutePath(), ex);
                }
            }

            if (result != null) {
                final AbstractMessageBase msg = MessageFactory
                        .parseMessage(new String(result, StandardCharsets.UTF_8).toCharArray());

                if (msg instanceof GetExamReply) {
                    exam = ((GetExamReply) msg).presentedExam;
                    Log.info("Presented ", exam.examVersion, " exam successfully loaded");
                }
            }
        }

        return exam;
    }

    /**
     * Writes the record of an exam update sent by the student. This currently writes the XML representation of the exam
     * into a subdirectory maintained for the student.
     *
     * @param studentId the ID of the student under which to record the exam
     * @param term      the term under which the exam is filed
     * @param answers   the current list of answers
     * @param recovery  true if this is a recovery
     * @return {@code true} if the record was successfully written
     */
    public boolean writeUpdatedExam(final String studentId, final TermRec term, final Object[][] answers,
                                    final boolean recovery) {

        final String id;

        if (answers == null || answers.length == 0 || answers[0] == null || !(answers[0][0] instanceof Long)) {
            Log.warning("Insufficient data to log updated exam.");
            return false;
        } else if (term == null) {
            Log.warning("No term information provided to log updated exam.");
            return false;
        } else if (studentId == null) {
            Log.warning("No student ID in incoming exam - writing under RECOVERY");
            id = "RECOVERY";
        } else {
            id = studentId;
        }

        final long serial = ((Long) answers[0][0]).longValue();
        final File path = makeExamPath(term.term.shortString, id, serial);

        if (!path.exists() && !path.mkdirs()) {
            Log.warning("Failed to create directory " + path.getAbsolutePath());
        }

        final HtmlBuilder builder = new HtmlBuilder(50);

        final int numAns = answers.length;
        for (int i = 0; i < numAns; i++) {
            builder.add(Integer.toString(i + 1), ": ");

            if (answers[i] != null) {
                final int size = answers[i].length;
                for (int j = 0; j < size; j++) {
                    if (j > 0) {
                        builder.add(", ");
                    }

                    if (answers[i][j] != null) {
                        builder.add(answers[i][j].toString());
                    } else {
                        builder.add("null");
                    }
                }
            } else {
                builder.add("(no answer)");
            }

            builder.addln();
        }

        final File file = new File(path, recovery ? RECOVERY_FILE : ANSWERS_FILE);
        boolean ok = false;

        try (final GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(file))) {
            gz.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            gz.finish();
            ok = true;
        } catch (final Exception ex) {
            Log.severe("Failed to write updated exam data", ex);
        }

        return ok;
    }

//    /**
//     * Main method to load an exam as many times as possible in 5 seconds and report an average
//     * number of exams per second loaded.
//     *
//     * @param args command-line arguments
//     */
//     public static void main(final String... args) {
//
//     final Term term = new Term(ETermName.FALL, 2017);
//     int count = 0;
//     final long end = System.currentTimeMillis() + 5000;
//
//     while (System.currentTimeMillis() < end) {
//     new ExamWriter().readPresentedExam("111223333", term, 123456);
//     ++count;
//     }
//
//     Log.info((float) (count / 5.0) + " per second");
//     }
}
