import json
from pathlib import Path

import yaml

from aerodue.domain.claim import ClaimRecommendation
from aerodue.domain.coverage import UserCoverageProfile
from aerodue.domain.disruption import DisruptionEvent


def load_model_config() -> dict:
    path = Path(__file__).parent / "models.yaml"
    with path.open() as f:
        return yaml.safe_load(f)


def build_rationale_prompt(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
    claims: list[ClaimRecommendation],
    excerpts: str = "",
) -> str:
    """Format prompt for on-device LLM; no network call here."""
    cfg = load_model_config()
    template = cfg["prompts"]["claim_rationale"]
    return template.format(
        disruption_json=event.model_dump_json(),
        coverage_json=profile.model_dump_json(),
        claims_json=json.dumps([c.model_dump() for c in claims]),
        excerpts=excerpts or "(no excerpts loaded — bundle regulation snippets in APK)",
    )


def explain_offline(
    event: DisruptionEvent,
    profile: UserCoverageProfile,
    claims: list[ClaimRecommendation],
) -> str:
    """
    Stub when LLM runtime is not wired. Android will call native inference later.
    """
    if not claims:
        return "No eligible compensation paths found for this disruption with your current coverage."
    lines = [f"• {c.title}: {c.summary}" for c in claims[:5]]
    return "Offline rule assessment complete.\n\n" + "\n".join(lines)
