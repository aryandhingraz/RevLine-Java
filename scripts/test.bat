@echo off
REM RevLine - Test Script (Windows)
REM Compiles and runs the TestRunner. Exit code != 0 on any failure.

setlocal enabledelayedexpansion

echo ============================================
echo  RevLine Test
echo ============================================

REM Build main sources first
call scripts\build.bat
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Main build failed. Cannot run tests.
    exit /b 1
)

REM Collect test sources
set "TESTS="
for /r src\test\java %%f in (*.java) do (
    set "TESTS=!TESTS! %%f"
)

if "!TESTS!"=="" (
    echo [WARN] No test sources found.
    exit /b 0
)

REM Compile tests
if exist out\test rmdir /s /q out\test
mkdir out\test

REM Only add lib to classpath if jars exist there
set "CP_LIB="
if exist lib\*.jar set "CP_LIB=;lib\*"

echo Compiling tests (Java 17 target)...
javac --release 17 -Xlint:all -encoding UTF-8 -d out\test -cp "out\main%CP_LIB%" -sourcepath src\test\java !TESTS!

if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Test compilation failed.
    exit /b 1
)

REM Run TestRunner
echo Running tests...
java -cp "out\main;out\test%CP_LIB%" com.revline.TestRunner

exit /b %ERRORLEVEL%
