from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.config import get_settings
from app.db.session import get_db
from app.schemas.data import (
    DataFeatureResponse,
    ObservationUpsert,
    PaymentUpsert,
    PlayerUpsert,
    SyncRequest,
    SyncResult,
    UpsertResult,
)
from app.services.data_service import (
    sync_from_football_backend,
    upsert_observations,
    upsert_payments,
    upsert_players,
)
from app.services.feature_engineering import build_feature_snapshot, get_player_by_external_id

router = APIRouter(prefix="/data", tags=["data"])


@router.post("/players/upsert", response_model=UpsertResult)
def upsert_players_endpoint(payloads: list[PlayerUpsert], db: Session = Depends(get_db)) -> UpsertResult:
    if not payloads:
        raise HTTPException(status_code=400, detail="Payload must not be empty")
    return upsert_players(db, payloads)


@router.post("/observations/upsert", response_model=UpsertResult)
def upsert_observations_endpoint(
    payloads: list[ObservationUpsert],
    db: Session = Depends(get_db),
) -> UpsertResult:
    if not payloads:
        raise HTTPException(status_code=400, detail="Payload must not be empty")
    return upsert_observations(db, payloads)


@router.post("/payments/upsert", response_model=UpsertResult)
def upsert_payments_endpoint(payloads: list[PaymentUpsert], db: Session = Depends(get_db)) -> UpsertResult:
    if not payloads:
        raise HTTPException(status_code=400, detail="Payload must not be empty")
    return upsert_payments(db, payloads)


@router.post("/sync/football-academy", response_model=SyncResult)
def sync_football_academy_endpoint(
    request: SyncRequest,
    db: Session = Depends(get_db),
) -> SyncResult:
    settings = get_settings()
    return sync_from_football_backend(db, settings, request)


@router.get("/features/{player_external_id}", response_model=DataFeatureResponse)
def get_features_endpoint(player_external_id: int, db: Session = Depends(get_db)) -> DataFeatureResponse:
    player = get_player_by_external_id(db, player_external_id)
    if player is None:
        raise HTTPException(status_code=404, detail="Player not found")

    snapshot = build_feature_snapshot(db, player)
    return DataFeatureResponse(
        player_external_id=player.external_id,
        full_name=player.full_name,
        position=player.position,
        age=player.age,
        feature_snapshot=snapshot,
    )


{'='*80}
