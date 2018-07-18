@ECHO OFF &SETLOCAL
setlocal enabledelayedexpansion

call :FindJava
set javaCmd=%result%
if "!javaCmd!" == "" (
  echo Could not find Java 8, 9, or 10 - please ensure it is installed correctly.
  exit 1
)

:: We have found Java at last.
:: Now check for properties file and launch Waters ...

if "%1" == "-p" (
  :: Use properties file from command line
  %javaCmd% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE %1 %2 %3 %4 %5 %6 %7 %8 %9
) else (
  :: Install properties file to home directory
  set HOME=%HOMEDRIVE%%HOMEPATH%
  set PROPFILE="!HOME!\waters.properties"
  if not exist !PROPFILE! copy waters.properties !PROPFILE!
  %javaCmd% -classpath %~dp0\Supremica.jar org.supremica.gui.ide.IDE -p %PROPFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9
)

exit


::FUNCTION
:FindJava
:: return %result% : full Java command, with possible arguments, or ""
call :FindJava8 ""
if not "!result!" == "" (
  exit /b
)
call :FindJava8 "\Wow6432Node"
if not "!result!" == "" (
  exit /b
)
call :FindJava10 ""
if not "!result!" == "" (
  exit /b
)
call :FindJava10 "\Wow6432Node"
if not "!result!" == "" (
  exit /b
)
if not "!result!" == "" (
  call :CheckJavaCommand "%JAVA_HOME%\bin\java.exe"
)
if not "!result!" == "" (
  exit /b
)
call :CheckJavaCommand java  
exit /b


::FUNCTION
:FindJava8
:: argument %~1%   : "\Wow6432Node" or ""
:: return %result% : full Java command, with possible arguments, or ""
set key=HKEY_LOCAL_MACHINE\SOFTWARE%~1%\JavaSoft
call :FindJavaInRegistryGroup "%key%\Java Runtime Environment"
if not "!result!" == "" (
  exit /b
)
call :FindJavaInRegistryGroup "%key%\Java Development Kit"
exit /b


::FUNCTION
:FindJava10
:: argument %~1%   : "\Wow6432Node" or ""
:: return %result% : full Java command, with possible arguments, or ""
set result=
set key=HKEY_LOCAL_MACHINE\SOFTWARE%~1%\JavaSoft
call :FindJavaInRegistryGroup "%key%\JRE"
if not "!result!" == "" (
  exit /b
)
call :FindJavaInRegistryGroup "%key%\JDK"
if not "!result!" == "" (
  exit /b
)
for %%s in (JRE JDK) do (
  set subkey=%key%\%%s
  for /f "tokens=3* usebackq" %%v in (`reg query "!subkey!" 2^>nul`) do (
    set vv=%%v 
    if "!vv:~0,2!"=="9." (
      goto :FindJava10Found
    ) else if "!vv:~0,3!"=="10." (
      goto :FindJava10Found
    )
  )
)
exit /b
:FindJava10Found
set key=%subkey%\%vv%
set valueName=JavaHome
set javaHome=
for /F "usebackq skip=2 tokens=1,2*" %%A in (`REG QUERY %key% /v %valueName% 2^>nul`) do (
  set javaHome=%%C
)
if not "%javaHome%"=="" (
  set result="%javaHome%\bin\java" --add-modules java.xml.bind
)
exit /b


::FUNCTION
:FindJavaInRegistryGroup
:: argument %~1%   : registry group prefix
:: return %result% : full Java command, with possible arguments, or ""
set result=
set keyName="%~1%"
set valueName=CurrentVersion
for /F "usebackq skip=2 tokens=1-3" %%A in (`REG QUERY %keyName% /v %valueName% 2^>nul`) do (
  set valueValue=%%C
)
if "%valueValue%"=="1.8" (
  set v=8
) else if "%valueValue:~0,4%"=="1.8." (
  set v=8
) else if "%valueValue:~0,2%"=="9." (
  set v=9
) else if "%valueValue:~0,3%"=="10." (
  set v=10
) else (
  exit /b
)
set keyName="%keyName:~1,-1%\%valueValue%"
set valueName=JavaHome
set javaHome=
for /F "usebackq skip=2 tokens=1,2*" %%A in (`REG QUERY %keyName% /v %valueName% 2^>nul`) do (
  set javaHome=%%C
)
if not "%javaHome%"=="" (
  if %v% geq 9 (
    set result="%javaHome%\bin\java" --add-modules java.xml.bind
  ) else (
    set result="%javaHome%\bin\java"
  )
)
exit /b


::FUNCTION
:CheckJavaCommand
:: argument %~1%   : full path name including java.exe without quotes
:: return %result% : launch command with added arguments or ""
set result=
"%~1%" -version 2>nul
if errorlevel 1 (
  exit /b
)
set TMP1=%TMP%\waters.tmp
"%~1%" -version 2>!TMP1!
find "java version ""1.8." %TMP1% >nul
if not errorlevel 1 (
  del %TMP1%
  set result="%~1"
  exit /b
)
find "java version ""1.8""" %TMP1% >nul
if not errorlevel 1 (
  del %TMP1%
  set result="%~1"
  exit /b
)
find "java version ""9." %TMP1% >nul
if not errorlevel 1 (
  del %TMP1%
  set result="%~1" --add-modules java.xml.bind
  exit /b
)
find "java version ""10." %TMP1% >nul
if not errorlevel 1 (
  del %TMP1%
  set result="%~1" --add-modules java.xml.bind
  exit /b
)
del %TMP1%
exit /b  
