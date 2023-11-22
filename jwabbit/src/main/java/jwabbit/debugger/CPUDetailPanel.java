package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.CPU;
import jwabbit.core.JWCoreConstants;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel that displays details of a CPU.
 */
final class CPUDetailPanel extends LoggedPanel implements ICalcStateListener {

    /** Background color. */
    private static final Color FIELD_COLOR = new Color(0, 120, 170);

    /** Background color. */
    private static final Color LBL_COLOR = new Color(80, 150, 80);

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7335137861825866386L;

    /** The CPU version. */
    private final JTextField version;

    /** The CPU model bits. */
    private final JTextField modelBits;

    /** The halt checkbox. */
    private final JCheckBox halt;

    /** The read checkbox. */
    private final JCheckBox read;

    /** the write checkbox. */
    private final JCheckBox write;

    /** The input checkbox. */
    private final JCheckBox input;

    /** The output checkbox. */
    private final JCheckBox output;

    /** The AF register. */
    private final JTextField afReg;

    /** The BC register. */
    private final JTextField bcReg;

    /** The DE register. */
    private final JTextField deReg;

    /** The HL register. */
    private final JTextField hlReg;

    /** The AF' register. */
    private final JTextField afpReg;

    /** The BC' register. */
    private final JTextField bcpReg;

    /** The DE' register. */
    private final JTextField depReg;

    /** The HL' register. */
    private final JTextField hlpReg;

    /** The IX register. */
    private final JTextField ixReg;

    /** The IY register. */
    private final JTextField iyReg;

    /** The PC register. */
    private final JTextField pcReg;

    /** The SP register. */
    private final JTextField spReg;

    /** The Z flag. */
    private final JCheckBox zFlag;

    /** The C flag. */
    private final JCheckBox cFlag;

    /** The S flag. */
    private final JCheckBox sFlag;

    /** The P/V flag. */
    private final JCheckBox pvFlag;

    /** The HC flag. */
    private final JCheckBox hcFlag;

    /** The N flag. */
    private final JCheckBox nFlag;

    /** The bus. */
    private final JTextField bus;

    /** The prefix. */
    private final JTextField prefix;

    /** The has hit enter flag. */
    private final JCheckBox hasHitEnter;

    /** The I register. */
    private final JTextField iReg;

    /** The R register. */
    private final JTextField rReg;

    /** The interrupt mode. */
    private final JTextField iMode;

    /** The interrupt flag. */
    private final JCheckBox interrupt;

    /** The EI block flag. */
    private final JCheckBox eiBlock;

    /** The IFF1 flag. */
    private final JCheckBox iff1;

    /** The IFF2 flag. */
    private final JCheckBox iff2;

    /** The last byte written to link. */
    private final JTextField linkWrite;

    /** The link instruction flag. */
    private final JCheckBox linkInstr;

