package dev.mathops.app.db.ui.configuration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A controller class that receives action events and performs the requested actions.
 */
public class Controller implements ActionListener {

    /** An action command. */
    static final String SERVERS_CMD = "SERVERS";

    /** An action command. */
    static final String PROFILES_CMD = "PROFILES";

    /** An action command. */
    static final String CODE_CTX_CMD = "CODE_CTX";

    /** An action command. */
    static final String WEB_CTX_CMD = "WEB_CTX";

    /** An action command. */
    static final String APPLY_CMD = "APPLY";

    /** An action command. */
    static final String REVERT_CMD = "REVERT";

    /**
     * Constructs a new {@code Controller}.
     */
    Controller() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        // TODO:
    }
}
