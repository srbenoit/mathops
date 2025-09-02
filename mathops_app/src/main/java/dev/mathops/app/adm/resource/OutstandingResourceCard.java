package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.schema.ESchema;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A card panel to display all outstanding resources.
 */
class OutstandingResourceCard extends AdmPanelBase implements ActionListener {

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1125912507644704215L;

    /** The data cache. */
    private final Cache cache;

    /** The outstanding resource table. */
    private final JTableOutstandingResource table;

    /** The refresh button. */
    private final JButton refreshBtn;

    /** An error message. */
    private final JLabel error1;

    /** An error message. */
    private final JLabel error2;

    /**
     * Constructs a new {@code OutstandingResourceCard}.
     *
     * @param theCache the data cache
     */
    OutstandingResourceCard(final Cache theCache) {

        super();

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_MAGENTA);
        panel.setBorder(getBorder());

        setBackground(Skin.LT_MAGENTA);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        add(panel, BorderLayout.CENTER);

        this.cache = theCache;

        panel.add(makeHeader("Outstanding Resources", false), BorderLayout.NORTH);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_MAGENTA);
        panel.add(center, BorderLayout.CENTER);

        // "Outstanding Resources" header
        // Table of outstanding resources
        // [Refresn] button
        // Error messages

        // In this panel's "center" position:
        // inner1 (header in N, errors in S, inner2 in center)
        // inner2 (table in center, [Done] in S )

        final JPanel inner1 = new JPanel(new BorderLayout(10, 10));
        inner1.setBackground(Skin.OFF_WHITE_MAGENTA);
        final JPanel inner2 = new JPanel(new BorderLayout(10, 10));
        inner2.setBackground(Skin.OFF_WHITE_MAGENTA);

        inner1.add(inner2, BorderLayout.CENTER);
        center.add(inner1, BorderLayout.CENTER);

        //

        this.table = new JTableOutstandingResource();
        final JScrollPane scroll = new JScrollPane(this.table);
        scroll.getViewport().setBackground(Skin.WHITE);
        scroll.setBackground(Skin.WHITE);
        inner2.add(scroll, BorderLayout.CENTER);

        scroll.setPreferredSize(this.table.getPreferredScrollSize(scroll, 3));

        //

        final JPanel buttonsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPane.setBackground(Skin.OFF_WHITE_MAGENTA);
        this.refreshBtn = new JButton("Refresh");
        this.refreshBtn.setFont(Skin.BIG_BUTTON_16_FONT);
        this.refreshBtn.setActionCommand(REFRESH);
        this.refreshBtn.addActionListener(this);
        buttonsPane.add(this.refreshBtn);
        inner2.add(buttonsPane, BorderLayout.SOUTH);

        //

        final JPanel errorPane = new JPanel(new BorderLayout());
        errorPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        errorPane.setBackground(Skin.OFF_WHITE_MAGENTA);
        inner1.add(errorPane, BorderLayout.SOUTH);

        this.error1 = new JLabel(CoreConstants.SPC);
        this.error1.setFont(Skin.MEDIUM_15_FONT);
        this.error1.setHorizontalAlignment(SwingConstants.CENTER);
        this.error1.setForeground(Skin.ERROR_COLOR);

        this.error2 = new JLabel(CoreConstants.SPC);
        this.error2.setFont(Skin.MEDIUM_15_FONT);
        this.error2.setHorizontalAlignment(SwingConstants.CENTER);
        this.error2.setForeground(Skin.ERROR_COLOR);

        errorPane.add(this.error1, BorderLayout.NORTH);
        errorPane.add(this.error2, BorderLayout.SOUTH);
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
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery("SELECT resource_id,resource_type FROM resource")) {
                while (rs.next()) {
                    resourceMap.put(rs.getString(1), rs.getString(2));
                }
            } catch (final SQLException ex) {
                this.error1.setText("Error querying resource table:");
                if (ex.getMessage() == null) {
                    this.error2.setText(ex.getClass().getSimpleName());
                } else {
                    this.error2.setText(ex.getMessage());
                }
            }

            if (resourceMap.isEmpty()) {
                this.table.updatePrefSize();
                revalidate();
                repaint();
            } else {
                final String sql2 = "SELECT student.stu_id, student.first_name, "
                                    + "student.last_name, stresource.resource_id, stresource.loan_dt, "
                                    + "stresource.start_time, stresource.due_dt "
                                    + "FROM stresource, student WHERE return_dt IS NULL "
                                    + "AND student.stu_id = stresource.stu_id "
                                    + "ORDER BY loan_dt, start_time";

                final List<OutstandingResourceRow> records = new ArrayList<>(10);
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql2)) {
                    while (rs.next()) {
                        final String stuId = rs.getString(1).trim();
                        final String first = rs.getString(2).trim();
                        final String last = rs.getString(3).trim();
                        final String resId = rs.getString(4);
                        final Date loanDt = rs.getDate(5);
                        final int loanTime = rs.getInt(6);
                        final Date dueDt = rs.getDate(7);
                        final String type = resourceMap.get(resId);

                        final int lh = loanTime / 60;
                        final int lm = loanTime % 60;
                        final LocalDateTime lent = loanDt == null ? null
                                : LocalDateTime.of(loanDt.toLocalDate(), LocalTime.of(lh, lm));

                        final LocalDate due = dueDt == null ? null : dueDt.toLocalDate();
                        final String stuName = last + ", " + first;

                        records.add(new OutstandingResourceRow(stuId, stuName, resId, lent, due, type));
                    }

                    this.table.addData(records, 2);

                    invalidate();
                    revalidate();
                    repaint();
                } catch (final SQLException ex) {
                    this.error1.setText("Error querying stresource table:");
                    if (ex.getMessage() == null) {
                        this.error2.setText(ex.getClass().getSimpleName());
                    } else {
                        this.error2.setText(ex.getMessage());
                    }
                }
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
