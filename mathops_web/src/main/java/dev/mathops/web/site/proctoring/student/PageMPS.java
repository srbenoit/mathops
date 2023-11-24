package dev.mathops.web.site.proctoring.student;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The main page of the Mathematics Proctoring System student site.
 *
 * <p>
 * This page will attempt to capture video and audio streams from the webcam, and a video stream from the desktop, and
 * then will guide the user through the proctoring session, including capturing a photo, capturing an image of the
 * user's ID, presenting instructions, then presenting the exam in an iFrame that allows page navigation, and finally
 * showing "finished" page where video capture is turned off. If video capture is halted any time during the exam, the
 * exam iFrame will be hidden, and instructions for re-starting the video will be shown.
 */
enum PageMPS {
    ;

    /** The page title. */
    private static final String TITLE = "Mathematics Proctoring System";

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ProctoringSite site, final ESiteType type,
                         final ServletRequest req, final HttpServletResponse resp,
                         final ImmutableSessionInfo session) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, false);

        htm.add("<input type='hidden' id='LSID' value='", session.loginSessionId, "'/>");

        // The page content has two DIVs:

        // One shown when the page is loaded to test compatibility and feature support, and to wait
        // for webcam and screen capture to be enabled. When everything is good, it presents either
        // a "pick exam" button (if there is no active session), or a "terminate" or "rejoin" choice
        // (if there is an existing session).

        // The second has all the pages that relate to a session, starting with picking an exam, and
        // ending with the "finished" message after an assessment. If the webcam/screen capture
        // stops during the session for any reason, this DIV is hidden and the first is shown until
        // the issue is resolved.

        htm.sDiv(null, "id='pre-container'", "style='display:none'");

        // Screen 1: compatibility test
        emitScreenCompatibilityTest(htm, session);

        // Screen 2: there is an existing session - rejoin or abandon?
        emitScreenExistingSession(htm);

        htm.eDiv(); // id='pre-container'

        htm.sDiv(null, "id='main-container'", "style='display:none'");

        // Screen 3: pick exam
        emitScreenPickExam(htm, session);

        // Screen 4: Capture student photo
        emitScreenPhotoCapture(htm);

        // Screen 5: Capture ID photo
        emitScreenIDCapture(htm);

        // Screen 6: Capture environment
        emitEnvironmentCapture(htm);

        // Screen 7: Instructions
        emitScreenInstructions(htm);

        // Screen 8: Assessment
        emitScreenPlacement(htm);
        emitScreenAssessment(htm);

        htm.eDiv(); // id='main-container'

        // TODO: Other screens

        // Error message screens
        emitErrorMessage(htm);

        // Hidden elements to manage media streams. */
        emitHiddenMediaElements(htm);

        if (type == ESiteType.PROD) {
            htm.addln("<script src='mps.js'></script>");
        } else {
            htm.addln("<script src='mpsdev.js'></script>");
        }

        Page.endOrdinaryPage(cache, site, htm, false);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits &lt;video&gt; elements that will receive streams from the webcam and the desktop, and that can resize those
     * streams if needed.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitHiddenMediaElements(final HtmlBuilder htm) {

        htm.sDiv(null, "id='hidden-media'", "style='display:none;'");

        htm.add("<video id='videoWebcam' width='320' muted autoplay></video>").br();
        htm.add("<video id='videoScreen' width='960' muted autoplay></video>");

        htm.eDiv(); // id='hidden-media'
    }

    /**
     * Emits an invisible error message block that can be turned on.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitErrorMessage(final HtmlBuilder htm) {

        htm.sDiv("inset", "style='display:none;'", "id='error-messages'");

        htm.sP(null, "style='font-weight:bold;'", "id='errorHeader'").eP();
        htm.sP(null, "id='errorBodyText'").eP();

        htm.eDiv(); // id='error-messages'
    }

    /**
     * Emits content used to perform a compatibility test and to ensure we can capture video, audio, and screen, and
     * obtain those streams as encoded blobs.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    private static void emitScreenCompatibilityTest(final HtmlBuilder htm,
                                                    final ImmutableSessionInfo session) {

        htm.sDiv("inset", "id='screen-compatibility'");

        htm.sH(1).add(TITLE).eH(1);
        final String screenName = session.getEffectiveScreenName();
        htm.sDiv().add("<strong>Welcome, ", screenName, "</strong>").eDiv();

        htm.div("vgap");

        htm.sDiv("left")
                .add("<img class='hidebelow600' src='/www/images/proctoring/coffee_with_laptop.png' ",
                        "style='border:1px solid #aaa;box-shadow:1px 1px 3px #888; margin:3px 30px 10px 0;'/>")
                .eDiv(); // left

        // Please wait message, and throbber

        htm.sDiv("right-of-float");
        htm.sP().add("Testing support for required features...</strong>").eP();

        htm.sP("indent2")
                .add("<img style='width:48px;height:48px;margin-right:20px' src='/www/images/proctoring/camera-web" +
                        ".png'/>")
                .add("<img style='width:48px;height:48px;' src='/www/images/proctoring/audio-input-microphone-2.png'/>")
                .add("<img style='width:48px;height:48px;margin-left:30px;' ",
                        "src='/www/images/proctoring/network-router-wireless.png'/>")
                .eP();
        htm.eDiv(); // right-of-float
        htm.div("clear");

        htm.hr();
        htm.div("vgap");

        htm.sDiv("center", "id='start-sharing-button-div'", "style='display:none;'");
        htm.add("<button class='btn' id='start-sharing-button'>Start camera and screen-sharing</button>").br();
        htm.sP().add("<strong>Please share the ENTIRE SCREEN.</strong>").eP();
        htm.eDiv();

        // Compatibility test results

        htm.sDiv(null, "id='compatibility-test-div'", "style='display:none;'");

        htm.sH(3).add("Compatibility Test Results:").eH(3);

        htm.sTable("indent");
        htm.sTr().sTd().add("<img id='compatibilityWebSocketResult' style='width:32px;height:32px;margin-right:10px;'",
                        "src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Network Connection to Service</strong>").eH(2).eTd().eTr();

        htm.sTable("indent");
        htm.sTr().sTd().add("<img id='compatibilityVideoCapResult' style='width:32px;height:32px;margin-right:10px;'",
                        "src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Webcam Capture Support</strong>").eH(2).eTd().eTr();

        htm.sTr().sTd().add("<img id='compatibilityScreenCapResult' style='width:32px;height:32px;margin-right:10px;'",
                        "src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Screen Capture Support</strong>").eTd().eTr();

        htm.sTr().sTd().add("<img id='compatibilityRecordingResult' style='width:32px;height:32px;margin-right:10px;'",
                        "src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Media Recording Support</strong>").eTd().eTr();

        htm.sTr().sTd().add("<img id='compatibilityWebcamResult' style='width:32px;height:32px;margin-right:10px;'",
                        "src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Camera and Microphone Access</strong> &nbsp; ").eTd() //
                .sTd().add("<span id='webcamVideoDataRate'></span>").eTd().eTr();

        htm.sTr().sTd().add("<img id='compatibilityScreenSharingResult' ",
                        "style='width:32px;height:32px;margin-right:10px;' src='/www/images/proctoring/throbber.svg'/>")
                .eTd().sTd().add("<strong>Screen Sharing</strong> &nbsp; ").eTd().sTd()
                .add("<span id='screenVideoDataRate'></span>").eTd().eTr();

        htm.eTable().hr().div("vgap");
        htm.eDiv(); // compatibility-test-div

        htm.sDiv("center", "id='choose-exam-button'", "style='display:none;'");
        htm.add("<button class='btn' id='pick-exam-button'>Choose Exam...</button>");
        htm.eDiv();

        htm.eDiv(); // id='screen-compatibility'
    }

    /**
     * Emits content shown when the user has an existing session, giving them the opportunity to rejoin that session
     * or abandon it and start a new session.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenExistingSession(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-existing-session'", "style='display:none;'");

        htm.sDiv("center");
        htm.addln("<img src='/www/images/proctoring/important.png' ",
                "style='position:relative; top:10px; padding-right:8px;'/>You have a proctoring session in progress!");
        htm.eDiv(); // center

        htm.div("vgap2");

        htm.sDiv("indent");

        htm.sP().add("You can terminate your existing session (which will submit any work that was started), or you ",
                "can re-join that session in progress.").eP();

        htm.div("vgap2");

        htm.sDiv("center");
        htm.addln("<button class='btn' id='terminate-existing-session'>Terminate my old session</button>");
        htm.div("vgap");
        htm.addln("<button class='btn' id='rejoin-existing-session'>Re-join my old session</button>");
        htm.eDiv(); // center

        htm.eDiv(); // indent

        htm.eDiv(); // id='screen-existing-session'
    }

    /**
     * Emits content used to perform a compatibility test and to ensure we can capture video, audio, and screen, and
     * obtain those streams as encoded blobs.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    private static void emitScreenPickExam(final HtmlBuilder htm,
                                           final ImmutableSessionInfo session) {

        htm.sDiv("inset", "id='screen-pick-exam'", "style='display:none;'");

        htm.sH(1).add(TITLE).eH(1);
        htm.div("vgap");

        htm.sDiv("left").add("<img class='hidebelow600' src='/www/images/proctoring/pick_exam.png' ",
                        "style='border:1px solid #aaa;box-shadow:1px 1px 3px #888; margin:3px 20px 10px 0;'/>")
                .eDiv(); // left

        htm.sH(3).add("Welcome, ").sSpan("btnmsg").add(session.getEffectiveScreenName()).eSpan().add('.').eH(3);

        htm.sDiv(null, "style='padding-top:6px;'", "id='pickExamsTop'").eDiv();

        htm.div("clear").hr();

        htm.sDiv(null, "id='pickExamsList'").eDiv();

        htm.eDiv(); // id='screen-pick-exam'
    }

    /**
     * Emits content used to capture a still photo of the student.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenPhotoCapture(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-capture-photo'", "style='display:none;;max-width:800px;'");

        htm.sDiv();
        htm.addln("<img class='hidebelow600' src='/www/images/proctoring/face-smile.png' ",
                "style='position:relative; top:17px; padding-right:8px;'/>",
                "Please center your face in the oval below and capture a photo.").eP();

        htm.div("vgap");
        htm.sP().add("When you are happy with your photo, click <strong>Next</strong>.").eP();
        htm.div("vgap");
        htm.eDiv();

        htm.addln("<canvas id='photo-cap-canvas' style='display:none;'></canvas>");

        htm.sDiv(null, "id='capture'");

        htm.sDiv(null, "style='min-width:670px;'");
        htm.sDiv("camera");
        htm.sDiv("videoContainer");
        htm.sDiv("faceOverlay").eDiv();
        htm.addln("<video id='photo-webcam-video' muted autoplay>Video stream not available.</video>");
        htm.eDiv(); // videoContainer
        htm.eDiv(); // camera

        htm.sDiv("photocapture").add("<img id='photo-img'/>").eDiv();
        htm.eDiv(); // min-width

        htm.sDiv(null, "style='min-width:670px;margin-top:16px;'");

        htm.sDiv(null, "style='display:inline-block;width:320px;margin-right:20px;'");
        htm.addln("<a id='capture-photo-btn' class='btn' style='margin-left:74px;'>Take photo</a>");
        htm.eDiv();

        htm.sDiv(null, "style='display:inline-block;width:320px;'");
        htm.addln("<a id='photo-ok-btn' class='btndim' style='margin-left:116px;'>Next</a>");
        htm.eDiv();

        htm.eDiv(); // min-width

        htm.eDiv(); // capture

        htm.div("vgap3");
        htm.addln("<button onclick='startOver()' onkeydown='startOver()' class='btnmsg'>Start over</button>");

        htm.eDiv(); // id='screen-capture-photo'
    }

    /**
     * Emits content used to capture a still photo of the student's ID card.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenIDCapture(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-capture-id'", "style='display:none;max-width:800px;'");

        htm.sDiv();
        htm.addln("<img class='hidebelow600' src='/www/images/proctoring/CSU_ID.png' ",
                "style='position:relative; top:17px; padding-right:8px;'/>",
                "Please hold your RamCard so it fits within the guides below.").eP();

        htm.div("vgap");
        htm.sP().add("If your camera cannot focus at that distance, move the card back until the ",
                "CSU ID number is clear and readable.").eP();
        htm.sP().add("If you do not have a CSU RamCard, please use a government-issued ID with your photo.").eP();
        htm.sP().add("When you are happy with the image, click <strong>Next</strong>.").eP();
        htm.div("vgap");
        htm.eDiv();

        htm.addln("<canvas id='id-cap-canvas' style='display:none;'></canvas>");

        htm.sDiv(null, "id='capture'");

        htm.sDiv(null, "style='min-width:670px;'");
        htm.sDiv("camera");
        htm.sDiv("videoContainer");
        htm.sDiv("cardOverlay").eDiv();
        htm.addln("<video id='id-webcam-video' muted autoplay>Video stream not available.</video>");
        htm.eDiv(); // videoContainer
        htm.eDiv(); // camera

        htm.sDiv("photocapture").add("<img id='id-img'/>").eDiv();
        htm.eDiv(); // min-width

        htm.sDiv(null, "style='min-width:670px;margin-top:16px;'");

        htm.sDiv(null, "style='display:inline-block;width:320px;margin-right:20px;'");
        htm.addln("<a id='capture-id-btn' class='btn' style='margin-left:74px;'>Take ID photo</a>");
        htm.eDiv();

        htm.sDiv(null, "style='display:inline-block;width:320px;'");
        htm.addln("<a id='id-ok-btn' class='btndim' style='margin-left:116px;'>Next</a>");
        htm.eDiv();

        htm.eDiv(); // min-width

        htm.eDiv(); // capture

        htm.div("vgap3");
        htm.addln("<button onclick='startOver()' onkeydown='startOver()' class='btnmsg'>Start over</button>");

        htm.eDiv(); // id='screen-capture-id'
    }

    /**
     * Emits content used to capture the environment.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitEnvironmentCapture(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-environment'", "style='display:none;max-width:800px;'");

        htm.sDiv("left")
                .add("<img class='hidebelow600' src='/www/images/proctoring/workspace.jpg' ",
                        "style='border:1px solid #aaa;box-shadow:1px 1px 3px #888; ",
                        "margin:3px 30px 10px 0;'/>")
                .eDiv(); // left

        htm.sH(2).add("Environment Scan").eH(2);

        htm.sP().add("To ensure no unauthorized resources are used during the exam, please perform ",
                        "a scan of your test-taking environment.")
                .eP();
        htm.div("clear");

        htm.sDiv("center");
        htm.sDiv("camera");
        htm.sDiv("videoContainer");
        htm.addln("<video id='env-webcam-video' muted autoplay>Video stream not available.</video>");
        htm.eDiv(); // videoContainer
        htm.eDiv(); // camera
        htm.eDiv(); // center

        htm.div("vgap");

        htm.sDiv("indent");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/radar-anim.gif' ",
                        "style='width:55px;height:55px;margin-right:16px;'/>")
                .eDiv(); // left

        htm.sP().add("Please slowly scan your <b>desk surface</b> with your web camera - make sure you <b>do not ",
                        "have a cell phone or other device on your desk</b>. You should have only blank scratch ",
                        "paper, pencil or pen, and a calculator.")
                .eP();
        htm.div("vgap");
        htm.div("clear");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/radar-anim.gif' ",
                        "style='width:55px;height:55px;margin-right:16px;'/>")
                .eDiv(); // left

        htm.sP().add("In addition, please scan the <b>walls or areas that you can see</b> from where you are taking",
                        "your exam.  Make sure <b>no reference materials or notes are visible</b>.")
                .eP();
        htm.div("vgap");
        htm.div("clear");

        htm.sDiv("center");
        htm.addln("<a id='env-done-btn' class='btn' style='margin-left:116px;'>Finished</a>");
        htm.eDiv(); // center

        htm.div("vgap3");
        htm.addln("<button onclick='startOver()' onkeydown='startOver()' class='btnmsg'>Start over</button>");

        htm.eDiv(); // indent

        htm.eDiv(); // id='environment'
    }

    /**
     * Emits content used to present instructions.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenInstructions(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-instructions'", "style='display:none;max-width:800px;'");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/icon_quiet_64.png' ",
                        "class='hidebelow600' style='padding: 6px 8px 6px 0;'/>")
                .eDiv();

        htm.sP().addln("Please take your exam in a quiet location where you won't be interrupted or distracted, and ",
                        "where there are no sources of extra noise.  Please silence your phone during the exam, and " +
                                "do not ",
                        "use headphones.")
                .eP();
        htm.div("clear");
        htm.div("vgap");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/icon_calculator_64.png' ",
                        "class='hidebelow600' style='padding: 6px 8px 6px 0;'/>")
                .eDiv();

        htm.sP().add(
                "During your exam, you <b>may not</b> use notes, textbooks, or online resources or communicate with ",
                "others.  You <b>may</b> use a hand-held scientific or graphing calculator and blank paper for ",
                "scratch work.").eP();
        htm.div("clear");
        htm.div("vgap");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/icon_camera_64.png' ",
                        "class='hidebelow600' style='padding: 6px 8px 6px 0;'/>")
                .eDiv();

        htm.sP().add("Please stay in view of your camera during the exam, and try not to change the camera ",
                "orientation during the exam.  It helps to have your computer or device on a table or desk, rather ",
                "than in your lap, in bed, or on the floor.").eP();
        htm.div("clear");
        htm.div("vgap");

        htm.sDiv("left")
                .add("<img src='/www/images/proctoring/icon_disguise_64.png' ",
                        "class='hidebelow600' style='padding: 6px 8px 6px 0;'/>")
                .eDiv();

        htm.sP().add("Please do not wear sunglasses or a hat with a brim, and try to avoid ",
                "having bright lights or a bright window behind you.").eP();
        htm.div("clear");
        htm.div("vgap");

        htm.sDiv(null, "style='display:inline-block;width:320px;'");
        htm.addln("<a id='start-btn' class='btn' style='margin-left:116px;'>Begin</a>");
        htm.eDiv();

        htm.div("vgap3");
        htm.addln("<button onclick='startOver()' onkeydown='startOver()' class='btnmsg'>Start over</button>");

        htm.eDiv(); // id='screen-instructions'
    }

    /**
     * Emits content used to present a placement assessment.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenPlacement(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-placement'", "style='display:none; height:calc(100vh - 32px);'");

        htm.addln("<iframe id='placement-iframe' src='empty.html'",
                "style='width:100%;height:calc(100vh - 40px);min-height:200px;border:0px;'></iframe>");

        htm.eDiv(); // id='screen-placement'
    }

    /**
     * Emits content used to present the assessment.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitScreenAssessment(final HtmlBuilder htm) {

        htm.sDiv("inset", "id='screen-assessment'", "style='display:none; height:calc(100vh - 32px);'");

        htm.addln("<iframe id='assessment-iframe' src='empty.html'",
                "style='width:100%;height:calc(100vh - 40px);min-height:200px;border:0px;'></iframe>");

        htm.eDiv(); // id='screen-assessment'
    }
}
