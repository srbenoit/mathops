package dev.mathops.app.exam;

import dev.mathops.app.GuiBuilderRunner;
import dev.mathops.app.IGuiBuilder;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.core.CoreConstants;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.util.Properties;

/**
 * A class to test the operation of the {@code TopBarPanel} class, by constructing a number of top bar panels
 * representing the same exam, but with different skin settings.
 */
public final class TestTopBarPanel implements IGuiBuilder {

    /** The exam session on which all top panel displays will be based. */
    private ExamSession examSession;

    /** The frame in which the top bar panels will display. */
    private final JFrame topFrame;

    /**
     * Constructs a new {@code TestTopBarPanel}.
     */
    private TestTopBarPanel() {

        makeExam();

        this.topFrame = new JFrame("TopBarPanel Tests");
        new GuiBuilderRunner(this).buildUI(this.topFrame);
    }

    /**
     * Gets the application frame.
     *
     * @return the frame
     */
    private JFrame getTopFrame() {

        return this.topFrame;
    }

    /**
     * Builds the exam used to drive the test display.
     */
    private void makeExam() {

        final ExamObj exam = new ExamObj();
        exam.examName = "M 999 Diagnostic Sample Exam";
        exam.course = "M 999";
        exam.courseUnit = "1";
        exam.allowedSeconds = Long.valueOf(10L);

        ExamSection sect = new ExamSection();
        sect.sectionName = "Section One: This is the long name";
        sect.shortName = "Section 1: Short name";
        sect.score = Long.valueOf(4L);
        sect.minMoveonScore = Long.valueOf(1L);
        sect.minMasteryScore = Long.valueOf(4L);
        sect.mastered = true;
        sect.enabled = true;
        sect.passed = true;
        sect.canComeBack = false;
        sect.canRegenerate = false;
        exam.addSection(sect);

        sect = new ExamSection();
        sect.sectionName = "Section Two: This is the long name";
        sect.shortName = "Section 2: Short name";
        sect.score = Long.valueOf(2L);
        sect.minMoveonScore = Long.valueOf(1L);
        sect.minMasteryScore = Long.valueOf(4L);
        sect.mastered = false;
        sect.enabled = true;
        sect.passed = true;
        sect.canComeBack = true;
        sect.canRegenerate = false;
        exam.addSection(sect);

        sect = new ExamSection();
        sect.sectionName = "Section Three: This is the long name";
        sect.shortName = "Section 3: Short name";
        sect.minMoveonScore = Long.valueOf(1L);
        sect.minMasteryScore = Long.valueOf(4L);
        sect.mastered = false;
        sect.enabled = true;
        sect.passed = false;
        sect.canComeBack = true;
        sect.canRegenerate = true;
        exam.addSection(sect);

        this.examSession = new ExamSession(EExamSessionState.INTERACTING, exam);
    }

    /**
     * Constructs the GUI, to be called in the AWT event thread.
     *
     * @param frame the frame to which to add menus if needed
     */
    @Override
    public void buildUI(final JFrame frame) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setPreferredSize(new Dimension(800, 600));
        frame.setContentPane(panel);

        // Build top panel with Skin 1: Used by testing center machines
        JPanel inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("SKIN USED BY TESTING CENTER MACHINES:"), BorderLayout.PAGE_START);

        TopBarPanel top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin1());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        // Move down in the window...
        panel.add(inner, BorderLayout.PAGE_START);
        inner = new JPanel(new BorderLayout());
        panel.add(inner, BorderLayout.CENTER);
        panel = inner;

        // Build top panel with Skin 2: Used by remote placement exam
        inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("SKIN USED BY REMOTE PLACEMENT EXAM:"), BorderLayout.PAGE_START);
        top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin2());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        // Move down in the window...
        panel.add(inner, BorderLayout.PAGE_START);
        inner = new JPanel(new BorderLayout());
        panel.add(inner, BorderLayout.CENTER);
        panel = inner;

        // Build top panel with Skin 3: Used by review exams
        inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("SKIN USED BY REVIEW EXAMS:"), BorderLayout.PAGE_START);
        top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin3());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        // Move down in the window...
        panel.add(inner, BorderLayout.PAGE_START);
        inner = new JPanel(new BorderLayout());
        panel.add(inner, BorderLayout.CENTER);
        panel = inner;

        // Build top panel with Skin 4: Used by past exam viewer
        inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("SKIN USED TO VIEW PAST REVIEW EXAMS:"), BorderLayout.PAGE_START);
        top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin4());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        // Move down in the window...
        panel.add(inner, BorderLayout.PAGE_START);
        inner = new JPanel(new BorderLayout());
        panel.add(inner, BorderLayout.CENTER);
        panel = inner;

        // Build top panel with Skin 5: Used by past exam viewer
        inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("SKIN USED BY HOMEWORK ASSIGNMENTS:"), BorderLayout.PAGE_START);
        top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin5());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        panel.add(inner, BorderLayout.PAGE_START);
        inner = new JPanel(new BorderLayout());
        panel.add(inner, BorderLayout.CENTER);
        panel = inner;

        // Build top panel with Skin 6: Experimental
        inner = new JPanel(new BorderLayout());
        inner.add(new JLabel("EXPERIMENTAL SKIN:"), BorderLayout.PAGE_START);
        top = new TopBarPanel(this.examSession, "John Doe", false, false, new TestTopBarSkin6());
        top.setSelectedSectionIndex(2);
        new Thread(top).start();
        inner.add(top, BorderLayout.PAGE_END);

        panel.add(inner, BorderLayout.PAGE_START);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Main method to run the test.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final TestTopBarPanel tests = new TestTopBarPanel();
        final JFrame frame = tests.topFrame;

        while (frame.isVisible()) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        frame.dispose();
    }
}

