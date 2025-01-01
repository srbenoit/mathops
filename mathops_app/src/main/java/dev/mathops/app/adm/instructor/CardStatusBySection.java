package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

/**
 * A card to display all current-term course sections for Precalculus courses, and then to display summary status for a
 * chosen section.
 */
final class CardStatusBySection extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 818369202377595216L;

    /** The owning admin pane. */
    private final TopPanelInstructor owner;

    /** The data cache. */
    private final Cache cache;

    /** The active term key. */
    private final TermKey activeTermKey;

    /** The fixed data. */
    private final UserData fixed;

    /**
     * Constructs a new {@code CardPickCourseSection}.
     *
     * @param theOwner the owning top-level student panel
     * @param theCache the data cache
     * @param theFixed the fixed data
     */
    CardStatusBySection(final TopPanelInstructor theOwner, final Cache theCache, final UserData theFixed) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));

        final Color bg = getBackground();
        final Color myBackground = bg;
        panel.setBackground(myBackground);

        final Border myBorder = getBorder();
        panel.setBorder(myBorder);

        setBackground(Skin.LT_GREEN);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        setBorder(newBorder);

        add(panel, BorderLayout.CENTER);

        this.owner = theOwner;
        this.cache = theCache;
        this.fixed = theFixed;

        final JLabel header = makeHeader("Select a course and section...", false);
        panel.add(header, BorderLayout.PAGE_START);

        final JPanel center = makeOffWhitePanel(new BorderLayout(10, 10));
        final Border centerPad = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        center.setBorder(centerPad);
        panel.add(center, BorderLayout.CENTER);

        final JPanel centerWest = new JPanel(new StackedBorderLayout());
        final JScrollPane centerWestScroll = new JScrollPane(centerWest);
        centerWestScroll.setPreferredSize(new Dimension(150, 1));
        center.add(centerWestScroll, BorderLayout.LINE_START);

        try {
            final TermRec active = TermLogic.get(this.cache).queryActive(this.cache);
            final List<RawCsection> sections = RawCsectionLogic.queryByTerm(this.cache, active.term);
            sections.sort(null);

            String currentCourse = null;

            for (final RawCsection row : sections) {
                final String course = row.course;

                if (course.startsWith("M 100") || "M 384".equals(course) || "M 1170".equals(course)
                    || "M 1180".equals(course) || "M 1240".equals(course) || "M 1250".equals(course)
                    || "M 1260".equals(course)) {
                    continue;
                }

                final String sect = row.sect;

                if ("550".equals(sect)) {
                    continue;
                }

                if (!course.equals(currentCourse)) {
                    final String name = course.replace("M ", " MATH ");
                    final JLabel courseTitle = makeLabelMedium(name);
                    centerWest.add(courseTitle, StackedBorderLayout.NORTH);
                    currentCourse = course;
                }

                final String cmd = course + "+" + sect;

                final String sectName = "Section " + sect;
                final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
                final JButton button = new JButton(sectName);
                button.setActionCommand(cmd);
                button.addActionListener(this);
                flow.add(button);
                centerWest.add(flow, StackedBorderLayout.NORTH);

            }
        } catch (final SQLException ex) {
            Log.warning("failed to query course sections", ex);
        }

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
     * Called when the panel is shown to refresh the list of courses and sections.
     */
    void reset() {

        // TODO:
    }

    /**
     * Called when the button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        final int plus = cmd.indexOf('+');

        if (plus > 0) {
            final String course = cmd.substring(0, plus);
            final String sect = cmd.substring(plus + 1);
        }
    }
}
