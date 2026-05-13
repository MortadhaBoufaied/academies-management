from __future__ import annotations

from datetime import date, datetime
from enum import Enum as PyEnum

from sqlalchemy import Boolean, Date, DateTime, Enum, Float, ForeignKey, Integer, String, Text, UniqueConstraint
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship

class Base(DeclarativeBase):
    pass

# =========================
# Enumerations
# =========================
class UserRole(str, PyEnum):
    SUPER_ADMIN = "SUPER_ADMIN"
    ADMIN = "ADMIN"
    PLAYER = "PLAYER"
    TRAINER = "TRAINER"
    PARENT = "PARENT"
    SCOUTER = "SCOUTER"

class AcademyStatus(str, PyEnum):
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    SUSPENDED = "SUSPENDED"

class PaymentStatusEnum(str, PyEnum):
    PENDING = "PENDING"
    PAID = "PAID"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    REFUNDED = "REFUNDED"

class ActivityType(str, PyEnum):
    TRAINING = "TRAINING"
    MATCH = "MATCH"
    OTHER = "OTHER"

class NotificationCategory(str, PyEnum):
    GENERAL = "GENERAL"
    PAYMENT = "PAYMENT"
    MESSAGE = "MESSAGE"
    ACTIVITY = "ACTIVITY"
    SCOUTING = "SCOUTING"

class ConversationType(str, PyEnum):
    DIRECT = "DIRECT"
    GROUP = "GROUP"
    DIVISION = "DIVISION"

class MatchEventType(str, PyEnum):
    PASS = "PASS"
    SHOT = "SHOT"
    DRIBBLE = "DRIBBLE"
    TACKLE = "TACKLE"
    INTERCEPTION = "INTERCEPTION"
    RECOVERY = "RECOVERY"
    PRESS = "PRESS"
    FOUL = "FOUL"
    SAVE = "SAVE"
    CORNER = "CORNER"
    CROSS = "CROSS"

class ScoutingStatus(str, PyEnum):
    DRAFT = "DRAFT"
    IN_REVIEW = "IN_REVIEW"
    SHORTLISTED = "SHORTLISTED"
    REJECTED = "REJECTED"
    APPROVED = "APPROVED"

class VideoAnalysisStatus(str, PyEnum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"

# =========================
# Legacy AI data tables kept compatible with existing services
# =========================
class PlayerProfile(Base):
    __tablename__ = "player_profiles"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    external_id: Mapped[int] = mapped_column(Integer, unique=True, index=True)
    full_name: Mapped[str] = mapped_column(String(150), index=True)
    position: Mapped[str | None] = mapped_column(String(50), nullable=True)
    age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    nationality: Mapped[str | None] = mapped_column(String(80), nullable=True)
    division_name: Mapped[str | None] = mapped_column(String(120), nullable=True)
    trainer_id: Mapped[int | None] = mapped_column(Integer, nullable=True)
    is_paid: Mapped[bool] = mapped_column(Boolean, default=True)
    goals: Mapped[int] = mapped_column(Integer, default=0)
    assists: Mapped[int] = mapped_column(Integer, default=0)
    matches: Mapped[int] = mapped_column(Integer, default=0)
    average_rating: Mapped[float] = mapped_column(Float, default=0.0)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

class PerformanceObservation(Base):
    __tablename__ = "performance_observations"
    __table_args__ = (UniqueConstraint("player_id", "observed_on", name="uq_observation_player_date"),)
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    observed_on: Mapped[date] = mapped_column(Date)
    goals: Mapped[int] = mapped_column(Integer, default=0)
    assists: Mapped[int] = mapped_column(Integer, default=0)
    matches_played: Mapped[int] = mapped_column(Integer, default=1)
    average_rating: Mapped[float] = mapped_column(Float, default=0.0)
    minutes_played: Mapped[int] = mapped_column(Integer, default=0)
    training_attendance: Mapped[float] = mapped_column(Float, default=1.0)
    injury_days: Mapped[int] = mapped_column(Integer, default=0)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)
    player: Mapped[PlayerProfile] = relationship()

class PaymentStatus(Base):
    __tablename__ = "payment_statuses"
    __table_args__ = (UniqueConstraint("player_id", "month", name="uq_payment_player_month"),)
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    month: Mapped[date] = mapped_column(Date)
    amount: Mapped[float] = mapped_column(Float, default=0.0)
    is_paid: Mapped[bool] = mapped_column(Boolean, default=False)
    player: Mapped[PlayerProfile] = relationship()

