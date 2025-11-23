import React from "react"
import EventChat from "./EventChat" // ya lo tienes creado

export default function EventChatModal({ event, onClose }) {
  if (!event) return null

  const participantsCount = Number(event.participants) || 0
  const capacity = Number(event.capacity) || 0

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal-card chat-modal" style={{ maxWidth: "960px", width: "100%" }}>
        {/* CABECERA DEL POPUP */}
        <div className="modal-header">
          <h3>Chat del evento</h3>
          <button className="btn-icon" aria-label="Cerrar" onClick={onClose}>
            ×
          </button>
        </div>

        {/* CUERPO: IZQ INFO / DER CHAT */}
        <div className="modal-body chat-modal-grid" style={{ display: "grid", gridTemplateColumns: "1.1fr 1.9fr", gap: 16 }}>
          {/* Columna izquierda: info + participantes */}
          <div className="chat-modal-left" style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <h4>{event.title}</h4>
            <p><strong>Deporte:</strong> {event.sport}</p>
            <p><strong>Fecha:</strong> {event.date}</p>
            <p><strong>Hora:</strong> {event.time}</p>
            <p><strong>Ubicación:</strong> {event.location}</p>
            <p><strong>Organizador:</strong> {event.organizer}</p>
            <p><strong>Participantes:</strong> {participantsCount}/{capacity}</p>

            <div style={{ marginTop: 16 }}>
              <h4>Participantes</h4>
              {/* Cuando tengáis backend de participantes lo rellenáis aquí */}
              <p className="muted">(Listado de participantes pendiente de backend)</p>
            </div>
          </div>

          {/* Columna derecha: CHAT */}
          <div className="chat-modal-right" style={{ borderLeft: "1px solid #eee", paddingLeft: 16, display: "flex", flexDirection: "column" }}>
            <EventChat eventId={event.id} eventTitle={event.title} />
          </div>
        </div>
      </div>
    </div>
  )
}
