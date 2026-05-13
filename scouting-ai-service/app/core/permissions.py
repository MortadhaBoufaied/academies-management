"""
Role-based permissions and service access control configuration.
Define which roles can access which services.
"""
from enum import Enum
from typing import Set

from app.db.models import UserRole


class ServiceName(str, Enum):
    """Available services in the system."""
    SCOUTING_REPORT = "scouting_report"
    TALENT_SCORE = "talent_score"
    VIDEO_ANALYSIS = "video_analysis"
    MATCH_EVENTS = "match_events"
    PLAYER_DATA = "player_data"
    DATA_IMPORT = "data_import"
    ADMIN_OPERATIONS = "admin_operations"
    CHATBOT = "chatbot"


# Role-to-Services mapping: Define which roles can access which services
ROLE_SERVICE_PERMISSIONS: dict[UserRole, Set[ServiceName]] = {
    UserRole.SUPER_ADMIN: {
        ServiceName.SCOUTING_REPORT,
        ServiceName.TALENT_SCORE,
        ServiceName.VIDEO_ANALYSIS,
        ServiceName.MATCH_EVENTS,
        ServiceName.PLAYER_DATA,
        ServiceName.DATA_IMPORT,
        ServiceName.ADMIN_OPERATIONS,
        ServiceName.CHATBOT,
    },
    UserRole.ADMIN: {
        ServiceName.SCOUTING_REPORT,
        ServiceName.TALENT_SCORE,
        ServiceName.VIDEO_ANALYSIS,
        ServiceName.MATCH_EVENTS,
        ServiceName.PLAYER_DATA,
        ServiceName.DATA_IMPORT,
        ServiceName.CHATBOT,
    },
    UserRole.SCOUTER: {
        ServiceName.SCOUTING_REPORT,
        ServiceName.TALENT_SCORE,
        ServiceName.MATCH_EVENTS,
        ServiceName.PLAYER_DATA,
        ServiceName.CHATBOT,
    },
    UserRole.TRAINER: {
        ServiceName.PLAYER_DATA,
        ServiceName.MATCH_EVENTS,
        ServiceName.TALENT_SCORE,
        ServiceName.CHATBOT,
    },
    UserRole.PLAYER: {
        ServiceName.PLAYER_DATA,
        ServiceName.CHATBOT,
    },
    UserRole.PARENT: {
        ServiceName.PLAYER_DATA,
        ServiceName.CHATBOT,
    },
}

# Service descriptions and explanations
SERVICE_DESCRIPTIONS: dict[ServiceName, dict] = {
    ServiceName.SCOUTING_REPORT: {
        "name": "Scouting Reports",
        "description": "Create and manage player scouting reports with comprehensive evaluations",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
    },
    ServiceName.TALENT_SCORE: {
        "name": "Talent Score Generation",
        "description": "Generate AI-powered talent scores and predictions for players",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER", "TRAINER"],
    },
    ServiceName.VIDEO_ANALYSIS: {
        "name": "Video Analysis",
        "description": "Analyze match videos and generate performance insights",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
    },
    ServiceName.MATCH_EVENTS: {
        "name": "Match Events",
        "description": "Track and analyze match events and player performance metrics",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER", "TRAINER"],
    },
    ServiceName.PLAYER_DATA: {
        "name": "Player Data",
        "description": "Access player profiles and performance data",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER", "TRAINER", "PLAYER", "PARENT"],
    },
    ServiceName.DATA_IMPORT: {
        "name": "Data Import",
        "description": "Import and manage bulk player and academy data",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN"],
    },
    ServiceName.ADMIN_OPERATIONS: {
        "name": "Admin Operations",
        "description": "System administration and user management",
        "allowed_roles": ["SUPER_ADMIN"],
    },
    ServiceName.CHATBOT: {
        "name": "Chatbot",
        "description": "Access the academy chatbot service for Q&A",
        "allowed_roles": ["SUPER_ADMIN", "ADMIN", "SCOUTER", "TRAINER", "PLAYER", "PARENT"],
    },
}


def can_access_service(user_role: UserRole, service: ServiceName) -> bool:
    """Check if a user role can access a specific service."""
    return service in ROLE_SERVICE_PERMISSIONS.get(user_role, set())


def get_allowed_services(user_role: UserRole) -> list[str]:
    """Get list of all services allowed for a user role."""
    return [s.value for s in ROLE_SERVICE_PERMISSIONS.get(user_role, set())]


def get_service_info(service: ServiceName) -> dict:
    """Get information about a service."""
    return SERVICE_DESCRIPTIONS.get(service, {})


def get_denied_reason(service: ServiceName, user_role: UserRole) -> dict:
    """Generate a detailed explanation for why access was denied."""
    service_info = get_service_info(service)
    
    return {
        "service": service.value,
        "service_name": service_info.get("name", service.value),
        "requested_permission": "access_denied",
        "reason": f"Your role ({user_role.value}) does not have permission to access this service.",
        "service_description": service_info.get("description", ""),
        "who_can_access": service_info.get("allowed_roles", []),
        "your_role": user_role.value,
        "contact_admin": "Please contact your academy administrator to request access.",
        "available_services": get_allowed_services(user_role),
    }
