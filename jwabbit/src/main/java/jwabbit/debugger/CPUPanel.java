package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.Serial;

/**
 * A panel to display a summary of all CPUs in the system.
 */
final class CPUPanel extends LoggedPanel implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1907340591871810158L;

    /** The detail panel. */
    private final CPUDetailPanel cpuDetail;

    /**
     * Constructs a new {@code CPUPanel}.
     */
    CPUPanel() {

        super(new BorderLayout(4, 4));

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        setBackground(Debugger.BG_COLOR);

        final JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(100, 0, 0)));
        top.setBackground(Debugger.BG_COLOR);
        final JLabel title = new JLabel("CPU Detail");
        title.setFont(Fonts.getSans().deriveFont(Font.BOLD, title.getFont().getSize2D() + 1.0f));
        title.setForeground(new Color(100, 0, 0));
        top.add(title);
        add(top, BorderLayout.PAGE_START);

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Debugger.BG_COLOR);
        add(center, BorderLayout.CENTER);

        this.cpuDetail = new CPUDetailPanel();
        center.add(this.cpuDetail, BorderLayout.CENTER);
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
            this.cpuDetail.calcState(theCalc);
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

        this.cpuDetail.enableControls(enable);
    }
}
