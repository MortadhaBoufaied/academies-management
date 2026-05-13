from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.db.models import MatchEvent, ScoutingReport
from app.db.session import get_db
from app.schemas.scouting_architecture import (
    MatchEventCreate, MatchEventResponse, ScoutingReportCreate, ScoutingReportResponse,
    SnapshotCreate, TalentScoreResponse, VideoAnalysisResponse, VideoAssetCreate,
)
from app.services.scouting_architecture_service import (
    add_match_event, approve_report, build_snapshot, create_report, create_video_asset,
    generate_talent_score, match_timeline, shortlist_report, start_video_analysis,
)

router = APIRouter(prefix="/scouting", tags=["scouting-architecture"])

def _report_response(report: ScoutingReport) -> ScoutingReportResponse:
    return ScoutingReportResponse(
        id=report.id,
        player_external_id=report.player.external_id if report.player else 0,
        overall_score=report.calculate_overall_score(),
        status=report.status,
        recommendation=report.recommendation,
        created_at=report.created_at,
    )

def _event_response(event: MatchEvent) -> MatchEventResponse:
    return MatchEventResponse(
        id=event.id,
        match_id=event.match_id,
        player_external_id=event.player.external_id if event.player else 0,
        event_type=event.event_type,
        minute=event.minute,
        second=event.second,
        success=event.success,
    )

@router.post("/reports", response_model=ScoutingReportResponse)
def create_report_endpoint(payload: ScoutingReportCreate, db: Session = Depends(get_db)):
    try:
        return _report_response(create_report(db, payload))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/reports/{report_id}/approve", response_model=ScoutingReportResponse)
def approve_report_endpoint(report_id: int, db: Session = Depends(get_db)):
    try:
        return _report_response(approve_report(db, report_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/reports/{report_id}/shortlist", response_model=ScoutingReportResponse)
def shortlist_report_endpoint(report_id: int, db: Session = Depends(get_db)):
    try:
        return _report_response(shortlist_report(db, report_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/events", response_model=MatchEventResponse)
def add_event_endpoint(payload: MatchEventCreate, db: Session = Depends(get_db)):
    try:
        return _event_response(add_match_event(db, payload))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.get("/matches/{match_id}/timeline", response_model=list[MatchEventResponse])
def timeline_endpoint(match_id: int, db: Session = Depends(get_db)):
    return [_event_response(event) for event in match_timeline(db, match_id)]

@router.post("/snapshots")
def snapshot_endpoint(payload: SnapshotCreate, db: Session = Depends(get_db)):
    try:
        snapshot = build_snapshot(db, payload)
        return {"id": snapshot.id, "player_external_id": payload.player_external_id, "overall_index_ready": True}
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/talent-score/{player_external_id}", response_model=TalentScoreResponse)
def talent_score_endpoint(player_external_id: int, db: Session = Depends(get_db)):
    try:
        score = generate_talent_score(db, player_external_id)
        return TalentScoreResponse(player_external_id=player_external_id, score=score.score, confidence=score.confidence, risk_level=score.risk_level or "MEDIUM", generated_at=score.generated_at)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/videos")
def create_video_asset_endpoint(payload: VideoAssetCreate, db: Session = Depends(get_db)):
    try:
        asset = create_video_asset(db, payload)
        return {"id": asset.id, "video_url": asset.video_url, "valid": bool(asset.video_url)}
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/videos/{video_asset_id}/analysis", response_model=VideoAnalysisResponse)
def start_analysis_endpoint(video_asset_id: int, db: Session = Depends(get_db)):
    try:
        analysis = start_video_analysis(db, video_asset_id)
        return VideoAnalysisResponse(id=analysis.id, video_asset_id=analysis.video_asset_id, status=analysis.status, detected_events=analysis.detected_events, heatmap_url=analysis.heatmap_url, highlight_url=analysis.highlight_url, analysis_score=analysis.analysis_score)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
