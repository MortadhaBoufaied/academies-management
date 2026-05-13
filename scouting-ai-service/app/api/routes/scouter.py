from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.scouter import (
    CompareRequest,
    CompareResponse,
    SearchResponse,
    ShortlistGenerateRequest,
    ShortlistResponse,
)
from app.services.scouter_service import compare_players, generate_shortlist, search_players

router = APIRouter(prefix="/scouter", tags=["scouter"])


@router.get("/players/search", response_model=SearchResponse)
def search_players_endpoint(
    q: str | None = Query(default=None),
    position: str | None = Query(default=None),
    age_min: int | None = Query(default=None, ge=5, le=60),
    age_max: int | None = Query(default=None, ge=5, le=60),
    min_potential: float | None = Query(default=None, ge=0.0, le=100.0),
    max_churn: float | None = Query(default=None, ge=0.0, le=1.0),
    trend_label: str | None = Query(default=None, pattern="^(progression|stabilite|regression)$"),
    min_avg_rating: float | None = Query(default=None, ge=0.0, le=10.0),
    limit: int = Query(default=20, ge=1, le=100),
    db: Session = Depends(get_db),
) -> SearchResponse:
    return search_players(
        db=db,
        q=q,
        position=position,
        age_min=age_min,
        age_max=age_max,
        min_potential=min_potential,
        max_churn=max_churn,
        trend_label=trend_label,
        min_avg_rating=min_avg_rating,
        limit=limit,
    )


@router.post("/players/compare", response_model=CompareResponse)
def compare_players_endpoint(request: CompareRequest, db: Session = Depends(get_db)) -> CompareResponse:
    try:
        return compare_players(db, request.player_external_ids)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc


@router.post("/shortlists/generate", response_model=ShortlistResponse)
def generate_shortlist_endpoint(
    request: ShortlistGenerateRequest,
    db: Session = Depends(get_db),
) -> ShortlistResponse:
    return generate_shortlist(db, request)


{'='*80}
