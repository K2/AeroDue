#!/usr/bin/env bash
# Build a Qwen2.5-0.5B-Instruct .litertlm bundle for on-device LiteRT-LM inference.
#
# The TFLite prefill/decode model + HF tokenizer are NOT enough on their own:
# LiteRT-LM aborts in nativeCreateEngine/nativeCreateConversation unless the
# bundle also carries an LlmMetadata section (start token, stop tokens, ChatML
# prompt templates, model type). This script assembles all three.
#
# Output: a .litertlm you can push with scripts/push-model-to-device.sh.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VENV="$ROOT/backend/.venv"
WORK="${WORK:-$ROOT/build/litertlm}"
OUT="${OUT:-$WORK/qwen2.5-0.5b-instruct.litertlm}"

REPO="${REPO:-litert-community/Qwen2.5-0.5B-Instruct}"
TFLITE="${TFLITE:-Qwen2.5-0.5B-Instruct_seq128_q8_ekv1280.tflite}"
TOKENIZER_REPO="${TOKENIZER_REPO:-Qwen/Qwen2.5-0.5B-Instruct}"

# Qwen2.5 ChatML special tokens.
IM_START=151644
IM_END=151645
EOS=151643
MAX_TOKENS="${MAX_TOKENS:-1280}"

mkdir -p "$WORK"

if [[ ! -d "$VENV" ]]; then
  echo "Missing backend venv at $VENV — run scripts/setup-android-env.sh first" >&2
  exit 1
fi
# shellcheck source=/dev/null
source "$VENV/bin/activate"
PY="$VENV/bin/python3.11"
[[ -x "$PY" ]] || PY="python"

if ! "$VENV/bin/litert-lm-builder" --help >/dev/null 2>&1; then
  echo "==> Installing litert-lm-builder"
  pip install -q litert-lm-builder
fi

if [[ ! -f "$WORK/$TFLITE" ]]; then
  echo "==> Downloading TFLite model: $REPO ($TFLITE)"
  hf download "$REPO" "$TFLITE" --local-dir "$WORK" >/dev/null
fi

if [[ ! -f "$WORK/tokenizer.json" ]]; then
  echo "==> Downloading tokenizer.json: $TOKENIZER_REPO"
  hf download "$TOKENIZER_REPO" tokenizer.json --local-dir "$WORK" >/dev/null
fi

echo "==> Generating LlmMetadata (ChatML, start=$IM_START stop=$IM_END,$EOS)"
IM_START="$IM_START" IM_END="$IM_END" EOS="$EOS" MAX_TOKENS="$MAX_TOKENS" \
OUT_PB="$WORK/llm_metadata.pb" "$PY" - <<'PY'
import os
from litert_lm_builder.runtime.proto import llm_metadata_pb2 as m

im_start = int(os.environ["IM_START"])
im_end = int(os.environ["IM_END"])
eos = int(os.environ["EOS"])

meta = m.LlmMetadata()
# Qwen has no BOS; the runtime still requires a non-empty start_token (reads ids[0]).
# ChatML's first real token is <|im_start|>, so use it and drop it from the system prefix.
meta.start_token.token_ids.ids.append(im_start)
for tid in (im_end, eos):
    meta.stop_tokens.add().token_ids.ids.append(tid)
meta.prompt_templates.system.prefix = "system\n"
meta.prompt_templates.system.suffix = "<|im_end|>\n"
meta.prompt_templates.user.prefix = "<|im_start|>user\n"
meta.prompt_templates.user.suffix = "<|im_end|>\n"
meta.prompt_templates.model.prefix = "<|im_start|>assistant\n"
meta.prompt_templates.model.suffix = "<|im_end|>\n"
meta.max_num_tokens = int(os.environ["MAX_TOKENS"])
meta.llm_model_type.qwen2p5.SetInParent()

with open(os.environ["OUT_PB"], "wb") as f:
    f.write(meta.SerializeToString())
print("   wrote", os.environ["OUT_PB"])
PY

echo "==> Packaging $OUT"
"$VENV/bin/litert-lm-builder" \
  system_metadata --str author AeroDue \
  hf_tokenizer --path "$WORK/tokenizer.json" \
  llm_metadata --path "$WORK/llm_metadata.pb" \
  tflite_model --path "$WORK/$TFLITE" --model_type prefill_decode \
  output --path "$OUT"

echo "==> Verifying sections"
"$VENV/bin/litert-lm-peek" --litertlm_file "$OUT" | grep -E "Sections|Data Type"

echo
echo "Done: $OUT"
echo "Next: ./scripts/push-model-to-device.sh \"$OUT\""
