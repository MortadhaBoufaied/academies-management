from functools import lru_cache

from pydantic import field_validator

try:
    from pydantic_settings import BaseSettings, SettingsConfigDict
except ModuleNotFoundError:  # Allows local syntax/import checks even before installing requirements.
    from pydantic import BaseModel as BaseSettings
    def SettingsConfigDict(**kwargs):
        return kwargs

class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", case_sensitive=False)

    app_name: str = "scouting-ai-service"
    app_env: str = "dev"
    api_v1_prefix: str = "/api/v1"
    database_url: str = "sqlite:///./data/scouting.db"
    football_backend_base_url: str = "http://localhost:8091/football-academy"
    football_backend_bearer_token: str | None = None
    request_timeout_seconds: int = 20
    cors_origins: list[str] = ["http://localhost:3000", "http://localhost:5173", "http://localhost:8080"]

    @field_validator("cors_origins", mode="before")
    @classmethod
    def parse_cors_origins(cls, value):
        if isinstance(value, str):
            return [item.strip() for item in value.split(",") if item.strip()]
        return value

@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