/**
 * The skin used by testing center machines.
 */
final class TestTopBarSkin1 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7006821402079442037L;

    /**
     * Construct a new {@code TestTopBarSkin1} properties object.
     */
    TestTopBarSkin1() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-username", "Logged In As: $USERNAME"},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray88"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "2"},
                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "14"},
                {"top-bar-username-style", "plain"},
                {"top-bar-username-color", "black"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},
                {"top-bar-clock-format", "Current Time: HH:MM"},
                {"top-bar-clock-font", "SANS"},
                {"top-bar-clock-size", "14"},
                {"top-bar-clock-style", "plain"},
                {"top-bar-clock-color", "black"},
                {"top-bar-clock-x", "0.5"},
                {"top-bar-clock-y", "53"},
                {"top-bar-clock-alignment", "center"},
                {"top-bar-timer-format", "Time Remaining: HH:MM:SS"},
                {"top-bar-timer-font", "SANS"},
                {"top-bar-timer-size", "14"},
                {"top-bar-timer-style", "plain"},
                {"top-bar-timer-color", "black"},
                {"top-bar-timer-x", "0.99"},
                {"top-bar-timer-y", "53"},
                {"top-bar-timer-alignment", "right"},
                {"top-bar-show-sections", "false"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}

/**
 * The skin used by remote placement exams.
 */
final class TestTopBarSkin2 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7318101720192630973L;

    /**
     * Constructs a new {@code TestTopBarSkin2} properties object.
     */
    TestTopBarSkin2() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-username", "Logged In As: $USERNAME"},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray80"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "1"},
                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "14"},
                {"top-bar-username-style", "plain"},
                {"top-bar-username-color", "black"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},
                {"top-bar-clock-format", "Current Time: HH:MM"},
                {"top-bar-clock-font", "SANS"},
                {"top-bar-clock-size", "14"},
                {"top-bar-clock-style", "plain"},
                {"top-bar-clock-color", "black"},
                {"top-bar-clock-x", "0.5"},
                {"top-bar-clock-y", "53"},
                {"top-bar-clock-alignment", "center"},
                {"top-bar-timer-format", "Time Remaining: HH:MM:SS"},
                {"top-bar-timer-font", "SANS"},
                {"top-bar-timer-size", "14"},
                {"top-bar-timer-style", "plain"},
                {"top-bar-timer-color", "black"},
                {"top-bar-timer-x", "0.99"},
                {"top-bar-timer-y", "53"},
                {"top-bar-timer-alignment", "right"},
                {"top-bar-show-sections", "false"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}

/**
 * The skin used by review exams.
 */
final class TestTopBarSkin3 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8660107880762654564L;

    /**
     * Constructs a new {@code TestTopBarSkin3} properties object.
     */
    TestTopBarSkin3() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-username", "$USERNAME"},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray88"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "2"},
                {"top-bar-show-sections", "false"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}

/**
 * The skin used when reviewing a historic exam record.
 */
final class TestTopBarSkin4 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2538701266741439851L;

    /**
     * Constructs a new {@code TestTopBarSkin4} properties object.
     */
    TestTopBarSkin4() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-username", "$USERNAME"},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray88"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "2"},
                {"top-bar-show-sections", "false"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}

