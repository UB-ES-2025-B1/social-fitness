import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'

vi.mock('../services/auth', () => ({ login: vi.fn(), register: vi.fn() }))
vi.mock('../services/notifications', () => ({ getUnreadCount: vi.fn() }))
vi.mock('../services/localNotifications', () => ({ getUnreadLocalCount: vi.fn() }))

vi.mock('../components/EventExplorer', () => ({ default: () => <div>EventExplorerMock</div> }))
vi.mock('../components/ProfileConfigurator', () => ({ default: () => <div>ProfileConfiguratorMock</div> }))

import App from '../App'
import * as auth from '../services/auth'
import * as notifications from '../services/notifications'
import * as localNotifications from '../services/localNotifications'

beforeEach(() => {
  localStorage.clear()
  vi.clearAllMocks()
  window.history.pushState({}, '', '/')

  notifications.getUnreadCount.mockResolvedValue({ ok: true, data: { count: 0 } })
  localNotifications.getUnreadLocalCount.mockReturnValue(0)
})

describe('App', () => {
  it('muestra login por defecto y valida campos requeridos', async () => {
    render(<App />)

    expect(screen.getByRole('region', { name: /login form/i })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /iniciar/i }))

    expect(auth.login).not.toHaveBeenCalled()
    expect(screen.getByText('Username is required')).toBeInTheDocument()
    expect(screen.getByText('Password is required')).toBeInTheDocument()
  })

  it('permite cambiar a register y valida campos', async () => {
    render(<App />)

    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }))

    expect(screen.getByRole('region', { name: /register form/i })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: /^crear cuenta$/i }))

    expect(auth.register).not.toHaveBeenCalled()
    expect(screen.getByText('Username is required')).toBeInTheDocument()
    expect(screen.getByText('Email is required')).toBeInTheDocument()
    expect(screen.getByText('Password is required')).toBeInTheDocument()
  })

  it('login OK llama al servicio y cambia a explore', async () => {
    auth.login.mockResolvedValue({ ok: true, status: 200, data: { user: { id: 5 } } })

    render(<App />)

    await userEvent.type(screen.getByLabelText(/nombre de usuario/i), 'pepe')
    await userEvent.type(screen.getByPlaceholderText(/Introduce tu contrase/i), 'password123')

    await userEvent.click(screen.getByRole('button', { name: /iniciar/i }))

    expect(auth.login).toHaveBeenCalledWith({ username: 'pepe', password: 'password123' })

    expect(await screen.findByRole('region', { name: /explore form/i })).toBeInTheDocument()
    expect(screen.getByText('EventExplorerMock')).toBeInTheDocument()

    expect(localStorage.getItem('userId')).toBe('5')
    expect(localStorage.getItem('username')).toBe('pepe')
  })

  it('register OK llama al servicio y cambia a profile-config', async () => {
    auth.register.mockResolvedValue({ ok: true, status: 201, data: { user: { id: 7 } } })

    render(<App />)

    await userEvent.click(screen.getByRole('button', { name: /crear cuenta/i }))

    await userEvent.type(screen.getByLabelText(/nombre de usuario/i), 'ana')
    await userEvent.type(screen.getByLabelText(/correo/i), 'ana@example.com')
    await userEvent.type(screen.getByPlaceholderText(/Elige una contrase/i), 'password123')
    await userEvent.type(screen.getByPlaceholderText(/Repite tu contrase/i), 'password123')

    await userEvent.click(screen.getByRole('button', { name: /^crear cuenta$/i }))

    expect(auth.register).toHaveBeenCalledWith({
      username: 'ana',
      email: 'ana@example.com',
      password: 'password123',
    })

    expect(await screen.findByRole('region', { name: /profile-config form/i })).toBeInTheDocument()
    expect(screen.getByText('ProfileConfiguratorMock')).toBeInTheDocument()

    expect(localStorage.getItem('userId')).toBe('7')
    expect(localStorage.getItem('username')).toBe('ana')
    expect(localStorage.getItem('email')).toBe('ana@example.com')
  })

  it('suma backend + local para el badge de notificaciones', async () => {
    window.history.pushState({}, '', '/?dev=explore')
    notifications.getUnreadCount.mockResolvedValue({ ok: true, data: { count: 2 } })
    localNotifications.getUnreadLocalCount.mockReturnValue(1)

    render(<App />)

    expect(screen.getByRole('region', { name: /explore form/i })).toBeInTheDocument()
    expect(await screen.findByText('3')).toBeInTheDocument()
  })
})

