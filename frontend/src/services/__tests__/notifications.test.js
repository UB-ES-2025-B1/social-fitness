import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead, deleteNotification } from '../notifications'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('notifications service', () => {
  it('GET endpoints usan rutas correctas', async () => {
    request.mockResolvedValue({ ok: true })

    await getNotifications()
    expect(request).toHaveBeenCalledWith('/notifications')

    await getUnreadCount()
    expect(request).toHaveBeenCalledWith('/notifications/unread-count')
  })

  it('markAsRead/markAllAsRead/deleteNotification usan métodos correctos', async () => {
    request.mockResolvedValue({ ok: true })

    await markAsRead(10)
    expect(request).toHaveBeenCalledWith('/notifications/10/read', { method: 'PUT' })

    await markAllAsRead()
    expect(request).toHaveBeenCalledWith('/notifications/read-all', { method: 'PUT' })

    await deleteNotification(10)
    expect(request).toHaveBeenCalledWith('/notifications/10', { method: 'DELETE' })
  })
})

