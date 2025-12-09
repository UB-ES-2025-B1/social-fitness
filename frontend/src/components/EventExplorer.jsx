import React, { useEffect, useState, useRef } from "react"
import "../components/events.css"
import "./chat.css"          
import { listEvents, joinEvent, leaveEvent } from "../services/events"
import EventFilterModal from "./EventFilterModal"
import { addLocalNotification } from "../services/localNotifications"

// servicios de chat + componentes de mensaje
import { getChatMessages, sendChatMessage, checkParticipant } from "../services/chat"
import ChatMessage from "./ChatMessage"
import ChatInput from "./ChatInput"
import { getEvent } from "../services/events"

const DEFAULT_AVATAR = '/img/user-profile-icon-profile.png'
const DEV_USE_SAMPLE = (import.meta.env.VITE_API_BASE || "") === ""

const SAMPLE_EVENTS = [
  {
    id: "e1",
    title: "Partido amistoso en el parque",
    sport: "Fútbol",
    date: "mié, 8 oct",
    time: "18:00",
    location: "Parque Central",
    organizer: "Carlos M.",
    participants: 8,
    capacity: 22,
    price: 5,
    image: "",
  },
  {
    id: "e2",
    title: "Torneo 3vs3",
    sport: "Básquet",
    date: "jue, 9 oct",
    time: "16:30",
    location: "Pista Municipal",
    organizer: "Ana S.",
    participants: 6,
    capacity: 12,
    price: 8,
    image: "",
  },
]

// -------- helpers imágenes --------
const LOCAL_SPORT_IMAGES_PRIMARY = {
  futbol: "/img/sports/football.jpg",
  basquet: "/img/sports/basketball.jpg",
  tenis: "/img/sports/tennis.jpg",
  running: "/img/sports/running.jpg",
  natacion: "/img/sports/swimming.jpg",
  volleyball: "/img/sports/volleyball.jpg",
}

const LOCAL_SPORT_IMAGES_FALLBACK = {
  futbol: "/img/sports/football.svg",
  basquet: "/img/sports/basketball.svg",
  tenis: "/img/sports/tennis.svg",
  running: "/img/sports/running.svg",
  natacion: "/img/sports/swimming.svg",
  volleyball: "/img/sports/volleyball.svg",
}

const LOCAL_GENERIC_IMAGE = "/img/sports/football.svg"

function normalizeSport(name = "") {
  return name
    .toString()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\s+/g, " ")
    .trim()
}

function withDefaultSportImage(ev) {
  const provided =
    ev?.image && String(ev.image).trim().length > 0 ? String(ev.image).trim() : ""
  const key = normalizeSport(ev?.sport)
  const primaryLocal = LOCAL_SPORT_IMAGES_PRIMARY[key]
  const fallbackLocal = LOCAL_SPORT_IMAGES_FALLBACK[key] || LOCAL_GENERIC_IMAGE
  const primary = provided || primaryLocal || fallbackLocal
  return { ...ev, imagePrimary: primary, imageFallback: fallbackLocal }
}

