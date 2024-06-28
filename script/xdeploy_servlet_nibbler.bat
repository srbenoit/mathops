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
SET HOST=online@nibbler.math.colostate.edu

REM ---------------------------------------------------------------------------
ECHO =
ECHO = Deploying to Tomcat server on NIBBLER
ECHO =
REM ---------------------------------------------------------------------------

CD %JARS%
DIR ROOT.*

%SCP% "%HOST%" /command "lcd %JARS%" "cd /imp/online" "put -nopreservetime ROOT.war" "mv ROOT.war /opt/tomcat/webapps/ROOT.war" "exit"

ECHO.
PAUSE
