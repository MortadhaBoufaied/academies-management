# Integration Guide - Adding Authorization to Existing Endpoints

## Overview

This guide shows how to integrate the new authentication and authorization system into your existing FastAPI endpoints.

## Step 1: Update Import Statements

```python
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.core.authorization import (
    require_service_access,
    require_authentication,
    require_role,
)
from app.core.permissions import ServiceName
from app.db.models import User, UserRole
from app.db.session import get_db
```

## Step 2: Update Route Signatures

### Before (No Authorization)
```python
@router.post("/reports", response_model=ScoutingReportResponse)
def create_report_endpoint(
    payload: ScoutingReportCreate,
    db: Session = Depends(get_db)
):
    return _report_response(create_report(db, payload))
```

### After (With Authorization)
```python
@router.post("/reports", response_model=ScoutingReportResponse)
def create_report_endpoint(
    payload: ScoutingReportCreate,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    # Authenticate and authorize
    user = require_service_access(
        ServiceName.SCOUTING_REPORT,
        user_id,
        token,
        db
    )
    
    # Now user is authenticated and authorized
    return _report_response(create_report(db, payload))
```

## Step 3: Different Authorization Patterns

### Pattern 1: Require Any Authenticated User
```python
@router.get("/player/{player_id}")
def get_player(
    player_id: int,
    user_id: int = Query(...),
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    user = require_authentication(user_id=user_id, token=token, db=db)
    # User is authenticated, get player data
    player = db.query(PlayerProfile).filter(PlayerProfile.id == player_id).first()
    return player
```

### Pattern 2: Require Specific Service Access
```python
@router.post("/scouting/reports")
def create_report(
    payload: ScoutingReportCreate,
    user_id: int = Query(...),
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    user = require_service_access(
        ServiceName.SCOUTING_REPORT,
        user_id,
        token,
        db
    )
    # Create report (user is authorized)
    return create_report_impl(db, user, payload)
```

### Pattern 3: Require Specific Role
```python
@router.post("/admin/users")
def create_user(
    payload: UserCreate,
    user_id: int = Query(...),
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    user = require_role(
        UserRole.SUPER_ADMIN,
        require_authentication(user_id, token, db)
    )
    # Only SUPER_ADMIN can reach here
    return create_user_impl(db, payload)
```

### Pattern 4: Require Any of Multiple Roles
```python
@router.post("/academy/data-import")
def import_data(
    file: UploadFile,
    user_id: int = Query(...),
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    user = require_any_role(
        [UserRole.SUPER_ADMIN, UserRole.ADMIN],
        require_authentication(user_id, token, db)
    )
    # Only SUPER_ADMIN or ADMIN can reach here
    return import_data_impl(db, file)
```

## Step 4: Complete Example - Updated Scouting Routes

Here's how to update [app/api/routes/scouting.py](app/api/routes/scouting.py):

