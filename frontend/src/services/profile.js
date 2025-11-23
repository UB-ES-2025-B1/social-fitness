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


// Upload avatar image using multipart/form-data.
// Note: `request()` sets JSON headers and stringifies bodies, so we use fetch directly here
// to send FormData. The returned object matches `request()`'s shape: { ok, status, data }.
async function uploadAvatar(userId, file) {
  if (!file) throw new Error('File is required')
  const base = import.meta.env.VITE_API_BASE || ''
  const url = base + `/profile/${userId}/avatar`
  const form = new FormData()
  form.append('avatar', file)

  const res = await fetch(url, {
    method: 'POST',
    body: form,
    credentials: 'include'
  })

  const text = await res.text()
  let data
  try { data = text ? JSON.parse(text) : null } catch (err) { data = text }

  return { ok: res.ok, status: res.status, data }
}

export { saveProfile, getProfile, uploadAvatar }
