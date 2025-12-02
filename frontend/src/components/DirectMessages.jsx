import React, { useEffect, useState } from 'react'
import './directMessages.css'
import { getChats } from '../services/directMessages'

const DEFAULT_AVATAR = '/img/user-profile-icon-profile.png'

export default function DirectMessages({ onSelectChat, onNewChat }) {
  const [chats, setChats] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    loadChats()
  }, [])

  async function loadChats() {
    setLoading(true)
    setError(null)
    try {
      const res = await getChats()
      if (res.ok && res.data) {
        setChats(res.data)
      } else {
        setError('Error al cargar los chats')
      }
    } catch (err) {
      setError('Error al cargar los chats')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  function formatTimestamp(timestamp) {
    if (!timestamp) return ''
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now - date
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (hours < 1) return 'Ahora'
    if (hours < 24) return `${hours}h`
    if (days < 7) return `${days}d`
    return date.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' })
  }

  function truncateMessage(text, maxLength = 50) {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }

  return (
    <div className="dm-container">
      <div className="dm-header">
        <h2>Mensajes</h2>
        <button 
          className="btn-new-chat" 
          onClick={onNewChat}
          title="Nuevo mensaje"
        >
          +
        </button>
      </div>

      {loading && <div className="dm-loading">Cargando chats...</div>}
      {error && <div className="dm-error">{error}</div>}

      {!loading && !error && chats.length === 0 && (
        <div className="dm-empty">
          <p>No tienes conversaciones aún</p>
          <button className="btn-primary" onClick={onNewChat}>
            Iniciar conversación
          </button>
        </div>
      )}

      {!loading && !error && chats.length > 0 && (
        <div className="dm-list">
          {chats.map((chat) => (
            <div
              key={chat.id}
              className="dm-chat-item"
              onClick={() => onSelectChat(chat.otherUser)}
            >
              <img
                src={chat.otherUser.profileImage || DEFAULT_AVATAR}
                alt={chat.otherUser.username}
                className="dm-avatar"
              />
              <div className="dm-chat-content">
                <div className="dm-chat-top">
                  <span className="dm-username">{chat.otherUser.username}</span>
                  <span className="dm-timestamp">
                    {formatTimestamp(chat.lastMessage?.timestamp)}
                  </span>
                </div>
                <div className="dm-chat-bottom">
                  <span className="dm-last-message">
                    {truncateMessage(chat.lastMessage?.text)}
                  </span>
                  {chat.unreadCount > 0 && (
                    <span className="dm-unread-badge">{chat.unreadCount}</span>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
