import React from "react"

export default function ChatMessage({ message, own }) {
  const time = new Date(message.timestamp).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  })

  return (
    <div className={`chat-message ${own ? "own" : ""}`}>
      <div className="chat-message-info">
        <strong className="chat-message-username">{message.username}</strong>
        <span className="chat-message-time">{time}</span>
      </div>
      <div className="chat-message-text">{message.text}</div>
    </div>
  )
}
