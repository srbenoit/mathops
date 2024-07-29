package dev.mathops.session.txn.handlers;

import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.GetExamReply;
import dev.mathops.session.txn.messages.GetPastExamReply;
import dev.mathops.session.txn.messages.GetPastExamRequest;
import dev.mathops.session.txn.messages.GetReviewExamReply;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * A handler for requests to get a copy of a past exam.
 * <p>
 * This class is not thread-safe. Use a new handler within each thread.
 */
public final class GetPastExamHandler extends AbstractHandlerBase {

    /**
     * Construct a new {@code GetPastExamHandler}.
     */
    public GetPastExamHandler() {

        super();
    }

    /**
     * Processes a message from the client.
     *
     * @param cache   the data cache
     * @param message the message received from the client
     * @return the reply to be sent to the client, or null if the connection should be closed
     */
    @Override
    public String process(final Cache cache, final AbstractRequestBase message) {

        setMachineId(message);
        touch(cache);

        final String result;

        if (message instanceof final GetPastExamRequest request) {
            result = processRequest(request);
        } else {
            final String clsName = message.getClass().getName();
            Log.info("GetPastExamHandler called with ", clsName);

            final GetPastExamReply reply = new GetPastExamReply();
            reply.error = "Invalid request type for get past exam request";
            result = reply.toXml();
        }

        return result;
    }

    /**
     * Process a request from the client.
     *
     * @param request the {@code GetPastExamRequest} received from the client
     * @return the generated reply XML to send to the client
     */
    private static String processRequest(final GetPastExamRequest request) {

        final GetPastExamReply reply = new GetPastExamReply();

        // Verify presence of all required files
        final File basePath1 = new File("/imp/data");
        final File basePath2 = new File("/impback");

        if ((!basePath1.exists() || !basePath1.isDirectory()) && (!basePath2.exists() || !basePath2.isDirectory())) {
            reply.error = "No student data repository found.";
            Log.warning(basePath1.getAbsolutePath() + " not found.");
            return reply.toXml();
        }

        File xmlPath = new File(basePath1, request.xmlPath);
        File updPath = new File(basePath1, request.updPath);

        if (!xmlPath.exists() || !updPath.exists()) {
            xmlPath = new File(basePath2, request.xmlPath);
            updPath = new File(basePath2, request.updPath);

            if (!xmlPath.exists() || !updPath.exists()) {
                xmlPath = new File(basePath1, request.xmlPath);
                updPath = new File(basePath1, request.updPath);
            }
        }

        if (!xmlPath.exists()) {
            reply.error = "Requested exam not found.";
            final String xmlAbsPath = xmlPath.getAbsolutePath();
            Log.warning(reply.error + CoreConstants.SPC + xmlAbsPath);
            return reply.toXml();
        }

        final String updAbsPath = updPath.getAbsolutePath();

        if (!updPath.exists()) {
            reply.error = "Requested exam answers not found.";
            Log.warning(reply.error + CoreConstants.SPC + updAbsPath);
            return reply.toXml();
        }

        if (!loadExam(xmlPath, reply)) {
            return reply.toXml();
        }

        // Load the updates file
        if (!loadUpdates(updPath, reply.exam)) {
            reply.error = "Unable to read historical exam answer record.";
            Log.warning(reply.error + CoreConstants.SPC + updAbsPath);
            return reply.toXml();
        }

        return reply.toXml();
    }

    /**
     * Read the exam XML file.
     *
     * @param xmlFile the XML file to read
     * @param reply   the reply to which to add the exam or any error messages
     * @return true if successful; false otherwise
     */
    private static boolean loadExam(final File xmlFile, final GetPastExamReply reply) {

        // Read the file and convert to a string and character array
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (final InputStream in = new FileInputStream(xmlFile)) {

            if (xmlFile.getName().endsWith(".Z")) {
                try (final InputStream zin = new GZIPInputStream(in)) {
                    for (int len = zin.read(buffer); len > 0; len = zin.read(buffer)) {
                        baos.write(buffer, 0, len);
                    }
                }
            } else {
                for (int len = in.read(buffer); len > 0; len = in.read(buffer)) {
                    baos.write(buffer, 0, len);
                }
            }

        } catch (final IOException ex) {
            // No action
        }

        final String str = baos.toString(StandardCharsets.UTF_8);
        final char[] data = str.toCharArray();

        // Convert the character array into the proper request type
        try {

            if (str.startsWith("<get-review-exam-reply>")) {
                final GetReviewExamReply review = new GetReviewExamReply(data);
                reply.exam = review.presentedExam;
            } else if (str.startsWith("<get-exam-reply>")) {
                final GetExamReply unit = new GetExamReply(data);
                reply.exam = unit.presentedExam;
            } else {
                reply.error = "Server could not determine type of exam.";
                Log.info(reply.error);
                return false;
            }
        } catch (final IllegalArgumentException e) {
            reply.error = "Server is unable to load exam record.";
            Log.info(reply.error);
            return false;
        }

        if (reply.exam == null) {
            reply.error = "Server failed to load exam record.";
            Log.info(reply.error);
            return false;
        }

        return true;
    }

