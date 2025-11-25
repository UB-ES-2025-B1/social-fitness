import { request } from './api'

// Save profile for a given userId. Backend expects POST /profile/{userId}
async function saveProfile(userId, { sports, bio, profileImage } = {}) {
  const res = await request(`/profile/${userId}`, { method: 'POST', body: { sports, bio, profileImage } })
  // normalize backend key -> frontend expected key
  if (res && res.data && res.data.profileImage && !res.data.avatarUrl) {
    res.data.avatarUrl = res.data.profileImage
  }
  return res
}

// Fetch user profile via users endpoint (backend provides GET /users/{userId})
async function getProfile(userId) {
  const res = await request(`/users/${userId}`, { method: 'GET' })
  // normalize backend key -> frontend expected key
  if (res && res.data && res.data.profileImage && !res.data.avatarUrl) {
    res.data.avatarUrl = res.data.profileImage
  }
  return res
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

  if (data && data.profileImage && !data.avatarUrl) {
    data = { ...data, avatarUrl: data.profileImage }
  }

  return { ok: res.ok, status: res.status, data }
}

export { saveProfile, getProfile, uploadAvatar }
