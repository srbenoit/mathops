package jwabbit;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.text.JTextComponent;

/**
 * An action representing a request to copy the current answer to a Swing text component.
 */
public class CalcCopyTextAction implements ICalcAction {

    /** The field to which to copy the text. */
    private final JTextComponent field;

    /**
     * Constructs a new {@code CalcCopyTextAction}.
     *
     * @param theField the field to which to copy the text
     */
    public CalcCopyTextAction(final JTextComponent theField) {

        this.field = theField;
    }

    /**
     * Gets the type of action.
     *
     * @return the action type
     */
    @Override
    public final ECalcAction getType() {

        return ECalcAction.COPY_TEXT;
    }

    /**
     * Gets the field to which to copy the answer.
     *
     * @return the field
     */
    public final JTextComponent getField() {

        return this.field;
    }
}
