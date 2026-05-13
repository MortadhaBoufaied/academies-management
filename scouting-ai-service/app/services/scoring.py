from __future__ import annotations

from app.schemas.data import FeatureSnapshot
from app.schemas.ml import FactorExplanation


def _clip(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def _age_component(age: int | None) -> float:
    if age is None:
        return 0.7
    if age <= 17:
        return 1.0
    if age <= 21:
        return 0.92
    if age <= 25:
        return 0.82
    if age <= 29:
        return 0.68
    return 0.55


def _potential_level(score: float) -> str:
    if score >= 80.0:
        return "elite"
    if score >= 60.0:
        return "prometteur"
    return "a_developper"


def _risk_level(score: float) -> str:
    if score < 0.33:
        return "faible"
    if score < 0.66:
        return "moyen"
    return "eleve"


def compute_potential(features: FeatureSnapshot, age: int | None) -> tuple[float, str, list[FactorExplanation]]:
    performance_component = _clip(features.performance_index / 100.0, 0.0, 1.0)
    progression_component = _clip((features.performance_slope + 0.03) / 0.08, 0.0, 1.0)
    age_component = _age_component(age)

    discipline_component = features.attendance_ratio
    discipline_component -= min(features.injury_days_avg / 30.0, 0.25)
    discipline_component -= features.unpaid_ratio * 0.15
    discipline_component = _clip(discipline_component, 0.0, 1.0)

    weighted = {
        "performance": (performance_component, 0.50),
        "progression": (progression_component, 0.20),
        "age": (age_component, 0.20),
        "discipline": (discipline_component, 0.10),
    }

    score_100 = sum(raw * weight for raw, weight in weighted.values()) * 100.0

    factors = [
        FactorExplanation(
            factor="Performance actuelle",
            value=round(performance_component, 4),
            weight=0.50,
            contribution_points=round(performance_component * 0.50 * 100.0, 2),
        ),
        FactorExplanation(
            factor="Dynamique recente",
            value=round(progression_component, 4),
            weight=0.20,
            contribution_points=round(progression_component * 0.20 * 100.0, 2),
        ),
        FactorExplanation(
            factor="Fenetre d'age",
            value=round(age_component, 4),
            weight=0.20,
            contribution_points=round(age_component * 0.20 * 100.0, 2),
        ),
        FactorExplanation(
            factor="Discipline et disponibilite",
            value=round(discipline_component, 4),
            weight=0.10,
            contribution_points=round(discipline_component * 0.10 * 100.0, 2),
        ),
    ]

    score_100 = round(_clip(score_100, 0.0, 100.0), 2)
    return score_100, _potential_level(score_100), factors


def evaluate_evolution(features: FeatureSnapshot) -> tuple[str, float, dict[str, float]]:
    slope = features.performance_slope

    if slope > 0.015:
        trend_label = "progression"
    elif slope < -0.015:
        trend_label = "regression"
    else:
        trend_label = "stabilite"

    confidence = 0.35
    confidence += min(features.observations_count, 12) * 0.04
    confidence += min(abs(slope) * 6.0, 0.25)
    confidence -= min(features.rating_std / 10.0, 0.10)
    confidence = round(_clip(confidence, 0.05, 0.99), 4)

    details = {
        "current_performance_index": round(features.performance_index, 4),
        "volatility": round(features.rating_std, 4),
        "attendance_ratio": round(features.attendance_ratio, 4),
        "observations_count": float(features.observations_count),
    }

    return trend_label, confidence, details


def predict_churn(features: FeatureSnapshot) -> tuple[float, str, list[str], list[str]]:
    inactivity_risk = min(features.days_since_last_activity / 45.0, 1.0) * 0.35
    attendance_risk = (1.0 - features.attendance_ratio) * 0.22
    financial_risk = features.unpaid_ratio * 0.20
    trend_risk = _clip((-features.performance_slope) / 0.05, 0.0, 1.0) * 0.13
    injury_risk = min(features.injury_days_avg / 20.0, 1.0) * 0.10

    risk_score = inactivity_risk + attendance_risk + financial_risk + trend_risk + injury_risk
    risk_score = round(_clip(risk_score, 0.0, 1.0), 4)

    probable_reasons: list[str] = []
    if inactivity_risk > 0.10:
        probable_reasons.append("Faible activite recente")
    if attendance_risk > 0.08:
        probable_reasons.append("Assiduite aux entrainements insuffisante")
    if financial_risk > 0.08:
        probable_reasons.append("Retards de paiement recurrents")
    if trend_risk > 0.06:
        probable_reasons.append("Baisse de performance sur les dernieres observations")
    if injury_risk > 0.05:
        probable_reasons.append("Impact des blessures sur la continuite sportive")

    recommended_actions: list[str] = []
    if inactivity_risk > 0.10:
        recommended_actions.append("Planifier un entretien individuel sous 7 jours")
    if attendance_risk > 0.08:
        recommended_actions.append("Mettre en place un plan de suivi de presence hebdomadaire")
    if financial_risk > 0.08:
        recommended_actions.append("Proposer un echeancier de paiement avec le parent")
    if trend_risk > 0.06:
        recommended_actions.append("Adapter la charge de travail avec un programme de remise a niveau")
    if injury_risk > 0.05:
        recommended_actions.append("Coordonner un suivi medical et prevention blessures")

    if not recommended_actions:
        recommended_actions.append("Maintenir le suivi actuel et controler le risque chaque mois")

    return risk_score, _risk_level(risk_score), probable_reasons, recommended_actions


{'='*80}
