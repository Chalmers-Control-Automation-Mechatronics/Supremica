@echo off

REM The commands below do not work on Vista
REM set supremicaDir = .
REM if exist %SUPREMICA_HOME% set supremicaDir=%SUPREMICA_HOME%/dist
REM @java -cp %supremicaDir%/Supremica.jar -enableassertions org.supremica.gui.ide.IDE -p %supremicaDir%/SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9

@java -cp ./Supremica.jar -enableassertions org.supremica.gui.ide.IDE -p %supremicaDir%/SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9

