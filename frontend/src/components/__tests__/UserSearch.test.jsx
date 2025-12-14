import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import UserSearch from '../UserSearch'

// Mock del servicio
vi.mock('../../services/directMessages', () => ({
  searchUsers: vi.fn()
}))

import { searchUsers } from '../../services/directMessages'

describe('UserSearch', () => {
  const mockUsers = [
    {
      id: 'u1',
      username: 'juan_perez',
      profileImage: '/img/juan.jpg'
    },
    {
      id: 'u2',
      username: 'juanito123',
      profileImage: null
    }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
    searchUsers.mockResolvedValue({ ok: true, data: mockUsers })
  })

  it('muestra el título "Nuevo mensaje"', () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    expect(screen.getByRole('heading', { name: 'Nuevo mensaje' })).toBeInTheDocument()
  })

  it('muestra el campo de búsqueda', () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    expect(screen.getByPlaceholderText('Buscar por nombre de usuario...')).toBeInTheDocument()
  })

  it('llama a onBack al hacer click en "Volver"', async () => {
    const onBack = vi.fn()
    render(<UserSearch onSelectUser={vi.fn()} onBack={onBack} />)

    await userEvent.click(screen.getByText('← Volver'))
    expect(onBack).toHaveBeenCalledTimes(1)
  })

  it('muestra error si se busca sin texto', async () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(screen.getByText('Ingresa un nombre de usuario')).toBeInTheDocument()
    expect(searchUsers).not.toHaveBeenCalled()
  })

  it('realiza búsqueda y muestra resultados', async () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(searchUsers).toHaveBeenCalledWith('juan')
      expect(screen.getByText('juan_perez')).toBeInTheDocument()
      expect(screen.getByText('juanito123')).toBeInTheDocument()
    })
  })

  it('muestra contador de resultados', async () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('Resultados (2)')).toBeInTheDocument()
    })
  })

  it('muestra mensaje cuando no hay resultados', async () => {
    searchUsers.mockResolvedValue({ ok: true, data: [] })
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'xyz')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('No se encontraron usuarios con "xyz"')).toBeInTheDocument()
    })
  })

  it('llama a onSelectUser al hacer click en un usuario', async () => {
    const onSelectUser = vi.fn()
    render(<UserSearch onSelectUser={onSelectUser} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('juan_perez')).toBeInTheDocument()
    })

    await userEvent.click(screen.getByText('juan_perez'))
    expect(onSelectUser).toHaveBeenCalledWith(mockUsers[0])
  })

  it('muestra estado de carga durante la búsqueda', async () => {
    searchUsers.mockImplementation(() => new Promise(() => {}))
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(screen.getByText('Buscando...')).toBeInTheDocument()
  })

  it('muestra error cuando falla la búsqueda', async () => {
    searchUsers.mockResolvedValue({ ok: false, data: null })
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('Error al buscar usuarios')).toBeInTheDocument()
    })
  })

  it('usa avatar por defecto cuando el usuario no tiene imagen', async () => {
    render(<UserSearch onSelectUser={vi.fn()} onBack={vi.fn()} />)

    await userEvent.type(screen.getByPlaceholderText('Buscar por nombre de usuario...'), 'juan')
    await userEvent.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      const avatars = screen.getAllByRole('img')
      // juanito123 no tiene profileImage
      const avatar = avatars.find(img => img.alt === 'juanito123')
      expect(avatar).toHaveAttribute('src', '/img/user-profile-icon-profile.png')
    })
  })
})
