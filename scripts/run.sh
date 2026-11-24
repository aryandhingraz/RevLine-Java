#!/bin/bash
# RevLine — Run Script (Unix)

echo "============================================"
echo " RevLine Run"
echo "============================================"

if [ ! -f out/main/com/revline/app/RevLineApp.class ]; then
    echo "[INFO] Build output not found. Building first..."
    bash scripts/build.sh
    if [ $? -ne 0 ]; then exit 1; fi
fi

java -cp "out/main:lib/*" com.revline.app.RevLineApp "$@"
exit $?
