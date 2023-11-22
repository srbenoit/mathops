package jwabbit.log;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import dev.mathops.core.CoreConstants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Handles the actual mechanics of writing log entries for a logger, including formatting log entries with dates.
 */
class ObjLoggerBase extends Synchronized {

    /** Initial allocation size for string builder for log lines. */
    private static final int INIT_BUILDER_SIZE = 200;

    /** Formatter for dates. */
    private static final String DATE_FMT = "MM/dd HH:mm:ss.SSS ";

    /** Date formatter. */
    private final SimpleDateFormat df;

    /** The writer that will write log records to configured outputs. */
    private final ObjLoggerWriter writer;

    /** A {@code StringBuilder} to assemble log messages. */
    private final StringBuilder builder;

    /** The name of this package, with trailing dot. */
    private final String pkg;

    /**
     * Constructs a new {@code ObjLoggerBase}.
     */
    ObjLoggerBase() {

        super();

        this.writer = new ObjLoggerWriter();
        this.builder = new StringBuilder(INIT_BUILDER_SIZE);
        this.df = new SimpleDateFormat(DATE_FMT, Locale.US);

        final String clsName = ObjLoggerBase.class.getName();
        final String simple = ObjLoggerBase.class.getSimpleName();
        this.pkg = clsName.substring(0, clsName.length() - simple.length());
    }

    /**
     * Gets the {@code ObjLoggerWriter} used by this logger.
     *
     * @return the {@code ObjLoggerWriter}
     */
    final ObjLoggerWriter getWriter() {

        return this.writer;
    }

    /**
     * Logs a record with the format (where '*' is filled by severity character).
     *
     * <pre>
     * MM/DD HH:mm:ss.SSS * [content] ([className]:[methodName] line [lineNumbner])
     *                      [any Throwables with stack trace]
     * </pre>
     *
     * @param severity the severity character to include in the log message
     * @param args     the list of arguments that make up the log message
     */
    final void log(final char severity, final Object... args) {

        synchronized (this.df) {
            this.builder.append(this.df.format(new Date(System.currentTimeMillis())));
        }
        this.builder.append(severity);
        this.builder.append(' ');
        appendContent(args);
        this.builder.append(' ');
        appendSource();
        addExceptionInfo("                     ", args);
        this.writer.writeMessage(this.builder.toString(), false);
        this.builder.setLength(0);
    }

    /**
     * Builds the content of the log message by concatenating the string representations of all non-{@code Throwable}
     * arguments.
     *
     * @param args the arguments to concatenate
     */
    private void appendContent(final Object... args) {

        for (final Object arg : args) {

            if (arg == null) {
                this.builder.append("null");
            } else if (!(arg instanceof Throwable)) {
                if (arg instanceof Object[]) {
                    appendContent((Object[]) arg);
                } else {
                    this.builder.append(arg);
                }
            }
        }
    }

    /**
     * Builds the exception portion of the log message by concatenating the information and stack trace of all
     * {@code Throwable} arguments, in the order in which they appear in the arguments list.
     *
     * @param indent the level to which to indent each line
     * @param args   the arguments to concatenate
     */
    private void addExceptionInfo(final String indent, final Object... args) {

        for (final Object arg : args) {

            if (arg instanceof Throwable thrown) {

                while (thrown != null) {
                    this.builder.append(indent);
                    this.builder.append(thrown.getClass().getSimpleName());

                    if (thrown.getLocalizedMessage() != null) {
                        this.builder.append(": ");
                        this.builder.append(thrown.getLocalizedMessage());
                    }

                    final StackTraceElement[] stack = thrown.getStackTrace();

                    for (final StackTraceElement stackTraceElement : stack) {
                        this.builder.append(CoreConstants.CRLF);
                        this.builder.append(indent);
                        this.builder.append(stackTraceElement.toString());
                    }

                    thrown = thrown.getCause();

                    if (thrown != null) {
                        this.builder.append(CoreConstants.CRLF);
                        this.builder.append(indent);
                        this.builder.append("CAUSED BY:");
                    }
                }
                this.builder.append(CoreConstants.CRLF);
            }
        }
    }

    /**
     * Appends the source information to the log message, in the format below.
     *
     * <pre>
     *   ([className]: [lineNumber])
     * </pre>
     * <p>
     * or
     *
     * <pre>
     *   (source unavailable for [className])
     * </pre>
     */
    private void appendSource() {

        final StackTraceElement[] stack = new IllegalArgumentException().getStackTrace();
        boolean found = false;

        for (final StackTraceElement stackTraceElement : stack) {
            if (!stackTraceElement.getClassName().startsWith(this.pkg)) {

                this.builder.append(" (");
                this.builder.append(stackTraceElement.getClassName());
                this.builder.append(".java:");
                this.builder.append(stackTraceElement.getLineNumber());
                this.builder.append(")\r\n");
                found = true;
                break;
            }
        }

        if (!found) {
            this.builder.append("(source unavailable for ").append(getClass().getName()).append(")");
        }
    }

    /**
     * Generates a string from a list of objects.
     *
     * @param args the objects
     * @return the resulting string
     */
    final String listToString(final Object... args) {

        appendContent(args);

        final String result = this.builder.toString();
        this.builder.setLength(0);

        return result;
    }
}
