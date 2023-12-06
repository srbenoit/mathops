@ECHO OFF

SET FQDN=jwabbit

SET WORKING=C:\Users\benoit\dev\IDEA\mathops
SET BIN=%WORKING%\out\production\mathcontainers
SET TEMP=C:\temp\jwabbittemp
SET JAR=jar cfm
SET TARGET=%WORKING%\jars

REM ---------------------------------------------------------------------------
REM Clean up leftover temp directory
REM ---------------------------------------------------------------------------
IF EXIST %TEMP% RD %TEMP% /S /Q
:WHILE1
IF EXIST %TEMP% (
  TIMEOUT 1
  RMDIR %TEMP% /S /Q
  GOTO :WHILE1
)
MD %TEMP%
CD %WORKING%
COPY script\jwabbit-manifest-additions.txt %TEMP%\manifest-additions.txt

REM ---------------------------------------------------------------------------
ECHO.
ECHO == Building JWabbit Program Library
REM ---------------------------------------------------------------------------

IF EXIST %TARGET%\jwabbit.jar DEL %TARGET%\jwabbit.jar
CD %BIN%
XCOPY /Q /E *.*  %TEMP%\.

CD %TEMP%
%JAR% %TARGET%\jwabbit.jar manifest-additions.txt jwabbit

CD %WORKING%
RD %TEMP% /S /Q
:WHILE2
IF EXIST %TEMP% (
  TIMEOUT 1
  RMDIR %TEMP% /S /Q
  GOTO :WHILE2
)

ECHO.
PAUSE
