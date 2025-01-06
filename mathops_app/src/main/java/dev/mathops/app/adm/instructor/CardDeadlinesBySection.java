package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

    /** The fixed data. */
    private final UserData fixed;

    /** The center panel. */
    private final JPanel center;

    /** The panel showing the current track deadlines. */
    private TrackDeadlinesPane currentDeadlines;

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
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        setBorder(newBorder);

        add(panel, BorderLayout.CENTER);

        final JLabel headerLabel = makeHeader("Select a pace and track...", false);
        panel.add(headerLabel, BorderLayout.PAGE_START);

        this.center = new JPanel(new BorderLayout(10, 10));
        this.center.setBackground(Skin.OFF_WHITE_RED);
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
            final List<RawMilestone> milestones = RawMilestoneLogic.getAllMilestones(this.cache, active.term);
            milestones.sort(null);

            final Map<Integer, Set<String>> paceTracks = new HashMap<>(5);
            for (final RawMilestone ms : milestones) {
                final Set<String> tracks = paceTracks.computeIfAbsent(ms.pace, x -> new TreeSet<>());
                tracks.add(ms.paceTrack);
            }

            for (final Map.Entry<Integer, Set<String>> entry : paceTracks.entrySet()) {
                final String paceStr = entry.getKey().toString();
                final String paceName = paceStr + "-course pace";
                final JLabel paceTitle = makeLabelMedium(paceName);
                final JPanel titleFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
                titleFlow.add(paceTitle);
                centerWest.add(titleFlow, StackedBorderLayout.NORTH);

                for (final String track : entry.getValue()) {
                    final String cmd = paceStr + "+" + track;

                    final String trackName = "Track " + track;
                    final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 2));
                    final JButton button = new JButton(trackName);
                    button.setActionCommand(cmd);
                    button.addActionListener(this);
                    flow.add(button);
                    centerWest.add(flow, StackedBorderLayout.NORTH);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("failed to query milestones", ex);
        }
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

        if (this.currentDeadlines != null) {
            this.center.remove(this.currentDeadlines);

            invalidate();
            revalidate();
            repaint();
        }
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
            if (this.currentDeadlines != null) {
                this.center.remove(this.currentDeadlines);
            }

            final String paceStr = cmd.substring(0, plus);
            try {
                final int pace = Integer.parseInt(paceStr);
                final String track = cmd.substring(plus + 1);

                this.currentDeadlines = new TrackDeadlinesPane(this.cache, pace, track);
                this.center.add(this.currentDeadlines, StackedBorderLayout.CENTER);
            } catch (final NumberFormatException ex) {
                Log.warning("Failed to parse pace", ex);
            }

            invalidate();
            revalidate();
            repaint();
        }
    }
}
