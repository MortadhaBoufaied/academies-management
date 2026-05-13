from __future__ import annotations

from datetime import datetime
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.models import (
    MatchEvent, PlayerAttributeSnapshot, PlayerProfile, ScoutingReport,
    ScoutingStatus, TalentScore, VideoAnalysis, VideoAnalysisStatus, VideoAsset,
)
from app.schemas.scouting_architecture import MatchEventCreate, ScoutingReportCreate, SnapshotCreate, VideoAssetCreate


def _player_by_external_id(db: Session, external_id: int) -> PlayerProfile:
    player = db.scalar(select(PlayerProfile).where(PlayerProfile.external_id == external_id))
    if player is None:
        raise ValueError(f"Player not found: {external_id}")
    return player


def create_report(db: Session, payload: ScoutingReportCreate) -> ScoutingReport:
    player = _player_by_external_id(db, payload.player_external_id)
    report = ScoutingReport(
        player_id=player.id,
        scouter_id=payload.scouter_id,
        academy_id=payload.academy_id,
        match_id=payload.match_id,
        technical_score=payload.technical_score,
        tactical_score=payload.tactical_score,
        physical_score=payload.physical_score,
        mental_score=payload.mental_score,
        potential_score=payload.potential_score,
        style_fit_score=payload.style_fit_score,
        recommendation=payload.recommendation,
        notes=payload.notes,
        status=ScoutingStatus.DRAFT,
    )
    db.add(report)
    db.commit()
    db.refresh(report)
    return report


def approve_report(db: Session, report_id: int) -> ScoutingReport:
    report = db.get(ScoutingReport, report_id)
    if report is None:
        raise ValueError("Scouting report not found")
    report.status = ScoutingStatus.APPROVED
    report.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(report)
    return report


def shortlist_report(db: Session, report_id: int) -> ScoutingReport:
    report = db.get(ScoutingReport, report_id)
    if report is None:
        raise ValueError("Scouting report not found")
    report.status = ScoutingStatus.SHORTLISTED
    report.updated_at = datetime.utcnow()
    db.commit()
    db.refresh(report)
    return report


def add_match_event(db: Session, payload: MatchEventCreate) -> MatchEvent:
    player = _player_by_external_id(db, payload.player_external_id)
    event = MatchEvent(
        match_id=payload.match_id,
        player_id=player.id,
        event_type=payload.event_type,
        minute=payload.minute,
        second=payload.second,
        x_position=payload.x_position,
        y_position=payload.y_position,
        success=payload.success,
        pressure_level=payload.pressure_level,
        intensity=payload.intensity,
        note=payload.note,
    )
    db.add(event)
    db.commit()
    db.refresh(event)
    return event


def match_timeline(db: Session, match_id: int) -> list[MatchEvent]:
    return list(db.scalars(select(MatchEvent).where(MatchEvent.match_id == match_id).order_by(MatchEvent.minute.asc(), MatchEvent.second.asc())))


def build_snapshot(db: Session, payload: SnapshotCreate) -> PlayerAttributeSnapshot:
    player = _player_by_external_id(db, payload.player_external_id)
    snapshot = PlayerAttributeSnapshot(player_id=player.id, **payload.model_dump(exclude={"player_external_id"}))
    db.add(snapshot)
    db.commit()
    db.refresh(snapshot)
    return snapshot


def generate_talent_score(db: Session, player_external_id: int) -> TalentScore:
    player = _player_by_external_id(db, player_external_id)
    reports = list(db.scalars(select(ScoutingReport).where(ScoutingReport.player_id == player.id)))
    report_component = sum(r.calculate_overall_score() for r in reports) / len(reports) if reports else player.average_rating * 10
    legacy_component = min(100.0, (player.average_rating or 0.0) * 10 + player.goals * 1.5 + player.assists)
    score = round((report_component * 0.65) + (legacy_component * 0.35), 2)
    confidence = round(min(0.95, 0.35 + 0.1 * len(reports)), 2)
    risk_level = "LOW" if score >= 75 else "MEDIUM" if score >= 55 else "HIGH"
    talent = TalentScore(player_id=player.id, score=score, confidence=confidence, risk_level=risk_level)
    db.add(talent)
    db.commit()
    db.refresh(talent)
    return talent


def create_video_asset(db: Session, payload: VideoAssetCreate) -> VideoAsset:
    player_id = None
    if payload.player_external_id is not None:
        player_id = _player_by_external_id(db, payload.player_external_id).id
    asset = VideoAsset(match_id=payload.match_id, player_id=player_id, video_url=payload.video_url, duration_seconds=payload.duration_seconds, resolution=payload.resolution)
    db.add(asset)
    db.commit()
    db.refresh(asset)
    return asset


def start_video_analysis(db: Session, video_asset_id: int) -> VideoAnalysis:
    if db.get(VideoAsset, video_asset_id) is None:
        raise ValueError("Video asset not found")
    analysis = VideoAnalysis(video_asset_id=video_asset_id, status=VideoAnalysisStatus.PROCESSING, detected_events=0, analysis_score=0.0)
    db.add(analysis)
    db.commit()
    db.refresh(analysis)
    return analysis
