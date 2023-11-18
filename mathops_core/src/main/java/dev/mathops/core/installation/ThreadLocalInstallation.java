package dev.mathops.core.installation;

/**
 * A thread-local variable that stores the {@code Installation} under which the thread is operating. This is used by the
 * logging package to obtain the log directory and logging configuration parameters.
 */
class ThreadLocalInstallation extends ThreadLocal<Installation> {

    /**
     * Constructs a new {@code ThreadLocalInstallation}.
     */
    ThreadLocalInstallation() {

        super();
    }

    /**
     * The initial value of the thread-local installation.
     *
     * @return the initial value
     */
    @Override
    protected Installation initialValue() {

        return null;
    }
}
