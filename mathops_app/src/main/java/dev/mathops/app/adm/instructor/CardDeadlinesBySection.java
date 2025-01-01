package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;

/**
 * A card to display all current-term course sections for Precalculus courses, and then to display summary status for a
 * chosen section.
 */
final class CardDeadlinesBySection extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 818369202377595217L;

    /** The owning admin pane. */
    private final TopPanelInstructor owner;

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey activeTermKey;

    /** The fixed data. */
    private final UserData fixed;

    /**
     * Constructs a new {@code CardDeadlinesBySection}.
     *
     * @param theOwner the owning top-level student panel
     * @param theCache the data cache
     * @param theFixed the fixed data
     */
    CardDeadlinesBySection(final TopPanelInstructor theOwner, final Cache theCache, final UserData theFixed) {

        super();

        this.owner = theOwner;
        this.cache = theCache;
        this.fixed = theFixed;

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_RED);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        panel.add(makeHeader("Select a pace and track to update...", false), BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_RED);
        final Border centerPad = BorderFactory.createEmptyBorder(20, 10, 20, 10);
        center.setBorder(centerPad);
        panel.add(center, BorderLayout.CENTER);

        final JPanel centerWest = makeOffWhitePanel(new BorderLayout());
        center.add(centerWest, BorderLayout.LINE_START);

        final Border etched = BorderFactory.createEtchedBorder();

        //
        //
        //
        //

        TermKey key = null;
        try {
            final TermRec term = this.cache.getSystemData().getActiveTerm();
            if (term == null) {
                Log.warning("No active term found");
            } else {
                key = term.term;
            }
        } catch (final SQLException ex) {
            Log.warning("Unable to query active term", ex);
        }
        this.activeTermKey = key;
    }

    /**
     * Called when the panel is shown to set focus in the student ID field.
     */
    void focus() {

        // No action
    }

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    void clearDisplay() {

        // No action
    }

    /**
     * Called when the button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        // TODO:
    }
}
