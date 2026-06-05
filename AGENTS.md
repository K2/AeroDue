# AeroDue — agent guide

**On-device source of truth:** Kotlin `android/core/` (compensation rules, domain, GPS/flight stubs).

**Host reference:** Python `backend/` for regulation corpus work, CLI fixtures, and parity tests — keep logic aligned with `core` when changing rules.

## Stack

| Layer | Path | Notes |
|-------|------|--------|
| Native engine | `android/core/` | `CompensationEngine`, DOT/EU261, card/business perks |
| Android UI | `android/app/` | Jetpack Compose, API 34 |
| On-device LLM | `android/llm/` LiteRT-LM 0.13 | `LlmRationaleRunner` · GPU→CPU fallback |
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

- **Today:** `Rationale.explainOffline` (rule-based copy)
- **Next:** `./scripts/download-models.sh` then verify `LiteRtLlmRationaleRunner` on Pixel 8

Do not commit `*.gguf`, `android/local.properties`, or `.env.android`.
