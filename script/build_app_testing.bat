@ECHO OFF

cd C:\Users\benoit\dev\IDEA\mathops
java -classpath ^
C:\Users\benoit\dev\IDEA\mathops\mathops_core\build\classes\java\main;^
C:\Users\benoit\dev\IDEA\mathops\mathops_app\build\classes\java\main ^
dev.mathops.app.deploy.TestingAppBuilder

pause