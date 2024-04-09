package dev.mathops.app;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogEntry;
import dev.mathops.commons.log.LogWriter;
import dev.mathops.commons.log.LoggingSubsystem;
import dev.mathops.db.Contexts;
import dev.mathops.session.txn.BlsWebServiceClient;
import dev.mathops.session.txn.IWebServiceClient;
import dev.mathops.session.txn.messages.AbstractMessageBase;
import dev.mathops.session.txn.messages.AbstractReplyBase;
import dev.mathops.session.txn.messages.AbstractRequestBase;
import dev.mathops.session.txn.messages.ExceptionSubmissionRequest;
import dev.mathops.session.txn.messages.MachineSetupReply;
import dev.mathops.session.txn.messages.MachineSetupRequest;
import dev.mathops.session.txn.messages.MessageFactory;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.TreeMap;

/**
 * A base class for session-based client applications, that takes care of testing for the presence of a certificate,
 * requesting one if not present, and connecting with the certificate to the server.
 */
public class ClientBase {

    /** The default host to which to attach. */
    public static final String DEFAULT_HOST = Contexts.TESTING_HOST;

    /** The default port to which to attach. */
    public static final int DEFAULT_PORT = 443;

    /** A status code indicating successful completion. */
    public static final int SUCCESS = 0;

    /** A status code indicating a failed operation. */
    public static final int FAILURE = -1;

    /** A status code indicating a failure to send data to the server. */
    public static final int CANT_SEND = 101;

    /** A status code indicating the server sent an unexpected response. */
    public static final int UNEXPECTED_REPLY = 102;

    /** A status code indicating the system is being maintained. */
    private static final int SYS_MAINT = 1;

    /** The filename of the machine ID file. */
    private static final String ID_FILE = ".CIL-ID";

    /** Flag indicating the application is running on the public Internet. */
    private boolean publicInternet;

    /** This machine's unique ID as assigned by the server. */
    private String machineId;

    /** The client connection to the server. */
    private final IWebServiceClient serverConnection;

    /** The home directory where we can read/write configuration files. */
    private final File homeDir;

    /** The TCP port of the secure web service. */
    private final int port;

    /**
     * Constructs a new {@code ClientBase}.
     *
     * @param theScheme    the scheme to use to communicate with the server
     * @param theServer    the server host address
     * @param thePort      the server port number
     * @param theSessionId the session ID to use to communicate with the server
     * @throws UnknownHostException if the hostname could not be resolved into an IP address
     */
    public ClientBase(final String theScheme, final String theServer, final int thePort,
                      final String theSessionId) throws UnknownHostException {

        // this.appName = theAppName;
        this.publicInternet = true;

        this.port = thePort;

        this.serverConnection = new BlsWebServiceClient(theScheme, theServer, thePort, theSessionId);

        final String userHome = System.getProperty("user.home");
        this.homeDir = new File(userHome);

        LoggingSubsystem.getSettings().setLogToFiles(false);
        LoggingSubsystem.getSettings().setLogToConsole(true);
        Log.getWriter().startList(1000);

    }

    /**
     * Gets the server port number.
     *
     * @return the port number
     */
    public int getPort() {

        return this.port;
    }

    /**
     * Creates the secure connection to the server. This may involve asking the user to fill in the request form and
     * submitting it to the server over the insecure network link.
     *
     * @return the result of the operation: {@code SUCCESS} if the connection was established,
     *         {@code SYSTEM_MAINTENANCE} if the server reports that the system is temporarily not accepting
     *         connections, {@code NOT_AUTHORIZED} if this computer is not authorized to connect to the server
     */
    protected final int connectToServer() {

        final int rc;

        if (this.serverConnection.init()) {
            if (this.publicInternet || loadMachineId()) {
                rc = SUCCESS;
            } else {
                rc = registerMachine(null);
            }
        } else {
            Log.warning("Unable to connect to the server.");
            disconnectFromServer();
            rc = FAILURE;
        }

        return rc;
    }

    /**
     * Closes the socket connection to the server.
     */
    protected final void disconnectFromServer() {

        this.serverConnection.close();
    }

    /**
     * Gets the connection to the server.
     *
     * @return the server connection
     */
    protected final IWebServiceClient getServerConnection() {

        return this.serverConnection;
    }

