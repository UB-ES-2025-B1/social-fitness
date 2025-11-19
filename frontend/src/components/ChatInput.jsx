import React, { useState } from "react"

export default function ChatInput({ onSend }) {
  const [text, setText] = useState("")

  function handleSubmit(e) {
    e.preventDefault()
    if (text.trim()) onSend(text)
    setText("")
  }

  return (
    <form className="chat-input" onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Escribe un mensaje..."
        value={text}
        onChange={(e) => setText(e.target.value)}
        className="chat-input-field"
      />
      <button type="submit" className="chat-input-send">
        Enviar
      </button>
    </form>
  )
}
