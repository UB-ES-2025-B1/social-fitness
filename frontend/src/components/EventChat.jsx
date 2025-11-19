import React, { useEffect, useState, useRef } from 'react'
import '../components/events.css'
import { listEvents, joinEvent, leaveEvent } from '../services/events'
import EventFilterModal from './EventFilterModal'
import EventDetail from './EventDetail'

// SAMPLE DATA SI NO HAY API
const DEV_USE_SAMPLE = (import.meta.env.VITE_API_BASE || '') === ''
const SAMPLE_EVENTS = [
  {
    id: 'e1',
    title: 'Partido amistoso en el parque',
    sport: 'Fútbol',
    date: 'mié, 8 oct',
    time: '18:00',
    location: 'Parque Central',
    organizer: 'Carlos M.',
    participants: 8,
    capacity: 22,
    price: 5,
    image: ''
  },
  {
    id: 'e2',
    title: 'Torneo 3vs3',
    sport: 'Básquet',
    date: 'jue, 9 oct',
    time: '16:30',
    location: 'Pista Municipal',
    organizer: 'Ana S.',
    participants: 6,
    capacity: 12,
    price: 8,
    image: ''
  }
]

// IMÁGENES
const LOCAL_SPORT_IMAGES_PRIMARY = {
  futbol: '/img/sports/football.jpg',
  basquet: '/img/sports/basketball.jpg',
  tenis: '/img/sports/tennis.jpg',
  running: '/img/sports/running.jpg',
  natacion: '/img/sports/swimming.jpg',
  volleyball: '/img/sports/volleyball.jpg'
}

const LOCAL_SPORT_IMAGES_FALLBACK = {
  futbol: '/img/sports/football.svg',
  basquet: '/img/sports/basketball.svg',
  tenis: '/img/sports/tennis.svg',
  running: '/img/sports/running.svg',
  natacion: '/img/sports/swimming.svg',
  volleyball: '/img/sports/volleyball.svg'
}

const LOCAL_GENERIC_IMAGE = '/img/sports/football.svg'

function normalizeSport(name = '') {
  return name
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
}

function withDefaultSportImage(ev) {
  const provided = ev.image?.trim() || ''
  const key = normalizeSport(ev.sport)
  const primaryLocal = LOCAL_SPORT_IMAGES_PRIMARY[key]
  const fallbackLocal = LOCAL_SPORT_IMAGES_FALLBACK[key] || LOCAL_GENERIC_IMAGE
  const primary = provided || primaryLocal || fallbackLocal
  return { ...ev, imagePrimary: primary, imageFallback: fallbackLocal }
}

// CARD DE EVENTO
function EventCard({ ev, joined, onJoin, onLeave, onOpen }) {

  function handleCardClick(e) {
    if (e.target.tagName === 'BUTTON') return
    if (joined) onOpen(ev)   // Solo deja abrir SI estás unido
  }

  return (
    <article className="event-card" onClick={handleCardClick} style={{ cursor: 'pointer' }}>
      <div className="event-image">
        <img className="event-image-img" src={ev.imagePrimary} alt={`${ev.sport} event`}
          onError={(e) => { e.currentTarget.src = ev.imageFallback }} />
      </div>

      <div className="event-body">
        <div className="event-row">
          <span className="event-tag">{ev.sport}</span>
          <span className="event-price">{ev.price ? `${ev.price} €` : ''}</span>
        </div>

        <h3 className="event-title">{ev.title}</h3>

        <div className="event-meta">
          <div className="muted">{ev.date} • {ev.time}</div>
          <div className="muted">{ev.location}</div>
          <div className="muted">Organizado por {ev.organizer}</div>
          <div className="muted small">{ev.participants}/{ev.capacity} participantes</div>
        </div>
      </div>

      <div className="event-actions">
        {!joined ? (
          <button className="btn-primary" onClick={() => onJoin(ev.id)}>Unirse</button>
        ) : (
          <div className="joined-box">
            <div className="joined-text">Te has unido</div>
            <button className="btn-leave" onClick={() => onLeave(ev.id)}>Salir</button>
          </div>
        )}
      </div>
    </article>
  )
}

