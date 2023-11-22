package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.IDevice;
import jwabbit.core.Interrupt;
import jwabbit.core.PIOContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.hardware.Clock;
import jwabbit.hardware.Delay;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;
import jwabbit.hardware.STDINT;
import jwabbit.hardware.Timer;
import jwabbit.hardware.XTAL;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to display details of a PIO context.
 */
final class PIOContextPanel extends LoggedPanel implements ICalcStateListener {

    /** Grid color. */
    private static final Color GRID_COLOR = new Color(220, 220, 240);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6835356439651910065L;

    /** The calculator model. */
    private final JTextField model;

    /** The table model for the device list. */
    private final DeviceTableModel deviceTableModel;

    /** The device list table. */
    private final JTable deviceTable;

    /** The STDINT set of active interrupts. */
    private final JTextField intactive;

    /** The STDINT last check time 1. */
    private final JTextField lastchk1;

    /** The STDINT last timer 1 max. */
    private final JTextField timermax1;

    /** The STDINT last check time 2. */
    private final JTextField lastchk2;

    /** The STDINT last timer 2 max. */
    private final JTextField timermax2;

    /** The STDINT frequencies. */
    private final JTextField[] freq;

    /** The STDINT memory byte. */
    private final JTextField mem;

    /** The STDINT x-y value. */
    private final JTextField xy;

    /** The STDINT on backup. */
    private final JTextField onBackup;

    /** The STDINT on latch. */
    private final JCheckBox onLatch;

    /** The link host value. */
    private final JTextField host;

    /** The link client value. */
    private final JTextField client;

    /** The link vlink send value. */
    private final JTextField vlinkSend;

    /** The link vlink receive value. */
    private final JTextField vlinkRecv;

    /** The link vlink size value. */
    private final JTextField vlinkSize;

    /** The link vout value. */
    private final JTextField vout;

    /** The link vin value. */
    private final JTextField vin;

    /** The link changed flag. */
    private final JCheckBox hasChanged;

    /** The link changed time. */
    private final JTextField changedTime;

    /** The link assist enable. */
    private final JTextField linkEnable;

    /** The link assist last access. */
    private final JTextField lastAccess;

    /** The link assist in value. */
    private final JTextField in;

    /** The link assist out value. */
    private final JTextField out;

    /** The link assist working data. */
    private final JTextField working;

    /** The link assist bit. */
    private final JTextField bit;

    /** The link assist sending flag. */
    private final JCheckBox sending;

    /** The link assist receiving flag. */
    private final JCheckBox receiving;

    /** The link assist read flag. */
    private final JCheckBox read;

    /** The link assist ready flag. */
    private final JCheckBox ready;

    /** The link assist error flag. */
    private final JCheckBox error;

    /** The xtal last time. */
    private final JTextField xtalLastTime;

    /** The xtal ticks. */
    private final JTextField xtalTicks;

    /** The xtal timer last tstates. */
    private final JTextField[] timerLastTStates;

    /** The xtal timer last ticks. */
    private final JTextField[] timerLastTicks;

    /** The xtal timer divisors. */
    private final JTextField[] timerDivisor;

    /** The xtal timer loop flags. */
    private final JCheckBox[] timerLoop;

    /** The xtal timer interrupt flags. */
    private final JCheckBox[] timerInterrupt;

    /** The xtal timer underflow flags. */
    private final JCheckBox[] timerUnderflow;

    /** The xtal timer generate flags. */
    private final JCheckBox[] timerGenerate;

    /** The xtal timer active flags. */
    private final JCheckBox[] timerActive;

    /** The xtal timer clock. */
    private final JTextField[] timerClock;

    /** The xtal timer count. */
    private final JTextField[] timerCount;

    /** The xtal timer max. */
    private final JTextField[] timerMax;

    /** The lcd1 delay. */
    private final JTextField delayLcd1;

    /** The lcd2 delay. */
    private final JTextField delayLcd2;

    /** The lcd3 delay. */
    private final JTextField delayLcd3;

    /** The lcd4 delay. */
    private final JTextField delayLcd4;

    /** The MAD delay. */
    private final JTextField delayMad;

    /** The unknown delay. */
    private final JTextField delayUnk;

    /** The LCD wait delay. */
    private final JTextField delayLcdWait;

    /** The clock enable flag. */
    private final JTextField clockEnable;

    /** The clock set. */
    private final JTextField clockSet;

    /** The clock base. */
    private final JTextField clockBase;

    /** The clock last time. */
    private final JTextField clockLastTime;

