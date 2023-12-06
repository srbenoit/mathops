@ECHO OFF

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET JARS=%WORKING%\jars
SET SCP=\bin\winscp /console
SET HOST=online@havoc.math.colostate.edu

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying to Tomcat server on HAVOC
ECHO =
REM ---------------------------------------------------------------------------

CD %JARS%
DIR

%SCP% /command "open %HOST%" "lcd %JARS%" "cd /imp/online" "put -nopreservetime ROOT.war" "mv ROOT.war /opt/tomcat/webapps/ROOT.war" "exit"

ECHO.
PAUSE
