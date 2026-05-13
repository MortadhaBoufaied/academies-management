from __future__ import annotations

from datetime import date

import numpy as np
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.db.models import PaymentStatus, PerformanceObservation, PlayerProfile
from app.schemas.data import FeatureSnapshot


def _clip(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def _slope(values: list[float]) -> float:
    if len(values) < 2:
        return 0.0
    x = np.arange(len(values), dtype=float)
    y = np.asarray(values, dtype=float)
    return float(np.polyfit(x, y, 1)[0])


def _observation_performance_index(observation: PerformanceObservation) -> float:
    matches = max(observation.matches_played, 1)
    goals_per_match = observation.goals / matches
    assists_per_match = observation.assists / matches

    attack_component = _clip((goals_per_match * 0.7 + assists_per_match * 0.3) / 1.5, 0.0, 1.0)
    rating_component = _clip(observation.average_rating / 10.0, 0.0, 1.0)
    attendance_component = _clip(observation.training_attendance, 0.0, 1.0)
    injury_penalty = _clip(observation.injury_days / 20.0, 0.0, 1.0) * 0.15

    score = 0.55 * rating_component + 0.30 * attack_component + 0.15 * attendance_component - injury_penalty
    return _clip(score, 0.0, 1.0)


def get_player_by_external_id(db: Session, player_external_id: int) -> PlayerProfile | None:
    return db.scalar(select(PlayerProfile).where(PlayerProfile.external_id == player_external_id))


def build_feature_snapshot(db: Session, player: PlayerProfile) -> FeatureSnapshot:
    observations = list(
        db.scalars(
            select(PerformanceObservation)
            .where(PerformanceObservation.player_id == player.id)
            .order_by(PerformanceObservation.observed_on.asc())
        )
    )

    payments = list(
        db.scalars(
            select(PaymentStatus)
            .where(PaymentStatus.player_id == player.id)
            .order_by(PaymentStatus.month.asc())
        )
    )

    if observations:
        ratings = [obs.average_rating for obs in observations]
        perf_indices = [_observation_performance_index(obs) for obs in observations]

        total_goals = sum(obs.goals for obs in observations)
        total_assists = sum(obs.assists for obs in observations)
        total_matches = sum(obs.matches_played for obs in observations)

        goals_per_match = total_goals / max(total_matches, 1)
        assists_per_match = total_assists / max(total_matches, 1)

        attendance_ratio = float(np.mean([obs.training_attendance for obs in observations]))
        injury_days_avg = float(np.mean([obs.injury_days for obs in observations]))

        avg_rating = float(np.mean(ratings))
        rating_std = float(np.std(ratings))
        performance_index = float(np.mean(perf_indices)) * 100.0

        rating_slope = _slope(ratings)
        performance_slope = _slope(perf_indices)

        days_since_last_activity = (date.today() - observations[-1].observed_on).days
    else:
        goals_per_match = player.goals / max(player.matches, 1) if player.matches > 0 else 0.0
        assists_per_match = player.assists / max(player.matches, 1) if player.matches > 0 else 0.0

        avg_rating = float(player.average_rating)
        rating_std = 0.0
        rating_slope = 0.0
        performance_slope = 0.0
        attendance_ratio = 0.8
        injury_days_avg = 0.0

        base_perf = 0.65 * _clip(avg_rating / 10.0, 0.0, 1.0)
        base_perf += 0.25 * _clip(goals_per_match / 1.2, 0.0, 1.0)
        base_perf += 0.10 * _clip(assists_per_match / 0.8, 0.0, 1.0)
        performance_index = _clip(base_perf, 0.0, 1.0) * 100.0

        days_since_last_activity = 999

    if payments:
        recent_payments = payments[-6:]
        unpaid_ratio = sum(1 for payment in recent_payments if not payment.is_paid) / len(recent_payments)
    else:
        unpaid_ratio = 0.0 if player.is_paid else 1.0

    return FeatureSnapshot(
        observations_count=len(observations),
        avg_rating=round(avg_rating, 4),
        rating_std=round(rating_std, 4),
        goals_per_match=round(goals_per_match, 4),
        assists_per_match=round(assists_per_match, 4),
        performance_index=round(performance_index, 4),
        performance_slope=round(performance_slope, 6),
        rating_slope=round(rating_slope, 6),
        attendance_ratio=round(_clip(attendance_ratio, 0.0, 1.0), 4),
        unpaid_ratio=round(_clip(unpaid_ratio, 0.0, 1.0), 4),
        injury_days_avg=round(max(injury_days_avg, 0.0), 4),
        days_since_last_activity=max(days_since_last_activity, 0),
    )


def build_evolution_timeline(
    db: Session,
    player: PlayerProfile,
    window: int = 8,
) -> list[dict[str, float | date]]:
    observations = list(
        db.scalars(
            select(PerformanceObservation)
            .where(PerformanceObservation.player_id == player.id)
            .order_by(PerformanceObservation.observed_on.asc())
        )
    )

    if window > 0:
        observations = observations[-window:]

    timeline: list[dict[str, float | date]] = []
    for observation in observations:
        timeline.append(
            {
                "observed_on": observation.observed_on,
                "average_rating": round(observation.average_rating, 4),
                "performance_index": round(_observation_performance_index(observation) * 100.0, 4),
            }
        )

    if not timeline:
        snapshot = build_feature_snapshot(db, player)
        timeline.append(
            {
                "observed_on": date.today(),
                "average_rating": snapshot.avg_rating,
                "performance_index": snapshot.performance_index,
            }
        )

    return timeline


{'='*80}
