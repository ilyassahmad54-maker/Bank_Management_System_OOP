@echo off
cd /d "%~dp0"
.maven\apache-maven-3.9.16\bin\mvn.cmd javafx:run
pause
