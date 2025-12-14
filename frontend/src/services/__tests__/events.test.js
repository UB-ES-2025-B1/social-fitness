import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { listEvents, getEvent, joinEvent, leaveEvent, createEvent } from '../events'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('events service', () => {
  it('listEvents sin params usa /events', async () => {
    request.mockResolvedValue({ ok: true })
    await listEvents()
    expect(request).toHaveBeenCalledWith('/events', { method: 'GET' })
  })

  it('listEvents construye query string con filtros', async () => {
    request.mockResolvedValue({ ok: true })

    await listEvents({
      q: 'run',
      location: 'NY',
      sports: ['Tennis', 'Cycling'],
      days: ['Mon', 'Tue'],
      timeFrom: '08:00',
      timeTo: '10:00',
    })

    expect(request).toHaveBeenCalledWith(
      '/events?q=run&location=NY&sports=Tennis%2CCycling&days=Mon%2CTue&timeFrom=08%3A00&timeTo=10%3A00',
      { method: 'GET' },
    )
  })

  it('getEvent/joinEvent/leaveEvent llaman a sus endpoints', async () => {
    request.mockResolvedValue({ ok: true })

    await getEvent(7)
    expect(request).toHaveBeenCalledWith('/events/7', { method: 'GET' })

    await joinEvent(7)
    expect(request).toHaveBeenCalledWith('/events/7/join', { method: 'POST' })

    await leaveEvent(7)
    expect(request).toHaveBeenCalledWith('/events/7/leave', { method: 'POST' })
  })

  it('createEvent envía payload', async () => {
    request.mockResolvedValue({ ok: true })

    const payload = { title: 'Run', sport: 'Running' }
    await createEvent(payload)

    expect(request).toHaveBeenCalledWith('/events', { method: 'POST', body: payload })
  })
})

