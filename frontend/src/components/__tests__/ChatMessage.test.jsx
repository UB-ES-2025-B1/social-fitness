import { describe, it, expect, vi, beforeAll } from 'vitest'
import { render, screen } from '@testing-library/react'
import ChatMessage from '../ChatMessage'

describe('ChatMessage', () => {
  const mockMessage = {
    id: 'm1',
    username: 'Juan',
    text: 'Hola, ¿cómo estás?',
    timestamp: '2025-12-13T10:30:00.000Z'
  }

  beforeAll(() => {
    // Mock de toLocaleTimeString para tests consistentes
    vi.spyOn(Date.prototype, 'toLocaleTimeString').mockReturnValue('10:30')
  })

  it('muestra el nombre de usuario', () => {
    render(<ChatMessage message={mockMessage} own={false} />)

    expect(screen.getByText('Juan')).toBeInTheDocument()
  })

  it('muestra el texto del mensaje', () => {
    render(<ChatMessage message={mockMessage} own={false} />)

    expect(screen.getByText('Hola, ¿cómo estás?')).toBeInTheDocument()
  })

  it('muestra la hora del mensaje', () => {
    render(<ChatMessage message={mockMessage} own={false} />)

    expect(screen.getByText('10:30')).toBeInTheDocument()
  })

  it('aplica clase "own" cuando es mensaje propio', () => {
    const { container } = render(<ChatMessage message={mockMessage} own={true} />)

    const messageDiv = container.querySelector('.chat-message')
    expect(messageDiv).toHaveClass('own')
  })

  it('no aplica clase "own" cuando no es mensaje propio', () => {
    const { container } = render(<ChatMessage message={mockMessage} own={false} />)

    const messageDiv = container.querySelector('.chat-message')
    expect(messageDiv).not.toHaveClass('own')
  })

  it('renderiza correctamente con diferentes mensajes', () => {
    const anotherMessage = {
      id: 'm2',
      username: 'María',
      text: 'Todo bien, ¿y tú?',
      timestamp: '2025-12-13T10:35:00.000Z'
    }

    render(<ChatMessage message={anotherMessage} own={false} />)

    expect(screen.getByText('María')).toBeInTheDocument()
    expect(screen.getByText('Todo bien, ¿y tú?')).toBeInTheDocument()
  })
})
