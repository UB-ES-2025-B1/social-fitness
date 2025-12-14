import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatInput from '../ChatInput'

describe('ChatInput', () => {
  it('renderiza el campo de entrada y el botón', () => {
    render(<ChatInput onSend={vi.fn()} />)

    expect(screen.getByPlaceholderText('Escribe un mensaje...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar' })).toBeInTheDocument()
  })

  it('permite escribir en el campo de texto', async () => {
    render(<ChatInput onSend={vi.fn()} />)

    const input = screen.getByPlaceholderText('Escribe un mensaje...')
    await userEvent.type(input, 'Hola mundo')

    expect(input).toHaveValue('Hola mundo')
  })

  it('llama a onSend con el texto al enviar', async () => {
    const onSend = vi.fn()
    render(<ChatInput onSend={onSend} />)

    const input = screen.getByPlaceholderText('Escribe un mensaje...')
    await userEvent.type(input, 'Hola mundo')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(onSend).toHaveBeenCalledWith('Hola mundo')
  })

  it('limpia el campo después de enviar', async () => {
    render(<ChatInput onSend={vi.fn()} />)

    const input = screen.getByPlaceholderText('Escribe un mensaje...')
    await userEvent.type(input, 'Hola mundo')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(input).toHaveValue('')
  })

  it('no llama a onSend si el texto está vacío', async () => {
    const onSend = vi.fn()
    render(<ChatInput onSend={onSend} />)

    await userEvent.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(onSend).not.toHaveBeenCalled()
  })

  it('no llama a onSend si el texto solo tiene espacios', async () => {
    const onSend = vi.fn()
    render(<ChatInput onSend={onSend} />)

    const input = screen.getByPlaceholderText('Escribe un mensaje...')
    await userEvent.type(input, '   ')
    await userEvent.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(onSend).not.toHaveBeenCalled()
  })

  it('envía al presionar Enter', async () => {
    const onSend = vi.fn()
    render(<ChatInput onSend={onSend} />)

    const input = screen.getByPlaceholderText('Escribe un mensaje...')
    await userEvent.type(input, 'Hola{enter}')

    expect(onSend).toHaveBeenCalledWith('Hola')
  })
})
