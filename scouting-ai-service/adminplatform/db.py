"""
adminplatform/db.py â€“ SQLAlchemy models + session for the admin platform.
Uses a separate SQLite file (data/admin.db) isolated from scouting-ai-service.
"""
from datetime import datetime
from enum import Enum as PyEnum
from pathlib import Path

from sqlalchemy import Boolean, DateTime, Enum, ForeignKey, Integer, String, create_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship, sessionmaker
from collections.abc import Generator

# â”€â”€ Database file 
_DB_PATH = Path(__file__).resolve().parent.parent / "data" / "admin.db"
_DB_PATH.parent.mkdir(parents=True, exist_ok=True)

engine = create_engine(
    f"sqlite:///{_DB_PATH}",
    connect_args={"check_same_thread": False},
    pool_pre_ping=True,
)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False, expire_on_commit=False)


# â”€â”€ Base 
class Base(DeclarativeBase):
    pass


# â”€â”€ Enums 
class UserRole(str, PyEnum):
    super_admin = "super_admin"
    academy_admin = "academy_admin"


# â”€â”€ Models 
class Academy(Base):
    __tablename__ = "academies"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    slug: Mapped[str] = mapped_column(String(100), unique=True, nullable=False, index=True)
    logo_path: Mapped[str | None] = mapped_column(String(500), nullable=True)
    accent_color: Mapped[str] = mapped_column(String(20), default="#7c6aef", nullable=False)
    chatbot_url: Mapped[str | None] = mapped_column(String(500), nullable=True)  # e.g. http://localhost:8020
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)

    admins: Mapped[list["AdminUser"]] = relationship(back_populates="academy", cascade="all, delete-orphan")


class AdminUser(Base):
    __tablename__ = "admin_users"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, index=True)
    email: Mapped[str] = mapped_column(String(200), unique=True, nullable=False, index=True)
    hashed_password: Mapped[str] = mapped_column(String(200), nullable=False)
    full_name: Mapped[str] = mapped_column(String(200), nullable=False)
    role: Mapped[UserRole] = mapped_column(Enum(UserRole), nullable=False)
    academy_id: Mapped[int | None] = mapped_column(
        ForeignKey("academies.id", ondelete="SET NULL"), nullable=True, index=True
    )
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    must_change_password: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow, nullable=False)

    academy: Mapped["Academy | None"] = relationship(back_populates="admins")


# â”€â”€ Helpers 
def create_tables() -> None:
    Base.metadata.create_all(bind=engine)


def get_db() -> Generator:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


{'='*80}
