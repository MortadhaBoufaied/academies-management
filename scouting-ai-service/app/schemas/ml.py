from datetime import date
from typing import Literal

from pydantic import BaseModel

from app.schemas.data import FeatureSnapshot


class FactorExplanation(BaseModel):
    factor: str
    value: float
    weight: float
    contribution_points: float


class PotentialResponse(BaseModel):
    player_external_id: int
    potential_score: float
    level: Literal["elite", "prometteur", "a_developper"]
    factors: list[FactorExplanation]
    feature_snapshot: FeatureSnapshot


class EvolutionPoint(BaseModel):
    observed_on: date
    average_rating: float
    performance_index: float


class EvolutionResponse(BaseModel):
    player_external_id: int
    trend_label: Literal["progression", "stabilite", "regression"]
    confidence: float
    performance_slope: float
    rating_slope: float
    details: dict[str, float]
    timeline: list[EvolutionPoint]


class ChurnResponse(BaseModel):
    player_external_id: int
    risk_score: float
    risk_level: Literal["faible", "moyen", "eleve"]
    probable_reasons: list[str]
    recommended_actions: list[str]
    feature_snapshot: FeatureSnapshot


{'='*80}
