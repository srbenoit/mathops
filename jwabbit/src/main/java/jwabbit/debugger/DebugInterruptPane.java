package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.CPU;
import jwabbit.gui.fonts.Fonts;
import jwabbit.hardware.STDINT;
import jwabbit.iface.Calc;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to show or update CPU interrupt values.
 */
final class DebugInterruptPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9096432059657366953L;

    /** Name color. */
    private static final Color NAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Value color. */
    private static final Color VAL_COLOR = Color.BLACK;

    /** The iff1 checkbox. */
    private final JCheckBox iff1;

    /** The iff2 checkbox. */
    private final JCheckBox iff2;

    /** The text fields for the registers. */
    private final JTextField[] fields;

    /** The text fields for the timers. */
    private final JTextField[] fields2;

    /** The most recent last check 1. */
    private double lastLastChk1;

    /** The most recent last check 2. */
    private double lastLastChk2;

    /**
     * Constructs a new {@code DebugInterruptPane}.
     */
    DebugInterruptPane() {

        super("Interrupts", new JPanel(new GridLayout(7, 1)));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow1.setBackground(center.getBackground());
        center.add(flow1);

        this.iff1 = new JCheckBox("iff1");
        this.iff1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        this.iff1.setForeground(VAL_COLOR);
        this.iff1.setBackground(getBackground());
        this.iff1.setFont(sans);
        flow1.add(this.iff1);

        this.iff2 = new JCheckBox("iff2");
        this.iff2.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 0));
        this.iff2.setForeground(VAL_COLOR);
        this.iff2.setBackground(getBackground());
        this.iff2.setFont(sans);
        flow1.add(this.iff2);

        final String[] titles = {"IM", "i", "Mask", "r"};
        final int numTitles = titles.length;

        final JLabel[] lbls = new JLabel[numTitles];
        this.fields = new JTextField[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setForeground(NAME_COLOR);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);

            this.fields[i] = new JTextField(4);
            this.fields[i].setForeground(VAL_COLOR);
            this.fields[i].setBackground(getBackground());
            this.fields[i].setBorder(null);
            this.fields[i].setFont(mono);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow2.setBackground(center.getBackground());
        flow2.add(lbls[0]);
        flow2.add(this.fields[0]);
        flow2.add(lbls[1]);
        flow2.add(this.fields[1]);
        center.add(flow2);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow3.setBackground(center.getBackground());
        flow3.add(lbls[2]);
        flow3.add(this.fields[2]);
        flow3.add(lbls[3]);
        flow3.add(this.fields[3]);
        center.add(flow3);

        final String[] titles2 = {"Next Timer1  ", "Timer1 dur.  ", "Next Timer2  ", "Timer2 dur.  "};
        final int numTitles2 = titles2.length;

        final JLabel[] lbls2 = new JLabel[numTitles2];
        this.fields2 = new JTextField[numTitles2];
        maxWidth = 0;
        maxHeight = 0;
        for (int i = 0; i < numTitles2; ++i) {
            lbls2[i] = new JLabel(" " + titles2[i]);
            lbls2[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls2[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls2[i].getPreferredSize().height);

            this.fields2[i] = new JTextField(9);
            this.fields2[i].setForeground(VAL_COLOR);
            this.fields2[i].setBackground(getBackground());
            this.fields2[i].setBorder(null);
            this.fields2[i].setFont(mono);
        }
        for (int i = 0; i < numTitles2; ++i) {
            lbls2[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        for (int i = 0; i < numTitles2; ++i) {
            final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flow.setBackground(center.getBackground());
            flow.add(lbls2[i]);
            flow.add(this.fields2[i]);

            final JLabel units = new JLabel(" ms");
            units.setFont(sans);
            flow.add(units);

            center.add(flow);
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

        final CPU cpu = theCalc.getCPU();
        final STDINT stdint = cpu.getPIOContext().getStdint();

        this.iff1.setSelected(cpu.isIff1());
        this.iff2.setSelected(cpu.isIff2());
        this.fields[0].setText(Integer.toString(cpu.getIMode()));
        this.fields[1].setText(Debugger.toHex2(cpu.getI()));
        this.fields[2].setText(Debugger.toHex2(stdint.getIntactive()));
        this.fields[3].setText(Integer.toString(cpu.getR()));

        final double lastElapsed = cpu.getTimerContext().getElapsed();

        final String str1 = Double.toString((lastElapsed - this.lastLastChk1) * 1000.0);
        final int dec1 = str1.lastIndexOf('.');
        final int after1 = str1.length() - dec1;
        if (after1 > 5) {
            this.fields2[0].setText(str1.substring(0, dec1 + 5));
        } else {
            this.fields2[0].setText(str1);
        }

        final String str2 = Double.toString((lastElapsed - this.lastLastChk2) * 1000.0);
        final int dec2 = str2.lastIndexOf('.');
        final int after2 = str2.length() - dec2;
        if (after2 > 5) {
            this.fields2[2].setText(str2.substring(0, dec2 + 5));
        } else {
            this.fields2[2].setText(str2);
        }

        this.lastLastChk1 = stdint.getLastchk1();

        final String str3 = Double.toString((lastElapsed - this.lastLastChk1) * 1000.0);
        final int dec3 = str3.lastIndexOf('.');
        final int after3 = str3.length() - dec3;
        if (after3 > 5) {
            this.fields2[0].setText(str3.substring(0, dec3 + 5));
        } else {
            this.fields2[0].setText(str3);
        }

        final double val1 = stdint.getTimermax1();

        final String str4 = Double.toString(val1 * 1000.0);
        final int dec4 = str4.lastIndexOf('.');
        final int after4 = str4.length() - dec4;
        if (after4 > 5) {
            this.fields2[1].setText(str4.substring(0, dec4 + 5));
        } else {
            this.fields2[1].setText(str4);
        }

        this.lastLastChk2 = stdint.getLastchk2();

        final String str5 = Double.toString((lastElapsed - this.lastLastChk2) * 1000.0);
        final int dec5 = str5.lastIndexOf('.');
        final int after5 = str5.length() - dec5;
        if (after5 > 5) {
            this.fields2[2].setText(str5.substring(0, dec5 + 5));
        } else {
            this.fields2[2].setText(str5);
        }

        final double val2 = stdint.getTimermax2();

        final String str = Double.toString(val2 * 1000.0);
        final int dec = str.lastIndexOf('.');
        final int after = str.length() - dec;
        if (after > 5) {
            this.fields2[3].setText(str.substring(0, dec + 5));
        } else {
            this.fields2[3].setText(str);
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

        this.iff1.setEnabled(enable);
        this.iff2.setEnabled(enable);
        for (final JTextField field : this.fields) {
            field.setEnabled(enable);
        }
        for (final JTextField jTextField : this.fields2) {
            jTextField.setEnabled(enable);
        }
    }
}
