"""
Authorization dependencies and middleware for FastAPI.
Handles service access validation and role-based checks.
"""
from typing import Optional
from fastapi import Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.core.auth import get_user_by_id, decode_access_token
from app.core.permissions import ServiceName, can_access_service, get_denied_reason
from app.db.models import User, UserRole
from app.db.session import get_db


class UnauthorizedException(HTTPException):
    """Exception for authorization failures."""
    def __init__(self, detail: dict):
        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=detail,
        )


def require_authentication(
    user_id: Optional[int] = Query(None, description="User ID"),
    token: Optional[str] = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
) -> User:
    """
    Dependency to require authenticated user.
    Can pass either user_id + token or just user_id with valid session.
    """
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": "authentication_required",
                "message": "User ID is required. Please provide 'user_id' parameter.",
            },
        )

    user = get_user_by_id(user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": "user_not_found",
                "message": f"User with ID {user_id} not found.",
            },
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": "user_inactive",
                "message": "Your account is inactive. Please contact support.",
            },
        )

    # If token provided, validate it
    if token:
        try:
            payload = decode_access_token(token)
            if int(payload.get("sub")) != user_id:
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail={
                        "error": "token_mismatch",
                        "message": "Token does not match user ID.",
                    },
                )
        except HTTPException:
            raise

    return user


def require_service_access(
    service: ServiceName,
    user_id: Optional[int] = Query(None, description="User ID"),
    token: Optional[str] = Query(None, description="JWT token"),
    db: Session = Depends(get_db),
) -> User:
    """
    Dependency to require authenticated user with permission for a specific service.
    Redirects to chatbot with explanation if access denied.
    """
    # First authenticate the user
    user = require_authentication(user_id=user_id, token=token, db=db)

    # Then check service access
    if not can_access_service(user.role, service):
        denial_info = get_denied_reason(service, user.role)
        raise UnauthorizedException(detail=denial_info)

    return user


def require_role(
    required_role: UserRole,
    user: User = Depends(require_authentication),
) -> User:
    """
    Dependency to require a specific user role.
    """
    if user.role != required_role:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={
                "error": "insufficient_permissions",
                "message": f"This operation requires {required_role.value} role.",
                "your_role": user.role.value,
            },
        )
    return user


def require_any_role(
    required_roles: list[UserRole],
    user: User = Depends(require_authentication),
) -> User:
    """
    Dependency to require one of several roles.
    """
    if user.role not in required_roles:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={
                "error": "insufficient_permissions",
                "message": f"This operation requires one of these roles: {', '.join(r.value for r in required_roles)}",
                "your_role": user.role.value,
            },
        )
    return user


def require_academy_membership(
    user: User = Depends(require_authentication),
) -> User:
    """
    Dependency to require user to be part of an academy.
    """
    if not user.academy_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail={
                "error": "academy_membership_required",
                "message": "You must be assigned to an academy to access this service.",
            },
        )
    return user
