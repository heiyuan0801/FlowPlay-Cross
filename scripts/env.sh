#!/usr/bin/env bash
# Source this file before invoking Gradle or Android SDK tools on this Mac.
export FLOWPLAY_TOOLCHAINS=/Volumes/mac/codex/.toolchains
export JAVA_HOME="$FLOWPLAY_TOOLCHAINS/jdk-21.0.13+9/Contents/Home"
export ANDROID_HOME="$FLOWPLAY_TOOLCHAINS/android-sdk"
export ANDROID_SDK_ROOT="$ANDROID_HOME"
export GRADLE_USER_HOME="$FLOWPLAY_TOOLCHAINS/gradle-user-home"
export TMPDIR="$FLOWPLAY_TOOLCHAINS/tmp/"
export FLOWPLAY_JAVA_TMP="$HOME/.android/flowplay-java-tmp"
export ANDROID_EMU_CRASH_REPORTING_DATABASE="$FLOWPLAY_TOOLCHAINS/emulator-crashes"
mkdir -p "$TMPDIR" "$FLOWPLAY_JAVA_TMP" "$ANDROID_EMU_CRASH_REPORTING_DATABASE"
# Use an internal-volume Java temp directory for this Mac's DMG mounting.
export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Djava.io.tmpdir=$FLOWPLAY_JAVA_TMP"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Java does not automatically use the shell's HTTP(S)_PROXY. Reuse a local proxy when present.
case "${HTTPS_PROXY:-${HTTP_PROXY:-}}" in
    http://127.0.0.1:*|http://localhost:*)
        _flowplay_proxy="${HTTPS_PROXY:-$HTTP_PROXY}"
        _flowplay_proxy="${_flowplay_proxy#http://}"
        _flowplay_proxy="${_flowplay_proxy%/}"
        export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} -Dhttp.proxyHost=${_flowplay_proxy%:*} -Dhttp.proxyPort=${_flowplay_proxy##*:} -Dhttps.proxyHost=${_flowplay_proxy%:*} -Dhttps.proxyPort=${_flowplay_proxy##*:}"
        unset _flowplay_proxy
        ;;
esac
