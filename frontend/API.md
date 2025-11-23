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

3) POST /profile/{userId}
- Request JSON body:
  {
    "sports": [
      { "id": "football", "level": "intermediate" },
      { "id": "swimming", "level": "beginner" }
    ],
    "bio": "optional string",
    "profileImage": "optional string url"
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

Notes for frontend behaviour
- The frontend `Profile` view will perform the following:
  - GET `/users/{userId}` to fetch the user object. The `sports` field is stored by the backend as a JSON string. The frontend should parse that string into an array of `{ id, level }` objects.
  - Allow the user to add/remove sports and change the `level` for each sport.
  - On save, the frontend will POST to `/profile/{userId}` the body shown above. The `sports` array must be an array of objects `{ id: string, level: string }` where `level` is one of `beginner|intermediate|advanced|expert`.
  - Username and email are read-only in the UI and should not be changed via this endpoint.

Validation expectations (backend)
- If `sports` is missing or an empty array, return 400 with `{"message":"Validation failed","errors":{"sports":"Select at least one sport"}}` (this is how `ConfigurarPerfilController` currently behaves).

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

7) POST /events
- Description: Create a new public event. The frontend will use this endpoint when the user fills the "Crear evento" form.
- Request JSON body (frontend sends):
  {
    "title": "string",            // required
    "sport": "football|basketball|tennis|running|swimming|volleyball", // required, one of the default UI sports
    "date": "YYYY-MM-DD",        // required, ISO local date
    "time": "HH:MM",             // required, 24h local time
    "location": "string",        // required
    "organizer": "string",       // required (frontend auto-fills from GET /users/{userId} when available and makes this field read-only)
    "capacity": number,            // required (integer)
    "price": number,               // required (decimal allowed)
    "image": "string",           // required: frontend will provide a static image path mapped from the sport (e.g. "/img/sports/football.jpg")
    "description": "string|null" // optional
  }
- Notes about image: the frontend auto-assigns a static image URL based on the selected sport. It uses `/img/sports/<id>.jpg` primarily and falls back to `/img/sports/<id>.svg` if the .jpg is not available. The backend should accept these local paths as valid image strings.
- Notes about organizer: when a logged-in user is present (localStorage.userId), the frontend will attempt GET `/users/{userId}` and autofill `organizer` with the returned username and mark the field read-only. If no userId is available, the organizer field remains editable.
- Successful response (201 Created):
  {
    "id": number,
    "message": "Event created"
  }
- Frontend behavior: after receiving a successful create response containing the new event `id`, the frontend will attempt to call POST `/events/{id}/join` on behalf of the creator so the creator appears as a participant immediately in the UI. The backend should accept this request for the event creator (idempotent if the creator is already a participant). If the backend instead auto-adds the creator during event creation, the subsequent join call should still succeed (return 200) or be safely ignored by the client.
- Error responses:
  - 400 Validation error: `{ "message": "Validation failed", "errors": { ... } }` — include field-level messages.
  - 401 Unauthorized: `{ "message": "Authentication required" }` if creating events is permission-protected (current backend config permits `/events` but may change).


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
  - Notes for backend implementers: this endpoint must be idempotent — calling it twice for the same authenticated user should not cause an error (return 200 and a harmless message). The frontend will call this endpoint automatically right after event creation when the creator is present on the client. Implementations may also choose to add the creator as a participant during the `POST /events` handling; in that case allow the `/join` call to return 200 or 204 and not fail.

  7) POST /events/:id/leave
  - Description: Leaves the event for the authenticated user (cookie or token, as described above).
  - Request body: none
  - Successful response (200):
    { "message": "Left" }
  - Error responses:
    - 400: { "message": "Not a participant" }
    - 401: { "message": "Authentication required" }

 #### 13) Upload and get profile image

- POST /profile/{userId}/avatar

  Upload the user's profile picture.

  **Request:** multipart/form-data
  
  **Field:** avatar — required, must be image

  Example (curl): 
      
      curl -X POST http://localhost:8080/profile/1/avatar \-F "avatar=@/path/to/file.png"

    Response (200):
    
      {"profileImage": "http://localhost:8080/uploads/avatars/1.png"}
      
  **Backend expectations:**
  
  - Save the uploaded file (e.g., under /uploads/avatars/)
  - Update the stored profile image URL.
  - Return the public URL in profileImage.
  - GET /users/{userId} must include the updated profileImage.

  **Error responses:**
  - 400 — missing file or invalid type
  - 403 — wrong user
  - 413 — file too large
  - 415 — unsupported media type

-  GET /users/{userId}

Returns profile information from a user.

**Auth:** requieres an authenticated user

**Response 200 OK**

```json
{
  "id": "123",
  "username": "sportsenthusiast",
  "email": "athlete@example.com",
  "profileImage": "null", //por defecto
  "sports": "[{\"id\":\"football\",\"level\":\"beginner\"},{\"id\":\"tennis\",\"level\":\"advanced\"}]",
  "bio":null
}
