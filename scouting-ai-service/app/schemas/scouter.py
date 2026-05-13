from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field


class ScouterPlayerCard(BaseModel):
    player_external_id: int
    full_name: str
    position: str | None
    age: int | None
    division_name: str | None
    potential_score: float
    churn_risk: float
    trend_label: Literal["progression", "stabilite", "regression"]
    avg_rating: float
    goals_per_match: float
    assists_per_match: float
    attendance_ratio: float
    shortlist_score: float | None = None


class SearchResponse(BaseModel):
    total: int
    items: list[ScouterPlayerCard]


class CompareRequest(BaseModel):
    player_external_ids: list[int] = Field(min_length=2, max_length=20)


class CompareResponse(BaseModel):
    players: list[ScouterPlayerCard]
    highlights: dict[str, int]


class ShortlistGenerateRequest(BaseModel):
    title: str = "Shortlist Scouting"
    strategy: Literal["balanced", "high_potential", "low_risk", "breakthrough"] = "balanced"
    q: str | None = None
    position: str | None = None
    age_min: int | None = Field(default=None, ge=5, le=60)
    age_max: int | None = Field(default=None, ge=5, le=60)
    min_potential: float | None = Field(default=None, ge=0.0, le=100.0)
    max_churn: float | None = Field(default=None, ge=0.0, le=1.0)
    trend_label: Literal["progression", "stabilite", "regression"] | None = None
    min_avg_rating: float | None = Field(default=None, ge=0.0, le=10.0)
    top_n: int = Field(default=15, ge=1, le=100)


class ShortlistResponse(BaseModel):
    title: str
    strategy: str
    generated_at: datetime
    total: int
    players: list[ScouterPlayerCard]


{'='*80}
