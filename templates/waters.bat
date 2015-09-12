@echo off

set TMP1=%TMP%\regtmp1.txt
set TMP2=%TMP%\regtmp2.txt

::Get the home directory of Java 1.7
start /w regedit /e %TMP1% "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.7"
type %TMP1% | find "JavaHome" > %TMP2%
if errorlevel 1 goto NOJAVA17_64
for /f "tokens=2 delims==" %%x in (%TMP2%) do set JAVA_HOME=%%~x
if not errorlevel 1 goto GOTJAVA

:NOJAVA17_64
start /w regedit /e %TMP1% "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\1.7"
type %TMP1% | find "JavaHome" > %TMP2%
if errorlevel 1 goto NOJAVA17
for /f "tokens=2 delims==" %%x in (%TMP2%) do set JAVA_HOME=%%~x
if not errorlevel 1 goto GOTJAVA

:NOJAVA17
::Get the home directory of Java 1.8
start /w regedit /e %TMP1% "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\1.8"
type %TMP1% | find "JavaHome" > %TMP2%
if errorlevel 1 goto NOJAVA18_64
for /f "tokens=2 delims==" %%x in (%TMP2%) do set JAVA_HOME=%%~x
if not errorlevel 1 goto GOTJAVA

:NOJAVA18_64
start /w regedit /e %TMP1% "HKEY_LOCAL_MACHINE\SOFTWARE\Wow6432Node\JavaSoft\Java Runtime Environment\1.8"
type %TMP1% | find "JavaHome" > %TMP2%
if errorlevel 1 goto NOJAVA
for /f "tokens=2 delims==" %%x in (%TMP2%) do set JAVA_HOME=%%~x
if not errorlevel 1 goto GOTJAVA

:NOJAVA
echo Could not find Java 1.7 or 1.8 in registry - please ensure it is installed correctly.
goto END

:GOTJAVA
del %TMP1%
del %TMP2%

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
