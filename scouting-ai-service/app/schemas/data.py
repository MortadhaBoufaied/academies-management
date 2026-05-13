from datetime import date

from pydantic import BaseModel, Field


class PlayerUpsert(BaseModel):
    external_id: int = Field(gt=0)
    full_name: str = Field(min_length=2, max_length=150)
    position: str | None = None
    age: int | None = Field(default=None, ge=5, le=60)
    nationality: str | None = None
    division_name: str | None = None
    trainer_id: int | None = Field(default=None, gt=0)
    is_paid: bool = True
    goals: int = Field(default=0, ge=0)
    assists: int = Field(default=0, ge=0)
    matches: int = Field(default=0, ge=0)
    average_rating: float = Field(default=0.0, ge=0.0, le=10.0)


class ObservationUpsert(BaseModel):
    player_external_id: int = Field(gt=0)
    observed_on: date
    goals: int = Field(default=0, ge=0)
    assists: int = Field(default=0, ge=0)
    matches_played: int = Field(default=1, ge=0)
    average_rating: float = Field(default=0.0, ge=0.0, le=10.0)
    minutes_played: int = Field(default=0, ge=0)
    training_attendance: float = Field(default=1.0, ge=0.0, le=1.0)
    injury_days: int = Field(default=0, ge=0)
    notes: str | None = None


class PaymentUpsert(BaseModel):
    player_external_id: int = Field(gt=0)
    month: date
    amount: float = Field(default=0.0, ge=0.0)
    is_paid: bool = False


class UpsertResult(BaseModel):
    inserted: int
    updated: int


class SyncRequest(BaseModel):
    base_url: str | None = None
    include_players: bool = True
    include_payments: bool = True
    create_observation_snapshot: bool = True


class SyncResult(BaseModel):
    players_upserted: int
    observations_upserted: int
    payments_upserted: int
    warnings: list[str]


class FeatureSnapshot(BaseModel):
    observations_count: int
    avg_rating: float
    rating_std: float
    goals_per_match: float
    assists_per_match: float
    performance_index: float
    performance_slope: float
    rating_slope: float
    attendance_ratio: float
    unpaid_ratio: float
    injury_days_avg: float
    days_since_last_activity: int


class DataFeatureResponse(BaseModel):
    player_external_id: int
    full_name: str
    position: str | None
    age: int | None
    feature_snapshot: FeatureSnapshot


{'='*80}
