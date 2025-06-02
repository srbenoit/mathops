@ECHO OFF

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET WARS=%WORKING%\out\wars
SET SCP=\bin\winscp /console
SET HOST=online@havoc

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying 'COURSE' to Tomcat server on HAVOC
ECHO =
REM ---------------------------------------------------------------------------

CD %WARS%
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\course\deploy_course_script.txt

ECHO.
PAUSE
