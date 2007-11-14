@echo off

set supremicaDir = .
if exist %SUPREMICA_HOME% set supremicaDir=%SUPREMICA_HOME%/dist

@java -cp %supremicaDir%/Supremica.jar -enableassertions org.supremica.gui.ide.IDE -p %supremicaDir%/SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9

