package dev.mathops.app.adm.office.dialogs;

import java.awt.Container;

/**
 * A listener to be notified when a student record has been changed.
 */
public interface IStudentListener {

    /**
     * Gets a parent container that can be used as the owner of a dialog.
     *
     * @return the parent container
     */
    Container getParent();

    /**
     * Called by the dialog that edits some student data when an edit is applied.
     */
    void updateStudent();
}
