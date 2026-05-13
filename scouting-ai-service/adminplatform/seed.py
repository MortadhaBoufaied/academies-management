"""
adminplatform/seed.py â€“ Bootstrap the first super_admin account.
Run once: python -m adminplatform.seed
"""
import secrets
import string
import sys

from adminplatform.db import AdminUser, UserRole, create_tables, get_db
from adminplatform.auth import hash_password
from adminplatform import crud


def seed():
    create_tables()
    db = next(get_db())

    existing = db.query(AdminUser).filter(AdminUser.role == UserRole.super_admin).first()
    if existing:
        print(f"[seed] Super admin already exists: {existing.email}")
        return

    email = "admin@platform.local"
    password = "".join(secrets.choice(string.ascii_letters + string.digits) for _ in range(14))

    crud.create_user(
        db,
        email=email,
        full_name="Super Admin",
        password=password,
        role=UserRole.super_admin,
        must_change_password=True,
    )

    print("=" * 55)
    print("  Super admin created!")
    print(f"  Email   : {email}")
    print(f"  Password: {password}")
    print("  NOTE: You will be forced to change this on first login.")
    print("=" * 55)


if __name__ == "__main__":
    seed()


{'='*80}
