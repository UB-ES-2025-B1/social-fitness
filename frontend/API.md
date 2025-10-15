Backend API contract for frontend integration

This file documents the minimal endpoints and request/response shapes the frontend expects. Keep this in sync with backend implementors.

Base URL
- Provided by environment variable: VITE_API_BASE (e.g. http://localhost:4000)

Auth endpoints

1) POST /auth/login
- Request JSON body:
  {
    "username": "string",
    "password": "string"
  }
- Successful response (200):
  {
    "user": { "id": "string", "username": "string", "email": "string" }
  }
  - Authentication token may be delivered via HttpOnly cookie or in the JSON as { token: '...' }
- Error response (400/401):
  {
    "message": "Invalid credentials",
    "errors": {
      "username": "optional field message",
      "password": "optional field message"
    }
  }

2) POST /auth/register
- Request JSON body:
  {
    "username": "string",
    "email": "string",
    "password": "string"
  }
- Successful response (201):
  {
    "user": { "id": "string", "username": "string", "email": "string" }
  }
- Error response (400):
  {
    "message": "Validation failed",
    "errors": {
      "username": "Username already taken",
      "email": "Invalid email",
      "password": "Password too short"
    }
  }

Tips for backend implementers
- Prefer setting HttpOnly, Secure cookies for session tokens. If using JWT in the response, document where it is returned (cookie vs JSON body).
- Allow CORS from the frontend origin and support credentials if you choose cookies (Access-Control-Allow-Credentials: true).
- Return clear field-level errors in the `errors` object to allow frontend to highlight fields.
- Use standard HTTP response codes (200/201 success, 400 validation, 401 unauthorized, 500 server error).

Optional endpoints (future)
- POST /auth/logout -> 200
- GET /auth/me -> 200 with user info or 401

If you want, I can also provide example Express routes that match this contract.

Profile endpoint

3) POST /profile
- Request JSON body:
  {
    "sports": [
      { "id": "football", "level": "intermediate" },
      { "id": "swimming", "level": "beginner" }
    ]
  }
- Successful response (200):
  { "message": "Profile saved" }
- Error response (400):
  { "message": "Validation failed", "errors": { "sports": "Select at least one sport" } }

  Events endpoints

  4) GET /events
  - Description: Returns a list of public events. Supports optional query parameters for searching and filtering.
  - Query parameters:
    - q: string - free text search against title, sport, and location (optional)
    - sport: string - filter by sport id (optional)
    - date: string - ISO date or date range (optional)
  - Successful response (200):
    [
      {
        "id": "string",
        "title": "string",
        "sport": "string",
        "date": "short date string",
        "time": "HH:MM",
        "location": "string",
        "organizer": "string",
        "participants": 8,
        "capacity": 22,
        "price": 5,
        "image": "optional image URL"
      }
    ]

  5) GET /events/:id
  - Description: Return details for a single event
  - Successful response (200):
    {
      "id": "string",
      "title": "string",
      "sport": "string",
      "date": "2023-10-08",
      "time": "18:00",
      "location": "string",
      "description": "string",
      "organizer": { "id": "", "name": "" },
      "participants": [ { "id": "", "name": "" } ],
      "capacity": 22,
      "price": 5,
      "image": "url"
    }

  6) POST /events/:id/join
  - Description: Join the event as the currently authenticated user. Uses the same authentication method described above (cookie or token).
  - Request body: none
  - Successful response (200):
    { "message": "Joined" }
  - Error responses:
    - 400: { "message": "Event full" }
    - 401: { "message": "Authentication required" }

