package jwabbit.log;

/**
 * Extends {@code LeveledLogger} with methods to configure logging from a {@code Properties} container.
 */
public final class ObjLogger extends LeveledLogger {

    /**
     * Constructs a new {@code ObjLogger}.
     */
    public ObjLogger() {

        super(LeveledLogger.ALL);
    }
}
