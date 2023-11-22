package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.CPU;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;

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
 * A panel to show or update CPU register values.
 */
final class DebugRegistersPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -580819620155984697L;

    /** Register name color. */
    private static final Color NAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Register value color. */
    private static final Color VAL_COLOR = Color.BLACK;

    /** The text fields. */
    private final JTextField[] fields;

    /**
     * Constructs a new {@code DebugRegistersPane}.
     */
    DebugRegistersPane() {

        super("Registers", new JPanel(new GridLayout(6, 1)));

        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);
        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final String[] titles = {"af", "af'", "bc", "bc'", "de", "de'", "hl", "hl'", "ix", "sp", "iy", "pc"};
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

            this.fields[i] = new JTextField(7);
            this.fields[i].setForeground(VAL_COLOR);
            this.fields[i].setBackground(getBackground());
            this.fields[i].setBorder(null);
            this.fields[i].setFont(mono);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        for (int i = 0; i < numTitles; i += 2) {
            final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flow.setBackground(center.getBackground());
            flow.add(lbls[i]);
            flow.add(this.fields[i]);
            flow.add(lbls[i + 1]);
            flow.add(this.fields[i + 1]);
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

        this.fields[0].setText(Debugger.toHex4(cpu.getAF()));
        this.fields[1].setText(Debugger.toHex4(cpu.getAFprime()));
        this.fields[2].setText(Debugger.toHex4(cpu.getBC()));
        this.fields[3].setText(Debugger.toHex4(cpu.getBCprime()));
        this.fields[4].setText(Debugger.toHex4(cpu.getDE()));
        this.fields[5].setText(Debugger.toHex4(cpu.getDEprime()));
        this.fields[6].setText(Debugger.toHex4(cpu.getHL()));
        this.fields[7].setText(Debugger.toHex4(cpu.getHLprime()));
        this.fields[8].setText(Debugger.toHex4(cpu.getIX()));
        this.fields[9].setText(Debugger.toHex4(cpu.getSP()));
        this.fields[10].setText(Debugger.toHex4(cpu.getIY()));
        this.fields[11].setText(Debugger.toHex4(cpu.getPC()));
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        for (final JTextField field : this.fields) {
            field.setEnabled(enable);
        }
    }
}
