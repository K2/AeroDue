# AeroDue Backend

Python package for regulation-aware compensation analysis. Designed to run **on-device** (bundled with Android) and in **dev/CI** for corpus updates.

## Install (development)

```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
```

## Layout

```
aerodue/
  domain/          # Shared types (flight, disruption, user coverage)
  core/
    location/      # GPS → airport / geofence helpers
    flight/        # Schedule match, delay classification
    regulations/   # DOT EU261 etc. rule packs
    compensation/  # Eligibility engine (deterministic)
  inference/       # Small LLM config & prompts (SmolLM / Qwen)
  data/            # Regulation snippets (JSON/YAML, not full legal corpus)
```

## Model configuration

See [`aerodue/inference/models.yaml`](aerodue/inference/models.yaml). Default profile: `qwen2.5-0.5b-instruct-gguf-q4` for Pixel 8.

Download weights separately; they are not committed. Use Hugging Face CLI:

```bash
huggingface-cli download Qwen/Qwen2.5-0.5B-Instruct-GGUF \
  --include "qwen2.5-0.5b-instruct-q4_k_m.gguf" \
  --local-dir ./models
```

## Quick check

```bash
python -m aerodue.cli assess --fixture samples/delayed_connection.json
```

## Android integration

Compensation rules are implemented in **`android/core/`** (Kotlin). This Python package remains the host reference — keep `samples/` and `tests/` in sync when changing eligibility logic.

**On-device LLM (next):** MediaPipe GenAI or llama.cpp JNI using `inference/models.yaml` / `ModelProfiles.kt`. Rules stay deterministic; the LLM only summarizes policy excerpts.
