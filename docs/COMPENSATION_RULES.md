# Compensation Rules

AeroDue stacks **independent** compensation sources so a single disruption can
yield money from several places at once. The deterministic rules live in
`android/core/` (mirrored in `backend/aerodue/core/`); the LLM only writes the
human-readable narrative on top.

- [Sources at a glance](#sources-at-a-glance)
- [US DOT](#us-dot)
- [EU261](#eu261)
- [Credit-card trip-delay insurance](#credit-card-trip-delay-insurance)
- [Business policy](#business-policy)
- [Airline goodwill](#airline-goodwill)
- [How stacking works](#how-stacking-works)
- [Extraordinary circumstances](#extraordinary-circumstances)
- [Not legal advice](#not-legal-advice)

## Sources at a glance

`CompensationSource` enumerates the stackable sources:

| Source | Trigger | Typical output | Confidence |
|--------|---------|----------------|------------|
| `REGULATION_DOT` | Cancellation (carrier control) or delay ≥ 180 min | Refund to original payment / meal-hotel accommodations | 0.85 / 0.70 |
| `REGULATION_EU261` | Delay ≥ 180 min or cancellation, EU-applicable | Fixed €250 / €400 / €600 by distance band | 0.75 |
| `CREDIT_CARD` | Delay ≥ card's hour threshold | Expense reimbursement up to card cap | 0.65 |
| `BUSINESS_POLICY` | Overnight delay ≥ 360 min | Hotel per-night reimbursement | 0.60 |
| `AIRLINE_GOODWILL` | Status-matched carrier IRROPS | Miles / upgrades / lounge passes | 0.50 |

Recommendations are returned sorted by descending confidence, so the most
defensible claims surface first on the receipt.

## US DOT

US Department of Transportation rules apply when `profile.dotApplicable` is true
and the disruption is a `DELAY` or `CANCELLATION`.

- **Cancellation (within carrier control):** a **refund to the original form of
  payment** under DOT enforcement guidance (confidence `0.85`,
  citation `dot-refund-cancel-2024`). Action steps: request the full refund, then
  file a DOT aviation consumer complaint if denied.
- **Delay ≥ 180 minutes:** **meal vouchers, rebooking, or hotel** per the
  operating carrier's customer-service plan (confidence `0.70`,
  citation `dot-cs-plan-delay`).

DOT compensation here is about **refunds and accommodations**, not a fixed cash
payout — that distinction matters when stacking with EU261.

## EU261

EU Regulation 261/2004 applies when `profile.eu261Applicable` is true, the
disruption is a `DELAY` or `CANCELLATION`, and no extraordinary circumstance is
claimed. For delays, the arrival delay must be **≥ 180 minutes** (cancellations
qualify regardless of the delay value).

Compensation is a **fixed amount by distance band**:

| Band | Leg distance | Amount |
|------|--------------|--------|
| `short` | < 1,500 km | €250 |
| `medium` | 1,500–3,499 km | €400 |
| `long` | ≥ 3,500 km | €600 |

```15:19:android/core/src/main/kotlin/com/aerodue/core/regulations/Eu261Rules.kt
private fun distanceBandKm(legDistanceKm: Double): String = when {
    legDistanceKm < 1500 -> "short"
    legDistanceKm < 3500 -> "medium"
    else -> "long"
}
```

The estimate is converted to USD (`eur * 1.08`) for receipt display but the
`currency` field stays `EUR`. Confidence `0.75`, citation `eu261-art7`.

## Credit-card trip-delay insurance

For each card in `profile.creditCards` that defines a `tripDelayHoursThreshold`,
AeroDue compares it against the disruption's delay in hours. If the delay meets
or exceeds the threshold, it surfaces the card's **trip-delay benefit** up to
`maxClaimUsd` (confidence `0.65`). Action steps point at the issuer's benefits
portal and the delay certificate + receipts.

This is the third independent pillar: regulator + card insurer can both pay for
the **same** disruption.

## Business policy

If `profile.businessPolicy` reimburses hotels on overnight delays and the delay
is **≥ 360 minutes (6 hours)**, AeroDue surfaces an **overnight-delay lodging**
reimbursement up to `maxLodgingPerNightUsd` (confidence `0.60`). Action steps:
book an approved hotel via the corporate travel tool and submit an expense report
with delay proof.

## Airline goodwill

When the carrier on the affected leg matches an entry in
`profile.airlineStatus`, AeroDue suggests **status goodwill** — miles, upgrades,
or lounge passes after IRROPS (confidence `0.50`). This is discretionary, hence
the lowest confidence, but it's free upside on top of the regulated claims.

## How stacking works

The engine evaluates **every** source independently and concatenates the results
— there is no mutual exclusion between DOT, EU261, and card insurance. A single
qualifying long-haul cancellation can therefore produce, e.g., a DOT refund **and**
a €600 EU261 payout **and** a card trip-delay reimbursement. The stacked,
confidence-sorted list is rendered as the **receipt of money owed**.

> The exact interaction of these sources varies by route, carrier, and card
> terms. AeroDue surfaces what *may* apply with a confidence score and citation;
> the user confirms and files each claim.

## Extraordinary circumstances

EU261 does **not** pay when the carrier can show "extraordinary circumstances"
(e.g. severe weather, ATC strikes, security risks) outside its control. AeroDue
models this with `DisruptionEvent.extraordinaryCircumstanceClaimed`: when set,
the EU261 evaluator returns no recommendation:

```26:28:android/core/src/main/kotlin/com/aerodue/core/regulations/Eu261Rules.kt
    if (!profile.eu261Applicable) return emptyList()
    if (event.extraordinaryCircumstanceClaimed) return emptyList()
    if (event.kind !in setOf(DisruptionKind.DELAY, DisruptionKind.CANCELLATION)) return emptyList()
```

DOT refunds for cancellations within carrier control and card/business benefits
may still apply even when an EU261 extraordinary-circumstances exclusion bites.

## Not legal advice

**AeroDue is not a legal service and does not provide legal advice.** It
automates **document preparation and status tracking**, and helps you follow up
on rejections — but **you submit and confirm every step.** Confidence scores and
citations are heuristics to help you decide what to file, not guarantees of
eligibility or payout. Regulations, carrier customer-service plans, and card
benefit terms change and vary by jurisdiction; verify the current rules and your
specific coverage before relying on any estimate here.

---

See also: [README](../README.md) · [Architecture](ARCHITECTURE.md) ·
[MCP connectors](MCP.md) · [Pitch](PITCH.md)