```python
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.db.models import MatchEvent, ScoutingReport, User
from app.db.session import get_db
from app.core.authorization import (
    require_service_access,
    require_authentication,
    require_academy_membership,
)
from app.core.permissions import ServiceName
from app.schemas.scouting_architecture import (
    MatchEventCreate, MatchEventResponse, ScoutingReportCreate, ScoutingReportResponse,
    SnapshotCreate, TalentScoreResponse, VideoAnalysisResponse, VideoAssetCreate,
)
from app.services.scouting_architecture_service import (
    add_match_event, approve_report, build_snapshot, create_report, create_video_asset,
    generate_talent_score, match_timeline, shortlist_report, start_video_analysis,
)

router = APIRouter(prefix="/scouting", tags=["scouting-architecture"])

def _report_response(report: ScoutingReport) -> ScoutingReportResponse:
    return ScoutingReportResponse(
        id=report.id,
        player_external_id=report.player.external_id if report.player else 0,
        overall_score=report.calculate_overall_score(),
        status=report.status,
        recommendation=report.recommendation,
        created_at=report.created_at,
    )

def _event_response(event: MatchEvent) -> MatchEventResponse:
    return MatchEventResponse(
        id=event.id,
        match_id=event.match_id,
        player_external_id=event.player.external_id if event.player else 0,
        event_type=event.event_type,
        minute=event.minute,
        second=event.second,
        success=event.success,
    )

# REPORTS

@router.post("/reports", response_model=ScoutingReportResponse)
def create_report_endpoint(
    payload: ScoutingReportCreate,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Create a new scouting report (requires SCOUTING_REPORT permission)."""
    user = require_service_access(ServiceName.SCOUTING_REPORT, user_id, token, db)
    
    try:
        return _report_response(create_report(db, payload))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/reports/{report_id}/approve", response_model=ScoutingReportResponse)
def approve_report_endpoint(
    report_id: int,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Approve a scouting report (requires SCOUTING_REPORT permission)."""
    user = require_service_access(ServiceName.SCOUTING_REPORT, user_id, token, db)
    
    try:
        return _report_response(approve_report(db, report_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/reports/{report_id}/shortlist", response_model=ScoutingReportResponse)
def shortlist_report_endpoint(
    report_id: int,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Shortlist a scouting report (requires SCOUTING_REPORT permission)."""
    user = require_service_access(ServiceName.SCOUTING_REPORT, user_id, token, db)
    
    try:
        return _report_response(shortlist_report(db, report_id))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

# MATCH EVENTS

@router.post("/events", response_model=MatchEventResponse)
def add_event_endpoint(
    payload: MatchEventCreate,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Record a match event (requires MATCH_EVENTS permission)."""
    user = require_service_access(ServiceName.MATCH_EVENTS, user_id, token, db)
    
    try:
        return _event_response(add_match_event(db, payload))
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.get("/matches/{match_id}/timeline", response_model=list[MatchEventResponse])
def timeline_endpoint(
    match_id: int,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Get match timeline (requires MATCH_EVENTS permission)."""
    user = require_service_access(ServiceName.MATCH_EVENTS, user_id, token, db)
    return [_event_response(event) for event in match_timeline(db, match_id)]

# SNAPSHOTS

@router.post("/snapshots")
def snapshot_endpoint(
    payload: SnapshotCreate,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Create player attribute snapshot (requires appropriate permission)."""
    user = require_service_access(ServiceName.TALENT_SCORE, user_id, token, db)
    
    try:
        snapshot = build_snapshot(db, payload)
        return {"id": snapshot.id, "player_external_id": payload.player_external_id, "overall_index_ready": True}
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

# TALENT SCORES

@router.post("/talent-score/{player_external_id}", response_model=TalentScoreResponse)
def talent_score_endpoint(
    player_external_id: int,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Generate talent score (requires TALENT_SCORE permission)."""
    user = require_service_access(ServiceName.TALENT_SCORE, user_id, token, db)
    
    try:
        score = generate_talent_score(db, player_external_id)
        return TalentScoreResponse(
            player_external_id=player_external_id,
            score=score.score,
            confidence=score.confidence,
            risk_level=score.risk_level or "MEDIUM",
            generated_at=score.generated_at
        )
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

# VIDEOS

@router.post("/videos")
def create_video_asset_endpoint(
    payload: VideoAssetCreate,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Upload video asset (requires VIDEO_ANALYSIS permission)."""
    user = require_service_access(ServiceName.VIDEO_ANALYSIS, user_id, token, db)
    
    try:
        asset = create_video_asset(db, payload)
        return {"id": asset.id, "video_url": asset.video_url, "valid": bool(asset.video_url)}
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc

@router.post("/videos/{video_asset_id}/analysis", response_model=VideoAnalysisResponse)
def start_analysis_endpoint(
    video_asset_id: int,
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
):
    """Start video analysis (requires VIDEO_ANALYSIS permission)."""
    user = require_service_access(ServiceName.VIDEO_ANALYSIS, user_id, token, db)
    
    try:
        analysis = start_video_analysis(db, video_asset_id)
        return VideoAnalysisResponse(
            id=analysis.id,
            video_asset_id=analysis.video_asset_id,
            status=analysis.status,
            detected_events=analysis.detected_events,
            heatmap_url=analysis.heatmap_url,
            highlight_url=analysis.highlight_url,
            analysis_score=analysis.analysis_score
        )
    except ValueError as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
```

## Step 5: Handle Authorization Errors in Frontend

```javascript
async function callScoutingAPI(endpoint, options) {
    const userId = getCurrentUserId();
    const token = getToken();
    
    const url = new URL(endpoint);
    url.searchParams.append('user_id', userId);
    if (token) {
        url.searchParams.append('token', token);
    }
    
    try {
        const response = await fetch(url.toString(), options);
        
        if (response.status === 403) {
            const error = await response.json();
            // Show authorization error
            showAuthorizationError(error.detail);
            return null;
        }
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
}

function showAuthorizationError(detail) {
    alert(`Access Denied: ${detail.reason || detail}`);
    // Optionally redirect to chatbot
    window.location.href = `/chatbot?service=${detail.service}`;
}
```

## Step 6: Update Data Routes

Similarly, update [app/api/routes/data.py](app/api/routes/data.py):

```python
@router.post("/players/import")
def import_players(
    file: UploadFile,
    user_id: int = Query(...),
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    """Import player data (requires DATA_IMPORT permission)."""
    user = require_service_access(ServiceName.DATA_IMPORT, user_id, token, db)
    
    # Process import
    return {"imported": count}
```

## Step 7: Error Handling Middleware

Add to your main.py to handle authorization errors gracefully:

```python
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from app.core.authorization import UnauthorizedException

@app.exception_handler(UnauthorizedException)
async def authorization_exception_handler(request, exc):
    """Handle authorization exceptions."""
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": "authorization_failed",
            "detail": exc.detail,
        },
    )

@app.exception_handler(HTTPException)
async def http_exception_handler(request, exc):
    """Handle HTTP exceptions including auth errors."""
    if exc.status_code in [401, 403]:
        return JSONResponse(
            status_code=exc.status_code,
            content={
                "error": "auth_error" if exc.status_code == 401 else "forbidden",
                "detail": exc.detail,
            },
        )
    raise exc
```

## Testing Your Updated Endpoints

```bash
# Test with authorization
curl -X POST http://localhost:8000/api/v1/scouting/reports \
  -H "Content-Type: application/json" \
  -d '{"...": "..."}' \
  -G --data-urlencode "user_id=1" \
  --data-urlencode "token=eyJ0..."

# Test without authorization (should fail)
curl -X POST http://localhost:8000/api/v1/scouting/reports \
  -H "Content-Type: application/json" \
  -d '{"...": "..."}' \
  -G --data-urlencode "user_id=5"  # PLAYER role
```

## Summary of Changes

| Before | After |
|--------|-------|
| No user identification | Every request includes user_id |
| No permission checks | Backend validates all requests |
| No role checking | Services require specific roles |
| No explanations | Users get detailed denial reasons |
| No escalation | Guidance to request access |

Your API is now fully authenticated and authorized!
