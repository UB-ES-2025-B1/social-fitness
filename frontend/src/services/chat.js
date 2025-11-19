import { request } from './api'

export async function getChatMessages(eventId) {
  return request(`/events/${eventId}/chat/messages`, {
    method: 'GET'
  })
}

export async function sendChatMessage(eventId, text) {
  return request(`/events/${eventId}/chat/messages`, {
    method: 'POST',
    body: { text }
  })
}

export async function checkParticipant(eventId, userId) {
  return request(`/events/${eventId}/participants/${userId}`, {
    method: 'GET'
  })
}
