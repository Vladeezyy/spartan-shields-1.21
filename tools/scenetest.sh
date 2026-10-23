#!/bin/sh
# Runs the client test scene (port/DevScene, max 15 min) on LOADER=neoforge|fabric: screenshots in
# <loader>/run/screenshots/scene_*.png, then prints the functional checks and the errors of the run.
# Extra gradle arguments are passed on (e.g. -Pwith_mods).
. "$(dirname "$0")/common.sh"
LOG=build-scene-$LOADER.log
rm -rf "$RUN/saves/ss_scene" "$RUN"/screenshots/scene_*.png "$LOG"
run_watched "$LOG" 900 ":$LOADER:runScene" "$@"
echo "exit $?"
grep -E '\[scene\] (CHECK|DONE)' "$LOG" | sed -E 's/^.*\[scene\] //'
ls "$RUN/screenshots/" 2>/dev/null | grep scene_
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception' "$LOG" | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
