import { request } from './api'

async function listEvents({ q } = {}) {
  const path = q ? `/events?q=${encodeURIComponent(q)}` : '/events'
  return request(path, { method: 'GET' })
}

async function getEvent(id) {
  return request(`/events/${id}`, { method: 'GET' })
}

async function joinEvent(id) {
  return request(`/events/${id}/join`, { method: 'POST' })
}

export { listEvents, getEvent, joinEvent }
