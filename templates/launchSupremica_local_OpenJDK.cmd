@echo off

if defined %JAVA_HOME% (
goto GOTJAVA
) else (
goto NOJAVA_HOME
)

:NOJAVA_HOME
echo Could not find JAVA_HOME in the environment variables - please ensure it is declared correctly.
goto END

:GOTJAVA
echo JAVA_HOME found in the environment variables: %JAVA_HOME%
set JAVACMD="%JAVA_HOME%\bin\java"

if "%1"=="-p" goto NOPROP

::Install properties file to local directory
set PROPFILE="waters.properties"
if not exist %PROPFILE% copy waters.properties %PROPFILE%

%JAVACMD% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE -p %PROPFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto END

:NOPROP

%JAVACMD% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE %1 %2 %3 %4 %5 %6 %7 %8 %9

:END
