@echo off
REM RevLine - Run Script (Windows)

setlocal

echo ============================================
echo  RevLine Run
echo ============================================

if not exist out\main\com\revline\app\RevLineApp.class (
    echo [INFO] Build output not found. Building first...
    call scripts\build.bat
    if %ERRORLEVEL% NEQ 0 exit /b 1
)

java -cp "out\main;lib\*" com.revline.app.RevLineApp
exit /b %ERRORLEVEL%
