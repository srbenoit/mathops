package dev.mathops.app.scheduling;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.model.AttrKey;
import dev.mathops.commons.model.ModelTreeNode;
import dev.mathops.commons.model.StringParseException;
import dev.mathops.commons.model.TypedMap;
import dev.mathops.commons.model.codec.StringCodec;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.text.model.ParsingLog;
import dev.mathops.text.model.SimpleModelTreeNodeFactory;
import dev.mathops.text.model.XmlTreeParser;
import dev.mathops.text.model.XmlTreeWriter;
import dev.mathops.text.parser.LineOrientedParserInput;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * A simple editor that takes XML data, parses it as a model tree, then generates XML from that model tree to validate
 * parsing and XML generation.
 */
public final class OfficeHoursScheduling implements Runnable, ListSelectionListener, ActionListener {

    /** An action command. */
    private static final String SAVE_CMD = "SAVE";

    /** An action command. */
    private static final String LOAD_CMD = "LOAD";

    /** An action command. */
    private static final String CALC_CMD = "CALC";

    /** The frame. */
    private JFrame frame = null;

    /** The center panel. */
    private JPanel center = null;

    /** The model for the list of student names. */
    private DefaultListModel<String> model = null;

    /** The list of students. */
    private JList<String> studentList = null;

    /** The list of student preferences. */
    private StudentPreferences[] prefs = null;

    /** The list of "yes" radio buttons. */
    private JRadioButton[][] yes = null;

    /** The list of "no" radio buttons. */
    private JRadioButton[][] no = null;

    /** The list of button groups. */
    private ButtonGroup[][] groups = null;

    /** The list of buttons to clear preferences. */
    private JButton[][] clear = null;

    /** The active index. */
    private int activeIndex = -1;

    /**
     * Private constructor to prevent instantiation
     */
    private OfficeHoursScheduling() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final String numStudentsStr = JOptionPane.showInputDialog("How many students?");
        try {
            final int numStudents = Integer.parseInt(numStudentsStr);
            if (numStudents > 0) {
                final String numTimeslotsStr = JOptionPane.showInputDialog("How many time slots?");
                try {
                    final int numTimeslots = Integer.parseInt(numTimeslotsStr);
                    if (numTimeslots > 0) {
                        buildUI(numStudents, numTimeslots);
                    } else {
                        JOptionPane.showMessageDialog(null, "Number of timeslots must be positive.");
                    }
                } catch (final NumberFormatException ex) {
                    final String msg = SimpleBuilder.concat("Unable to interpret '", numTimeslotsStr,
                            "' as a number of timeslots.");
                    JOptionPane.showMessageDialog(null, msg);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Number of students must be positive.");
            }
        } catch (final NumberFormatException ex) {
            final String msg = SimpleBuilder.concat("Unable to interpret '", numStudentsStr,
                    "' as a number of students.");
            JOptionPane.showMessageDialog(null, msg);
        }
    }

    /**
     * Constructs the UI.
     *
     * @param numStudents  the number of students
     * @param numTimeslots the number of time slots
     */
    private void buildUI(final int numStudents, final int numTimeslots) {

        this.frame = new JFrame("Office Hours Scheduling");
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout());
        this.frame.setContentPane(content);

        this.model = new DefaultListModel<>();
        this.studentList = new JList<>(this.model);
        final Border lineBorder = BorderFactory.createLineBorder(Color.GRAY);
        this.studentList.setBorder(lineBorder);
        this.studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.studentList.addListSelectionListener(this);
        content.add(this.studentList, StackedBorderLayout.WEST);

        final JPanel grid = createDataAndGrid(numStudents, numTimeslots);
        this.center = new JPanel(new StackedBorderLayout());
        content.add(this.center, StackedBorderLayout.CENTER);
        this.center.add(grid, StackedBorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        final JButton save = new JButton("Save");
        save.setActionCommand(SAVE_CMD);
        save.addActionListener(this);
        final JButton load = new JButton("Load");
        load.setActionCommand(LOAD_CMD);
        load.addActionListener(this);
        final JButton calc = new JButton("Calculate");
        calc.setActionCommand(CALC_CMD);
        calc.addActionListener(this);
        buttons.add(save);
        buttons.add(load);
        buttons.add(calc);
        content.add(buttons, StackedBorderLayout.SOUTH);

        UIUtilities.packAndCenter(this.frame);
        this.frame.setVisible(true);
    }

