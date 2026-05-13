"""adminplatform/main.py â€“ Full multi-tenant admin platform (Starlette 1.0 compatible)."""
import secrets
import string
from datetime import datetime
from pathlib import Path

import httpx
from fastapi import Depends, FastAPI, File, Form, HTTPException, Request, UploadFile, status
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session

from adminplatform import auth, crud, merge
from adminplatform.auth import (
    create_access_token, get_current_user, require_super_admin,
    require_any_admin, verify_password,
)
from adminplatform.db import AdminUser, UserRole, create_tables, get_db
from adminplatform.merge import save_global_csv, save_academy_csv, merge_and_write

# â”€â”€ Boot 
create_tables()

_BASE = Path(__file__).resolve().parent
_LOGO_DIR = _BASE.parent / "data" / "logos"
_LOGO_DIR.mkdir(parents=True, exist_ok=True)

app = FastAPI(title="Admin Platform", version="2.0.0")
app.mount("/static", StaticFiles(directory=str(_BASE / "static")), name="static")
app.mount("/logos", StaticFiles(directory=str(_LOGO_DIR)), name="logos")
templates = Jinja2Templates(directory=str(_BASE / "templates"))


# â”€â”€ Helpers 
def _r(url: str) -> RedirectResponse:
    return RedirectResponse(url, status_code=303)


def _t(request: Request, name: str, ctx: dict | None = None, status_code: int = 200):
    """Wrapper for Starlette 1.0 TemplateResponse(request, name, context)."""
    base = {"now": datetime.utcnow()}
    if ctx:
        base.update(ctx)
    return templates.TemplateResponse(request, name, base, status_code=status_code)


def _gen_password(length: int = 12) -> str:
    chars = string.ascii_letters + string.digits
    return "".join(secrets.choice(chars) for _ in range(length))


async def _notify_chatbot(chatbot_url: str | None, slug: str) -> None:
    if not chatbot_url:
        return
    try:
        async with httpx.AsyncClient(timeout=5) as client:
            await client.post(f"{chatbot_url}/api/reload", json={"academy": slug})
    except Exception:
        pass


# â”€â”€ Exception handlers 
@app.exception_handler(401)
async def auth_redirect(_req: Request, _exc: HTTPException):
    return _r("/auth/login")


@app.exception_handler(403)
async def forbidden_handler(req: Request, exc: HTTPException):
    return _t(req, "error.html", {"message": exc.detail}, status_code=403)


# â”€â”€ Root 
@app.get("/")
async def root():
    return _r("/auth/login")


# â”€â”€ Auth routes 
@app.get("/auth/login", response_class=HTMLResponse)
async def login_page(request: Request, error: str = ""):
    return _t(request, "login.html", {"error": error})


@app.post("/auth/login")
async def login_submit(
    request: Request,
    email: str = Form(...),
    password: str = Form(...),
    db: Session = Depends(get_db),
):
    user = crud.get_user_by_email(db, email)
    if not user or not verify_password(password, user.hashed_password) or not user.is_active:
        return _t(request, "login.html", {"error": "Invalid email or password."})
    token = create_access_token(user.id, user.role.value, user.academy_id)
    dest = "/auth/change-password" if user.must_change_password else (
        "/super/dashboard" if user.role == UserRole.super_admin else "/admin/dashboard"
    )
    resp = _r(dest)
    resp.set_cookie("access_token", token, httponly=True, max_age=28800, samesite="lax")
    return resp


@app.get("/auth/logout")
async def logout():
    resp = _r("/auth/login")
    resp.delete_cookie("access_token")
    return resp


@app.get("/auth/change-password", response_class=HTMLResponse)
async def change_pw_page(request: Request, user: AdminUser = Depends(get_current_user)):
    return _t(request, "change_password.html", {"current_user": user})


