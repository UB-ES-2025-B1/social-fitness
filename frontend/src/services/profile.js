import { request } from './api'

async function saveProfile({ sports }) {
  // `sports` should be an array like: [{ id: 'football', level: 'intermediate' }, ...]
  // The backend can save these preferences and return success or field-level errors.
  return request('/profile', { method: 'POST', body: { sports } })
}

export { saveProfile }
