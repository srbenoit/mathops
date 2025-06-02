'use strict';

// June 28, 2024 9:00 AM

const posturl = "https://nibbler.math.colostate.edu/mps-media/upload.html";
const websocketUrl = "wss://coursedev.math.colostate.edu/ws/mps";
      
const videoWebcamConstraints = {
    audio: true,
    video: { width: 160, height: 120, frameRate: 10 }
};
const videoScreenConstraints = {
    audio: false,
    video: { width: 960, height: 540, frameRate: 10 }
};

// Login session ID provided by web page

var lsidInput;
var lsid;

// Elements on the page that will get hidden or shown based on state

var divChooseExamButton;
var divScreenExistingSession;

var divScreenPickExam;
var divScreenCapturePhoto;
var divScreenCaptureId;
var divScreenEnvironment;
var divScreenInstructions;
var divScreenPlacement;
var divScreenAssessment;
var divErrorMessages;

var divStartSharing;
var btnStartSharing;
var divCompatTest;

var btnPickExamButton;
 
// Elements whose content are updated by this script

var videoWebcam;
var videoScreen;

var pErrorHeader;
var pErrorBodyText;

var imgCompatibilityBrowserResult;
var imgCompatibilityBrowserString;
var imgCompatibilityWebSocketResult;
var imgCompatibilityVideoCapResult;
var imgCompatibilityScreenCapResult;
var imgCompatibilityRecordingResult;
var imgCompatibilityWebcamResult;
var imgCompatibilityScreenSharingResult;
var imgPhotoCapture;
var imgIdCapture;

var videoPhotoWebcamVideo;
var videoIdWebcamVideo;
var videoEnvWebcamVideo;

var canvasPhotoCapture;
var canvasIdCapture;

var placementIFrame;
var assessmentIFrame;

var spanWebcamVideoDataRate;
var spanScreenVideoDataRate;

var pPickExamsTop;
var divPickExamsList;

var divPageWrapper;
var divPageBanner;
var divPageFooter;
      
var webcamVideoRecorder;
var screenVideoRecorder;
 
var webcamStream;
var screenStream;

var webcamVideoChunks;
var screenVideoChunks;

var compatStatus;
var psid;
var stuid;
var courseid;
var examid;
var state;
var eligibleExams;

// Current screen:
// 0 = compatibility
// 1 = pick exam
// 2 = existing session
// 3 = capture photo
// 4 = capture ID
// 5 = scan environment
// 6 = instructions
// 7 = placement tool
// 8 = general assessment
// 9 = finished

var currentScreen;

// Buttons and handlers

var btnCapturePhoto;
var btnPhotoOk;
var btnCaptureId;
var btnIdOk;
var btnEnvDone;
var btnStart;
var btnTerminateExisting;
var btnRejoinExsiting;

var websocketOpen;
var websocketConnected;
var websocket;

var divPreContainer;
var divMainContainer;

function clearSessionData() {
    psid = null;
    stuid = null;
    examid = null;
    courseid = null;
    state = null;
}

function handleConnectedNoSession(message) {
    
    clearSessionData();
    
    divPickExamsList.innerHTML="";
    
    let obj = JSON.parse(message.substring(20));
    
    if (obj.hasOwnProperty('categories') && obj.categories.length) {
        pPickExamsTop.innerHTML = "<p>The exams you are eligible for are listed below.</p>"
                                + "<p>Please select the exam you would like to take...</p>";
        
        let content = "";
        
        obj.categories.forEach(element => {
        
            if (element.hasOwnProperty('title') && element.hasOwnProperty('exams') && element.exams.length) {
                content += "<div class='vgap'></div>\n";
                content += "<h2>" + element.title + "</h2>\n"; 
                
                element.exams.forEach(inner => {
                    content += "<div class='indent'>";
                    content += "<button onclick='clickPickExam(this)' onkeydown='clickPickExam(this)' class='btn' id='pick-exam-id-"
                             + inner.id + "'>" + inner.label + "</button>";
                    if (inner.hasOwnProperty('note')) {
                        content += " <span class='btnmsg' style='padding-left:20px;'>"
                                 + inner.note + "</span>";
                    }                       
                    
                    content += "</div><div class='vgap'></div>\n";
                });
            }
        });    
        
        if (!content.length) {
            content = "<p>You are not eligible for any proctored exams at this time.</p>";
        }
        divPickExamsList.innerHTML = content;
    } else {
        pPickExamsTop.innerHTML = "<p>You are not eligible for any proctored exams at this time.</p>";
    }

    hideAll();
    showPreContainer();
    
    divStartSharing.style.display = 'block';
    divCompatTest.style.display = 'none';
}

