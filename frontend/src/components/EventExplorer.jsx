import React, { useEffect, useState } from 'react'
import '../components/events.css'
import { listEvents, joinEvent } from '../services/events'
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

// Local images per sport. Primary points to JPG you can drop in /public/img/sports/,
// and fallback points to SVGs already included in the repo.
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
    .replace(/[\u0300-\u036f]/g, '') // remove accents (broadly supported)
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

function EventCard({ ev, onJoin }) {
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
        <button className="btn-primary" onClick={() => onJoin(ev.id)}>Unirse</button>
      </div>
    </article>
  )
}

export default function EventExplorer() {
  const [query, setQuery] = useState('')
  const [filters, setFilters] = useState({ sports: [], location: '', days: [], timeFrom: '', timeTo: '' })
  const [showFilters, setShowFilters] = useState(false)
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

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
        // Ensure an image is always present, pick by sport if missing
        setEvents(items.map(withDefaultSportImage))
      } else {
        if (DEV_USE_SAMPLE) setEvents(SAMPLE_EVENTS.map(withDefaultSportImage))
        else setError('Failed to load events')
      }
    } catch (err) {
      if (DEV_USE_SAMPLE) setEvents(SAMPLE_EVENTS.map(withDefaultSportImage))
      else setError('Network error')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function handleJoin(id) {
    const res = await joinEvent(id)
    if (res.ok) {
      // optimistic: reload list
      load(query)
    } else {
      alert('No s\u2019ha pogut unir a lâ€™esdeveniment')
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

        {events.map(ev => (
          <EventCard key={ev.id} ev={ev} onJoin={handleJoin} />
        ))}

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
