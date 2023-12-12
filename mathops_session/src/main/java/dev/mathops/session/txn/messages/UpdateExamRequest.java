package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.session.txn.handlers.AbstractHandlerBase;
import dev.mathops.session.txn.handlers.UpdateExamHandler;

/**
 * A network message to submit an update to an in-progress exam, or to submit a completed exam for grading.
 */
public final class UpdateExamRequest extends AbstractRequestBase {

    /** The identifier reference of the exam being updated. */
    public final String identifierReference;

    /** The realization time from the exam being updated. */
    public final Long realizationTime;

    /** The presentation time from the exam being updated. */
    public final Long presentationTime;

    /** The timestamp when the update was generated. */
    public Long updateTime;

    /** An array of answers. */
    private Object[][] answers;

    /** True if exam is being submitted for grading; false otherwise. */
    public final boolean finalize;

    /** The student ID taking the exam. */
    public final String studentId;

    /** True if exam is being supervised by a proctor (outside a proctored testing center). */
    public final boolean proctored;

    /** True if this exam was recovered after an error sending. */
    private boolean recovered;

    /**
     * Constructs a new {@code UpdateExamRequest}.
     *
     * @param theStudentId           the ID of the student taking the exam
     * @param theIdentifierRef the identifier reference of the exam being updated
     * @param theRealizationTime     the realization time from the exam being updated
     * @param theAnswers             the list of answers as they currently exist
     * @param isFinalized            true if exam is being submitted for grading; false otherwise
     * @param isProctored            true if exam is being supervised by a proctor (outside a proctored testing center)
     */
    public UpdateExamRequest(final String theStudentId, final String theIdentifierRef, final Long theRealizationTime,
                             final Object[][] theAnswers, final boolean isFinalized, final boolean isProctored) {

        super();

        this.studentId = theStudentId;
        this.identifierReference = theIdentifierRef;
        this.realizationTime = theRealizationTime;
        this.presentationTime = null;
        this.answers = theAnswers;
        this.finalize = isFinalized;
        this.proctored = isProctored;
    }

    /**
     * Constructs a new {@code UpdateExamRequest}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    public UpdateExamRequest(final char[] xml) throws IllegalArgumentException {
        super();

        final String message = extractMessage(xml, xmlTag());

        this.machineId = extractField(message, "machine-id");
        this.studentId = extractField(message, "student");
        this.identifierReference = extractField(message, "exam-ref");

        Long realizeTime = null;
        String value = extractField(message, "realized");
        if (value != null) {
            try {
                realizeTime = Long.valueOf(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_REALIZED));
            }
        }
        this.realizationTime = realizeTime;

        Long curSect = null;
        value = extractField(message, "cur-section");
        if (value != null) {
            try {
                curSect = Long.valueOf(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_CUR_SECTION));
            }
        }

        Long curProb = null;
        value = extractField(message, "cur-problem");
        if (value != null) {
            try {
                curProb = Long.valueOf(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_CUR_PROBLEM));
            }
        }

        Long presentTime = null;
        value = extractField(message, "presented");
        if (value != null) {
            try {
                presentTime = Long.valueOf(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_PRESENTED));
            }
        }
        this.presentationTime = presentTime;

        value = extractField(message, "updated");

        if (value != null) {
            try {
                this.updateTime = Long.valueOf(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_UPDATED));
            }
        }

        int count = 0;
        value = extractField(message, "num-answers");

        if (value != null) {
            try {
                count = Integer.parseInt(value);
            } catch (final NumberFormatException e) {
                Log.warning(Res.get(Res.BAD_NUM_ANSWERS));
            }
        }

        if (count == 0) {
            if ((curSect != null) || (curProb != null) || (this.presentationTime != null)) {
                this.answers = new Object[1][];
                this.answers[0] = new Object[4];
                this.answers[0][0] = curSect;
                this.answers[0][1] = curProb;
                this.answers[0][2] = this.presentationTime;
            }
        } else {
            this.answers = new Object[count + 1][];
            this.answers[0] = new Object[4];
            this.answers[0][0] = curSect;
            this.answers[0][1] = curProb;
            this.answers[0][2] = this.presentationTime;

            final String sub = extractField(message, "answers");

            if (sub != null) {
                final String[] list = extractFieldList(sub, "answer");
                if (list != null) {
                    if (count > list.length) {
                        count = list.length;
                    }

                    for (int i = 0; i < count; i++) {

                        if (!list[i].isEmpty()) {
                            this.answers[i + 1] = extractValueList(list[i]);
                        }
                    }
                }
            }
        }

        value = extractField(message, "finalize");
        this.finalize = "TRUE".equalsIgnoreCase(value);

        value = extractField(message, "proctored");
        this.proctored = "TRUE".equalsIgnoreCase(value);

        value = extractField(message, "recovered");
        this.recovered = "TRUE".equalsIgnoreCase(value);
    }

    /**
     * Gets the current list of answers.
     *
     * @return the array of answers
     */
    public Object[][] getAnswers() {

        return this.answers;
    }

