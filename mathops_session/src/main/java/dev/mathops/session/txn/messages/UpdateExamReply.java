package dev.mathops.session.txn.messages;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A network message to request that a new exam be realized and stored on a user's account as being in progress.
 */
public final class UpdateExamReply extends AbstractReplyBase {

    /** Result code indicating exam was updated successfully. */
    public static final int SUCCESS = 0;

    /** The result of attempting to update the presented exam. */
    public final int status;

    /** A hash table of scores, key is subtest name, value is score. */
    public Map<String, Integer> subtestScores;

    /**
     * Missed questions, key is question number, value is sub-objective the question relates to.
     */
    public SortedMap<Integer, String> missed;

    /** Grades, key is grading rule name, value is grade. */
    public Map<String, Object> examGrades;

    /**
     * Constructs a new {@code UpdateExamReply}.
     */
    public UpdateExamReply() {

        super();

        this.status = SUCCESS;
        this.subtestScores = new HashMap<>(1);
        this.examGrades = new HashMap<>(1);
        this.missed = new TreeMap<>();
    }

    /**
     * Constructs a new {@code UpdateExamReply}, initializing with data from an XML stream.
     *
     * @param xml The XML stream from which to initialize data.
     * @throws IllegalArgumentException If the XML stream is not valid.
     */
    public UpdateExamReply(final char[] xml) throws IllegalArgumentException {

        super();

        this.subtestScores = new HashMap<>(1);
        this.examGrades = new HashMap<>(1);
        this.missed = new TreeMap<>();

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

        int stat = SUCCESS;
        String value = extractField(message, "status");
        if (value != null) {
            try {
                stat = Long.valueOf(value).intValue();
            } catch (final NumberFormatException ex) {
                Log.warning(Res.get(Res.CANT_PARSE_STATUS));
            }
        }
        this.status = stat;

        String sub = extractField(message, "scores");

        if ((sub != null) && (!sub.isEmpty())) {
            final String[] list = extractFieldList(sub, "score");

            if (list != null) {
                for (final String s : list) {
                    final int pos = s.indexOf('=');

                    if (pos == -1) {
                        throw new IllegalArgumentException(Res.fmt(Res.BAD_SCORE_ENTRY, s));
                    }

                    final String key = s.substring(0, pos).replace('\u2261', '=');
                    value = s.substring(pos + 1);

                    try {
                        this.subtestScores.put(key, Integer.valueOf(value));
                    } catch (final NumberFormatException e) {
                        throw new IllegalArgumentException(Res.fmt(Res.BAD_SCORE, value));
                    }
                }
            }
        }

        sub = extractField(message, "grades");

        if ((sub != null) && (!sub.isEmpty())) {
            final String[] list = extractFieldList(sub, "grade");
            if (list != null) {
                for (final String s : list) {
                    final int pos = s.indexOf('=');

                    if (pos == -1) {
                        throw new IllegalArgumentException(Res.fmt(Res.BAD_GRADE_ENTRY, s));
                    }

                    final String key = s.substring(0, pos).replace('\u2261', '=');
                    value = s.substring(pos + 1);

                    if ("TRUE".equalsIgnoreCase(value)) {
                        this.examGrades.put(key, Boolean.TRUE);
                    } else if ("FALSE".equalsIgnoreCase(value)) {
                        this.examGrades.put(key, Boolean.FALSE);
                    } else {
                        this.examGrades.put(key, value);
                    }
                }
            }
        }

        sub = extractField(message, "missed");

        if ((sub != null) && (!sub.isEmpty())) {
            final String[] list = extractFieldList(sub, "question");

            if (list != null) {
                for (final String s : list) {
                    final int pos = s.indexOf('=');

                    if (pos == -1) {
                        throw new IllegalArgumentException(Res.fmt(Res.BAD_MISSED_ENTRY, s));
                    }

                    final String key = s.substring(0, pos);
                    value = s.substring(pos + 1);

                    try {
                        this.missed.put(Integer.valueOf(key), value);
                    } catch (final NumberFormatException e) {
                        throw new IllegalArgumentException(Res.fmt(Res.BAD_MISSED_QUESTION, value));
                    }
                }
            }
        }
    }

    /**
     * Method to tell the message to free any resources allocated to it. The message will assume no other methods will
     * be called after this one.
     */
    @Override
    public void die() {

        if (this.subtestScores != null) {
            this.subtestScores.clear();
            this.subtestScores = null;
        }

        if (this.examGrades != null) {
            this.examGrades.clear();
            this.examGrades = null;
        }

        if (this.missed != null) {
            this.missed.clear();
            this.missed = null;
        }

        super.die();
    }

    /**
     * /** Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "update-exam-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<update-exam-reply>");
        builder.addln(" <status>", Integer.toString(this.status), "</status>");

        if (!this.subtestScores.isEmpty()) {
            builder.addln(" <scores>");

            for (final Map.Entry<String, Integer> entry : this.subtestScores.entrySet()) {
                builder.addln("  <score>", entry.getKey().replace('=', '\u2261'),
                        "=", entry.getValue(), "</score>");
            }

            builder.addln(" </scores>");
        }

        if (!this.examGrades.isEmpty()) {
            builder.addln(" <grades>");

            for (final Map.Entry<String, Object> entry : this.examGrades.entrySet()) {
                builder.addln("  <grade>", entry.getKey().replace('=', '\u2261'),
                        "=", entry.getValue(), "</grade>");
            }

            builder.addln(" </grades>");
        }

        if (!this.missed.isEmpty()) {
            builder.addln(" <missed>");

            for (final Map.Entry<Integer, String> entry : this.missed.entrySet()) {
                builder.addln("  <question>", entry.getKey(), "=", entry.getValue(), "</question>");
            }

            builder.addln(" </missed>");
        }

        printError(builder);

        builder.addln("</update-exam-reply>");

        return builder.toString();
    }
}
