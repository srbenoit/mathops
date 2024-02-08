package dev.mathops.app.mocks;

import com.formdev.flatlaf.FlatLightLaf;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * A class that mocks the RamReady client within RamWeb.  This client sends requests to the RamReady service and
 * displays results.  It can simulate various error conditions as well.
 */
public class RamReadyMock implements Runnable, ActionListener {

    private static final String CHECK_MATH_PLAN_CMD = "CHECK_MATH_PLAN";

    private static final String CHECK_PLACEMENT_CMD = "CHECK_PLACEMENT";

    private static final String PROD_HOST = "https://placement.math.colostate.edu/";

    private static final String DEV_HOST = "https://placementdev.math.colostate.edu/";

    /** The Development server selector. */
    private JCheckBox devServer;

    /** The PIDM field. */
    private JTextField pidm;

    /** The result. */
    private JTextArea result;

    RamReadyMock() {

    }

    public void run() {

        final JFrame frame = new JFrame("RamReady Mock");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout());
        frame.setContentPane(content);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        topFlow.add(new JLabel("PIDM: "));
        this.pidm = new JTextField(10);
        topFlow.add(this.pidm);
        this.devServer = new JCheckBox("Use Development Server");
        topFlow.add(this.devServer);
        content.add(topFlow, StackedBorderLayout.NORTH);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JButton checkMathPlan = new JButton("CheckMathPlan");
        checkMathPlan.setActionCommand(CHECK_MATH_PLAN_CMD);
        checkMathPlan.addActionListener(this);
        buttonFlow.add(checkMathPlan);
        final JButton checkMathPlacement = new JButton("CheckMathPlacement");
        checkMathPlacement.setActionCommand(CHECK_PLACEMENT_CMD);
        checkMathPlacement.addActionListener(this);
        buttonFlow.add(checkMathPlacement);
        content.add(buttonFlow, StackedBorderLayout.NORTH);

        this.result = new JTextArea(10, 90);
        this.result.setBorder(this.pidm.getBorder());
        this.result.setEnabled(false);
        content.add(new JLabel("Results"), StackedBorderLayout.NORTH);
        content.add(this.result, StackedBorderLayout.CENTER);

        frame.pack();
        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CHECK_MATH_PLAN_CMD.equals(cmd)) {
            doCheckMathPlan();
        } else if (CHECK_PLACEMENT_CMD.equals(cmd)) {
            doCheckMathPlacement();
        }
     }

    private void doCheckMathPlan() {

        this.result.setText(CoreConstants.EMPTY);

        final String pidmStr = this.pidm.getText();
        final String host = this.devServer.isSelected() ? DEV_HOST : PROD_HOST;
        final String httpsURL = host + "/welcome/ramready.svc/CheckMathPlan/" + pidmStr;

        try {
            final URL myUrl = new URL(httpsURL);
            final HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
            conn.setRequestProperty("X-CSUMATH-RamWeb", "93u54ki3bngtowIE");
            final InputStream is = conn.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader br = new BufferedReader(isr);

            final HtmlBuilder reply = new HtmlBuilder(200);
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                reply.addln(inputLine);
            }

            br.close();

            this.result.setText(reply.toString());
        } catch (final IOException e) {
            Log.warning(e);
        }
    }

    private void doCheckMathPlacement() {

        this.result.setText(CoreConstants.EMPTY);

        final String pidmStr = this.pidm.getText();
        final String host = this.devServer.isSelected() ? DEV_HOST : PROD_HOST;
        final String httpsURL = host + "/welcome/ramready.svc/CheckMathPlacement/" + pidmStr;

        try {
            final URL myUrl = new URL(httpsURL);
            final HttpsURLConnection conn = (HttpsURLConnection) myUrl.openConnection();
            conn.setRequestProperty("X-CSUMATH-RamWeb", "93u54ki3bngtowIE");
            final InputStream is = conn.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader br = new BufferedReader(isr);

            final HtmlBuilder reply = new HtmlBuilder(200);
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                reply.addln(inputLine);
            }

            br.close();

            this.result.setText(reply.toString());
        } catch (final IOException e) {
            Log.warning(e);
        }
    }

    public static void main(final String... args) {

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new RamReadyMock());
    }
}
