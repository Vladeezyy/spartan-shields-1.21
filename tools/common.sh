#!/bin/sh
# Shared by the test scripts: LOADER=neoforge (default) or fabric, JAVA_HOME on Windows, killing dev runs.
cd "$(dirname "$0")/.."
LOADER=${LOADER:-neoforge}
RUN="$LOADER/run"
if [ -z "$JAVA_HOME" ] && ! command -v java >/dev/null 2>&1; then
    for j in "$HOME"/.gradle/jdks/*21*; do [ -d "$j/bin" ] && JAVA_HOME="$j" && break; done
    export JAVA_HOME
fi
# Stops this project's dev runs (Git Bash pkill does not see Windows' java.exe)
kill_dev() {
    case "$(uname -s)" in
        MINGW*|MSYS*) powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | ? { \$_.CommandLine -match 'spartan-shields' -and \$_.CommandLine -notmatch 'GradleDaemon|GradleWrapperMain' } | % { Stop-Process -Id \$_.ProcessId -Force }" 2>/dev/null ;;
        *) pkill -f 'spartan-shields.*(fml.modFolders|loom-cache|net.fabricmc)' 2>/dev/null ;;
    esac
}
# Runs a gradle task into a log, stopping the game when it writes a crash report (it would wait on the crash screen)
run_watched() {
    log=$1; timeout=$2; shift 2
    perl -e "alarm $timeout; exec @ARGV" ./gradlew "$@" --console=plain > "$log" 2>&1 &
    gradle_pid=$!
    while kill -0 $gradle_pid 2>/dev/null; do
        if grep -q -- '---- Minecraft Crash Report ----' "$log" 2>/dev/null; then
            sleep 3
            kill_dev
        fi
        sleep 2
    done
    wait $gradle_pid
    echo "exit $?"
}
