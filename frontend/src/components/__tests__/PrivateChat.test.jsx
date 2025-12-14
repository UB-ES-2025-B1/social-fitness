import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PrivateChat from '../PrivateChat'

// Mocks de servicios
vi.mock('../../services/directMessages', () => ({
  getDirectMessages: vi.fn(),
  sendDirectMessage: vi.fn()
}))

// Mock de componentes hijos
vi.mock('../ChatMessage', () => ({
  default: ({ message, own }) => (
    <div data-testid="chat-message" data-own={own}>
      <span>{message.username}</span>: <span>{message.text}</span>
    </div>
  )
}))

vi.mock('../ChatInput', () => ({
  default: ({ onSend, disabled }) => (
    <div data-testid="chat-input">
      <input 
        data-testid="chat-input-field"
        disabled={disabled}
        onChange={() => {}}
      />
      <button 
        data-testid="send-button"
        onClick={() => onSend('Test message')}
        disabled={disabled}
      >
        Enviar
      </button>
    </div>
  )
}))

import { getDirectMessages, sendDirectMessage } from '../../services/directMessages'

describe('PrivateChat', () => {
  const mockUser = {
    id: 'u1',
    username: 'Juan',
    profileImage: '/img/juan.jpg'
  }

  const mockMessages = [
    {
      id: 'm1',
      senderId: 'u1',
      senderUsername: 'Juan',
      text: 'Hola!',
      timestamp: new Date().toISOString()
    },
    {
      id: 'm2',
      senderId: 'currentUser',
      senderUsername: 'Yo',
      text: 'Hola, ¿qué tal?',
      timestamp: new Date().toISOString()
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('userId', 'currentUser')
    getDirectMessages.mockResolvedValue({ ok: true, data: mockMessages })
    sendDirectMessage.mockResolvedValue({ 
      ok: true, 
      data: { id: 'm3', senderId: 'currentUser', text: 'Test message', timestamp: new Date().toISOString() }
    })
  })

  it('muestra mensaje de error si no hay usuario seleccionado', () => {
    render(<PrivateChat user={null} onBack={vi.fn()} />)

    expect(screen.getByText('Usuario no seleccionado')).toBeInTheDocument()
  })

  it('muestra el nombre del usuario en el header', async () => {
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    expect(screen.getByText('Juan')).toBeInTheDocument()
  })

  it('muestra estado de carga mientras carga mensajes', () => {
    getDirectMessages.mockImplementation(() => new Promise(() => {}))
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    expect(screen.getByText('Cargando mensajes...')).toBeInTheDocument()
  })

  it('muestra los mensajes correctamente', async () => {
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    await waitFor(() => {
      const messages = screen.getAllByTestId('chat-message')
      expect(messages).toHaveLength(2)
    })
  })

  it('muestra mensaje vacío cuando no hay mensajes', async () => {
    getDirectMessages.mockResolvedValue({ ok: true, data: [] })
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('No hay mensajes aún')).toBeInTheDocument()
      expect(screen.getByText('Escribe un mensaje para iniciar la conversación')).toBeInTheDocument()
    })
  })

  it('llama a onBack cuando se hace click en el botón volver', async () => {
    const onBack = vi.fn()
    render(<PrivateChat user={mockUser} onBack={onBack} />)

    await userEvent.click(screen.getByText('←'))
    expect(onBack).toHaveBeenCalledTimes(1)
  })

  it('envía mensaje correctamente', async () => {
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByTestId('chat-input')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByTestId('send-button'))

    expect(sendDirectMessage).toHaveBeenCalledWith('u1', 'Test message')
  })

  it('muestra error cuando falla la carga de mensajes', async () => {
    getDirectMessages.mockResolvedValue({ ok: false, data: null })
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    await waitFor(() => {
      expect(screen.getByText('Error al cargar los mensajes')).toBeInTheDocument()
    })
  })

  it('muestra avatar del usuario', async () => {
    render(<PrivateChat user={mockUser} onBack={vi.fn()} />)

    const avatar = screen.getByAltText('Juan')
    expect(avatar).toHaveAttribute('src', '/img/juan.jpg')
  })

  it('usa avatar por defecto si el usuario no tiene imagen', async () => {
    const userWithoutImage = { ...mockUser, profileImage: null }
    render(<PrivateChat user={userWithoutImage} onBack={vi.fn()} />)

    const avatar = screen.getByAltText('Juan')
    expect(avatar).toHaveAttribute('src', '/img/user-profile-icon-profile.png')
  })
})
