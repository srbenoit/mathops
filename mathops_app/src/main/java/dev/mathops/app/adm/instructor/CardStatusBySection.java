package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.rec.TermRec;

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

    /** The data cache. */
    private final Cache cache;

    /** The center panel. */
    private final JPanel center;

    /** The panel showing the current section status. */
    private SectionStatusPane currentStatus;

    /**
     * Constructs a new {@code CardPickCourseSection}.
     *
     * @param theCache the data cache
     */
    CardStatusBySection(final Cache theCache) {

        super();

        this.cache = theCache;

        final JPanel panel = new JPanel(new BorderLayout(5, 5));

        final Color bg = getBackground();
        panel.setBackground(bg);

        final Border myBorder = getBorder();
        panel.setBorder(myBorder);

        setBackground(Skin.LT_GREEN);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        setBorder(newBorder);

        add(panel, BorderLayout.CENTER);

        final JLabel header = makeHeader("Select a course and section...", false);
        panel.add(header, BorderLayout.PAGE_START);

        this.center = makeOffWhitePanel(new BorderLayout(10, 10));
        final Border centerPad = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        this.center.setBorder(centerPad);
        panel.add(this.center, BorderLayout.CENTER);

        final JPanel centerWest = new JPanel(new StackedBorderLayout());
        final JScrollPane centerWestScroll = new JScrollPane(centerWest);
        centerWestScroll.getVerticalScrollBar().setUnitIncrement(10);
        centerWestScroll.setPreferredSize(new Dimension(150, 1));
        this.center.add(centerWestScroll, BorderLayout.LINE_START);

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

            if (this.currentStatus != null) {
                this.center.remove(this.currentStatus);
            }

            this.currentStatus = new SectionStatusPane(this.cache, course, sect);
            this.center.add(this.currentStatus, StackedBorderLayout.CENTER);

            invalidate();
            revalidate();
            repaint();
        }
    }
}
