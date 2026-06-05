#!/usr/bin/env bash
# Push a .litertlm model to a connected Pixel for the dev workflow.
#
# We push to the app's EXTERNAL files dir (Android/data/<pkg>/files/models),
# which is writable over adb without root and is read by LlmModelPaths as a
# fallback when the model isn't bundled in the APK. This avoids the slow/blocked
# `run-as cat` copy into internal storage for a ~491MB file.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PKG="${PKG:-com.aerodue.app}"
SRC="${1:-$ROOT/build/litertlm/qwen2.5-0.5b-instruct.litertlm}"
NAME="$(basename "$SRC")"
DEST_DIR="/storage/emulated/0/Android/data/$PKG/files/models"
DEST="$DEST_DIR/$NAME"

if ! command -v adb >/dev/null 2>&1; then
  echo "adb not found on PATH. Add Android platform-tools to PATH." >&2
  exit 1
fi

if [[ ! -f "$SRC" ]]; then
  echo "Missing model: $SRC" >&2
  echo "Build it first: ./scripts/build-litertlm.sh" >&2
  exit 1
fi

# Pick the first connected device if multiple are attached.
SERIAL="${SERIAL:-$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')}"
if [[ -z "${SERIAL:-}" ]]; then
  echo "No connected device. Run: adb devices" >&2
  exit 1
fi

echo "==> Device: $SERIAL"
adb -s "$SERIAL" shell mkdir -p "$DEST_DIR"
echo "==> Pushing $NAME ($(du -h "$SRC" | cut -f1)) -> $DEST"
adb -s "$SERIAL" push "$SRC" "$DEST"
adb -s "$SERIAL" shell ls -lh "$DEST_DIR"
echo
echo "Done. Relaunch the app; LiteRtLlmRationaleRunner loads from the external files dir."
