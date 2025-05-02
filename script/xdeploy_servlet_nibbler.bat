@ECHO OFF

ECHO.
ECHO.
ECHO You are about to deploy to the VIDEO server!
ECHO.
ECHO.

PAUSE

ECHO.
ECHO.
ECHO **************  REALLY!?!?! ****************  
ECHO.
ECHO.

PAUSE

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET JARS=%WORKING%\jars
SET SCP=\bin\winscp /console
SET HOST=online@nibbler

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying to Tomcat server on NIBBLER
ECHO =
REM ---------------------------------------------------------------------------

CD %JARS%
DEL log.log
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\deploy_servlet_script.txt /log=log.log

ECHO.
PAUSE
