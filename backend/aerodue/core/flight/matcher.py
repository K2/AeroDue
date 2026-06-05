from datetime import datetime, timedelta

from aerodue.core.location.gps import GeoFix, nearest_airport_icao
from aerodue.domain.flight import FlightLeg, FlightStatus, Trip


def match_trip_from_location(
    fix: GeoFix,
    candidate_legs: list[FlightLeg],
    window_hours: int = 18,
) -> Trip | None:
    """
    Pick the most likely active leg using airport proximity and schedule window.
    """
    airport = nearest_airport_icao(fix)
    if not airport:
        return None

    now = fix.recorded_at
    window = timedelta(hours=window_hours)
    matches: list[FlightLeg] = []

    for leg in candidate_legs:
        dep_near = leg.departure_airport == airport
        arr_near = leg.arrival_airport == airport
        in_window = (
            leg.scheduled_departure - window <= now <= leg.scheduled_arrival + window
        )
        if in_window and (dep_near or arr_near):
            matches.append(leg)

    if not matches:
        return None

    matches.sort(key=lambda l: abs((l.scheduled_departure - now).total_seconds()))
    return Trip(trip_id=f"auto-{matches[0].flight_number}", legs=matches)


def classify_delay_minutes(leg: FlightLeg) -> int | None:
    if leg.actual_departure is None or leg.status not in (
        FlightStatus.DELAYED,
        FlightStatus.DEPARTED,
        FlightStatus.ARRIVED,
    ):
        return None
    delta = leg.actual_departure - leg.scheduled_departure
    return max(0, int(delta.total_seconds() // 60))
