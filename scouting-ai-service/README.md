# Scouting AI Service (FastAPI)

Microservice FastAPI pour le scouting des joueurs avec IA.

## Fonctionnalites couvertes

- ML01: Score de potentiel sportif avec explications des facteurs.
- ML02: Analyse de l'evolution sportive (progression, stabilite, regression).
- ML03: Prediction du churn (risque d'abandon, raisons probables, actions recommandees).
- SC01: Recherche avancee, comparaison des joueurs, generation de shortlists en lecture seule.
- Data backend: endpoints pour alimenter les donnees ML (upsert players, observations, paiements, sync depuis Spring).

## Structure

- `app/main.py`: point d'entree FastAPI.
- `app/api/routes`: routes REST (`health`, `data`, `ml`, `scouter`).
- `app/services`: logique metier ML, feature engineering, sync backend.
- `app/db`: modeles SQLAlchemy + session DB.
- `tests`: tests unitaires de scoring IA.

## Demarrage local

1. Copier `.env.example` vers `.env`.
2. Installer les dependances:

```powershell
pip install -r requirements.txt
```

3. Lancer l'API:

```powershell
uvicorn app.main:app --reload --port 8010
```

4. Ouvrir la doc:

- Swagger: `http://localhost:8010/docs`
- OpenAPI: `http://localhost:8010/openapi.json`

## Sync avec football-academy

Le endpoint `POST /api/v1/data/sync/football-academy` appelle:

- `GET /football-academy/api/players`
- `GET /football-academy/api/payments`

Le base URL est configurable via `FOOTBALL_BACKEND_BASE_URL`.

## Endpoints principaux

- `POST /api/v1/data/players/upsert`
- `POST /api/v1/data/observations/upsert`
- `POST /api/v1/data/payments/upsert`
- `GET /api/v1/data/features/{player_external_id}`
- `POST /api/v1/data/sync/football-academy`
- `GET /api/v1/ml/potential/{player_external_id}`
- `GET /api/v1/ml/evolution/{player_external_id}`
- `GET /api/v1/ml/churn/{player_external_id}`
- `GET /api/v1/scouter/players/search`
- `POST /api/v1/scouter/players/compare`
- `POST /api/v1/scouter/shortlists/generate`

## Exemples d'utilisation

### 1) Alimenter les donnees players

```http
POST /api/v1/data/players/upsert
Content-Type: application/json

[
	{
		"external_id": 101,
		"full_name": "Ali Ben Salah",
		"position": "MID",
		"age": 17,
		"nationality": "TN",
		"division_name": "U18",
		"is_paid": true,
		"goals": 6,
		"assists": 9,
		"matches": 14,
		"average_rating": 7.4
	}
]
```

### 2) Alimenter les observations sportives

```http
POST /api/v1/data/observations/upsert
Content-Type: application/json

[
	{
		"player_external_id": 101,
		"observed_on": "2026-04-16",
		"goals": 1,
		"assists": 1,
		"matches_played": 1,
		"average_rating": 7.8,
		"training_attendance": 0.92,
		"injury_days": 0
	}
]
```

### 3) ML01/ML02/ML03

- `GET /api/v1/ml/potential/101`
- `GET /api/v1/ml/evolution/101?window=8`
- `GET /api/v1/ml/churn/101`

### 4) SC01 recherche et shortlist

- `GET /api/v1/scouter/players/search?position=MID&min_potential=65&max_churn=0.4&limit=20`

```http
POST /api/v1/scouter/shortlists/generate
Content-Type: application/json

{
	"title": "Shortlist Inter-saison",
	"strategy": "balanced",
	"position": "MID",
	"min_potential": 65,
	"max_churn": 0.45,
	"top_n": 10
}
```
