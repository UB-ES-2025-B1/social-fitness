import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../api', () => ({ request: vi.fn() }))

import { request } from '../api'
import { getChatMessages, sendChatMessage, checkParticipant } from '../chat'

beforeEach(() => {
  vi.clearAllMocks()
})

describe('chat service', () => {
  it('getChatMessages construye endpoint de chat', async () => {
    request.mockResolvedValue({ ok: true })
    await getChatMessages(3)
    expect(request).toHaveBeenCalledWith('/events/3/chat/messages', { method: 'GET' })
  })

  it('sendChatMessage envía el mensaje', async () => {
    request.mockResolvedValue({ ok: true })
    await sendChatMessage(3, 'hola')
    expect(request).toHaveBeenCalledWith('/events/3/chat/messages', { method: 'POST', body: { text: 'hola' } })
  })

  it('checkParticipant construye endpoint de participants', async () => {
    request.mockResolvedValue({ ok: true })
    await checkParticipant(3, 99)
    expect(request).toHaveBeenCalledWith('/events/3/participants/99', { method: 'GET' })
  })
})