    /**
     * Read the updates file, rebuild the list of student answers, and apply them to the exam.
     *
     * @param updatesFile the updates file to read
     * @param exam        the exam to which to apply the updates
     * @return true if successful; false otherwise
     */
    private static boolean loadUpdates(final File updatesFile, final ExamObj exam) {

        boolean ok = false;

        // Read the file and convert to String
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (updatesFile.exists()) {
            try (final InputStream in = new FileInputStream(updatesFile)) {

                if (updatesFile.getName().endsWith(".Z")) {
                    try (final InputStream zin = new GZIPInputStream(in)) {
                        for (int len = zin.read(buffer); len > 0; len = zin.read(buffer)) {
                            baos.write(buffer, 0, len);
                        }
                    }
                } else {
                    for (int len = in.read(buffer); len > 0; len = in.read(buffer)) {
                        baos.write(buffer, 0, len);
                    }
                }
            } catch (final IOException ex) {
                Log.warning("Failed to read updates file", ex);
            }
        }

        final String str = baos.toString(StandardCharsets.UTF_8);

        // Convert to a series of lines, trimming away line numbers
        final List<String> data = new ArrayList<>(30);
        int num = 1;
        try (final BufferedReader br = new BufferedReader(new StringReader(str))) {
            String line = br.readLine();

            while (line != null) {
                final String test = num + ": ";

                if (!line.startsWith(test)) {
                    num = -1;
                    break;
                }

                final int testLen = test.length();
                final String substring = line.substring(testLen);
                data.add(substring);
                line = br.readLine();
                ++num;
            }

            ok = num > 1;
        } catch (final IOException ex) {
            Log.warning("Failed to scan updates file", ex);
        }

        // Convert first line if updates file into "state" of exam
        if (ok) {
            final int numAns = data.size();
            final Serializable[][] ans = new Serializable[numAns][];
            ans[0] = new Serializable[4];

            String[] split = data.getFirst().split(CoreConstants.COMMA);

            if (split.length == 4) {
                ans[0][0] = Long.valueOf(0L);
                ans[0][1] = ans[0][0];
                ans[0][2] = ans[0][0];
                ans[0][3] = ans[0][0];
            } else {
                Log.warning("Updates file has invalid state line: ", data.getFirst());
                ok = false;
            }

            if (ok) {

                // First line can be ignored, but convert every other line into an answer in an
                // answer array.
                for (num = 1; ok && (num < numAns); ++num) {
                    final String test = data.get(num);

                    if ("(no answer)".equals(test)) {
                        continue;
                    }

                    // Break on comma boundaries
                    split = test.split(CoreConstants.COMMA);
                    if (split.length == 0) {
                        continue;
                    }

                    // Now the tricky part - find out what data type the answers are and convert
                    // them back to answer objects

                    // Get the exam problem so we have some clue
                    final ExamProblem eprob = exam.getProblem(num);
                    final AbstractProblemTemplate prob = eprob.getSelectedProblem();

                    if (prob instanceof ProblemEmbeddedInputTemplate) {

                        if (split[0].contains("{") && split[0].contains("}")) {

                            // Type is set of embedded input parameters - we
                            // just store these in the answer list as strings
                            final int len = split.length;
                            ans[num] = new String[len];
                            for (int i = 0; i < len; ++i) {
                                split[i] = split[i].trim();
                            }

                            ans[num] = split;
                        } else {
                            Log.warning("Embedded input answer " + num + " is not parameter list");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemMultipleChoiceTemplate) {

                        if (split.length == 1) {
                            final Long[] ints = new Long[1];

                            try {
                                final String trimmed = split[0].trim();
                                ints[0] = Long.valueOf(trimmed);
                                ans[num] = ints;
                            } catch (final NumberFormatException e) {
                                Log.warning("Multiple choice answer " + num + " not integer" + split[0]);
                                ok = false;
                            }
                        } else {
                            Log.warning("Multiple choice answer " + num + " not length 1");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemMultipleSelectionTemplate) {
                        final int len = split.length;
                        final Long[] ints = new Long[len];
                        try {
                            for (int i = 0; i < len; ++i) {
                                final String trimmed = split[i].trim();
                                ints[i] = Long.valueOf(trimmed);
                            }

                            ans[num] = ints;
                        } catch (final NumberFormatException e) {
                            Log.warning("Multiple selection answer " + num + " not integer");
                            ok = false;
                        }
                    } else if (prob instanceof ProblemNumericTemplate) {

                        if (split.length != 1) {
                            Log.warning("Numeric answer " + num + " not length 1");
                            ok = false;
                        } else if (split[0].contains(CoreConstants.DOT)) {
                            final Double[] reals = new Double[1];

                            try {
                                final String trimmed = split[0].trim();
                                reals[0] = Double.valueOf(trimmed);
                                ans[num] = reals;
                            } catch (final NumberFormatException e) {
                                Log.warning("Numeric real answer " + num + " invalid: " + split[0]);
                                ok = false;
                            }
                        } else {
                            final Long[] longs = new Long[1];

                            try {
                                longs[0] = Long.valueOf(split[0]);
                                ans[num] = longs;
                            } catch (final NumberFormatException e) {
                                Log.warning("Numeric integer answer " + num + " invalid");
                                ok = false;
                            }
                        }

                        // TODO: Handle fractions?
                    }
                }
            }

            // If answers were loaded, apply them to the exam
            if (ok) {
                exam.importState(ans);
            }
        }

        return ok;
    }
}
