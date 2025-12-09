import React, { useState, useEffect } from 'react'
import * as notificationService from '../services/notifications'
import * as localNotificationService from '../services/localNotifications'
import './notifications.css'

export default function Notifications({ onNavigate = () => {} }) {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadNotifications()
    
    // Listen for local notification updates
    const handleLocalUpdate = () => {
      loadNotifications()
    }
    
    window.addEventListener('localNotificationAdded', handleLocalUpdate)
    window.addEventListener('localNotificationUpdated', handleLocalUpdate)
    
    return () => {
      window.removeEventListener('localNotificationAdded', handleLocalUpdate)
      window.removeEventListener('localNotificationUpdated', handleLocalUpdate)
    }
  }, [])

  async function loadNotifications() {
    setLoading(true)
    setError('')
    try {
      // Load backend notifications
      const res = await notificationService.getNotifications()
      const backendNotifications = (res.ok && res.data && Array.isArray(res.data)) ? res.data : []
      
      // Load local notifications
      const localNotifications = localNotificationService.getLocalNotifications()
      
      // Merge and sort by timestamp (newest first)
      const allNotifications = [...localNotifications, ...backendNotifications]
        .sort((a, b) => {
          const timeA = new Date(a.timestamp || a.createdAt).getTime()
          const timeB = new Date(b.timestamp || b.createdAt).getTime()
          return timeB - timeA // Newest first
        })
      
      setNotifications(allNotifications)
    } catch (err) {
      console.error('Error loading notifications:', err)
      // Still load local notifications even if backend fails
      const localNotifications = localNotificationService.getLocalNotifications()
      setNotifications(localNotifications)
      setError('No se pudieron cargar todas las notificaciones')
    } finally {
      setLoading(false)
    }
  }

  async function handleMarkAsRead(notificationId) {
    try {
      // Check if it's a local notification
      const notification = notifications.find(n => n.id === notificationId)
      
      if (notification?.isLocal) {
        // Handle local notification
        localNotificationService.markLocalNotificationAsRead(notificationId)
        setNotifications(prev =>
          prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
        )
      } else {
        // Handle backend notification
        const res = await notificationService.markAsRead(notificationId)
        if (res.ok) {
          setNotifications(prev =>
            prev.map(n => n.id === notificationId ? { ...n, read: true } : n)
          )
        }
      }
    } catch (err) {
      console.error('Error marking notification as read:', err)
    }
  }

  async function handleMarkAllAsRead() {
    try {
      // Mark all backend notifications as read
      const res = await notificationService.markAllAsRead()
      if (res.ok) {
        // Mark all local notifications as read
        localNotificationService.markAllLocalNotificationsAsRead()
        // Mark all as read in local state
        setNotifications(prev => prev.map(n => ({ ...n, read: true })))
      }
    } catch (err) {
      console.error('Error marking all as read:', err)
      // Still mark local notifications as read even if backend fails
      localNotificationService.markAllLocalNotificationsAsRead()
      setNotifications(prev => 
        prev.map(n => n.isLocal ? { ...n, read: true } : n)
      )
    }
  }

  async function handleDelete(notificationId) {
    try {
      // Check if it's a local notification
      const notification = notifications.find(n => n.id === notificationId)
      
      if (notification?.isLocal) {
        // Handle local notification
        localNotificationService.deleteLocalNotification(notificationId)
        setNotifications(prev => prev.filter(n => n.id !== notificationId))
      } else {
        // Handle backend notification
        const res = await notificationService.deleteNotification(notificationId)
        if (res.ok) {
          setNotifications(prev => prev.filter(n => n.id !== notificationId))
        }
      }
    } catch (err) {
      console.error('Error deleting notification:', err)
    }
  }

  function handleNotificationClick(notification) {
    // Mark as read when clicked
    if (!notification.read) {
      handleMarkAsRead(notification.id)
    }

    // Navigate based on notification type
    if (notification.eventId) {
      // Navigate to event detail
      onNavigate('explore', { eventId: notification.eventId })
    } else if (notification.relatedUserId && notification.type === 'NEW_MESSAGE') {
      // Navigate to message chat
      onNavigate('messages-chat', { userId: notification.relatedUserId })
    }
  }

  function getNotificationIcon(type) {
    switch (type) {
      case 'EVENT_STARTING':
        return '⏰'
      case 'NEW_MESSAGE':
        return '💬'
      case 'JOINED_EVENT':
        return '✅'
      case 'LEFT_EVENT':
        return '👋'
      case 'NEW_PARTICIPANT':
        return '👥'
      case 'EVENT_CANCELLED':
        return '❌'
      case 'EVENT_UPDATED':
        return '📝'
      default:
        return '🔔'
    }
  }

  function getRelativeTime(timestamp) {
    const now = new Date()
    const notifTime = new Date(timestamp)
    const diffMs = now - notifTime
    const diffMins = Math.floor(diffMs / (1000 * 60))
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

    if (diffMins < 1) return 'ahora mismo'
    if (diffMins < 60) return `hace ${diffMins} minuto${diffMins > 1 ? 's' : ''}`
    if (diffHours < 24) return `hace ${diffHours} hora${diffHours > 1 ? 's' : ''}`
    if (diffDays < 30) return `hace ${diffDays} día${diffDays > 1 ? 's' : ''}`
    return notifTime.toLocaleDateString('es-ES')
  }

  const hasUnread = notifications.some(n => !n.read)

  if (loading) {
    return (
      <div className="notifications-container">
        <div className="notifications-header">
          <h2>Notificaciones</h2>
        </div>
        <div className="notifications-loading">Cargando notificaciones...</div>
      </div>
    )
  }

  return (
    <div className="notifications-container">
      <div className="notifications-header">
        <h2>Notificaciones</h2>
        {hasUnread && (
          <button
            className="mark-all-read-btn"
            onClick={handleMarkAllAsRead}
          >
            Marcar todas como leídas
          </button>
        )}
      </div>

      {error && <div className="notifications-error">{error}</div>}

      {notifications.length === 0 ? (
        <div className="notifications-empty">
          <span className="empty-icon">🔔</span>
          <p>No tienes notificaciones</p>
        </div>
      ) : (
        <div className="notifications-list">
          {notifications.map(notification => (
            <div
              key={notification.id}
              className={`notification-item ${!notification.read ? 'unread' : ''}`}
              onClick={() => handleNotificationClick(notification)}
            >
              <div className="notification-icon">
                {getNotificationIcon(notification.type)}
              </div>
              <div className="notification-content">
                <div className="notification-message">
                  {notification.message}
                </div>
                <div className="notification-time">
                  {getRelativeTime(notification.timestamp || notification.createdAt)}
                </div>
              </div>
              {!notification.read && (
                <div className="notification-unread-indicator" />
              )}
              <button
                className="notification-delete-btn"
                onClick={(e) => {
                  e.stopPropagation()
                  handleDelete(notification.id)
                }}
                aria-label="Eliminar notificación"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
