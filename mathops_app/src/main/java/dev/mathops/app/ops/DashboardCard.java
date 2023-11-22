package dev.mathops.app.ops;

import dev.mathops.app.ops.snapin.AbstractDashboardPanel;
import dev.mathops.app.ops.snapin.AbstractSnapIn;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The dashboard panel.
 */
final class DashboardCard extends JPanel implements ComponentListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 5994737305950561487L;

    /** The main window. */
    private final MainWindow mainWindow;

    /** The tiles in the dashboard. */
    private final List<TilePanel> tiles;

    /** The width of a tile. */
    private final int tileWidth;

    /** The current number of columns. */
    private int numColumns;

    /**
     * Constructs a new {@code DashboardPanel}.
     *
     * @param theSnapIns    the list of snap-ins
     * @param theMainWindow the main window
     */
    DashboardCard(final Collection<AbstractSnapIn> theSnapIns, final MainWindow theMainWindow) {

        super(new GridBagLayout());

        this.mainWindow = theMainWindow;

        final int len = theSnapIns.size();

        int prefWidth = 0;
        this.tiles = new ArrayList<>(len);
        for (final AbstractSnapIn snap : theSnapIns) {
            final AbstractDashboardPanel inner = snap.getDashboardTile();
            final Dimension pref = inner.getPreferredSize();
            prefWidth = Math.max(prefWidth, pref.width);

            final TilePanel tile = new TilePanel(snap, inner, this);
            this.tiles.add(tile);

        }
        this.tileWidth = prefWidth;
        this.numColumns = -1;

        // We do not install the tiles here - a panel resize event will cause that load based on
        // the window size and preferred sizes of the tiles
        addComponentListener(this);
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    void tick() {

        // Called on a timer thread periodically to refresh displays
        for (final TilePanel tile : this.tiles) {
            tile.tick();
        }
    }

    /**
     * Called when the component is resized.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        final int numTiles = this.tiles.size();
        final Insets insets = getInsets();
        final int myWidth = getWidth() - insets.left - insets.right;
        final int newNumColumns = Math.min(numTiles, Math.max(1, myWidth / this.tileWidth));

        if (newNumColumns != this.numColumns) {
            removeAll();
            final GridBagConstraints constraints = new GridBagConstraints();

            constraints.gridx = 0;
            constraints.gridy = 0;
            for (final TilePanel tile : this.tiles) {
                add(tile, constraints);
                ++constraints.gridx;
                if (constraints.gridx == newNumColumns) {
                    constraints.gridx = 0;
                    ++constraints.gridy;
                }
            }

            if (constraints.gridx > 0) {
                final Dimension size = new Dimension(300, 240);
                while (constraints.gridx < newNumColumns) {
                    final JPanel filler = new JPanel();
                    filler.setPreferredSize(size);
                    filler.setMinimumSize(size);
                    filler.setBackground(Color.GRAY);
                    filler.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
                    add(filler, constraints);
                    ++constraints.gridx;
                }
            }

            this.numColumns = newNumColumns;
            invalidate();
            revalidate();
            repaint();
        }
    }

    /**
     * Called when the component is moved.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        // No action
    }

    /**
     * Called when the component is shown.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Called when the component is hidden.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }

    /**
     * Called when one of the dashboard tiles is clicked.
     *
     * @param snapIn the snap-in associated with the clicked tile
     */
    private void tileClicked(final AbstractSnapIn snapIn) {

        this.mainWindow.tileClicked(snapIn);
    }

    /**
     * A dashboard panel tile.
     */
    private static final class TilePanel extends JPanel implements MouseListener {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 127358411634130263L;

        /** The color for title bar. */
        private static final Color TITLEBAR_COLOR = new Color(230, 230, 250);

        /** The snap in. */
        private final AbstractSnapIn snapIn;

        /** The owning panel to notify when the tile is clicked. */
        private final DashboardCard owner;

        /**
         * Constructs a new {@code TilePanel}.
         *
         * @param theSnapIn the snap-in this tile represents
         * @param inner     the inner panel
         * @param theOwner  an owning panel to notify when the tile is clicked
         */
        private TilePanel(final AbstractSnapIn theSnapIn, final AbstractDashboardPanel inner,
                          final DashboardCard theOwner) {

            super(new BorderLayout());

            this.snapIn = theSnapIn;
            this.owner = theOwner;

            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1),
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY)));

            final JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 2));
            add(titleBar, BorderLayout.PAGE_START);

            titleBar.setBackground(TITLEBAR_COLOR);
            titleBar.setBorder(BorderFactory.createEtchedBorder());
            final JLabel lbl = new JLabel(theSnapIn.getTitle());
            final Font boldTextFont = lbl.getFont().deriveFont(Font.BOLD, 12.0f);
            lbl.setFont(boldTextFont);
            titleBar.add(lbl);

            add(inner, BorderLayout.CENTER);

            addMouseListener(this);
        }

        /**
         * Called on a timer thread to periodically refresh displays.
         */
        void tick() {

            this.snapIn.getDashboardTile().tick();
        }

        /**
         * Called when the mouse is clicked within the tile.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseClicked(final MouseEvent e) {

            this.owner.tileClicked(this.snapIn);
        }

        /**
         * Called when the mouse button is pressed within the tile.
         *
         * @param e the mouse event
         */
        @Override
        public void mousePressed(final MouseEvent e) {

            // No action
        }

        /**
         * Called when the mouse button is released after bring pressed within the tile.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseReleased(final MouseEvent e) {

            // No action
        }

        /**
         * Called when the mouse cursor enters the tile.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseEntered(final MouseEvent e) {

            // No action
        }

        /**
         * Called when the mouse cursor exits the tile.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseExited(final MouseEvent e) {

            // No action
        }
    }
}
