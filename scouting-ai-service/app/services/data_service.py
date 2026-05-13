from __future__ import annotations

from datetime import date

import httpx
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.core.config import Settings
from app.db.models import PaymentStatus, PerformanceObservation, PlayerProfile
from app.schemas.data import (
    ObservationUpsert,
    PaymentUpsert,
    PlayerUpsert,
    SyncRequest,
    SyncResult,
    UpsertResult,
)


def _to_int(value: object, default: int = 0) -> int:
    if value is None:
        return default
    if isinstance(value, bool):
        return int(value)
    if isinstance(value, (int, float)):
        return int(value)
    try:
        return int(str(value).strip())
    except Exception:
        return default


def _to_float(value: object, default: float = 0.0) -> float:
    if value is None:
        return default
    if isinstance(value, (int, float)):
        return float(value)
    try:
        return float(str(value).strip())
    except Exception:
        return default


def _to_bool(value: object, default: bool = False) -> bool:
    if value is None:
        return default
    if isinstance(value, bool):
        return value
    if isinstance(value, (int, float)):
        return value != 0
    lowered = str(value).strip().lower()
    if lowered in {"1", "true", "yes", "y"}:
        return True
    if lowered in {"0", "false", "no", "n"}:
        return False
    return default


def _to_date(value: object) -> date | None:
    if isinstance(value, date):
        return value
    if value is None:
        return None
    text = str(value).strip()
    if not text:
        return None
    try:
        return date.fromisoformat(text[:10])
    except Exception:
        return None


def _get_or_create_player(db: Session, external_id: int) -> PlayerProfile:
    player = db.scalar(select(PlayerProfile).where(PlayerProfile.external_id == external_id))
    if player is not None:
        return player

    player = PlayerProfile(
        external_id=external_id,
        full_name=f"Player {external_id}",
        is_paid=True,
    )
    db.add(player)
    db.flush()
    return player


def upsert_players(db: Session, payloads: list[PlayerUpsert]) -> UpsertResult:
    inserted = 0
    updated = 0

    for payload in payloads:
        player = db.scalar(select(PlayerProfile).where(PlayerProfile.external_id == payload.external_id))

        if player is None:
            player = PlayerProfile(external_id=payload.external_id, full_name=payload.full_name)
            db.add(player)
            inserted += 1
        else:
            updated += 1

        player.full_name = payload.full_name
        player.position = payload.position
        player.age = payload.age
        player.nationality = payload.nationality
        player.division_name = payload.division_name
        player.trainer_id = payload.trainer_id
        player.is_paid = payload.is_paid
        player.goals = payload.goals
        player.assists = payload.assists
        player.matches = payload.matches
        player.average_rating = payload.average_rating

    db.commit()
    return UpsertResult(inserted=inserted, updated=updated)


def upsert_observations(db: Session, payloads: list[ObservationUpsert]) -> UpsertResult:
    inserted = 0
    updated = 0

    for payload in payloads:
        player = _get_or_create_player(db, payload.player_external_id)
        observation = db.scalar(
            select(PerformanceObservation).where(
                PerformanceObservation.player_id == player.id,
                PerformanceObservation.observed_on == payload.observed_on,
            )
        )

        if observation is None:
            observation = PerformanceObservation(player_id=player.id, observed_on=payload.observed_on)
            db.add(observation)
            inserted += 1
        else:
            updated += 1

        observation.goals = payload.goals
        observation.assists = payload.assists
        observation.matches_played = payload.matches_played
        observation.average_rating = payload.average_rating
        observation.minutes_played = payload.minutes_played
        observation.training_attendance = payload.training_attendance
        observation.injury_days = payload.injury_days
        observation.notes = payload.notes

    db.commit()
    return UpsertResult(inserted=inserted, updated=updated)


def upsert_payments(db: Session, payloads: list[PaymentUpsert]) -> UpsertResult:
    inserted = 0
    updated = 0

    for payload in payloads:
        player = _get_or_create_player(db, payload.player_external_id)
        payment = db.scalar(
            select(PaymentStatus).where(
                PaymentStatus.player_id == player.id,
                PaymentStatus.month == payload.month,
            )
        )

        if payment is None:
            payment = PaymentStatus(player_id=player.id, month=payload.month)
            db.add(payment)
            inserted += 1
        else:
            updated += 1

        payment.amount = payload.amount
        payment.is_paid = payload.is_paid

        if payload.is_paid:
            player.is_paid = True

    db.commit()
    return UpsertResult(inserted=inserted, updated=updated)