    /**
     * Sets the flag that indicates the connection is over the public Internet.
     *
     * @param isPublicInternet true if on the public Internet
     */
    protected final void setPublicInternet(final boolean isPublicInternet) {

        this.publicInternet = isPublicInternet;
    }

    /**
     * Gets the machine ID.
     *
     * @return the machine ID
     */
    protected final String getMachineId() {

        return this.machineId;
    }

    /**
     * Determines whether this machine has a valid machine ID, and if so, loads it.
     *
     * @return {@code true} if a machine ID was loaded, {@code false} on any error
     */
    private boolean loadMachineId() {

        final File f = new File(this.homeDir, ID_FILE);
        boolean ok;

        if (f.exists()) {
            if (f.length() == 40L) {
                final byte[] data = new byte[40];

                try (final FileInputStream fis = new FileInputStream(f)) {
                    ok = fis.read(data) == 40;

                    if (ok) {
                        // Verify that all bytes are in valid range
                        for (int i = 0; i < 40; i++) {

                            if ((int) data[i] < 'A' || (int) data[i] > 'Z') {
                                Log.warning("Invalid character in machine ID file.");
                                ok = false;

                                break;
                            }
                        }

                        if (ok) {
                            this.machineId = new String(data, StandardCharsets.UTF_8);
                        }
                    } else {
                        Log.warning("Unable to read machine ID file");
                    }
                } catch (final IOException ex) {
                    Log.warning(ex);
                    ok = false;
                }
            } else {
                Log.warning("Invalid Machine ID file found");
                ok = false;
            }
        } else {
            Log.warning("Machine ID file not found");
            ok = false;
        }

        return ok;
    }

    /**
     * Presents the machine authorization request form, accept the response.
     *
     * @param dialogParent a frame to contain a registration information dialog
     * @return the status of the request/response (if {@code SUCCESS}, a secure connection can now be made)
     */
    private int registerMachine(final JFrame dialogParent) {

        // Make sure we're in a clean state.
        deleteMachineId();

        // Build the machine setup request to send to the server.
        final ShowRegistrationPanel opener = new ShowRegistrationPanel(dialogParent);

        try {
            SwingUtilities.invokeAndWait(opener);
        } catch (final Exception ex) {
            Log.warning(ex);
        }

        final ClientPCRegisterPanel panel = opener.getPanel();
        panel.waitForButton();
        final int rc;

        final String action = panel.getAction();
        if ("Cancel".equals(action) || !panel.isVisible()) {
            rc = FAILURE;
        } else {
            final int testCenterId;
            final String testingCenter = panel.getTestingCenter();
            if ("Precalculus Center".equals(testingCenter)) {
                testCenterId = 1;
            } else if ("Development Center".equals(testingCenter)) {
                testCenterId = 3;
            } else {
                testCenterId = MachineSetupRequest.PUBLIC_INTERNET;
            }

            final String description = panel.getDescription();
            final String stationNum = panel.getStationNumber();

            final Runnable closer = new CloseRegistrationPanel(dialogParent, panel);

            try {
                SwingUtilities.invokeAndWait(closer);
            } catch (final Exception ex) {
                Log.warning(ex);
            }

            rc = doRegisterMachine(testCenterId, description, stationNum);
        }

        return rc;
    }

    /**
     * Deletes the machine ID.
     */
    private void deleteMachineId() {

        final File file = new File(this.homeDir, ID_FILE);

        if (file.exists() && !file.delete()) {
            Log.warning("Failed to delete ", ID_FILE);
        }
    }

