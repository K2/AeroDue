# AeroDue

**Privacy-first, on-device flight disruption compensation recovery.**

AeroDue figures out *everything* a traveler is owed after a flight delay or
cancellation and stacks it across three independent sources — **US DOT refunds**,
**EU261 compensation**, and **credit-card trip-delay insurance** — then renders the
result as a plain-language **receipt of money owed**. It prepares the filing
paperwork, files on your confirmation, and follows up on rejections
(analyze → refile/appeal).

> AeroDue is **not a legal service.** It automates document preparation and
> status tracking; you submit and confirm every step. See
> [docs/COMPENSATION_RULES.md](docs/COMPENSATION_RULES.md#not-legal-advice).

## Why it exists

- Airlines and insurers make claims deliberately painful, so most travelers
  leave **hundreds of dollars unclaimed** after a disruption.
- AeroDue automates the boring/hard parts: detecting the disruption, computing
  entitlements across DOT + EU261 + card insurance, preparing the paperwork, and
  chasing rejections.
- The rebates go **directly back to the consumer**. Recovering this money is the
  free tier — there is no cut taken from your refund.

## Freemium model

| Tier | What you get | What powers it |
|------|--------------|----------------|
| **Free** | Compensation recovery + filing/follow-up across DOT, EU261, and card insurance | Opt-in, anonymized GPS / door-to-door travel telemetry (a data flywheel) that improves premium routing |
| **Premium** | Door-to-door trip optimization: rebooking, refundable hotel holds, ground transport (train vs. rideshare), carry-on/airport tips, check-in timing, card-plan selection, and an on-device agent that plans the optimal trip | The telemetry flywheel + the on-device LLM |

Telemetry sharing is **opt-out by default** and gated by explicit, granular
consent (see `android/app/.../consent/`).

**Optional plugins** ship under their *own* separate terms — **not** AeroDue's
EULA — and are disabled by default. Example: a Polymarket weather/flight hedge,
shipped as an inert "later option."

## Architecture

| Layer | Path | Notes |
|-------|------|-------|
| Native engine | `android/core/` | `CompensationEngine`, DOT/EU261/card/business/airline perks — pure Kotlin rules |
| Android UI | `android/app/` | Jetpack Compose, Material 3, API 34 |
| On-device LLM | `android/llm/` (LiteRT-LM 0.13) | Qwen2.5-0.5B-Instruct on the **CPU backend**, deterministic rules fallback |
| Host Python | `backend/aerodue/` | CLI, YAML regulation corpus, parity tests with Kotlin core |

Data flows **disruption → entitlements → filing → follow-up**, with the LLM
adding grounded narrative copy on top of deterministic rule output. See
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full module breakdown and
data flow.

App screens: **Home, Claims, Coverage, Profile, Regulations, Premium** (trip
planner), **Consent** (GPS/telemetry opt-in), and **Integrations** (MCP
connectors).

## Build & test

Android (core unit tests, LLM module, debug APK):

```bash
cd android
./gradlew :core:testDebugUnitTest :llm:assemble :app:assembleDebug
```

Host Python (regulation corpus + parity):

```bash
cd backend && source .venv/bin/activate
python -m aerodue.cli assess --fixture samples/delayed_connection.json
pytest
```

When changing compensation rules, update **both** `android/core/` and
`backend/aerodue/core/` (or add a sync check) so host parity holds.

## On-device LLM

AeroDue runs offline by default. Qwen2.5-0.5B-Instruct runs on-device via
LiteRT-LM and generates grounded rationales, filing cover notes, rejection
analysis, and trip-plan narratives in **~9s on a Pixel 8**. If no model is
installed or generation fails, every flow falls back to deterministic
rules-based copy (`RuleOnlyRationaleRunner`).

Build and deploy the model (dev workflow):

```bash
./scripts/build-litertlm.sh        # TFLite + tokenizer + LlmMetadata -> build/litertlm/*.litertlm
./scripts/push-model-to-device.sh  # adb push to Android/data/<pkg>/files/models (no root)
adb logcat -s AeroDueLlm           # watch generation
```

**Gotchas (don't regress these):**

- The bundle **must** include an `LlmMetadata` section. A TFLite + tokenizer
  alone aborts natively in `nativeCreateEngine` / `nativeCreateConversation`.
- `start_token` must be **non-empty** (the runtime reads `ids[0]`). Qwen has no
  BOS, so we use `<|im_start|>` (151644) and drop it from the system-prompt prefix.
- Use the **CPU** backend. The GPU backend aborts (native `SIGABRT`) for this
  int8 prefill/decode variant, and a native abort can't be caught by Kotlin
  `try/catch` — it kills the app.

## MCP connectors & the historical-filing superpower

AeroDue ships an **MCP connector framework** (`android/app/.../mcp/`) that lets a
user plug in their **own** cloud model (OpenAI-compatible `chat/completions`) or
an **MCP tools server** (JSON-RPC 2.0 over HTTP: `initialize` + `tools/list` +
`tools/call`). Connectors are persisted (DataStore), **off by default**, run
under the provider's own terms, and assistant generation routes
**cloud → on-device → rules**.

Because the phone runs a SMOL local LLM *and* exposes an MCP surface, it becomes
both an MCP client and a conceptually MCP-addressable assistant node. The
flagship flow:

1. A user has **Cursor** (or any MCP-capable host) connected to their **Gmail**.
2. Cursor connects to the MCP exposed by/through **AeroDue on the phone**.
3. The desktop agent and the phone's on-device agent collaborate to **scan Gmail
   for past flight confirmations and disruption emails**, reconstruct trips,
   compute back-dated entitlements, and **batch-file/track historical claims** —
   recovering money never claimed.
4. **Privacy:** sensitive parsing can stay on-device via the SMOL model; the
   cloud host is used only where the user opts in.

Full wire formats and a sequence walkthrough are in [docs/MCP.md](docs/MCP.md).

## Documentation

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — modules, data flow, LLM fallback chain
- [docs/MCP.md](docs/MCP.md) — connector framework, wire formats, historical-filing flow
- [docs/COMPENSATION_RULES.md](docs/COMPENSATION_RULES.md) — DOT vs EU261 vs card logic, disclaimers
- [docs/PITCH.md](docs/PITCH.md) — navigable slide deck
- [AGENTS.md](AGENTS.md) — agent / host Python workflow

## Repo hygiene

Do **not** commit `*.gguf`, `*.litertlm`, `android/local.properties`, or
`.env.android` (model weights, secrets, and machine-local config).
