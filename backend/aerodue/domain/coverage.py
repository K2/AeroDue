from pydantic import BaseModel, Field


class CreditCardCoverage(BaseModel):
    issuer: str
    product_name: str
    trip_delay_hours_threshold: int | None = 6
    cancellation: bool = True
    baggage_delay: bool = False
    max_claim_usd: float | None = None


class BusinessTravelPolicy(BaseModel):
    employer: str
    policy_id: str | None = None
    requires_manager_approval: bool = False
    reimburses_hotels_on_overnight_delay: bool = True
    max_lodging_per_night_usd: float | None = None


class AirlineStatus(BaseModel):
    program: str = Field(..., description="e.g. United MileagePlus")
    tier: str | None = None
    oneworld_star_alliance: str | None = None


class UserCoverageProfile(BaseModel):
    user_id: str
    home_airport: str | None = None
    credit_cards: list[CreditCardCoverage] = Field(default_factory=list)
    business_policy: BusinessTravelPolicy | None = None
    airline_status: list[AirlineStatus] = Field(default_factory=list)
    eu261_applicable: bool = False
    dot_applicable: bool = True
