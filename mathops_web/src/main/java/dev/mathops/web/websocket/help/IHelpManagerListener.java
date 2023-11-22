package dev.mathops.web.websocket.help;

/**
 * A listener that registers with the help manager to receive heart-beats.
 */
@FunctionalInterface
public interface IHelpManagerListener {

    /**
     * Called when the help manager sends a heart-beat (every 10 seconds).
     */
    void heartbeat();
}
