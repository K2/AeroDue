"""U.S. DOT / airline customer service plan hooks (simplified rule stubs)."""

from aerodue.domain.claim import ClaimRecommendation, CompensationSource
from aerodue.domain.disruption import DisruptionEvent, DisruptionKind
from aerodue.domain.coverage import UserCoverageProfile


def evaluate_dot_delay(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
) -> list[ClaimRecommendation]:
    if not profile.dot_applicable:
        return []
    if event.kind not in (DisruptionKind.DELAY, DisruptionKind.CANCELLATION):
        return []

    delay = event.delay_minutes or 0
    claims: list[ClaimRecommendation] = []

    if event.kind == DisruptionKind.CANCELLATION:
        claims.append(
            ClaimRecommendation(
                source=CompensationSource.REGULATION_DOT,
                title="DOT refund for cancelled flight",
                summary=(
                    "For cancellations within carrier control, you may be entitled to a "
                    "refund to original form of payment (DOT enforcement guidance)."
                ),
                confidence=0.85,
                citation_ids=["dot-refund-cancel-2024"],
                action_steps=[
                    "Request full refund via airline app or agent",
                    "If denied, file DOT aviation consumer complaint",
                ],
            )
        )

    if delay >= 180:
        claims.append(
            ClaimRecommendation(
                source=CompensationSource.REGULATION_DOT,
                title="Significant tarmac / delay accommodations",
                summary=(
                    "Extended delays may trigger meal vouchers, rebooking, or hotel per "
                    "carrier customer service plan — check operating carrier's CS plan."
                ),
                estimated_amount_usd=None,
                confidence=0.7,
                citation_ids=["dot-cs-plan-delay"],
                action_steps=["Ask gate agent for meal/hotel voucher", "Document expenses"],
            )
        )

    return claims
