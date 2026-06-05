# AeroDue Android

Kotlin + Jetpack Compose client for Pixel-class devices.

## Modules

| Module | Role |
|--------|------|
| **`:core`** | Native offline engine — domain, regulations, compensation, GPS/flight stubs |
| **`:llm`** | LiteRT-LM `LlmRationaleRunner` (GPU→CPU, rules fallback) |
| **`:app`** | UI, `AeroDueApplication`, `CompensationService` |

## Screens

| Route | Purpose |
|-------|---------|
| `auth` | Sign up / sign in |
| `home` | Active trip & live assessment summary |
| `profile` | Home airport, DOT/EU261 toggles, GPS |
| `coverage` | Credit cards, business policy, airline status |
| `claims` | Recommendations from `CompensationEngine` |

## Build

```bash
cd android
./scripts/download-models.sh   # optional .litertlm into assets/models/
./gradlew :core:testDebugUnitTest :llm:assemble :app:assembleDebug
```

Open in Android Studio if the Gradle wrapper is missing (File → Open → `android/`).

Model weights (optional, for future LLM JNI): `app/src/main/assets/models/` — see `core/.../ModelProfiles.kt`.