    /**
     * Performs the client machine registration process based on the values entered into the registration panel.
     *
     * @param testCenterId the ID of the testing center where the machine resides
     * @param description  the machine description
     * @param stationNum   the station number
     * @return the status of the request/response (if {@code SUCCESS}, a secure connection can now be made)
     */
    private int doRegisterMachine(final int testCenterId, final String description, final String stationNum) {

        // Send the machine setup request to the server.
        final MachineSetupRequest req = new MachineSetupRequest(testCenterId, stationNum, description, new TreeMap<>());
        final Properties props = System.getProperties();

        for (final String key : props.stringPropertyNames()) {
            final String property = props.getProperty(key);
            req.systemProperties.put(key, property);
        }

        int rc;
        final String xml = req.toXml();
        if (this.serverConnection.writeObject(xml)) {
            final char[] obj = this.serverConnection.readObject("MachineSetupReply");

            if (obj == null) {
                Log.warning("No reply to machine registration request.");
                rc = FAILURE;
            } else {
                try {
                    final MachineSetupReply reply = new MachineSetupReply(obj);

                    switch (reply.resultCode) {

                        case MachineSetupReply.SUCCESS:
                            rc = writeMachineId(reply.machineId);
                            break;

                        case MachineSetupReply.SYSTEM_MAINTENANCE:
                            Log.warning("The system is undergoing maintenance.  Try again later.");
                            rc = SYS_MAINT;
                            break;

                        case MachineSetupReply.FAILURE:
                            Log.warning("This machine could not be registered at this time.  Try again later.");
                            deleteMachineId();
                            rc = FAILURE;
                            break;

                        default:
                            rc = FAILURE;
                            break;
                    }
                } catch (final IllegalArgumentException e) {
                    Log.warning("Invalid machine auth reply XML");
                    rc = FAILURE;
                }
            }
        } else {
            Log.warning("Failed to send registration request to server");
            rc = FAILURE;
        }

        return rc;
    }

    /**
     * Stores the server's certificate in this client's trust store.
     *
     * @param theMachineId the machine ID
     * @return {@code SUCCESS} if stored successfully, {@code FAILURE} otherwise.
     */
    private int writeMachineId(final String theMachineId) {

        int rc;

        try (final FileOutputStream fos = new FileOutputStream(new File(this.homeDir, ID_FILE))) {
            final byte[] bytes = theMachineId.getBytes(StandardCharsets.UTF_8);
            fos.write(bytes);
            rc = SUCCESS;
        } catch (final IOException ex) {
            Log.warning(ex);
            rc = FAILURE;
        }

        return rc;
    }

    /**
     * Connects to the server, secure the link, send a request, receive a reply, close the connection, and return the
     * reply to the caller.
     *
     * @param request   the request to send to the server
     * @param name      the name of the type of transaction being performed
     * @param leaveOpen {@code true} to leave the connection open after the exchange; {@code false} to close the
     *                  connection
     * @return the server's reply, or {@code null} on any error
     */
    protected AbstractReplyBase doExchange(final AbstractRequestBase request, final String name,
                                           final boolean leaveOpen) {

        // Open the connection if needed
        final boolean open;

        if (this.serverConnection.isOpen() || (connectToServer() == SUCCESS)) {
            open = true;
        } else {
            disconnectFromServer();
            open = connectToServer() == SUCCESS;
        }

        final AbstractReplyBase reply;
        if (open) {
            reply = exchangeMessages(name, request);

            if (!leaveOpen) {
                disconnectFromServer();
            }
        } else {
            Log.warning("Unable to open connection to server.");
            reply = null;
        }

        return reply;
    }

    /**
     * Send a request to the server and await the reply.
     *
     * @param name    the name of the transaction
     * @param request the request
     * @return the reply; {@code null} on failure
     */
    private AbstractReplyBase exchangeMessages(final String name, final AbstractRequestBase request) {

        AbstractReplyBase reply = null;

        // Perform the exchange of messages
        final String requestXml = request.toXml();
        if (this.serverConnection.writeObject(requestXml)) {
            final char[] xml = this.serverConnection.readObject(name + " reply");

            if (xml == null) {
                Log.warning("Received message is null.");
            } else {
                final AbstractMessageBase msg = MessageFactory.parseMessage(xml);

                if (msg instanceof AbstractReplyBase) {
                    reply = (AbstractReplyBase) msg;
                } else {
                    Log.warning("Received message is not a reply (", msg, ").");
                }
            }
        }

        // If a retry was indicated, restart the connection and retry.
        if (reply == null) {
            disconnectFromServer();

            if (connectToServer() == SUCCESS && this.serverConnection.writeObject(requestXml)) {
                final char[] xml = this.serverConnection.readObject(name + " reply");

                if (xml != null) {
                    final AbstractMessageBase msg = MessageFactory.parseMessage(xml);

                    if (msg instanceof AbstractReplyBase) {
                        reply = (AbstractReplyBase) msg;
                    } else {
                        Log.warning("Received message is not a reply (", msg, ").");
                    }
                }
            }
        }

        return reply;
    }

