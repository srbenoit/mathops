package dev.mathops.app.adm.management;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.schema.legacy.impl.RawPlcFeeLogic;
import dev.mathops.dbjobs.batch.ChallengeBilling;
import dev.mathops.dbjobs.batch.PlacementBilling;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * A panel that supports management of special student populations.
 */
class PlacementBillingPanel extends AdmPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2936279244428809936L;

    /** An action command. */
    private static final String BILL_PLC = "BILL_PLC";

    /** An action command. */
    private static final String BILL_CHAL = "BILL_CHAL";

    /** The data cache. */
    private final Cache cache;

    /** A text field to display the last bill date. */
    private final JTextField lastBillDate;

    /** A text field to display the number of outstanding placement activities. */
    private final JTextField outstandingPlc;

    /** A label to show the dollar amount of unbilled placement. */
    private final JLabel outstandingPlcAmt;

    /** A text field to display the number of outstanding challenge exams. */
    private final JTextField outstandingChal;

    /** A label to show the dollar amount of unbilled challenge exams. */
    private final JLabel outstandingChalAmt;

    /**
     * Constructs a new {@code PlacementBillingPanel}.
     *
     * @param theCache         the data cache
     */
    PlacementBillingPanel(final Cache theCache) {

        super();
        setBackground(Skin.LIGHTEST);

        this.cache = theCache;

        // Left side: billing status and billing buttons

        final JPanel col1 = makeOffWhitePanel(new BorderLayout(5, 5));
        col1.setBackground(Skin.LIGHTEST);

        add(col1, StackedBorderLayout.WEST);

        col1.add(makeHeader("Billing Status", false), BorderLayout.NORTH);

        final JLabel[] labels = new JLabel[3];
        labels[0] = new JLabel("Date of last billing:");
        labels[1] = new JLabel("Placement activities to be billed:");
        labels[2] = new JLabel("Challenge exams to be billed:");

        int maxW = 0;
        int maxH = 0;
        for (final JLabel label : labels) {
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension size = label.getPreferredSize();
            maxW = Math.max(maxW, size.width);
            maxH = Math.max(maxH, size.height);
        }
        final Dimension newSize = new Dimension(maxW, maxH);
        for (final JLabel label : labels) {
            label.setPreferredSize(newSize);
        }

        this.lastBillDate = new JTextField(20);
        this.lastBillDate.setEditable(false);
        this.outstandingPlc = new JTextField(5);
        this.outstandingPlc.setEditable(false);
        this.outstandingChal = new JTextField(5);
        this.outstandingChal.setEditable(false);

        this.outstandingPlcAmt = new JLabel(" ");
        this.outstandingChalAmt = new JLabel(" ");

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.LIGHTEST);
        col1.add(center, BorderLayout.CENTER);

        final JPanel top3 = new JPanel(new BorderLayout());
        top3.setBackground(Skin.LIGHTEST);
        center.add(top3, BorderLayout.PAGE_START);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        flow1.setBackground(Skin.LIGHTEST);
        flow1.add(labels[0]);
        flow1.add(this.lastBillDate);
        top3.add(flow1, BorderLayout.PAGE_START);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        flow2.setBackground(Skin.LIGHTEST);
        flow2.add(labels[1]);
        flow2.add(this.outstandingPlc);
        flow2.add(this.outstandingPlcAmt);
        top3.add(flow2, BorderLayout.CENTER);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        flow3.setBackground(Skin.LIGHTEST);
        flow3.add(labels[2]);
        flow3.add(this.outstandingChal);
        flow3.add(this.outstandingChalAmt);
        top3.add(flow3, BorderLayout.PAGE_END);

        final JPanel center2 = new JPanel(new BorderLayout());
        center2.setBackground(Skin.LIGHTEST);
        center.add(center2, BorderLayout.CENTER);

        final JPanel next3 = new JPanel(new BorderLayout());
        next3.setBackground(Skin.LIGHTEST);
        center2.add(next3, BorderLayout.PAGE_START);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttons.setBackground(Skin.LIGHTEST);
        next3.add(buttons, BorderLayout.PAGE_START);

        final JButton plc = new JButton("Bill Placement Activities");
        plc.setActionCommand(BILL_PLC);
        plc.addActionListener(this);

        final JButton chal = new JButton("Bill Challenge Exams");
        chal.setActionCommand(BILL_CHAL);
        chal.addActionListener(this);

        buttons.add(plc);
        buttons.add(chal);
    }

    /**
     * Refreshes the billing status display.
     */
    public void refreshStatus() {

        final LocalDate lastDt;
        try {
            lastDt = RawPlcFeeLogic.queryMostRecentBillDate(this.cache);
            if (lastDt == null) {
                this.lastBillDate.setText("(none)");
            } else {
                final LocalDate today = LocalDate.now();
                if (lastDt.isBefore(today)) {
                    int days = 1;
                    LocalDate dt = lastDt;
                    while (today.isAfter(dt)) {
                        dt = dt.plusDays(1L);
                        ++days;
                    }
                    this.lastBillDate.setText(TemporalUtils.FMT_WMDY.format(lastDt)
                            + " (" + days + " days ago)");
                } else {
                    this.lastBillDate.setText(TemporalUtils.FMT_WMDY.format(lastDt));
                }
            }

            final int unbilledPlacement = PlacementBilling.countUnbilled(this.cache);
            this.outstandingPlc.setText(Integer.toString(unbilledPlacement));
            final int placementFees = unbilledPlacement * 15;
            this.outstandingPlcAmt.setText("$" + placementFees + ".00");

            final int unbilledChallenge = ChallengeBilling.countUnbilled(this.cache);
            this.outstandingChal.setText(Integer.toString(unbilledChallenge));
            final int clallengeFees = unbilledChallenge * 20;
            this.outstandingChalAmt.setText("$" + clallengeFees + ".00");
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (BILL_PLC.equals(cmd)) {
            try {
                PlacementBilling.execute(this.cache);
                final String[] message = new String[2];
                message[0] = "Placement activity billing job executed.";
                message[1] = "Reports were written to /opt/zircon/reports/";
                JOptionPane.showMessageDialog(this, message);
            } catch (final SQLException ex) {
                final String[] message = new String[2];
                message[0] = "Failed to execute billing job:";
                message[1] = ex.getMessage();
                JOptionPane.showMessageDialog(this, message);
            }
        } else if (BILL_CHAL.equals(cmd)) {
            try {
                ChallengeBilling.execute(this.cache);
                final String[] message = new String[2];
                message[0] = "Challenge exam billing job executed.";
                message[1] = "Reports were written to /opt/zircon/reports/";
                JOptionPane.showMessageDialog(this, message);
            } catch (final SQLException ex) {
                final String[] message = new String[2];
                message[0] = "Failed to execute billing job:";
                message[1] = ex.getMessage();
                JOptionPane.showMessageDialog(this, message);
            }
        }
    }
}
