import { request } from './api'

async function saveProfile({ sports }) {
  // `sports` is an array like: [{ id: 'football', level: 'intermediate' }, ...]
  return request('/profile', { method: 'POST', body: { sports } })
}

export { saveProfile }