function handleTerminated(message) {
    
    clearSessionData();
    
    divPickExamsList.innerHTML="";
    
    let obj = JSON.parse(message.substring(10));
    
    if (obj.hasOwnProperty('categories') && obj.categories.length) {
        pPickExamsTop.innerHTML = "<p>The exams you are eligible for are listed below.</p>"
                                + "<p>Please select the exam you would like to take...</p>";
        
        let content = "";
        
        obj.categories.forEach(element => {
        
            if (element.hasOwnProperty('title') && element.hasOwnProperty('exams') && element.exams.length) {
                content += "<div class='vgap'/>\n";
                content += "<h2>" + element.title + "</h2>\n"; 
                
                element.exams.forEach(inner => {
                    content += "<div class='indent'>";
                    content += "<button onclick='clickPickExam(this)' onkeydown='clickPickExam(this)' class='btn' id='pick-exam-id-"
                             + inner.id + "'>" + inner.label + "</button>";
                    if (inner.hasOwnProperty('note')) {
                        content += " <span class='btnmsg' style='padding-left:20px;'>"
                                 + inner.note + "</span>";
                    }                       
                    
                    content += "</div><div class='vgap'></div>\n";
                });
            }
        });    
        
        if (!content.length) {
            content = "<p>You are not eligible for any proctored exams at this time.</p>";
        }
        divPickExamsList.innerHTML = content;
    } else {
        pPickExamsTop.innerHTML = "<p>You are not eligible for any proctored exams at this time.</p>";
    }
    
    // We should still be showing the compat test page - go to the exam selection page
    
    currentScreen = 1;
    hideAll();
    divScreenPickExam.style.display='block';
    showMainContainer();
}


function handleConnectedWithSession(message) {

    console.warn("Connected with session: " + message); 

    let obj = JSON.parse(message.substring(17));
    
    if (obj.hasOwnProperty("psid") && obj.hasOwnProperty("stuid") 
        && obj.hasOwnProperty("state")) {
        
        psid = obj.psid;
        stuid = obj.stuid;
        courseid = obj.courseid;
        examid = obj.examid;
        state = obj.state;
        
        goToSessionScreen();
    } else {
        console.warn("Invalid message");
    }
    
    hideAll();
    divChooseExamButton.style.display = 'none';
    divScreenExistingSession.style.display = 'none';
     
    showPreContainer();
    
    divStartSharing.style.display = 'block';
    divCompatTest.style.display = 'none';
}

function handleSession(message) {

    let obj = JSON.parse(message.substring(7));
    
    if (obj.hasOwnProperty("psid") && obj.hasOwnProperty("stuid") 
        && obj.hasOwnProperty("state")) {
        
        psid = obj.psid;
        stuid = obj.stuid;
        courseid = obj.courseid;
        examid = obj.examid;
        state = obj.state;
     
        goToSessionScreen();
        showMainContainer(); 
    } else {
        console.warn("Invalid message");
    }
}

function goToSessionScreen() {
    
    if ("AWAITING_STUDENT_PHOTO" == state) {
        uploadMetadata();
        goToPhotoCapture();
    } else if ("AWAITING_STUDENT_ID" == state) {
        goToIdCapture();
    } else if ("ENVIRONMENT" == state) {
         goToEnvironment();
    } else if ("SHOWING_INSTRUCTIONS" == state) {
        goToInstructions();
    } else if ("ASSESSMENT" == state) {
        if ("MPTRW" == examid) {
            // console.info("Going to placement tool");
            goToPlacementTool();
        } else {
            // console.info("Going to general exam");
            goToAssessment();
        }
    } else {
        goToFinished();
    }
}

// ==========================================================================================
// CONTAINERS
// ==========================================================================================


function showPreContainer() {
    divPreContainer.style.display='block';
    divMainContainer.style.display='none';
}

function showMainContainer() {
    divPreContainer.style.display='none';
    divMainContainer.style.display='block';
}

// ==========================================================================================
// PAGES
// ==========================================================================================


function clickPickExam(button) {
    if (button && button.hasAttributes()) {
        let selected = button.getAttribute("id");
        if (selected.startsWith("pick-exam-id-")) {
            examid = selected.substring(13);
            // console.info("Selected exam " + examid);
            websocket.send("S" + examid);
        }
    } 
}

//
// Screen 1: compatibility testing - check for availability of needed interfaces, create webcam
// streams and screen-capture streams to hidden <video> elements and check that they generate blobs
//

