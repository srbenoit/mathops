package dev.mathops.session.txn.messages;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * A reply to a placement status request.
 */
public final class PlacementStatusReply extends AbstractReplyBase {

    /** The list of courses that have a placement status. */
    public String[] courses;

    /** The placement status for those courses. */
    public char[] status;

    /**
     * Constructs a new {@code PlacementStatusReply}.
     */
    public PlacementStatusReply() {

        super();
    }

    /**
     * Constructs a new {@code PlacementStatusReply}, initializing with data from an XML stream.
     *
     * @param xml the XML stream from which to initialize data
     * @throws IllegalArgumentException if the XML stream is not valid
     */
    PlacementStatusReply(final char[] xml) throws IllegalArgumentException {

        super();

        int index = 0;

        final String message = extractMessage(xml, xmlTag());

        this.error = extractField(message, "error");

        final String sub = extractField(message, "status");
        final String[] list1 = extractFieldList(sub, "placed");
        final String[] list2 = extractFieldList(sub, "credit");
        final String[] list3 = extractFieldList(sub, "elm");

        final int len1 = (list1 != null) ? list1.length : 0;
        final int len2 = (list2 != null) ? list2.length : 0;
        final int len3 = (list3 != null) ? list3.length : 0;

        this.courses = new String[len1 + len2 + len3];
        this.status = new char[this.courses.length];

        if (list1 != null) {

            for (int i = 0; i < len1; i++) {
                this.courses[index] = list1[i];
                this.status[index] = 'P';
                index++;
            }
        }

        if (list2 != null) {

            for (int inx = 0; inx < len2; inx++) {
                this.courses[index] = list2[inx];
                this.status[index] = 'C';
                index++;
            }
        }

        if (list3 != null) {

            for (int i = 0; i < len3; i++) {
                this.courses[index] = list3[i];
                this.status[index] = 'E';
                index++;
            }
        }
    }

    /**
     * Gets the unique XML tag of all messages of this class, to identify such a message in an XML stream.
     *
     * @return the XML tag
     */
    static String xmlTag() {

        return "placement-status-reply";
    }

    /**
     * Generates the XML representation of the message.
     *
     * @return the XML representation
     */
    @Override
    public String toXml() {

        final HtmlBuilder builder = new HtmlBuilder(512);

        builder.addln("<placement-status-reply>");
        printError(builder);

        builder.addln("<status>");

        if (this.courses != null && this.status != null) {

            final int numCourses = this.courses.length;
            final int numStatus = this.status.length;

            for (int i = 0; (i < numCourses) && (i < numStatus); i++) {

                switch (this.status[i]) {

                    case 'P':
                    case 'p':
                        builder.addln("<placed>", this.courses[i], "</placed>");
                        break;

                    case 'C':
                    case 'c':
                        builder.addln("<credit>", this.courses[i], "</credit>");
                        break;

                    case 'E':
                    case 'e':
                        builder.addln("<elm>", this.courses[i], "</elm>");
                        break;

                    default:
                        break;
                }
            }
        }

        builder.addln("</status>");

        builder.addln("</placement-status-reply>");

        return builder.toString();
    }
}
