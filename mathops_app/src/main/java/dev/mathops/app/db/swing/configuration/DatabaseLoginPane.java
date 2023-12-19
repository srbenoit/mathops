package dev.mathops.app.db.swing.configuration;

import dev.mathops.app.db.config.MutableDatabaseConfig;
import dev.mathops.app.db.config.MutableLoginConfig;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.DatabaseConfig;
import dev.mathops.db.config.LoginConfig;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Objects;

/**
 * A pane that manages a single database login.  A login's configuration consists of a unique ID, the ID of a server,
 * and a username/password pair.
 *
 * <p>
 * The "View" portion of this pane displays the ID, the server ID, the username, and the password.  The "Control"
 * portion allows these attributes to be altered, and has a button to delete the login all together.  The
 * @code DatabaseLoginsPane} that presents a list of these panes should provide a button to add a new login within a
 * server.
 */
public final class DatabaseLoginPane extends JPanel implements IModelListener {

    /** The server ID. */
    private String serverId;

    /** The login ID. */
    private String loginId;

    /** The ID label. */
    private final JLabel idLabel;

    /** The login ID field. */
    private final JTextField idField;

    /** The username label. */
    private final JLabel usernameLabel;

    /** The username field. */
    private final JTextField usernameField;

    /** The password label. */
    private final JLabel passwordLabel;

    /** The password field. */
    private final JTextField passwordField;

    /** The enabled label color. */
    private final Color enabledLabelColor;

    /** The "delete" button. */
    private final JButton deleteButton;

    /**
     * Constructs a new {@code DatabaseLoginPane}.
     *
     * @param theModel the model
     * @param theServerId the server ID
     * @param theLoginId the login ID
     */
    public DatabaseLoginPane(final Model theModel, final String theServerId, final String theLoginId) {

        super(new FlowLayout(FlowLayout.LEADING, 10, 3));

        this.serverId = theServerId;
        this.loginId = theLoginId;

        this.idLabel = new JLabel("Login ID:");
        add(this.idLabel);

        this.idField = new JTextField(10);
        add(this.idField);

        this.usernameLabel = new JLabel("Username");
        add(this.usernameLabel);

        this.usernameField = new JTextField(10);
        add(this.usernameField);

        this.passwordLabel = new JLabel("Password:");
        add(this.passwordLabel);

        this.passwordField = new JTextField(10);
        add(this.passwordField);

        this.enabledLabelColor = this.idLabel.getForeground();

        this.deleteButton = new JButton("Delete Login");
        add(this.deleteButton);

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

        final LoginConfig activeLogin = active.getLogin(this.loginId);
        final MutableLoginConfig mutableLogin = mutable.getLogin(this.loginId);

        if (activeLogin == null) {
            if (mutableLogin == null) {
                // Neither exists - this record must have been created and deleted without a "commit".
                disablePane();
            } else {
                // Mutable exists, active does not exist: this is a new record that has been created but not committed
                enablePane(mutableLogin);
            }
        } else if (mutableLogin == null) {
            // Active exists but mutable does not: this is a deletion that has not been committed
            disablePane();
        } else {
            // Both exist
            enablePane(mutableLogin);
        }
    }

    /**
     * Disables the pane.
     */
    private void disablePane() {

        this.idLabel.setForeground(Color.GRAY);
        this.usernameLabel.setForeground(Color.GRAY);
        this.passwordLabel.setForeground(Color.GRAY);
        this.idField.setEnabled(false);
        this.usernameField.setEnabled(false);
        this.passwordField.setEnabled(false);
        this.deleteButton.setEnabled(false);
    }

    /**
     * Enables the pane and ensures its field contents display the current mutable login configuration.
     *
     * @param mutableLogin the configuration whose values to display
     */
    private void enablePane(final MutableLoginConfig mutableLogin) {

        final String displayId = mutableLogin.getId();
        final String displayUsername = mutableLogin.getUser();
        final String displayPassword = mutableLogin.getPassword();

        final String currentId = this.idField.getText();
        final String currentUsername = this.usernameField.getText();
        final String currentPassword = this.passwordField.getText();

        if (!Objects.equals(currentId, displayId)) {
            this.idField.setText(displayId);
        }
        if (!Objects.equals(currentUsername, displayUsername)) {
            this.usernameField.setText(displayUsername);
        }
        if (!Objects.equals(currentPassword, displayPassword)) {
            this.passwordField.setText(displayPassword);
        }

        this.idLabel.setForeground(this.enabledLabelColor);
        this.usernameLabel.setForeground(this.enabledLabelColor);
        this.passwordLabel.setForeground(this.enabledLabelColor);
        this.idField.setEnabled(true);
        this.usernameField.setEnabled(true);
        this.passwordField.setEnabled(true);
        this.deleteButton.setEnabled(true);
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
