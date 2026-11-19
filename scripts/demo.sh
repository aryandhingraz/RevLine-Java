#!/bin/bash
# RevLine — Demo Script (Unix)
# Feeds scripted input to the app and saves console output.

echo "============================================"
echo " RevLine Demo"
echo "============================================"

bash scripts/build.sh
if [ $? -ne 0 ]; then
    echo "[FAIL] Build failed. Cannot run demo."
    exit 1
fi

TIMESTAMP=$(date +%Y%m%d_%H%M%S)

if [ -f docs/evidence/demo_input.txt ]; then
    echo "Running demo with scripted input..."
    java -cp "out/main:lib/*" com.revline.app.RevLineApp < docs/evidence/demo_input.txt > "docs/evidence/demo_output_${TIMESTAMP}.txt" 2>&1
    cat "docs/evidence/demo_output_${TIMESTAMP}.txt"
    echo ""
    echo "[DONE] Output saved to docs/evidence/demo_output_${TIMESTAMP}.txt"
else
    echo "[WARN] No demo_input.txt found. Running interactively."
    java -cp "out/main:lib/*" com.revline.app.RevLineApp
fi

exit $?
