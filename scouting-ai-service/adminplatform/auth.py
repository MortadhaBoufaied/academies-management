"""
adminplatform/auth.py â€“ JWT creation/validation and FastAPI role-guard dependencies.
"""
import os
from datetime import datetime, timedelta
from typing import Any

from fastapi import Depends, HTTPException, Request, status
from fastapi.responses import RedirectResponse
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from adminplatform.db import AdminUser, UserRole, get_db

# â”€â”€ Config 
SECRET_KEY: str = os.getenv("ADMIN_JWT_SECRET", "change-me-use-a-long-random-secret-here")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_HOURS = 8

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


# â”€â”€ JWT helpers 
def create_access_token(user_id: int, role: str, academy_id: int | None) -> str:
    expire = datetime.utcnow() + timedelta(hours=ACCESS_TOKEN_EXPIRE_HOURS)
    payload: dict[str, Any] = {
        "sub": str(user_id),
        "role": role,
        "academy_id": academy_id,
        "exp": expire,
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def _decode_token(token: str) -> dict[str, Any]:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Session expired")


# â”€â”€ Dependencies 
def get_current_user(
    request: Request,
    db: Session = Depends(get_db),
) -> AdminUser:
    """Returns the current logged-in user or raises 401 (caught by exception handler â†’ redirect)."""
    token = request.cookies.get("access_token")
    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Not authenticated")

    payload = _decode_token(token)
    user = db.get(AdminUser, int(payload["sub"]))
    if not user or not user.is_active:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User inactive or not found")
    return user


def require_super_admin(user: AdminUser = Depends(get_current_user)) -> AdminUser:
    if user.role != UserRole.super_admin:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Super admin access required")
    return user


def require_any_admin(user: AdminUser = Depends(get_current_user)) -> AdminUser:
    """Allows both super_admin and academy_admin."""
    return user


{'='*80}
