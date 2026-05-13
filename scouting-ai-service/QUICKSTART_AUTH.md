# Quick Start Guide - Authentication & Authorization

## 5-Minute Setup

### 1. Create Database User

```python
from app.db.models import User, UserRole
from app.core.auth import hash_password
from app.db.session import SessionLocal

db = SessionLocal()

# Create a new user
new_user = User(
    email="john@academy.com",
    full_name="John Scout",
    hashed_password=hash_password("secure_password"),
    role=UserRole.SCOUTER,
    academy_id=1,
    is_active=True,
    is_verified=True,
)
db.add(new_user)
db.commit()
db.refresh(new_user)

print(f"Created user: {new_user.id} - {new_user.email}")
```

### 2. Login and Get Token

```bash
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=john@academy.com&password=secure_password"

# Response:
# {
#   "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
#   "token_type": "bearer",
#   "user": {
#     "id": 1,
#     "email": "john@academy.com",
#     "full_name": "John Scout",
#     "role": "SCOUTER",
#     "academy_id": 1
#   }
# }
```

### 3. Verify Token Works

```bash
curl -X GET "http://localhost:8000/api/v1/auth/verify?user_id=1&token=eyJ0eXAi..."
```

### 4. Check What Services User Can Access

```bash
curl -X GET "http://localhost:8000/api/v1/auth/services/1"

# Response:
# {
#   "user_id": 1,
#   "user_role": "SCOUTER",
#   "available_services": [
#     {"service": "scouting_report", "name": "scouting_report"},
#     {"service": "talent_score", "name": "talent_score"},
#     ...
#   ]
# }
```

## Common Scenarios

### Scenario 1: User Tries to Access Restricted Service

**Request**
```bash
POST /api/v1/broker/validate-access?service=data_import&user_id=1
```

**Response (Denied)**
```json
{
    "status": "access_denied",
    "service": "data_import",
    "reason": "Your role (SCOUTER) does not have permission to access this service.",
    "who_can_access": ["SUPER_ADMIN", "ADMIN"],
    "your_role": "SCOUTER",
    "available_services": ["scouting_report", "talent_score", "match_events", "player_data", "chatbot"],
    "contact_admin": "Please contact your academy administrator to request access.",
    "chatbot_redirect": {
        "message": "Access denied. You can ask the chatbot for more information.",
        "redirect_url": "/api/v1/chatbot/explain?service=data_import",
        "explanation_prompt": "Who can access the data import service?"
    }
}
```

**What to do**
- Show the user the explanation
- Provide contact info for admin
- Suggest asking the chatbot

### Scenario 2: User Sends Chatbot Message Detected as Service Request

**Request**
```bash
POST /chat
{
    "message": "Can I create a new scouting report?",
    "user_id": 1
}
```

**Response**
```json
{
    "response": "I can help you create a scouting report. I'm checking your permissions...",
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
                "user_role": "SCOUTER"
            }
        }
    }
}
```

The user can proceed with the request.

### Scenario 3: Player Tries to Check Video Analysis

**Request**
```bash
POST /chat
{
    "message": "Can you analyze this match video?",
    "user_id": 5  // PLAYER role
}
```

**Response**
```json
{
    "response": "I cannot help with video analysis as your role (PLAYER) doesn't have access to that service.",
    "score": 0.0,
    "category": "service_detection",
    "services": ["video_analysis"],
    "authorization": {
        "user_id": 5,
        "services_detected": ["video_analysis"],
        "access_results": {
            "video_analysis": {
                "can_access": false,
                "reason": "Your role (PLAYER) does not have permission to access this service.",
                "who_can_access": ["SUPER_ADMIN", "ADMIN", "SCOUTER"],
                "your_role": "PLAYER",
                "available_services": ["player_data", "chatbot"]
            }
        }
    },
    "note": "Some services in your request require higher permissions. Here's what you can access: player_data, chatbot"
}
```

## Role-Based Scenarios

### I'm a SUPER_ADMIN - What Can I Do?

✅ All services including:
- Create scouting reports
- Analyze videos
- Generate talent scores
- Import data
- Manage users

```bash
# Check your access
curl -X GET "http://localhost:8000/api/v1/broker/my-services/1"
# Returns: 8 available services
```

### I'm an ADMIN - What Can I Do?

✅ Academy management services:
- Create scouting reports
- Analyze videos
- Generate talent scores
- Import academy data
- View player data

❌ Cannot:
- Manage system admins
- System-wide operations

### I'm a SCOUTER - What Can I Do?

✅ Evaluation services:
- Create scouting reports
- Generate talent scores
- Review match events
- View player data

❌ Cannot:
- Video analysis
- Data import
- Admin operations

