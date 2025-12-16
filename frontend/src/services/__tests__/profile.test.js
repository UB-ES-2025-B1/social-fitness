import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { saveProfile, getProfile, uploadAvatar } from '../profile'

let originalFetch

beforeEach(() => {
  vi.clearAllMocks()
  localStorage.clear()
  originalFetch = globalThis.fetch
})

afterEach(() => {
  globalThis.fetch = originalFetch
})

describe('profile service', () => {
  it('saveProfile normaliza profileImage -> avatarUrl', async () => {
    request.mockResolvedValue({
      ok: true,
      status: 200,
      data: { profileImage: '/uploads/a.png' },
    })

    const res = await saveProfile(1, { sports: [], bio: 'hola', profileImage: '/uploads/a.png' })

    expect(request).toHaveBeenCalledWith('/profile/1', {
      method: 'POST',
      body: { sports: [], bio: 'hola', profileImage: '/uploads/a.png' },
    })
    expect(res.data.avatarUrl).toBe('/uploads/a.png')
  })

  it('getProfile normaliza profileImage -> avatarUrl', async () => {
    request.mockResolvedValue({
      ok: true,
      status: 200,
      data: { profileImage: '/uploads/b.png' },
    })

    const res = await getProfile(2)
    expect(request).toHaveBeenCalledWith('/users/2', { method: 'GET' })
    expect(res.data.avatarUrl).toBe('/uploads/b.png')
  })

  it('uploadAvatar requiere file', async () => {
    await expect(uploadAvatar(1)).rejects.toThrow('File is required')
  })

  it('uploadAvatar usa multipart y normaliza profileImage -> avatarUrl', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 201,
      text: vi.fn().mockResolvedValue(JSON.stringify({ profileImage: '/uploads/c.png' })),
    })

    const file = new File(['x'], 'avatar.png', { type: 'image/png' })
    const res = await uploadAvatar(3, file)

    expect(fetch).toHaveBeenCalledTimes(1)
    const [url, opts] = fetch.mock.calls[0]
    expect(url).toMatch(/\/profile\/3\/avatar$/)
    expect(opts).toEqual(
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: expect.any(FormData),
      }),
    )
    expect(res.ok).toBe(true)
    expect(res.data.avatarUrl).toBe('/uploads/c.png')
  })

  it('uploadAvatar devuelve texto si la respuesta no es JSON', async () => {
    globalThis.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 500,
      text: vi.fn().mockResolvedValue('NOT_JSON'),
    })

    const file = new File(['x'], 'avatar.png', { type: 'image/png' })
    const res = await uploadAvatar(3, file)
    expect(res.data).toBe('NOT_JSON')
  })
})

