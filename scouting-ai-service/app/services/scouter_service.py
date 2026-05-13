from __future__ import annotations

from datetime import datetime

from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.models import PlayerProfile
from app.schemas.scouter import (
    CompareResponse,
    ScouterPlayerCard,
    SearchResponse,
    ShortlistGenerateRequest,
    ShortlistResponse,
)
from app.services.feature_engineering import build_feature_snapshot, get_player_by_external_id
from app.services.scoring import compute_potential, evaluate_evolution, predict_churn


def _build_card(db: Session, player: PlayerProfile) -> ScouterPlayerCard:
    snapshot = build_feature_snapshot(db, player)
    potential_score, _level, _factors = compute_potential(snapshot, player.age)
    trend_label, _confidence, _details = evaluate_evolution(snapshot)
    churn_risk, _risk_level, _reasons, _actions = predict_churn(snapshot)

    return ScouterPlayerCard(
        player_external_id=player.external_id,
        full_name=player.full_name,
        position=player.position,
        age=player.age,
        division_name=player.division_name,
        potential_score=round(potential_score, 2),
        churn_risk=round(churn_risk, 4),
        trend_label=trend_label,
        avg_rating=round(snapshot.avg_rating, 4),
        goals_per_match=round(snapshot.goals_per_match, 4),
        assists_per_match=round(snapshot.assists_per_match, 4),
        attendance_ratio=round(snapshot.attendance_ratio, 4),
    )


def search_players(
    db: Session,
    q: str | None = None,
    position: str | None = None,
    age_min: int | None = None,
    age_max: int | None = None,
    min_potential: float | None = None,
    max_churn: float | None = None,
    trend_label: str | None = None,
    min_avg_rating: float | None = None,
    limit: int = 20,
) -> SearchResponse:
    players = list(db.scalars(select(PlayerProfile).order_by(PlayerProfile.full_name.asc())))

    cards: list[ScouterPlayerCard] = []
    q_norm = (q or "").strip().lower()
    position_norm = (position or "").strip().lower()

    for player in players:
        card = _build_card(db, player)

        if q_norm and q_norm not in card.full_name.lower():
            continue
        if position_norm and (card.position or "").lower() != position_norm:
            continue
        if age_min is not None and (card.age is None or card.age < age_min):
            continue
        if age_max is not None and (card.age is None or card.age > age_max):
            continue
        if min_potential is not None and card.potential_score < min_potential:
            continue
        if max_churn is not None and card.churn_risk > max_churn:
            continue
        if trend_label is not None and card.trend_label != trend_label:
            continue
        if min_avg_rating is not None and card.avg_rating < min_avg_rating:
            continue

        cards.append(card)

    cards.sort(key=lambda card: (-card.potential_score, card.churn_risk, -card.avg_rating))
    safe_limit = max(1, min(limit, 100))

    return SearchResponse(total=len(cards), items=cards[:safe_limit])


def compare_players(db: Session, player_external_ids: list[int]) -> CompareResponse:
    cards: list[ScouterPlayerCard] = []
    for external_id in player_external_ids:
        player = get_player_by_external_id(db, external_id)
        if player is None:
            continue
        cards.append(_build_card(db, player))

    if len(cards) < 2:
        raise ValueError("At least two valid players are required for comparison")

    trend_rank = {"progression": 2, "stabilite": 1, "regression": 0}

    best_potential = max(cards, key=lambda card: card.potential_score).player_external_id
    lowest_churn = min(cards, key=lambda card: card.churn_risk).player_external_id
    best_trend = max(cards, key=lambda card: trend_rank.get(card.trend_label, 0)).player_external_id

    highlights = {
        "best_potential": best_potential,
        "lowest_churn": lowest_churn,
        "best_trend": best_trend,
    }

    cards.sort(key=lambda card: card.player_external_id)
    return CompareResponse(players=cards, highlights=highlights)


def _strategy_score(card: ScouterPlayerCard, strategy: str) -> float:
    progression_score = {"progression": 1.0, "stabilite": 0.55, "regression": 0.1}.get(card.trend_label, 0.5)
    potential_norm = card.potential_score / 100.0
    churn_stability = 1.0 - card.churn_risk

    if strategy == "high_potential":
        score = 0.80 * potential_norm + 0.20 * progression_score
    elif strategy == "low_risk":
        score = 0.65 * churn_stability + 0.25 * potential_norm + 0.10 * progression_score
    elif strategy == "breakthrough":
        score = 0.45 * potential_norm + 0.40 * progression_score + 0.15 * churn_stability
    else:
        score = 0.60 * potential_norm + 0.25 * churn_stability + 0.15 * progression_score

    return round(score * 100.0, 4)


def generate_shortlist(db: Session, request: ShortlistGenerateRequest) -> ShortlistResponse:
    candidates = search_players(
        db=db,
        q=request.q,
        position=request.position,
        age_min=request.age_min,
        age_max=request.age_max,
        min_potential=request.min_potential,
        max_churn=request.max_churn,
        trend_label=request.trend_label,
        min_avg_rating=request.min_avg_rating,
        limit=100,
    ).items

    for card in candidates:
        card.shortlist_score = _strategy_score(card, request.strategy)

    ranked = sorted(candidates, key=lambda card: (-(card.shortlist_score or 0.0), -card.potential_score, card.churn_risk))
    selected = ranked[: request.top_n]

    return ShortlistResponse(
        title=request.title,
        strategy=request.strategy,
        generated_at=datetime.utcnow(),
        total=len(selected),
        players=selected,
    )


{'='*80}
