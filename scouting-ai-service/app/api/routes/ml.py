from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.schemas.ml import ChurnResponse, EvolutionResponse, PotentialResponse
from app.services.ml_service import get_churn_response, get_evolution_response, get_potential_response

router = APIRouter(prefix="/ml", tags=["ml"])


@router.get("/potential/{player_external_id}", response_model=PotentialResponse)
def potential_endpoint(player_external_id: int, db: Session = Depends(get_db)) -> PotentialResponse:
    try:
        return get_potential_response(db, player_external_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.get("/evolution/{player_external_id}", response_model=EvolutionResponse)
def evolution_endpoint(
    player_external_id: int,
    window: int = Query(default=8, ge=1, le=24),
    db: Session = Depends(get_db),
) -> EvolutionResponse:
    try:
        return get_evolution_response(db, player_external_id, window=window)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


@router.get("/churn/{player_external_id}", response_model=ChurnResponse)
def churn_endpoint(player_external_id: int, db: Session = Depends(get_db)) -> ChurnResponse:
    try:
        return get_churn_response(db, player_external_id)
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc


{'='*80}
