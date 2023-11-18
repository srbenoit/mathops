package dev.mathops.core.ui;

import dev.mathops.core.log.Log;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * A convenience class to house the static code to change to the Nimbus UI.
 */
public final class ChangeUI {

    /**
     * Private constructor to prevent instantiation.
     */
    private ChangeUI() {

        super();
    }

    /**
     * Sets the UI to the Nimbus UI.
     */
    public static void changeUI() {

        final UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        UIManager.LookAndFeelInfo selected = null;
        for (final UIManager.LookAndFeelInfo test : lafs) {
            if ("Nimbus".equals(test.getName())) {
                selected = test;
                break;
            }
        }

        if (selected != null) {
            try {
                UIManager.setLookAndFeel(selected.getClassName());
            } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                           | UnsupportedLookAndFeelException ex) {
                Log.warning(ex);
            }
        }
    }
}
