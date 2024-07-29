package dev.mathops.app.adm.office.dialogs;

import java.awt.Container;

/**
 * A listener to be notified when pace appeals have been changed.
 */
public interface IPaceAppealsListener {

    /**
     * Gets a parent container that can be used as the owner of a dialog.
     *
     * @return the parent container
     */
    Container getParent();

    /**
     * Called by the dialog that edits accommodations when an edit is applied.
     */
    void updateAppeals();
}
