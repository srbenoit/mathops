package dev.mathops.app.ops;

import dev.mathops.app.ops.snapin.AbstractFullPanel;
import dev.mathops.app.ops.snapin.AbstractSnapIn;
import dev.mathops.app.ops.snapin.AbstractThumbnailButton;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A panel that shows a row of thumbnails for all installed plugins, with one plugin maximized to its full window view,
 * and with a button to return to the dashboard pane.
 */
final class FullCard extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 4577642928759277793L;

    /** Action command for dashboard button. */
    private static final String DASHBOARD = "DASHBOARD";

    /** The main window. */
    private final MainWindow mainWindow;

    /** Map from snap-in name to snap-in. */
    private final Map<String, AbstractSnapIn> snapIns;

    /** The currently-displayed full-window panel. */
    private FullPanel currentFull;

    /**
     * Constructs a new {@code FullPanel}.
     *
     * @param theSnapIns    the list of snap-ins
     * @param theMainWindow the main window
     */
    FullCard(final Collection<AbstractSnapIn> theSnapIns, final MainWindow theMainWindow) {

        super(new BorderLayout());

        this.mainWindow = theMainWindow;

        this.snapIns = new HashMap<>(theSnapIns.size());
        for (final AbstractSnapIn snap : theSnapIns) {
            this.snapIns.put(snap.getTitle(), snap);
        }

        final JPanel thumbs = new JPanel(new FlowLayout(FlowLayout.CENTER, 1, 1));
        add(thumbs, BorderLayout.PAGE_END);

        thumbs.add(new GoToDashButton(this));

        for (final AbstractSnapIn snap : theSnapIns) {
            final JButton thumb = snap.getThumbnail();
            thumb.setActionCommand(snap.getTitle());
            thumb.addActionListener(this);
            thumbs.add(thumb);
        }
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    void tick() {

        // Called on a timer thread periodically to refresh displays
        if (this.currentFull != null) {
            this.currentFull.tick();
        }
    }

    /**
     * Selects a single snap-in.
     *
     * @param selected the selected snap-in
     */
    void selectSnapin(final AbstractSnapIn selected) {

        // Log.info("Selecting " + selected.getTitle());

        for (final AbstractSnapIn snap : this.snapIns.values()) {
            final AbstractThumbnailButton thumb = snap.getThumbnail();
            thumb.setHighlighted(snap == selected);
        }

        final AbstractFullPanel selectedFull = selected.getFull();

        if (this.currentFull == null) {
            final FullPanel full = new FullPanel(selected, selectedFull);
            add(full, BorderLayout.CENTER);
            this.currentFull = full;
        } else if (selectedFull != this.currentFull.getInner()) {
            remove(this.currentFull);
            final FullPanel full = new FullPanel(selected, selectedFull);
            add(full, BorderLayout.CENTER);
            this.currentFull = full;
        }

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Called when a thumbnail is clicked.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DASHBOARD.equals(cmd)) {
            this.mainWindow.returnToDashboard();
        } else if (cmd != null) {
            for (final AbstractSnapIn snap : this.snapIns.values()) {
                if (cmd.equals(snap.getTitle())) {
                    selectSnapin(snap);
                    break;
                }
            }
        }
    }

    /**
     * A dashboard panel tile.
     */
    private static final class FullPanel extends JPanel {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 127358411634130263L;

        /** The color for title bar. */
        private static final Color TITLEBAR_COLOR = new Color(230, 230, 250);

        /** The inner full-window panel. */
        private final AbstractFullPanel inner;

        /**
         * Constructs a new {@code FullPanel}.
         *
         * @param theSnapIn the snap-in this tile represents
         * @param theInner  the inner panel
         */
        private FullPanel(final AbstractSnapIn theSnapIn, final AbstractFullPanel theInner) {

            super(new BorderLayout());

            this.inner = theInner;

            setBorder(BorderFactory.createEtchedBorder());

            final JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 2));
            add(titleBar, BorderLayout.PAGE_START);

            titleBar.setBackground(TITLEBAR_COLOR);
            titleBar.setBorder(BorderFactory.createEtchedBorder());
            final JLabel lbl = new JLabel(theSnapIn.getTitle());
            final Font boldTextFont = lbl.getFont().deriveFont(Font.BOLD, 12.0f);
            lbl.setFont(boldTextFont);
            titleBar.add(lbl);

            add(this.inner, BorderLayout.CENTER);
        }

        /**
         * Gets the inner full-window panel.
         *
         * @return the inner panel
         */
        AbstractFullPanel getInner() {

            return this.inner;
        }

        /**
         * Called on a timer thread to periodically refresh displays.
         */
        void tick() {

            this.inner.tick();
        }
    }

    /**
     * A button to return to the dashboard view.
     */
    private static final class GoToDashButton extends JButton {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 8881457139024609894L;

        /** The font. */
        private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

        /** The button label. */
        private static final String BTN_LBL = "Dashboard";

        /**
         * Constructs a new {@code GoToDashButton}.
         *
         * @param listener the listener
         */
        private GoToDashButton(final ActionListener listener) {

            super();

            setPreferredSize(new Dimension(60, 80));
            setActionCommand(DASHBOARD);
            addActionListener(listener);
        }

        /**
         * Paints the button.
         *
         * @param g the {@code Graphics} to which to draw
         */
        @Override
        public void paintComponent(final Graphics g) {

            super.paintComponent(g);

            final Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            final Dimension size = getSize();
            final int w = size.width * 3 / 8;
            final int cx = size.width / 2;
            final int cy = (size.height - 10) / 2;

            g.setColor(Color.LIGHT_GRAY);
            g.drawRect(cx - w - 2, cy - w - 2, w, w);
            g.drawRect(cx - w - 2, cy + 2, w, w);
            g.drawRect(cx + 2, cy - w - 2, w, w);
            g.drawRect(cx + 2, cy + 2, w, w);

            g.setFont(FONT);
            final FontMetrics metr = g.getFontMetrics();
            final int txtW = metr.stringWidth(BTN_LBL);

            g.setColor(Color.WHITE);
            g.drawString(BTN_LBL, cx - txtW / 2, size.height - 5);
        }
    }
}
