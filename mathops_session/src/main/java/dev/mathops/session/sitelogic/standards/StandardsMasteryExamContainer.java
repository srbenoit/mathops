package dev.mathops.session.sitelogic.standards;

/**
 * The result of an attempt to create a standards mastery exam. This will either have a realized exam or a result code
 * indicating why an exam could not be created.
 */
public class StandardsMasteryExamContainer {

    /** The result code. */
    private final EStandardsMasteryExamResult result;

    /** The generated exam. */
    private final StandardsMasteryExam exam;

    /**
     * Constructs a new {@code StandardsMasteryExamContainer}.
     *
     * @param theResult the result code
     * @param theExam   the generated exam
     */
    StandardsMasteryExamContainer(final EStandardsMasteryExamResult theResult,
                                  final StandardsMasteryExam theExam) {

        this.result = theResult;
        this.exam = theExam;
    }
}
