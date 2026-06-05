"""EU Regulation 261/2004 style compensation (simplified distance bands)."""

from aerodue.domain.claim import ClaimRecommendation, CompensationSource
from aerodue.domain.disruption import DisruptionEvent, DisruptionKind
from aerodue.domain.coverage import UserCoverageProfile


# EUR amounts by distance band (simplified)
_EU261_EUR = {
    "short": 250,
    "medium": 400,
    "long": 600,
}


def _distance_band_km(leg_distance_km: float) -> str:
    if leg_distance_km < 1500:
        return "short"
    if leg_distance_km < 3500:
        return "medium"
    return "long"


def evaluate_eu261_delay(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
    leg_distance_km: float = 2000,
) -> list[ClaimRecommendation]:
    if not profile.eu261_applicable:
        return []
    if event.extraordinary_circumstance_claimed:
        return []
    if event.kind not in (DisruptionKind.DELAY, DisruptionKind.CANCELLATION):
        return []

    delay = event.delay_minutes or 0
    if delay < 180 and event.kind != DisruptionKind.CANCELLATION:
        return []

    band = _distance_band_km(leg_distance_km)
    eur = _EU261_EUR[band]

    return [
        ClaimRecommendation(
            source=CompensationSource.REGULATION_EU261,
            title="EU261 fixed compensation",
            summary=(
                f"Arrival delay 3+ hours may entitle €{eur} ({band} haul) unless "
                "extraordinary circumstances apply."
            ),
            estimated_amount_usd=eur * 1.08,
            currency="EUR",
            confidence=0.75,
            citation_ids=["eu261-art7"],
            action_steps=[
                "Submit EU261 claim form to operating carrier",
                "Keep boarding pass and delay confirmation",
            ],
        )
    ]
