@echo off
REM RevLine - Demo Script (Windows)
REM Feeds scripted input to the app and saves console output.

setlocal

echo ============================================
echo  RevLine Demo
echo ============================================

call scripts\build.bat
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Build failed. Cannot run demo.
    exit /b 1
)

set "TIMESTAMP=%date:~-4%%date:~4,2%%date:~7,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=%TIMESTAMP: =0%"

if exist docs\evidence\demo_input.txt (
    echo Running demo with scripted input...
    java -cp "out\main;lib\*" com.revline.app.RevLineApp < docs\evidence\demo_input.txt > "docs\evidence\demo_output_%TIMESTAMP%.txt" 2>&1
    type "docs\evidence\demo_output_%TIMESTAMP%.txt"
    echo.
    echo [DONE] Output saved to docs\evidence\demo_output_%TIMESTAMP%.txt
) else (
    echo [WARN] No demo_input.txt found. Running interactively.
    java -cp "out\main;lib\*" com.revline.app.RevLineApp
)

exit /b 0
