package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * The General tab.
 */
final class DisplayTab extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7742332125124926255L;

    /** Possible sources for preview image. */
    private static final String[] SOURCES = {"Bounce", "Scroll", "Gradient", "Live"};

    /** Possible modes. */
    private static final String[] MODES = {"Perfect gray", "Steady freq", "Game gray"};

    /**
     * Constructs a new {@code DisplayTab}.
     */
    DisplayTab() {

        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JPanel north = new JPanel(new BorderLayout(4, 4));
        add(north, BorderLayout.PAGE_START);

        final DisplayPane pane = new DisplayPane();
        pane.setBorder(BorderFactory.createLoweredBevelBorder());
        north.add(pane, BorderLayout.LINE_START);

        final JPanel settings = new JPanel(new BorderLayout(4, 4));
        settings.setBorder(BorderFactory.createTitledBorder("Preview settings"));
        north.add(settings, BorderLayout.LINE_END);

        final JPanel sourcePnl = new JPanel(new BorderLayout());
        sourcePnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        sourcePnl.add(new JLabel("Source"), BorderLayout.PAGE_START);

        final JComboBox<String> source = new JComboBox<>(SOURCES);
        source.setSelectedIndex(0);
        source.setPreferredSize(new Dimension(source.getPreferredSize().width * 3 / 2,
                source.getPreferredSize().height));
        sourcePnl.add(source, BorderLayout.PAGE_END);
        settings.add(sourcePnl, BorderLayout.PAGE_START);

        final JPanel fpsPnl = new JPanel(new BorderLayout());
        fpsPnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        fpsPnl.add(new JLabel("FPS"), BorderLayout.PAGE_START);

        final JPanel fpsFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        final JSlider fps = new JSlider(15, 60);
        fps.setSnapToTicks(true);
        fps.setMajorTickSpacing(45);
        fps.setPaintLabels(true);
        fps.setPaintTicks(true);
        fps.setPreferredSize(new Dimension(source.getPreferredSize().width, fps.getPreferredSize().height));
        fpsFlow.add(fps);

        fpsPnl.add(fpsFlow, BorderLayout.PAGE_END);
        settings.add(fpsPnl, BorderLayout.CENTER);

        final JPanel center = new JPanel(new BorderLayout(4, 4));
        add(center, BorderLayout.CENTER);

        final JPanel centerW = new JPanel(new BorderLayout());
        center.add(centerW, BorderLayout.LINE_START);

        final JPanel centerWN = new JPanel(new GridLayout(2, 1));
        centerWN.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        centerW.add(centerWN, BorderLayout.PAGE_START);
        centerWN.add(new JLabel("Mode:"));
        final JComboBox<String> mode = new JComboBox<>(MODES);
        mode.setSelectedIndex(0);
        centerWN.add(mode);

        final JPanel centerE = new JPanel(new BorderLayout());
        center.add(centerE, BorderLayout.LINE_END);

        final JPanel centerEN = new JPanel(new GridLayout(2, 1));
        centerE.add(centerEN, BorderLayout.PAGE_START);

        final JSlider shades = new JSlider(2, 12);
        shades.setMajorTickSpacing(10);
        shades.setMinorTickSpacing(1);
        shades.setPaintTicks(true);
        shades.setSnapToTicks(true);
        shades.setPaintLabels(true);
        shades.setPreferredSize(new Dimension((source.getPreferredSize().width << 2) / 3,
                shades.getPreferredSize().height));

        final JSlider freq = new JSlider(30, 120);
        freq.setPaintTicks(true);
        freq.setMajorTickSpacing(90);
        freq.setPaintLabels(true);
        freq.setPreferredSize(new Dimension((source.getPreferredSize().width << 2) / 3,
                shades.getPreferredSize().height));

        final JLabel shadesLbl = new JLabel("Shades:");
        final JLabel freqLbl = new JLabel("Freq:");
        freqLbl.setPreferredSize(shadesLbl.getPreferredSize());

        final JPanel shadesFlow = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        final JPanel freqFlow = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 0));
        shadesFlow.add(shadesLbl);
        shadesFlow.add(shades);
        freqFlow.add(freqLbl);
        freqFlow.add(freq);

        centerEN.add(shadesFlow);
        centerEN.add(freqFlow);

    }
}
