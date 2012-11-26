Willow - A web browser.

Author: John Smith
send2jsmith@gmail.com


Build Pre-requisites
--------------------
JDK 1.7.0_09+


Build Instructions - Maven
--------------------------
run a maven build on the project root directory (contains a pom.xml file).
> mvn package

run the browser
> %JDK_HOME%\bin\java -jar target\willow-0.1-jar-with-dependencies.jar


Build Instructions - Windows Command Line
-----------------------------------------
If developing using a command line build =>
set the environment variable JDK_HOME to the location of your JDK install, for example:
> set JDK_HOME=C:\Program Files (x86)\Java\jdk1.7.0_09

set the environment variable JAVAFX_SDK_HOME to the location of your JavaFX SDK install, for example:
> set JAVAFX_SDK_HOME=C:\Program Files (x86)\Oracle\JavaFX 2.2 SDK

compile and package the application
> package.bat

run the browser
> "%JDK_HOME%\bin\java" -jar dist\willow.jar
