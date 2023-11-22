package dev.mathops.session.txn.messages;

import dev.mathops.core.log.Log;

/**
 * A factory class that takes an XML character array and builds the appropriate message object.
 */
public enum MessageFactory {
    ;

    /**
     * Method to convert an XML character array into a message object.
     *
     * @param xml The XML string
     * @return the constructed message, or null if the stream could not be parsed.
     */
    public static AbstractMessageBase parseMessage(final char[] xml) {

        final AbstractMessageBase msg;

        if (xml == null) {
            Log.info(Res.get(Res.NULL_XML_MSG));
            msg = null;
        } else {
            final String value = String.valueOf(xml);
            final int start = value.indexOf('<');

            if (start == -1) {
                Log.info(Res.fmt(Res.MSG_NO_START_OPEN, value));
                msg = null;
            } else {
                final int end = value.indexOf('>', start + 1);

                if (end == -1) {
                    Log.info(Res.fmt(Res.MSG_NO_START_CLOSE, value));
                    msg = null;
                } else {
                    final String tag = value.substring(start + 1, end);

                    if (tag.isEmpty()) {
                        Log.info(Res.fmt(Res.MSG_EMPTY_START_TAG, value));
                        msg = null;
                    } else {
                        msg = doParseMessage(tag, xml);
                    }
                }
            }
        }

        return msg;
    }

    /**
     * Method to convert an XML character array into a message object.
     *
     * @param tag the tag
     * @param xml The XML string
     * @return the constructed message, or null if the stream could not be parsed.
     */
    private static AbstractMessageBase doParseMessage(final String tag, final char[] xml) {

        AbstractMessageBase msg;

        try {
            if (tag.equals(EchoRequest.xmlTag())) {
                msg = new EchoRequest(xml);
            } else if (tag.equals(EchoReply.xmlTag())) {
                msg = new EchoReply(xml);
            } else if (tag.equals(ExamStartResultRequest.xmlTag())) {
                msg = new ExamStartResultRequest(xml);
            } else if (tag.equals(ExamStartResultReply.xmlTag())) {
                msg = new ExamStartResultReply(xml);
            } else if (tag.equals(ExceptionSubmissionRequest.xmlTag())) {
                msg = new ExceptionSubmissionRequest(xml);
            } else if (tag.equals(ExceptionSubmissionReply.xmlTag())) {
                msg = new ExceptionSubmissionReply(xml);
            } else if (tag.equals(GetExamRequest.xmlTag())) {
                msg = new GetExamRequest(xml);
            } else if (tag.equals(GetExamReply.xmlTag())) {
                msg = new GetExamReply(xml);
            } else if (tag.equals(GetHomeworkRequest.xmlTag())) {
                msg = new GetHomeworkRequest(xml);
            } else if (tag.equals(GetHomeworkReply.xmlTag())) {
                msg = new GetHomeworkReply(xml);
            } else if (tag.equals(GetReviewExamRequest.xmlTag())) {
                msg = new GetReviewExamRequest(xml);
            } else if (tag.equals(GetReviewExamReply.xmlTag())) {
                msg = new GetReviewExamReply(xml);
            } else if (tag.equals(MachineSetupRequest.xmlTag())) {
                msg = new MachineSetupRequest(xml);
            } else if (tag.equals(MachineSetupReply.xmlTag())) {
                msg = new MachineSetupReply(xml);
            } else if (tag.equals(PlacementStatusRequest.xmlTag())) {
                msg = new PlacementStatusRequest(xml);
            } else if (tag.equals(PlacementStatusReply.xmlTag())) {
                msg = new PlacementStatusReply(xml);
            } else if (tag.equals(SubmitHomeworkRequest.xmlTag())) {
                msg = new SubmitHomeworkRequest(xml);
            } else if (tag.equals(SubmitHomeworkReply.xmlTag())) {
                msg = new SubmitHomeworkReply(xml);
            } else if (tag.equals(SurveyStatusRequest.xmlTag())) {
                msg = new SurveyStatusRequest(xml);
            } else if (tag.equals(SurveyStatusReply.xmlTag())) {
                msg = new SurveyStatusReply(xml);
            } else if (tag.equals(SurveySubmitRequest.xmlTag())) {
                msg = new SurveySubmitRequest(xml);
            } else if (tag.equals(SurveySubmitReply.xmlTag())) {
                msg = new SurveySubmitReply(xml);
            } else if (tag.equals(TestingStationInfoRequest.xmlTag())) {
                msg = new TestingStationInfoRequest(xml);
            } else if (tag.equals(TestingStationInfoReply.xmlTag())) {
                msg = new TestingStationInfoReply(xml);
            } else if (tag.equals(TestingStationResetRequest.xmlTag())) {
                msg = new TestingStationResetRequest(xml);
            } else if (tag.equals(TestingStationResetReply.xmlTag())) {
                msg = new TestingStationResetReply(xml);
            } else if (tag.equals(TestingStationStatusRequest.xmlTag())) {
                msg = new TestingStationStatusRequest(xml);
            } else if (tag.equals(TestingStationStatusReply.xmlTag())) {
                msg = new TestingStationStatusReply(xml);
            } else if (tag.equals(UpdateExamRequest.xmlTag())) {
                msg = new UpdateExamRequest(xml);
            } else if (tag.equals(UpdateExamReply.xmlTag())) {
                msg = new UpdateExamReply(xml);
            } else if (tag.equals(GetPastExamRequest.xmlTag())) {
                msg = new GetPastExamRequest(xml);
            } else if (tag.equals(GetPastExamReply.xmlTag())) {
                msg = new GetPastExamReply(xml);
            } else {
                Log.warning(Res.fmt(Res.BAD_MSG_TAG, tag));
                msg = null;
            }
        } catch (final IllegalArgumentException ex) {
            Log.warning(Res.fmt(Res.MSG_PARSE_ERROR, new String(xml)), ex);
            msg = null;
        }

        return msg;
    }
}
