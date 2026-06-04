from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field

from aerodue.domain.flight import FlightLeg


class DisruptionKind(str, Enum):
    DELAY = "delay"
    CANCELLATION = "cancellation"
    MISSED_CONNECTION = "missed_connection"
    DENIED_BOARDING = "denied_boarding"
    BAGGAGE_DELAY = "baggage_delay"
    SCHEDULE_CHANGE = "schedule_change"


class DisruptionEvent(BaseModel):
    """Observed or inferred disruption tied to a leg."""

    kind: DisruptionKind
    leg: FlightLeg
    detected_at: datetime
    delay_minutes: int | None = None
    extraordinary_circumstance_claimed: bool = False
    notes: str | None = None
