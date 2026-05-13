"""
adminplatform/crud.py â€“ Database CRUD operations for AdminUser and Academy.
"""
from pathlib import Path
from sqlalchemy.orm import Session

from adminplatform.auth import hash_password
from adminplatform.db import Academy, AdminUser, UserRole


# â”€â”€ User operations 

def get_user_by_email(db: Session, email: str) -> AdminUser | None:
    return db.query(AdminUser).filter(AdminUser.email == email.lower().strip()).first()


def get_user_by_id(db: Session, user_id: int) -> AdminUser | None:
    return db.get(AdminUser, user_id)


def get_all_users(db: Session) -> list[AdminUser]:
    return db.query(AdminUser).order_by(AdminUser.created_at.desc()).all()


def create_user(
    db: Session,
    email: str,
    full_name: str,
    password: str,
    role: UserRole,
    academy_id: int | None = None,
    must_change_password: bool = True,
) -> AdminUser:
    user = AdminUser(
        email=email.lower().strip(),
        full_name=full_name.strip(),
        hashed_password=hash_password(password),
        role=role,
        academy_id=academy_id,
        must_change_password=must_change_password,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


def update_user_password(db: Session, user: AdminUser, new_password: str) -> AdminUser:
    user.hashed_password = hash_password(new_password)
    user.must_change_password = False
    db.commit()
    db.refresh(user)
    return user


def reset_user_password(db: Session, user: AdminUser, temp_password: str) -> AdminUser:
    """Super admin resets a sub-admin password; forces change on next login."""
    user.hashed_password = hash_password(temp_password)
    user.must_change_password = True
    db.commit()
    db.refresh(user)
    return user


def set_user_active(db: Session, user_id: int, active: bool) -> AdminUser | None:
    user = db.get(AdminUser, user_id)
    if user:
        user.is_active = active
        db.commit()
        db.refresh(user)
    return user


# â”€â”€ Academy operations 

def get_all_academies(db: Session) -> list[Academy]:
    return db.query(Academy).order_by(Academy.name).all()


def get_academy_by_id(db: Session, academy_id: int) -> Academy | None:
    return db.get(Academy, academy_id)


def get_academy_by_slug(db: Session, slug: str) -> Academy | None:
    return db.query(Academy).filter(Academy.slug == slug).first()


def create_academy(
    db: Session,
    name: str,
    slug: str,
    accent_color: str = "#7c6aef",
    logo_path: str | None = None,
    chatbot_url: str | None = None,
) -> Academy:
    academy = Academy(
        name=name.strip(),
        slug=slug.strip().lower(),
        accent_color=accent_color,
        logo_path=logo_path,
        chatbot_url=chatbot_url,
    )
    db.add(academy)
    db.commit()
    db.refresh(academy)
    # Create academy data directories
    _ensure_academy_dirs(academy.slug)
    return academy


def update_academy(
    db: Session,
    academy: Academy,
    name: str | None = None,
    accent_color: str | None = None,
    logo_path: str | None = None,
    chatbot_url: str | None = None,
    is_active: bool | None = None,
) -> Academy:
    if name is not None:
        academy.name = name.strip()
    if accent_color is not None:
        academy.accent_color = accent_color
    if logo_path is not None:
        academy.logo_path = logo_path
    if chatbot_url is not None:
        academy.chatbot_url = chatbot_url
    if is_active is not None:
        academy.is_active = is_active
    db.commit()
    db.refresh(academy)
    return academy


def get_users_for_academy(db: Session, academy_id: int) -> list[AdminUser]:
    return db.query(AdminUser).filter(AdminUser.academy_id == academy_id).all()


# â”€â”€ Helpers 
_DATA_ROOT = Path(__file__).resolve().parent.parent / "data"


def _ensure_academy_dirs(slug: str) -> None:
    (_DATA_ROOT / "academies" / slug).mkdir(parents=True, exist_ok=True)
    (_DATA_ROOT / "global").mkdir(parents=True, exist_ok=True)
    (_DATA_ROOT / "logos").mkdir(parents=True, exist_ok=True)


{'='*80}
