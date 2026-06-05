"""JSON entry points for host tests / tooling. On-device Android uses Kotlin core, not this module."""

from __future__ import annotations

import json
from typing import Any

from aerodue.core.compensation.engine import assess_compensation
from aerodue.domain.coverage import UserCoverageProfile
from aerodue.domain.disruption import DisruptionEvent
from aerodue.inference.rationale import build_rationale_prompt, explain_offline


def assess_disruption_json(event_json: str, profile_json: str) -> str:
    event = DisruptionEvent.model_validate(json.loads(event_json))
    profile = UserCoverageProfile.model_validate(json.loads(profile_json))
    claims = assess_compensation(event, profile)
    return json.dumps([c.model_dump(mode="json") for c in claims], default=str)


def explain_claim_json(event_json: str, profile_json: str, claims_json: str) -> str:
    event = DisruptionEvent.model_validate(json.loads(event_json))
    profile = UserCoverageProfile.model_validate(json.loads(profile_json))
    raw_claims: list[dict[str, Any]] = json.loads(claims_json)
    from aerodue.domain.claim import ClaimRecommendation

    claims = [ClaimRecommendation.model_validate(c) for c in raw_claims]
    return explain_offline(event, profile, claims)


def build_prompt_json(event_json: str, profile_json: str, claims_json: str) -> str:
    event = DisruptionEvent.model_validate(json.loads(event_json))
    profile = UserCoverageProfile.model_validate(json.loads(profile_json))
    raw_claims: list[dict[str, Any]] = json.loads(claims_json)
    from aerodue.domain.claim import ClaimRecommendation

    claims = [ClaimRecommendation.model_validate(c) for c in raw_claims]
    return build_rationale_prompt(event, profile, claims)