// -------- POP-UP DEL CHAT --------
function EventChatModal({ event, onClose }) {
  const [messages, setMessages] = useState([])
  const [hasAccess, setHasAccess] = useState(false)
  const [loadingChat, setLoadingChat] = useState(true)
  const [eventDetails, setEventDetails] = useState(null)
  const scrollRef = useRef(null)

  const userId = localStorage.getItem("userId")
  const username = localStorage.getItem("username") || "Usuario"

  function storageKey(eventId) {
    return `eventChat:${eventId}`
  }

  function loadFromStorage(eventId) {
    try {
      const raw = localStorage.getItem(storageKey(eventId))
      if (!raw) return []
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }

  function saveToStorage(eventId, msgs) {
    try {
      localStorage.setItem(storageKey(eventId), JSON.stringify(msgs))
    } catch {}
  }

  // cargar mensajes guardados localmente al abrir
  useEffect(() => {
    const cached = loadFromStorage(event.id)
    if (cached.length > 0) setMessages(cached)
  }, [event.id])

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
      }
    }

    fetchEventDetails()
    return () => { cancelled = true }
  }, [event.id])

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

  // comprobar acceso
  useEffect(() => {
    let cancelled = false

    async function loadAccess() {
      try {
        const res = await checkParticipant(event.id, userId)
        if (!cancelled) {
          const allowed = res?.data?.isParticipant
          setHasAccess(allowed === undefined ? true : !!allowed)
          setLoadingChat(false)
        }
      } catch {
        if (!cancelled) {
          setHasAccess(true)
          setLoadingChat(false)
        }
      }
    }

    loadAccess()
    return () => { cancelled = true }
  }, [event.id, userId])

  // cargar mensajes desde backend + polling
  useEffect(() => {
    if (!hasAccess) return
    let cancelled = false

    async function loadMessages() {
      try {
        const res = await getChatMessages(event.id)
        if (!cancelled && res.ok && Array.isArray(res.data)) {
          setMessages(res.data)
          saveToStorage(event.id, res.data)
        }
      } catch {
        // nos quedamos con lo de localStorage si falla
      }
    }

    loadMessages()
    const interval = setInterval(loadMessages, 2000)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [event.id, hasAccess])

  // autoscroll
  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight
    }
  }, [messages])

  async function handleSend(text) {
    const trimmed = text.trim()
    if (!trimmed) return

    try {
      await sendChatMessage(event.id, trimmed)
    } catch {}

    setMessages(prev => {
      const next = [
        ...prev,
        {
          id: Date.now(),
          eventId: event.id,
          userId,
          username,
          text: trimmed,
          timestamp: new Date().toISOString(),
        },
      ]
      saveToStorage(event.id, next)
      return next
    })
  }

  const participants = eventDetails?.participants || []
  const participantsCount = Array.isArray(participants) ? participants.length : (Number(event.participants) || 0)
  const capacity = Number(eventDetails?.capacity || event.capacity) || 0

  return (
    <div className="chat-backdrop">
      <div className="chat-modal">
        {/* CABECERA */}
        <div className="chat-header">
          <div>
            <div className="chat-header-title">Chat del evento</div>
            <div className="chat-header-subtitle">{event.title}</div>
          </div>
          <button
            className="chat-close-btn"
            onClick={onClose}
            aria-label="Cerrar"
          >
            ×
          </button>
        </div>

        {/* CUERPO */}
        <div className="chat-layout">
          {/* IZQUIERDA: Participantes */}
          <aside className="chat-sidebar">
            <strong className="chat-sidebar-title">Participantes</strong>
            <span className="chat-sidebar-count">
              {participantsCount}/{capacity} participantes
            </span>
            
            {/* Lista de participantes */}
            <div className="participants-list" style={{ marginTop: 16, maxHeight: '400px', overflowY: 'auto' }}>
              {participants.length === 0 ? (
                <p className="muted" style={{ fontSize: '0.9rem' }}>Cargando participantes...</p>
              ) : (
                participants.map((participant) => (
                  <div key={participant.id} className="participant-item" style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                    <img 
                      src={participant.profileImage || DEFAULT_AVATAR} 
                      alt={participant.name}
                      className="participant-avatar"
                      style={{ width: 32, height: 32, borderRadius: '50%', objectFit: 'cover' }}
                      onError={(e) => { e.currentTarget.src = DEFAULT_AVATAR }}
                    />
                    <span className="participant-name" style={{ fontSize: '0.9rem' }}>{participant.name}</span>
                  </div>
                ))
              )}
            </div>
          </aside>

          {/* DERECHA: Chat */}
          <section className="chat-main">
            {loadingChat ? (
              <div className="chat-loading">Cargando chat...</div>
            ) : !hasAccess ? (
              <div className="chat-no-access">
                <h4>No tienes acceso al chat</h4>
                <p>Debes unirte al evento para ver y enviar mensajes.</p>
              </div>
            ) : (
              <div className="chat-main-inner">
                <div className="chat-messages" ref={scrollRef}>
                  {messages.length === 0 ? (
                    <div className="chat-empty">
                      No hay mensajes todavía. ¡Escribe el primero!
                    </div>
                  ) : (
                    messages.map((msg) => (
                      <ChatMessage
                        key={msg.id}
                        message={msg}
                        own={String(msg.userId) === String(userId)}
                      />
                    ))
                  )}
                </div>

                <ChatInput onSend={handleSend} />
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  )
}



// -------- tarjeta de evento --------
function EventCard({ ev, joined, onJoin, onLeave, onOpenChat }) {
  function handleCardClick(e) {
    if (e.target.tagName === "BUTTON") return
    if (joined && typeof onOpenChat === "function") {
      onOpenChat(ev)
    }
  }

  return (
    <article
      className="event-card"
      onClick={handleCardClick}
      style={{ cursor: joined ? "pointer" : "default" }}
    >
      <div className="event-image">
        <img
          className="event-image-img"
          src={ev.imagePrimary}
          alt={`${ev.sport} event`}
          onError={(e) => {
            e.currentTarget.src = ev.imageFallback
          }}
        />
      </div>
      <div className="event-body">
        <div className="event-row">
          <span className="event-tag">{ev.sport}</span>
          <span className="event-price">{ev.price ? `${ev.price} €` : ""}</span>
        </div>
        <h3 className="event-title">{ev.title}</h3>
        <div className="event-meta">
          <div className="muted">
            {ev.date} • {ev.time}
          </div>
          <div className="muted">{ev.location}</div>
          <div className="muted">Organizado por {ev.organizer}</div>
          <div className="muted small">
            {ev.participants}/{ev.capacity} participantes
          </div>
        </div>
      </div>
      <div className="event-actions">
        {!joined ? (
          <button className="btn-primary" onClick={() => onJoin(ev.id)}>
            Unirse
          </button>
        ) : (
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 8,
            }}
          >
            <div className="joined-text">Te has unido</div>
            <button className="btn-leave" onClick={() => onLeave(ev.id)}>
              Salir de evento
            </button>
          </div>
        )}
      </div>
    </article>
  )
}