# =========================
# Updated UML architecture tables for full scouting
# =========================
class Academy(Base):
    __tablename__ = "academies"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(255), index=True)
    slug: Mapped[str] = mapped_column(String(255), unique=True, index=True)
    email: Mapped[str | None] = mapped_column(String(255), nullable=True)
    phone: Mapped[str | None] = mapped_column(String(80), nullable=True)
    address: Mapped[str | None] = mapped_column(String(255), nullable=True)
    city: Mapped[str | None] = mapped_column(String(120), nullable=True)
    country: Mapped[str | None] = mapped_column(String(120), nullable=True)
    status: Mapped[AcademyStatus] = mapped_column(Enum(AcademyStatus), default=AcademyStatus.ACTIVE)
    subscription_offer: Mapped[str | None] = mapped_column(String(40), nullable=True)
    subscription_payment_status: Mapped[str | None] = mapped_column(String(40), nullable=True)
    users: Mapped[list["User"]] = relationship(back_populates="academy", cascade="all, delete-orphan")


class User(Base):
    """System user with role-based access control."""
    __tablename__ = "users"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    email: Mapped[str] = mapped_column(String(255), unique=True, index=True, nullable=False)
    full_name: Mapped[str] = mapped_column(String(255), nullable=False)
    hashed_password: Mapped[str] = mapped_column(String(255), nullable=False)
    role: Mapped[UserRole] = mapped_column(Enum(UserRole), nullable=False, index=True)
    academy_id: Mapped[int | None] = mapped_column(ForeignKey("academies.id"), nullable=True, index=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)
    last_login: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    academy: Mapped["Academy | None"] = relationship(back_populates="users")


class Division(Base):
    __tablename__ = "divisions"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    academy_id: Mapped[int | None] = mapped_column(ForeignKey("academies.id"), nullable=True, index=True)
    nom: Mapped[str] = mapped_column(String(255), index=True)
    categorie: Mapped[str | None] = mapped_column(String(120), nullable=True)
    min_age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    max_age: Mapped[int | None] = mapped_column(Integer, nullable=True)
    gender: Mapped[str | None] = mapped_column(String(40), nullable=True)
    level: Mapped[str | None] = mapped_column(String(80), nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

class ScouterProfile(Base):
    __tablename__ = "scouters"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int | None] = mapped_column(Integer, nullable=True, index=True)
    academy_id: Mapped[int | None] = mapped_column(ForeignKey("academies.id"), nullable=True, index=True)
    region: Mapped[str | None] = mapped_column(String(120), nullable=True)
    speciality: Mapped[str | None] = mapped_column(String(120), nullable=True)
    experience_level: Mapped[str | None] = mapped_column(String(120), nullable=True)
    active: Mapped[bool] = mapped_column(Boolean, default=True)

class ScoutingAssignment(Base):
    __tablename__ = "scouting_assignments"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    academy_id: Mapped[int | None] = mapped_column(ForeignKey("academies.id"), nullable=True, index=True)
    scouter_id: Mapped[int | None] = mapped_column(ForeignKey("scouters.id"), nullable=True, index=True)
    division_id: Mapped[int | None] = mapped_column(ForeignKey("divisions.id"), nullable=True, index=True)
    assigned_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    status: Mapped[ScoutingStatus] = mapped_column(Enum(ScoutingStatus), default=ScoutingStatus.IN_REVIEW)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)

class ScoutingReport(Base):
    __tablename__ = "scouting_reports"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    scouter_id: Mapped[int | None] = mapped_column(ForeignKey("scouters.id"), nullable=True, index=True)
    academy_id: Mapped[int | None] = mapped_column(ForeignKey("academies.id"), nullable=True, index=True)
    match_id: Mapped[int | None] = mapped_column(Integer, nullable=True, index=True)
    technical_score: Mapped[float] = mapped_column(Float, default=0.0)
    tactical_score: Mapped[float] = mapped_column(Float, default=0.0)
    physical_score: Mapped[float] = mapped_column(Float, default=0.0)
    mental_score: Mapped[float] = mapped_column(Float, default=0.0)
    potential_score: Mapped[float] = mapped_column(Float, default=0.0)
    style_fit_score: Mapped[float] = mapped_column(Float, default=0.0)
    recommendation: Mapped[str | None] = mapped_column(String(500), nullable=True)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)
    status: Mapped[ScoutingStatus] = mapped_column(Enum(ScoutingStatus), default=ScoutingStatus.DRAFT)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    updated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    player: Mapped[PlayerProfile] = relationship()

    def calculate_overall_score(self) -> float:
        values = [self.technical_score, self.tactical_score, self.physical_score, self.mental_score, self.potential_score, self.style_fit_score]
        return round(sum(values) / len(values), 2)

class ScoutingCriterion(Base):
    __tablename__ = "scouting_criteria"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(255))
    domain: Mapped[str | None] = mapped_column(String(80), nullable=True)
    weight: Mapped[float] = mapped_column(Float, default=1.0)
    description: Mapped[str | None] = mapped_column(Text, nullable=True)
    active: Mapped[bool] = mapped_column(Boolean, default=True)