    /**
     * Constructs a new {@code PIOContextPanel}.
     */
    PIOContextPanel() {

        super(new BorderLayout(10, 4));

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        setBackground(Debugger.BG_COLOR);

        final JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 0, 0)));
        top.setBackground(Debugger.BG_COLOR);
        final JLabel title = new JLabel("PIO Context Detail");
        title.setFont(Fonts.getSans().deriveFont(Font.BOLD, title.getFont().getSize2D() + 1.0f));
        title.setForeground(new Color(100, 0, 0));
        top.add(title);
        add(top, BorderLayout.PAGE_START);

        final JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Debugger.BG_COLOR);
        add(west, BorderLayout.LINE_START);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topFlow.setBackground(Debugger.BG_COLOR);
        topFlow.add(new JLabel("Model:"));
        this.model = new JTextField(12);
        topFlow.add(this.model);
        west.add(topFlow, BorderLayout.PAGE_START);

        this.deviceTableModel = new DeviceTableModel();
        this.deviceTable = new JTable(this.deviceTableModel);

        this.deviceTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        this.deviceTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        this.deviceTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        this.deviceTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        this.deviceTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        this.deviceTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        this.deviceTable.getColumnModel().getColumn(0).setResizable(false);
        this.deviceTable.getColumnModel().getColumn(1).setResizable(false);
        this.deviceTable.getColumnModel().getColumn(2).setResizable(false);
        this.deviceTable.getColumnModel().getColumn(3).setResizable(false);
        this.deviceTable.getColumnModel().getColumn(4).setResizable(false);
        this.deviceTable.getColumnModel().getColumn(5).setResizable(false);

        this.deviceTable.setShowHorizontalLines(true);
        this.deviceTable.setShowVerticalLines(true);
        this.deviceTable.setRowSelectionAllowed(false);
        this.deviceTable.setColumnSelectionAllowed(false);
        this.deviceTable.setGridColor(GRID_COLOR);
        this.deviceTable.setBackground(Color.WHITE);

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font bold = Fonts.getSans().deriveFont(Font.BOLD, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        this.deviceTable.setFont(mono);
        this.deviceTable.getTableHeader().setFont(sans);

        final JPanel devices = new JPanel(new BorderLayout());
        west.add(devices, BorderLayout.CENTER);

        devices.add(this.deviceTable.getTableHeader(), BorderLayout.PAGE_START);
        final JScrollPane scroll = new JScrollPane(this.deviceTable);
        scroll.setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(
                40 + 100 + 120 + 120 + scroll.getVerticalScrollBar().getPreferredSize().width + 2, 200));
        devices.add(scroll, BorderLayout.CENTER);

        final JPanel center = new JPanel(new BorderLayout(4, 4));
        center.setBackground(Debugger.BG_COLOR);
        add(center, BorderLayout.CENTER);

        this.intactive = new JTextField(4);
        this.lastchk1 = new JTextField(12);
        this.timermax1 = new JTextField(12);
        this.lastchk2 = new JTextField(12);
        this.timermax2 = new JTextField(12);
        this.freq = new JTextField[]{new JTextField(10), new JTextField(10), new JTextField(10), new JTextField(10)};
        this.mem = new JTextField(4);
        this.xy = new JTextField(4);
        this.onBackup = new JTextField(3);
        this.onLatch = new JCheckBox("On latch");

        final JPanel stdint = buildStdintPanel(sans, bold, mono);
        center.add(stdint, BorderLayout.PAGE_START);

        final JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        nest1.setBackground(Debugger.BG_COLOR);
        center.add(nest1, BorderLayout.CENTER);

        this.host = new JTextField(4);
        this.client = new JTextField(4);
        this.vout = new JTextField(4);
        this.vin = new JTextField(4);
        this.vlinkSend = new JTextField(4);
        this.vlinkRecv = new JTextField(4);
        this.vlinkSize = new JTextField(6);
        this.hasChanged = new JCheckBox("Has changed");
        this.changedTime = new JTextField(11);

        final JPanel link = buildLinkPanel(sans, bold, mono);
        nest1.add(link, BorderLayout.PAGE_START);

        final JPanel nest2 = new JPanel(new BorderLayout(4, 4));
        nest2.setBackground(Debugger.BG_COLOR);
        nest1.add(nest2, BorderLayout.CENTER);

        this.linkEnable = new JTextField(4);
        this.lastAccess = new JTextField(12);
        this.in = new JTextField(4);
        this.out = new JTextField(4);
        this.working = new JTextField(4);
        this.bit = new JTextField(4);
        this.sending = new JCheckBox("Sending");
        this.sending.setBackground(Debugger.BG_COLOR);
        this.receiving = new JCheckBox("Receiving");
        this.receiving.setBackground(Debugger.BG_COLOR);
        this.read = new JCheckBox("Read");
        this.read.setBackground(Debugger.BG_COLOR);
        this.ready = new JCheckBox("Ready");
        this.ready.setBackground(Debugger.BG_COLOR);
        this.error = new JCheckBox("Error");
        this.error.setBackground(Debugger.BG_COLOR);

        final JPanel linkasist = buildLinkAssistPanel(sans, bold, mono);
        nest2.add(linkasist, BorderLayout.PAGE_START);

        final JPanel nest3 = new JPanel(new BorderLayout(4, 4));
        nest3.setBackground(Debugger.BG_COLOR);
        nest2.add(nest3, BorderLayout.CENTER);

        this.xtalLastTime = new JTextField(12);
        this.xtalTicks = new JTextField(12);
        this.timerLastTStates = new JTextField[3];
        this.timerLastTicks = new JTextField[3];
        this.timerLoop = new JCheckBox[3];
        this.timerInterrupt = new JCheckBox[3];
        this.timerUnderflow = new JCheckBox[3];
        this.timerGenerate = new JCheckBox[3];
        this.timerActive = new JCheckBox[3];
        this.timerDivisor = new JTextField[3];
        this.timerClock = new JTextField[3];
        this.timerCount = new JTextField[3];
        this.timerMax = new JTextField[3];

        for (int i = 0; i < 3; ++i) {
            this.timerLastTStates[i] = new JTextField(12);
            this.timerLastTicks[i] = new JTextField(12);

            this.timerLoop[i] = new JCheckBox("Loop");
            this.timerLoop[i].setBackground(Debugger.BG_COLOR);
            this.timerLoop[i].setBorder(BorderFactory.createEmptyBorder());

            this.timerInterrupt[i] = new JCheckBox("Interrupt");
            this.timerInterrupt[i].setBackground(Debugger.BG_COLOR);
            this.timerInterrupt[i].setBorder(BorderFactory.createEmptyBorder());

            this.timerUnderflow[i] = new JCheckBox("Underflow");
            this.timerUnderflow[i].setBackground(Debugger.BG_COLOR);
            this.timerUnderflow[i].setBorder(BorderFactory.createEmptyBorder());

            this.timerGenerate[i] = new JCheckBox("Generate");
            this.timerGenerate[i].setBackground(Debugger.BG_COLOR);
            this.timerGenerate[i].setBorder(BorderFactory.createEmptyBorder());

            this.timerActive[i] = new JCheckBox("Active");
            this.timerActive[i].setBackground(Debugger.BG_COLOR);
            this.timerActive[i].setBorder(BorderFactory.createEmptyBorder());

            this.timerDivisor[i] = new JTextField(12);
            this.timerClock[i] = new JTextField(4);
            this.timerCount[i] = new JTextField(4);
            this.timerMax[i] = new JTextField(4);
        }

        final JPanel xtal = buildXtalPanel(sans, bold, mono);
        nest3.add(xtal, BorderLayout.PAGE_START);

        final JPanel nest4 = new JPanel(new BorderLayout(4, 4));
        nest4.setBackground(Debugger.BG_COLOR);
        nest3.add(nest4, BorderLayout.CENTER);

        this.delayLcd1 = new JTextField(4);
        this.delayLcd2 = new JTextField(4);
        this.delayLcd3 = new JTextField(4);
        this.delayLcd4 = new JTextField(4);
        this.delayMad = new JTextField(4);
        this.delayUnk = new JTextField(4);
        this.delayLcdWait = new JTextField(4);
        this.clockEnable = new JTextField(4);
        this.clockSet = new JTextField(10);
        this.clockBase = new JTextField(10);
        this.clockLastTime = new JTextField(10);

        final JPanel delay = buildDelayClockPanel(sans, bold, mono);
        nest4.add(delay, BorderLayout.PAGE_START);
    }

    /**
     * Builds the standard interrupt panel.
     *
     * @param sans the sans font
     * @param bold the bold font
     * @param mono the monospace font
     * @return the link panel
     */
    private JPanel buildStdintPanel(final Font sans, final Font bold, final Font mono) {

        final JPanel stdint = new JPanel(new GridLayout(5, 1, 4, 4));
        stdint.setBackground(Debugger.BG_COLOR);

        final JLabel stdintTitle = new JLabel("Standard Interrupts:");
        stdintTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        stdintTitle.setFont(bold);
        stdintTitle.setVerticalAlignment(SwingConstants.BOTTOM);
        stdint.add(stdintTitle);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row1.setBackground(Debugger.BG_COLOR);
        final JLabel lbl1a = new JLabel("Active:");
        lbl1a.setFont(sans);
        row1.add(lbl1a);
        this.intactive.setFont(mono);
        row1.add(this.intactive);

        final JLabel lbl1b = new JLabel("Mem:");
        lbl1b.setFont(sans);
        row1.add(lbl1b);
        this.mem.setFont(mono);
        row1.add(this.mem);

        final JLabel lbl1c = new JLabel("XY:");
        lbl1c.setFont(sans);
        row1.add(lbl1c);
        this.xy.setFont(mono);
        row1.add(this.xy);

        final JLabel lbl1d = new JLabel("On backup:");
        lbl1d.setFont(sans);
        row1.add(lbl1d);
        this.onBackup.setFont(mono);
        row1.add(this.onBackup);

        this.onLatch.setBackground(Debugger.BG_COLOR);
        this.onLatch.setFont(sans);
        row1.add(this.onLatch);
        stdint.add(row1);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row2.setBackground(Debugger.BG_COLOR);
        final JLabel lbl2a = new JLabel("Timer 1 Last Check:");
        lbl2a.setFont(sans);
        row2.add(lbl2a);
        this.lastchk1.setFont(sans);
        row2.add(this.lastchk1);
        final JLabel lbl2b = new JLabel("  Maximum:");
        lbl2b.setFont(sans);
        row2.add(lbl2b);
        this.timermax1.setFont(sans);
        row2.add(this.timermax1);
        stdint.add(row2);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row3.setBackground(Debugger.BG_COLOR);
        final JLabel lbl3a = new JLabel("Timer 2 Last Check:");
        lbl3a.setFont(sans);
        row3.add(lbl3a);
        this.lastchk2.setFont(sans);
        row3.add(this.lastchk2);
        final JLabel lbl3b = new JLabel("  Maximum:");
        lbl3b.setFont(sans);
        row3.add(lbl3b);
        this.timermax2.setFont(sans);
        row3.add(this.timermax2);
        stdint.add(row3);

        final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row4.setBackground(Debugger.BG_COLOR);
        final JLabel lbl4 = new JLabel("Frequencies:");
        lbl4.setFont(sans);
        row4.add(lbl4);
        this.freq[0].setFont(sans);
        row4.add(this.freq[0]);
        this.freq[1].setFont(sans);
        row4.add(this.freq[1]);
        this.freq[2].setFont(sans);
        row4.add(this.freq[2]);
        this.freq[3].setFont(sans);
        row4.add(this.freq[3]);
        stdint.add(row4);

        return stdint;
    }

    /**
     * Builds the link panel.
     *
     * @param sans the sans font
     * @param bold the bold font
     * @param mono the monospace font
     * @return the link panel
     */
    private JPanel buildLinkPanel(final Font sans, final Font bold, final Font mono) {

        final JPanel link = new JPanel(new GridLayout(3, 1, 4, 4));
        link.setBackground(Debugger.BG_COLOR);

        final JLabel linkTitle = new JLabel("Virtual Link:");
        linkTitle.setFont(bold);
        linkTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        linkTitle.setVerticalAlignment(SwingConstants.BOTTOM);
        link.add(linkTitle);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row1.setBackground(Debugger.BG_COLOR);
        final JLabel lbl1a = new JLabel("Host:");
        lbl1a.setFont(sans);
        row1.add(lbl1a);
        this.host.setFont(mono);
        row1.add(this.host);
        final JLabel lbl1b = new JLabel(" Client:");
        lbl1b.setFont(sans);
        row1.add(lbl1b);
        this.client.setFont(mono);
        row1.add(this.client);
        final JLabel lbl1c = new JLabel(" Out:");
        lbl1c.setFont(sans);
        row1.add(lbl1c);
        this.vout.setFont(mono);
        row1.add(this.vout);
        final JLabel lbl1d = new JLabel(" In:");
        lbl1d.setFont(sans);
        row1.add(lbl1d);
        this.vin.setFont(mono);
        row1.add(this.vin);
        this.hasChanged.setBackground(Debugger.BG_COLOR);
        row1.add(this.hasChanged);
        link.add(row1);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row2.setBackground(Debugger.BG_COLOR);
        final JLabel lbl2a = new JLabel("Send:");
        lbl2a.setFont(sans);
        row2.add(lbl2a);
        this.vlinkSend.setFont(mono);
        row2.add(this.vlinkSend);
        final JLabel lbl2b = new JLabel(" Receive:");
        lbl2b.setFont(sans);
        row2.add(lbl2b);
        this.vlinkRecv.setFont(mono);
        row2.add(this.vlinkRecv);
        final JLabel lbl2c = new JLabel(" Size");
        lbl2c.setFont(sans);
        row2.add(lbl2c);
        this.vlinkSize.setFont(sans);
        row2.add(this.vlinkSize);
        final JLabel lbl2d = new JLabel(" When changed:");
        lbl2d.setFont(sans);
        row2.add(lbl2d);
        this.changedTime.setFont(sans);
        row2.add(this.changedTime);
        link.add(row2);

        return link;
    }

    /**
     * Builds the link assist panel.
     *
     * @param sans the sans font
     * @param bold the bold font
     * @param mono the monospace font
     * @return the link assist panel
     */
    private JPanel buildLinkAssistPanel(final Font sans, final Font bold, final Font mono) {

        final JPanel linkAsst = new JPanel(new GridLayout(4, 1, 4, 4));
        linkAsst.setBackground(Debugger.BG_COLOR);

        final JLabel linkAsstTitle = new JLabel("Link Assist:");
        linkAsstTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        linkAsstTitle.setFont(bold);
        linkAsstTitle.setVerticalAlignment(SwingConstants.BOTTOM);
        linkAsst.add(linkAsstTitle);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row1.setBackground(Debugger.BG_COLOR);
        final JLabel lbl6a = new JLabel("Enable:");
        lbl6a.setFont(sans);
        row1.add(lbl6a);
        this.linkEnable.setFont(mono);
        row1.add(this.linkEnable);
        final JLabel lbl6b = new JLabel("Last access:");
        lbl6b.setFont(sans);
        row1.add(lbl6b);
        this.lastAccess.setFont(sans);
        row1.add(this.lastAccess);
        linkAsst.add(row1);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row2.setBackground(Debugger.BG_COLOR);
        final JLabel lbl7a = new JLabel("In:");
        lbl7a.setFont(sans);
        row2.add(lbl7a);
        this.in.setFont(mono);
        row2.add(this.in);
        final JLabel lbl7b = new JLabel("Out:");
        lbl7b.setFont(sans);
        row2.add(lbl7b);
        this.out.setFont(mono);
        row2.add(this.out);
        final JLabel lbl7c = new JLabel("Working");
        lbl7c.setFont(sans);
        row2.add(lbl7c);
        this.working.setFont(sans);
        row2.add(this.working);
        final JLabel lbl7d = new JLabel("Bit");
        lbl7d.setFont(sans);
        row2.add(lbl7d);
        this.bit.setFont(sans);
        row2.add(this.bit);
        linkAsst.add(row2);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row3.setBackground(Debugger.BG_COLOR);
        this.sending.setFont(sans);
        row3.add(this.sending);
        this.receiving.setFont(sans);
        row3.add(this.receiving);
        this.read.setFont(sans);
        row3.add(this.read);
        this.ready.setFont(sans);
        row3.add(this.ready);
        this.error.setFont(sans);
        row3.add(this.error);
        linkAsst.add(row3);

        return linkAsst;
    }

    /**
     * Builds the crystal panel.
     *
     * @param sans the sans font
     * @param bold the bold font
     * @param mono the monospace font
     * @return the crystal panel
     */
    private JPanel buildXtalPanel(final Font sans, final Font bold, final Font mono) {

        final JPanel xtal = new JPanel(new BorderLayout(4, 4));
        xtal.setBackground(Debugger.BG_COLOR);

        final JLabel xtalTitle = new JLabel("Crystal and Timers:");
        xtalTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        xtalTitle.setFont(bold);
        xtalTitle.setVerticalAlignment(SwingConstants.BOTTOM);
        xtal.add(xtalTitle, BorderLayout.PAGE_START);

        JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        nest1.setBackground(Debugger.BG_COLOR);
        xtal.add(nest1, BorderLayout.CENTER);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row1.setBackground(Debugger.BG_COLOR);
        final JLabel lbl6a = new JLabel("Last time:");
        lbl6a.setFont(sans);
        row1.add(lbl6a);
        this.xtalLastTime.setFont(sans);
        row1.add(this.xtalLastTime);
        final JLabel lbl6b = new JLabel("Ticks:");
        lbl6b.setFont(sans);
        row1.add(lbl6b);
        this.xtalTicks.setFont(sans);
        row1.add(this.xtalTicks);
        nest1.add(row1, BorderLayout.PAGE_START);

        JPanel nest2 = new JPanel(new BorderLayout(4, 4));
        nest2.setBackground(Debugger.BG_COLOR);
        nest1.add(nest2, BorderLayout.CENTER);

        final JLabel[] headers = new JLabel[3];
        headers[0] = new JLabel("Timer 0");
        headers[1] = new JLabel("Timer 1");
        headers[2] = new JLabel("Timer 2");
        headers[0].setFont(bold);
        headers[1].setFont(bold);
        headers[2].setFont(bold);
        final int max = Math.max(headers[0].getPreferredSize().width,
                Math.max(headers[1].getPreferredSize().width, headers[2].getPreferredSize().width));
        final Dimension widest = new Dimension(max, headers[0].getPreferredSize().height);
        headers[0].setPreferredSize(widest);
        headers[1].setPreferredSize(widest);
        headers[2].setPreferredSize(widest);

        for (int i = 0; i < 3; ++i) {
            final JPanel timerGrid = new JPanel(new GridLayout(3, 1, 2, 2));
            timerGrid.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            timerGrid.setBackground(Debugger.BG_COLOR);

            nest2.add(timerGrid, BorderLayout.PAGE_START);
            nest1 = nest2;
            nest2 = new JPanel(new BorderLayout(4, 4));
            nest2.setBackground(Debugger.BG_COLOR);
            nest1.add(nest2, BorderLayout.CENTER);

            final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            row2.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            row2.setBackground(Debugger.BG_COLOR);
            row2.add(headers[i]);
            final JLabel lbl2b = new JLabel("Last T-states:");
            lbl2b.setFont(sans);
            row2.add(lbl2b);
            this.timerLastTStates[i].setFont(sans);
            row2.add(this.timerLastTStates[i]);
            final JLabel lbl2c = new JLabel("Last ticks:");
            lbl2c.setFont(sans);
            row2.add(lbl2c);
            this.timerLastTicks[i].setFont(sans);
            row2.add(this.timerLastTicks[i]);
            timerGrid.add(row2);

            final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            row3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            row3.setBackground(Debugger.BG_COLOR);
            final JLabel spacer3 = new JLabel("");
            spacer3.setPreferredSize(widest);
            row3.add(spacer3);
            final JLabel lbl3a = new JLabel("Divisor:");
            lbl3a.setFont(sans);
            row3.add(lbl3a);
            this.timerDivisor[i].setFont(sans);
            row3.add(this.timerDivisor[i]);
            final JLabel lbl3b = new JLabel("Clock:");
            lbl3b.setFont(sans);
            row3.add(lbl3b);
            this.timerClock[i].setFont(mono);
            row3.add(this.timerClock[i]);
            final JLabel lbl3c = new JLabel("Count:");
            lbl3c.setFont(sans);
            row3.add(lbl3c);
            this.timerCount[i].setFont(mono);
            row3.add(this.timerCount[i]);
            final JLabel lbl3d = new JLabel("Max:");
            lbl3d.setFont(sans);
            row3.add(lbl3d);
            this.timerMax[i].setFont(mono);
            row3.add(this.timerMax[i]);
            timerGrid.add(row3);

            final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            row4.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            row4.setBackground(Debugger.BG_COLOR);
            final JLabel spacer4 = new JLabel("");
            spacer4.setPreferredSize(widest);
            row4.add(spacer4);
            this.timerLoop[i].setFont(sans);
            row4.add(this.timerLoop[i]);
            row4.add(new JLabel("  "));
            this.timerInterrupt[i].setFont(sans);
            row4.add(this.timerInterrupt[i]);
            row4.add(new JLabel("  "));
            this.timerUnderflow[i].setFont(sans);
            row4.add(this.timerUnderflow[i]);
            row4.add(new JLabel("  "));
            this.timerGenerate[i].setFont(sans);
            row4.add(this.timerGenerate[i]);
            row4.add(new JLabel("  "));
            this.timerActive[i].setFont(sans);
            row4.add(this.timerActive[i]);
            timerGrid.add(row4);
        }

        return xtal;
    }

    /**
     * Builds the delay and clock panel.
     *
     * @param sans the sans font
     * @param bold the bold font
     * @param mono the monospace font
     * @return the delay and clock panel
     */
    private JPanel buildDelayClockPanel(final Font sans, final Font bold, final Font mono) {

        final JPanel delayClock = new JPanel(new BorderLayout(4, 4));
        delayClock.setBackground(Debugger.BG_COLOR);

        final JLabel xtalTitle = new JLabel("Delays and Clock:");
        xtalTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        xtalTitle.setFont(bold);
        xtalTitle.setVerticalAlignment(SwingConstants.BOTTOM);
        delayClock.add(xtalTitle, BorderLayout.PAGE_START);

        final JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        nest1.setBackground(Debugger.BG_COLOR);
        delayClock.add(nest1, BorderLayout.CENTER);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row1.setBackground(Debugger.BG_COLOR);
        final JLabel lbl6a = new JLabel("Delays:");
        lbl6a.setFont(bold);
        row1.add(lbl6a);
        final JLabel lbl6b = new JLabel("  LCD:");
        lbl6b.setFont(sans);
        row1.add(lbl6b);
        this.delayLcd1.setFont(sans);
        row1.add(this.delayLcd1);
        this.delayLcd2.setFont(sans);
        row1.add(this.delayLcd2);
        this.delayLcd3.setFont(sans);
        row1.add(this.delayLcd3);
        this.delayLcd4.setFont(sans);
        row1.add(this.delayLcd4);

        final JLabel lbl6c = new JLabel("MAD:");
        lbl6c.setFont(sans);
        row1.add(lbl6c);
        this.delayMad.setFont(sans);
        row1.add(this.delayMad);

        final JLabel lbl6d = new JLabel("Unk:");
        lbl6d.setFont(sans);
        row1.add(lbl6d);
        this.delayUnk.setFont(sans);
        row1.add(this.delayUnk);

        final JLabel lbl6e = new JLabel("LCD Wait:");
        lbl6e.setFont(sans);
        row1.add(lbl6e);
        this.delayLcdWait.setFont(sans);
        row1.add(this.delayLcdWait);

        nest1.add(row1, BorderLayout.PAGE_START);

        final JPanel nest2 = new JPanel(new BorderLayout(4, 4));
        nest2.setBackground(Debugger.BG_COLOR);
        nest1.add(nest2, BorderLayout.CENTER);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        row2.setBackground(Debugger.BG_COLOR);
        final JLabel lbl2a = new JLabel("Clock:");
        lbl2a.setFont(bold);
        row2.add(lbl2a);
        final JLabel lbl2b = new JLabel("  Enable:");
        lbl2b.setFont(sans);
        row2.add(lbl2b);
        this.clockEnable.setFont(mono);
        row2.add(this.clockEnable);

        final JLabel lbl2c = new JLabel(" Set:");
        lbl2c.setFont(sans);
        row2.add(lbl2c);
        this.clockSet.setFont(sans);
        row2.add(this.clockSet);

        final JLabel lbl2d = new JLabel(" Base:");
        lbl2d.setFont(sans);
        row2.add(lbl2d);
        this.clockBase.setFont(sans);
        row2.add(this.clockBase);

        final JLabel lbl2e = new JLabel(" Last:");
        lbl2e.setFont(sans);
        row2.add(lbl2e);
        this.clockLastTime.setFont(sans);
        row2.add(this.clockLastTime);

        nest2.add(row2, BorderLayout.PAGE_START);

        return delayClock;
    }

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    @Override
    public void calcState(final Calc theCalc) {

        // Called from the AWT event thread while the calculator thread is suspended

        if (this.isShowing()) {
            final PIOContext pio = theCalc.getCPU().getPIOContext();

            this.model.setText(pio.getModel().name());
            this.deviceTableModel.update(theCalc.getCPU().getPIOContext());

            final STDINT stdint = pio.getStdint();
            this.intactive.setText(Debugger.toHex2(stdint.getIntactive()));
            this.lastchk1.setText(Float.toString((float) stdint.getLastchk1()));
            this.timermax1.setText(Float.toString((float) stdint.getTimermax1()));
            this.lastchk2.setText(Float.toString((float) stdint.getLastchk2()));
            this.timermax2.setText(Float.toString((float) stdint.getTimermax2()));
            this.freq[0].setText(Float.toString((float) stdint.getFreq(0)));
            this.freq[1].setText(Float.toString((float) stdint.getFreq(1)));
            this.freq[2].setText(Float.toString((float) stdint.getFreq(2)));
            this.freq[3].setText(Float.toString((float) stdint.getFreq(3)));
            this.mem.setText(Debugger.toHex2(stdint.getMem()));
            this.xy.setText(Debugger.toHex2(stdint.getXy()));
            this.onBackup.setText(Integer.toString(stdint.getOnBackup()));
            this.onLatch.setSelected(stdint.isOnLatch());

            final Link link = pio.getLink();
            this.host.setText(Debugger.toHex2(link.getHost()));
            if (link.getClient() == null) {
                this.client.setText("-");
            } else {
                this.client.setText(Debugger.toHex2(link.getClient()[0]));
            }
            this.vout.setText(Debugger.toHex2(link.getVout()));
            if (link.getVin() == null) {
                this.vin.setText("-");
            } else {
                this.vin.setText(Debugger.toHex2(link.getVin()[0]));
            }
            this.vlinkSend.setText(Debugger.toHex2(link.getVlinkSend()));
            this.vlinkRecv.setText(Debugger.toHex2(link.getVlinkRecv()));
            this.vlinkSize.setText(Integer.toString(link.getVlinkSize()));
            this.hasChanged.setSelected(link.isHasChanged());
            this.changedTime.setText(Long.toString(link.getChangedTime()));

            LinkAssist assist = pio.getLinkAssist();
            if (assist == null) {
                assist = pio.getSeAux().getLinka();
            }
            if (assist == null) {
                this.linkEnable.setText("-");
                this.lastAccess.setText("-");
                this.in.setText("-");
                this.out.setText("-");
                this.working.setText("-");
                this.bit.setText("-");
                this.sending.setSelected(false);
                this.receiving.setSelected(false);
                this.read.setSelected(false);
                this.ready.setSelected(false);
                this.error.setSelected(false);
            } else {
                this.linkEnable.setText(Debugger.toHex2(assist.getLinkEnable()));
                this.lastAccess.setText(Float.toString((float) assist.getLastAccess()));
                this.in.setText(Debugger.toHex2(assist.getIn()));
                this.out.setText(Debugger.toHex2(assist.getOut()));
                this.working.setText(Debugger.toHex2(assist.getWorking()));
                this.bit.setText(Debugger.toHex2(assist.getBit()));
                this.sending.setSelected(assist.isSending());
                this.receiving.setSelected(assist.isReceiving());
                this.read.setSelected(assist.isRead());
                this.ready.setSelected(assist.isReady());
                this.error.setSelected(assist.isError());
            }

            XTAL xtal = null;
            if (pio.getSeAux() != null) {
                xtal = pio.getSeAux().getXtal();
            }

            if (xtal == null) {
                this.xtalLastTime.setText("-");
                this.xtalTicks.setText("-");
                for (int i = 0; i < 3; ++i) {
                    this.timerLastTStates[i].setText("-");
                    this.timerLastTicks[i].setText("-");
                    this.timerDivisor[i].setText("-");
                    this.timerLoop[i].setSelected(false);
                    this.timerInterrupt[i].setSelected(false);
                    this.timerUnderflow[i].setSelected(false);
                    this.timerGenerate[i].setSelected(false);
                    this.timerActive[i].setSelected(false);
                    this.timerClock[i].setText("-");
                    this.timerCount[i].setText("-");
                    this.timerMax[i].setText("-");
                }
            } else {
                this.xtalLastTime.setText(Float.toString((float) xtal.getLastTime()));
                this.xtalTicks.setText(Long.toString(xtal.getTicks()));
                for (int i = 0; i < 3; ++i) {
                    final Timer timer = xtal.getTimer(i);

                    this.timerLastTStates[i].setText(Long.toString(timer.getLastTstates()));
                    this.timerLastTicks[i].setText(Float.toString((float) timer.getLastTicks()));
                    this.timerDivisor[i].setText(Float.toString((float) timer.getDivisor()));
                    this.timerLoop[i].setSelected(timer.isLoop());
                    this.timerInterrupt[i].setSelected(timer.isInterrupt());
                    this.timerUnderflow[i].setSelected(timer.isUnderflow());
                    this.timerGenerate[i].setSelected(timer.isGenerate());
                    this.timerActive[i].setSelected(timer.isActive());
                    this.timerClock[i].setText(Debugger.toHex2(timer.getClock()));
                    this.timerCount[i].setText(Debugger.toHex2(timer.getCount()));
                    this.timerMax[i].setText(Debugger.toHex2(timer.getMax()));
                }
            }

            Delay delay = null;
            if (pio.getSeAux() != null) {
                delay = pio.getSeAux().getDelay();
            }

            if (delay == null) {
                this.delayLcd1.setText("-");
                this.delayLcd2.setText("-");
                this.delayLcd3.setText("-");
                this.delayLcd4.setText("-");
                this.delayMad.setText("-");
                this.delayUnk.setText("-");
                this.delayLcdWait.setText("-");
            } else {
                this.delayLcd1.setText(Integer.toString(delay.getLcd1()));
                this.delayLcd2.setText(Integer.toString(delay.getLcd2()));
                this.delayLcd3.setText(Integer.toString(delay.getLcd3()));
                this.delayLcd4.setText(Integer.toString(delay.getLcd4()));
                this.delayMad.setText(Integer.toString(delay.getMad()));
                this.delayUnk.setText(Integer.toString(delay.getUnknown()));
                this.delayLcdWait.setText(Integer.toString(delay.getLcdWait()));
            }

            Clock clock = null;
            if (pio.getSeAux() != null) {
                clock = pio.getSeAux().getClock();
            }

            if (clock == null) {
                this.clockEnable.setText("-");
                this.clockSet.setText("-");
                this.clockBase.setText("-");
                this.clockLastTime.setText("-");
            } else {
                this.clockEnable.setText(Debugger.toHex2(clock.getEnable()));
                this.clockSet.setText(Long.toString(clock.getSet()));
                this.clockBase.setText(Long.toString(clock.getBase()));
                this.clockLastTime.setText(Float.toString((float) clock.getLasttime()));
            }
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.model.setEnabled(enable);

        this.deviceTable.setEnabled(enable);
        this.deviceTable.setBackground(enable ? Color.WHITE : GRID_COLOR);

        this.intactive.setEnabled(enable);
        this.lastchk1.setEnabled(enable);
        this.timermax1.setEnabled(enable);
        this.lastchk2.setEnabled(enable);
        this.timermax2.setEnabled(enable);
        this.freq[0].setEnabled(enable);
        this.freq[1].setEnabled(enable);
        this.freq[2].setEnabled(enable);
        this.freq[3].setEnabled(enable);
        this.mem.setEnabled(enable);
        this.xy.setEnabled(enable);
        this.onBackup.setEnabled(enable);
        this.onLatch.setEnabled(enable);
        this.host.setEnabled(enable);
        this.client.setEnabled(enable);
        this.vlinkSend.setEnabled(enable);
        this.vlinkRecv.setEnabled(enable);
        this.vlinkSize.setEnabled(enable);
        this.vout.setEnabled(enable);
        this.vin.setEnabled(enable);
        this.hasChanged.setEnabled(enable);
        this.changedTime.setEnabled(enable);
        this.linkEnable.setEnabled(enable);
        this.lastAccess.setEnabled(enable);
        this.in.setEnabled(enable);
        this.out.setEnabled(enable);
        this.working.setEnabled(enable);
        this.bit.setEnabled(enable);
        this.sending.setEnabled(enable);
        this.receiving.setEnabled(enable);
        this.read.setEnabled(enable);
        this.ready.setEnabled(enable);
        this.error.setEnabled(enable);
        this.xtalLastTime.setEnabled(enable);
        this.xtalTicks.setEnabled(enable);

        for (int i = 0; i < 3; ++i) {
            this.timerLastTStates[i].setEnabled(enable);
            this.timerLastTicks[i].setEnabled(enable);
            this.timerDivisor[i].setEnabled(enable);
            this.timerLoop[i].setEnabled(enable);
            this.timerInterrupt[i].setEnabled(enable);
            this.timerUnderflow[i].setEnabled(enable);
            this.timerGenerate[i].setEnabled(enable);
            this.timerActive[i].setEnabled(enable);
            this.timerClock[i].setEnabled(enable);
            this.timerCount[i].setEnabled(enable);
            this.timerMax[i].setEnabled(enable);
        }

        this.delayLcd1.setEnabled(enable);
        this.delayLcd2.setEnabled(enable);
        this.delayLcd3.setEnabled(enable);
        this.delayLcd4.setEnabled(enable);
        this.delayMad.setEnabled(enable);
        this.delayUnk.setEnabled(enable);
        this.delayLcdWait.setEnabled(enable);
        this.clockEnable.setEnabled(enable);
        this.clockSet.setEnabled(enable);
        this.clockBase.setEnabled(enable);
        this.clockLastTime.setEnabled(enable);
    }

    /**
     * A table model for a list of installed devices.
     */
    private static final class DeviceTableModel extends AbstractTableModel {

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -2018307505336902880L;

        /** The list of device indexes. */
        private int[] devList;

        /** The list of device names. */
        private String[] devNames;

        /** The list of last input values. */
        private int[] lastInput;

        /** The list of last output values. */
        private int[] lastOutput;

        /** The list of skip factors for interrupts. */
        private int[] skipFactors;

        /** The list of skip counts for interrupts. */
        private int[] skipCounts;

        /**
         * Constructs a new {@code DeviceTableModel}.
         */
        DeviceTableModel() {

            super();

            this.devList = new int[0];
            this.devNames = new String[0];
            this.lastInput = this.devList;
            this.lastOutput = this.devList;
            this.skipFactors = new int[0];
            this.skipCounts = new int[0];
        }

        /**
         * Updates the table data.
         *
         * @param pio the PIO context from which to update
         */
        void update(final PIOContext pio) {

            this.devList = pio.getDeviceIndexes();
            final int numDevs = this.devList.length;

            this.devNames = new String[numDevs];
            this.lastInput = new int[numDevs];
            this.lastOutput = new int[numDevs];
            this.skipFactors = new int[numDevs];
            this.skipCounts = new int[numDevs];

            for (int i = 0; i < numDevs; ++i) {
                final IDevice dev = pio.getDevice(this.devList[i]);
                this.devNames[i] = dev.getClass().getSimpleName();
                this.lastInput[i] = pio.getMostRecentInput(this.devList[i]);
                this.lastOutput[i] = pio.getMostRecentOutput(this.devList[i]);
                this.skipFactors[i] = -1;
                this.skipCounts[i] = -1;
            }

            for (int i = 0; i < pio.getNumInterrupt(); ++i) {
                final Interrupt inter = pio.getInterrupt(i);
                for (int j = 0; j < numDevs; ++j) {
                    if (this.devList[j] == inter.getInterruptVal()) {
                        this.skipFactors[j] = inter.getSkipFactor();
                        this.skipCounts[j] = inter.getSkipCount();
                        break;
                    }
                }
            }

            fireTableDataChanged();
        }

        /**
         * Gets the number of rows.
         *
         * @return the number of rows
         */
        @Override
        public int getRowCount() {

            return this.devList.length;
        }

        /**
         * Gets the number of columns.
         *
         * @return the number of columns
         */
        @Override
        public int getColumnCount() {

            return 6;
        }

        /**
         * Gets the name of a column.
         */
        @Override
        public String getColumnName(final int column) {

            final String name;

            if (column == 0) {
                name = "Port";
            } else if (column == 1) {
                name = "Name";
            } else if (column == 2) {
                name = "Last In";
            } else if (column == 3) {
                name = "Last Out";
            } else if (column == 4) {
                name = "Skip Factor";
            } else if (column == 5) {
                name = "Skip Count";
            } else {
                name = "?";
            }

            return name;
        }

        /**
         * Gets the value at a cell.
         *
         * @param rowIndex    the row
         * @param columnIndex the column
         * @return the value
         */
        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {

            final StringBuilder result = new StringBuilder(10);

            if (rowIndex >= 0 && rowIndex < this.devList.length) {

                final int index = this.devList[rowIndex];
                if (index < 0) {
                    result.append("ERR");
                } else if (columnIndex == 0) {
                    result.append(Integer.toHexString((index >> 4) & 0x0F));
                    result.append(Integer.toHexString(index & 0x0F));
                } else if (columnIndex == 1) {
                    result.append(this.devNames[rowIndex]);
                } else if (columnIndex == 2) {
                    final int input = this.lastInput[rowIndex];
                    if (input == -1) {
                        result.append('-');
                    } else {
                        result.append("0x");
                        result.append(Integer.toHexString((input >> 4) & 0x0F));
                        result.append(Integer.toHexString(input & 0x0F));
                    }
                } else if (columnIndex == 3) {
                    final int output = this.lastOutput[rowIndex];
                    if (output == -1) {
                        result.append('-');
                    } else {
                        result.append("0x");
                        result.append(Integer.toHexString((output >> 4) & 0x0F));
                        result.append(Integer.toHexString(output & 0x0F));
                    }
                } else if (columnIndex == 4) {
                    final int output = this.skipFactors[rowIndex];
                    if (output == -1) {
                        result.append('-');
                    } else {
                        result.append(output);
                    }
                } else if (columnIndex == 5) {
                    final int output = this.skipCounts[rowIndex];
                    if (output == -1) {
                        result.append('-');
                    } else {
                        result.append(output);
                    }
                }
            }

            return result.toString();
        }

        /**
         * Checks whether a cell is editable.
         *
         * @param rowIndex    the row
         * @param columnIndex the column
         * @return true if editable, false if not
         */
        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {

            return false;
        }
    }
}
