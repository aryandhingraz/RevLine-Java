#!/bin/bash
# RevLine — Test Script (Unix)
# Compiles and runs the TestRunner. Exit code != 0 on any failure.

echo "============================================"
echo " RevLine Test"
echo "============================================"

bash scripts/build.sh
if [ $? -ne 0 ]; then
    echo "[FAIL] Main build failed. Cannot run tests."
    exit 1
fi

TESTS=$(find src/test/java -name "*.java")

if [ -z "$TESTS" ]; then
    echo "[WARN] No test sources found."
    exit 0
fi

rm -rf out/test
mkdir -p out/test

CP_LIB=""
if compgen -G "lib/*.jar" > /dev/null; then
    CP_LIB=":lib/*"
fi

echo "Compiling tests (Java 17 target)..."
javac --release 17 -Xlint:all -encoding UTF-8 -d out/test -cp "out/main$CP_LIB" -sourcepath src/test/java $TESTS

if [ $? -ne 0 ]; then
    echo "[FAIL] Test compilation failed."
    exit 1
fi

echo "Running tests..."
java -cp "out/main:out/test$CP_LIB" com.revline.TestRunner
exit $?