function compatibilityTests() {

    pErrorHeader.innerHTML = "";
    pErrorBodyText.innerHTML = "";
    divErrorMessages.style.display='none';
    
    compatStatus = 0;
    divChooseExamButton.style.display = 'none';
    divScreenExistingSession.style.display = 'none';
    
    setTimeout(hasWebSocketConnected, 5000);

    console.log("Checking system compatibility.");

    let userAgentString = navigator.userAgent;
    if (userAgentString) {
        imgCompatibilityBrowserString.innerHTML = userAgentString;
        imgCompatibilityBrowserResult.src='/www/images/proctoring/emblem-default.png';
    } else {
        imgCompatibilityBrowserResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("Unable to determine browser.");
    }
           
    const hasVideoCap = navigator.mediaDevices 
            && navigator.mediaDevices.enumerateDevices
            && navigator.mediaDevices.getUserMedia;
    if (hasVideoCap) {
        imgCompatibilityVideoCapResult.src='/www/images/proctoring/emblem-default.png';
        compatStatus += 1;
    } else {
        imgCompatibilityVideoCapResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("This browser does not support the required webcam capture API.");

        console.warn("Browser does not support video capture media devices");
    }
    
    const hasScreenCap = navigator.mediaDevices
            && navigator.mediaDevices.getDisplayMedia;
    if (hasScreenCap) {
        imgCompatibilityScreenCapResult.src='/www/images/proctoring/emblem-default.png';
        compatStatus += 2;
    } else {
        imgCompatibilityScreenCapResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("This browser does not support the required screen capture API.");

        console.warn("Browser does not support screen capture media devices");
    }
    
    const hasRecording = window.MediaRecorder;
    if (hasRecording) {
        imgCompatibilityRecordingResult.src='/www/images/proctoring/emblem-default.png';
        compatStatus += 4;
    } else {
        imgCompatibilityRecordingResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("This browser does not support the required media recording API.");

        console.warn("Browser does not support media recording");
    }
    
    navigator.mediaDevices.getUserMedia(videoWebcamConstraints)
    .then(function(stream) {
        webcamStream = stream;
        videoWebcam.srcObject = webcamStream;
        videoPhotoWebcamVideo.srcObject = webcamStream;
        videoIdWebcamVideo.srcObject = webcamStream;
        videoEnvWebcamVideo.srcObject = webcamStream;
       
        webcamStream.getVideoTracks()[0].onended = function () {
            webcamStopped();
        };
        
        videoWebcam.onloadedmetadata = function(e) {
            videoWebcam.play();
            
            if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8,vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp8,vp9,opus'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=vp8,vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp8,opus'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=vp8,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp9,opus'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264,opus')) {
                var options = {mimeType: 'video/webm;codecs=h264,opus'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=h264,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264,vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=h264,vp9,opus'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=h264,vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264')) {
                var options = {mimeType: 'video/webm;codecs=h264'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=h264");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8')) {
                var options = {mimeType: 'video/webm;codecs=vp8'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=vp8");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp9')) {
                var options = {mimeType: 'video/webm;codecs=vp9'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=vp9");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=avc1')) {
                var options = {mimeType: 'video/webm;codecs=avc1'};
                webcamVideoRecorder = new MediaRecorder(webcamStream, options);
                showCompatibilityMessage("    Webcam streaming format: video/webm;codecs=avc1");
            } else {
                webcamVideoRecorder = new MediaRecorder(webcamStream);
                showCompatibilityMessage("    Webcam streaming format: default");
            }
            
            webcamVideoRecorder.onstop = function(e) {
                var blob = new Blob(webcamVideoChunks, { 'type' : 'video/webm' });
                webcamVideoChunks = [];
                var size = blob.size;
                if (size > 0) {
                    imgCompatibilityWebcamResult.src='/www/images/proctoring/emblem-default.png';
                    spanWebcamVideoDataRate.innerHTML = "(" + Math.round(size / 2048) + " kBps data rate)";
                    compatStatus += 8;
                    if (compatStatus == 63) {
                        systemIsCompatible();
                    }
                } else {
                    showCompatibilityError("Unable to record from webcam.");
                    console.warn("Media recording stopped.");
                }
                webcamVideoRecorder = null;
            }
        
            webcamVideoRecorder.ondataavailable = function(e) {
                webcamVideoChunks.push(e.data);
            }
            
            // console.info("Starting webcam recorder");
            webcamVideoRecorder.start();
            setTimeout(stopWebcamVideoRecording, 2000);
        };
    })
    .catch(function(err) {
        imgCompatibilityWebcamResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("Unable to start webcam - refresh this page and grant "
            + "permission to use your camera and microphone.");

        console.warn("Unable to start webcam: " + err.message);

        if ("Failed to allocate videosource" == err.message) {
            showCompatibilityMessage("It appears some other application or browser is controlling the webcam");
        } else {
            showCompatibilityMessage("Error message from browser: " + err.message);
        }
        showCompatibilityMessage("<hr/>");
    });
    
    return navigator.mediaDevices.getDisplayMedia(videoScreenConstraints)
    .then(function(stream) {
        screenStream = stream;        
        videoScreen.srcObject = screenStream;
        
        screenStream.getVideoTracks()[0].onended = function () {
            screenCaptureStopped();
        };
         
        videoScreen.onloadedmetadata = function(e) {
            videoScreen.play();
            
            if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8,vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp8,vp9,opus'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=vp8,vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp8,opus'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=vp8,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=vp9,opus'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264,opus')) {
                var options = {mimeType: 'video/webm;codecs=h264,opus'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=h264,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264,vp9,opus')) {
                var options = {mimeType: 'video/webm;codecs=h264,vp9,opus'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=h264,vp9,opus");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=h264')) {
                var options = {mimeType: 'video/webm;codecs=h264'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=h264");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8')) {
                var options = {mimeType: 'video/webm;codecs=vp8'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=vp8");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=vp9')) {
                var options = {mimeType: 'video/webm;codecs=vp9'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=vp9");
            } else if (MediaRecorder.isTypeSupported('video/webm;codecs=avc1')) {
                var options = {mimeType: 'video/webm;codecs=avc1'};
                screenVideoRecorder = new MediaRecorder(screenStream, options);
                showCompatibilityMessage("    Screen capture streaming format: video/webm;codecs=avc1");
            } else {
                screenVideoRecorder = new MediaRecorder(screenStream);
                showCompatibilityMessage("    Screen capture streaming format: default");
            }
            
            screenVideoRecorder.onstop = function(e) {
                var blob = new Blob(screenVideoChunks, { 'type' : 'video/webm' });
                screenVideoChunks = [];
                var size = blob.size;
                if (size > 0) {
                    imgCompatibilityScreenSharingResult.src='/www/images/proctoring/emblem-default.png';
                    spanScreenVideoDataRate.innerHTML = "(" + Math.round(size / 2048) + " kBps data rate)";
                    compatStatus += 16;
                    if (compatStatus == 63) {
                        systemIsCompatible();
                    }
                } else {
                    showCompatibilityError("Unable to record screen-capture data.");
                    console.warn("Screen capture stopped");
                }
                screenVideoRecorder = null;
            };
        
            screenVideoRecorder.ondataavailable = function(e) {
                screenVideoChunks.push(e.data);
            };
            
            // console.info("Starting screen recorder");
            screenVideoRecorder.start();
            setTimeout(stopScreenVideoRecording, 2000);
        };
    })
    .catch(function(err) {
        imgCompatibilityScreenSharingResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("Unable to start screen capture - refresh this page and grant "
            + "permission to share your screen.");
        showCompatibilityMessage("Error message from browser: " + err.message);
        showCompatibilityMessage("<hr/>");

        console.warn("Unable to start screen capture: " + err.message);
    });
}

function systemIsCompatible() {

    console.log("System is compatible, psid is " + psid);
    
    if (psid == null) {
        divChooseExamButton.style.display = 'block';
        divScreenExistingSession.style.display = 'none';
    } else {
        divChooseExamButton.style.display = 'none';
        divScreenExistingSession.style.display = 'block';
    }
}

function hasWebSocketConnected() {

    if (websocketConnected) {
        imgCompatibilityWebSocketResult.src='/www/images/proctoring/emblem-default.png';
        compatStatus += 32;
        if (compatStatus == 63) {
            systemIsCompatible();
        }
    } else {
        imgCompatibilityWebSocketResult.src='/www/images/proctoring/emblem-important.png';
        showCompatibilityError("Unable to establish proctoring connection to server.");
    }
}

function stopWebcamVideoRecording() {
    if (webcamVideoRecorder != null) {
        if ("recording" == webcamVideoRecorder.state) {
            // console.info("Stopping webcam recorder");
            webcamVideoRecorder.stop();
        }
        webcamVideoRecorder = null;
    }
}

function stopScreenVideoRecording() { 
    if (screenVideoRecorder != null) {
        if ("recording" == screenVideoRecorder.state) {
            // console.info("Stopping screen recorder");
            screenVideoRecorder.stop();
        }
        screenVideoRecorder = null;
    }
}

function showCompatibilityError(error) {        
    pErrorHeader.innerHTML = "Messages:";
    pErrorBodyText.innerHTML += "&bullet; " + error + "<br/>";
    divErrorMessages.style.display='block';
}

function showCompatibilityMessage(msg) {
    pErrorHeader.innerHTML = "Messages:";
    pErrorBodyText.innerHTML += "&nbsp; &nbsp; " + msg + "<br/>";
    divErrorMessages.style.display='block';
}

function startOver() {
    websocket.send("X" + lsid);
}
 

//
// Screen 3: photo capture
//

function goToPhotoCapture() {

    currentScreen = 3;
    hideAll();
    divScreenCapturePhoto.style.display = 'block';
    
    btnPhotoOk.setAttribute('class', 'btndim');
    
    startCapture();
}

//
// Screen 4: ID card capture
//

function goToIdCapture() {

    currentScreen = 4;
    hideAll();
    divScreenCaptureId.style.display='block';
    
    btnIdOk.setAttribute('class', 'btndim');
    
    startCapture();
}

//
// Screen 5: Scan environment
//

function goToEnvironment() {

    currentScreen = 5;
    hideAll();
    divScreenEnvironment.style.display='block';
    
    startCapture();
}

//
// Screen 6: Instructions
//

function goToInstructions() {

    currentScreen = 6;
    hideAll();
    divScreenInstructions.style.display='block';
    
    startCapture();
}

//
// Screen 7: Placement Tool
//

function goToPlacementTool() {

    currentScreen = 7;
    hideAll();
    divScreenPlacement.style.display = 'block';
    
    if(divPageBanner) {
        divPageBanner.style.display='none';
    }
    if (divPageFooter) {
        divPageFooter.style.display='none';
    }
    
    if (divPageWrapper) {
        divPageWrapper.style.minHeight='100vw';
    } else {
        console.warn("can't find wrapper DIV");
    }
    
    placementIFrame.src = 'placement.html'; 
    
    startCapture();
}

//
// Screen 8: General assessment
//

function goToAssessment() {

    currentScreen = 8;
    hideAll();
    divScreenAssessment.style.display='block';
    
    if (divPageBanner) {
        divPageBanner.style.display='none';
    } else {
        console.warn("can't find banner DIV");
    }
    
    if (divPageFooter) {
        divPageFooter.style.display='none';
    } else {
        console.warn("can't find footer DIV");
    } 
    
    if (divPageWrapper) {
        divPageWrapper.style.minHeight='100vw';
    } else {
        console.warn("can't find wrapper DIV");
    }
    
    assessmentIFrame.src = 'exam.html'; 
    
    startCapture();
}

//
// Screen 9: finished
//

function goToFinished() {
    
    websocket.send("F");
    
    // console.info("Showing finished");
    
    currentScreen = 9;
    hideAll(); 
    divScreenAssessment.style.display='block';
    
    stopWebcamVideoRecording();
    if (webcamStream) { 
        webcamStream.getTracks().forEach(track => track.stop())
        webcamStream = null;
    }
        
    stopScreenVideoRecording();
    if (screenStream) { 
        screenStream.getTracks().forEach(track => track.stop())
        screenStream = null;
    }
}

function webcamStopped() {

    // console.info("Webcam stopped");
    
    // Trigger an upload
    if (webcamVideoRecorder != null && "recording" == webcamVideoRecorder.state) {
        webcamVideoRecorder.stop();
    }
                
    hideAll();
    showPreContainer();
    
    divStartSharing.style.display = 'block';
    divCompatTest.style.display = 'none';
}

function screenCaptureStopped() {

    // console.info("Screen capture stopped");
    
    // Trigger an upload
    if (screenVideoRecorder != null && "recording" == screenVideoRecorder.state) {
        screenVideoRecorder.stop();
    }   
            
    hideAll();
    showPreContainer();
    
    divStartSharing.style.display = 'block';
    divCompatTest.style.display = 'none';
}

function hideAll() {
    
    if (divPageBanner) {
        divPageBanner.style.display='block';
    } else {
        console.warn("can't find banner div");
    }
    if (divPageFooter) {
        divPageFooter.style.display='block';
    } else {
        console.warn("can't find footer div");
    }
    
    if (divPageWrapper) {
        divPageWrapper.style.minHeight='calc(100vh - 165px)';
    } else {
        console.warn("can't find wrapper DIV");
    }
    
    if (divErrorMessages) {
        divErrorMessages.style.display='none';
    }
    if (divScreenPickExam) {
        divScreenPickExam.style.display='none';
    }
    if (divScreenCaptureId) {
        divScreenCaptureId.style.display='none';
    }
    if (divScreenEnvironment) {
        divScreenEnvironment.style.display='none';
    }
    if (divScreenInstructions) {
        divScreenInstructions.style.display = 'none';
    }
    if (divScreenAssessment) {
        divScreenAssessment.style.display = 'none';
    }
    if (divScreenCapturePhoto) {
        divScreenCapturePhoto.style.display = 'none';
    }
}

function startCapture() {

    if (MediaRecorder.isTypeSupported('video/webm;codecs=vp8,opus')) {
        var options = {mimeType: 'video/webm;codecs=vp8,opus'};
        
        if (webcamVideoRecorder == null && webcamStream) {
            webcamVideoRecorder = new MediaRecorder(webcamStream, options);
        }
        if (screenVideoRecorder == null && screenStream) {
            screenVideoRecorder = new MediaRecorder(screenStream, options);
        }
    } else {
        if (webcamVideoRecorder == null && webcamStream) {
            webcamVideoRecorder = new MediaRecorder(webcamStream);
        }
        if (screenVideoRecorder == null && screenStream) {
            screenVideoRecorder = new MediaRecorder(screenStream);
        }
    }
    
    let started = false;
    
    if (webcamVideoRecorder) {
        webcamVideoRecorder.onstop = function(e) {
            uploadWebcam();
        }
        webcamVideoRecorder.ondataavailable = function(e) {
            // console.info("Webcam chunk " + e.data.size);
            webcamVideoChunks.push(e.data);
            uploadWebcam();
        }
        if ("recording" != webcamVideoRecorder.state) {
            webcamVideoRecorder.start(1000);
            started = true;
        }
    }
    
    if (screenVideoRecorder) {
        screenVideoRecorder.onstop = function(e) {
            uploadScreen();
        };
        screenVideoRecorder.ondataavailable = function(e) {
            // console.info("Screen chunk " + e.data.size);
            screenVideoChunks.push(e.data);
            uploadScreen();
        };
        if ("recording" != screenVideoRecorder.state) {
            screenVideoRecorder.start(1000);
            started = true;
        }
    }
    
    if (started) {
        uploadEvent("START-STREAMING");
    }
}

function takePhotoPicture() {

    let width = videoPhotoWebcamVideo.offsetWidth;
    let height = videoPhotoWebcamVideo.offsetHeight;
    let context = canvasPhotoCapture.getContext('2d');
    
    canvasPhotoCapture.width = width;
    canvasPhotoCapture.height = height;
    context.drawImage(videoWebcam, 0, 0, width, height);

    let data = canvasPhotoCapture.toDataURL('image/jpeg', 5);
    imgPhotoCapture.setAttribute('width', width);
    imgPhotoCapture.setAttribute('height', height);
    imgPhotoCapture.setAttribute('src', data); 
}

function takeIdPicture() {

    let width = videoIdWebcamVideo.offsetWidth;
    let height = videoIdWebcamVideo.offsetHeight;
    let context = canvasIdCapture.getContext('2d');
    
    canvasIdCapture.width = width;
    canvasIdCapture.height = height;
    context.drawImage(videoWebcam, 0, 0, width, height);

    let data = canvasIdCapture.toDataURL('image/jpeg', 0.5);
    imgIdCapture.setAttribute('width', width);
    imgIdCapture.setAttribute('height', height);
    imgIdCapture.setAttribute('src', data);
}

function uploadMetadata() {

    if (psid) {
        let body = { exam: examid, course: courseid };
        fetch(posturl + "?psid=" + psid + "&stuid=" + stuid + "&type=M&when=" + dateString(), {
          mode: "no-cors", credentials: "include", 
          headers: {'Content-Type': 'text/plain'},
          method: "POST", body: JSON.stringify(body)
        }); 
    } else {
        console.error('Proctoring session ID not set - cannot upload photo');
    }
}

function uploadPhoto() {
 
    if (psid) {
        canvasPhotoCapture.toBlob(function(blob) {
            fetch(posturl + "?psid=" + psid + "&stuid=" + stuid + "&type=P&when=" + dateString(), {
              mode: "no-cors", credentials: "include", 
              headers: {'Content-Type': blob.type},
              method: "POST", body: blob
            });
        }, 'image/jpeg', 0.5);
    } else {
        console.error('Proctoring session ID not set - cannot upload photo');
    }
}

function uploadId() {
 
    if (psid) {
        canvasIdCapture.toBlob(function(blob) {
            fetch(posturl + "?psid=" + psid + "&stuid=" + stuid+ "&type=I&when=" + dateString(), {
              mode: "no-cors", credentials: "include",
              headers: {'Content-Type': blob.type},
              method: "POST", body: blob
            });
        }, 'image/jpeg', 0.5);
    } else {
        console.error('Proctoring session ID not set - cannot upload photo');
    }
}

function uploadWebcam() {
 
    if (psid) {
        var blob = new Blob(webcamVideoChunks, { 'type' : 'video/webm' });
        webcamVideoChunks = [];
        
        if (blob.size > 0) {
            fetch(posturl + "?psid=" + psid + "&stuid=" + stuid + "&type=V&when=" + dateString(), {
              mode: "no-cors", credentials: "include", 
              headers: {'Content-Type': blob.type},
              headers: {'Content-Length': blob.size},
              method: "POST", body: blob
            });
        }
    } else {
        console.warn('Proctoring session ID not set - cannot upload webcam stream');
    }
}

function uploadScreen() {
 
    if (psid) {
        var blob = new Blob(screenVideoChunks, { 'type' : 'video/webm' });
        screenVideoChunks = [];
        
        if (blob.size > 0) {
            fetch(posturl + "?psid=" + psid + "&stuid=" + stuid + "&type=S&when=" + dateString(), {
              mode: "no-cors", credentials: "include", 
              headers: {'Content-Type': blob.type},
              headers: {'Content-Length': blob.size},
              method: "POST", body: blob
            });
        }
    } else {
        console.warn('Proctoring session ID not set - cannot upload screen stream');
    }
}

function uploadEvent(msg) {
 
    if (psid) {
        fetch(posturl + "?psid=" + psid + "&stuid=" + stuid+ "&type=E&when=" + dateString(), {
          mode: "no-cors", credentials: "include",
          headers: {'Content-Type': 'text/plain'},
          method: "POST", body: msg
        });
    } else if (currentScreen != 9) {
        console.error('Proctoring session ID not set - cannot upload event');
    }
}

function dateString() {

    let LEX = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        
    let d = new Date();
    let year = d.getFullYear() - 1990;
    let month = d.getMonth() + 1;
    let day = d.getDate();
    let hour = d.getHours();
    let min = d.getMinutes();
    let sec = d.getSeconds(); 
                            
    return LEX.charAt(year) + LEX.charAt(month) + LEX.charAt(day) + LEX.charAt(hour)
            + LEX.charAt(min) + LEX.charAt(sec);
}


function handleExamEnded(e) {
    // console.info("Received 'examEnded' event.");

    goToFinished();
    
    if (websocket.readyState === WebSocket.OPEN) {
        uploadEvent("EXAM-ENDED");
    }
}

function heartbeat() {
    if (websocketOpen) {
        console.log("Keepalive ping");
        websocket.send(".");
    }
}

//
// Commands executed on page load
//

function pageLoaded() {
 
    lsidInput = document.getElementById('LSID');
    lsid = lsidInput ? lsidInput.value : null;

    divChooseExamButton = document.getElementById('choose-exam-button');
    divScreenExistingSession = document.getElementById('screen-existing-session');

    divScreenPickExam = document.getElementById('screen-pick-exam');
    divScreenCapturePhoto = document.getElementById('screen-capture-photo');
    divScreenCaptureId = document.getElementById('screen-capture-id');
    divScreenEnvironment = document.getElementById('screen-environment');
    divScreenInstructions = document.getElementById('screen-instructions');
    divScreenPlacement = document.getElementById('screen-placement');
    divScreenAssessment = document.getElementById('screen-assessment');
    divErrorMessages = document.getElementById('error-messages');

    divStartSharing = document.getElementById('start-sharing-button-div');
    btnStartSharing = document.getElementById('start-sharing-button');
    divCompatTest = document.getElementById('compatibility-test-div');
     
    btnPickExamButton = document.getElementById('pick-exam-button');

    videoWebcam = document.getElementById('videoWebcam');
    videoScreen = document.getElementById('videoScreen');

    pErrorHeader = document.getElementById('errorHeader');
    pErrorBodyText = document.getElementById('errorBodyText');

    imgCompatibilityBrowserResult = document.getElementById('compatibilityBrowserResult');
    imgCompatibilityBrowserString = document.getElementById('compatibilityBrowserString');
    imgCompatibilityWebSocketResult = document.getElementById('compatibilityWebSocketResult');
    imgCompatibilityVideoCapResult = document.getElementById('compatibilityVideoCapResult');
    imgCompatibilityScreenCapResult = document.getElementById('compatibilityScreenCapResult');
    imgCompatibilityRecordingResult = document.getElementById('compatibilityRecordingResult');
    imgCompatibilityWebcamResult = document.getElementById('compatibilityWebcamResult');
    imgCompatibilityScreenSharingResult = document.getElementById('compatibilityScreenSharingResult');
    imgPhotoCapture = document.getElementById('photo-img');
    imgIdCapture = document.getElementById('id-img');

    videoPhotoWebcamVideo = document.getElementById('photo-webcam-video');
    videoIdWebcamVideo = document.getElementById('id-webcam-video');
    videoEnvWebcamVideo = document.getElementById('env-webcam-video');

    canvasPhotoCapture = document.getElementById('photo-cap-canvas');
    canvasIdCapture = document.getElementById('id-cap-canvas');

    placementIFrame = document.getElementById('placement-iframe');
    assessmentIFrame = document.getElementById('assessment-iframe');

    spanWebcamVideoDataRate = document.getElementById('webcamVideoDataRate');
    spanScreenVideoDataRate = document.getElementById('screenVideoDataRate');

    pPickExamsTop = document.getElementById('pickExamsTop');
    divPickExamsList = document.getElementById('pickExamsList');

    divPageWrapper = document.getElementById('page_wrapper');
    divPageBanner = document.getElementById('page_banner');
    divPageFooter = document.getElementById('page_footer');

    webcamVideoRecorder = null;
    screenVideoRecorder = null;
         
    webcamVideoChunks = [];
    screenVideoChunks = [];
    
    compatStatus = 0;
    eligibleExams = [];
    
    currentScreen = 0;
    
    btnCapturePhoto = document.getElementById('capture-photo-btn');
    btnPhotoOk = document.getElementById('photo-ok-btn');
    btnCaptureId = document.getElementById('capture-id-btn');
    btnIdOk = document.getElementById('id-ok-btn');
    btnEnvDone = document.getElementById('env-done-btn');
    btnStart = document.getElementById('start-btn');
    btnTerminateExisting = document.getElementById('terminate-existing-session');
    btnRejoinExsiting = document.getElementById('rejoin-existing-session');
    
    btnStartSharing.onclick = function(ev) {
        divStartSharing.style.display = 'none';
        divCompatTest.style.display = 'block';
        
        compatibilityTests();
    };
    
    btnCapturePhoto.onclick = function(ev) {
        takePhotoPicture();
        ev.preventDefault();
        btnPhotoOk.className = "btn";
    };
    
    btnPhotoOk.onclick = function() {
        if (btnPhotoOk.className == "btn") { 
            btnPhotoOk.className = "btndim";
            uploadPhoto();
            websocket.send("P");
        }
    };
    
    btnCaptureId.onclick = function(ev) {
        takeIdPicture();
        ev.preventDefault();
        btnIdOk.className = "btn";
    };
    
    btnIdOk.onclick = function() {
        if (btnIdOk.className == "btn") { 
            btnIdOk.className = "btndim";
            uploadId();
            websocket.send("I");
        }
    };
    
    btnStart.onclick = function() {
        websocket.send("A");
    };
    
    btnEnvDone.onclick = function() {
        websocket.send("E");
    };
    
    btnTerminateExisting.onclick = function() {
        websocket.send("X" + lsid);
    }
    
    btnRejoinExsiting.onclick = function() {
        websocket.send("R");
    }
    
    btnPickExamButton.onclick = function() {
    
        // console.info("Pick exam click");
    
        hideAll();
        divScreenPickExam.style.display = 'block';
        
        currentScreen = 1;
        showMainContainer();
    }
    
    
    divPreContainer = document.getElementById('pre-container');
    divMainContainer = document.getElementById('main-container');
            
    websocketOpen = false;
    websocketConnected = false;
    websocket = new WebSocket(websocketUrl);
          
    websocket.onopen = function (event) {
        console.log("*** WebSocket opened");
        websocketOpen = true;
        websocket.send("!" + lsid);
        window.setInterval(heartbeat, 55000);
    };
    
    websocket.onclose = function (event) {
        console.log("*** WebSocket closing: " + event.code + "/" + event.reason);
        websocketOpen = false;
        window.clearInterval();
    };
    
    websocket.onerror = function(event) {
        console.error("WebSocket error:", event);
        websocketOpen = false;
    };
    
    websocket.onmessage = function (event) {   
        let message = event.data;
        
        // console.log("*** WebSocket received message: " + message);
        websocketConnected = true;
        
        if (message.startsWith("CONNECTED-NO-SESSION")) {
            handleConnectedNoSession(message);
        } else if (message.startsWith("CONNECTED-SESSION")) {
            handleConnectedWithSession(message);
        } else if (message.startsWith("TERMINATED")) {
            handleTerminated(message);
        } else if (message.startsWith("SESSION")) {
            handleSession(message);
        } else if (message.startsWith("ERROR")) {
            console.warn("ERROR received: " + message);
            clearSessionData();
        } else if (message.startsWith("CLOSED")) {
            console.warn("Web socket closed");
            clearSessionData();
        } else {
            console.warn("Unexpected message");
        }
    }

    window.addEventListener("beforeunload", function (e) {
        stopWebcamVideoRecording();
        stopScreenVideoRecording();
        
        if (websocket.readyState === WebSocket.OPEN) {
            uploadEvent("PAGE-CLOSED");
        }
    });

    window.document.addEventListener('examEnded', handleExamEnded, false);

    showPreContainer();
    
    divStartSharing.style.display = 'block';
    divCompatTest.style.display = 'none';
}

window.addEventListener('load', pageLoaded, false);
