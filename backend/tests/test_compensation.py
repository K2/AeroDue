import json
from pathlib import Path

from aerodue.core.compensation.engine import assess_compensation
from aerodue.domain.coverage import UserCoverageProfile
from aerodue.domain.disruption import DisruptionEvent


FIXTURE = Path(__file__).resolve().parents[1] / "samples" / "delayed_connection.json"


def test_delayed_connection_finds_multiple_sources():
    data = json.loads(FIXTURE.read_text())
    event = DisruptionEvent.model_validate(data["event"])
    profile = UserCoverageProfile.model_validate(data["profile"])
    claims = assess_compensation(event, profile)
    sources = {c.source.value for c in claims}
    assert "regulation_dot" in sources
    assert "credit_card" in sources
    assert "business_policy" in sources