    /**
     * Sets the flag that indicates this exam was recovered after a failure.
     */
    public void indicateRecovered() {

        this.recovered = true;
    }

    /**
     * Tests whether the exam was recovered after a failure.
     *
     * @return {@code true} if the exam was recovered; {@code false} if not
     */
    public boolean isRecovered() {

        return this.recovered;
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "update-exam-request";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<update-exam-request>");

        if (this.studentId != null) {
            builder.addln(" <student>", this.studentId, "</student>");
        }

        if (this.identifierReference != null) {
            builder.addln(" <exam-ref>", this.identifierReference, "</exam-ref>");
        }

        if (this.realizationTime != null) {
            builder.addln(" <realized>", this.realizationTime, "</realized>");
        }

        if (this.updateTime != null) {
            builder.addln(" <updated>", this.updateTime, "</updated>");
        }

        if (this.answers != null) {
            final int numAns = this.answers.length;

            if (numAns > 0 && this.answers[0].length == 4) {

                if (this.answers[0][0] != null) {
                    builder.addln(" <cur-section>", this.answers[0][0], "</cur-section>");
                }

                if (this.answers[0][1] != null) {
                    builder.addln(" <cur-problem>", this.answers[0][1], "</cur-problem>");
                }

                if (this.answers[0][2] != null) {
                    builder.addln(" <presented>", this.answers[0][2], "</presented>");
                }
            }

            if (numAns > 1) {
                builder.addln(" <num-answers>", Integer.toString(numAns - 1), "</num-answers>");

                builder.addln(" <answers>");

                // Include one answer line per problem, even if unanswered
                for (int i = 1; i < numAns; ++i) {
                    builder.add("  <answer>");

                    if (this.answers[i] != null) {

                        final int innerLen = this.answers[i].length;
                        for (int j = 0; j < innerLen; j++) {

                            if (this.answers[i][j] == null) {
                                continue;
                            }

                            if ("null".equals(this.answers[i][j])) {
                                continue;
                            }

                            if (this.answers[i][j] instanceof Long) {
                                builder.add("<long>", this.answers[i][j], "</long>");
                            } else if (this.answers[i][j] instanceof Double) {
                                builder.add("<double>", this.answers[i][j], "</double>");
                            } else if (this.answers[i][j] instanceof String) {
                                builder.add("<string>", this.answers[i][j], "</string>");
                            } else {
                                Log.warning(Res.fmt(Res.BAD_ANSWER, this.answers[i][j].getClass().getName()));
                            }
                        }
                    }

                    builder.addln("</answer>");
                }

                builder.addln("</answers>");
            }
        }

        if (this.finalize) {
            builder.addln(" <finalize>true</finalize>");
        }

        if (this.proctored) {
            builder.addln(" <proctored>true</proctored>");
        }

        if (this.recovered) {
            builder.addln(" <recovered>true</recovered>");
        }

        printMachineId(builder);
        builder.addln("</update-exam-request>");

        return builder.toString();
    }

    /**
     * Generates a handler that can process this message.
     *
     * @param dbProfile the database profile in which the handler will operate
     * @return a handler that can process the message
     */
    @Override
    public AbstractHandlerBase createHandler(final DbProfile dbProfile) {

        return new UpdateExamHandler(dbProfile);
    }
}
