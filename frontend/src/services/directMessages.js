import { request } from './api'

/**
 * Get all existing direct message conversations for the authenticated user
 * @returns {Promise} Array of chat conversations with last message and unread count
 */
export async function getChats() {
  return request('/messages/chats', {
    method: 'GET'
  })
}

/**
 * Search for users by username to start a new conversation
 * @param {string} query - Username search query
 * @returns {Promise} Array of users matching the search
 */
export async function searchUsers(query) {
  return request(`/messages/users/search?q=${encodeURIComponent(query)}`, {
    method: 'GET'
  })
}

/**
 * Get direct message history with a specific user
 * @param {string} userId - ID of the other user
 * @returns {Promise} Array of messages between users
 */
export async function getDirectMessages(userId) {
  return request(`/messages/users/${userId}`, {
    method: 'GET'
  })
}

/**
 * Send a direct message to a specific user
 * @param {string} userId - ID of the recipient
 * @param {string} text - Message content
 * @returns {Promise} Created message object
 */
export async function sendDirectMessage(userId, text) {
  return request(`/messages/users/${userId}`, {
    method: 'POST',
    body: { text }
  })
}
