#!/bin/sh
# Runs the functional / stress scene (port/StressScene, max 20 min) on a fresh world and default config, then prints
# the checks, the measurements and the errors. Extra gradle arguments are passed on (e.g. -Pwith_mods, -Pwith_compat).
cd "$(dirname "$0")/.."
rm -rf run/saves/ss_stress run/screenshots/stress_*.png run/config/spartanshields-common.toml build-stress.log
perl -e 'alarm 1200; exec @ARGV' ./gradlew runStress --console=plain "$@" > build-stress.log 2>&1
echo "exit $?"
grep -E '\[stress\] (CHECK|STAT|DONE)' build-stress.log | sed -E 's/^.*\[stress\] //'
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception' build-stress.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
