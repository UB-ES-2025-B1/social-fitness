import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

import {
  getLocalNotifications,
  addLocalNotification,
  markLocalNotificationAsRead,
  markAllLocalNotificationsAsRead,
  deleteLocalNotification,
  clearAllLocalNotifications,
  getUnreadLocalCount,
} from '../localNotifications'

let dispatchSpy

beforeEach(() => {
  localStorage.clear()
  dispatchSpy = vi.spyOn(window, 'dispatchEvent')
})

afterEach(() => {
  dispatchSpy.mockRestore()
  vi.clearAllMocks()
})

describe('localNotifications', () => {
  it('por defecto devuelve [] si no hay nada', () => {
    expect(getLocalNotifications()).toEqual([])
  })

  it('usa userId para la clave y dispara evento al añadir', () => {
    localStorage.setItem('userId', '7')

    const notif = addLocalNotification({ type: 'info', message: 'hola' })

    expect(notif).toEqual(expect.objectContaining({ type: 'info', message: 'hola', read: false, isLocal: true }))

    const raw = localStorage.getItem('local_notifications:7')
    expect(raw).not.toBeNull()
    expect(JSON.parse(raw)).toHaveLength(1)

    expect(dispatchSpy).toHaveBeenCalled()
    const evt = dispatchSpy.mock.calls[0][0]
    expect(evt.type).toBe('localNotificationAdded')
  })

  it('si el JSON guardado está roto, getLocalNotifications devuelve []', () => {
    const errorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    localStorage.setItem('local_notifications:anon', '{bad json')

    expect(getLocalNotifications()).toEqual([])
    expect(errorSpy).toHaveBeenCalled()
    errorSpy.mockRestore()
  })

  it('recorta a 50 notificaciones como máximo', () => {
    const seed = Array.from({ length: 55 }, (_, i) => ({ id: `n${i}`, read: false }))
    localStorage.setItem('local_notifications:anon', JSON.stringify(seed))

    addLocalNotification({ type: 'info', message: 'nueva' })

    const stored = JSON.parse(localStorage.getItem('local_notifications:anon'))
    expect(stored).toHaveLength(50)
  })

  it('markLocalNotificationAsRead marca una como leída y dispara evento', () => {
    const seed = [{ id: 'a', read: false }, { id: 'b', read: false }]
    localStorage.setItem('local_notifications:anon', JSON.stringify(seed))

    markLocalNotificationAsRead('a')

    const stored = JSON.parse(localStorage.getItem('local_notifications:anon'))
    expect(stored.find(n => n.id === 'a').read).toBe(true)
    expect(dispatchSpy).toHaveBeenCalled()
    expect(dispatchSpy.mock.calls[0][0].type).toBe('localNotificationUpdated')
  })

  it('markAllLocalNotificationsAsRead marca todas como leídas', () => {
    const seed = [{ id: 'a', read: false }, { id: 'b', read: true }]
    localStorage.setItem('local_notifications:anon', JSON.stringify(seed))

    markAllLocalNotificationsAsRead()

    const stored = JSON.parse(localStorage.getItem('local_notifications:anon'))
    expect(stored.every(n => n.read)).toBe(true)
  })

  it('deleteLocalNotification elimina una y clearAllLocalNotifications borra todas', () => {
    const seed = [{ id: 'a', read: false }, { id: 'b', read: false }]
    localStorage.setItem('local_notifications:anon', JSON.stringify(seed))

    deleteLocalNotification('a')
    let stored = JSON.parse(localStorage.getItem('local_notifications:anon'))
    expect(stored.map(n => n.id)).toEqual(['b'])

    clearAllLocalNotifications()
    expect(localStorage.getItem('local_notifications:anon')).toBeNull()
  })

  it('getUnreadLocalCount cuenta no leídas', () => {
    const seed = [{ id: 'a', read: false }, { id: 'b', read: true }, { id: 'c', read: false }]
    localStorage.setItem('local_notifications:anon', JSON.stringify(seed))

    expect(getUnreadLocalCount()).toBe(2)
  })
})