### I'm a TRAINER - What Can I Do?

✅ Training services:
- View player data
- See talent scores
- Review match events
- Use chatbot

❌ Cannot:
- Create scouting reports
- Video analysis
- Data import

### I'm a PLAYER - What Can I Do?

✅ Personal services:
- View own player data
- Use chatbot

❌ Cannot:
- Create reports about others
- Access video analysis
- View academy data

## Request/Response Examples

### Example 1: Create Scouting Report (Authorized)

**Frontend sends:**
```javascript
const response = await fetch('/api/v1/broker/process-request', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        service: 'scouting_report',
        user_id: 2,  // ADMIN
        action: 'create',
        payload: {
            player_id: 10,
            technical_score: 8.5,
            tactical_score: 7.8,
            physical_score: 8.0,
            mental_score: 7.5,
            potential_score: 8.2,
            style_fit_score: 7.9,
            recommendation: "Excellent prospect"
        }
    })
});
```

**Backend responds:**
```json
{
    "status": "success",
    "service": "scouting_report",
    "user_id": 2,
    "can_process": true,
    "user_role": "ADMIN",
    "action": "create",
    "next_endpoint": "/api/v1/scouting_report/create"
}
```

**Frontend then:**
1. Show success message
2. Disable form
3. Redirect to next endpoint to actually create the report

### Example 2: Player Tries Data Import (Denied)

**Frontend sends:**
```javascript
const response = await fetch('/api/v1/broker/validate-access?service=data_import&user_id=5');
```

**Backend responds:**
```json
{
    "status": "access_denied",
    "service": "data_import",
    "reason": "Your role (PLAYER) does not have permission to access this service.",
    "who_can_access": ["SUPER_ADMIN", "ADMIN"],
    "available_services": ["player_data", "chatbot"],
    "chatbot_redirect": {
        "redirect_url": "/api/v1/chatbot/chat",
        "explanation_prompt": "Why can't I import data?"
    }
}
```

**Frontend then:**
1. Show error message with explanation
2. Show available services
3. Offer to ask chatbot
4. Disable the import button

## Testing with cURL

### Test 1: Login
```bash
curl -X POST http://localhost:8000/api/v1/auth/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "email=admin@academy.com&password=password"
```

### Test 2: Check Service Access
```bash
curl -X POST "http://localhost:8000/api/v1/auth/check-service-access?service=scouting_report&user_id=1"
```

### Test 3: Process Request
```bash
curl -X POST http://localhost:8000/api/v1/broker/process-request \
  -H "Content-Type: application/json" \
  -d '{
    "service": "scouting_report",
    "user_id": 1,
    "action": "create",
    "payload": {"player_id": 10}
  }'
```

### Test 4: Chatbot with User ID
```bash
curl -X POST http://localhost:8000/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "create a scouting report",
    "user_id": 1
  }'
```

## Debugging

### Check if User Exists
```python
from app.db.session import SessionLocal
from app.db.models import User

db = SessionLocal()
user = db.query(User).filter(User.id == 1).first()
print(f"User: {user.email}, Role: {user.role}, Active: {user.is_active}")
```

### Check User's Permissions
```python
from app.core.permissions import get_allowed_services
from app.db.models import UserRole

services = get_allowed_services(UserRole.SCOUTER)
print(f"SCOUTER can access: {services}")
```

### Verify Service Exists
```python
from app.core.permissions import ServiceName

try:
    service = ServiceName.scouting_report
    print(f"Service exists: {service.value}")
except ValueError:
    print("Service not found - check spelling")
```

### Test Authorization Flow
```python
from app.core.permissions import can_access_service
from app.db.models import UserRole

result = can_access_service(UserRole.SCOUTER, ServiceName.SCOUTING_REPORT)
print(f"SCOUTER can access scouting_report: {result}")
```

## Migration Guide

If you have existing users without roles:

```python
from app.db.session import SessionLocal
from app.db.models import User, UserRole

db = SessionLocal()

# Find users without roles
users_to_update = db.query(User).filter(User.role == None).all()

for user in users_to_update:
    if 'admin' in user.email.lower():
        user.role = UserRole.ADMIN
    elif 'scouter' in user.email.lower():
        user.role = UserRole.SCOUTER
    else:
        user.role = UserRole.PLAYER
    
    db.add(user)

db.commit()
print(f"Updated {len(users_to_update)} users")
```

## Next Steps

1. ✅ Create users with appropriate roles
2. ✅ Test login and token generation
3. ✅ Verify service access checks
4. ✅ Test chatbot integration with user_id
5. ✅ Update frontend to send user_id
6. ✅ Implement authorization error handling
7. ✅ Monitor logs for access denials
8. ✅ Handle role upgrade requests
