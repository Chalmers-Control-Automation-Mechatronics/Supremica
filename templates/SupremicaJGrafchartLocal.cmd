@cd ..\resources\jgrafchart\bin

@set LIB=../lib
@set CODE=../code
@set OUTPUT=../code

@REM set CP="%CODE%;%LIB%/JGo.jar;%LIB%/crimson.jar;%LIB%/xalan.jar;%LIB%/jaxp.jar;%LIB%/regler.jar;%LIB%/xmlBlaster.jar;%LIB%/jutils.jar;%LIB%/xtdash.jar;%LIB%/jacorb.jar;%LIB%/jh.jar;%LIB%/CCOM.jar"
@REM java -classpath %CP% grafchart.sfc.Editor  -graphicalSteps 1 -geometry 1024x768

@java -cp ..\..\..\build;..\..\..\lib\unjared\;..\..\..\images -enableassertions org.supremica.apps.Supremica -p ..\..\..\dist\SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9

@cd ..\..\..\dist