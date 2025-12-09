/**
 * Local notification management (frontend-only, no backend)
 * These notifications are stored in localStorage and displayed alongside backend notifications
 */

function getStorageKey() {
  const userId = localStorage.getItem('userId')
  return `local_notifications:${userId || 'anon'}`
}

/**
 * Get all local notifications from localStorage
 * @returns {Array} Array of local notification objects
 */
export function getLocalNotifications() {
  try {
    const stored = localStorage.getItem(getStorageKey())
    return stored ? JSON.parse(stored) : []
  } catch (err) {
    console.error('Error reading local notifications:', err)
    return []
  }
}

/**
 * Add a new local notification
 * @param {Object} notification - Notification object with type, message, etc.
 * @returns {Object} The created notification with id and timestamp
 */
export function addLocalNotification({ type, message, eventId = null, eventTitle = null }) {
  try {
    const notifications = getLocalNotifications()
    
    const newNotification = {
      id: `local_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      type,
      message,
      eventId,
      eventTitle,
      read: false,
      timestamp: new Date().toISOString(),
      isLocal: true // Flag to distinguish from backend notifications
    }
    
    notifications.unshift(newNotification) // Add to beginning
    
    // Keep only last 50 local notifications
    const trimmed = notifications.slice(0, 50)
    
    localStorage.setItem(getStorageKey(), JSON.stringify(trimmed))
    
    // Dispatch custom event so other components can react
    window.dispatchEvent(new CustomEvent('localNotificationAdded', { 
      detail: newNotification 
    }))
    
    return newNotification
  } catch (err) {
    console.error('Error adding local notification:', err)
    return null
  }
}

/**
 * Mark a local notification as read
 * @param {string} notificationId - The notification ID
 */
export function markLocalNotificationAsRead(notificationId) {
  try {
    const notifications = getLocalNotifications()
    const updated = notifications.map(n => 
      n.id === notificationId ? { ...n, read: true } : n
    )
    localStorage.setItem(getStorageKey(), JSON.stringify(updated))
    
    window.dispatchEvent(new CustomEvent('localNotificationUpdated'))
  } catch (err) {
    console.error('Error marking local notification as read:', err)
  }
}

/**
 * Mark all local notifications as read
 */
export function markAllLocalNotificationsAsRead() {
  try {
    const notifications = getLocalNotifications()
    const updated = notifications.map(n => ({ ...n, read: true }))
    localStorage.setItem(getStorageKey(), JSON.stringify(updated))
    
    window.dispatchEvent(new CustomEvent('localNotificationUpdated'))
  } catch (err) {
    console.error('Error marking all local notifications as read:', err)
  }
}

/**
 * Delete a local notification
 * @param {string} notificationId - The notification ID
 */
export function deleteLocalNotification(notificationId) {
  try {
    const notifications = getLocalNotifications()
    const filtered = notifications.filter(n => n.id !== notificationId)
    localStorage.setItem(getStorageKey(), JSON.stringify(filtered))
    
    window.dispatchEvent(new CustomEvent('localNotificationUpdated'))
  } catch (err) {
    console.error('Error deleting local notification:', err)
  }
}

/**
 * Clear all local notifications
 */
export function clearAllLocalNotifications() {
  try {
    localStorage.removeItem(getStorageKey())
    window.dispatchEvent(new CustomEvent('localNotificationUpdated'))
  } catch (err) {
    console.error('Error clearing local notifications:', err)
  }
}

/**
 * Get count of unread local notifications
 * @returns {number} Count of unread notifications
 */
export function getUnreadLocalCount() {
  const notifications = getLocalNotifications()
  return notifications.filter(n => !n.read).length
}
