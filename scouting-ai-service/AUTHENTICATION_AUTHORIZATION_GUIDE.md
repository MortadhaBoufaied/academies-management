# Authentication & Authorization System Documentation

## Overview

This document describes the comprehensive role-based authentication and authorization system implemented across the scouting-ai-service backend and chatbot frontend. The system ensures that:

1. **Every user request includes their user_id** for identification
2. **Backend validates all service requests** before processing
3. **Role-based access control (RBAC)** determines what services each user can access
4. **Unauthorized users are redirected to the chatbot** with detailed explanations
5. **Each role has defined services** they can access with clear escalation paths

## System Architecture

### Components

```
User/Client
    ↓
Chatbot (Django)
    ↓ user_id in request
FastAPI Backend Service Broker
    ↓ validates user & role
Authorization Layer
    ↓ checks permissions
Service Endpoint (if authorized)
OR
Chatbot Explanation (if denied)
```

## User Roles & Permissions

### Role Hierarchy

```
SUPER_ADMIN    (Full system access)
    ↓
ADMIN          (Academy admin)
    ↓
SCOUTER        (Scout/evaluator)
├── TRAINER
└── PLAYER / PARENT (Limited access)
```

### Services by Role

| Service | SUPER_ADMIN | ADMIN | SCOUTER | TRAINER | PLAYER/PARENT |
|---------|-------------|-------|---------|---------|---------------|
| Scouting Reports | ✅ | ✅ | ✅ | ❌ | ❌ |
| Talent Score | ✅ | ✅ | ✅ | ✅ | ❌ |
| Video Analysis | ✅ | ✅ | ✅ | ❌ | ❌ |
| Match Events | ✅ | ✅ | ✅ | ✅ | ❌ |
| Player Data | ✅ | ✅ | ✅ | ✅ | ✅ |
| Data Import | ✅ | ✅ | ❌ | ❌ | ❌ |
| Admin Operations | ✅ | ❌ | ❌ | ❌ | ❌ |
| Chatbot | ✅ | ✅ | ✅ | ✅ | ✅ |

## API Endpoints

### 1. Authentication Endpoints

#### Login
```
POST /api/v1/auth/login
Content-Type: application/x-www-form-urlencoded

email=user@academy.com
password=secure_password

Response:
{
    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "token_type": "bearer",
    "user": {
        "id": 1,
        "email": "user@academy.com",
        "full_name": "John Doe",
        "role": "ADMIN",
        "academy_id": 5
    }
}
```

#### Verify Token
```
GET /api/v1/auth/verify?user_id=1&token=eyJ0eXAi...

Response:
{
    "valid": true,
    "user": {
        "id": 1,
        "email": "user@academy.com",
        "full_name": "John Doe",
        "role": "ADMIN",
        "academy_id": 5,
        "is_active": true
    }
}
```

#### Check Service Access
```
POST /api/v1/auth/check-service-access?service=scouting_report&user_id=1

Response (if authorized):
{
    "service": "scouting_report",
    "user_id": 1,
    "can_access": true,
    "user_role": "ADMIN"
}

Response (if denied):
{
    "service": "scouting_report",
    "user_id": 1,
    "can_access": false,
    "reason": "Your role (PLAYER) does not have permission to access this service.",
    "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
    "your_role": "PLAYER",
    "available_services": ["player_data", "chatbot"],
    "contact_admin": "Please contact your academy administrator to request access."
}
```

#### Get User Services
```
GET /api/v1/auth/services/1?token=eyJ0eXAi...

Response:
{
    "user_id": 1,
    "user_role": "ADMIN",
    "available_services": [
        {
            "service": "scouting_report",
            "name": "scouting_report"
        },
        {
            "service": "talent_score",
            "name": "talent_score"
        },
        ...
    ]
}
```

### 2. Service Broker Endpoints

#### Validate Access (Pre-flight)
```
POST /api/v1/broker/validate-access?service=scouting_report&user_id=1

Response (authorized):
{
    "status": "authorized",
    "service": "scouting_report",
    "user_id": 1,
    "can_proceed": true
}

Response (denied with chatbot redirect):
{
    "status": "access_denied",
    "service": "scouting_report",
    "reason": "...",
    "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
    "your_role": "PLAYER",
    "available_services": ["player_data", "chatbot"],
    "contact_admin": "...",
    "chatbot_redirect": {
        "message": "Access denied. You can ask the chatbot for more information.",
        "redirect_url": "/api/v1/chatbot/explain?service=scouting_report",
        "explanation_prompt": "Who can access the scouting report service?"
    }
}
```

