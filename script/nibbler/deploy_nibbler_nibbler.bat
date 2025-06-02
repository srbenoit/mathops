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
SET WARS=%WORKING%\out\wars
SET SCP=\bin\winscp /console
SET HOST=online@nibbler

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying 'NIBBLER' to Tomcat server on NIBBLER
ECHO =
REM ---------------------------------------------------------------------------

CD %WARS%
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\nibbler\deploy_nibbler_script.txt

ECHO.
PAUSE
