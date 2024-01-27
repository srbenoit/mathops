package dev.mathops.app.assessment.examprinter;

import dev.mathops.commons.res.ResBundle;

import java.util.Locale;

/**
 * Localized resources.
 */
final class Res extends ResBundle {

    /** An incrementing index for resource keys. */
    private static int index = 1;

    /** A resource key. */
    static final String APP_TITLE = key(index++);

    /** A resource key. */
    static final String SEND_TO_PRINTER = key(index++);

    /** A resource key. */
    static final String GENERATE_LATEX = key(index++);

    /** A resource key. */
    static final String WHAT_TO_DO = key(index++);

    /** A resource key. */
    static final String FIND_INSTR_PARENT = key(index++);

    /** A resource key. */
    static final String SELECT_EXAM = key(index++);

    /** A resource key. */
    static final String PRINTING = key(index++);

    /** A resource key. */
    static final String GENERATING_LATEX = key(index++);

    /** A resource key. */
    static final String SAVE_LATEX_FILES = key(index++);

    /** A resource key. */
    static final String LOADING_EXAM = key(index++);

    /** A resource key. */
    static final String EXAM_LOAD_FAILED = key(index++);

    /** A resource key. */
    static final String LOADING_QUESTIONS = key(index++);

    /** A resource key. */
    static final String RANDOMIZING = key(index++);

    /** A resource key. */
    static final String RANDOMIZING_FAILED = key(index++);

    /** A resource key. */
    static final String LAYING_OUT = key(index++);

    /** A resource key. */
    static final String STARTING_PRINT = key(index++);

    /** A resource key. */
    static final String PRINTING_EXAM = key(index++);

    /** A resource key. */
    static final String PRINTING_ANSWERS = key(index++);

    /** A resource key. */
    static final String GENERTING_EXAM_TEX = key(index++);

    /** A resource key. */
    static final String GENERTING_ANSWER_TEX = key(index++);

    /** A resource key. */
    static final String GENERTING_SOLUTION_TEX = key(index++);

    /** A resource key. */
    static final String COMPLETE = key(index++);

    /** A resource key. */
    static final String PAGE = key(index++);

    /** A resource key. */
    static final String TIME_LIMIT = key(index++);

    /** A resource key. */
    static final String HOUR = key(index++);

    /** A resource key. */
    static final String HOURS = key(index++);

    /** A resource key. */
    static final String MINUTE = key(index++);

    /** A resource key. */
    static final String MINUTES = key(index++);

    /** A resource key. */
    static final String SERIAL = key(index++);

    /** A resource key. */
    static final String HAD_NO_QUESTION = key(index++);

    /** A resource key. */
    static final String ENTER_ANS_HERE = key(index++);

    /** A resource key. */
    static final String ANSWER_KEY_FOR = key(index++);

    /** A resource key. */
    static final String EXAM_ID = key(index++);

    /** A resource key. */
    static final String GENERATED = key(index++);

    /** A resource key. */
    static final String CORRECT_ANS_IS = key(index++);

    /** A resource key. */
    static final String ALLOWED_VARIANCE_IS = key(index++);

    /** A resource key. */
    static final String OVERWRITE = key(index++);

    /** A resource key. */
    static final String OVERWRITE_ALL = key(index++);

    /** A resource key. */
    static final String CANCEL = key(index++);

    /** A resource key. */
    static final String FILE_EXISTS = key(index++);

    /** A resource key. */
    static final String LATEX_FILE_GEN = key(index++);

    /** A resource key. */
    static final String CANT_WRITE_LATEX = key(index++);

    //

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = { //
            {APP_TITLE, "Exam Printer"},
            {SEND_TO_PRINTER, "Send to Printer"},
            {GENERATE_LATEX, "Generate LaTeX"},
            {WHAT_TO_DO, "What would you like to do:"},
            {FIND_INSTR_PARENT, "Find the folder containing 'instruction'"},
            {SELECT_EXAM, "Select an Exam"},
            {PRINTING, "Printing Exam..."},
            {GENERATING_LATEX, "Generating LaTeX..."},
            {SAVE_LATEX_FILES, "Save Generated LaTeX Files"},
            {LOADING_EXAM, "Loading Exam"},
            {EXAM_LOAD_FAILED, "Failed to load exam"},
            {LOADING_QUESTIONS, "Loading Questions"},
            {RANDOMIZING, "Randomizing Exam"},
            {RANDOMIZING_FAILED, "Unable to randomize exam"},
            {LAYING_OUT, "Performing Layout"},
            {STARTING_PRINT, "Printing"},
            {PRINTING_EXAM, "Printing Exam"},
            {PRINTING_ANSWERS, "Printing Answer Key"},
            {GENERTING_EXAM_TEX, "Generating Exam LaTeX"},
            {GENERTING_ANSWER_TEX, "Generating Answer Key LaTeX"},
            {GENERTING_SOLUTION_TEX, "Generating Solutions LaTeX"},
            {COMPLETE, "Complete"},
            {PAGE, "Page"},
            {TIME_LIMIT, "TIME LIMIT:"},
            {HOUR, "hour"},
            {HOURS, "hours"},
            {MINUTE, "minute"},
            {MINUTES, "minutes"},
            {SERIAL, "SERIAL NUMBER:"},
            {HAD_NO_QUESTION, "had no question"},
            {ENTER_ANS_HERE, "Enter your answer here:"},
            {ANSWER_KEY_FOR, "ANSWER KEY FOR"},
            {EXAM_ID, "EXAM ID:"},
            {GENERATED, "GENERATED:"},
            {CORRECT_ANS_IS, "The correct answer is :"},
            {ALLOWED_VARIANCE_IS, "The allowed variance is :"},
            {OVERWRITE, "Overwrite"},
            {OVERWRITE_ALL, "Overwrite All"},
            {CANCEL, "Cancel"},
            {FILE_EXISTS, "File ''{0}'' exists."},
            {LATEX_FILE_GEN, "LaTeX File Generation"},
            {CANT_WRITE_LATEX, "Unable to write LaTeX file:"},

            //
    };

    /** The singleton instance. */
    private static final Res instance = new Res();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Res() {

        super(Locale.US, EN_US);
    }

    /**
     * Gets the message with a specified key using the current locale.
     *
     * @param key the message key
     * @return the best-matching message, an empty string if none is registered (never {@code null})
     */
    static String get(final String key) {

        return instance.getMsg(key);
    }

    /**
     * Retrieves the message with a specified key, then uses a {@code MessageFormat} to format that message pattern with
     * a collection of arguments.
     *
     * @param key       the message key
     * @param arguments the arguments, as for {@code MessageFormat}
     * @return the formatted string (never {@code null})
     */
    static String fmt(final String key, final Object... arguments) {

        return instance.formatMsg(key, arguments);
    }
}
