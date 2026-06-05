# AeroDue — Pitch

A navigable, demo-ready deck. Use the index to jump, or the prev/next links at
the bottom of each slide.

**Also:** [Product deck (HTML)](flow.html) · [Technical deck — built with Cursor (HTML)](tech.html) · [README screenshots](../README.md#screenshots-hackathon-build)

## Built with Cursor (hackathon) {#hackathon}

This repo is the **Cursor hackathon** submission: a live Android app built with
**Cursor agents** driving implementation across `android/core`, `android/app`,
`android/llm`, MCP connectors, and `backend/` parity — not a slide-deck prototype.

| Screen | What it shows |
|--------|----------------|
| [Compensation receipt](assets/compensation-receipt.png) | **$750 across 3 stacked sources** — DOT, Chase trip delay, corporate policy — estimated on-device |
| [Trip recovery](assets/trip-recovery.png) | Delay detected (7h30m), three compensation paths, rules + optional Qwen LLM |
| [Claims filing](assets/claims-filing.png) | Itemized claims, confidence, advance to paid / simulate rejection |
| [Premium optimizer](assets/premium-optimizer.png) | Door-to-door re-plan: flight, hotel hold, ground — on-device agent |

**One line:** *They get you money when prices drop. We get you money when your flight falls apart.*

## Slide index {#index}

0. [Hackathon — Built with Cursor](#hackathon)
1. [Title — Get back what flying owes you](#slide-1)
2. [The pain — hundreds left unclaimed](#slide-2)
3. [Free money — the stacked receipt](#slide-3) · screenshots in [flow.html](flow.html) slides 4–5
4. [Freemium — the data flywheel](#slide-4)
5. [The superpower — offline LLM + MCP](#slide-5)
6. [Compliance & trust](#slide-6)
7. [Technicals](#slide-7)

---

## Slide 1 — Get back what flying owes you {#slide-1}

# ✈️ AeroDue / Comped

**Get paid when your flight falls apart.**

> Forward once → watch → detect → file. After the first tap, we chase it to paid.

AeroDue stacks **US DOT refunds + EU261 compensation + credit-card trip-delay
insurance** into a single **compensation receipt** (every source shown, no black
box), prepares the paperwork, and follows up on rejections — all on your phone.

Built **with Cursor at the hackathon** — see [Hackathon](#hackathon) and
[tech.html](tech.html).

[Index](#index) · [Next →](#slide-2)

---

## Slide 2 — The pain: hundreds left unclaimed {#slide-2}

**Airlines and insurers make claims deliberately painful.**

- Most travelers leave **hundreds of dollars unclaimed** after a delay or
  cancellation.
- Entitlements are scattered across **three unrelated systems** — a federal
  regulator, an EU regulation, and your card's benefits portal — each with its
  own forms, thresholds, and deadlines.
- Rejections are common, and chasing an appeal is even more painful than the
  first filing.

The money is real and it's *yours* — the friction is the only thing standing
between you and it.

[← Prev](#slide-1) · [Index](#index) · [Next →](#slide-3)

---

## Slide 3 — Free money: the stacked receipt {#slide-3}

**AeroDue automates the boring/hard parts and gives you back free money.**

A single disruption can pay from **multiple independent sources at once**:

| Source | Example payout |
|--------|----------------|
| US DOT | Refund to original payment / meal + hotel accommodations |
| EU261 | Fixed €250 / €400 / €600 by distance band |
| Credit card | Trip-delay expense reimbursement up to the card cap |

The engine evaluates every source, sorts by confidence, and renders the
**receipt of money owed** — then prepares filings and tracks them
(file → in review → paid, or analyze → refile/appeal).

It goes **directly back to you.** Recovery is the **free tier** — no cut of your
refund. *(Details: [Compensation rules](COMPENSATION_RULES.md).)*

[← Prev](#slide-2) · [Index](#index) · [Next →](#slide-4)

---

## Slide 4 — Freemium: the data flywheel {#slide-4}

**Free recovery funds a premium travel optimizer.**

- **Free tier:** compensation recovery + filing/follow-up. In exchange (opt-in,
  off by default) AeroDue mines **anonymized GPS / door-to-door travel
  telemetry**.
- That telemetry powers **premium routing data** — a flywheel that gets better
  the more trips it sees.
- **Premium tier:** door-to-door optimization — rebook flights, hold refundable
  hotels, choose **train vs. rideshare**, carry-on/airport tips, check-in timing
  by arrival, card-plan selection, and an **on-device agent** that plans the
  optimal trip.

Recovery earns trust and data; data earns a product people pay for.

[← Prev](#slide-3) · [Index](#index) · [Next →](#slide-5)

---

## Slide 5 — The superpower: offline LLM + MCP {#slide-5}

**The phone is both an MCP client and an MCP-addressable assistant node.**

AeroDue runs a **SMOL on-device LLM** *and* exposes an **MCP connector surface**.
Plug in your own cloud model or MCP tools; generation routes
**cloud → on-device → rules**.

**Flagship flow — historical filings via Cursor + Gmail:**

1. You have **Cursor** connected to your **Gmail**.
2. Cursor connects to the MCP exposed by/through **AeroDue on your phone**.
3. Desktop agent + on-device agent collaborate: **scan Gmail** for past flight
   confirmations + disruptions → reconstruct trips → compute **back-dated
   entitlements** → **batch-file & track** historical claims.
4. **Privacy:** sensitive parsing stays **on-device**; the cloud host is used
   only where you opt in.

> Recover money from trips you forgot you could claim. *(Wire formats &
> sequence: [MCP](MCP.md).)*

[← Prev](#slide-4) · [Index](#index) · [Next →](#slide-6)

---

## Slide 6 — Compliance & trust {#slide-6}

**Built to earn trust, not to overreach.**

- **Not a legal service.** AeroDue automates document prep + status tracking; the
  user submits and confirms every step.
- **Opt-out by default.** GPS tracking and telemetry sharing are granular,
  explicit, and off until you turn them on.
- **Bring-your-own connectors run under the provider's terms** — off by default,
  enabled only after accepting those terms; enabling sends data off-device by
  your choice.
- **Decoupled plugins under their own EULA** — e.g. a Polymarket weather/flight
  hedge — ship as a disabled "later option," isolated from the core product.

Privacy-first isn't a tagline here; it's the default configuration.

[← Prev](#slide-5) · [Index](#index) · [Next →](#slide-7)

---

## Slide 7 — Technicals {#slide-7}

**On-device, deterministic, and extensible.**

- **On-device LLM:** LiteRT-LM 0.13 running **Qwen2.5-0.5B-Instruct** on the
  **CPU backend** (~9s on Pixel 8), with a deterministic `RuleOnlyRationaleRunner`
  fallback. *(Gotchas: must include `LlmMetadata`, non-empty `start_token`, CPU
  only.)*
- **Rules engine:** pure Kotlin `android/core/` `CompensationEngine` — DOT /
  EU261 / card / business / airline perks, returning confidence-sorted
  `ClaimRecommendation`s.
- **MCP connector framework:** `ConnectorKind.MCP_TOOLS` (JSON-RPC 2.0) and
  `CLOUD_MODEL` (OpenAI-compatible), persisted in DataStore, zero third-party
  deps (`HttpURLConnection` + `org.json`).
- **UI:** Jetpack Compose + Material 3, API 34.
- **Parity backend:** Python `backend/aerodue/` mirrors the core rules with a CLI
  and `pytest` parity tests.

Build it:

```bash
cd android && ./gradlew :core:testDebugUnitTest :llm:assemble :app:assembleDebug
```

*(Deep dive: [Architecture](ARCHITECTURE.md) · [README](../README.md).)*

[← Prev](#slide-6) · [Index](#index)
