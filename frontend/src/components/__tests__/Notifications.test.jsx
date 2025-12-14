import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Notifications from '../Notifications'

// Mocks de servicios
vi.mock('../../services/notifications', () => ({
  getNotifications: vi.fn(),
  markAsRead: vi.fn(),
  markAllAsRead: vi.fn(),
  deleteNotification: vi.fn()
}))

vi.mock('../../services/localNotifications', () => ({
  getLocalNotifications: vi.fn(() => []),
  markLocalNotificationAsRead: vi.fn(),
  markAllLocalNotificationsAsRead: vi.fn(),
  deleteLocalNotification: vi.fn()
}))

import * as notificationService from '../../services/notifications'
import * as localNotificationService from '../../services/localNotifications'

describe('Notifications', () => {
  const mockNotifications = [
    {
      id: '1',
      type: 'EVENT_STARTING',
      message: 'Tu evento comienza en 30 minutos',
      timestamp: new Date(Date.now() - 1000 * 60 * 5).toISOString(), // 5 min ago
      read: false,
      eventId: 'e1'
    },
    {
      id: '2',
      type: 'NEW_MESSAGE',
      message: 'Tienes un nuevo mensaje de Juan',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(), // 2 hours ago
      read: true,
      relatedUserId: 'u1'
    },
    {
      id: '3',
      type: 'JOINED_EVENT',
      message: 'Te has unido al evento "Fútbol en el parque"',
      timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(), // 1 day ago
      read: false
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: [] })
    localNotificationService.getLocalNotifications.mockReturnValue([])
  })

  it('muestra estado de carga inicialmente', () => {
    notificationService.getNotifications.mockImplementation(() => new Promise(() => {}))
    render(<Notifications />)

    expect(screen.getByText('Cargando notificaciones...')).toBeInTheDocument()
  })

  it('muestra el título "Notificaciones"', async () => {
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Notificaciones' })).toBeInTheDocument()
    })
  })

  it('muestra mensaje vacío cuando no hay notificaciones', async () => {
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('No tienes notificaciones')).toBeInTheDocument()
    })
  })

  it('muestra la lista de notificaciones', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('Tu evento comienza en 30 minutos')).toBeInTheDocument()
      expect(screen.getByText('Tienes un nuevo mensaje de Juan')).toBeInTheDocument()
      expect(screen.getByText('Te has unido al evento "Fútbol en el parque"')).toBeInTheDocument()
    })
  })

  it('muestra iconos según el tipo de notificación', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('⏰')).toBeInTheDocument() // EVENT_STARTING
      expect(screen.getByText('💬')).toBeInTheDocument() // NEW_MESSAGE
      expect(screen.getByText('✅')).toBeInTheDocument() // JOINED_EVENT
    })
  })

  it('muestra el botón "Marcar todas como leídas" si hay no leídas', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('Marcar todas como leídas')).toBeInTheDocument()
    })
  })

  it('llama a markAllAsRead al hacer click en el botón', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    notificationService.markAllAsRead.mockResolvedValue({ ok: true })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('Marcar todas como leídas')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByText('Marcar todas como leídas'))
    expect(notificationService.markAllAsRead).toHaveBeenCalled()
  })

  it('elimina una notificación al hacer click en el botón eliminar', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    notificationService.deleteNotification.mockResolvedValue({ ok: true })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('Tu evento comienza en 30 minutos')).toBeInTheDocument()
    })

    const deleteButtons = screen.getAllByLabelText('Eliminar notificación')
    await userEvent.click(deleteButtons[0])

    expect(notificationService.deleteNotification).toHaveBeenCalledWith('1')
  })

  it('navega a evento al hacer click en notificación con eventId', async () => {
    const onNavigate = vi.fn()
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    notificationService.markAsRead.mockResolvedValue({ ok: true })
    render(<Notifications onNavigate={onNavigate} />)

    await waitFor(() => {
      expect(screen.getByText('Tu evento comienza en 30 minutos')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByText('Tu evento comienza en 30 minutos'))
    
    expect(onNavigate).toHaveBeenCalledWith('explore', { eventId: 'e1' })
  })

  it('muestra tiempo relativo correctamente', async () => {
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText(/hace 5 minutos/i)).toBeInTheDocument()
      expect(screen.getByText(/hace 2 horas/i)).toBeInTheDocument()
      expect(screen.getByText(/hace 1 día/i)).toBeInTheDocument()
    })
  })

  it('carga notificaciones locales junto con las del backend', async () => {
    const localNotif = {
      id: 'local-1',
      type: 'LEFT_EVENT',
      message: 'Has salido del evento',
      timestamp: new Date().toISOString(),
      read: false,
      isLocal: true
    }
    
    localNotificationService.getLocalNotifications.mockReturnValue([localNotif])
    notificationService.getNotifications.mockResolvedValue({ ok: true, data: mockNotifications })
    
    render(<Notifications />)

    await waitFor(() => {
      expect(screen.getByText('Has salido del evento')).toBeInTheDocument()
    })
  })
})
