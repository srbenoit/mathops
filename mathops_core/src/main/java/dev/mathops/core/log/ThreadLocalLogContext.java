package dev.mathops.core.log;

/**
 * A thread-local variable that stores the {@code LogContext} under which the thread is operating. This is used by the
 * logging package to annotate log entries.
 */
class ThreadLocalLogContext extends ThreadLocal<LogContext> {

    /**
     * Constructs a new {@code ThreadLocalLogContext}.
     */
    ThreadLocalLogContext() {

        super();
    }

    /**
     * The initial value of the thread-local log context.
     *
     * @return the initial value
     */
    @Override
    protected LogContext initialValue() {

        return null;
    }
}
