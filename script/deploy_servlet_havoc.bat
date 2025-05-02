@ECHO OFF

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET JARS=%WORKING%\jars
SET SCP=\bin\winscp /console
SET HOST=online@havoc

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying to Tomcat server on HAVOC
ECHO =
REM ---------------------------------------------------------------------------

CD %JARS%
DEL log.log
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\deploy_servlet_script.txt /log=log.log

ECHO.
PAUSE
