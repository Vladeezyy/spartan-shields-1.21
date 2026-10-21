#!/bin/sh
# Runs the functional / stress scene (port/StressScene, max 20 min) on LOADER=neoforge|fabric with a fresh world and
# the default config, then prints the checks, the measurements and the errors. Extra gradle arguments are passed on.
. "$(dirname "$0")/common.sh"
LOG=build-stress-$LOADER.log
rm -rf "$RUN/saves/ss_stress" "$RUN"/screenshots/stress_*.png "$RUN/config/spartanshields-common.toml" "$LOG"
perl -e 'alarm 1200; exec @ARGV' ./gradlew ":$LOADER:runStress" --console=plain "$@" > "$LOG" 2>&1
echo "exit $?"
grep -E '\[stress\] (CHECK|STAT|DONE)' "$LOG" | sed -E 's/^.*\[stress\] //'
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception' "$LOG" | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
