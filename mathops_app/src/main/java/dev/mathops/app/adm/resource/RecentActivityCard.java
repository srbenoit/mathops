package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A card panel to display recent activity.
 */
final class RecentActivityCard extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2745365421747593671L;

    /** The data cache. */
    private final Cache cache;

    /** The history table. */
    private final JTableResourceActivity table;

    /** The refresh button. */
    private final JButton refreshBtn;

    /** An error message. */
    private final JLabel error1;

    /** An error message. */
    private final JLabel error2;

    /**
     * Constructs a new {@code RecentActivityCard}.
     *
     * @param theCache         the data cache
     */
    RecentActivityCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_YELLOW);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_YELLOW);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Today's Resource Activity", false), BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_YELLOW);
        panel.add(center, BorderLayout.CENTER);

        // "Outstanding Resources" header
        // Table of outstanding resources
        // [Refresn] button
        // Error messages

        // In this panel's "center" position:
        // inner1 (header in N, errors in S, inner2 in center)
        // inner2 (table in center, [Done] in S )

        final JPanel inner1 = new JPanel(new BorderLayout(10, 10));
        inner1.setBackground(Skin.OFF_WHITE_YELLOW);
        final JPanel inner2 = new JPanel(new BorderLayout(10, 10));
        inner2.setBackground(Skin.OFF_WHITE_YELLOW);

        inner1.add(inner2, BorderLayout.CENTER);
        center.add(inner1, BorderLayout.CENTER);

        //

        //

        this.table = new JTableResourceActivity();
        final JScrollPane scroll = new JScrollPane(this.table);
        scroll.getViewport().setBackground(Skin.WHITE);
        scroll.setBackground(Skin.WHITE);
        inner2.add(scroll, BorderLayout.CENTER);

        scroll.setPreferredSize(this.table.getPreferredScrollSize(scroll, 3));

        //

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_YELLOW);
        this.refreshBtn = new JButton("Refresh");
        this.refreshBtn.setFont(Skin.BIG_BUTTON_16_FONT);
        this.refreshBtn.setActionCommand(REFRESH);
        this.refreshBtn.addActionListener(this);
        buttonsPane.add(this.refreshBtn);
        inner2.add(buttonsPane, BorderLayout.PAGE_END);

        //

        final JPanel errorPane = new JPanel(new BorderLayout());
        errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        errorPane.setBackground(Skin.OFF_WHITE_YELLOW);
        inner1.add(errorPane, BorderLayout.PAGE_END);

        this.error1 = new JLabel(CoreConstants.SPC);
        this.error1.setFont(Skin.MEDIUM_15_FONT);
        this.error1.setHorizontalAlignment(SwingConstants.CENTER);
        this.error1.setForeground(Skin.ERROR_COLOR);

        this.error2 = new JLabel(CoreConstants.SPC);
        this.error2.setFont(Skin.MEDIUM_15_FONT);
        this.error2.setHorizontalAlignment(SwingConstants.CENTER);
        this.error2.setForeground(Skin.ERROR_COLOR);

        errorPane.add(this.error1, BorderLayout.PAGE_START);
        errorPane.add(this.error2, BorderLayout.PAGE_END);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (REFRESH.equals(cmd)) {
            processRefresh();
        }
    }

    /**
     * Sets focus.
     */
    void focus() {

        // No action
    }

    /**
     * Resets the card to accept data for a new loan.
     */
    void reset() {

        this.table.clear();

        this.error1.setText(CoreConstants.SPC);
        this.error2.setText(CoreConstants.SPC);

        populateTable();

        getRootPane().setDefaultButton(this.refreshBtn);
    }

    /**
     * Populates the table.
     */
    private void populateTable() {

        this.table.clear();

        // Get all resource records and build map from resource ID to resource type
        final Map<String, String> resourceMap = new HashMap<>(100);

        final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

        try {
            try (final Statement stmt = conn.createStatement()) {
                try (final ResultSet rs = stmt.executeQuery("SELECT resource_id,resource_type FROM resource")) {
                    while (rs.next()) {
                        resourceMap.put(rs.getString(1), rs.getString(2));
                    }
                }
            } catch (final SQLException ex) {
                this.error1.setText("Error querying resource table:");
                if (ex.getMessage() == null) {
                    this.error2.setText(ex.getClass().getSimpleName());
                } else {
                    this.error2.setText(ex.getMessage());
                }
            }

            if (!resourceMap.isEmpty()) {
                final LocalDate today = LocalDate.now();
                final List<ResourceActivityRow> records = new ArrayList<>(10);

                // Get all loans that occurred today

                final String sql2 = "SELECT stu_id, resource_id, start_time FROM stresource WHERE loan_dt=?";

                try (final PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setDate(1, Date.valueOf(today));

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final String stuId = rs.getString(1);
                            final String resId = rs.getString(2);
                            final int start = rs.getInt(3);

                            final int lh = start / 60;
                            final int lm = start % 60;
                            final LocalDateTime when = LocalDateTime.of(today, LocalTime.of(lh, lm));

                            final String type = resourceMap.get(resId);

                            records.add(new ResourceActivityRow("Loan", stuId, resId, when, type));
                        }
                    }
                } catch (final SQLException ex) {
                    this.error1.setText("Error querying stresource table:");
                    if (ex.getMessage() == null) {
                        this.error2.setText(ex.getClass().getSimpleName());
                    } else {
                        this.error2.setText(ex.getMessage());
                    }
                }

                // Get all returns that occurred today

                final String sql3 = "SELECT stu_id, resource_id, finish_time FROM stresource WHERE return_dt=?";

                try (final PreparedStatement ps = conn.prepareStatement(sql3)) {
                    ps.setDate(1, Date.valueOf(today));

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final String stuId = rs.getString(1);
                            final String resId = rs.getString(2);
                            final int finish = rs.getInt(3);

                            final int lh = finish / 60;
                            final int lm = finish % 60;
                            final LocalDateTime when = LocalDateTime.of(today, LocalTime.of(lh, lm));

                            final String type = resourceMap.get(resId);

                            records.add(new ResourceActivityRow("Return", stuId, resId, when, type));
                        }
                    }
                } catch (final SQLException ex) {
                    this.error1.setText("Error querying stresource table:");
                    if (ex.getMessage() == null) {
                        this.error2.setText(ex.getClass().getSimpleName());
                    } else {
                        this.error2.setText(ex.getMessage());
                    }
                }

                Collections.sort(records);
                this.table.clear();
                this.table.addData(records, 2);
            }
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Called when the "Refresh" button is pressed.
     */
    private void processRefresh() {

        reset();
    }
}
