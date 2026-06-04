from dataclasses import dataclass
from datetime import datetime


@dataclass(frozen=True)
class GeoFix:
    latitude: float
    longitude: float
    accuracy_m: float
    recorded_at: datetime


def nearest_airport_icao(fix: GeoFix) -> str | None:
    """
    Resolve ICAO/IATA from coordinates.

    Production: use bundled airport index (OurAirports) + geohash lookup.
    """
    # Placeholder — JFK bbox stub for dev
    if 40.6 <= fix.latitude <= 40.7 and -73.9 <= fix.longitude <= -73.7:
        return "JFK"
    if 37.6 <= fix.latitude <= 37.7 and -122.4 <= fix.longitude <= -122.3:
        return "SFO"
    return None


def infer_on_ground_at_airport(fix: GeoFix, airport_icao: str, radius_km: float = 5.0) -> bool:
    """True when fix is within geofence of airport (simplified)."""
    resolved = nearest_airport_icao(fix)
    return resolved == airport_icao
