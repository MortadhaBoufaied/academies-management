from __future__ import annotations

from sqlalchemy.orm import Session

from app.schemas.ml import ChurnResponse, EvolutionPoint, EvolutionResponse, PotentialResponse
from app.services.feature_engineering import (
    build_evolution_timeline,
    build_feature_snapshot,
    get_player_by_external_id,
)
from app.services.scoring import compute_potential, evaluate_evolution, predict_churn


def get_potential_response(db: Session, player_external_id: int) -> PotentialResponse:
    player = get_player_by_external_id(db, player_external_id)
    if player is None:
        raise ValueError(f"Player not found: {player_external_id}")

    snapshot = build_feature_snapshot(db, player)
    score, level, factors = compute_potential(snapshot, player.age)

    return PotentialResponse(
        player_external_id=player_external_id,
        potential_score=score,
        level=level,
        factors=factors,
        feature_snapshot=snapshot,
    )


def get_evolution_response(db: Session, player_external_id: int, window: int = 8) -> EvolutionResponse:
    player = get_player_by_external_id(db, player_external_id)
    if player is None:
        raise ValueError(f"Player not found: {player_external_id}")

    snapshot = build_feature_snapshot(db, player)
    trend_label, confidence, details = evaluate_evolution(snapshot)

    timeline_raw = build_evolution_timeline(db, player, window=window)
    timeline = [EvolutionPoint(**point) for point in timeline_raw]

    return EvolutionResponse(
        player_external_id=player_external_id,
        trend_label=trend_label,
        confidence=confidence,
        performance_slope=snapshot.performance_slope,
        rating_slope=snapshot.rating_slope,
        details=details,
        timeline=timeline,
    )


def get_churn_response(db: Session, player_external_id: int) -> ChurnResponse:
    player = get_player_by_external_id(db, player_external_id)
    if player is None:
        raise ValueError(f"Player not found: {player_external_id}")

    snapshot = build_feature_snapshot(db, player)
    risk_score, risk_level, probable_reasons, recommended_actions = predict_churn(snapshot)

    return ChurnResponse(
        player_external_id=player_external_id,
        risk_score=risk_score,
        risk_level=risk_level,
        probable_reasons=probable_reasons,
        recommended_actions=recommended_actions,
        feature_snapshot=snapshot,
    )


{'='*80}
