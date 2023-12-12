package dev.mathops.db.old.rawlogic;

/**
 * Utilities used by raw logic classes.
 */
enum RawLogicUtilities {
    ;

    /**
     * Validates a test student ID that begins with "99PL". There are only certain combinations of remaining digits that
     * constitute a valid ID.
     *
     * @param studentId the student ID
     * @return {@code true} if the ID is valid; {@code false} if not
     */
    static boolean validate99PLStudentId(final CharSequence studentId) {

        final char c5 = studentId.charAt(4);
        final char c6 = studentId.charAt(5);
        final char c7 = studentId.charAt(6);
        final char c9 = studentId.charAt(8);

        boolean valid = false;

        if (c5 == '0') {
            valid = c6 == '0' && c7 == '0' && c9 == '0';
        } else if (c5 == '3') {
            valid = c6 >= '0' && c6 <= '8' && c7 == '0';
        } else if (c5 >= '1' && c5 <= '5') {
            if (c6 == '0' || c6 == '1') {
                valid = c7 == '0';
            } else if (c6 == '2') {
                valid = c7 == '0' || c7 == '2';
            } else if (c6 == '3') {
                valid = c7 == '0' || c7 == '2' || c7 == '3';
            } else if (c6 == '4') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4';
            } else if (c6 == '5') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '5';
            } else if (c6 == '6') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4' || c7 == '5' || c7 == '6';
            } else if (c6 == '7') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '5' || c7 == '7';
            } else if (c6 == '8') {
                valid = c7 == '0' || c7 == '2' || c7 == '3' || c7 == '4' || c7 == '5' || c7 == '6' || c7 == '7'
                        || c7 == '8';
            }
        }

        if (valid) {
            final char c8 = studentId.charAt(7);
            valid = c8 >= '0' && c8 <= '9' || c8 >= 'A' && c8 <= 'R';

            if (c6 == '0') {
                if (c9 != '0' && c9 != '1') {
                    valid = false;
                }
            } else if (c6 == '1') {
                if (c9 != '0' && c9 != '2') {
                    valid = false;
                }
            } else if (c6 == '2') {
                if (c9 != '0' && c9 != '3') {
                    valid = false;
                }
            } else if (c6 == '3' || c6 == '5' || c6 == '7') {
                if (c9 != '0' && c9 != '4') {
                    valid = false;
                }
            } else if (c6 == '4') {
                if (c9 != '0' && c9 != '5') {
                    valid = false;
                }
            } else if (c6 == '6') {
                if (c9 != '0' && c9 != '6') {
                    valid = false;
                }
            } else if (c9 != '0') {
                valid = false;
            }
        }

        return valid;
    }
}