    /**
     * Transmits all stored errors to the server and clears the stored errors list.
     */
    public final void transmitErrors() {

        final LogWriter writer = Log.getWriter();
        writer.stopList();

        final int count = writer.getNumInList();

        if (count > 0) {
            final HtmlBuilder allError = new HtmlBuilder(count * 100);
            for (int i = 0; i < count; i++) {
                final LogEntry msg = writer.getListMessage(i);
                allError.addln(msg.message);
            }

            final String errorStr = allError.toString();
            final ExceptionSubmissionRequest req = new ExceptionSubmissionRequest(null, errorStr, null);

            doExchange(req, "Exception submission", false);
        }

        writer.clearList();
    }
}

/**
 * Builds and displays the registration panel. It is intended to be executed in the AWT event thread.
 */
final class ShowRegistrationPanel implements Runnable {

    /** the parent container to which to add the dialog. */
    private final JFrame dialogParent;

    /** the generated registration dialog panel. */
    private ClientPCRegisterPanel panel = null;

    /**
     * Constructs a new {@code ShowRegistrationPanel}.
     *
     * @param theDialogParent the parent container to which to add the dialog
     */
    ShowRegistrationPanel(final JFrame theDialogParent) {

        this.dialogParent = theDialogParent;
    }

    /**
     * Gets the generated registration panel.
     *
     * @return the generated panel
     */
    public ClientPCRegisterPanel getPanel() {

        return this.panel;
    }

    /**
     * Displays the registration dialog in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Gather testing center information
        if (this.dialogParent == null) {
            final JFrame frame = new JFrame("Register this station");
            this.panel = new ClientPCRegisterPanel();
            frame.setContentPane(this.panel);
            frame.pack();

            final Dimension size = frame.getSize();
            final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
            frame.setVisible(true);
        } else {
            final Dimension screen = this.dialogParent.getSize();
            final Container content = this.dialogParent.getContentPane();

            if (content instanceof JDesktopPane) {
                final JInternalFrame iFrame = new JInternalFrame("Register this station");
                this.panel = new ClientPCRegisterPanel();
                iFrame.setContentPane(this.panel);
                iFrame.pack();
                content.add(iFrame);
                final Dimension size = iFrame.getSize();
                iFrame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
                iFrame.setVisible(true);
            } else {
                final JPanel temp = new JPanel(null);
                this.dialogParent.setContentPane(temp);
                this.panel = new ClientPCRegisterPanel();
                temp.add(this.panel);
                final Dimension size = this.panel.getSize();
                this.panel.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
            }
        }
    }
}

/**
 * Hides and destroys the registration panel. It is intended to be executed in the AWT event thread.
 */
final class CloseRegistrationPanel implements Runnable {

    /** the parent container to which the dialog was added. */
    private final JFrame dialogParent;

    /** the registration dialog panel to close. */
    private final ClientPCRegisterPanel panel;

    /**
     * Constructs a new {@code CloseRegistrationPanel}.
     *
     * @param theDialogParent the parent container to which to add the dialog
     * @param thePanel        the registration dialog panel to close
     */
    CloseRegistrationPanel(final JFrame theDialogParent, final ClientPCRegisterPanel thePanel) {

        this.dialogParent = theDialogParent;
        this.panel = thePanel;
    }

    /**
     * Displays the registration dialog in the AWT event thread.
     */
    @Override
    public void run() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.panel.setVisible(false);

        if (this.dialogParent == null) {
            // A frame was generated - the panel will be the content pane of the frame.
            Container parent = this.panel.getParent();
            parent.remove(this.panel);

            while (parent != null) {
                parent.setVisible(false);

                if (parent instanceof final JFrame frm) {
                    frm.dispose();
                    break;
                }

                parent = parent.getParent();
            }
        } else {

            // We want to leave the parent alone, just remove the panel
            final Container content = this.dialogParent.getContentPane();

            if (content instanceof JDesktopPane) {

                // "parent" should be the constructed JInternalFrame's content pane.
                Container parent = this.panel.getParent();
                parent.remove(this.panel);

                if (!(parent instanceof JInternalFrame)) {
                    parent = parent.getParent();

                    if (!(parent instanceof JInternalFrame)) {
                        parent = parent.getParent();
                    }
                }

                if (parent instanceof JInternalFrame) {
                    parent.setVisible(false);
                    content.remove(parent);
                    ((JInternalFrame) parent).dispose();
                }
            } else {
                content.remove(this.panel);
            }
        }
    }
}
