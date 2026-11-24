#!/bin/bash
# RevLine — Build Script (Unix)
# Compiles all Java sources with -Xlint:all, fails on any warning.

echo "============================================"
echo " RevLine Build"
echo "============================================"

SOURCES=$(find src/main/java -name "*.java")

if [ -z "$SOURCES" ]; then
    echo "[ERROR] No Java source files found."
    exit 1
fi

rm -rf out/main
mkdir -p out/main

echo "Compiling sources (Java 17 target)..."
javac --release 17 -Xlint:all -Werror -encoding UTF-8 -d out/main -cp "lib/*" -sourcepath src/main/java $SOURCES

if [ $? -ne 0 ]; then
    echo "[FAIL] Compilation failed."
    exit 1
fi

echo "[PASS] Build succeeded with zero warnings."
exit 0
