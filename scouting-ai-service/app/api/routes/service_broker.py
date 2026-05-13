"""
Service broker - Unified entry point for all service requests with authentication and authorization.
All service requests pass through this broker for immediate authentication checks.
"""
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.core.auth import get_user_by_id
from app.core.permissions import ServiceName, can_access_service, get_denied_reason, get_allowed_services
from app.db.models import User
from app.db.session import get_db

router = APIRouter(prefix="/broker", tags=["service-broker"])


class ServiceRequest(BaseModel):
    """Service request with user authentication."""
    service: ServiceName
    user_id: int
    action: str
    payload: dict = {}


class UnauthorizedServiceResponse(BaseModel):
    """Response when user is not authorized for a service."""
    status: str = "access_denied"
    service: str
    reason: str
    who_can_access: list[str]
    your_role: str
    available_services: list[str]
    contact_admin: str
    chatbot_redirect: dict = {}


@router.post("/validate-access")
def validate_service_access(
    service: ServiceName = Query(..., description="Service to access"),
    user_id: int = Query(..., description="User ID"),
    db: Session = Depends(get_db),
):
    """
    Validate if a user can access a service before making the actual request.
    Use this as a pre-flight check before redirecting to the service.
    
    Usage:
        POST /api/v1/broker/validate-access?service=scouting_report&user_id=1
    
    Response if authorized:
        {
            "status": "authorized",
            "service": "scouting_report",
            "user_id": 1,
            "can_proceed": true
        }
    
    Response if not authorized:
        {
            "status": "access_denied",
            "service": "scouting_report",
            "reason": "Your role (PLAYER) does not have permission...",
            "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
            "your_role": "PLAYER",
            "available_services": ["player_data", "chatbot"],
            "contact_admin": "Please contact your academy administrator...",
            "chatbot_redirect": {
                "message": "Access denied. You can ask the chatbot for more information.",
                "redirect_url": "/api/v1/chatbot/explain?service=scouting_report",
                "explanation_prompt": "Who can access the scouting report service?"
            }
        }
    """
    user = get_user_by_id(user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "error": "user_not_found",
                "message": f"User with ID {user_id} not found",
            },
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": "user_inactive",
                "message": "User account is inactive",
            },
        )

    # Check service access
    if can_access_service(user.role, service):
        return {
            "status": "authorized",
            "service": service.value,
            "user_id": user_id,
            "can_proceed": True,
        }

    # Access denied - prepare chatbot redirect
    denial_info = get_denied_reason(service, user.role)
    return {
        "status": "access_denied",
        "service": service.value,
        "reason": denial_info["reason"],
        "who_can_access": denial_info["who_can_access"],
        "your_role": user.role.value,
        "available_services": denial_info["available_services"],
        "contact_admin": denial_info["contact_admin"],
        "chatbot_redirect": {
            "message": f"Access to {denial_info['service_name']} is restricted. Let me explain who can access it.",
            "redirect_url": f"/api/v1/chatbot/explain-service?service={service.value}&user_id={user_id}",
            "explanation_prompt": f"Who can access the {denial_info['service_name']} service and why?",
        },
    }


@router.post("/process-request")
def process_service_request(
    request: ServiceRequest,
    db: Session = Depends(get_db),
):
    """
    Process a service request with full authentication and authorization.
    This is the unified entry point for all service requests from the chatbot or other clients.
    
    Usage:
        POST /api/v1/broker/process-request
        {
            "service": "scouting_report",
            "user_id": 1,
            "action": "create",
            "payload": { "player_id": 10, "score": 8.5 }
        }
    
    Response if authorized:
        {
            "status": "success",
            "service": "scouting_report",
            "can_process": true,
            "next_endpoint": "/api/v1/scouting/reports"
        }
    
    Response if not authorized:
        {
            "status": "access_denied",
            "service": "scouting_report",
            "can_process": false,
            "reason": "...",
            "redirect_to_chatbot": true,
            "chatbot_message": "...",
            "chatbot_redirect": {...}
        }
    """
    user = get_user_by_id(request.user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "error": "user_not_found",
                "message": f"User with ID {request.user_id} not found",
            },
        )

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={
                "error": "user_inactive",
                "message": "User account is inactive",
            },
        )

    # Check service access
    if can_access_service(user.role, request.service):
        return {
            "status": "success",
            "service": request.service.value,
            "user_id": request.user_id,
            "can_process": True,
            "user_role": user.role.value,
            "action": request.action,
            "next_endpoint": f"/api/v1/{request.service.value}/{request.action}",
        }

    # Access denied
    denial_info = get_denied_reason(request.service, user.role)
    return {
        "status": "access_denied",
        "service": request.service.value,
        "user_id": request.user_id,
        "can_process": False,
        "user_role": user.role.value,
        "reason": denial_info["reason"],
        "service_name": denial_info["service_name"],
        "who_can_access": denial_info["who_can_access"],
        "available_services": denial_info["available_services"],
        "redirect_to_chatbot": True,
        "chatbot_message": f"I cannot process your request for {denial_info['service_name']} because your role ({user.role.value}) doesn't have access. "
                          f"This service is only available to: {', '.join(denial_info['who_can_access'])}. "
                          f"You currently have access to: {', '.join(denial_info['available_services'])}. "
                          f"{denial_info['contact_admin']}",
        "chatbot_redirect": {
            "redirect_url": f"/api/v1/chatbot/chat",
            "prompt": f"I tried to use {denial_info['service_name']}, but was denied. Can you explain why?",
        },
    }


@router.get("/my-services/{user_id}")
def get_user_accessible_services(
    user_id: int,
    db: Session = Depends(get_db),
):
    """
    Get all services available to a user.
    Useful for UI to show what services are available.
    
    Usage:
        GET /api/v1/broker/my-services/1
    
    Response:
        {
            "user_id": 1,
            "user_role": "ADMIN",
            "available_services": ["scouting_report", "talent_score", ...],
            "total_services": 7
        }
    """
    user = get_user_by_id(user_id, db)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"User with ID {user_id} not found",
        )

    services = get_allowed_services(user.role)
    
    return {
        "user_id": user_id,
        "user_role": user.role.value,
        "available_services": services,
        "total_services": len(services),
    }
