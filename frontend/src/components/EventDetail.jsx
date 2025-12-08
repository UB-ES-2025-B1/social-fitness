import React, { useState, useEffect } from "react"
import EventChat from "./EventChat"
import { getEvent } from "../services/events"

const DEFAULT_AVATAR = '/img/user-profile-icon-profile.png'

export default function EventChatModal({ event, onClose }) {
  const [eventDetails, setEventDetails] = useState(null)
  const [loading, setLoading] = useState(true)

  // Fetch full event details including participants
  useEffect(() => {
    if (!event?.id) return

    let cancelled = false

    async function fetchEventDetails() {
      try {
        const res = await getEvent(event.id)
        if (!cancelled && res.ok && res.data) {
          setEventDetails(res.data)
        }
      } catch (err) {
        console.error('Error fetching event details:', err)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetchEventDetails()
    return () => { cancelled = true }
  }, [event?.id])

  // Listen for participant changes (join/leave events)
  useEffect(() => {
    function handleParticipantChange(e) {
      const detail = e?.detail
      if (!detail || String(detail.id) !== String(event?.id)) return

      // Refetch event details when someone joins or leaves
      getEvent(event.id).then(res => {
        if (res.ok && res.data) {
          setEventDetails(res.data)
        }
      }).catch(console.error)
    }

    window.addEventListener('joinedEventsChanged', handleParticipantChange)
    return () => window.removeEventListener('joinedEventsChanged', handleParticipantChange)
  }, [event?.id])

  if (!event) return null

  const participants = eventDetails?.participants || []
  const participantsCount = Array.isArray(participants) ? participants.length : (Number(event.participants) || 0)
  const capacity = Number(eventDetails?.capacity || event.capacity) || 0

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
              {loading ? (
                <p className="muted">Cargando participantes...</p>
              ) : participants.length === 0 ? (
                <p className="muted">No hay participantes todavía</p>
              ) : (
                <div className="participants-list">
                  {participants.map((participant) => (
                    <div key={participant.id} className="participant-item">
                      <img 
                        src={participant.profileImage || DEFAULT_AVATAR} 
                        alt={participant.name}
                        className="participant-avatar"
                        onError={(e) => { e.currentTarget.src = DEFAULT_AVATAR }}
                      />
                      <span className="participant-name">{participant.name}</span>
                    </div>
                  ))}
                </div>
              )}
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
