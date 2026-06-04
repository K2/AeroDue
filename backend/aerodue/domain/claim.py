from enum import Enum

from pydantic import BaseModel, Field


class CompensationSource(str, Enum):
    REGULATION_DOT = "regulation_dot"
    REGULATION_EU261 = "regulation_eu261"
    CREDIT_CARD = "credit_card"
    BUSINESS_POLICY = "business_policy"
    AIRLINE_GOODWILL = "airline_goodwill"
    CARRIER_CONTRACT = "carrier_contract"


class ClaimRecommendation(BaseModel):
    source: CompensationSource
    title: str
    summary: str
    estimated_amount_usd: float | None = None
    currency: str = "USD"
    confidence: float = Field(..., ge=0.0, le=1.0)
    citation_ids: list[str] = Field(default_factory=list)
    action_steps: list[str] = Field(default_factory=list)