    /**
     * Constructs a new {@code CPUDetailPanel}.
     */
    CPUDetailPanel() {

        super(new BorderLayout(10, 10));

        setBackground(Debugger.BG_COLOR);

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font bold = Fonts.getSans().deriveFont(Font.BOLD, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        // North is basic CPU data
        final JPanel north = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        north.setBackground(Debugger.BG_COLOR);
        add(north, BorderLayout.PAGE_START);

        final JLabel verLabel = new JLabel("CPU Version: ");
        verLabel.setFont(sans);
        north.add(verLabel);
        this.version = new JTextField(4);
        this.version.setFont(mono);
        north.add(this.version);

        final JLabel modBitsLabel = new JLabel("   Model bits: ");
        modBitsLabel.setFont(sans);
        north.add(modBitsLabel);
        this.modelBits = new JTextField(4);
        this.modelBits.setFont(mono);
        north.add(this.modelBits);

        final JLabel spacer = new JLabel("   ");
        spacer.setFont(sans);
        north.add(spacer);

        this.halt = new JCheckBox("Halt   ");
        this.halt.setBackground(Debugger.BG_COLOR);
        this.halt.setFont(sans);
        north.add(this.halt);

        this.read = new JCheckBox("Read   ");
        this.read.setBackground(Debugger.BG_COLOR);
        this.read.setFont(sans);
        north.add(this.read);

        this.write = new JCheckBox("Write   ");
        this.write.setBackground(Debugger.BG_COLOR);
        this.write.setFont(sans);
        north.add(this.write);

        this.input = new JCheckBox("Input   ");
        this.input.setBackground(Debugger.BG_COLOR);
        this.input.setFont(sans);
        north.add(this.input);

        this.output = new JCheckBox("Output   ");
        this.output.setBackground(Debugger.BG_COLOR);
        this.output.setFont(sans);
        north.add(this.output);

        // West edge is registers and flags
        final JPanel regFlagCol = new JPanel(new BorderLayout(0, 8));
        regFlagCol.setBackground(Debugger.BG_COLOR);
        add(regFlagCol, BorderLayout.LINE_START);

        // At top is registers
        final JPanel regPaneBox = new JPanel(new BorderLayout());
        regPaneBox.setBackground(Debugger.BG_COLOR);
        final JLabel regPaneTit = new JLabel("Registers:");
        regPaneTit.setFont(bold);
        regPaneBox.add(regPaneTit, BorderLayout.PAGE_START);
        regFlagCol.add(regPaneBox, BorderLayout.PAGE_START);

        final JPanel regPane = new JPanel(new GridLayout(6, 1, 2, 2));
        regPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        regPane.setBackground(Color.WHITE);
        regPaneBox.add(regPane, BorderLayout.CENTER);

        this.afReg = mkField(mono, true);
        this.bcReg = mkField(mono, true);
        this.deReg = mkField(mono, true);
        this.hlReg = mkField(mono, true);
        this.afpReg = mkField(mono, true);
        this.bcpReg = mkField(mono, true);
        this.depReg = mkField(mono, true);
        this.hlpReg = mkField(mono, true);
        this.ixReg = mkField(mono, true);
        this.iyReg = mkField(mono, true);
        this.pcReg = mkField(mono, true);
        this.spReg = mkField(mono, true);

        final JLabel[] labels = {mkLabel(sans, "af: "),
                mkLabel(sans, "af': "), mkLabel(sans, "bc: "),
                mkLabel(sans, "bc': "), mkLabel(sans, "de: "),
                mkLabel(sans, "de': "), mkLabel(sans, "hl: "),
                mkLabel(sans, "hl': "), mkLabel(sans, "ix: "),
                mkLabel(sans, "sp: "), mkLabel(sans, "iy: "),
                mkLabel(sans, "pc: "),};
        int maxWidth = 0;
        int maxHeight = 0;
        for (final JLabel label : labels) {
            maxWidth = Math.max(maxWidth, label.getPreferredSize().width);
            maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
        }
        final Dimension pref = new Dimension(maxWidth, maxHeight);
        for (final JLabel label : labels) {
            label.setPreferredSize(pref);
        }

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row1.setBackground(Color.WHITE);
        regPane.add(row1);
        row1.add(labels[0]);
        row1.add(this.afReg);
        row1.add(new JLabel("   "));
        row1.add(labels[1]);
        row1.add(this.afpReg);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row2.setBackground(Color.WHITE);
        regPane.add(row2);
        row2.add(labels[2]);
        row2.add(this.bcReg);
        row2.add(new JLabel("   "));
        row2.add(labels[3]);
        row2.add(this.bcpReg);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row3.setBackground(Color.WHITE);
        regPane.add(row3);
        row3.add(labels[4]);
        row3.add(this.deReg);
        row3.add(new JLabel("   "));
        row3.add(labels[5]);
        row3.add(this.depReg);

        final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row4.setBackground(Color.WHITE);
        regPane.add(row4);
        row4.add(labels[6]);
        row4.add(this.hlReg);
        row4.add(new JLabel("   "));
        row4.add(labels[7]);
        row4.add(this.hlpReg);

        final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row5.setBackground(Color.WHITE);
        regPane.add(row5);
        row5.add(labels[8]);
        row5.add(this.ixReg);
        row5.add(new JLabel("   "));
        row5.add(labels[9]);
        row5.add(this.spReg);

        final JPanel row6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        row6.setBackground(Color.WHITE);
        regPane.add(row6);
        row6.add(labels[10]);
        row6.add(this.iyReg);
        row6.add(new JLabel("   "));
        row6.add(labels[11]);
        row6.add(this.pcReg);

        final JPanel west2 = new JPanel(new BorderLayout(0, 8));
        west2.setBackground(Debugger.BG_COLOR);
        regFlagCol.add(west2, BorderLayout.CENTER);

        // Below registers are flags
        final JPanel flagsPaneBox = new JPanel(new BorderLayout());
        flagsPaneBox.setBackground(Debugger.BG_COLOR);
        final JLabel flagsPaneTit = new JLabel("Flags:");
        flagsPaneTit.setFont(bold);
        flagsPaneBox.add(flagsPaneTit, BorderLayout.PAGE_START);
        west2.add(flagsPaneBox, BorderLayout.PAGE_START);

        final JPanel flagsPane = new JPanel(new GridLayout(2, 7, 2, 2));
        flagsPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        flagsPane.setBackground(Color.WHITE);
        flagsPaneBox.add(flagsPane, BorderLayout.CENTER);

        this.zFlag = new JCheckBox();
        this.zFlag.setBackground(Color.WHITE);
        this.cFlag = new JCheckBox();
        this.cFlag.setBackground(Color.WHITE);
        this.sFlag = new JCheckBox();
        this.sFlag.setBackground(Color.WHITE);
        this.pvFlag = new JCheckBox();
        this.pvFlag.setBackground(Color.WHITE);
        this.hcFlag = new JCheckBox();
        this.hcFlag.setBackground(Color.WHITE);
        this.nFlag = new JCheckBox();
        this.nFlag.setBackground(Color.WHITE);

        flagsPane.add(this.zFlag);
        flagsPane.add(mkLabel(sans, "z"));
        flagsPane.add(this.cFlag);
        flagsPane.add(mkLabel(sans, "c"));
        flagsPane.add(this.sFlag);
        flagsPane.add(mkLabel(sans, "s"));
        flagsPane.add(this.pvFlag);
        flagsPane.add(mkLabel(sans, "p/v"));
        flagsPane.add(this.hcFlag);
        flagsPane.add(mkLabel(sans, "hc"));
        flagsPane.add(this.nFlag);
        flagsPane.add(mkLabel(sans, "n"));

        final JPanel west3 = new JPanel(new BorderLayout(0, 8));
        west3.setBackground(Debugger.BG_COLOR);
        west2.add(west3, BorderLayout.CENTER);

        // Below flags is CPU status
        final JPanel statPaneBox = new JPanel(new BorderLayout());
        statPaneBox.setBackground(Debugger.BG_COLOR);
        final JLabel statPaneTit = new JLabel("Status:");
        statPaneTit.setFont(bold);
        statPaneBox.add(statPaneTit, BorderLayout.PAGE_START);
        west3.add(statPaneBox, BorderLayout.PAGE_START);

        final JPanel statPane = new JPanel(new GridLayout(2, 1, 2, 2));
        statPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        statPane.setBackground(Color.WHITE);
        statPaneBox.add(statPane, BorderLayout.CENTER);

        this.bus = mkField(mono, false);
        this.prefix = mkField(mono, false);
        this.hasHitEnter = new JCheckBox();
        this.hasHitEnter.setBackground(Color.WHITE);

        final JPanel row7 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row7.setBackground(Color.WHITE);
        statPane.add(row7);
        row7.add(mkLabel(sans, "Bus: "));
        row7.add(this.bus);
        row7.add(mkLabel(sans, "   Prefix: "));
        row7.add(this.prefix);

        final JPanel row8 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row8.setBackground(Color.WHITE);
        statPane.add(row8);
        row8.add(this.hasHitEnter);
        row8.add(mkLabel(sans, "Has hit enter"));

        final JPanel west4 = new JPanel(new BorderLayout(0, 8));
        west4.setBackground(Debugger.BG_COLOR);
        west3.add(west4, BorderLayout.CENTER);

        // Below status is interrupt state
        final JPanel intPaneBox = new JPanel(new BorderLayout());
        intPaneBox.setBackground(Debugger.BG_COLOR);
        final JLabel intPaneTit = new JLabel("Interrupts:");
        intPaneTit.setFont(bold);
        intPaneBox.add(intPaneTit, BorderLayout.PAGE_START);
        west4.add(intPaneBox, BorderLayout.PAGE_START);

        final JPanel intPane = new JPanel(new GridLayout(3, 1, 2, 2));
        intPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        intPane.setBackground(Color.WHITE);
        intPaneBox.add(intPane, BorderLayout.CENTER);

        this.iReg = mkField(mono, false);
        this.rReg = mkField(mono, false);
        this.iMode = mkField(mono, false);
        this.interrupt = new JCheckBox();
        this.interrupt.setBackground(Color.WHITE);
        this.eiBlock = new JCheckBox();
        this.eiBlock.setBackground(Color.WHITE);
        this.iff1 = new JCheckBox();
        this.iff1.setBackground(Color.WHITE);
        this.iff2 = new JCheckBox();
        this.iff2.setBackground(Color.WHITE);

        final JPanel row9 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row9.setBackground(Color.WHITE);
        intPane.add(row9);
        row9.add(mkLabel(sans, "IM: "));
        row9.add(this.iMode);
        row9.add(mkLabel(sans, "   i: "));
        row9.add(this.iReg);
        row9.add(mkLabel(sans, "   r: "));
        row9.add(this.rReg);

        final JPanel row10 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row10.setBackground(Color.WHITE);
        intPane.add(row10);
        row10.add(this.interrupt);
        row10.add(mkLabel(sans, "Interrupt   "));
        row10.add(this.eiBlock);
        row10.add(mkLabel(sans, "EI Block"));

        final JPanel row11 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row11.setBackground(Color.WHITE);
        intPane.add(row11);
        row11.add(this.iff1);
        row11.add(mkLabel(sans, "iff1   "));
        row11.add(this.iff2);
        row11.add(mkLabel(sans, "iff2"));

        final JPanel west5 = new JPanel(new BorderLayout(0, 8));
        west5.setBackground(Debugger.BG_COLOR);
        west4.add(west5, BorderLayout.CENTER);

        // Below status is interrupt state
        final JPanel linkPaneBox = new JPanel(new BorderLayout());
        linkPaneBox.setBackground(Debugger.BG_COLOR);
        final JLabel linkPaneTit = new JLabel("Link:");
        linkPaneTit.setFont(bold);
        linkPaneBox.add(linkPaneTit, BorderLayout.PAGE_START);
        west5.add(linkPaneBox, BorderLayout.PAGE_START);

        final JPanel linkPane = new JPanel(new GridLayout(2, 1, 2, 2));
        linkPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        linkPane.setBackground(Color.WHITE);
        linkPaneBox.add(linkPane, BorderLayout.CENTER);

        this.linkWrite = mkField(mono, false);
        this.linkInstr = new JCheckBox();
        this.linkInstr.setBackground(Color.WHITE);
        final JTextField linkTime = new JTextField(6);
        linkTime.setFont(sans);
        linkTime.setForeground(FIELD_COLOR);
        linkTime.setBackground(Color.WHITE);
        linkTime.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        final JPanel row12 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row12.setBackground(Color.WHITE);
        linkPane.add(row12);
        row12.add(this.linkInstr);
        row12.add(mkLabel(sans, "Link instr.   Write: "));
        row12.add(this.linkWrite);

        final JPanel row13 = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        row13.setBackground(Color.WHITE);
        linkPane.add(row13);
        row13.add(mkLabel(sans, "Link time: "));
        row13.add(linkTime);
    }

    /**
     * Builds a small text field.
     *
     * @param font the font
     * @param wide true for a wider (4-digit) field, false for a 2-digit field
     * @return the field
     */
    private static JTextField mkField(final Font font, final boolean wide) {

        final JTextField field = new JTextField(wide ? 5 : 3);
        field.setFont(font);
        field.setForeground(FIELD_COLOR);
        field.setBackground(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return field;
    }

    /**
     * Builds a text label.
     *
     * @param font the font
     * @param str  the label string
     * @return the label
     */
    private static JLabel mkLabel(final Font font, final String str) {

        final JLabel lbl = new JLabel(str);
        lbl.setFont(font);
        lbl.setForeground(LBL_COLOR);

        return lbl;
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

        final CPU cpu = theCalc.getCPU();

        this.version.setText(Integer.toString(cpu.getVersion()));
        this.modelBits.setText(Debugger.toHex2(cpu.getModelBits()));
        this.halt.setSelected(cpu.isHalt());
        this.read.setSelected(cpu.isRead());
        this.write.setSelected(cpu.isWrite());
        this.input.setSelected(cpu.isInput());
        this.output.setSelected(cpu.isOutput());

        this.afReg.setText(Debugger.toHex4(cpu.getAF()));
        this.bcReg.setText(Debugger.toHex4(cpu.getBC()));
        this.deReg.setText(Debugger.toHex4(cpu.getDE()));
        this.hlReg.setText(Debugger.toHex4(cpu.getHL()));
        this.afpReg.setText(Debugger.toHex4(cpu.getAFprime()));
        this.bcpReg.setText(Debugger.toHex4(cpu.getBCprime()));
        this.depReg.setText(Debugger.toHex4(cpu.getDEprime()));
        this.hlpReg.setText(Debugger.toHex4(cpu.getHLprime()));
        this.ixReg.setText(Debugger.toHex4(cpu.getIX()));
        this.iyReg.setText(Debugger.toHex4(cpu.getIY()));
        this.spReg.setText(Debugger.toHex4(cpu.getSP()));
        this.pcReg.setText(Debugger.toHex4(cpu.getPC()));

        final int flags = cpu.getF();
        this.zFlag.setSelected((flags & JWCoreConstants.ZERO_MASK) != 0);
        this.cFlag.setSelected((flags & JWCoreConstants.CARRY_MASK) != 0);
        this.sFlag.setSelected((flags & JWCoreConstants.SIGN_MASK) != 0);
        this.pvFlag.setSelected((flags & JWCoreConstants.PV_MASK) != 0);
        this.hcFlag.setSelected((flags & JWCoreConstants.HC_MASK) != 0);
        this.nFlag.setSelected((flags & JWCoreConstants.N_MASK) != 0);

        this.bus.setText(Debugger.toHex2(cpu.getBus()));
        this.prefix.setText(Debugger.toHex2(cpu.getPrefix()));

        this.hasHitEnter.setSelected(cpu.getHasHitEnter() != 0L);
        this.interrupt.setSelected(cpu.isInterrupt());
        this.eiBlock.setSelected(cpu.isEiBlock());
        this.iff1.setSelected(cpu.isIff1());
        this.iff2.setSelected(cpu.isIff2());
        this.linkInstr.setSelected(cpu.isLinkInstruction());

        this.iMode.setText(Integer.toString(cpu.getIMode()));
        this.iReg.setText(Debugger.toHex2(cpu.getI()));
        this.rReg.setText(Debugger.toHex2(cpu.getR()));
        this.linkWrite.setText(Debugger.toHex2(cpu.getLinkWrite()));
        this.linkWrite.setText(Long.toString(cpu.getLinkingTime()));
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.version.setEnabled(enable);
        this.modelBits.setEnabled(enable);
        this.halt.setEnabled(enable);
        this.read.setEnabled(enable);
        this.write.setEnabled(enable);
        this.input.setEnabled(enable);
        this.output.setEnabled(enable);

        this.afReg.setEnabled(enable);
        this.bcReg.setEnabled(enable);
        this.deReg.setEnabled(enable);
        this.hlReg.setEnabled(enable);
        this.afpReg.setEnabled(enable);
        this.bcpReg.setEnabled(enable);
        this.depReg.setEnabled(enable);
        this.hlpReg.setEnabled(enable);
        this.ixReg.setEnabled(enable);
        this.iyReg.setEnabled(enable);
        this.spReg.setEnabled(enable);
        this.pcReg.setEnabled(enable);

        this.zFlag.setEnabled(enable);
        this.cFlag.setEnabled(enable);
        this.sFlag.setEnabled(enable);
        this.pvFlag.setEnabled(enable);
        this.hcFlag.setEnabled(enable);
        this.nFlag.setEnabled(enable);

        this.bus.setEnabled(enable);
        this.prefix.setEnabled(enable);

        this.hasHitEnter.setEnabled(enable);
        this.interrupt.setEnabled(enable);
        this.eiBlock.setEnabled(enable);
        this.iff1.setEnabled(enable);
        this.iff2.setEnabled(enable);
        this.linkInstr.setEnabled(enable);

        this.iMode.setEnabled(enable);
        this.iReg.setEnabled(enable);
        this.rReg.setEnabled(enable);
        this.linkWrite.setEnabled(enable);
        this.linkWrite.setEnabled(enable);
    }
}
