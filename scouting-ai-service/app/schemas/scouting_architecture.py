from datetime import date, datetime
from pydantic import BaseModel, Field
from app.db.models import MatchEventType, ScoutingStatus, VideoAnalysisStatus

class ScoutingReportCreate(BaseModel):
    player_external_id: int = Field(gt=0)
    scouter_id: int | None = None
    academy_id: int | None = None
    match_id: int | None = None
    technical_score: float = Field(default=0.0, ge=0, le=100)
    tactical_score: float = Field(default=0.0, ge=0, le=100)
    physical_score: float = Field(default=0.0, ge=0, le=100)
    mental_score: float = Field(default=0.0, ge=0, le=100)
    potential_score: float = Field(default=0.0, ge=0, le=100)
    style_fit_score: float = Field(default=0.0, ge=0, le=100)
    recommendation: str | None = None
    notes: str | None = None

class ScoutingReportResponse(BaseModel):
    id: int
    player_external_id: int
    overall_score: float
    status: ScoutingStatus
    recommendation: str | None
    created_at: datetime

class MatchEventCreate(BaseModel):
    match_id: int
    player_external_id: int = Field(gt=0)
    event_type: MatchEventType
    minute: int | None = Field(default=None, ge=0, le=130)
    second: int | None = Field(default=None, ge=0, le=59)
    x_position: float | None = Field(default=None, ge=0, le=100)
    y_position: float | None = Field(default=None, ge=0, le=100)
    success: bool = True
    pressure_level: str | None = None
    intensity: int | None = Field(default=None, ge=0, le=10)
    note: str | None = None

class MatchEventResponse(BaseModel):
    id: int
    match_id: int
    player_external_id: int
    event_type: MatchEventType
    minute: int | None
    second: int | None
    success: bool

class SnapshotCreate(BaseModel):
    player_external_id: int = Field(gt=0)
    match_id: int | None = None
    speed: float | None = Field(default=None, ge=0, le=100)
    acceleration: float | None = Field(default=None, ge=0, le=100)
    agility: float | None = Field(default=None, ge=0, le=100)
    stamina: float | None = Field(default=None, ge=0, le=100)
    strength: float | None = Field(default=None, ge=0, le=100)
    positioning: float | None = Field(default=None, ge=0, le=100)
    decision_making: float | None = Field(default=None, ge=0, le=100)
    vision: float | None = Field(default=None, ge=0, le=100)
    off_ball_movement: float | None = Field(default=None, ge=0, le=100)
    composure: float | None = Field(default=None, ge=0, le=100)

class TalentScoreResponse(BaseModel):
    player_external_id: int
    score: float
    confidence: float
    risk_level: str
    generated_at: datetime

class VideoAssetCreate(BaseModel):
    match_id: int | None = None
    player_external_id: int | None = None
    video_url: str
    duration_seconds: int | None = Field(default=None, ge=0)
    resolution: str | None = None

class VideoAnalysisResponse(BaseModel):
    id: int
    video_asset_id: int
    status: VideoAnalysisStatus
    detected_events: int
    heatmap_url: str | None
    highlight_url: str | None
    analysis_score: float
