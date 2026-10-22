#!/bin/sh
# Compatibility runs on LOADER=neoforge|fabric: the client scene and the stress scene with -Pwith_compat (Sodium, Iris
# with a shader pack from run/shaderpacks, Lithium, FerriteCore, ModernFix, Jade, JEI on NeoForge, EMI on Fabric).
# Outputs: build-compat-<loader>-*.out, screenshots in build/compat-<loader>-*/.
. "$(dirname "$0")/common.sh"
mkdir -p "$RUN/shaderpacks" "$RUN/config"
cp run/shaderpacks/*.zip "$RUN/shaderpacks/" 2>/dev/null
[ -f run/config/iris.properties ] && cp run/config/iris.properties "$RUN/config/"
OUT=build-compat-$LOADER
tools/scenetest.sh -Pwith_compat > $OUT-scene.out 2>&1
rm -rf build/compat-$LOADER-scene && mkdir -p build/compat-$LOADER-scene && cp "$RUN"/screenshots/scene_*.png build/compat-$LOADER-scene/ 2>/dev/null
tools/stresstest.sh -Pwith_compat > $OUT-stress.out 2>&1
rm -rf build/compat-$LOADER-stress && mkdir -p build/compat-$LOADER-stress && cp "$RUN"/screenshots/stress_*.png build/compat-$LOADER-stress/ 2>/dev/null
for f in $OUT-scene.out $OUT-stress.out; do
	echo "=== $f: $(grep -c 'CHECK' $f) checks, $(grep -oE 'DONE, failed checks: [0-9]+' $f)"
	grep 'CHECK FAIL' $f | head -10
	sed -n '/--- errors/,$p' $f | head -12
done
