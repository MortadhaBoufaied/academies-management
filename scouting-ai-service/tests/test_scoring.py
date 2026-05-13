from app.schemas.data import FeatureSnapshot
from app.services.scoring import compute_potential, evaluate_evolution, predict_churn


def test_potential_is_high_for_progressive_player() -> None:
    features = FeatureSnapshot(
        observations_count=8,
        avg_rating=8.1,
        rating_std=0.3,
        goals_per_match=0.7,
        assists_per_match=0.4,
        performance_index=82.0,
        performance_slope=0.03,
        rating_slope=0.02,
        attendance_ratio=0.94,
        unpaid_ratio=0.0,
        injury_days_avg=1.0,
        days_since_last_activity=4,
    )

    score, level, _factors = compute_potential(features, age=18)

    assert score >= 75.0
    assert level in {"elite", "prometteur"}


def test_evolution_detects_regression() -> None:
    features = FeatureSnapshot(
        observations_count=6,
        avg_rating=6.2,
        rating_std=0.7,
        goals_per_match=0.2,
        assists_per_match=0.1,
        performance_index=52.0,
        performance_slope=-0.03,
        rating_slope=-0.02,
        attendance_ratio=0.7,
        unpaid_ratio=0.4,
        injury_days_avg=6.0,
        days_since_last_activity=18,
    )

    trend_label, confidence, _details = evaluate_evolution(features)

    assert trend_label == "regression"
    assert 0.0 <= confidence <= 1.0


def test_churn_risk_is_high_for_inactive_player() -> None:
    features = FeatureSnapshot(
        observations_count=3,
        avg_rating=5.8,
        rating_std=1.1,
        goals_per_match=0.0,
        assists_per_match=0.0,
        performance_index=39.0,
        performance_slope=-0.04,
        rating_slope=-0.03,
        attendance_ratio=0.42,
        unpaid_ratio=0.66,
        injury_days_avg=12.0,
        days_since_last_activity=62,
    )

    risk_score, risk_level, reasons, actions = predict_churn(features)

    assert risk_score >= 0.66
    assert risk_level == "eleve"
    assert reasons
    assert actions


{'='*80}
