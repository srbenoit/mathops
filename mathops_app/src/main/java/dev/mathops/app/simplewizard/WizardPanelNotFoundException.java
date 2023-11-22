package dev.mathops.app.simplewizard;

import java.io.Serial;

/**
 * An exception that is thrown when a wizard is told to show a panel that it does not contain.
 */
class WizardPanelNotFoundException extends RuntimeException {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2204428981458683411L;

    /**
     * Construct a new {@code WizardPanelNotFoundException}.
     */
    WizardPanelNotFoundException() {

        super();
    }
}
