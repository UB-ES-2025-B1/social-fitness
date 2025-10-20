import { request } from './api'

function buildQuery(params = {}) {
  const usp = new URLSearchParams()
  if (params.q) usp.set('q', params.q)
  if (params.location) usp.set('location', params.location)
  if (params.sports && params.sports.length) usp.set('sports', params.sports.join(','))
  if (params.days && params.days.length) usp.set('days', params.days.join(','))
  if (params.timeFrom) usp.set('timeFrom', params.timeFrom)
  if (params.timeTo) usp.set('timeTo', params.timeTo)
  const qs = usp.toString()
  return qs ? `?${qs}` : ''
}

async function listEvents(params = {}) {
  const path = `/events${buildQuery(params)}`
  return request(path, { method: 'GET' })
}

async function getEvent(id) {
  return request(`/events/${id}`, { method: 'GET' })
}

async function joinEvent(id) {
  return request(`/events/${id}/join`, { method: 'POST' })
}

export { listEvents, getEvent, joinEvent }
