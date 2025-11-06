import React, { useEffect, useState } from 'react'
import '../components/events.css'
import { listEvents, joinEvent, leaveEvent } from '../services/events'
import EventFilterModal from './EventFilterModal'

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

// Local sport images. Primary uses JPGs in /public/img/sports; fallback uses bundled SVGs.
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
    .toString()
    .toLowerCase()
    .normalize('NFD')
  .replace(/[\u0300-\u036f]/g, '') // remove accents
  .replace(/\s+/g, ' ') // collapse spaces
    .trim()
}

function withDefaultSportImage(ev) {
  const provided = ev?.image && String(ev.image).trim().length > 0 ? String(ev.image).trim() : ''
  const key = normalizeSport(ev?.sport)
  const primaryLocal = LOCAL_SPORT_IMAGES_PRIMARY[key]
  const fallbackLocal = LOCAL_SPORT_IMAGES_FALLBACK[key] || LOCAL_GENERIC_IMAGE
  const primary = provided || primaryLocal || fallbackLocal
  return { ...ev, imagePrimary: primary, imageFallback: fallbackLocal }
}

function EventCard({ ev, joined, onJoin, onLeave }) {
  return (
    <article className="event-card">
      <div className="event-image">
        <img className="event-image-img" src={ev.imagePrimary} alt={`${ev.sport} event`} onError={(e) => { e.currentTarget.src = ev.imageFallback }} />
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
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8 }}>
            <div className="joined-text">Te has unido</div>
            <button className="btn-leave" onClick={() => onLeave(ev.id)}>Salir de evento</button>
          </div>
        )}
      </div>
    </article>
  )
}

export default function EventExplorer() {
  const [query, setQuery] = useState('')
  const [filters, setFilters] = useState({ sports: [], location: '', days: [], timeFrom: '', timeTo: '' })
  const [showFilters, setShowFilters] = useState(false)
  const [events, setEvents] = useState([])
  const [joinedSet, setJoinedSet] = useState(() => {
    try {
      const raw = localStorage.getItem('joinedEvents')
      return new Set(raw ? JSON.parse(raw) : [])
    } catch (e) { return new Set() }
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
    } catch (e) {
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
  // Ensure each card has an image; fall back by sport if missing
  const mapped = items.map(withDefaultSportImage)
  setEvents(sortByJoined(mapped))
        } else {
  if (DEV_USE_SAMPLE) setEvents(sortByJoined(SAMPLE_EVENTS.map(withDefaultSportImage)))
        else setError('Error al cargar eventos')
      }
    } catch (err) {
  if (DEV_USE_SAMPLE) setEvents(SAMPLE_EVENTS.map(withDefaultSportImage))
  else setError('Error de red')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function handleJoin(id) {
    setError(null)
    try {
      const res = await joinEvent(id)
      if (res.ok) {
        // optimistic update: mark joined and increase participants locally
        const next = new Set(joinedSet)
        next.add(id)
        try { localStorage.setItem('joinedEvents', JSON.stringify(Array.from(next))) } catch (e) {}
        setJoinedSet(next)
        setEvents(prev => sortByJoined(prev.map(ev => ev.id === id ? { ...ev, participants: (Number(ev.participants) || 0) + 1 } : ev), next))
      } else {
        setError('No se pudo unir al evento')
      }
    } catch (err) {
      setError('Network error')
    }
  }

  async function handleLeave(id) {
    setError(null)
    try {
      const res = await leaveEvent(id)
      if (res.ok) {
        const next = new Set(joinedSet)
        next.delete(id)
        try { localStorage.setItem('joinedEvents', JSON.stringify(Array.from(next))) } catch (e) {}
        setJoinedSet(next)
        setEvents(prev => sortByJoined(prev.map(ev => ev.id === id ? { ...ev, participants: Math.max(0, (Number(ev.participants) || 1) - 1) } : ev), next))
      } else {
        setError('No se pudo salir del evento')
      }
    } catch (err) {
      setError('Network error')
    }
  }

  return (
    <div className="explorer-root">
      <header className="explorer-header">
        <h2>Explorar Eventos</h2>
        <div className="search-row">
          <input placeholder="Buscar eventos, deportes o ubicaciones..." value={query} onChange={e => setQuery(e.target.value)} />
          <button className="btn-ghost" onClick={() => load(query)}>Buscar</button>
        </div>
      </header>

      <main className="explorer-list">
        <button className="filter-toggle btn-ghost" onClick={() => setShowFilters(true)}>Filtrar resultados</button>

        {loading && <div className="muted">Cargando...</div>}
        {error && <div className="general-error">{error}</div>}

        {(() => {
          const joinedEvents = events.filter(ev => joinedSet.has(ev.id))
          const otherEvents = events.filter(ev => !joinedSet.has(ev.id))
          return (
            <>
              {joinedEvents.length > 0 && (
                <section className="joined-group">
                  <div className="group-header">
                    <button className="group-toggle" onClick={() => setJoinedCollapsed(s => !s)} aria-expanded={!joinedCollapsed}>
                      {joinedCollapsed ? '►' : '▾'} Tus eventos ({joinedEvents.length})
                    </button>
                  </div>
                  {!joinedCollapsed && (
                    <div className="group-list">
                      {joinedEvents.map(ev => (
                        <EventCard key={ev.id} ev={ev} joined={true} onJoin={handleJoin} onLeave={handleLeave} />
                      ))}
                    </div>
                  )}
                </section>
              )}

              {otherEvents.length > 0 && (
                <section className="other-group">
                  <div className="group-header small"><strong>Otros eventos</strong></div>
                  <div className="group-list">
                    {otherEvents.map(ev => (
                      <EventCard key={ev.id} ev={ev} joined={joinedSet.has(ev.id)} onJoin={handleJoin} onLeave={handleLeave} />
                    ))}
                  </div>
                </section>
              )}

              {joinedEvents.length === 0 && otherEvents.length === 0 && <div className="muted">No hay eventos</div>}
            </>
          )
        })()}

        {!loading && events.length === 0 && <div className="muted">No hay eventos</div>}
      </main>

      <EventFilterModal
        open={showFilters}
        initialFilters={filters}
        onClose={() => setShowFilters(false)}
        onClear={() => { setFilters({ sports: [], location: '', days: [], timeFrom: '', timeTo: '' }); setShowFilters(false); load('',{ sports: [], location: '', days: [], timeFrom: '', timeTo: '' }) }}
        onApply={(f) => { setFilters(f); setShowFilters(false); load(query, f) }}
      />
    </div>
  )
}
