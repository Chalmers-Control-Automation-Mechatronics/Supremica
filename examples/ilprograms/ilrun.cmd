@IF "%1"=="" GOTO USAGE
@IF "%SUPREMICADEV_PATH%"=="" GOTO SET_DIR
@GOTO RUN

:SET_DIR

@SET SUPREMICADEV_PATH=..\..

:RUN
java -cp .;%SUPREMICADEV_PATH%\build;%SUPREMICADEV_PATH%\lib\unjared;%SUPREMICADEV_PATH%\images org.supremica.softplc.RunTime.Shell -IO org.supremica.softplc.Drivers.AdlinkPCI7432 -IL %1 %2 %3 %4 %5

@GOTO END

:USAGE
@ECHO Usage: ilrun file
@GOTO END

:END
