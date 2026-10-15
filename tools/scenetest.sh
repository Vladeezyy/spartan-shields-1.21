#!/bin/sh
# Runs the client test scene (port/DevScene, max 15 min): screenshots in run/screenshots/scene_*.png, then prints the
# functional checks and the errors of the run. Extra gradle arguments are passed on (e.g. -Pwith_mods).
cd "$(dirname "$0")/.."
rm -rf run/saves/ss_scene run/screenshots/scene_*.png build-scene.log
perl -e 'alarm 900; exec @ARGV' ./gradlew runScene --console=plain "$@" > build-scene.log 2>&1
echo "exit $?"
grep -E '\[scene\] (CHECK|DONE)' build-scene.log | sed -E 's/^.*\[scene\] //'
ls run/screenshots/ 2>/dev/null | grep scene_
echo "--- errors"
grep -E '/(ERROR|FATAL)\]|Caused by|Exception' build-scene.log | grep -v 'NoSuchFileException: server.properties' | sed -E 's/^\[[^]]*\] //' | cut -c1-300 | sort | uniq -c | sort -rn | head -30
