import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('react-dom/client', () => {
  return {
    createRoot: vi.fn(() => ({ render: vi.fn() })),
  }
})

vi.mock('../App.jsx', () => ({ default: () => null }))

describe('main.jsx', () => {
  beforeEach(() => {
    vi.resetModules()
    document.body.innerHTML = '<div id="root"></div>'
  })

  it('monta la app en #root', async () => {
    const { createRoot } = await import('react-dom/client')

    await import('../main.jsx')

    expect(createRoot).toHaveBeenCalledTimes(1)
    expect(createRoot).toHaveBeenCalledWith(document.getElementById('root'))

    const root = createRoot.mock.results[0].value
    expect(root.render).toHaveBeenCalledTimes(1)
  })
})

