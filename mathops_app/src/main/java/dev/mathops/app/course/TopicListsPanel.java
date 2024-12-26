package dev.mathops.app.course;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;

final class TopicListsPanel extends JPanel implements ActionListener, ListSelectionListener {

    /** The action command for the refresh button. */
    private static final String REFRESH_CMD = "REFRESH";

    /** A character used in subject and module directory names. */
    private static final int UNDERSCORE = (int) '_';

    /** The course directory. */
    private final File courseDir;

    /** The model for the list of subjects. */
    private final DefaultListModel<String> subjectModel;

    /** The list of subjects. */
    private final JList<String> subjectList;

    /** The model for the list of topic modules. */
    private final DefaultListModel<String> moduleModel;

    /** The list of modules. */
    private final JList<String> moduleList;

    /** The refresh button. */
    private final JButton refresh;

    /** The owning main window. */
    private MainWindow owner = null;

    /** Flag to ignore change events during mutation. */
    private boolean allowMutate = true;

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

        this.subjectModel = new DefaultListModel<>();
        this.subjectList = new JList<>(this.subjectModel);
        final JScrollPane subjectScroll = new JScrollPane(this.subjectList);
        subjectScroll.setPreferredSize(subjectSize);
        add(subjectScroll, StackedBorderLayout.NORTH);

        add(heading2, StackedBorderLayout.NORTH);
        this.moduleModel = new DefaultListModel<>();
        this.moduleList = new JList<>(this.moduleModel);
        final JScrollPane moduleScroll = new JScrollPane(this.moduleList);
        add(moduleScroll, StackedBorderLayout.CENTER);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     *
     * @param theOwner the owning main window
     */
    void init(final MainWindow theOwner) {

        this.owner = theOwner;

        this.refresh.setActionCommand(REFRESH_CMD);
        this.refresh.addActionListener(this);

        this.subjectList.addListSelectionListener(this);
        this.moduleList.addListSelectionListener(this);

        refresh();
    }

    /**
     * Refreshes the display.
     */
    private void refresh() {

        final String selectedSubject = this.subjectList.getSelectedValue();
        final String selectedModule = this.moduleList.getSelectedValue();

        File selectedFile = null;
        this.subjectModel.removeAllElements();
        final File[] topList = this.courseDir.listFiles();
        if (topList != null) {
            for (final File file : topList) {
                if (file.isDirectory()) {
                    final String name = file.getName();
                    if (name.length() > 3) {
                        final char c1 = name.charAt(0);
                        final char c2 = name.charAt(1);
                        final char c3 = name.charAt(2);

                        if (Character.isDigit(c1) && Character.isDigit(c2) && (int) c3 == UNDERSCORE) {
                            this.subjectModel.addElement(name);

                            if (name.equals(selectedSubject)) {
                                selectedFile = file;
                            }
                        }
                    }
                }
            }
        }

        if (Objects.nonNull(selectedFile)) {
            this.subjectList.setSelectedValue(selectedSubject, true);

            boolean reselectModule = false;
            this.moduleModel.removeAllElements();

            final File[] modList = selectedFile.listFiles();
            if (modList != null) {
                for (final File file : modList) {
                    if (file.isDirectory()) {
                        final String name = file.getName();
                        if (name.length() > 3) {
                            final char c1 = name.charAt(0);
                            final char c2 = name.charAt(1);
                            final char c3 = name.charAt(2);

                            if (Character.isDigit(c1) && Character.isDigit(c2) && (int) c3 == UNDERSCORE) {
                                this.moduleModel.addElement(name);

                                if (name.equals(selectedModule)) {
                                    reselectModule = true;
                                }
                            }
                        }
                    }
                }
            }

            if (reselectModule) {
                this.moduleList.setSelectedValue(selectedModule, true);
                this.owner.setSelection(selectedSubject, selectedModule);
            } else {
                this.owner.setSelection(selectedSubject, null);
            }
        } else {
            this.moduleModel.removeAllElements();
            this.owner.setSelection(null, null);
        }
    }

    /**
     * Called when the "refresh" button is clicked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        if (REFRESH_CMD.equals(cmd)) {
            refresh();
        }
    }

    /**
     * Called when the selection is changed in either list.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        if (this.allowMutate) {
            this.allowMutate = false;
            refresh();
            this.allowMutate = true;
        }
    }
}
