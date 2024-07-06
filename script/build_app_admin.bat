@ECHO OFF

cd C:\Users\benoit\dev\IDEA\mathops
java -classpath ^
C:\Users\benoit\dev\IDEA\mathops\jars\mathops_commons.jar;^
C:\Users\benoit\dev\IDEA\mathops\mathops_app\build\classes\java\main ^
dev.mathops.app.deploy.AdminJarBuilder

cd jars
dir ADMIN.jar

pause