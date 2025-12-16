import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EventChatModal from '../EventDetail'

// Mock de servicios
vi.mock('../../services/events', () => ({
  getEvent: vi.fn()
}))

vi.mock('../EventChat', () => ({
  default: () => <div data-testid="event-chat">Chat Mock</div>
}))

import { getEvent } from '../../services/events'

describe('EventChatModal (EventDetail)', () => {
  const mockEvent = {
    id: '1',
    title: 'Partido de Fútbol',
    sport: 'Fútbol',
    date: 'lun, 15 dic',
    time: '18:00',
    location: 'Parque Central',
    organizer: 'Carlos M.',
    participants: 5,
    capacity: 10
  }

  const mockEventDetails = {
    ...mockEvent,
    participants: [
      { id: '1', name: 'Juan', profileImage: '/img/juan.jpg' },
      { id: '2', name: 'María', profileImage: null }
    ]
  }

  beforeEach(() => {
    vi.clearAllMocks()
    getEvent.mockResolvedValue({ ok: true, data: mockEventDetails })
  })

  it('no renderiza nada si event es null', () => {
    const { container } = render(<EventChatModal event={null} onClose={vi.fn()} />)
    expect(container.firstChild).toBeNull()
  })

  it('muestra la información del evento', async () => {
    render(<EventChatModal event={mockEvent} onClose={vi.fn()} />)

    expect(screen.getByText('Chat del evento')).toBeInTheDocument()
    expect(screen.getByText('Partido de Fútbol')).toBeInTheDocument()
    expect(screen.getByText(/Deporte:/)).toBeInTheDocument()
    expect(screen.getByText(/lun, 15 dic/)).toBeInTheDocument()
    expect(screen.getByText(/18:00/)).toBeInTheDocument()
    expect(screen.getByText(/Parque Central/)).toBeInTheDocument()
    expect(screen.getByText(/Carlos M./)).toBeInTheDocument()
  })

  it('muestra los participantes cuando se cargan', async () => {
    render(<EventChatModal event={mockEvent} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('Juan')).toBeInTheDocument()
      expect(screen.getByText('María')).toBeInTheDocument()
    })
  })

  it('muestra estado de carga de participantes', () => {
    getEvent.mockImplementation(() => new Promise(() => {})) // Never resolves
    render(<EventChatModal event={mockEvent} onClose={vi.fn()} />)

    expect(screen.getByText('Cargando participantes...')).toBeInTheDocument()
  })

  it('llama a onClose cuando se hace click en cerrar', async () => {
    const onClose = vi.fn()
    render(<EventChatModal event={mockEvent} onClose={onClose} />)

    const closeButton = screen.getByLabelText('Cerrar')
    await userEvent.click(closeButton)

    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('renderiza el componente EventChat', async () => {
    render(<EventChatModal event={mockEvent} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByTestId('event-chat')).toBeInTheDocument()
    })
  })

  it('muestra mensaje cuando no hay participantes', async () => {
    getEvent.mockResolvedValue({ 
      ok: true, 
      data: { ...mockEvent, participants: [] } 
    })
    
    render(<EventChatModal event={mockEvent} onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('No hay participantes todavía')).toBeInTheDocument()
    })
  })
})
