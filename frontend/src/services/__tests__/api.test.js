import { describe, it, expect, vi, beforeAll, beforeEach, afterEach, afterAll } from 'vitest'

let request
let originalFetch

beforeAll(async () => {
  originalFetch = globalThis.fetch
  vi.spyOn(console, 'log').mockImplementation(() => {})
  ;({ request } = await import('../api'))
})

afterAll(() => {
  globalThis.fetch = originalFetch
  vi.restoreAllMocks()
})

beforeEach(() => {
  globalThis.fetch = vi.fn()
})

afterEach(() => {
  globalThis.fetch = originalFetch
  vi.clearAllMocks()
})

describe('api.request', () => {
  it('hace fetch y parsea JSON', async () => {
    fetch.mockResolvedValue({
      ok: true,
      status: 200,
      text: vi.fn().mockResolvedValue(JSON.stringify({ hello: 'world' })),
    })

    const res = await request('/ping')

    expect(res.ok).toBe(true)
    expect(res.status).toBe(200)
    expect(res.data).toEqual({ hello: 'world' })

    expect(fetch).toHaveBeenCalledTimes(1)
    const [url, opts] = fetch.mock.calls[0]
    expect(url).toMatch(/\/ping$/)
    expect(opts).toEqual(
      expect.objectContaining({
        method: 'GET',
        credentials: 'include',
        headers: expect.objectContaining({ 'Content-Type': 'application/json' }),
      }),
    )
  })

  it('stringifica body y respeta method/credentials', async () => {
    fetch.mockResolvedValue({
      ok: true,
      status: 201,
      text: vi.fn().mockResolvedValue(''),
    })

    await request('/submit', { method: 'POST', body: { a: 1 }, credentials: 'omit' })

    const [url, opts] = fetch.mock.calls[0]
    expect(url).toMatch(/\/submit$/)
    expect(opts.method).toBe('POST')
    expect(opts.credentials).toBe('omit')
    expect(opts.body).toBe(JSON.stringify({ a: 1 }))
  })

  it('permite sobrescribir headers (incluyendo Content-Type)', async () => {
    fetch.mockResolvedValue({
      ok: true,
      status: 204,
      text: vi.fn().mockResolvedValue(''),
    })

    await request('/x', { headers: { 'Content-Type': 'text/plain', 'X-Test': '1' } })

    const [, opts] = fetch.mock.calls[0]
    expect(opts.headers).toEqual(
      expect.objectContaining({
        'Content-Type': 'text/plain',
        'X-Test': '1',
      }),
    )
  })

  it('si la respuesta no es JSON, devuelve texto', async () => {
    fetch.mockResolvedValue({
      ok: false,
      status: 500,
      text: vi.fn().mockResolvedValue('NOT_JSON'),
    })

    const res = await request('/err')
    expect(res.data).toBe('NOT_JSON')
  })

  it('si la respuesta es vacía, devuelve null', async () => {
    fetch.mockResolvedValue({
      ok: true,
      status: 200,
      text: vi.fn().mockResolvedValue(''),
    })

    const res = await request('/empty')
    expect(res.data).toBeNull()
  })
})