@app.post("/auth/change-password")
async def change_pw_submit(
    request: Request,
    new_password: str = Form(...),
    confirm: str = Form(...),
    user: AdminUser = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if new_password != confirm:
        return _t(request, "change_password.html", {"current_user": user, "error": "Passwords do not match."})
    if len(new_password) < 8:
        return _t(request, "change_password.html", {"current_user": user, "error": "Minimum 8 characters."})
    crud.update_user_password(db, user, new_password)
    dest = "/super/dashboard" if user.role == UserRole.super_admin else "/admin/dashboard"
    return _r(dest)


# â”€â”€ Super Admin routes 
@app.get("/super/dashboard", response_class=HTMLResponse)
async def super_dashboard(
    request: Request,
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    academies = crud.get_all_academies(db)
    users = crud.get_all_users(db)
    return _t(request, "super_dashboard.html", {
        "current_user": user,
        "academies": academies,
        "users": users,
        "global_rows": merge.get_global_row_count(),
        "total_academy_rows": sum(merge.get_academy_row_count(a.slug) for a in academies),
    })


@app.get("/super/academies", response_class=HTMLResponse)
async def super_academies(
    request: Request,
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
    success: str = "",
    error: str = "",
):
    return _t(request, "super_academies.html", {
        "current_user": user,
        "academies": crud.get_all_academies(db),
        "success": success,
        "error": error,
    })


@app.post("/super/academies")
async def super_create_academy(
    name: str = Form(...),
    slug: str = Form(...),
    accent_color: str = Form("#7c6aef"),
    chatbot_url: str = Form(""),
    admin_name: str = Form(...),
    admin_email: str = Form(...),
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    slug = slug.lower().strip()
    if crud.get_academy_by_slug(db, slug):
        return _r(f"/super/academies?error=Slug+'{slug}'+already+exists")
    if crud.get_user_by_email(db, admin_email):
        return _r(f"/super/academies?error=Email+already+registered")
    academy = crud.create_academy(db, name, slug, accent_color, chatbot_url=chatbot_url or None)
    temp_pw = _gen_password()
    crud.create_user(db, admin_email, admin_name, temp_pw, UserRole.academy_admin,
                     academy_id=academy.id, must_change_password=True)
    return _r(f"/super/academies/{slug}?success=Academy+created.+Temp+pw:+{temp_pw}")


@app.get("/super/academies/{slug}", response_class=HTMLResponse)
async def super_academy_detail(
    slug: str,
    request: Request,
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
    success: str = "",
    error: str = "",
):
    academy = crud.get_academy_by_slug(db, slug)
    if not academy:
        raise HTTPException(404, "Academy not found")
    return _t(request, "super_academy_detail.html", {
        "current_user": user,
        "academy": academy,
        "admins": crud.get_users_for_academy(db, academy.id),
        "success": success,
        "error": error,
        "academy_rows": merge.get_academy_row_count(slug),
        "global_rows": merge.get_global_row_count(),
        "merged_rows": merge.get_merged_row_count(slug),
    })


@app.post("/super/academies/{slug}/update")
async def super_update_academy(
    slug: str,
    name: str = Form(...),
    accent_color: str = Form(...),
    chatbot_url: str = Form(""),
    is_active: str = Form("on"),
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    academy = crud.get_academy_by_slug(db, slug)
    if not academy:
        raise HTTPException(404)
    crud.update_academy(db, academy, name=name, accent_color=accent_color,
                        chatbot_url=chatbot_url or None, is_active=(is_active == "on"))
    return _r(f"/super/academies/{slug}?success=Academy+updated")


@app.post("/super/academies/{slug}/logo")
async def super_upload_logo(
    slug: str,
    logo: UploadFile = File(...),
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    academy = crud.get_academy_by_slug(db, slug)
    if not academy:
        raise HTTPException(404)
    ext = Path(logo.filename).suffix.lower()
    if ext not in {".png", ".jpg", ".jpeg", ".svg", ".webp"}:
        return _r(f"/super/academies/{slug}?error=Invalid+image+format")
    fname = f"{slug}{ext}"
    (_LOGO_DIR / fname).write_bytes(await logo.read())
    crud.update_academy(db, academy, logo_path=f"/logos/{fname}")
    return _r(f"/super/academies/{slug}?success=Logo+updated")


@app.post("/super/users/{user_id}/toggle")
async def super_toggle_user(
    user_id: int,
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    target = crud.get_user_by_id(db, user_id)
    if not target:
        raise HTTPException(404)
    crud.set_user_active(db, user_id, not target.is_active)
    slug = target.academy.slug if target.academy else ""
    return _r(f"/super/academies/{slug}?success=User+status+updated")


@app.post("/super/users/{user_id}/reset-password")
async def super_reset_password(
    user_id: int,
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    target = crud.get_user_by_id(db, user_id)
    if not target:
        raise HTTPException(404)
    temp_pw = _gen_password()
    crud.reset_user_password(db, target, temp_pw)
    slug = target.academy.slug if target.academy else ""
    return _r(f"/super/academies/{slug}?success=PW+reset.+New+temp:+{temp_pw}")


@app.get("/super/global-upload", response_class=HTMLResponse)
async def super_global_upload_page(
    request: Request,
    user: AdminUser = Depends(require_super_admin),
    success: str = "",
    error: str = "",
):
    return _t(request, "super_global_upload.html", {
        "current_user": user,
        "success": success,
        "error": error,
        "global_rows": merge.get_global_row_count(),
        "global_exists": merge.get_global_csv_exists(),
    })


@app.post("/super/global-upload")
async def super_global_upload_submit(
    file: UploadFile = File(...),
    user: AdminUser = Depends(require_super_admin),
    db: Session = Depends(get_db),
):
    if not file.filename.endswith(".csv"):
        return _r("/super/global-upload?error=Only+.csv+files+accepted")
    rows = save_global_csv(await file.read())
    for a in crud.get_all_academies(db):
        if merge.get_academy_csv_exists(a.slug):
            merge_and_write(a.slug)
    return _r(f"/super/global-upload?success=Global+data+updated+({rows}+rows).+All+academies+re-merged.")


# â”€â”€ Academy Admin routes 
@app.get("/admin/dashboard", response_class=HTMLResponse)
async def admin_dashboard(
    request: Request,
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
):
    if user.role == UserRole.super_admin:
        return _r("/super/dashboard")
    academy = crud.get_academy_by_id(db, user.academy_id) if user.academy_id else None
    if not academy:
        raise HTTPException(403, "No academy assigned to your account")
    return _t(request, "admin_dashboard.html", {
        "current_user": user,
        "academy": academy,
        "academy_rows": merge.get_academy_row_count(academy.slug),
        "global_rows": merge.get_global_row_count(),
        "merged_rows": merge.get_merged_row_count(academy.slug),
        "has_data": merge.get_academy_csv_exists(academy.slug),
    })


@app.get("/admin/upload", response_class=HTMLResponse)
async def admin_upload_page(
    request: Request,
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
    success: str = "",
    error: str = "",
):
    if user.role == UserRole.super_admin:
        return _r("/super/global-upload")
    academy = crud.get_academy_by_id(db, user.academy_id)
    return _t(request, "admin_upload.html", {
        "current_user": user,
        "academy": academy,
        "success": success,
        "error": error,
        "academy_rows": merge.get_academy_row_count(academy.slug) if academy else 0,
    })


@app.post("/admin/upload")
async def admin_upload_submit(
    file: UploadFile = File(...),
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
):
    if user.role == UserRole.super_admin:
        return _r("/super/global-upload")
    academy = crud.get_academy_by_id(db, user.academy_id)
    if not academy:
        raise HTTPException(403, "No academy assigned")
    if not file.filename.endswith(".csv"):
        return _r("/admin/upload?error=Only+.csv+files+accepted")
    rows = save_academy_csv(academy.slug, await file.read())
    merge_and_write(academy.slug)
    await _notify_chatbot(academy.chatbot_url, academy.slug)
    return _r(f"/admin/upload?success=Uploaded+{rows}+rows.+Merged+data+ready.")


@app.get("/admin/chatbot", response_class=HTMLResponse)
async def admin_chatbot_page(
    request: Request,
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
    success: str = "",
    error: str = "",
):
    if user.role == UserRole.super_admin:
        return _r("/super/dashboard")
    academy = crud.get_academy_by_id(db, user.academy_id)
    if not academy:
        raise HTTPException(403)
    return _t(request, "admin_chatbot.html", {
        "current_user": user,
        "academy": academy,
        "success": success,
        "error": error,
        "merged_rows": merge.get_merged_row_count(academy.slug),
        "global_rows": merge.get_global_row_count(),
        "academy_rows": merge.get_academy_row_count(academy.slug),
        "has_merged": merge.get_academy_csv_exists(academy.slug),
    })


@app.post("/admin/chatbot/rebuild")
async def admin_rebuild_merge(
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
):
    academy = crud.get_academy_by_id(db, user.academy_id)
    if not academy:
        raise HTTPException(403)
    merge_and_write(academy.slug)
    await _notify_chatbot(academy.chatbot_url, academy.slug)
    return _r("/admin/chatbot?success=Chatbot+data+rebuilt+and+deployed.")


@app.get("/admin/settings", response_class=HTMLResponse)
async def admin_settings_page(
    request: Request,
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
    success: str = "",
    error: str = "",
):
    if user.role == UserRole.super_admin:
        return _r("/super/dashboard")
    academy = crud.get_academy_by_id(db, user.academy_id)
    return _t(request, "admin_settings.html", {
        "current_user": user,
        "academy": academy,
        "success": success,
        "error": error,
    })


@app.post("/admin/settings")
async def admin_update_settings(
    name: str = Form(...),
    accent_color: str = Form(...),
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
):
    academy = crud.get_academy_by_id(db, user.academy_id)
    if not academy:
        raise HTTPException(403)
    crud.update_academy(db, academy, name=name, accent_color=accent_color)
    return _r("/admin/settings?success=Settings+saved")


@app.post("/admin/settings/logo")
async def admin_upload_logo(
    logo: UploadFile = File(...),
    user: AdminUser = Depends(require_any_admin),
    db: Session = Depends(get_db),
):
    academy = crud.get_academy_by_id(db, user.academy_id)
    if not academy:
        raise HTTPException(403)
    ext = Path(logo.filename).suffix.lower()
    if ext not in {".png", ".jpg", ".jpeg", ".svg", ".webp"}:
        return _r("/admin/settings?error=Invalid+image+format")
    fname = f"{academy.slug}{ext}"
    (_LOGO_DIR / fname).write_bytes(await logo.read())
    crud.update_academy(db, academy, logo_path=f"/logos/{fname}")
    return _r("/admin/settings?success=Logo+updated")


{'='*80}