// -------- componente principal --------
export default function EventExplorer() {
  const [query, setQuery] = useState("")
  const [filters, setFilters] = useState({
    sports: [],
    location: "",
    days: [],
    timeFrom: "",
    timeTo: "",
  })
  const [showFilters, setShowFilters] = useState(false)
  const [events, setEvents] = useState([])
  const [chatEvent, setChatEvent] = useState(null)

  function joinedStorageKey() {
    const uid =
      typeof window !== "undefined" ? localStorage.getItem("userId") : null
    return `joinedEvents:${uid || "anon"}`
  }

  const [joinedSet, setJoinedSet] = useState(() => {
    try {
      const raw = localStorage.getItem(joinedStorageKey())
      return new Set(raw ? JSON.parse(raw) : [])
    } catch {
      return new Set()
    }
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [joinedCollapsed, setJoinedCollapsed] = useState(true)

  function sortByJoined(list, set = joinedSet) {
    try {
      return list.slice().sort((a, b) => {
        const A = set && set.has(a.id) ? 0 : 1
        const B = set && set.has(b.id) ? 0 : 1
        return A - B
      })
    } catch {
      return list
    }
  }

  async function load(q = query, f = filters) {
    setLoading(true)
    setError(null)
    try {
      const res = await listEvents({ q, ...f })
      if (res.ok) {
        let items
        const d = res.data
        if (Array.isArray(d)) items = d
        else if (Array.isArray(d?.events)) items = d.events
        else if (Array.isArray(d?.items)) items = d.items
        else items = []

        if (items.length === 0 && DEV_USE_SAMPLE) {
          items = SAMPLE_EVENTS
        }

        const mapped = items.map(withDefaultSportImage)
        setEvents(sortByJoined(mapped))
      } else {
        if (DEV_USE_SAMPLE)
          setEvents(sortByJoined(SAMPLE_EVENTS.map(withDefaultSportImage)))
        else setError("Error al cargar eventos")
      }
    } catch {
      if (DEV_USE_SAMPLE) setEvents(SAMPLE_EVENTS.map(withDefaultSportImage))
      else setError("Error de red")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  // escucha join/leave desde otras partes
  useEffect(() => {
    function handler(e) {
      try {
        const d = e && e.detail ? e.detail : null
        if (!d || !d.id) return
        const id = String(d.id)
        const action = d.action || "join"
        setJoinedSet((prev) => {
          const next = new Set(prev)
          if (action === "join") next.add(id)
          else next.delete(id)
          try {
            localStorage.setItem(
              joinedStorageKey(),
              JSON.stringify(Array.from(next))
            )
          } catch {}
          setEvents((prevEvents) =>
            sortByJoined(
              prevEvents.map((ev) =>
                ev.id === id
                  ? {
                      ...ev,
                      participants:
                        action === "join"
                          ? (Number(ev.participants) || 0) + 1
                          : Math.max(
                              0,
                              (Number(ev.participants) || 1) - 1
                            ),
                    }
                  : ev
              ),
              next
            )
          )
          return next
        })
      } catch {
        // ignore
      }
    }
    window.addEventListener("joinedEventsChanged", handler)
    return () => window.removeEventListener("joinedEventsChanged", handler)
  }, [])

  async function handleJoin(id) {
    setError(null)
    try {
      const res = await joinEvent(id)
      if (res.ok) {
        const next = new Set(joinedSet)
        next.add(id)
        try {
          localStorage.setItem(
            joinedStorageKey(),
            JSON.stringify(Array.from(next))
          )
        } catch {}
        setJoinedSet(next)
        setEvents((prev) =>
          sortByJoined(
            prev.map((ev) =>
              ev.id === id
                ? {
                    ...ev,
                    participants: (Number(ev.participants) || 0) + 1,
                  }
                : ev
            ),
            next
          )
        )
        
        // Add local notification
        const event = events.find(e => e.id === id)
        if (event) {
          addLocalNotification({
            type: 'JOINED_EVENT',
            message: `Te has unido al evento "${event.title}"`,
            eventId: id,
            eventTitle: event.title
          })
        }
      } else {
        setError("No se pudo unir al evento")
      }
    } catch {
      setError("Network error")
    }
  }

  async function handleLeave(id) {
    setError(null)
    try {
      const res = await leaveEvent(id)
      if (res.ok) {
        const next = new Set(joinedSet)
        next.delete(id)
        try {
          localStorage.setItem(
            joinedStorageKey(),
            JSON.stringify(Array.from(next))
          )
        } catch {}
        setJoinedSet(next)
        setEvents((prev) =>
          sortByJoined(
            prev.map((ev) =>
              ev.id === id
                ? {
                    ...ev,
                    participants: Math.max(
                      0,
                      (Number(ev.participants) || 1) - 1
                    ),
                  }
                : ev
            ),
            next
          )
        )
        setChatEvent((prev) =>
          prev && String(prev.id) === String(id) ? null : prev
        )
        
        // Add local notification
        const event = events.find(e => e.id === id)
        if (event) {
          addLocalNotification({
            type: 'LEFT_EVENT',
            message: `Has salido del evento "${event.title}"`,
            eventId: id,
            eventTitle: event.title
          })
        }
      } else {
        setError("No se pudo salir del evento")
      }
    } catch {
      setError("Network error")
    }
  }

  return (
    <div className="explorer-root">
      <header className="explorer-header">
        <h2>Explorar Eventos</h2>
        <div className="search-row">
          <input
            placeholder="Buscar eventos, deportes o ubicaciones..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
          <button className="btn-ghost" onClick={() => load(query)}>
            Buscar
          </button>
        </div>
      </header>

      <main className="explorer-list">
        <button
          className="filter-toggle btn-ghost"
          onClick={() => setShowFilters(true)}
        >
          Filtrar resultados
        </button>

        {loading && <div className="muted">Cargando...</div>}
        {error && <div className="general-error">{error}</div>}

        {(() => {
          const joinedEvents = events.filter((ev) => joinedSet.has(ev.id))
          const otherEvents = events.filter((ev) => !joinedSet.has(ev.id))

          return (
            <>
              {joinedEvents.length > 0 && (
                <section className="joined-group">
                  <div className="group-header">
                    <button
                      className="group-toggle"
                      onClick={() => setJoinedCollapsed((s) => !s)}
                      aria-expanded={!joinedCollapsed}
                    >
                      {joinedCollapsed ? "►" : "▾"} Tus eventos (
                      {joinedEvents.length})
                    </button>
                  </div>
                  {!joinedCollapsed && (
                    <div className="group-list">
                      {joinedEvents.map((ev) => (
                        <EventCard
                          key={ev.id}
                          ev={ev}
                          joined={true}
                          onJoin={handleJoin}
                          onLeave={handleLeave}
                          onOpenChat={setChatEvent}
                        />
                      ))}
                    </div>
                  )}
                </section>
              )}

              {otherEvents.length > 0 && (
                <section className="other-group">
                  <div className="group-header small">
                    <strong>Otros eventos</strong>
                  </div>
                  <div className="group-list">
                    {otherEvents.map((ev) => (
                      <EventCard
                        key={ev.id}
                        ev={ev}
                        joined={joinedSet.has(ev.id)}
                        onJoin={handleJoin}
                        onLeave={handleLeave}
                        onOpenChat={setChatEvent}
                      />
                    ))}
                  </div>
                </section>
              )}

              {joinedEvents.length === 0 && otherEvents.length === 0 && (
                <div className="muted">No hay eventos</div>
              )}
            </>
          )
        })()}

        {!loading && events.length === 0 && (
          <div className="muted">No hay eventos</div>
        )}
      </main>

      <EventFilterModal
        open={showFilters}
        initialFilters={filters}
        onClose={() => setShowFilters(false)}
        onClear={() => {
          const empty = {
            sports: [],
            location: "",
            days: [],
            timeFrom: "",
            timeTo: "",
          }
          setFilters(empty)
          setShowFilters(false)
          load("", empty)
        }}
        onApply={(f) => {
          setFilters(f)
          setShowFilters(false)
          load(query, f)
        }}
      />

      {/* POP-UP DEL CHAT */}
      {chatEvent && (
        <EventChatModal event={chatEvent} onClose={() => setChatEvent(null)} />
      )}
    </div>
  )
}
