#!/usr/bin/env bash
# Download default LiteRT-LM bundle for Pixel 8 (Qwen2.5-0.5B-Instruct).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="$ROOT/android/app/src/main/assets/models"
REPO="litert-community/Qwen2.5-0.5B-Instruct"
# Prefer .litertlm task bundle when published; fallback to documented tflite name for dev.
LITERTLM_FILE="${LITERTLM_FILE:-qwen2.5-0.5b-instruct.litertlm}"
TFLITE_FALLBACK="Qwen2.5-0.5B-Instruct_seq128_q8_ekv1280.tflite"

mkdir -p "$ASSETS"

if [[ -f "$ASSETS/$LITERTLM_FILE" ]]; then
  echo "Model already present: $ASSETS/$LITERTLM_FILE"
  exit 0
fi

if ! command -v huggingface-cli >/dev/null 2>&1; then
  if [[ -d "$ROOT/backend/.venv" ]]; then
    # shellcheck source=/dev/null
    source "$ROOT/backend/.venv/bin/activate"
  fi
  pip install -q "huggingface_hub[cli]"
fi

echo "==> Trying LiteRT-LM bundle: $REPO ($LITERTLM_FILE)"
if huggingface-cli download "$REPO" --include "$LITERTLM_FILE" --local-dir "$ASSETS" 2>/dev/null; then
  if [[ -f "$ASSETS/$LITERTLM_FILE" ]]; then
    echo "Installed: $ASSETS/$LITERTLM_FILE"
    exit 0
  fi
fi

echo "==> .litertlm not found; downloading TFLite fallback (rename for dev only)"
huggingface-cli download "$REPO" --include "$TFLITE_FALLBACK" --local-dir "$ASSETS"
if [[ -f "$ASSETS/$TFLITE_FALLBACK" && ! -f "$ASSETS/$LITERTLM_FILE" ]]; then
  echo "WARN: Got $TFLITE_FALLBACK — convert to .litertlm or fetch the task bundle from HuggingFace LiteRT community."
  echo "      LiteRtLlmRationaleRunner expects: $LITERTLM_FILE"
fi

echo "Optional: browse https://huggingface.co/litert-community for Gemma/Qwen .litertlm builds."
