package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;

public enum ReportUtils {
    ;

    /**
     * Either pads a string with spaces or trims the string to achieve a target length.
     *
     * @param str       the string (if null, a string consisting entirely of spaces will be returned)
     * @param targetLen the target length
     * @return the trimmed or padded string (guaranteed to be the target length)
     */
    public static String padOrTrimString(final String str, final int targetLen) {

        final String result;

        if (str == null) {
            result = CoreConstants.SPC.repeat(targetLen);
        } else {
            final int len = str.length();
            if (len > targetLen) {
                result = str.substring(0, targetLen);
            } else if (len == targetLen) {
                result = str;
            } else {
                final String padding = CoreConstants.SPC.repeat(targetLen - len);
                result = str + padding;
            }
        }

        return result;
    }
}
