@echo off

set TMP1=%TMP%\regtmp1.txt

::Get the home directory of Java 1.7
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.7" /v JavaHome >%TMP1%
if errorlevel 1 goto NOJAVA17_64
::  note: delims is a TAB followed by a space
for /f "tokens=2* delims=	 " %%A in (%TMP1%) do set JAVA_HOME=%%B
if not errorlevel 1 goto GOTJAVA

:NOJAVA17_64
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\1.7" /v JavaHome >%TMP1%
if errorlevel 1 goto NOJAVA17
for /f "tokens=2* delims=	 " %%A in (%TMP1%) do set JAVA_HOME=%%B
if not errorlevel 1 goto GOTJAVA

:NOJAVA17
::Get the home directory of Java 1.8
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.8" /v JavaHome >%TMP1%
if errorlevel 1 goto NOJAVA18_64
for /f "tokens=2* delims=	 " %%A in (%TMP1%) do set JAVA_HOME=%%B
if not errorlevel 1 goto GOTJAVA

:NOJAVA18_64
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\1.8" /v JavaHome >%TMP1%
if errorlevel 1 goto NOJAVA
for /f "tokens=2* delims=	 " %%A in (%TMP1%) do set JAVA_HOME=%%B
if not errorlevel 1 goto GOTJAVA

:NOJAVA
echo Could not find Java 1.7 or 1.8 in registry - please ensure it is installed correctly.
goto END

:GOTJAVA
del %TMP1%

set JAVACMD="%JAVA_HOME%\bin\java"

if "%1"=="-p" goto NOPROP

::Install properties file to home directory
set HOME=%HOMEDRIVE%%HOMEPATH%
set PROPFILE="%HOME%\waters.properties"
if not exist %PROPFILE% copy waters.properties %PROPFILE%

%JAVACMD% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE -p %PROPFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto END

:NOPROP

%JAVACMD% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE %1 %2 %3 %4 %5 %6 %7 %8 %9

:END
