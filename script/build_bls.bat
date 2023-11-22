@ECHO OFF

SET MATH=edu\colostate\math
SET WORKING=C:\Users\benoit\dev\IDEA\precalculus
SET BIN=%WORKING%\out\production\precalculus
SET TEMP=C:\temp\jartemp
SET JAR=jar cfm
SET TARGET=%WORKING%\jars

REM ---------------------------------------------------------------------------
REM Clean up leftover temp directory
REM ---------------------------------------------------------------------------
IF EXIST %TEMP% RMDIR %TEMP% /S /Q
:WHILE1
IF EXIST %TEMP% (
  TIMEOUT 1
  RMDIR %TEMP% /S /Q
  GOTO :WHILE1
)
MD %TEMP%
CD %WORKING%
COPY script\manifest-additions.txt %TEMP%

REM ---------------------------------------------------------------------------
ECHO.
ECHO == Building BLS Program Library
REM ---------------------------------------------------------------------------

IF EXIST %TARGET%\bls8.jar    DEL %TARGET%\bls8.jar
CD %BIN%
XCOPY /Q /E *.*  %TEMP%\.

CD %TEMP%

REM ---------------------------------------------------------------------------
ECHO Cleaning core project
REM ---------------------------------------------------------------------------

RMDIR %MATH%\core\builder\test /S /Q
RMDIR %MATH%\core\file\test /S /Q
RMDIR %MATH%\core\installation\test /S /Q
RMDIR %MATH%\core\log\test /S /Q
RMDIR %MATH%\core\parser\test /S /Q
RMDIR %MATH%\core\res\test /S /Q
RMDIR %MATH%\core\unicode\test /S /Q

REM ---------------------------------------------------------------------------
ECHO Cleaning db project
REM ---------------------------------------------------------------------------

RMDIR %MATH%\db\logic\test /S /Q
RMDIR %MATH%\db\rawlogic\test /S /Q
RMDIR %MATH%\db\rawrecord\test /S /Q
RMDIR %MATH%\db\rec\test /S /Q
RMDIR %MATH%\db\reclogic\test /S /Q

REM ---------------------------------------------------------------------------
ECHO Cleaning main project
REM ---------------------------------------------------------------------------

RMDIR %MATH%\system\finalgrading /S /Q
RMDIR %MATH%\system\web\site\admin /S /Q
RMDIR %MATH%\system\web\site\course /S /Q
RMDIR %MATH%\system\web\site\help /S /Q
RMDIR %MATH%\system\web\site\html /S /Q
RMDIR %MATH%\system\web\site\landing /S /Q
RMDIR %MATH%\system\web\site\lti /S /Q
RMDIR %MATH%\system\web\site\newprecalc /S /Q
RMDIR %MATH%\system\web\site\placement /S /Q
RMDIR %MATH%\system\web\site\proctoring /S /Q
RMDIR %MATH%\system\web\site\ramwork /S /Q
RMDIR %MATH%\system\web\site\root /S /Q
RMDIR %MATH%\system\web\site\testing /S /Q
RMDIR %MATH%\system\web\site\tutorial /S /Q
RMDIR %MATH%\system\web\site\video /S /Q
RMDIR %MATH%\system\web\skin /S /Q
RMDIR %MATH%\system\web\websocket /S /Q

REM ---------------------------------------------------------------------------
ECHO Cleaning deploy project
REM ---------------------------------------------------------------------------
RMDIR %MATH%\deploy /S /Q

REM ---------------------------------------------------------------------------
ECHO Cleaning jwabbit project
REM ---------------------------------------------------------------------------
RMDIR jwabbit /S /Q


%JAR% %TARGET%\bls8.jar manifest-additions.txt edu

CD %WORKING%
RMDIR %TEMP% /S /Q
:WHILE2
IF EXIST %TEMP% (
  TIMEOUT 1
  RMDIR %TEMP% /S /Q
  GOTO :WHILE2
)

ECHO.
PAUSE
