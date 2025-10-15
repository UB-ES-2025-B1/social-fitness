import React, { useEffect, useState } from 'react'
import '../components/events.css'
import { listEvents, joinEvent } from '../services/events'

function EventCard({ ev, onJoin }) {
  return (
    <article className="event-card">
      <div className="event-image" style={{ backgroundImage: `url(${ev.image || '/public/placeholder.jpg'})` }} />
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
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function load(q = '') {
    setLoading(true)
    setError(null)
    try {
      const res = await listEvents({ q })
      if (res.ok) setEvents(res.data || [])
      else setError('Failed to load events')
    } catch (err) {
      setError('Network error')
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
        <button className="filter-toggle btn-ghost">Filtrar resultados</button>

        {loading && <div className="muted">Cargando...</div>}
        {error && <div className="general-error">{error}</div>}

        {events.map(ev => (
          <EventCard key={ev.id} ev={ev} onJoin={handleJoin} />
        ))}

        {!loading && events.length === 0 && <div className="muted">No hay eventos</div>}
      </main>
    </div>
  )
}