export default function EventExplorer() {

  // Estado para el detalle
  const [selectedEvent, setSelectedEvent] = useState(null)

  // TODOS LOS HOOKS AQUÍ (SIN returns antes)
  const [query, setQuery] = useState('')
  const [filters, setFilters] = useState({ sports: [], location: '', days: [], timeFrom: '', timeTo: '' })
  const [showFilters, setShowFilters] = useState(false)
  const [events, setEvents] = useState([])

  function joinedStorageKey() {
    const uid = localStorage.getItem('userId')
    return `joinedEvents:${uid || 'anon'}`
  }

  const [joinedSet, setJoinedSet] = useState(() => {
    try {
      return new Set(JSON.parse(localStorage.getItem(joinedStorageKey())) || [])
    } catch { return new Set() }
  })

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [joinedCollapsed, setJoinedCollapsed] = useState(true)

  function sortByJoined(list, set = joinedSet) {
    return list.slice().sort((a, b) =>
      (set.has(a.id) ? 0 : 1) - (set.has(b.id) ? 0 : 1)
    )
  }

  async function load(q = query, f = filters) {
    setLoading(true)
    setError(null)
    try {
      const res = await listEvents({ q, ...f })
      let items = res.ok
        ? res.data?.events || res.data?.items || res.data || []
        : []

      if (items.length === 0 && DEV_USE_SAMPLE) items = SAMPLE_EVENTS

      const mapped = items.map(withDefaultSportImage)
      setEvents(sortByJoined(mapped))
    } catch {
      if (DEV_USE_SAMPLE) setEvents(SAMPLE_EVENTS.map(withDefaultSportImage))
      else setError('Error de red')
    } finally {
      setLoading(false)
    }
  }

  // Cargar eventos
  useEffect(() => { load() }, [])

  // Eventos join/leave
  useEffect(() => {
    function handler(e) {
      const d = e.detail
      if (!d?.id) return
      const id = String(d.id)

      setJoinedSet(prev => {
        const next = new Set(prev)
        d.action === 'join' ? next.add(id) : next.delete(id)
        localStorage.setItem(joinedStorageKey(), JSON.stringify([...next]))

        setEvents(prevEvents =>
          sortByJoined(
            prevEvents.map(ev =>
              ev.id === id
                ? { ...ev, participants: d.action === 'join' ? ev.participants + 1 : Math.max(0, ev.participants - 1) }
                : ev
            ),
            next
          )
        )
        return next
      })
    }
    window.addEventListener('joinedEventsChanged', handler)
    return () => window.removeEventListener('joinedEventsChanged', handler)
  }, [])

  // AQUI SE PINTA EL DETALLE
  if (selectedEvent !== null) {
    return (
      <EventDetail
        event={selectedEvent}
        onBack={() => setSelectedEvent(null)}
      />
    )
  }

  // LISTADO DE EVENTOS 
  return (
    <div className="explorer-root">
      <header className="explorer-header">
        <h2>Explorar Eventos</h2>
        <div className="search-row">
          <input
            placeholder="Buscar eventos…"
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          <button className="btn-ghost" onClick={() => load(query)}>Buscar</button>
        </div>
      </header>

      <main className="explorer-list">
        <button className="filter-toggle btn-ghost" onClick={() => setShowFilters(true)}>
          Filtrar resultados
        </button>

        {loading && <div className="muted">Cargando…</div>}
        {error && <div className="general-error">{error}</div>}

        {(() => {
          const joinedEvents = events.filter(ev => joinedSet.has(ev.id))
          const otherEvents = events.filter(ev => !joinedSet.has(ev.id))

          return (
            <>
              {joinedEvents.length > 0 && (
                <section className="joined-group">
                  <div className="group-header">
                    <button
                      className="group-toggle"
                      onClick={() => setJoinedCollapsed(s => !s)}
                    >
                      {joinedCollapsed ? '►' : '▾'} Tus eventos ({joinedEvents.length})
                    </button>
                  </div>

                  {!joinedCollapsed && (
                    <div className="group-list">
                      {joinedEvents.map(ev => (
                        <EventCard
                          key={ev.id}
                          ev={ev}
                          joined={true}
                          onJoin={() => handleJoin(ev.id)}
                          onLeave={() => handleLeave(ev.id)}
                          onOpen={setSelectedEvent}
                        />
                      ))}
                    </div>
                  )}
                </section>
              )}

              <section className="other-group">
                <div className="group-header small"><strong>Otros eventos</strong></div>
                <div className="group-list">
                  {otherEvents.map(ev => (
                    <EventCard
                      key={ev.id}
                      ev={ev}
                      joined={joinedSet.has(ev.id)}
                      onJoin={() => handleJoin(ev.id)}
                      onLeave={() => handleLeave(ev.id)}
                      onOpen={setSelectedEvent}
                    />
                  ))}
                </div>
              </section>
            </>
          )
        })()}
      </main>

      <EventFilterModal
        open={showFilters}
        initialFilters={filters}
        onClose={() => setShowFilters(false)}
        onClear={() => {
          const empty = { sports: [], location: '', days: [], timeFrom: '', timeTo: '' }
          setFilters(empty)
          setShowFilters(false)
          load('', empty)
        }}
        onApply={(f) => {
          setFilters(f)
          setShowFilters(false)
          load(query, f)
        }}
      />
    </div>
  )
}

export function EventChat({ event, visible }) {
    // hooks siempre al inicio y en el mismo orden
    const [messages, setMessages] = useState([]);
    const [text, setText] = useState('');
    const socketRef = useRef(null);

    useEffect(() => {
      if (!event || !visible) return; // lógica dependiente dentro del effect (no el hook)
      // conectar socket / fetch mensajes
      // ejemplo: socketRef.current = new WebSocket(...)
      // fetch initial messages...
      return () => {
        // limpiar socket
        if (socketRef.current) socketRef.current.close();
      };
    }, [event?.id, visible]);

    // retorno condicional SOLO después de declarar hooks
    if (!event || !visible) return null;

    return (
      <div className="event-chat">
         {/* render messages, input */}
      </div>
    );
}
