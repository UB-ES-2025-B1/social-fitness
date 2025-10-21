Backend API contract for frontend integration

This document describes the endpoints and request/response shapes the frontend expects. Keep it in sync with the backend.

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
  - Authentication token is delivered via an HttpOnly cookie or as { "token": "..." } in the JSON response
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

Notes for backend integration
- Authentication: deliver the session token via HttpOnly, Secure cookie or include a token field in the JSON response; document which method is used.
- CORS: allow the frontend origin and credentials when using cookies (Access-Control-Allow-Credentials: true).
- Validation: return field errors in the `errors` object so the frontend can highlight fields.
- HTTP response codes: 200/201 success, 400 validation, 401 unauthorized, 500 server error.

Optional endpoints (future)
- POST /auth/logout -> 200
- GET /auth/me -> 200 with user info or 401


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
  - q: string — free text search against title, sport, and location (optional)
  - sports: string — comma-separated sport names/ids matching UI chips (e.g. "Fútbol,Básquet")
  - location: string — free text location filter
  - days: string — comma-separated day ids from {mon,tue,wed,thu,fri,sat,sun}
  - timeFrom: string — HH:mm lower bound (24h)
  - timeTo: string — HH:mm upper bound (24h)
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
- Description: Returns the details for a single event.
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
- Description: Joins the event as the authenticated user (cookie or token, as described above).
- Request body: none
- Successful response (200):
  { "message": "Joined" }
- Error responses:
  - 400: { "message": "Event full" }
  - 401: { "message": "Authentication required" }

