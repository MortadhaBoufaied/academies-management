"""
Authentication and authorization endpoints.
Handles user login, token generation, and service access validation.
"""
from fastapi import APIRouter, Depends, HTTPException, Query, status, Form
from sqlalchemy.orm import Session
from datetime import datetime

from app.core.auth import (
    create_access_token,
    authenticate_user,
    get_user_by_id,
    hash_password,
)
from app.core.authorization import require_authentication
from app.core.permissions import ServiceName, can_access_service, get_denied_reason
from app.db.models import User, UserRole
from app.db.session import get_db

router = APIRouter(prefix="/auth", tags=["authentication"])


@router.post("/login")
def login(
    email: str = Form(...),
    password: str = Form(...),
    db: Session = Depends(get_db),
):
    """
    Authenticate user and return JWT token.
    
    Usage:
        POST /api/v1/auth/login
        Body: { "email": "user@academy.com", "password": "password" }
    
    Response:
        { "access_token": "eyJ0...", "token_type": "bearer", "user": {...} }
    """
    user = authenticate_user(email, password, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid email or password",
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User account is inactive",
        )

    token = create_access_token(user.id, user.role.value, user.academy_id)
    
    # Update last login
    user.last_login = datetime.utcnow()
    db.add(user)
    db.commit()

    return {
        "access_token": token,
        "token_type": "bearer",
        "user": {
            "id": user.id,
            "email": user.email,
            "full_name": user.full_name,
            "role": user.role.value,
            "academy_id": user.academy_id,
        },
    }


@router.get("/verify")
def verify_token(
    user: User = Depends(require_authentication),
):
    """
    Verify JWT token and get user information.
    
    Usage:
        GET /api/v1/auth/verify?user_id=1&token=eyJ0...
    
    Response:
        { "user": {...}, "valid": true }
    """
    return {
        "valid": True,
        "user": {
            "id": user.id,
            "email": user.email,
            "full_name": user.full_name,
            "role": user.role.value,
            "academy_id": user.academy_id,
            "is_active": user.is_active,
        },
    }


@router.post("/check-service-access")
def check_service_access(
    service: ServiceName = Query(..., description="Service name"),
    user_id: int = Query(..., description="User ID"),
    token: str = Query(None, description="JWT token (optional)"),
    db: Session = Depends(get_db),
):
    """
    Check if user can access a specific service.
    Returns permission status and explanation.
    
    Usage:
        POST /api/v1/auth/check-service-access?service=scouting_report&user_id=1&token=eyJ0...
    
    Response on success:
        {
            "service": "scouting_report",
            "user_id": 1,
            "can_access": true,
            "user_role": "ADMIN"
        }
    
    Response on denied:
        {
            "service": "scouting_report",
            "user_id": 1,
            "can_access": false,
            "reason": "Your role (PLAYER) does not have permission...",
            "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
            ...
        }
    """
    # Get user
    user = get_user_by_id(user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with ID {user_id} not found",
        )

    if not user.is_active:
        return {
            "service": service.value,
            "user_id": user_id,
            "can_access": False,
            "reason": "User account is inactive",
            "user_role": user.role.value,
        }

    # Check service access
    has_access = can_access_service(user.role, service)

    if has_access:
        return {
            "service": service.value,
            "user_id": user_id,
            "can_access": True,
            "user_role": user.role.value,
        }
    else:
        denial_info = get_denied_reason(service, user.role)
        denial_info["user_id"] = user_id
        denial_info["can_access"] = False
        return denial_info


@router.get("/services/{user_id}")
def get_user_services(
    user_id: int,
    token: str = Query(None),
    db: Session = Depends(get_db),
):
    """
    Get all services available to a user based on their role.
    
    Usage:
        GET /api/v1/auth/services/1?token=eyJ0...
    
    Response:
        {
            "user_id": 1,
            "user_role": "ADMIN",
            "available_services": [
                {
                    "service": "scouting_report",
                    "name": "Scouting Reports",
                    "description": "..."
                },
                ...
            ]
        }
    """
    user = get_user_by_id(user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with ID {user_id} not found",
        )

    services = []
    for service in ServiceName:
        if can_access_service(user.role, service):
            services.append({
                "service": service.value,
                "name": service.name,
            })

    return {
        "user_id": user_id,
        "user_role": user.role.value,
        "available_services": services,
    }
