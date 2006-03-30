@echo off
REM sometimes, we need more memory than the default JVM heap.
java -Xms224M -Xmx224M  -cp ..\build;..\lib\unjared\;..\images;..\examples -enableassertions org.supremica.apps.Supremica -p SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9