#### Process Service Request
```
POST /api/v1/broker/process-request

{
    "service": "scouting_report",
    "user_id": 1,
    "action": "create",
    "payload": {
        "player_id": 10,
        "technical_score": 8.5,
        "tactical_score": 7.8
    }
}

Response (authorized):
{
    "status": "success",
    "service": "scouting_report",
    "user_id": 1,
    "can_process": true,
    "user_role": "ADMIN",
    "action": "create",
    "next_endpoint": "/api/v1/scouting_report/create"
}

Response (denied):
{
    "status": "access_denied",
    "service": "scouting_report",
    "user_id": 1,
    "can_process": false,
    "user_role": "PLAYER",
    "reason": "Your role (PLAYER) does not have permission...",
    "service_name": "Scouting Reports",
    "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
    "available_services": ["player_data", "chatbot"],
    "redirect_to_chatbot": true,
    "chatbot_message": "I cannot process your request for Scouting Reports because...",
    "chatbot_redirect": {
        "redirect_url": "/api/v1/chatbot/chat",
        "prompt": "I tried to use Scouting Reports, but was denied. Can you explain why?"
    }
}
```

#### Get User's Available Services
```
GET /api/v1/broker/my-services/1

Response:
{
    "user_id": 1,
    "user_role": "ADMIN",
    "available_services": [
        "scouting_report",
        "talent_score",
        "video_analysis",
        "match_events",
        "player_data",
        "data_import",
        "chatbot"
    ],
    "total_services": 7
}
```

## Chatbot Integration

### Updated Chatbot Request Format

The chatbot now accepts an optional `user_id` parameter:

```
POST /chat

{
    "message": "I want to create a scouting report",
    "sender_id": "optional",
    "user_id": 1  // NEW: User ID for authorization
}

Response:
{
    "response": "I can help you create a scouting report. Let me route your request...",
    "score": 0.85,
    "category": "service_detection",
    "source": "n8n_services",
    "services": ["scouting_report"],
    "authorization": {
        "user_id": 1,
        "services_detected": ["scouting_report"],
        "access_results": {
            "scouting_report": {
                "can_access": true,
                "user_role": "ADMIN"
            }
        }
    }
}
```

### Access Denied Response

When a user tries to access a service they don't have permission for:

```
Response (Denied):
{
    "response": "I cannot help you with that service based on your current permissions...",
    "score": 0.0,
    "authorization": {
        "user_id": 1,
        "services_detected": ["scouting_report"],
        "access_results": {
            "scouting_report": {
                "can_access": false,
                "reason": "Your role (PLAYER) does not have permission...",
                "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
                "available_services": ["player_data", "chatbot"]
            }
        }
    },
    "note": "Some services in your request require higher permissions. Here's what you can access: player_data, chatbot"
}
```

## Implementation Guide

### For Frontend Developers

#### 1. Authenticate User
```javascript
// Login
const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
        email: 'user@academy.com',
        password: 'password'
    })
});

const { access_token, user } = await response.json();
const userId = user.id;
```

#### 2. Send Chat Message with User ID
```javascript
const response = await fetch('/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        message: 'I want to create a scouting report',
        user_id: userId
    })
});

const chatResponse = await response.json();

// Check if authorization info is present
if (chatResponse.authorization) {
    if (chatResponse.authorization.access_results.scouting_report.can_access) {
        // User can proceed
    } else {
        // Show explanation
        showAuthorizationMessage(chatResponse.authorization);
    }
}
```

#### 3. Check Service Access Before Using
```javascript
// Pre-flight check
const canAccess = await fetch(
    `/api/v1/broker/validate-access?service=scouting_report&user_id=${userId}`
);

const result = await canAccess.json();

if (result.status === 'authorized') {
    // Show service UI
    showScoutingReportUI();
} else {
    // Show explanation
    showChatbotExplanation(result.chatbot_redirect);
}
```

