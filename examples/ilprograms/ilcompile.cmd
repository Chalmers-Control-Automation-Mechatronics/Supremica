@IF "%1"=="" GOTO USAGE
@SET COMPILE_DIR=%2
@rem @echo %COMPILE_DIR%
@IF "%COMPILE_DIR%"=="" GOTO SET_DIR
@GOTO RUN

:SET_DIR

@SET COMPILE_DIR=.

:RUN
java -cp .;..\..\build;..\..\lib\unjared org.supremica.softplc.CompILer.ilc %1 %COMPILE_DIR%

@GOTO END

:USAGE
@ECHO Usage: ilcompile file.il
@GOTO END

:END