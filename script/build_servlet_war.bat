@ECHO OFF

cd C:\Users\benoit\dev\IDEA\mathops

java -classpath C:\Users\benoit\dev\IDEA\mathops\jars\mathops_commons.jar;C:\Users\benoit\dev\IDEA\mathops\mathops_web\build\classes\java\main dev.mathops.web.deploy.ServletWarBuilder

cd jars
dir ROOT.*

pause