package dev.mathops.app.adm.office.student;

import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.enums.EDisciplineActionType;
import dev.mathops.db.enums.EDisciplineIncidentType;
import dev.mathops.db.schema.legacy.RawDiscipline;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A card within the "Discipline" tab of the admin app that displays the list of all discipline incidents on a student's
 * record.
 */
class DisciplineIncidentsCard extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8770589602131465348L;

    /** The panel that contains the dynamically generated list of incidents. */
    private final JPanel incidentList;

    /**
     * Constructs a new {@code DisciplineIncidentsCard}.
     *
     * @param theListener the listener to notify when the "Add" button is pressed.
     */
    DisciplineIncidentsCard(final ActionListener theListener) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.incidentList = new JPanel(new BorderLayout(10, 10));
        this.incidentList.setBackground(Skin.WHITE);
        this.incidentList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(this.incidentList, BorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.setBackground(Skin.WHITE);
        final JButton addButton = new JButton("Add Incident");
        addButton.setActionCommand(StuDisciplinePanel.ADD_CMD);
        addButton.addActionListener(theListener);
        buttons.add(addButton);
        add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Clears the display.
     */
    public void clear() {

        this.incidentList.removeAll();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    public void populateDisplay(final StudentData data) {

        if (data.studentDisciplines.isEmpty()) {
            final JLabel lbl = new JLabel("(No incidents on record)");
            this.incidentList.add(lbl, BorderLayout.NORTH);
        } else {
            JPanel outer = new JPanel(new BorderLayout(5, 5));
            outer.setBackground(Skin.WHITE);
            outer.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

            final JScrollPane scroll = new JScrollPane(outer);
            this.incidentList.add(scroll, BorderLayout.CENTER);

            scroll.getVerticalScrollBar().setUnitIncrement(6);

            for (final RawDiscipline record : data.studentDisciplines) {
                final JPanel pane = createDisciplinePane(record);
                outer.add(pane, BorderLayout.NORTH);
                final JPanel inner = new JPanel(new BorderLayout(5, 5));
                inner.setBackground(Skin.WHITE);
                outer.add(inner, BorderLayout.CENTER);
                outer = inner;
            }
        }

        invalidate();
        revalidate();
        repaint();
    }

    /**
     * Creates a panel to present a record.
     *
     * @param record the record
     * @return the panel
     */
    private JPanel createDisciplinePane(final RawDiscipline record) {

        final JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Skin.LIGHT);
        panel.setBorder(BorderFactory.createCompoundBorder(//
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JLabel[] lbls = new JLabel[8];
        lbls[0] = new JLabel("Incident Date:");
        lbls[1] = new JLabel("Interviewer:");
        lbls[2] = new JLabel("Proctor:");
        lbls[3] = new JLabel("Incident Code:");
        lbls[4] = new JLabel("Course/Unit:");
        lbls[5] = new JLabel("Description:");
        lbls[6] = new JLabel("Action Code:");
        lbls[7] = new JLabel("Comments:");
        int maxw = 0;
        int maxh = 0;
        for (final JLabel lbl : lbls) {
            lbl.setForeground(Skin.LABEL_COLOR);
            lbl.setFont(Skin.BOLD_12_FONT);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension dim = lbl.getPreferredSize();
            maxw = Math.max(maxw, dim.width);
            maxh = Math.max(maxh, dim.height);
        }
        final Dimension dim = new Dimension(maxw, maxh);
        for (final JLabel lbl : lbls) {
            lbl.setPreferredSize(dim);
        }

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.setBackground(Skin.LIGHT);
        flow1.add(lbls[0]);
        final JLabel date = new JLabel(record.dtIncident == null
                ? CoreConstants.SPC : TemporalUtils.FMT_MDY.format(record.dtIncident));
        date.setFont(Skin.BODY_12_FONT);
        flow1.add(date);
        panel.add(flow1, BorderLayout.NORTH);

        final JPanel inner1 = new JPanel(new BorderLayout());
        inner1.setBackground(Skin.LIGHT);
        panel.add(inner1, BorderLayout.CENTER);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.setBackground(Skin.LIGHT);
        flow2.add(lbls[1]);
        final JLabel interviewer = new JLabel(
                record.interviewer == null ? CoreConstants.SPC : record.interviewer);
        interviewer.setFont(Skin.BODY_12_FONT);
        flow2.add(interviewer);
        inner1.add(flow2, BorderLayout.NORTH);

        final JPanel inner2 = new JPanel(new BorderLayout());
        inner2.setBackground(Skin.LIGHT);
        inner1.add(inner2, BorderLayout.CENTER);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.setBackground(Skin.LIGHT);
        flow3.add(lbls[2]);
        final JLabel proctor =
                new JLabel(record.proctor == null ? CoreConstants.SPC : record.proctor);
        proctor.setFont(Skin.BODY_12_FONT);
        flow3.add(proctor);
        inner2.add(flow3, BorderLayout.NORTH);

        final JPanel inner3 = new JPanel(new BorderLayout());
        inner3.setBackground(Skin.LIGHT);
        inner2.add(inner3, BorderLayout.CENTER);

        final EDisciplineIncidentType type = EDisciplineIncidentType.forCode(record.incidentType);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.setBackground(Skin.LIGHT);
        flow4.add(lbls[3]);
        final JLabel incidCode =
                new JLabel(type == null ? CoreConstants.SPC : type.code + ": " + type.label);
        incidCode.setFont(Skin.BODY_12_FONT);
        flow4.add(incidCode);
        inner3.add(flow4, BorderLayout.NORTH);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.setBackground(Skin.LIGHT);
        flow5.add(lbls[4]);
        final JLabel course =
                new JLabel((record.course == null ? CoreConstants.SPC //
                        : record.course.replace("M ", "MATH "))
                        + (record.incidentType == null ? CoreConstants.SPC //
                        : ", Unit " + record.incidentType));
        course.setFont(Skin.BODY_12_FONT);
        flow5.add(course);
        inner3.add(flow5, BorderLayout.SOUTH);

        final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.setBackground(Skin.LIGHT);
        flow6.add(lbls[5]);
        final JTextArea description = new JTextArea(1, 60);
        description.setFont(Skin.BODY_12_FONT);
        description.setEditable(false);
        if (record.cheatDesc != null) {
            description.setText(record.cheatDesc);
        }
        flow6.add(description);
        inner2.add(flow6, BorderLayout.SOUTH);

        final JPanel flow7 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.setBackground(Skin.LIGHT);
        flow7.add(lbls[6]);

        final EDisciplineActionType action = EDisciplineActionType.forCode(record.actionType);

        final JLabel actCode =
                new JLabel(action == null ? CoreConstants.SPC : action.code + ": " + action.label);
        actCode.setFont(Skin.BODY_12_FONT);
        flow7.add(actCode);
        inner1.add(flow7, BorderLayout.SOUTH);

        final JPanel flow8 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.setBackground(Skin.LIGHT);
        flow8.add(lbls[7]);
        final JTextArea comments = new JTextArea(1, 60);
        comments.setFont(Skin.BODY_12_FONT);
        comments.setEditable(false);
        if (record.actionComment != null) {
            comments.setText(record.actionComment);
        }
        flow8.add(comments);
        panel.add(flow8, BorderLayout.SOUTH);

        return panel;
    }
}
