import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { login, register } from '../auth'

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
})

describe('auth service', () => {
  it('login guarda userId y username en localStorage cuando ok', async () => {
    request.mockResolvedValue({
      ok: true,
      status: 200,
      data: { user: { id: 123, username: 'pepe' } },
    })

    const res = await login({ username: 'pepe', password: 'secret' })

    expect(request).toHaveBeenCalledWith('/auth/login', {
      method: 'POST',
      body: { username: 'pepe', password: 'secret' },
    })
    expect(res.ok).toBe(true)
    expect(localStorage.getItem('userId')).toBe('123')
    expect(localStorage.getItem('username')).toBe('pepe')
  })

  it('login NO guarda nada si la respuesta no es ok o no hay user', async () => {
    request.mockResolvedValue({ ok: false, status: 401, data: { message: 'bad' } })
    await login({ username: 'x', password: 'y' })
    expect(localStorage.getItem('userId')).toBeNull()

    request.mockResolvedValue({ ok: true, status: 200, data: {} })
    await login({ username: 'x', password: 'y' })
    expect(localStorage.getItem('userId')).toBeNull()
  })

  it('register llama al endpoint correcto', async () => {
    request.mockResolvedValue({ ok: true, status: 201, data: { message: 'ok' } })

    await register({ username: 'ana', email: 'ana@example.com', password: 'pass' })

    expect(request).toHaveBeenCalledWith('/auth/register', {
      method: 'POST',
      body: { username: 'ana', email: 'ana@example.com', password: 'pass' },
    })
  })
})

