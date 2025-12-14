import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { getChats, searchUsers, getDirectMessages, sendDirectMessage } from '../directMessages'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('directMessages service', () => {
  it('getChats llama a /messages/chats', async () => {
    request.mockResolvedValue({ ok: true })
    await getChats()
    expect(request).toHaveBeenCalledWith('/messages/chats', { method: 'GET' })
  })

  it('searchUsers encodea el query', async () => {
    request.mockResolvedValue({ ok: true })
    await searchUsers('a/b c?')
    expect(request).toHaveBeenCalledWith('/messages/users/search?q=a%2Fb%20c%3F', { method: 'GET' })
  })

  it('getDirectMessages y sendDirectMessage usan /messages/users/:id', async () => {
    request.mockResolvedValue({ ok: true })

    await getDirectMessages(2)
    expect(request).toHaveBeenCalledWith('/messages/users/2', { method: 'GET' })

    await sendDirectMessage(2, 'hola')
    expect(request).toHaveBeenCalledWith('/messages/users/2', { method: 'POST', body: { text: 'hola' } })
  })
})

