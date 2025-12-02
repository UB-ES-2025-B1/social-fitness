import { request } from './api'

/**
 * Get all notifications for the authenticated user
 * @returns {Promise<Array>} Array of notification objects
 */
export async function getNotifications() {
  return request('/notifications')
}

/**
 * Get the count of unread notifications
 * @returns {Promise<Object>} Object with count property
 */
export async function getUnreadCount() {
  return request('/notifications/unread-count')
}

/**
 * Mark a specific notification as read
 * @param {string|number} notificationId - The ID of the notification
 * @returns {Promise<Object>} Success message
 */
export async function markAsRead(notificationId) {
  return request(`/notifications/${notificationId}/read`, { method: 'PUT' })
}

/**
 * Mark all notifications as read
 * @returns {Promise<Object>} Success message with count
 */
export async function markAllAsRead() {
  return request('/notifications/read-all', { method: 'PUT' })
}

/**
 * Delete a specific notification
 * @param {string|number} notificationId - The ID of the notification
 * @returns {Promise<Object>} Success message
 */
export async function deleteNotification(notificationId) {
  return request(`/notifications/${notificationId}`, { method: 'DELETE' })
}
