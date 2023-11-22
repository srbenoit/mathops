package dev.mathops.session.login;

/**
 * Information about an authentication method.
 */
public interface IAuthenticationMethod {

    /**
     * Gets a type name (safe for use as an HTTP request parameter) for the login authentication method.
     *
     * @return the type name
     */
    String getType();

    /**
     * Gets a display name for the login authentication method.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Gets a list of the fields that the login form should display.
     *
     * @return the array of fields
     */
    LoginFormField[] getLoginFormFields();
}