/**
 * The skin used by homework assignments.
 */
final class TestTopBarSkin5 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4503077766223166055L;

    /**
     * Constructs a new {@code TestTopBarSkin5} properties object.
     */
    TestTopBarSkin5() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-title-show-answers", "$EXAM_TITLE Answers"},
                {"top-bar-username", "All sections must be mastered to earn a homework credit."},
                {"top-bar-username-practice", CoreConstants.EMPTY},
                {"top-bar-username-zero-req", CoreConstants.EMPTY},
                {"top-bar-username-one-section", CoreConstants.EMPTY},
                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},
                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray80"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "1"},
                {"top-bar-show-sections", "true"},
                {"top-bar-show-sections-if-one", "false"},
                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "15"},
                {"top-bar-username-style", "bold"},
                {"top-bar-username-color", "navy"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},
                {"top-bar-section-font", "SANS"},
                {"top-bar-section-size", "14"},
                {"top-bar-section-style", "bold"},
                {"top-bar-section-color", "navy"},
                {"top-bar-section-x", "0.05"},
                {"top-bar-section-y", "62"},
                {"top-bar-section-alignment", "left"},
                {"top-bar-divider-y", "56"},
                {"top-bar-divider-color", "gray70"},
                {"top-bar-divider-start-x", "0.01"},
                {"top-bar-divider-end-x", "0.99"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}

/**
 * An experimental skin.
 */
final class TestTopBarSkin6 extends Properties {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4118261014012970966L;

    /**
     * Constructs a new {@code TestTopBarSkin6} properties object.
     */
    TestTopBarSkin6() {

        super();

        final String[][] contents = {
                {"top-bar-title", "$EXAM_TITLE"},
                {"top-bar-title-show-answers", "$EXAM_TITLE Answers"},

                {"top-bar-username", "Logged In As: $USERNAME"},
                {"top-bar-username-practice", "This is a practice assignment."},
                {"top-bar-username-zero-req", "Min move-on/mastery is 0."},
                {"top-bar-username-one-section", "There is only one section."},

                {"top-bar-background-color", "alice blue"},
                {"top-bar-border-color", "steel blue"},
                {"top-bar-border-size", "1"},
                {"top-bar-border-inset", "1"},

                {"top-bar-title-font", "SANS"},
                {"top-bar-title-size", "24"},
                {"top-bar-title-style", "bold"},
                {"top-bar-title-color", "SteelBlue4"},
                {"top-bar-title-x", "0.5"},
                {"top-bar-title-y", "28"},
                {"top-bar-title-alignment", "center"},
                {"top-bar-title-shadow-color", "gray88"},
                {"top-bar-title-shadow-dx", "2"},
                {"top-bar-title-shadow-dy", "2"},

                {"top-bar-username-font", "SANS"},
                {"top-bar-username-size", "14"},
                {"top-bar-username-style", "plain"},
                {"top-bar-username-color", "black"},
                {"top-bar-username-x", "0.01"},
                {"top-bar-username-y", "53"},
                {"top-bar-username-alignment", "left"},

                {"top-bar-clock-format", "Current Time: HH:MM"},
                {"top-bar-clock-font", "SANS"},
                {"top-bar-clock-size", "14"},
                {"top-bar-clock-style", "plain"},
                {"top-bar-clock-color", "black"},
                {"top-bar-clock-x", "0.5"},
                {"top-bar-clock-y", "53"},
                {"top-bar-clock-alignment", "center"},

                {"top-bar-timer-format", "Time Remaining: HH:MM:SS"},
                {"top-bar-timer-font", "SANS"},
                {"top-bar-timer-size", "14"},
                {"top-bar-timer-style", "plain"},
                {"top-bar-timer-color", "black"},
                {"top-bar-timer-x", "0.99"},
                {"top-bar-timer-y", "53"},
                {"top-bar-timer-alignment", "right"},

                {"top-bar-divider-y", "58"},
                {"top-bar-divider-color", "gray70"},
                {"top-bar-divider-start-x", "0.01"},
                {"top-bar-divider-end-x", "0.99"},

                {"top-bar-show-sections", "true"},
                {"top-bar-section-font", "SANS"},
                {"top-bar-section-size", "14"},
                {"top-bar-section-style", "bold"},
                {"top-bar-section-color", "navy"},
                {"top-bar-section-x", "0.05"},
                {"top-bar-section-y", "64"},
                {"top-bar-section-alignment", "left"},};
        for (final String[] content : contents) {
            setProperty(content[0], content[1]);
        }
    }
}
