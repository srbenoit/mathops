@ECHO OFF

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET WARS=%WORKING%\out\wars
SET SCP=\bin\winscp /console
SET HOST=online@havoc

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying 'PRECALC' to Tomcat server on HAVOC
ECHO =
REM ---------------------------------------------------------------------------

CD %WARS%
DIR ROOT.*

%SCP% "%HOST%" /console /script=C:\Users\benoit\dev\IDEA\mathops\script\precalc\deploy_precalc_script.txt

ECHO.
PAUSE
