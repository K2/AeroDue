#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "$ROOT/.env.android" ]]; then
  # shellcheck source=/dev/null
  source "$ROOT/.env.android"
fi

fail=0
check() {
  if eval "$2" >/dev/null 2>&1; then
    echo "OK  $1"
  else
    echo "MISSING  $1"
    fail=1
  fi
}

check "JAVA_HOME (17+)" '[[ -n "${JAVA_HOME:-}" ]] && java -version 2>&1 | grep -q "17\|21"'
check "ANDROID_HOME" '[[ -n "${ANDROID_HOME:-}" ]] && [[ -d "$ANDROID_HOME" ]]'
check "adb" command -v adb
check "sdkmanager" command -v sdkmanager
check "platform android-34" '[[ -d "${ANDROID_HOME}/platforms/android-34" ]]'
check "build-tools 34" 'ls -d "${ANDROID_HOME}"/build-tools/34.* >/dev/null 2>&1'
check "NDK" 'ls -d "${ANDROID_HOME}"/ndk/* >/dev/null 2>&1'
check "android/local.properties" '[[ -f "$ROOT/android/local.properties" ]]'
check "gradlew" '[[ -x "$ROOT/android/gradlew" ]]'
check "backend venv" '[[ -x "$ROOT/backend/.venv/bin/python" ]]'

if [[ -f "$ROOT/android/app/src/main/assets/models/"*.gguf ]]; then
  echo "OK  GGUF model in assets"
else
  echo "SKIP  GGUF model (run ./scripts/download-models.sh)"
fi

exit "$fail"
