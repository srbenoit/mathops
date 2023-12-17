package dev.mathops.app.db.ui.configuration;

import dev.mathops.app.db.config.MutableDatabaseConfig;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.FlowLayout;

/**
 * A pane that manages a single database login, existing within a database server configuration.  A login's
 * configuration consists of an ID (unique over all defined servers), and a username/password pair.
 *
 * <p>
 * The "View" portion of this pane displays the ID, username, and password.  The "Control" portion allows these
 * attributes to be altered, and has a button to delete the login all together.  The {@code DatabaseLoginsPane} that
 * presents a list of these panes should provide a button to add a new login within a server.
 */
public final class DatabaseLoginPane extends JPanel implements IModelListener {

    /** The server ID. */
    private String serverId;

    /** The login ID. */
    private String loginId;

    /** The login ID field. */
    private JTextField idField;

    /** The username field. */
    private JTextField usernameField;

    /** The password field. */
    private JTextField passwordField;

    /**
     * Constructs a new {@code DatabaseServersPane}.
     *
     * @param theModel the model
     * @param theServerId the server ID
     * @param theLoginId the login ID
     */
    public DatabaseLoginPane(final Model theModel, final String theServerId, final String theLoginId) {

        super(new FlowLayout(FlowLayout.LEADING, 10, 3));

        this.serverId = theServerId;
        this.loginId = theLoginId;

        final JLabel lbl1 = new JLabel("Login ID:");
        add(lbl1);

        this.idField = new JTextField(10);
        add(this.idField);

        final JLabel lbl2 = new JLabel("Username");
        add(lbl2);

        this.usernameField = new JTextField(10);
        add(this.usernameField);

        final JLabel lbl3 = new JLabel("Password:");
        add(lbl3);

        this.passwordField = new JTextField(10);
        add(this.passwordField);

        final JButton delete = new JButton("Delete Login");
        add(delete);

        // Update controls with information from the model
        modelChanged(theModel);
    }

    /**
     * Called when the model has changed.
     *
     * @param updatedModel the updated model
     */
    public void modelChanged(final Model updatedModel) {

        final DatabaseConfig active = updatedModel.getActiveConfig();
        final MutableDatabaseConfig mutable = updatedModel.getMutableConfig();


    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DatabaseLoginPane{}");
    }
}
