@echo off
REM ***** we _could_ have a Makefile here, but that would be too easy, wouldn't it?
REM ------------------- set default paths
SET JAVAHOME=c:\Program Files\JDK


REM ------------------- create the JNI header
javah -classpath ..\..\..\build -o executer.h  org.supremica.gui.simulator.ExternalEventExecuter

REM ------------------- compile and link to a DLL
del /Q *.dll
cl /c -I"%JAVAHOME%\include" -I"%JAVAHOME%\include\win32" random_executer.c  
rename random_executer.obj  executer.obj 
link executer.obj /DLL /out:executer.dll
REM ------------------- cleanup
del *.obj
del *.lib
del *.exp
copy executer.dll  ..\..\..\dist
dir *.dll