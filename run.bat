@echo off
cd /d "%~dp0"

:: Try system Maven first, then bundled tools/ Maven
where mvn >nul 2>&1
if %errorlevel% == 0 (
    mvn javafx:run
) else if exist "tools\apache-maven-3.9.9\bin\mvn.cmd" (
    tools\apache-maven-3.9.9\bin\mvn.cmd javafx:run
) else (
    echo ERROR: Maven not found.
    echo Install Maven from https://maven.apache.org or add it to PATH.
    pause
    exit /b 1
)
pause