### For Backend Developers

#### 1. Use Authorization Dependencies
```python
from app.core.authorization import require_service_access
from app.core.permissions import ServiceName

@app.post("/scouting/reports")
def create_report(
    payload: ScoutingReportCreate,
    user: User = Depends(
        lambda user_id, token=None, db=Depends(get_db): 
            require_service_access(ServiceName.SCOUTING_REPORT, user_id, token, db)
    ),
    db: Session = Depends(get_db),
):
    # User is automatically authorized for this service
    # Create the report
    pass
```

#### 2. Get User Info from Request
```python
from app.core.auth import get_user_by_id
from fastapi import Query

@app.post("/some-endpoint")
def some_endpoint(
    user_id: int = Query(...),
    db: Session = Depends(get_db),
):
    user = get_user_by_id(user_id, db)
    # Use user.role for role-based logic
    if user.role == UserRole.SUPER_ADMIN:
        # Admin-only logic
        pass
```

## Environment Configuration

### Set Environment Variables

```bash
# In scouting-ai-service
export JWT_SECRET_KEY="your-super-secret-key-min-32-chars"
export ADMIN_JWT_SECRET="your-admin-secret-key"

# In chatbot
export BACKEND_SERVICE_URL="http://backend-service:8000/api/v1"
```

## Database Setup

### Create Test Users

```sql
-- Create super admin
INSERT INTO users (
    email, full_name, hashed_password, role, is_active, is_verified
) VALUES (
    'admin@system.com',
    'System Admin',
    '$2b$12$...',  -- hashed password
    'SUPER_ADMIN',
    TRUE,
    TRUE
);

-- Create academy admin
INSERT INTO users (
    email, full_name, hashed_password, role, academy_id, is_active, is_verified
) VALUES (
    'admin@academy1.com',
    'Academy Admin',
    '$2b$12$...',  -- hashed password
    'ADMIN',
    1,
    TRUE,
    TRUE
);

-- Create scouter
INSERT INTO users (
    email, full_name, hashed_password, role, academy_id, is_active, is_verified
) VALUES (
    'scouter@academy1.com',
    'John Scout',
    '$2b$12$...',  -- hashed password
    'SCOUTER',
    1,
    TRUE,
    TRUE
);

-- Create player
INSERT INTO users (
    email, full_name, hashed_password, role, academy_id, is_active, is_verified
) VALUES (
    'player@academy1.com',
    'Player Name',
    '$2b$12$...',  -- hashed password
    'PLAYER',
    1,
    TRUE,
    TRUE
);
```

## Escalation & Access Request Workflow

When a user is denied access:

1. **Backend responds with detailed explanation**
   - Why they were denied
   - Who can access the service
   - What services they have access to

2. **Chatbot provides explanation**
   - User asks: "Why can't I access scouting reports?"
   - Chatbot explains the role hierarchy
   - Provides contact info for admin

3. **Admin approves access change**
   - Updates user role in database
   - User can then access the service

## Security Considerations

1. **JWT Tokens**: Expire after 24 hours (configurable)
2. **Password Hashing**: bcrypt with proper salting
3. **HTTPS Only**: Always use HTTPS in production
4. **CORS**: Restrict origins to known domains
5. **Rate Limiting**: Applied to all endpoints
6. **User Verification**: Email verification before enabling account

## Troubleshooting

### User Can't Login
- Check email and password in database
- Verify `is_active` flag is True
- Check `is_verified` flag if required

### Service Returns 403 Forbidden
- Check user's role in database
- Verify role is in permissions mapping
- Check if backend service URL is correct

### Token Expired
- User needs to login again to get new token
- Token expires after 24 hours by default

### Authorization Check Failed
- Verify backend service is running
- Check BACKEND_SERVICE_URL in environment
- Check network connectivity between services

## Future Enhancements

1. **Service Subscription Model**: Users can request access to specific services
2. **Time-limited Access**: Grant access for a specific time period
3. **Audit Logging**: Track all access attempts and role changes
4. **Service Quotas**: Limit number of operations per service per day
5. **Custom Roles**: Allow academies to define custom roles
6. **OAuth Integration**: Support OAuth providers for authentication
