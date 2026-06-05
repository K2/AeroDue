# AeroDue — agent guide

**On-device source of truth:** Kotlin `android/core/` (compensation rules, domain, GPS/flight stubs).

**Host reference:** Python `backend/` for regulation corpus work, CLI fixtures, and parity tests — keep logic aligned with `core` when changing rules.

## Stack

| Layer | Path | Notes |
|-------|------|--------|
| Native engine | `android/core/` | `CompensationEngine`, DOT/EU261, card/business perks |
| Android UI | `android/app/` | Jetpack Compose, API 34 |
| On-device LLM | `android/llm/` LiteRT-LM 0.13 | `LiteRtLlmRationaleRunner` · CPU backend · rules fallback |
| Host Python | `backend/aerodue/` | CLI, YAML regulations, optional parity with Kotlin |

## Build & test

```bash
cd android
./gradlew :core:testDebugUnitTest :llm:assemble :app:assembleDebug
```

Isolated agent worktree (optional): `/Users/primecourts/Projects/AeroDue-worktrees/feat-llm`

## Python dev (host parity)

```bash
cd backend && source .venv/bin/activate
python -m aerodue.cli assess --fixture samples/delayed_connection.json
pytest
```

When changing compensation rules, update **both** `android/core/` and `backend/aerodue/core/` or add a sync check.

## LLM status

- **Working:** Qwen2.5-0.5B-Instruct runs on-device via LiteRT-LM (CPU backend).
  Verified on Pixel 8: engine loads the `.litertlm` and generates a grounded
  rationale in ~9s. Falls back to `Rationale.explainOffline` (rules) if the
  model is missing or generation fails.

### Build & deploy the model (dev workflow)

```bash
./scripts/build-litertlm.sh          # TFLite + tokenizer + LlmMetadata -> build/litertlm/*.litertlm
./scripts/push-model-to-device.sh    # adb push to Android/data/<pkg>/files/models (no root needed)
```

Then rebuild/reinstall the app and relaunch. `LlmModelPaths` resolves the model
from internal `filesDir/models` first, then the external app files dir (where
the push script lands it). Watch generation with:

```bash
adb logcat -s AeroDueLlm
```

**Gotchas (don't regress these):**
- The bundle MUST include an `LlmMetadata` section. A TFLite + tokenizer alone
  aborts natively in `nativeCreateEngine`/`nativeCreateConversation`.
- `start_token` must be non-empty (runtime reads `ids[0]`). Qwen has no BOS, so
  we use `<|im_start|>` (151644) and drop it from the system prompt prefix.
- GPU backend aborts (native SIGABRT) for this int8 prefill/decode variant;
  use CPU. A native abort can't be caught by Kotlin try/catch — it kills the app.

Do not commit `*.gguf`, `*.litertlm`, `android/local.properties`, or `.env.android`.
