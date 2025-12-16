import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import DirectMessages from '../DirectMessages'

// Mock del servicio
vi.mock('../../services/directMessages', () => ({
  getChats: vi.fn()
}))

import { getChats } from '../../services/directMessages'

describe('DirectMessages', () => {
  const mockChats = [
    {
      id: '1',
      otherUser: {
        id: 'u1',
        username: 'usuario1',
        profileImage: '/img/avatar1.jpg'
      },
      lastMessage: {
        text: 'Hola, ¿quedamos mañana?',
        timestamp: new Date(Date.now() - 1000 * 60 * 30).toISOString() // 30 min ago
      },
      unreadCount: 2
    },
    {
      id: '2',
      otherUser: {
        id: 'u2',
        username: 'usuario2',
        profileImage: null
      },
      lastMessage: {
        text: 'Perfecto!',
        timestamp: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString() // 5 hours ago
      },
      unreadCount: 0
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra estado de carga inicialmente', () => {
    getChats.mockImplementation(() => new Promise(() => {}))
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    expect(screen.getByText('Cargando chats...')).toBeInTheDocument()
  })

  it('muestra el título "Mensajes"', async () => {
    getChats.mockResolvedValue({ ok: true, data: [] })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    expect(screen.getByRole('heading', { name: 'Mensajes' })).toBeInTheDocument()
  })

  it('muestra lista vacía cuando no hay chats', async () => {
    getChats.mockResolvedValue({ ok: true, data: [] })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('No tienes conversaciones aún')).toBeInTheDocument()
    })
  })

  it('muestra la lista de chats correctamente', async () => {
    getChats.mockResolvedValue({ ok: true, data: mockChats })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('usuario1')).toBeInTheDocument()
      expect(screen.getByText('usuario2')).toBeInTheDocument()
      expect(screen.getByText('Hola, ¿quedamos mañana?')).toBeInTheDocument()
    })
  })

  it('muestra el badge de mensajes no leídos', async () => {
    getChats.mockResolvedValue({ ok: true, data: mockChats })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('2')).toBeInTheDocument() // unreadCount
    })
  })

  it('llama a onSelectChat al hacer click en un chat', async () => {
    const onSelectChat = vi.fn()
    getChats.mockResolvedValue({ ok: true, data: mockChats })
    render(<DirectMessages onSelectChat={onSelectChat} onNewChat={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('usuario1')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByText('usuario1'))
    expect(onSelectChat).toHaveBeenCalledWith(mockChats[0].otherUser)
  })

  it('llama a onNewChat al hacer click en el botón nuevo chat', async () => {
    const onNewChat = vi.fn()
    getChats.mockResolvedValue({ ok: true, data: mockChats })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={onNewChat} />)

    await waitFor(() => {
      expect(screen.getByTitle('Nuevo mensaje')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByTitle('Nuevo mensaje'))
    expect(onNewChat).toHaveBeenCalledTimes(1)
  })

  it('muestra error cuando falla la carga', async () => {
    getChats.mockResolvedValue({ ok: false, data: null })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('Error al cargar los chats')).toBeInTheDocument()
    })
  })

  it('muestra botón "Iniciar conversación" cuando no hay chats', async () => {
    const onNewChat = vi.fn()
    getChats.mockResolvedValue({ ok: true, data: [] })
    render(<DirectMessages onSelectChat={vi.fn()} onNewChat={onNewChat} />)

    await waitFor(() => {
      const btn = screen.getByRole('button', { name: 'Iniciar conversación' })
      expect(btn).toBeInTheDocument()
    })

    await userEvent.click(screen.getByRole('button', { name: 'Iniciar conversación' }))
    expect(onNewChat).toHaveBeenCalled()
  })
})