class ScoutingCriterionScore(Base):
    __tablename__ = "scouting_criterion_scores"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    report_id: Mapped[int] = mapped_column(ForeignKey("scouting_reports.id"), index=True)
    criterion_id: Mapped[int] = mapped_column(ForeignKey("scouting_criteria.id"), index=True)
    score: Mapped[float] = mapped_column(Float, default=0.0)
    comment: Mapped[str | None] = mapped_column(Text, nullable=True)
    criterion: Mapped[ScoutingCriterion] = relationship()

class MatchEvent(Base):
    __tablename__ = "match_events"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    match_id: Mapped[int] = mapped_column(Integer, index=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    event_type: Mapped[MatchEventType] = mapped_column(Enum(MatchEventType), index=True)
    minute: Mapped[int | None] = mapped_column(Integer, nullable=True)
    second: Mapped[int | None] = mapped_column(Integer, nullable=True)
    x_position: Mapped[float | None] = mapped_column(Float, nullable=True)
    y_position: Mapped[float | None] = mapped_column(Float, nullable=True)
    success: Mapped[bool] = mapped_column(Boolean, default=True)
    pressure_level: Mapped[str | None] = mapped_column(String(80), nullable=True)
    intensity: Mapped[int | None] = mapped_column(Integer, nullable=True)
    note: Mapped[str | None] = mapped_column(Text, nullable=True)

class PlayerAttributeSnapshot(Base):
    __tablename__ = "player_attribute_snapshots"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    match_id: Mapped[int | None] = mapped_column(Integer, nullable=True, index=True)
    captured_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    speed: Mapped[float | None] = mapped_column(Float, nullable=True)
    acceleration: Mapped[float | None] = mapped_column(Float, nullable=True)
    agility: Mapped[float | None] = mapped_column(Float, nullable=True)
    stamina: Mapped[float | None] = mapped_column(Float, nullable=True)
    strength: Mapped[float | None] = mapped_column(Float, nullable=True)
    positioning: Mapped[float | None] = mapped_column(Float, nullable=True)
    decision_making: Mapped[float | None] = mapped_column(Float, nullable=True)
    vision: Mapped[float | None] = mapped_column(Float, nullable=True)
    off_ball_movement: Mapped[float | None] = mapped_column(Float, nullable=True)
    composure: Mapped[float | None] = mapped_column(Float, nullable=True)

class PlayerProgression(Base):
    __tablename__ = "player_progressions"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    metric_name: Mapped[str] = mapped_column(String(120))
    old_value: Mapped[float | None] = mapped_column(Float, nullable=True)
    new_value: Mapped[float | None] = mapped_column(Float, nullable=True)
    change_rate: Mapped[float | None] = mapped_column(Float, nullable=True)
    recorded_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

class VideoAsset(Base):
    __tablename__ = "video_assets"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    match_id: Mapped[int | None] = mapped_column(Integer, nullable=True, index=True)
    player_id: Mapped[int | None] = mapped_column(ForeignKey("player_profiles.id"), nullable=True, index=True)
    video_url: Mapped[str] = mapped_column(String(1000))
    duration_seconds: Mapped[int | None] = mapped_column(Integer, nullable=True)
    resolution: Mapped[str | None] = mapped_column(String(80), nullable=True)
    uploaded_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

class VideoAnalysis(Base):
    __tablename__ = "video_analyses"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    video_asset_id: Mapped[int] = mapped_column(ForeignKey("video_assets.id"), index=True)
    status: Mapped[VideoAnalysisStatus] = mapped_column(Enum(VideoAnalysisStatus), default=VideoAnalysisStatus.PENDING)
    detected_events: Mapped[int] = mapped_column(Integer, default=0)
    heatmap_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    highlight_url: Mapped[str | None] = mapped_column(String(1000), nullable=True)
    analysis_score: Mapped[float] = mapped_column(Float, default=0.0)
    completed_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)

class TalentScore(Base):
    __tablename__ = "talent_scores"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    score: Mapped[float] = mapped_column(Float, default=0.0)
    confidence: Mapped[float] = mapped_column(Float, default=0.0)
    risk_level: Mapped[str | None] = mapped_column(String(80), nullable=True)
    generated_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)

class InjuryRecord(Base):
    __tablename__ = "injury_records"
    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    player_id: Mapped[int] = mapped_column(ForeignKey("player_profiles.id"), index=True)
    injury_type: Mapped[str | None] = mapped_column(String(120), nullable=True)
    severity: Mapped[str | None] = mapped_column(String(80), nullable=True)
    start_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    end_date: Mapped[date | None] = mapped_column(Date, nullable=True)
    recovered: Mapped[bool] = mapped_column(Boolean, default=False)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)
