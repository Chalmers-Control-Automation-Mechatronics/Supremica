@echo off
REM sometimes, we need more memory than the default JVM heap.
java -Xms500M -Xmx500M -Dcom.sun.management.jmxremote -cp ..\build;..\lib\unjared\;..\images;..\examples -enableassertions org.supremica.gui.ide.IDE -p SupremicaProperties.cfg %1 %2 %3 %4 %5 %6 %7 %8 %9