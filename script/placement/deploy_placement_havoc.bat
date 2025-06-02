@ECHO OFF

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET WARS=%WORKING%\out\wars
SET SCP=\bin\winscp /console
SET HOST=online@havoc

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying 'PLACEMENT' to Tomcat server on HAVOC
ECHO =
REM ---------------------------------------------------------------------------

CD %WARS%
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\placement\deploy_placement_script.txt

ECHO.
PAUSE
