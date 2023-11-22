package dev.mathops.session.login;

/**
 * A field that can appear in a login form.
 */
public final class LoginFormField {

//    /** Labels for the options for a [select] field type. */
//    private final String[] optionLabels;

//    /** Values for the options for a [select] field type. */
//    private final String[] optionValues;

    /**
     * Constructs a new {@code LoginFormField}.
     *
     * @param theType the type of field
     */
    LoginFormField(final ELoginFieldType theType) {

        if (theType == ELoginFieldType.SELECT) {
            throw new IllegalArgumentException(
                    "Must provide option labels/values for select type fields");
        }

//        this.optionLabels = null;
//        this.optionValues = null;
    }

//    /**
//     * Gets labels for the options for a [select] field type.
//     *
//     * @return the option labels
//     */
//    public String[] getOptionLabels() {
//
//        return this.optionLabels == null ? null : this.optionLabels.clone();
//    }

//    /**
//     * Gets values for the options for a [select] field type.
//     *
//     * @return the option values
//     */
//    public String[] getOptionValues() {
//
//        return this.optionValues == null ? null : this.optionValues.clone();
//    }
}
