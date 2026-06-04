# AeroDue `:llm`

LiteRT-LM integration for passenger-facing claim rationale (Pixel 8 / Tensor G3).

## Runtime

| Class | When |
|-------|------|
| `RuleOnlyRationaleRunner` | No `.litertlm` on disk — uses `Rationale.explainOffline` |
| `LiteRtLlmRationaleRunner` | Model present under `filesDir/models/` |

## Model install

```bash
./scripts/download-models.sh
adb push android/app/src/main/assets/models/*.litertlm /sdcard/Android/data/com.aerodue.app/files/models/
```

Or let the app copy from assets on first launch (see `ModelInstaller`).

Default bundle: `litert-community/Qwen2.5-0.5B-Instruct` (`.litertlm` task format).

## Backends

Tries `Backend.GPU()` first (OpenCL on Pixel), falls back to `Backend.CPU()`. NPU can be enabled later via `Backend.NPU(nativeLibraryDir)`.
