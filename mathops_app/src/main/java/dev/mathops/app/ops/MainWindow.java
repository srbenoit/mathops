package dev.mathops.app.ops;

import dev.mathops.app.ops.snapin.AbstractSnapIn;
import dev.mathops.app.ops.snapin.activity.SystemActivitySnapIn;
import dev.mathops.app.ops.snapin.canvas.CanvasSnapIn;
import dev.mathops.app.ops.snapin.messaging.MessagingSnapIn;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Facet;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The main window for the administration application.
 */
final class MainWindow implements Runnable {

    /** The key of the dashboard panel in the card layout. */
    private static final String DASHBOARD = "DASHBOARD";

    /** The key of the full-screen panel in the card layout. */
    private static final String FULLWINDOW = "FULLSCREEN";

    /** The timer interval. */
    private static final int TIMER_DELAY_MS = 5000;

    /** Object on which to synchronize access to variables. */
    private final Object synch;

    /** The username. */
    private final String username;

    /** The database schema. */
    private final Facet schema;

    /** The live database schema. */
    private final Facet liveSchema;

    /** The Canvas access token. */
    private final String accessToken;

    /** The data cache. */
    private final Cache cache;

    /** The content panel. */
    private JPanel content;

    /** The layout manager for the content area. */
    private CardLayout cards;

    /** The dashboard card. */
    private DashboardCard dashCard;

    /** The full-window card. */
    private FullCard fullCard;

    /** Flag indicating dashboard is being shown. */
    private boolean inDashboard;

    /**
     * Constructs a new {@code MainWindow}
     *
     * @param theUsername    the username
     * @param theSchema      the database schema
     * @param theCache       the data cache
     * @param theLiveSchema  the live data database schema
     * @param theAccessToken the Canvas access token
     */
    MainWindow(final String theUsername, final Facet theSchema, final Cache theCache, final Facet theLiveSchema,
               final String theAccessToken) {

        this.synch = new Object();
        this.username = theUsername;
        this.schema = theSchema;
        this.cache = theCache;

        this.liveSchema = theLiveSchema;
        this.accessToken = theAccessToken;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final JFrame frame = new JFrame(Res.get(Res.TITLE));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.cards = new CardLayout();
        this.content = new JPanel(this.cards);
        this.content.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        frame.setContentPane(this.content);

        final String dbName = this.schema.login.database.id;
        final String server = this.schema.login.database.server.type.name();

        final HtmlBuilder windowTitle = new HtmlBuilder(100);
        windowTitle.add("Connected to database  [", dbName, "]  on server [", server, "]  (",
                this.schema.data.use, ")  as  [", this.username, "]");

        frame.setTitle(windowTitle.toString());

        // TODO: Populate contents

        final List<AbstractSnapIn> snapIns = new ArrayList<>(10);
        snapIns.add(new SystemActivitySnapIn(this.schema, this.liveSchema, this.cache));
        snapIns.add(new MessagingSnapIn(this.schema, this.liveSchema, this.cache, frame, this.accessToken));
        snapIns.add(new CanvasSnapIn(this.schema, this.liveSchema, this.cache, frame, this.accessToken));

        this.dashCard = new DashboardCard(snapIns, this);
        this.content.add(this.dashCard, DASHBOARD);

        this.fullCard = new FullCard(snapIns, this);
        this.content.add(this.fullCard, FULLWINDOW);

        this.inDashboard = true;

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice selected = env.getDefaultScreenDevice();

        final GraphicsDevice[] devs = env.getScreenDevices();
        if (devs.length > 1) {
            for (final GraphicsDevice test : devs) {
                if (test != selected) {
                    selected = test;
                    break;
                }
            }
        }

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        this.content.setPreferredSize(new Dimension(bounds.width * 6 / 7, bounds.height * 6 / 7));
        frame.pack();

        final Dimension size = frame.getSize();
        frame.setLocation(bounds.x + (bounds.width - size.width) / 2,
                bounds.y + (bounds.height - size.height) / 2);
        frame.setVisible(true);

        // Start a Swing timer to "tick" every 5 seconds to refresh displays

        tick();
        final ActionListener taskPerformer = new ActionListener() {

            /**
             * Called when the timer fires.
             *
             * @param e the action event
             */
            @Override
            public void actionPerformed(final ActionEvent e) {

                tick();
            }
        };
        final Timer timer = new Timer(TIMER_DELAY_MS, taskPerformer);
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Called when one of the dashboard tiles is clicked.
     *
     * @param snapIn the snap-in associated with the clicked tile
     */
    void tileClicked(final AbstractSnapIn snapIn) {

        synchronized (this.synch) {
            this.fullCard.selectSnapin(snapIn);
            this.cards.show(this.content, FULLWINDOW);
            this.inDashboard = false;
        }
    }

    /**
     * Returns to the dashboard display.
     */
    void returnToDashboard() {

        synchronized (this.synch) {
            this.cards.show(this.content, DASHBOARD);
            this.inDashboard = true;
        }
    }

    /**
     * Called on a timer thread to periodically refresh displays.
     */
    private void tick() {

        synchronized (this.synch) {
            if (this.inDashboard) {
                this.dashCard.tick();
            } else {
                this.fullCard.tick();
            }
        }
    }
}