def sync_from_football_backend(
    db: Session,
    settings: Settings,
    request: SyncRequest,
) -> SyncResult:
    base_url = (request.base_url or settings.football_backend_base_url).rstrip("/")
    headers: dict[str, str] = {}
    if settings.football_backend_bearer_token:
        headers["Authorization"] = f"Bearer {settings.football_backend_bearer_token}"

    warnings: list[str] = []
    players_upserted = 0
    observations_upserted = 0
    payments_upserted = 0

    timeout = settings.request_timeout_seconds

    with httpx.Client(timeout=timeout, headers=headers) as client:
        if request.include_players:
            try:
                players_resp = client.get(f"{base_url}/api/players")
                players_resp.raise_for_status()
                players_json = players_resp.json()
                if not isinstance(players_json, list):
                    raise ValueError("Unexpected payload for /api/players")

                player_payloads: list[PlayerUpsert] = []
                observation_payloads: list[ObservationUpsert] = []

                for raw_player in players_json:
                    external_id = _to_int(raw_player.get("id"), 0)
                    if external_id <= 0:
                        continue

                    player_payloads.append(
                        PlayerUpsert(
                            external_id=external_id,
                            full_name=str(raw_player.get("nom") or f"Player {external_id}"),
                            position=raw_player.get("position"),
                            age=_to_int(raw_player.get("age"), 0) or None,
                            nationality=raw_player.get("nationalite") or raw_player.get("nationality"),
                            division_name=(str(raw_player.get("divisionId")) if raw_player.get("divisionId") else None),
                            trainer_id=_to_int(raw_player.get("trainerId"), 0) or None,
                            is_paid=_to_bool(raw_player.get("paid"), _to_bool(raw_player.get("isPaid"), True)),
                            goals=_to_int(raw_player.get("goals"), 0),
                            assists=_to_int(raw_player.get("assists"), 0),
                            matches=_to_int(raw_player.get("matches"), 0),
                            average_rating=_to_float(
                                raw_player.get("rating", raw_player.get("averageRating", 0.0)),
                                0.0,
                            ),
                        )
                    )

                    if request.create_observation_snapshot:
                        observation_payloads.append(
                            ObservationUpsert(
                                player_external_id=external_id,
                                observed_on=date.today(),
                                goals=_to_int(raw_player.get("goals"), 0),
                                assists=_to_int(raw_player.get("assists"), 0),
                                matches_played=max(_to_int(raw_player.get("matches"), 0), 0),
                                average_rating=_to_float(
                                    raw_player.get("rating", raw_player.get("averageRating", 0.0)),
                                    0.0,
                                ),
                                minutes_played=0,
                                training_attendance=1.0,
                                injury_days=0,
                                notes="snapshot_sync_from_football_academy",
                            )
                        )

                if player_payloads:
                    result = upsert_players(db, player_payloads)
                    players_upserted = result.inserted + result.updated

                if observation_payloads:
                    result = upsert_observations(db, observation_payloads)
                    observations_upserted = result.inserted + result.updated
            except Exception as exc:
                warnings.append(f"Players sync failed: {exc}")

        if request.include_payments:
            try:
                payments_resp = client.get(f"{base_url}/api/payments")
                payments_resp.raise_for_status()
                payments_json = payments_resp.json()
                if not isinstance(payments_json, list):
                    raise ValueError("Unexpected payload for /api/payments")

                payment_payloads: list[PaymentUpsert] = []
                for raw_payment in payments_json:
                    player_external_id = _to_int(raw_payment.get("playerId"), 0)
                    month = _to_date(raw_payment.get("mois"))

                    if player_external_id <= 0 or month is None:
                        continue

                    payment_payloads.append(
                        PaymentUpsert(
                            player_external_id=player_external_id,
                            month=month,
                            amount=_to_float(raw_payment.get("montant"), 0.0),
                            is_paid=_to_bool(raw_payment.get("paid"), _to_bool(raw_payment.get("isPaid"), False)),
                        )
                    )

                if payment_payloads:
                    result = upsert_payments(db, payment_payloads)
                    payments_upserted = result.inserted + result.updated
            except Exception as exc:
                warnings.append(f"Payments sync failed: {exc}")

    return SyncResult(
        players_upserted=players_upserted,
        observations_upserted=observations_upserted,
        payments_upserted=payments_upserted,
        warnings=warnings,
    )


{'='*80}
