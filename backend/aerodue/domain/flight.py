from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field


class FlightStatus(str, Enum):
    SCHEDULED = "scheduled"
    BOARDING = "boarding"
    DEPARTED = "departed"
    ARRIVED = "arrived"
    DELAYED = "delayed"
    CANCELLED = "cancelled"
    DIVERTED = "diverted"


class FlightLeg(BaseModel):
    """Single segment as matched from GPS + schedule data."""

    carrier_iata: str = Field(..., description="Operating carrier, e.g. UA")
    flight_number: str
    departure_airport: str = Field(..., min_length=3, max_length=4)
    arrival_airport: str = Field(..., min_length=3, max_length=4)
    scheduled_departure: datetime
    scheduled_arrival: datetime
    actual_departure: datetime | None = None
    actual_arrival: datetime | None = None
    status: FlightStatus = FlightStatus.SCHEDULED
    booking_reference: str | None = None


class Trip(BaseModel):
    """Ordered legs for one journey."""

    trip_id: str
    legs: list[FlightLeg]
    passenger_count: int = 1

    @property
    def is_international(self) -> bool:
        if not self.legs:
            return False
        first = self.legs[0].departure_airport
        last = self.legs[-1].arrival_airport
        return first[:2] != last[:2]  # simplified; replace with airport country DB
