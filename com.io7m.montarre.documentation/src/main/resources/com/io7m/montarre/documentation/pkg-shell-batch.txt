@echo off

REM Auto generated: Do not edit.
REM This is a launch script for Windows-like platforms.

if NOT DEFINED MONTARRE_HOME (
  echo MONTARRE_HOME is unset
  exit /b 1
)

REM Check that the available Java runtime is suitable.
java -jar "%MONTARRE_HOME%/bin/launch.jar" check-java-version 21
if %errorlevel% neq 0 exit /b 1

REM Build a module path.
for /f %%i in ('java -jar %MONTARRE_HOME%\bin\launch.jar get-module-path %MONTARRE_HOME%') do set MONTARRE_MODULE_PATH=%%i
if %errorlevel% neq 0 exit /b 1

REM Run the application.
java -p %MONTARRE_MODULE_PATH% -m com.io7m.montarre.cmdline/com.io7m.montarre.cmdline.MMain %*
if %errorlevel% neq 0 exit /b 1