    /**
     * Creates the data arrays and grid based on some number of slots and students.
     *
     * @param numStudents  the number of students
     * @param numTimeslots the number of time slots
     * @return the grid pane
     */
    private JPanel createDataAndGrid(final int numStudents, final int numTimeslots) {

        this.prefs = new StudentPreferences[numStudents];
        this.yes = new JRadioButton[5][numTimeslots];
        this.no = new JRadioButton[5][numTimeslots];
        this.groups = new ButtonGroup[5][numTimeslots];
        this.clear = new JButton[5][numTimeslots];

        this.model.clear();
        for (int i = 0; i < numStudents; ++i) {
            final String name = "Student " + (i + 1);
            this.model.addElement(name);

            this.prefs[i] = new StudentPreferences(numTimeslots);
        }

        final JPanel grid = new JPanel(new GridLayout(numTimeslots + 1, 6, 10, 10));

        grid.add(new JLabel(" "));
        final JLabel mon = new JLabel("Monday");
        final Font font = mon.getFont();
        final Font bold = font.deriveFont(Font.BOLD);
        mon.setHorizontalAlignment(SwingConstants.CENTER);
        mon.setFont(bold);
        grid.add(mon);

        final JLabel tue = new JLabel("Tuesday");
        tue.setHorizontalAlignment(SwingConstants.CENTER);
        tue.setFont(bold);
        grid.add(tue);

        final JLabel wed = new JLabel("Wednesday");
        wed.setHorizontalAlignment(SwingConstants.CENTER);
        wed.setFont(bold);
        grid.add(wed);

        final JLabel thu = new JLabel("Thursday");
        thu.setHorizontalAlignment(SwingConstants.CENTER);
        thu.setFont(bold);
        grid.add(thu);

        final JLabel fri = new JLabel("Friday");
        fri.setHorizontalAlignment(SwingConstants.CENTER);
        fri.setFont(bold);
        grid.add(fri);

        for (int i = 0; i < numTimeslots; ++i) {
            final JLabel lbl = new JLabel("Slot " + (i + 1));
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            grid.add(lbl);
            for (int j = 0; j < 5; ++j) {
                final JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
                this.yes[j][i] = new JRadioButton("Yes");
                this.no[j][i] = new JRadioButton("No");
                this.groups[j][i] = new ButtonGroup();
                this.groups[j][i].add(this.yes[j][i]);
                this.groups[j][i].add(this.no[j][i]);
                this.clear[j][i] = new JButton(CoreConstants.SPC);

                this.yes[j][i].addActionListener(this);
                this.no[j][i].addActionListener(this);
                this.clear[j][i].addActionListener(this);

                inner.add(this.yes[j][i]);
                inner.add(this.no[j][i]);
                inner.add(this.clear[j][i]);
                grid.add(inner);
            }
        }
        setGridEnabled(false);

        return grid;
    }

    /**
     * Called when the list selection changes.
     *
     * @param e the event that characterizes the change.
     */
    @Override
    public void valueChanged(final ListSelectionEvent e) {

        if (!e.getValueIsAdjusting()) {
            this.activeIndex = this.studentList.getSelectedIndex();
            if (this.activeIndex == -1) {
                clearGrid();
                setGridEnabled(false);
            } else {
                setGridEnabled(true);
                final StudentPreferences studentPrefs = this.prefs[this.activeIndex];
                populateGrid(studentPrefs);
            }
        }
    }

