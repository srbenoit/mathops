package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.BankState;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel that displays details of a memory context.
 */
final class MemoryContextDetailPanel extends LoggedPanel implements ICalcStateListener {

    /** Background color. */
    private static final Color FIELD_COLOR = new Color(0, 80, 80);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5749929782305280741L;

    /** The boot mapped check box. */
    private final JCheckBox bootmapped;

    /** The normal banks radio button box. */
    private final JRadioButton useNormalBanks;

    /** The boot banks radio button box. */
    private final JRadioButton useBootBanks;

    /** The changed page 0 check box. */
    private final JCheckBox changed0;

    /** The flash locked check box. */
    private final JCheckBox flashLocked;

    /** The RAM version. */
    private final JTextField ramVersion;

    /** The number of RAM pages. */
    private final JTextField ramPages;

    /** The total RAM size. */
    private final JTextField ramSize;

    /** The RAM lower limit. */
    private final JTextField ramLower;

    /** The RAM upper limit. */
    private final JTextField ramUpper;

    /** The Flash version. */
    private final JTextField flashVersion;

    /** The number of Flash pages. */
    private final JTextField flashPages;

    /** The total Flash size. */
    private final JTextField flashSize;

    /** The Flash lower limit. */
    private final JTextField flashLower;

    /** The Flash upper limit. */
    private final JTextField flashUpper;

    /** The flash step. */
    private final JTextField flashStep;

    /** The flash write delay. */
    private final JTextField flashWriteDelay;

    /** The flash write byte. */
    private final JTextField flashWriteByte;

    /** The flash error flag. */
    private final JTextField flashError;

    /** The flash toggles. */
    private final JTextField flashToggles;

    /** The protection mode. */
    private final JTextField protMode;

    /** The protected page set. */
    private final JTextField protPageSet;

    /** The protected page 0. */
    private final JTextField protPages0;

    /** The protected page 1. */
    private final JTextField protPages1;

    /** The protected page 2. */
    private final JTextField protPages2;

    /** The protected page 3. */
    private final JTextField protPages3;

    /** The flash read op time. */
    private final JTextField flashReadOp;

    /** The flash read nop time. */
    private final JTextField flashReadNop;

    /** The flash write time. */
    private final JTextField flashWrite;

    /** The ram read op time. */
    private final JTextField ramReadOp;

    /** The ram read nop time. */
    private final JTextField ramReadNop;

    /** The ram write time. */
    private final JTextField ramWrite;

    /** The port 06 data. */
    private final JTextField port06;

    /** The port 07 data. */
    private final JTextField port07;

    /** The port 0E data. */
    private final JTextField port0E;

    /** The port 0F data. */
    private final JTextField port0F;

    /** The port 24 data. */
    private final JTextField port24;

    /** The port 27 data. */
    private final JTextField port27;

    /** The port 28 data. */
    private final JTextField port28;

    /** The normal bank pages. */
    private final JTextField[] normalPage;

    /** The normal bank addresses. */
    private final JTextField[] normalAddr;

    /** The normal read-only checkbox. */
    private final JCheckBox[] normalRO;

    /** The normal RAM checkbox. */
    private final JCheckBox[] normalRam;

    /** The normal no-exec checkbox. */
    private final JCheckBox[] normalNoExec;

    /** The boot map bank addresses. */
    private final JTextField[] bootmapAddr;

    /** The boot map bank pages. */
    private final JTextField[] bootmapPage;

    /** The boot map read-only checkbox. */
    private final JCheckBox[] bootmapRO;

    /** The boot map RAM checkbox. */
    private final JCheckBox[] bootmapRam;

    /** The boot map no-exec checkbox. */
    private final JCheckBox[] bootmapNoExec;

