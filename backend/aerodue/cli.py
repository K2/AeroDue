import argparse
import json
from pathlib import Path

from aerodue.core.compensation.engine import assess_compensation
from aerodue.domain.coverage import (
    CreditCardCoverage,
    UserCoverageProfile,
)
from aerodue.domain.disruption import DisruptionEvent
from aerodue.inference.rationale import explain_offline


def main() -> None:
    parser = argparse.ArgumentParser(prog="aerodue")
    sub = parser.add_subparsers(dest="command", required=True)

    assess = sub.add_parser("assess", help="Run compensation assessment on a fixture JSON")
    assess.add_argument("--fixture", type=Path, required=True)

    args = parser.parse_args()
    if args.command == "assess":
        _cmd_assess(args.fixture)


def _cmd_assess(fixture: Path) -> None:
    data = json.loads(fixture.read_text())
    event = DisruptionEvent.model_validate(data["event"])
    profile = UserCoverageProfile.model_validate(data.get("profile", {"user_id": "demo"}))
    claims = assess_compensation(event, profile)
    print(json.dumps([c.model_dump() for c in claims], indent=2, default=str))
    print("\n--- passenger summary ---\n")
    print(explain_offline(event, profile, claims))


if __name__ == "__main__":
    main()
