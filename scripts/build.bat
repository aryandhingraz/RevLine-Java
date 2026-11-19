@echo off
REM RevLine - Build Script (Windows)
REM Compiles all Java sources with -Xlint:all, fails on any warning.

setlocal enabledelayedexpansion

echo ============================================
echo  RevLine Build
echo ============================================

REM Collect all .java files from src/main
set "SOURCES="
for /r src\main\java %%f in (*.java) do (
    set "SOURCES=!SOURCES! %%f"
)

if "!SOURCES!"=="" (
    echo [ERROR] No Java source files found.
    exit /b 1
)

REM Clean and create output directory
if exist out\main rmdir /s /q out\main
mkdir out\main

REM Only add lib to classpath if jars exist there
set "CP_LIB="
if exist lib\*.jar set "CP_LIB=-cp lib\*"

echo Compiling sources (Java 17 target)...
javac --release 17 -Xlint:all -encoding UTF-8 -d out\main %CP_LIB% -sourcepath src\main\java !SOURCES!

if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Compilation failed.
    exit /b 1
)

echo [PASS] Build succeeded with zero warnings.
exit /b 0
