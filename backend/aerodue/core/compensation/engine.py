from aerodue.core.regulations import evaluate_dot_delay, evaluate_eu261_delay
from aerodue.domain.claim import ClaimRecommendation, CompensationSource
from aerodue.domain.coverage import UserCoverageProfile
from aerodue.domain.disruption import DisruptionEvent


def _credit_card_claims(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
) -> list[ClaimRecommendation]:
    claims: list[ClaimRecommendation] = []
    delay_hours = (event.delay_minutes or 0) / 60

    for card in profile.credit_cards:
        threshold = card.trip_delay_hours_threshold
        if threshold is None or delay_hours < threshold:
            continue
        claims.append(
            ClaimRecommendation(
                source=CompensationSource.CREDIT_CARD,
                title=f"{card.issuer} trip delay benefit",
                summary=(
                    f"{card.product_name} may cover expenses after {threshold}h delay "
                    f"(cancellation={card.cancellation})."
                ),
                estimated_amount_usd=card.max_claim_usd,
                confidence=0.65,
                citation_ids=[f"cc-{card.issuer.lower()}-guide"],
                action_steps=[
                    "File claim on issuer benefits portal",
                    "Attach delay certificate and receipts",
                ],
            )
        )
    return claims


def _business_policy_claims(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
) -> list[ClaimRecommendation]:
    policy = profile.business_policy
    if policy is None:
        return []
    if not policy.reimburses_hotels_on_overnight_delay:
        return []
    if (event.delay_minutes or 0) < 360:
        return []

    return [
        ClaimRecommendation(
            source=CompensationSource.BUSINESS_POLICY,
            title=f"{policy.employer} overnight delay lodging",
            summary="Corporate policy may reimburse hotel when delay exceeds 6 hours.",
            estimated_amount_usd=policy.max_lodging_per_night_usd,
            confidence=0.6,
            citation_ids=[f"biz-{policy.employer.lower()}-travel"],
            action_steps=[
                "Book approved hotel via corporate travel tool",
                "Submit expense report with delay proof",
            ],
        )
    ]


def assess_compensation(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
    leg_distance_km: float = 2000,
) -> list[ClaimRecommendation]:
    """
    Aggregate deterministic eligibility across regulations and user coverage.
    LLM rationale is layered separately in inference.rationale.
    """
    results: list[ClaimRecommendation] = []
    results.extend(evaluate_dot_delay(event, profile))
    results.extend(evaluate_eu261_delay(event, profile, leg_distance_km))
    results.extend(_credit_card_claims(event, profile))
    results.extend(_business_policy_claims(event, profile))

    for status in profile.airline_status:
        if event.leg.carrier_iata.lower() in status.program.lower():
            results.append(
                ClaimRecommendation(
                    source=CompensationSource.AIRLINE_GOODWILL,
                    title=f"{status.program} status goodwill",
                    summary=(
                        f"Tier {status.tier or 'member'} may qualify for miles, "
                        "upgrades, or lounge passes after IRROPS."
                    ),
                    confidence=0.5,
                    citation_ids=["airline-status-policy"],
                    action_steps=["Contact elite line or use app IRROPS chat"],
                )
            )
            break

    return sorted(results, key=lambda c: c.confidence, reverse=True)
