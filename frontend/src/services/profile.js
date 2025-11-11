import { request } from './api'

// Save profile for a given userId. Backend expects POST /profile/{userId}
async function saveProfile(userId, { sports, bio, profileImage } = {}) {
  // `sports` is an array like: [{ id: 'football', level: 'intermediate' }, ...]
  return request(`/profile/${userId}`, { method: 'POST', body: { sports, bio, profileImage } })
}

// Fetch user profile via users endpoint (backend provides GET /users/{userId})
async function getProfile(userId) {
  return request(`/users/${userId}`, { method: 'GET' })
}

export { saveProfile, getProfile }