    /**
     * Called when a button is activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        if (this.yes != null) {
            if (SAVE_CMD.equals(cmd)) {
                doSave();
            } else if (LOAD_CMD.equals(cmd)) {
                doLoad();
            } else if (CALC_CMD.equals(cmd)) {
                doCalc();
            } else if (this.activeIndex != -1) {
                final int numSlots = this.yes[0].length;
                final Object source = e.getSource();

                for (int day = 0; day < 5; ++day) {
                    for (int slot = 0; slot < numSlots; ++slot) {
                        if (source == this.clear[day][slot]) {
                            this.groups[day][slot].clearSelection();
                            this.prefs[this.activeIndex].set(day, slot, 0);
                        } else if (source == this.yes[day][slot] || source == this.no[day][slot]) {
                            if (this.yes[day][slot].isSelected()) {
                                this.prefs[this.activeIndex].set(day, slot, 1);
                            } else if (this.no[day][slot].isSelected()) {
                                this.prefs[this.activeIndex].set(day, slot, -1);
                            } else {
                                this.prefs[this.activeIndex].set(day, slot, 0);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets the enabled state of the grid.
     *
     * @param enabled true to enable; false to disable
     */
    private void setGridEnabled(final boolean enabled) {

        if (this.yes != null) {
            final int numSlots = this.yes[0].length;
            for (int day = 0; day < 5; ++day) {
                for (int slot = 0; slot < numSlots; ++slot) {
                    this.yes[day][slot].setEnabled(enabled);
                    this.no[day][slot].setEnabled(enabled);
                    this.clear[day][slot].setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Clears the grid.
     */
    private void clearGrid() {

        if (this.yes != null) {
            final int numSlots = this.yes[0].length;
            for (int day = 0; day < 5; ++day) {
                for (int slot = 0; slot < numSlots; ++slot) {
                    this.groups[day][slot].clearSelection();
                }
            }
        }
    }

    /**
     * Populates the grid with preferences for a single student.
     *
     * @param studentPrefs the student preferences
     */
    private void populateGrid(final StudentPreferences studentPrefs) {

        if (this.yes != null) {
            final int numSlots = this.yes[0].length;
            for (int day = 0; day < 5; ++day) {
                for (int slot = 0; slot < numSlots; ++slot) {
                    final int pref = studentPrefs.get(day, slot);
                    if (pref == 1) {
                        this.yes[day][slot].setSelected(true);
                    } else if (pref == -1) {
                        this.no[day][slot].setSelected(true);
                    } else {
                        this.groups[day][slot].clearSelection();
                    }
                }
            }
        }
    }

    /**
     * Saves the data to a file.
     */
    private void doSave() {

        final int numStudents = this.prefs.length;
        final String numStudentsStr = Integer.toString(numStudents);

        final int numSlots = this.yes[0].length;
        final String numSlotsStr = Integer.toString(numSlots);

        final HtmlBuilder xml = new HtmlBuilder(1000);
        xml.addln("<office-hours-scheduling num-students='", numStudentsStr, "' num-slots='", numSlotsStr, "'>");
        for (int stuIndex = 0; stuIndex < numStudents; ++stuIndex) {
            final StudentPreferences pref = this.prefs[stuIndex];
            xml.addln("  <stu>");
            for (int day = 0; day < 5; ++day) {
                final String dayStr = Integer.toString(day);
                xml.add("    <d", dayStr);
                for (int slot = 0; slot < numSlots; ++slot) {
                    final String slotStr = Integer.toString(slot);
                    final int value = pref.get(day, slot);
                    final String valueStr = Integer.toString(value);
                    xml.add(" s", slotStr, "='", valueStr, "'");
                }
                xml.addln("/>");
            }
            xml.addln("  </stu>");
        }
        xml.addln("</office-hours-scheduling>");

        final String fileContents = xml.toString();
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        if (chooser.showSaveDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
            final File target = chooser.getSelectedFile();
            boolean write = true;
            if (target.exists()) {
                if (JOptionPane.showConfirmDialog(this.frame,
                        "Overwrite existing file?") != JOptionPane.YES_OPTION) {
                    write = false;
                }
            }

            if (write) {
                try (final FileWriter writer = new FileWriter(target)) {
                    writer.write(fileContents);
                } catch (final IOException ex) {
                    Log.warning(ex);
                    JOptionPane.showMessageDialog(this.frame, "Failed to write file.");
                }
            }
        }
    }

    /**
     * Loads data from a file
     */
    private void doLoad() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        if (chooser.showOpenDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
            final File source = chooser.getSelectedFile();
            final String[] lines = FileLoader.loadFileAsLines(source, true);
            if (lines == null) {
                JOptionPane.showMessageDialog(this.frame, "Failed to load file.");
            } else {
                final LineOrientedParserInput parserInput = new LineOrientedParserInput(lines);
                final ParsingLog log = new ParsingLog(10);
                final ModelTreeNode root = XmlTreeParser.parseXml(parserInput, new SimpleModelTreeNodeFactory(), log);

                if (root == null) {
                    JOptionPane.showMessageDialog(this.frame, "Failed to parse file.");
                } else {
                    interpretTree(root);
                }
            }
        }
    }

    /**
     * Interprets a model tree representing a loaded XML file.
     * @param root the root tree node
     */
    private void interpretTree(final ModelTreeNode root) {

        final TypedMap rootMap = root.map();
        final String tag = rootMap.getString(XmlTreeWriter.TAG);
        if ("office-hours-scheduling".equals(tag)) {
            final AttrKey<String> NUM_STUDENTS = new AttrKey<>("num-students", StringCodec.INST);
            final AttrKey<String> NUM_SLOTS = new AttrKey<>("num-slots", StringCodec.INST);
            final String numStu = rootMap.getString(NUM_STUDENTS);
            final String numSlots = rootMap.getString(NUM_SLOTS);

            if (numStu == null || numSlots == null) {
                JOptionPane.showMessageDialog(this.frame, "Failed to extract number of students and time slots.");
            } else {
                try {
                    final int stuCount = Integer.parseInt(numStu);
                    final int slotCount = Integer.parseInt(numSlots);

                    if (stuCount < 1 || slotCount < 1) {
                        JOptionPane.showMessageDialog(this.frame, "Invalid numbers of tudents and time slots.");
                    } else {
                        this.center.removeAll();
                        final JPanel grid = createDataAndGrid(stuCount, slotCount);
                        this.center.add(grid, StackedBorderLayout.NORTH);

                        interpretStudents(root, stuCount, slotCount);

                        this.center.invalidate();
                        this.center.revalidate();
                        this.center.repaint();
                    }
                } catch (final NumberFormatException ex) {
                    Log.warning(ex);
                    JOptionPane.showMessageDialog(this.frame, "Failed to extract number of students and time slots.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this.frame, "Top-level element is not 'office-hours-scheduling'.");
        }
    }

    /**
     * Interprets the student elements in a parsed XML file model tree.
     *
     * <pre>
     *   &lt;stu&gt;
     *     &lt;d0 s0='1' s1='-1' s2='0'/&gt;
     *   &lt;/stu&gt;
     * </pre>
     *
     * @param root the root tree node
     * @param stuCount the student count
     * @param slotCount the slot count
     * @throws StringParseException if there is an error parsing a preference value
     */
    private void interpretStudents(final ModelTreeNode root, final int stuCount, final int slotCount)
            throws NumberFormatException {

        int stuIndex = 0;

        ModelTreeNode stuNode = root.getFirstChild();
        while (stuNode != null) {
            final TypedMap stuMap = stuNode.map();
            final String stuTag = stuMap.getString(XmlTreeWriter.TAG);

            if ("stu".equals(stuTag)) {
                final StudentPreferences stuPrefs = this.prefs[stuIndex];

                ModelTreeNode dayNode = stuNode.getFirstChild();
                while (dayNode != null) {
                    final TypedMap dayMap = dayNode.map();
                    final String dayTag = dayMap.getString(XmlTreeWriter.TAG);
                    final int dayIndex;
                    if ("d0".equals(dayTag)) {
                        dayIndex = 0;
                    } else if ("d1".equals(dayTag)) {
                        dayIndex = 1;
                    } else if ("d2".equals(dayTag)) {
                        dayIndex = 2;
                    } else if ("d3".equals(dayTag)) {
                        dayIndex = 3;
                    } else if ("d4".equals(dayTag)) {
                        dayIndex = 4;
                    } else {
                        dayIndex = -1;
                    }

                    if (dayIndex >= 0) {
                        for (int s = 0; s < slotCount; ++s) {
                            final AttrKey<String> slotKey = new AttrKey<>("s" + s, StringCodec.INST);
                            final String valueStr = dayMap.getString(slotKey);
                            final int val = Integer.parseInt(valueStr);
                            stuPrefs.set(dayIndex, s, val);
                        }
                    }

                    dayNode = dayNode.getNextSibling();
                }

                ++stuIndex;
                if (stuIndex == stuCount) {
                    break;
                }
            }

            stuNode = stuNode.getNextSibling();
        }
    }

    /**
     * Calculates the best office hours time slots.
     */
    private void doCalc() {

        // For each slot, find the number of students that slot can help.  Take the slot with the greatest, and remove
        // all students from consideration that were helped.  Repeat for up to 5 slots.  With each slot, report the
        // number of students helped and the number that remain unable to get help.

        final int numStudents = this.prefs.length;
        final boolean[] helped = new boolean[numStudents];

        final int numSlots = this.yes[0].length;
        final int[][] numberHelped = new int[5][numSlots];
        final int[][] numberGood = new int[5][numSlots];

        for (int round = 0; round < 5; ++round) {
            for (int stu = 0; stu < numStudents; ++stu) {
                if (!helped[stu]) {
                    final StudentPreferences stuPrefs = this.prefs[stu];

                    for (int day = 0; day < 5; ++day) {
                        for (int slot = 0; slot < numSlots; ++slot) {
                            final int pref = stuPrefs.get(day, slot);
                            if (pref == 1) {
                                ++numberHelped[day][slot];
                                ++numberGood[day][slot];
                            } else if (pref == 0) {
                                ++numberHelped[day][slot];
                            }
                        }
                    }
                }
            }

            // Pick the best one, mark those students as helped.
            int bestDay = 0;
            int bestSlot = 0;
            int highestHelped = numberHelped[0][0];
            int highestGood = numberGood[0][0];
            for (int day = 0; day < 5; ++day) {
                for (int slot = 0; slot < numSlots; ++slot) {
                    if (numberHelped[day][slot] > highestHelped) {
                        bestDay = day;
                        bestSlot = slot;
                        highestHelped = numberHelped[day][slot];
                        highestGood = numberGood[day][slot];
                    } else if (numberHelped[day][slot] == highestHelped && numberGood[day][slot] > highestGood) {
                        bestDay = day;
                        bestSlot = slot;
                        highestGood = numberGood[day][slot];
                    }
                }
            }

            Log.info("Best day/slot for office hours is day " + bestDay + " slot " + bestSlot);
            Log.info("    This helps " + highestHelped + " students, with " + highestGood + " labeling slot as good");

            // Mark those students as helped
            int remainingToHelp = 0;
            for (int stu = 0; stu < numStudents; ++stu) {
                if (!helped[stu]) {
                    final StudentPreferences stuPrefs = this.prefs[stu];
                    final int pref = stuPrefs.get(bestDay, bestSlot);
                    if (pref > -1) {
                        helped[stu] = true;
                    } else {
                        ++remainingToHelp;
                    }
                }
            }
            if (remainingToHelp == 0) {
                Log.info("All students can access an office hour.");
                break;
            }

            // Clear the arrays
            for (int day = 0; day < 5; ++day) {
                Arrays.fill(numberHelped[day], 0);
                Arrays.fill(numberGood[day], 0);
            }
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        FlatLightLaf.setup();
        SwingUtilities.invokeLater(new OfficeHoursScheduling());
    }
}

