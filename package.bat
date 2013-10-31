rem Windows build script (requires Java JDK 8b113 or later)

rem cleanup the output directories
rd /S /Q out
rd /S /Q dist
rd /S /Q dist-web
rd /S /Q dist-signed

rem create the compile output directory
mkdir out

set JDK_HOME=C:\Program Files\Java\jdk1.8.0

rem compile the source
"%JDK_HOME%\bin\javac"^
 src\main\java\org\jewelsea\willow\*.java^
 -classpath "%JDK_HOME%\jre\lib\jfxrt.jar;lib\*"^
 -d out

rem copy the resources to the output
xcopy /S src\main\resources\* out\*

@rem delete the unnecessary css source file from the output directory.
@rem del out\org\jewelsea\willow\willow.css

@rem convert the css to a binary format
@rem commented out as this step is buggy for some JavaFX versions and it adds little value anyway
@rem "%JDK_HOME%\bin\javafxpackager"^
@rem -createbss^
@rem -classpath "%JDK_HOME%\jre\lib\jfxrt.jar"^
@rem -srcdir src\main\resources\org\jewelsea\willow^
@rem -srcfiles willow.css^
@rem -outdir out\org\jewelsea\willow^
@rem -outfile willow^
@rem -v

rem package the app as a click to run jar
"%JDK_HOME%\bin\javafxpackager"^
 -createjar^
 -appclass org.jewelsea.willow.Willow^
 -classpath lib/image4j.jar;lib/PDFRenderer-0.9.1.jar^
 -nocss2bin^
 -srcdir out^
 -outdir dist^
 -runtimeversion 2.2^
 -outfile willow.jar^
 -v

rem copy the lib files to the distribution
mkdir dist\lib
xcopy /S lib dist\lib

rem use this instead if you want a self signed app
"%JDK_HOME%\bin\javafxpackager" -signjar -outdir dist-signed -keyStore keys\willow.jks -storePass willow -alias willow -keypass willow -srcdir dist

@rem sign the app
@rem "%JDK_HOME%\bin\javafxpackager"^
@rem -signjar^
@rem -outdir dist-signed^
@rem -keyStore realkeys\jewelsea.jks^
@rem -storePass ****^
@rem -alias jewelsea^
@rem -keypass ****^
@rem -srcdir dist^
@rem -v

rem package the app as a browser embedded app.
"%JDK_HOME%\bin\javafxpackager"^
 -deploy^
 -outdir dist-web^
 -outfile Willow^
 -width 100 -height 100^
 -name "Willow Browser"^
 -title "Willow Browser"^
 -vendor "John Smith"^
 -description "A web browser"^
 -appclass org.jewelsea.willow.Willow.class^
 -srcdir dist-signed -srcfiles Willow.jar;lib\image4j.jar;lib\PDFRenderer-0.9.1.jar^
 -updatemode always^
 -allpermissions^
 -embedCertificates^
 -embedJnlp^
 -appId Willow^
 -templateInFilename src\main\assembly\WillowEmbeddedTemplate.html^
 -templateOutFilename dist-web\WillowEmbedded.html^
 -argument war^
 -v

rem package the app as an webstart app
"%JDK_HOME%\bin\javafxpackager"^
 -deploy^
 -outdir dist-web^
 -outfile Willow^
 -width 100 -height 100^
 -name "Willow Browser"^
 -title "Willow Browser"^
 -vendor "John Smith"^
 -description "A web browser"^
 -appclass org.jewelsea.willow.Willow.class^
 -srcdir dist-signed -srcfiles Willow.jar;lib\image4j.jar;lib\PDFRenderer-0.9.1.jar^
 -updatemode always^
 -allpermissions^
 -embedCertificates^
 -embedJnlp^
 -appId Willow^
 -templateInFilename src\main\assembly\WillowLauncherTemplate.html^
 -templateOutFilename dist-web\WillowLauncher.html^
 -argument war^
 -v
