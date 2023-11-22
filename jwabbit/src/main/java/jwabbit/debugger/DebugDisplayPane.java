package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.gui.fonts.Fonts;
import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.HardwareConstants;
import jwabbit.iface.Calc;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to show or update display status values.
 */
final class DebugDisplayPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5244395523263210177L;

    /** Name color. */
    private static final Color NAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Value color. */
    private static final Color VAL_COLOR = Color.BLACK;

    /** The powered checkbox. */
    private final JCheckBox powered;

    /** The contrast field. */
    private final JTextField contrast;

    /** The cursor x,y,z fields. */
    private final JTextField[] cursor;

    /** The x-inc radio button field. */
    private final JRadioButton xInc;

    /** The x-dec radio button field. */
    private final JRadioButton xDec;

    /** The y-inc radio button field. */
    private final JRadioButton yInc;

    /** The y-dec radio button field. */
    private final JRadioButton yDec;

    /**
     * Constructs a new {@code DebugDisplayPane}.
     */
    DebugDisplayPane() {

        super("Display", new JPanel(new BorderLayout()));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final JPanel west = new JPanel(new GridLayout(2, 1));
        west.setBackground(getBackground());
        center.add(west, BorderLayout.LINE_START);

        this.powered = new JCheckBox("Powered");
        this.powered.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 0));
        this.powered.setForeground(VAL_COLOR);
        this.powered.setBackground(getBackground());
        this.powered.setFont(sans);
        west.add(this.powered);

        final JPanel westFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        westFlow.setBackground(getBackground());
        west.add(westFlow);

        final JLabel con = new JLabel(" Contrast");
        con.setForeground(NAME_COLOR);
        con.setFont(sans);
        westFlow.add(con);
        this.contrast = new JTextField(4);
        this.contrast.setForeground(VAL_COLOR);
        this.contrast.setBackground(getBackground());
        this.contrast.setBorder(null);
        this.contrast.setFont(mono);
        westFlow.add(this.contrast);

        final JPanel east = new JPanel(new GridLayout(3, 1));
        east.setBackground(getBackground());
        center.add(east, BorderLayout.CENTER);

        final String[] titles = {"  x", "  y", "  z"};
        final int numTitles = titles.length;

        final JLabel[] lbls = new JLabel[numTitles];
        this.cursor = new JTextField[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setForeground(NAME_COLOR);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);

            this.cursor[i] = new JTextField(4);
            this.cursor[i].setForeground(VAL_COLOR);
            this.cursor[i].setBackground(getBackground());
            this.cursor[i].setBorder(null);
            this.cursor[i].setFont(mono);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));
        }

        for (int i = 0; i < numTitles; ++i) {
            final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flow.setBackground(center.getBackground());
            flow.add(lbls[i]);
            flow.add(this.cursor[i]);
            east.add(flow);
        }

        final JPanel south = new JPanel(new GridLayout(2, 2));
        south.setBackground(getBackground());
        south.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 200)), "Cursor Mode",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                sans.deriveFont(Font.BOLD)));
        center.add(south, BorderLayout.PAGE_END);

        this.xInc = new JRadioButton("X-Inc.");
        this.xInc.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.xInc.setBackground(getBackground());
        this.xInc.setFont(sans);
        this.xDec = new JRadioButton("X-Dec.");
        this.xDec.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.xDec.setBackground(getBackground());
        this.xDec.setFont(sans);
        this.yInc = new JRadioButton("Y-Inc.");
        this.yInc.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.yInc.setBackground(getBackground());
        this.yInc.setFont(sans);
        this.yDec = new JRadioButton("Y-Dec.");
        this.yDec.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        this.yDec.setBackground(getBackground());
        this.yDec.setFont(sans);

        final ButtonGroup group = new ButtonGroup();
        group.add(this.xInc);
        group.add(this.xDec);
        group.add(this.yInc);
        group.add(this.yDec);

        south.add(this.xInc);
        south.add(this.yInc);
        south.add(this.xDec);
        south.add(this.yDec);
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

        final AbstractLCDBase lcdBase = theCalc.getCPU().getPIOContext().getLcd();

        this.powered.setSelected(lcdBase.isActive());
        this.contrast.setText(Integer.toString(lcdBase.getContrast()));
        this.cursor[0].setText(Integer.toString(lcdBase.getX()));
        this.cursor[1].setText(Integer.toString(lcdBase.getY()));
        this.cursor[2].setText(Integer.toString(lcdBase.getZ()));

        final int mode = lcdBase.getCursorMode();

        this.xDec.setSelected(mode == HardwareConstants.X_UP);
        this.xInc.setSelected(mode == HardwareConstants.X_DOWN);
        this.yDec.setSelected(mode == HardwareConstants.Y_DOWN);
        this.yInc.setSelected(mode == HardwareConstants.Y_UP);
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.powered.setEnabled(enable);
        this.contrast.setEnabled(enable);
        this.cursor[0].setEnabled(enable);
        this.cursor[1].setEnabled(enable);
        this.cursor[2].setEnabled(enable);

        this.xDec.setEnabled(enable);
        this.xInc.setEnabled(enable);
        this.yDec.setEnabled(enable);
        this.yInc.setEnabled(enable);
    }
}
