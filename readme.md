Willow - A javafx based web browser.
using WebKit

Authorio who starts this project: John Smith
mailto:send2jsmith@gmail.com


Build Pre-requisites
--------------------
* JDK 1.8b113+
* JAVA_HOME
* maven


Build Instructions - Maven
--------------------------
run a maven build on the project root directory (contains a pom.xml file).
> mvn package

run the browser
> java -jar target\willow-0.2-jar-with-dependencies.jar


Build Instructions - Windows Command Line
-----------------------------------------
If developing using a command line build =>
edit package.bat and set the environment variable JDK_HOME to the location of your JDK install, for example:
> set JDK_HOME=C:\Program Files (x86)\Java\jdk1.8.0

compile and package the application
> package.bat

run the browser
> "%JDK_HOME%\bin\java" -jar dist\willow.jar
