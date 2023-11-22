package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawClientPcLogic;
import dev.mathops.db.rawrecord.RawClientPc;
import dev.mathops.session.scramsha256.ScramClientStub;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;

/**
 * The "Testing" pane.
 */
public class TestingTabPane extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String MAP_CMD = "MAP";

    /** A button action command. */
    private static final String MANAGE_CMD = "MANAGE";

    /** A button action command. */
    private static final String ISSUE_CMD = "ISSUE";

    /** A button action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9054536124109728232L;

    /** The data cache. */
    private final Cache cache;

    /** The client stub. */
    private final ScramClientStub stub;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The "map" card. */
    private final TestingMapCard mapCard;

    /** The "manage" card. */
    private final TestingManageCard manageCard;

    /** The "issue" card. */
    private final TestingIssueCard issueCard;

    /** The "issue" card. */
    private final TestingCancelCard cancelCard;

    /**
     * Constructs a new {@code TestingTabPane}.
     *
     * @param theCache         the data cache
     * @param theStub          the web services client stub
     * @param fixed            the fixed data
     * @param theFrame         the owning frame
     */
    public TestingTabPane(final Cache theCache, final ScramClientStub theStub,
                          final Object theRenderingHint, final FixedData fixed, final JFrame theFrame) {

        // Functions:
        // [ Map ]
        // [ Manage ]
        // [ Issue ]

        super(new BorderLayout(5, 5));
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        this.cache = theCache;
        this.stub = theStub;

        setBackground(Skin.OFF_WHITE_GRAY);
        setBorder(BorderFactory.createCompoundBorder( //
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel menu = new JPanel();
        menu.setBackground(Skin.OFF_WHITE_GRAY);
        menu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));

        if (fixed.getClearanceLevel("TST_MAP") != null) {
            menu.add(makeTopButton("Map", MAP_CMD, Skin.LT_GREEN));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("TST_MANAG") != null) {
            menu.add(makeTopButton("Manage", MANAGE_CMD, Skin.LT_RED));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("TST_ISSUE") != null) {
            menu.add(makeTopButton("Issue Exam", ISSUE_CMD, Skin.LT_CYAN));
            menu.add(new JLabel(CoreConstants.SPC));

            menu.add(makeTopButton("Cancel Exam", CANCEL_CMD, Skin.LT_MAGENTA));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        add(menu, BorderLayout.NORTH);

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.OFF_WHITE_GREEN);
        add(this.cardPane, BorderLayout.CENTER);

        this.mapCard = new TestingMapCard(theCache);
        this.cardPane.add(this.mapCard, MAP_CMD);

        this.manageCard = new TestingManageCard(theCache, theStub, this);
        this.cardPane.add(this.manageCard, MANAGE_CMD);

        this.issueCard = new TestingIssueCard(theCache, theFrame);
        this.cardPane.add(this.issueCard, ISSUE_CMD);

        this.cancelCard = new TestingCancelCard(theCache);
        this.cardPane.add(this.cancelCard, CANCEL_CMD);

        this.cards.show(this.cardPane, MAP_CMD);
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title            the button title
     * @param command          the action command
     * @param background       the background color
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background) {

        final JButton btn = new JButton(title);
        btn.setActionCommand(command);
        btn.addActionListener(this);
        btn.setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        menuBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        menuBox.add(btn, BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (MAP_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, MAP_CMD);
            this.mapCard.refresh();
        } else if (MANAGE_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, MANAGE_CMD);
            this.manageCard.refresh();
        } else if (ISSUE_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, ISSUE_CMD);
            this.issueCard.reset();
        } else if (CANCEL_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, CANCEL_CMD);
            this.cancelCard.reset();
        }
    }

    /**
     * Disables (sets usage to paper-only) a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    public void disableStations(final String[] stationIds) {

        try (final Statement s = this.cache.conn.createStatement()) {

            for (final String id : stationIds) {

                final String sql1 = SimpleBuilder.concat(
                        "update client_pc set pc_usage='P' where testing_center_id='1' ",
                        "and station_nbr='", id, "' and pc_usage = 'O'");

                final String sql2 = SimpleBuilder.concat(//
                        "UPDATE client_pc SET current_status='5'",
                        " WHERE testing_center_id='1'",
                        "   AND station_nbr='", id,
                        "' AND current_status='4'");

                s.executeUpdate(sql1);
                s.executeUpdate(sql2);
            }

            this.cache.conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Enables (sets usage to Online) on a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    public void enableStations(final String[] stationIds) {

        try (final Statement s = this.cache.conn.createStatement()) {

            for (final String id : stationIds) {

                final String sql1 = SimpleBuilder.concat(
                        "update client_pc set pc_usage='O' where testing_center_id='1' ",
                        "and station_nbr='", id, "' and pc_usage = 'P'");

                final String sql2 = SimpleBuilder.concat( //
                        "UPDATE client_pc SET current_status='4'",
                        " WHERE testing_center_id='1' ",
                        "   AND station_nbr='", id,
                        "'  AND current_status='5'");

                s.executeUpdate(sql1);
                s.executeUpdate(sql2);
            }

            this.cache.conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Powers on a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    public void powerOnStations(final String[] stationIds) {

        final int recent = TemporalUtils.secondOfDay(LocalTime.now()) - 40;

        try {
            final RawClientPc[] toPowerOn = findStations(stationIds);

            // Power those stations on
            for (final RawClientPc station : toPowerOn) {

                if (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)
                        && station.lastPing != null && station.lastPing.intValue() > recent) {
                    // Station is already on and reporting - skip
                    continue;
                }

                Log.info("Powering on ", station.stationNbr);

                try {
                    final URL url = new URL(this.stub.siteUrl //
                            + "testing-power-station-on.ws?token=" + this.stub.getToken()
                            + "&computer-id=" + station.computerId);

                    final URLConnection conn = url.openConnection();
                    final Object content = conn.getContent();
                    if (content == null) {
                        Log.warning("Server response from 'testing-power-station-on.ws' was null");
                    } else if (content instanceof InputStream) {
                        try (final InputStream in = (InputStream) content) {
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                            final byte[] buffer = new byte[1024];
                            int count = in.read(buffer);
                            while (count > 0) {
                                baos.write(buffer, 0, count);
                                count = in.read(buffer);
                            }

                            final String result = baos.toString();
                            if (!"OK".equals(result)) {
                                Log.info(result);
                            }
                        }
                    } else {
                        Log.warning("Server response from 'testing-power-station-on.ws' was ",
                                content.getClass().getName());
                    }
                } catch (final IOException ex) {
                    Log.warning(ex);
                }

                try {
                    Thread.sleep(100L);
                } catch (final InterruptedException ex) {
                    Log.warning(ex);
                    Thread.currentThread().interrupt();
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query testing stations.", ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Powers off a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    public void powerOffStations(final String[] stationIds) {

        try {
            final RawClientPc[] toPowerOff = findStations(stationIds);

            // Power those stations off (skip any that are currently in use)
            for (final RawClientPc station : toPowerOff) {

                if (station.currentStuId == null) {
                    Log.info("Powering off ", station.stationNbr);

                    try {
                        final URL url = new URL(this.stub.siteUrl //
                                + "testing-power-station-off.ws?token=" + this.stub.getToken()
                                + "&computer-id=" + station.computerId);

                        final URLConnection conn = url.openConnection();
                        final Object content = conn.getContent();
                        if (content == null) {
                            Log.warning(
                                    "Server response from 'testing-power-station-off.ws' was null");
                        } else if (content instanceof InputStream) {
                            try (final InputStream in = (InputStream) content) {
                                final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                                final byte[] buffer = new byte[1024];
                                int count = in.read(buffer);
                                while (count > 0) {
                                    baos.write(buffer, 0, count);
                                    count = in.read(buffer);
                                }

                                final String result = baos.toString();
                                if (!"OK".equals(result)) {
                                    Log.info(result);
                                }
                            }
                        } else {
                            Log.warning("Server response from 'testing-power-station-off.ws' was ",
                                    content.getClass().getName());
                        }
                    } catch (final IOException ex) {
                        Log.warning(ex);
                    }
                } else {
                    Log.warning("Skipping station ", station.stationNbr, " (currently in use)");
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query testing stations.", ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Given a list of station IDs, assumed to be in testing centers 1 (main) or 4 (quiet testing), retrieves the {@code
     * RawClientPc} records for each and returns that list.
     *
     * @param stationIds the station numbers
     * @return the array of corresponding {@code RawClientPc} records (if a station number cannot be found, this array
     *         will have {@code null} at the corresponding index)
     * @throws SQLException if there is an error accessing the database
     */
    private RawClientPc[] findStations(final String[] stationIds) throws SQLException {

        final int numStations = stationIds.length;
        final RawClientPc[] result = new RawClientPc[numStations];

        final List<RawClientPc> stations = RawClientPcLogic.INSTANCE.queryAll(this.cache);

        for (int i = 0; i < numStations; ++i) {
            for (final RawClientPc station : stations) {
                final String center = station.testingCenterId;
                if (station.stationNbr.equals(stationIds[i])
                        && ("1".equals(center) || "4".equals(center))) {
                    result[i] = station;
                    break;
                }
            }
        }

        return result;
    }
}