    /**
     * Constructs a new {@code MemoryContextDetailPanel}.
     */
    MemoryContextDetailPanel() {

        super(new BorderLayout(4, 4));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font bold = Fonts.getSans().deriveFont(Font.BOLD, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        setBackground(Debugger.BG_COLOR);

        // Row 1: general settings

        final JPanel genRow = new JPanel(new BorderLayout(4, 4));
        genRow.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        genRow.setBackground(Debugger.BG_COLOR);
        add(genRow, BorderLayout.PAGE_START);

        final JPanel genRowFields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        genRowFields.setBackground(Debugger.BG_COLOR);
        genRow.add(genRowFields, BorderLayout.PAGE_END);

        this.bootmapped = new JCheckBox("Bootmapped  ");
        this.bootmapped.setBackground(Debugger.BG_COLOR);
        this.bootmapped.setFont(sans);
        genRowFields.add(this.bootmapped);

        final ButtonGroup group = new ButtonGroup();

        this.useNormalBanks = new JRadioButton("Normal");
        this.useNormalBanks.setBackground(Debugger.BG_COLOR);
        this.useNormalBanks.setFont(sans);
        group.add(this.useNormalBanks);
        genRowFields.add(this.useNormalBanks);
        this.useBootBanks = new JRadioButton("Boot   ");
        this.useBootBanks.setBackground(Debugger.BG_COLOR);
        this.useBootBanks.setFont(sans);
        group.add(this.useBootBanks);
        genRowFields.add(this.useBootBanks);

        final JLabel spacer2 = new JLabel("   ");
        spacer2.setFont(sans);
        genRowFields.add(spacer2);

        this.changed0 = new JCheckBox("Changed Page 0  ");
        this.changed0.setBackground(Debugger.BG_COLOR);
        this.changed0.setFont(sans);
        genRowFields.add(this.changed0);

        this.flashLocked = new JCheckBox("Flash Locked  ");
        this.flashLocked.setBackground(Debugger.BG_COLOR);
        this.flashLocked.setFont(sans);
        genRowFields.add(this.flashLocked);

        final JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        nest1.setBackground(Debugger.BG_COLOR);
        add(nest1, BorderLayout.CENTER);

        // Row 2: RAM

        final JPanel ramRow = new JPanel(new BorderLayout(4, 4));
        ramRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        ramRow.setBackground(Debugger.BG_COLOR);
        nest1.add(ramRow, BorderLayout.PAGE_START);

        final JLabel ramRowTitle = new JLabel("RAM Memory:");
        ramRowTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        ramRowTitle.setFont(bold);
        ramRow.add(ramRowTitle, BorderLayout.PAGE_START);

        final JPanel ramRowFields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        ramRowFields.setBackground(Debugger.BG_COLOR);
        ramRow.add(ramRowFields, BorderLayout.PAGE_END);

        final JLabel ramlbl1 = new JLabel("  Version: ");
        ramlbl1.setFont(sans);
        ramRowFields.add(ramlbl1);
        this.ramVersion = new JTextField("-");
        this.ramVersion.setFont(mono);
        this.ramVersion.setForeground(FIELD_COLOR);
        this.ramVersion
                .setPreferredSize(new Dimension(40, this.ramVersion.getPreferredSize().height));
        ramRowFields.add(this.ramVersion);

        final JLabel ramlbl2 = new JLabel("  Pages: ");
        ramlbl2.setFont(sans);
        ramRowFields.add(ramlbl2);
        this.ramPages = new JTextField("-");
        this.ramPages.setFont(mono);
        this.ramPages.setForeground(FIELD_COLOR);
        this.ramPages.setPreferredSize(new Dimension(50, this.ramPages.getPreferredSize().height));
        ramRowFields.add(this.ramPages);

        final JLabel ramlbl3 = new JLabel("  Size: ");
        ramlbl3.setFont(sans);
        ramRowFields.add(ramlbl3);
        this.ramSize = new JTextField("-");
        this.ramSize.setFont(mono);
        this.ramSize.setForeground(FIELD_COLOR);
        this.ramSize.setPreferredSize(new Dimension(90, this.ramSize.getPreferredSize().height));
        ramRowFields.add(this.ramSize);

        final JLabel ramlbl4 = new JLabel("  Lower: ");
        ramlbl4.setFont(sans);
        ramRowFields.add(ramlbl4);
        this.ramLower = new JTextField("-");
        this.ramLower.setFont(mono);
        this.ramLower.setForeground(FIELD_COLOR);
        this.ramLower.setPreferredSize(new Dimension(60, this.ramLower.getPreferredSize().height));
        ramRowFields.add(this.ramLower);

        final JLabel ramlbl5 = new JLabel("  Upper: ");
        ramlbl5.setFont(sans);
        ramRowFields.add(ramlbl5);
        this.ramUpper = new JTextField("-");
        this.ramUpper.setFont(mono);
        this.ramUpper.setForeground(FIELD_COLOR);
        this.ramUpper.setPreferredSize(new Dimension(60, this.ramUpper.getPreferredSize().height));
        ramRowFields.add(this.ramUpper);

        final JPanel nest2 = new JPanel(new BorderLayout(4, 4));
        nest2.setBackground(Debugger.BG_COLOR);
        nest1.add(nest2, BorderLayout.CENTER);

        // Row 3: Flash

        final JPanel flashRow = new JPanel(new BorderLayout(4, 8));
        flashRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        flashRow.setBackground(Debugger.BG_COLOR);
        nest2.add(flashRow, BorderLayout.PAGE_START);

        final JLabel flashRowTitle = new JLabel("Flash Memory:");
        flashRowTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        flashRowTitle.setFont(bold);
        flashRow.add(flashRowTitle, BorderLayout.PAGE_START);

        final JPanel flashRow1Fields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        flashRow1Fields.setBackground(Debugger.BG_COLOR);
        flashRow.add(flashRow1Fields, BorderLayout.CENTER);

        final JLabel flashlbl1 = new JLabel("  Version: ");
        flashlbl1.setFont(sans);
        flashRow1Fields.add(flashlbl1);
        this.flashVersion = new JTextField("-");
        this.flashVersion.setFont(mono);
        this.flashVersion.setForeground(FIELD_COLOR);
        this.flashVersion
                .setPreferredSize(new Dimension(40, this.flashVersion.getPreferredSize().height));
        flashRow1Fields.add(this.flashVersion);

        final JLabel flashlbl2 = new JLabel("  Pages: ");
        flashlbl2.setFont(sans);
        flashRow1Fields.add(flashlbl2);
        this.flashPages = new JTextField("-");
        this.flashPages.setFont(mono);
        this.flashPages.setForeground(FIELD_COLOR);
        this.flashPages
                .setPreferredSize(new Dimension(50, this.flashPages.getPreferredSize().height));
        flashRow1Fields.add(this.flashPages);

        final JLabel flashlbl3 = new JLabel("  Size: ");
        flashlbl3.setFont(sans);
        flashRow1Fields.add(flashlbl3);
        this.flashSize = new JTextField("-");
        this.flashSize.setFont(mono);
        this.flashSize.setForeground(FIELD_COLOR);
        this.flashSize.setPreferredSize(new Dimension(90, this.ramSize.getPreferredSize().height));
        flashRow1Fields.add(this.flashSize);

        final JLabel flashlbl4 = new JLabel("  Lower: ");
        flashlbl4.setFont(sans);
        flashRow1Fields.add(flashlbl4);
        this.flashLower = new JTextField("-");
        this.flashLower.setFont(mono);
        this.flashLower.setForeground(FIELD_COLOR);
        this.flashLower
                .setPreferredSize(new Dimension(60, this.flashLower.getPreferredSize().height));
        flashRow1Fields.add(this.flashLower);

        final JLabel flashlbl5 = new JLabel("  Upper: ");
        flashlbl5.setFont(sans);
        flashRow1Fields.add(flashlbl5);
        this.flashUpper = new JTextField("-");
        this.flashUpper.setFont(mono);
        this.flashUpper.setForeground(FIELD_COLOR);
        this.flashUpper
                .setPreferredSize(new Dimension(60, this.flashUpper.getPreferredSize().height));
        flashRow1Fields.add(this.flashUpper);

        final JPanel flashRow2Fields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        flashRow2Fields.setBackground(Debugger.BG_COLOR);
        flashRow.add(flashRow2Fields, BorderLayout.PAGE_END);

        final JLabel flashlbl6 = new JLabel("  Step: ");
        flashlbl6.setFont(sans);
        flashRow2Fields.add(flashlbl6);
        this.flashStep = new JTextField("-");
        this.flashStep.setFont(mono);
        this.flashStep.setForeground(FIELD_COLOR);
        this.flashStep
                .setPreferredSize(new Dimension(140, this.flashStep.getPreferredSize().height));
        flashRow2Fields.add(this.flashStep);

        final JLabel flashlbl7 = new JLabel("  Write Delay: ");
        flashlbl7.setFont(sans);
        flashRow2Fields.add(flashlbl7);
        this.flashWriteDelay = new JTextField("-");
        this.flashWriteDelay.setFont(mono);
        this.flashWriteDelay.setForeground(FIELD_COLOR);
        this.flashWriteDelay
                .setPreferredSize(new Dimension(50, this.flashWriteDelay.getPreferredSize().height));
        flashRow2Fields.add(this.flashWriteDelay);

        final JLabel flashlbl8 = new JLabel("  Write Byte: ");
        flashlbl8.setFont(sans);
        flashRow2Fields.add(flashlbl8);
        this.flashWriteByte = new JTextField("-");
        this.flashWriteByte.setFont(mono);
        this.flashWriteByte.setForeground(FIELD_COLOR);
        this.flashWriteByte
                .setPreferredSize(new Dimension(40, this.flashWriteByte.getPreferredSize().height));
        flashRow2Fields.add(this.flashWriteByte);

        final JLabel flashlbl9 = new JLabel("  Error: ");
        flashlbl9.setFont(sans);
        flashRow2Fields.add(flashlbl9);
        this.flashError = new JTextField("-");
        this.flashError.setFont(mono);
        this.flashError.setForeground(FIELD_COLOR);
        this.flashError
                .setPreferredSize(new Dimension(40, this.flashError.getPreferredSize().height));
        flashRow2Fields.add(this.flashError);

        final JLabel flashlbl10 = new JLabel("  Toggles: ");
        flashlbl10.setFont(sans);
        flashRow2Fields.add(flashlbl10);
        this.flashToggles = new JTextField("-");
        this.flashToggles.setFont(mono);
        this.flashToggles.setForeground(FIELD_COLOR);
        this.flashToggles
                .setPreferredSize(new Dimension(40, this.flashToggles.getPreferredSize().height));
        flashRow2Fields.add(this.flashToggles);

        final JPanel nest3 = new JPanel(new BorderLayout(4, 4));
        nest3.setBackground(Debugger.BG_COLOR);
        nest2.add(nest3, BorderLayout.CENTER);

        // Row 4: Page Protections

        final JPanel protRow = new JPanel(new BorderLayout(4, 4));
        protRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        protRow.setBackground(Debugger.BG_COLOR);
        nest3.add(protRow, BorderLayout.PAGE_START);

        final JLabel protRowTitle = new JLabel("Page Protection:");
        protRowTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        protRowTitle.setFont(bold);
        protRow.add(protRowTitle, BorderLayout.PAGE_START);

        final JPanel protRowFields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        protRowFields.setBackground(Debugger.BG_COLOR);
        protRow.add(protRowFields, BorderLayout.PAGE_END);

        final JLabel protlbl1 = new JLabel("  Mode: ");
        protlbl1.setFont(sans);
        protRowFields.add(protlbl1);
        this.protMode = new JTextField("-");
        this.protMode.setFont(mono);
        this.protMode.setForeground(FIELD_COLOR);
        this.protMode.setPreferredSize(new Dimension(40, this.protMode.getPreferredSize().height));
        protRowFields.add(this.protMode);

        final JLabel protlbl2 = new JLabel("  Page Set: ");
        protlbl2.setFont(sans);
        protRowFields.add(protlbl2);
        this.protPageSet = new JTextField("-");
        this.protPageSet.setFont(mono);
        this.protPageSet.setForeground(FIELD_COLOR);
        this.protPageSet
                .setPreferredSize(new Dimension(40, this.protPageSet.getPreferredSize().height));
        protRowFields.add(this.protPageSet);

        final JLabel protlbl3 = new JLabel("  Pages: ");
        protlbl3.setFont(sans);
        protRowFields.add(protlbl3);
        this.protPages0 = new JTextField("-");
        this.protPages0.setFont(mono);
        this.protPages0.setForeground(FIELD_COLOR);
        this.protPages0
                .setPreferredSize(new Dimension(40, this.protPageSet.getPreferredSize().height));
        protRowFields.add(this.protPages0);

        this.protPages1 = new JTextField("-");
        this.protPages1.setFont(mono);
        this.protPages1.setForeground(FIELD_COLOR);
        this.protPages1
                .setPreferredSize(new Dimension(40, this.protPages1.getPreferredSize().height));
        protRowFields.add(this.protPages1);

        this.protPages2 = new JTextField("-");
        this.protPages2.setFont(mono);
        this.protPages2.setForeground(FIELD_COLOR);
        this.protPages2
                .setPreferredSize(new Dimension(40, this.protPages2.getPreferredSize().height));
        protRowFields.add(this.protPages2);

        this.protPages3 = new JTextField("-");
        this.protPages3.setFont(mono);
        this.protPages3.setForeground(FIELD_COLOR);
        this.protPages3
                .setPreferredSize(new Dimension(40, this.protPages3.getPreferredSize().height));
        protRowFields.add(this.protPages3);

        final JPanel nest4 = new JPanel(new BorderLayout(4, 4));
        nest4.setBackground(Debugger.BG_COLOR);
        nest3.add(nest4, BorderLayout.CENTER);

        // Row 4: Timing

        final JPanel timingRow = new JPanel(new BorderLayout(4, 4));
        timingRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        timingRow.setBackground(Debugger.BG_COLOR);
        nest4.add(timingRow, BorderLayout.PAGE_START);

        final JLabel tstatesRowTitle = new JLabel("Timings:");
        tstatesRowTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        tstatesRowTitle.setFont(bold);
        timingRow.add(tstatesRowTitle, BorderLayout.PAGE_START);

        final JPanel tstatesRowFields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        tstatesRowFields.setBackground(Debugger.BG_COLOR);
        timingRow.add(tstatesRowFields, BorderLayout.PAGE_END);

        final JLabel tstatelbl0 = new JLabel("  Flash ");
        tstatelbl0.setFont(bold);
        tstatesRowFields.add(tstatelbl0);

        final JLabel tstatelbl1 = new JLabel("  Rd/Op: ");
        tstatelbl1.setFont(sans);
        tstatesRowFields.add(tstatelbl1);
        this.flashReadOp = new JTextField("-");
        this.flashReadOp.setFont(mono);
        this.flashReadOp.setForeground(FIELD_COLOR);
        this.flashReadOp
                .setPreferredSize(new Dimension(40, this.flashReadOp.getPreferredSize().height));
        tstatesRowFields.add(this.flashReadOp);

        final JLabel tstatelbl2 = new JLabel("  Rd/Nop: ");
        tstatelbl2.setFont(sans);
        tstatesRowFields.add(tstatelbl2);
        this.flashReadNop = new JTextField("-");
        this.flashReadNop.setFont(mono);
        this.flashReadNop.setForeground(FIELD_COLOR);
        this.flashReadNop
                .setPreferredSize(new Dimension(40, this.flashReadNop.getPreferredSize().height));
        tstatesRowFields.add(this.flashReadNop);

        final JLabel tstatelbl3 = new JLabel("  Wr: ");
        tstatelbl3.setFont(sans);
        tstatesRowFields.add(tstatelbl3);
        this.flashWrite = new JTextField("-");
        this.flashWrite.setFont(mono);
        this.flashWrite.setForeground(FIELD_COLOR);
        this.flashWrite
                .setPreferredSize(new Dimension(40, this.flashWrite.getPreferredSize().height));
        tstatesRowFields.add(this.flashWrite);

        final JLabel tstatelbl4 = new JLabel("      RAM ");
        tstatelbl4.setFont(bold);
        tstatesRowFields.add(tstatelbl4);

        final JLabel tstatelbl5 = new JLabel("  Rd/Op: ");
        tstatelbl5.setFont(sans);
        tstatesRowFields.add(tstatelbl5);
        this.ramReadOp = new JTextField("-");
        this.ramReadOp.setFont(mono);
        this.ramReadOp.setForeground(FIELD_COLOR);
        this.ramReadOp
                .setPreferredSize(new Dimension(40, this.ramReadOp.getPreferredSize().height));
        tstatesRowFields.add(this.ramReadOp);

        final JLabel tstatelbl6 = new JLabel("  Rd/Nop: ");
        tstatelbl6.setFont(sans);
        tstatesRowFields.add(tstatelbl6);
        this.ramReadNop = new JTextField("-");
        this.ramReadNop.setFont(mono);
        this.ramReadNop.setForeground(FIELD_COLOR);
        this.ramReadNop
                .setPreferredSize(new Dimension(40, this.ramReadNop.getPreferredSize().height));
        tstatesRowFields.add(this.ramReadNop);

        final JLabel tstatelbl7 = new JLabel("  Wr: ");
        tstatelbl7.setFont(sans);
        tstatesRowFields.add(tstatelbl7);
        this.ramWrite = new JTextField("-");
        this.ramWrite.setFont(mono);
        this.ramWrite.setForeground(FIELD_COLOR);
        this.ramWrite.setPreferredSize(new Dimension(40, this.ramWrite.getPreferredSize().height));
        tstatesRowFields.add(this.ramWrite);

        final JPanel nest5 = new JPanel(new BorderLayout(4, 4));
        nest5.setBackground(Debugger.BG_COLOR);
        nest4.add(nest5, BorderLayout.CENTER);

        // Row 5: Port Data

        final JPanel portRow = new JPanel(new BorderLayout(4, 4));
        portRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        portRow.setBackground(Debugger.BG_COLOR);
        nest5.add(portRow, BorderLayout.PAGE_START);

        final JLabel portRowTitle = new JLabel("Port Data:");
        portRowTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        portRowTitle.setFont(bold);
        portRow.add(portRowTitle, BorderLayout.PAGE_START);

        final JPanel portRowFields = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        portRowFields.setBackground(Debugger.BG_COLOR);
        portRow.add(portRowFields, BorderLayout.PAGE_END);

        final JLabel portlbl1 = new JLabel("  06: ");
        portlbl1.setFont(sans);
        portRowFields.add(portlbl1);
        this.port06 = new JTextField("-");
        this.port06.setFont(mono);
        this.port06.setForeground(FIELD_COLOR);
        this.port06.setPreferredSize(new Dimension(40, this.port06.getPreferredSize().height));
        portRowFields.add(this.port06);

        final JLabel portlbl2 = new JLabel("  07: ");
        portlbl2.setFont(sans);
        portRowFields.add(portlbl2);
        this.port07 = new JTextField("-");
        this.port07.setFont(mono);
        this.port07.setForeground(FIELD_COLOR);
        this.port07.setPreferredSize(new Dimension(40, this.port07.getPreferredSize().height));
        portRowFields.add(this.port07);

        final JLabel portlbl3 = new JLabel("  0E: ");
        portlbl3.setFont(sans);
        portRowFields.add(portlbl3);
        this.port0E = new JTextField("-");
        this.port0E.setFont(mono);
        this.port0E.setForeground(FIELD_COLOR);
        this.port0E.setPreferredSize(new Dimension(40, this.port0E.getPreferredSize().height));
        portRowFields.add(this.port0E);

        final JLabel portlbl4 = new JLabel("  0F: ");
        portlbl4.setFont(sans);
        portRowFields.add(portlbl4);
        this.port0F = new JTextField("-");
        this.port0F.setFont(mono);
        this.port0F.setForeground(FIELD_COLOR);
        this.port0F.setPreferredSize(new Dimension(40, this.port0F.getPreferredSize().height));
        portRowFields.add(this.port0F);

        final JLabel portlbl5 = new JLabel("  24: ");
        portlbl5.setFont(sans);
        portRowFields.add(portlbl5);
        this.port24 = new JTextField("-");
        this.port24.setFont(mono);
        this.port24.setForeground(FIELD_COLOR);
        this.port24.setPreferredSize(new Dimension(40, this.port24.getPreferredSize().height));
        portRowFields.add(this.port24);

        final JLabel portlbl6 = new JLabel("  27 Count: ");
        portlbl6.setFont(sans);
        portRowFields.add(portlbl6);
        this.port27 = new JTextField("-");
        this.port27.setFont(mono);
        this.port27.setForeground(FIELD_COLOR);
        this.port27.setPreferredSize(new Dimension(40, this.port27.getPreferredSize().height));
        portRowFields.add(this.port27);

        final JLabel portlbl7 = new JLabel("  28 Count: ");
        portlbl7.setFont(sans);
        portRowFields.add(portlbl7);
        this.port28 = new JTextField("-");
        this.port28.setFont(mono);
        this.port28.setForeground(FIELD_COLOR);
        this.port28.setPreferredSize(new Dimension(40, this.port28.getPreferredSize().height));
        portRowFields.add(this.port28);

        final JPanel nest6 = new JPanel(new BorderLayout(4, 4));
        nest6.setBackground(Debugger.BG_COLOR);
        nest5.add(nest6, BorderLayout.CENTER);

        // Row 6: Normal Banks

        final JPanel normalBankRow = new JPanel(new BorderLayout(4, 4));
        normalBankRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        normalBankRow.setBackground(Debugger.BG_COLOR);
        nest6.add(normalBankRow, BorderLayout.PAGE_START);

        final JLabel normalBankTitle = new JLabel("Normal Banks:");
        normalBankTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        normalBankTitle.setFont(bold);
        normalBankRow.add(normalBankTitle, BorderLayout.PAGE_START);

        final JPanel normalBankFields = new JPanel(new GridLayout(4, 1, 0, 0));
        normalBankFields.setBackground(Debugger.BG_COLOR);
        normalBankRow.add(normalBankFields, BorderLayout.PAGE_END);

        this.normalPage = new JTextField[4];
        this.normalAddr = new JTextField[4];
        this.normalRO = new JCheckBox[4];
        this.normalRam = new JCheckBox[4];
        this.normalNoExec = new JCheckBox[4];
        for (int i = 0; i < 4; ++i) {
            this.normalPage[i] = new JTextField("-");
            this.normalPage[i].setFont(mono);
            this.normalPage[i].setForeground(FIELD_COLOR);
            this.normalPage[i]
                    .setPreferredSize(new Dimension(40, this.normalPage[i].getPreferredSize().height));

            this.normalAddr[i] = new JTextField("-");
            this.normalAddr[i].setFont(mono);
            this.normalAddr[i].setForeground(FIELD_COLOR);
            this.normalAddr[i]
                    .setPreferredSize(new Dimension(70, this.normalAddr[i].getPreferredSize().height));

            this.normalRO[i] = new JCheckBox();
            this.normalRO[i].setBackground(Debugger.BG_COLOR);

            this.normalRam[i] = new JCheckBox();
            this.normalRam[i].setBackground(Debugger.BG_COLOR);

            this.normalNoExec[i] = new JCheckBox();
            this.normalNoExec[i].setBackground(Debugger.BG_COLOR);

            final JPanel normalRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            normalRow.setBackground(Debugger.BG_COLOR);
            final JLabel normallbl1 = new JLabel("  Page: ");
            normallbl1.setFont(sans);
            normalRow.add(normallbl1);
            normalRow.add(this.normalPage[i]);
            final JLabel normallbl2 = new JLabel("  Address: ");
            normallbl2.setFont(sans);
            normalRow.add(normallbl2);
            normalRow.add(this.normalAddr[i]);

            final JLabel normallbl3 = new JLabel("  ");
            normallbl3.setFont(sans);
            normalRow.add(normallbl3);

            normalRow.add(this.normalRam[i]);
            final JLabel normallbl4 = new JLabel("RAM  ");
            normallbl4.setFont(sans);
            normalRow.add(normallbl4);

            normalRow.add(this.normalRO[i]);
            final JLabel normallbl5 = new JLabel("Readonly  ");
            normallbl5.setFont(sans);
            normalRow.add(normallbl5);

            normalRow.add(this.normalNoExec[i]);
            final JLabel normallbl6 = new JLabel("No-exec  ");
            normallbl6.setFont(sans);
            normalRow.add(normallbl6);

            normalBankFields.add(normalRow);
        }

        final JPanel nest7 = new JPanel(new BorderLayout(4, 4));
        nest7.setBackground(Debugger.BG_COLOR);
        nest6.add(nest7, BorderLayout.CENTER);

        // Row 7: Bootmap Banks

        final JPanel bootmapBankRow = new JPanel(new BorderLayout(4, 4));
        bootmapBankRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bootmapBankRow.setBackground(Debugger.BG_COLOR);
        nest7.add(bootmapBankRow, BorderLayout.NORTH);

        final JLabel bootmapBankTitle = new JLabel("Bootmap Banks:");
        bootmapBankTitle.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        bootmapBankTitle.setFont(bold);
        bootmapBankRow.add(bootmapBankTitle, BorderLayout.PAGE_START);

        final JPanel bootmapBankFields = new JPanel(new GridLayout(4, 1, 0, 0));
        bootmapBankFields.setBackground(Debugger.BG_COLOR);
        bootmapBankRow.add(bootmapBankFields, BorderLayout.PAGE_END);

        this.bootmapPage = new JTextField[4];
        this.bootmapAddr = new JTextField[4];
        this.bootmapRO = new JCheckBox[4];
        this.bootmapRam = new JCheckBox[4];
        this.bootmapNoExec = new JCheckBox[4];
        for (int i = 0; i < 4; ++i) {
            this.bootmapPage[i] = new JTextField("-");
            this.bootmapPage[i].setFont(mono);
            this.bootmapPage[i].setForeground(FIELD_COLOR);
            this.bootmapPage[i]
                    .setPreferredSize(new Dimension(40, this.bootmapPage[i].getPreferredSize().height));

            this.bootmapAddr[i] = new JTextField("-");
            this.bootmapAddr[i].setFont(mono);
            this.bootmapAddr[i].setForeground(FIELD_COLOR);
            this.bootmapAddr[i]
                    .setPreferredSize(new Dimension(70, this.bootmapAddr[i].getPreferredSize().height));

            this.bootmapRO[i] = new JCheckBox();
            this.bootmapRO[i].setBackground(Debugger.BG_COLOR);

            this.bootmapRam[i] = new JCheckBox();
            this.bootmapRam[i].setBackground(Debugger.BG_COLOR);

            this.bootmapNoExec[i] = new JCheckBox();
            this.bootmapNoExec[i].setBackground(Debugger.BG_COLOR);

            final JPanel bootmapRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            bootmapRow.setBackground(Debugger.BG_COLOR);
            final JLabel bootmaplbl1 = new JLabel("  Page: ");
            bootmaplbl1.setFont(sans);
            bootmapRow.add(bootmaplbl1);
            bootmapRow.add(this.bootmapPage[i]);
            final JLabel bootmaplbl2 = new JLabel("  Address: ");
            bootmaplbl2.setFont(sans);
            bootmapRow.add(bootmaplbl2);
            bootmapRow.add(this.bootmapAddr[i]);

            final JLabel bootmaplbl3 = new JLabel("  ");
            bootmaplbl3.setFont(sans);
            bootmapRow.add(bootmaplbl3);

            bootmapRow.add(this.bootmapRam[i]);
            final JLabel bootmaplbl4 = new JLabel("RAM  ");
            bootmaplbl4.setFont(sans);
            bootmapRow.add(bootmaplbl4);

            bootmapRow.add(this.bootmapRO[i]);
            final JLabel bootmaplbl5 = new JLabel("Readonly  ");
            bootmaplbl5.setFont(sans);
            bootmapRow.add(bootmaplbl5);

            bootmapRow.add(this.bootmapNoExec[i]);
            final JLabel bootmaplbl6 = new JLabel("No-exec  ");
            bootmaplbl6.setFont(sans);
            bootmapRow.add(bootmaplbl6);

            bootmapBankFields.add(bootmapRow);
        }
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

        final MemoryContext memc = theCalc.getCPU().getMemoryContext();
        final Memory ram = memc.getRam();
        final Memory flash = memc.getFlash();
        final BankState[] normal = memc.getNormalBanks();
        final BankState[] bootmap = memc.getBootmapBanks();
        final BankState[] active = memc.getBanks();

        // Row 1

        this.bootmapped.setSelected(memc.isBootmapped());
        this.useNormalBanks.setSelected(active == normal);
        this.useBootBanks.setSelected(active == bootmap);
        this.changed0.setSelected(memc.isChangedPage0());
        this.flashLocked.setSelected(memc.isFlashLocked());

        // Row 2

        this.ramVersion.setText(Integer.toString(ram.getVersion()));
        this.ramPages.setText(Integer.toString(ram.getPages()));
        this.ramSize.setText("0x" + Integer.toHexString(ram.getSize()));
        this.ramLower.setText("0x" + Integer.toHexString(ram.getLower()));
        this.ramUpper.setText("0x" + Integer.toHexString(ram.getUpper()));

        this.flashVersion.setText(Integer.toString(flash.getVersion()));
        this.flashPages.setText(Integer.toString(flash.getPages()));
        this.flashSize.setText("0x" + Integer.toHexString(flash.getSize()));
        this.flashLower.setText("0x" + Integer.toHexString(flash.getLower()));
        this.flashUpper.setText("0x" + Integer.toHexString(flash.getUpper()));

        for (int i = 0; i < 4; ++i) {
            this.normalPage[i].setText(Integer.toHexString(normal[i].getPage()));
            this.normalAddr[i].setText(Integer.toHexString(normal[i].getAddr()));
            this.normalRam[i].setSelected(normal[i].isRam());
            this.normalRO[i].setSelected(normal[i].isReadOnly());
            this.normalNoExec[i].setSelected(normal[i].isNoExec());
        }

        for (int i = 0; i < 4; ++i) {
            this.bootmapPage[i].setText(Integer.toHexString(bootmap[i].getPage()));
            this.bootmapAddr[i].setText(Integer.toHexString(bootmap[i].getAddr()));
            this.bootmapRam[i].setSelected(bootmap[i].isRam());
            this.bootmapRO[i].setSelected(bootmap[i].isReadOnly());
            this.bootmapNoExec[i].setSelected(bootmap[i].isNoExec());
        }

        this.flashStep.setText(memc.getStep().name());
        this.flashWriteDelay.setText(Long.toString(memc.getFlashWriteDelay()));
        this.flashWriteByte.setText(Debugger.toHex2(memc.getFlashWriteByte()));
        this.flashError.setText(memc.isFlashError() ? "Yes" : "No");
        this.flashToggles.setText(Debugger.toHex2(memc.getFlashToggles()));

        this.protMode.setText(Integer.toString(memc.getProtMode()));
        this.protPageSet.setText(Integer.toString(memc.getProtectedPageSet()));

        this.protPages0.setText(Integer.toString(memc.getProtectedPage(0)));
        this.protPages1.setText(Integer.toString(memc.getProtectedPage(1)));
        this.protPages2.setText(Integer.toString(memc.getProtectedPage(2)));
        this.protPages3.setText(Integer.toString(memc.getProtectedPage(3)));

        this.flashReadOp.setText(Integer.toString(memc.getReadOPFlashTStates()));
        this.flashReadNop.setText(Integer.toString(memc.getReadNOPFlashTStates()));
        this.flashWrite.setText(Integer.toString(memc.getWriteFlashTStates()));
        this.ramReadOp.setText(Integer.toString(memc.getReadOPRamTStates()));
        this.ramReadNop.setText(Integer.toString(memc.getReadNOPRamTStates()));
        this.ramWrite.setText(Integer.toString(memc.getWriteRamTStates()));

        this.port06.setText(Debugger.toHex2(memc.getPort06()));
        this.port07.setText(Debugger.toHex2(memc.getPort07()));
        this.port0E.setText(Debugger.toHex2(memc.getPort0E()));
        this.port0F.setText(Debugger.toHex2(memc.getPort0F()));
        this.port24.setText(Debugger.toHex2(memc.getPort24()));
        this.port27.setText(Integer.toString(memc.getPort27RemapCount()[0]));
        this.port28.setText(Integer.toString(memc.getPort28RemapCount()[0]));
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.ramVersion.setEnabled(enable);
        this.ramPages.setEnabled(enable);
        this.ramSize.setEnabled(enable);
        this.ramLower.setEnabled(enable);
        this.ramUpper.setEnabled(enable);

        this.flashVersion.setEnabled(enable);
        this.flashPages.setEnabled(enable);
        this.flashSize.setEnabled(enable);
        this.flashLower.setEnabled(enable);
        this.flashUpper.setEnabled(enable);

        this.bootmapped.setEnabled(enable);
        this.useNormalBanks.setEnabled(enable);
        this.useBootBanks.setEnabled(enable);

        for (int i = 0; i < 4; ++i) {
            this.normalPage[i].setEnabled(enable);
            this.normalAddr[i].setEnabled(enable);
            this.normalRam[i].setEnabled(enable);
            this.normalRO[i].setEnabled(enable);
            this.normalNoExec[i].setEnabled(enable);
        }

        for (int i = 0; i < 4; ++i) {
            this.bootmapPage[i].setEnabled(enable);
            this.bootmapAddr[i].setEnabled(enable);
            this.bootmapRam[i].setEnabled(enable);
            this.bootmapRO[i].setEnabled(enable);
            this.bootmapNoExec[i].setEnabled(enable);
        }

        this.changed0.setEnabled(enable);
        this.flashLocked.setEnabled(enable);
        this.flashStep.setEnabled(enable);
        this.flashWriteDelay.setEnabled(enable);
        this.flashWriteByte.setEnabled(enable);
        this.flashError.setEnabled(enable);
        this.flashToggles.setEnabled(enable);

        this.protMode.setEnabled(enable);
        this.protPageSet.setEnabled(enable);

        this.protPages0.setEnabled(enable);
        this.protPages1.setEnabled(enable);
        this.protPages2.setEnabled(enable);
        this.protPages3.setEnabled(enable);

        this.flashReadOp.setEnabled(enable);
        this.flashReadNop.setEnabled(enable);
        this.flashWrite.setEnabled(enable);
        this.ramReadOp.setEnabled(enable);
        this.ramReadNop.setEnabled(enable);
        this.ramWrite.setEnabled(enable);

        this.port06.setEnabled(enable);
        this.port07.setEnabled(enable);
        this.port0E.setEnabled(enable);
        this.port0F.setEnabled(enable);
        this.port24.setEnabled(enable);
        this.port27.setEnabled(enable);
        this.port28.setEnabled(enable);
    }
}
