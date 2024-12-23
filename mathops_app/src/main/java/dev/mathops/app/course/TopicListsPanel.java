package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

final class TopicListsPanel extends JPanel implements ActionListener {

    /** The action command for the refresh button. */
    private static final String REFRESH_CMD = "REFRESH";

    /** The course directory. */
    private final File courseDir;

    /** The list of subjects. */
    private final JList<String> subjectList;

    /** The list of topic modules. */
    private final JList<String> moduleList;

    /** The refresh button. */
    private final JButton refresh;

    /**
     * Constructs a new {@code TopicListsPanel}.
     *
     * @param theCourseDir the course directory
     * @param ownerSize    the preferred size of the panel that will contain this panel
     */
    TopicListsPanel(final File theCourseDir, final Dimension ownerSize) {

        super(new StackedBorderLayout());

        this.courseDir = theCourseDir;

        final Dimension mySize = new Dimension(240, ownerSize.height - 26);
        setPreferredSize(mySize);

        final Color bg = getBackground();
        final int level = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color lineColor = level < 384 ? bg.brighter() : bg.darker();

        final Border padding = BorderFactory.createEmptyBorder(6, 6, 6, 6);
        final Border padTop = BorderFactory.createEmptyBorder(6, 0, 0, 0);
        final Border rightLine = BorderFactory.createMatteBorder(0, 0, 0, 1, lineColor);

        final Border border = BorderFactory.createCompoundBorder(rightLine, padding);
        setBorder(border);

        final JLabel heading1 = new JLabel("Subject Directories:");
        final Font font = heading1.getFont();
        final int size = font.getSize();
        final Font larger = font.deriveFont(1.2f * (float) size);
        heading1.setFont(larger);

        final JLabel heading2 = new JLabel("Topic Module Directories:");
        heading2.setFont(larger);
        heading2.setBorder(padTop);

        this.refresh = new JButton("Refresh");
        this.refresh.setFont(larger);
        final JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        buttonRow.setBorder(padTop);

        buttonRow.add(this.refresh);
        add(buttonRow, StackedBorderLayout.SOUTH);

        final Dimension headingSize = heading1.getPreferredSize();
        final Dimension buttonSize = this.refresh.getSize();
        final int availableHeight =
                ownerSize.height - 26 - headingSize.height - headingSize.height - buttonSize.height - 14;

        add(heading1, StackedBorderLayout.NORTH);

        final Dimension subjectSize = new Dimension(236, availableHeight * 2 / 7);
        this.subjectList = new JList<>();
        final JScrollPane subjectScroll = new JScrollPane(this.subjectList);
        subjectScroll.setPreferredSize(subjectSize);
        add(subjectScroll, StackedBorderLayout.NORTH);

        add(heading2, StackedBorderLayout.NORTH);
        this.moduleList = new JList<>();
        final JScrollPane moduleScroll = new JScrollPane(this.moduleList);
        add(moduleScroll, StackedBorderLayout.CENTER);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        this.refresh.setActionCommand(REFRESH_CMD);
        this.refresh.addActionListener(this);

        refresh();
    }

    /**
     * Refreshes the display.
     */
    private void refresh() {

    }

    /**
     * Called when the "refresh" button is clicked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        final String cmd = e.getActionCommand();
        if (REFRESH_CMD.equals(cmd)) {
            refresh();
        }
    }
}
