import React, { useEffect, useState, useRef } from 'react'
import './privateChat.css'
import { getDirectMessages, sendDirectMessage } from '../services/directMessages'
import ChatMessage from './ChatMessage'
import ChatInput from './ChatInput'

const DEFAULT_AVATAR = '/img/user-profile-icon-profile.png'

export default function PrivateChat({ user, onBack }) {
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [sending, setSending] = useState(false)
  const messagesEndRef = useRef(null)
  const currentUserId = localStorage.getItem('userId')

  useEffect(() => {
    if (user?.id) {
      loadMessages()
    }
  }, [user?.id])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  async function loadMessages() {
    setLoading(true)
    setError(null)
    try {
      const res = await getDirectMessages(user.id)
      if (res.ok && res.data) {
        setMessages(res.data)
      } else {
        setError('Error al cargar los mensajes')
      }
    } catch (err) {
      setError('Error al cargar los mensajes')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  async function handleSendMessage(text) {
    if (!text.trim() || sending) return

    setSending(true)
    try {
      const res = await sendDirectMessage(user.id, text.trim())
      if (res.ok && res.data) {
        setMessages((prev) => [...prev, res.data])
      } else {
        alert('Error al enviar el mensaje')
      }
    } catch (err) {
      alert('Error al enviar el mensaje')
      console.error(err)
    } finally {
      setSending(false)
    }
  }

  function scrollToBottom() {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  if (!user) {
    return (
      <div className="private-chat-container">
        <div className="private-chat-error">Usuario no seleccionado</div>
      </div>
    )
  }

  return (
    <div className="private-chat-container">
      <div className="private-chat-header">
        <button className="btn-back" onClick={onBack}>
          ←
        </button>
        <img
          src={user.profileImage || DEFAULT_AVATAR}
          alt={user.username}
          className="private-chat-avatar"
        />
        <h2 className="private-chat-username">{user.username}</h2>
      </div>

      <div className="private-chat-main">
        {loading && (
          <div className="private-chat-loading">Cargando mensajes...</div>
        )}

        {error && <div className="private-chat-error">{error}</div>}

        {!loading && !error && messages.length === 0 && (
          <div className="private-chat-empty">
            <p>No hay mensajes aún</p>
            <p className="muted">Escribe un mensaje para iniciar la conversación</p>
          </div>
        )}

        {!loading && !error && messages.length > 0 && (
          <div className="private-chat-messages">
            {messages.map((msg) => (
              <ChatMessage
                key={msg.id}
                message={{
                  id: msg.id,
                  username: msg.senderUsername,
                  text: msg.text,
                  timestamp: msg.timestamp
                }}
                own={msg.senderId === currentUserId}
              />
            ))}
            <div ref={messagesEndRef} />
          </div>
        )}
      </div>

      <div className="private-chat-input-container">
        <ChatInput onSend={handleSendMessage} disabled={sending} />
      </div>
    </div>
  )
}
