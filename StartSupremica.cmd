@echo off
:: Set the working directory to the main root folder
cd /d "%~dp0"

:: Launch Supremica silently using the system's modern Java (Java 21+)
start javaw --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED -enableassertions -jar dist\Supremica.jar %*