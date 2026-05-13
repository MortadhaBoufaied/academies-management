import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes.auth import router as auth_router
from app.api.routes.service_broker import router as broker_router
from app.api.routes.data import router as data_router
from app.api.routes.health import router as health_router
from app.api.routes.ml import router as ml_router
from app.api.routes.scouter import router as scouter_router
from app.api.routes.scouting import router as scouting_router
from app.core.config import get_settings
from app.db.init_db import create_all_tables

settings = get_settings()


app = FastAPI(
    title=settings.app_name,
    version="1.0.0",
    description="FastAPI microservice for scouting intelligence and ML predictions.",
)


app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event() -> None:
    if settings.database_url.startswith("sqlite:///./"):
        db_relative_path = settings.database_url.replace("sqlite:///./", "")
        db_folder = os.path.dirname(db_relative_path)
        if db_folder:
            os.makedirs(db_folder, exist_ok=True)
    create_all_tables()


app.include_router(health_router)
app.include_router(auth_router, prefix=settings.api_v1_prefix)
app.include_router(broker_router, prefix=settings.api_v1_prefix)
app.include_router(data_router, prefix=settings.api_v1_prefix)
app.include_router(ml_router, prefix=settings.api_v1_prefix)
app.include_router(scouter_router, prefix=settings.api_v1_prefix)
app.include_router(scouting_router, prefix=settings.api_v1_prefix)


@app.get("/")
def root() -> dict[str, str]:
    return {
        "service": settings.app_name,
        "env": settings.app_env,
        "docs": "/docs",
    }


{'='*80}
