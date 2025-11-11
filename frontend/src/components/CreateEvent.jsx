import React, { useState, useEffect } from 'react'
import '../components/profile.css'
import { createEvent, joinEvent } from '../services/events'
import { getProfile } from '../services/profile'

const SPORTS = [
  { id: 'football', label: 'Fútbol' },
  { id: 'basketball', label: 'Básquet' },
  { id: 'tennis', label: 'Tenis' },
  { id: 'running', label: 'Running' },
  { id: 'swimming', label: 'Natación' },
  { id: 'volleyball', label: 'Volleyball' },
]

export default function CreateEvent({ onCreated = () => {} }) {
  const [title, setTitle] = useState('')
  const [sport, setSport] = useState(SPORTS[0].id)
  const [date, setDate] = useState('')
  const [time, setTime] = useState('18:00')
  const [location, setLocation] = useState('')
  const [organizer, setOrganizer] = useState(localStorage.getItem('username') || '')
  const [organizerReadOnly, setOrganizerReadOnly] = useState(false)
  const [capacity, setCapacity] = useState(10)
  const [price, setPrice] = useState(0)
  // Image will be auto-assigned based on sport using local static assets
  const IMAGE_PRIMARY = {
    football: '/img/sports/football.jpg',
    basketball: '/img/sports/basketball.jpg',
    tennis: '/img/sports/tennis.jpg',
    running: '/img/sports/running.jpg',
    swimming: '/img/sports/swimming.jpg',
    volleyball: '/img/sports/volleyball.jpg',
  }
  const IMAGE_FALLBACK = {
    football: '/img/sports/football.svg',
    basketball: '/img/sports/basketball.svg',
    tennis: '/img/sports/tennis.svg',
    running: '/img/sports/running.svg',
    swimming: '/img/sports/swimming.svg',
    volleyball: '/img/sports/volleyball.svg',
  }
  function sportToImage(id) { return IMAGE_PRIMARY[id] || IMAGE_FALLBACK[id] || '' }
  const [description, setDescription] = useState('')
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function validate() {
    if (!title) return 'Title is required'
    if (!sport) return 'Sport is required'
    if (!date) return 'Date is required'
    if (!time) return 'Time is required'
    if (!location) return 'Location is required'
    if (!organizer) return 'Organizer is required'
    return null
  }

  function isDateTimeInPast(d, t) {
    try {
      if (!d || !t) return false
      // combine date (YYYY-MM-DD) and time (HH:MM) into local Date
      const [y, m, day] = d.split('-').map(Number)
      const [hh, mm] = t.split(':').map(Number)
      const dt = new Date(y, m - 1, day, hh, mm, 0, 0)
      return dt.getTime() <= Date.now()
    } catch (e) {
      return false
    }
  }

  const showDateError = date && time && isDateTimeInPast(date, time)

  useEffect(() => {
    const uid = localStorage.getItem('userId')
    if (!uid) return
    ;(async () => {
      try {
        const res = await getProfile(uid)
        if (res.ok && res.data) {
          setOrganizer(res.data.username || res.data.user?.username || organizer)
          setOrganizerReadOnly(true)
        }
      } catch (e) {
        // ignore
      }
    })()
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setMessage('')
    setError('')
    const v = validate()
    if (v) { setError(v); return }
    if (isDateTimeInPast(date, time)) { setError('Cannot create an event in the past'); return }
    setSaving(true)
    const payload = {
      title,
      sport,
      date, // ISO yyyy-mm-dd
      time, // HH:mm
      location,
      organizer,
      capacity: Number(capacity),
      price: Number(price),
  // auto-assign image from local static images
  image: sportToImage(sport),
      description: description || null,
    }

    try {
      const res = await createEvent(payload)
      if (res.ok) {
        setMessage('Evento creado')
        setError('')
        // call optional callback to switch view or refresh list
        onCreated(res.data)
        // try to join the creator to the event on the backend, then persist per-user joined list
        try {
          if (res.data && res.data.id) {
            // call backend join endpoint
            try {
              const jr = await joinEvent(res.data.id)
              if (jr && jr.ok) {
                // mark creator as joined for this event in per-user local storage so they see it in "Tus eventos"
                try {
                  const uid = typeof window !== 'undefined' ? localStorage.getItem('userId') : null
                  const key = `joinedEvents:${uid || 'anon'}`
                  const raw = localStorage.getItem(key)
                  const arr = raw ? JSON.parse(raw) : []
                  if (!arr.includes(String(res.data.id))) {
                    arr.push(String(res.data.id))
                    localStorage.setItem(key, JSON.stringify(arr))
                  }
                } catch (e) {
                  // ignore storage failures
                }
              } else {
                // join failed; ignore for now but leave event created
              }
            } catch (e) {
              // network / join error - ignore to avoid blocking create flow
            }
          }
        } catch (e) {
          // ignore
        }
  // reset form
        setTitle('')
        setDate('')
        setTime('18:00')
        setLocation('')
        setCapacity(10)
        setPrice(0)
        setDescription('')
      } else {
        setError(res.data?.message || `Error (${res.status})`)
      }
    } catch (err) {
      setError('Network error')
    } finally {
      setSaving(false)
      setTimeout(() => setMessage(''), 4000)
    }
  }

  return (
    <div className="profile-root">
      <div className="profile-card">
        <h2 className="profile-title">Crear evento</h2>
        {message && <div className="banner success">{message}</div>}
        {error && <div className="banner error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="create-event-grid">
            <div>
              <div className="create-event-image">
                <img src={sportToImage(sport)} alt={sport} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              </div>
            </div>
            <div className="create-event-right">
              <div>
                <label className="label">Título</label>
                <input className="profile-input" value={title} onChange={e => setTitle(e.target.value)} placeholder="Nombre del evento" />
              </div>

              <div style={{ display: 'flex', gap: 8 }}>
                <div style={{ flex: 1 }}>
                  <label className="label">Deporte</label>
                  <select className="level-select" value={sport} onChange={e => setSport(e.target.value)}>
                    {SPORTS.map(s => <option key={s.id} value={s.id}>{s.label}</option>)}
                  </select>
                </div>
                <div style={{ width: 160 }}>
                  <label className="label">Capacidad</label>
                  <input className="profile-input" type="number" min={2} value={capacity} onChange={e => setCapacity(e.target.value)} />
                </div>
              </div>

              <div style={{ display: 'flex', gap: 8 }}>
                <div style={{ flex: 1 }}>
                  <label className="label">Fecha</label>
                  <input className="profile-input" type="date" value={date} onChange={e => setDate(e.target.value)} />
                </div>
                <div style={{ width: 140 }}>
                  <label className="label">Hora</label>
                  <input className="profile-input" type="time" value={time} onChange={e => setTime(e.target.value)} />
                </div>
              </div>
              {/* always render the error element but toggle visibility to reserve space and avoid layout shifts */}
              <div className="field-error" style={{ visibility: showDateError ? 'visible' : 'hidden' }}>
                The selected date/time is in the past. Choose a future date/time.
              </div>

              <div>
                <label className="label">Ubicación</label>
                <input className="profile-input" value={location} onChange={e => setLocation(e.target.value)} placeholder="Lugar (ej. Parque Central)" />
              </div>

              <div>
                <label className="label">Organizador</label>
                <input className="profile-input" value={organizer} onChange={e => setOrganizer(e.target.value)} placeholder="Tu nombre" readOnly={organizerReadOnly} />
              </div>

              <div>
                <label className="label">Precio (€)</label>
                <input className="profile-input" type="number" min={0} step="0.5" value={price} onChange={e => setPrice(e.target.value)} />
              </div>

              <div>
                <label className="label">Descripción</label>
                <textarea className="profile-input" rows={4} value={description} onChange={e => setDescription(e.target.value)} placeholder="Detalles del evento" />
              </div>

              <div className="actions" style={{ marginTop: 12 }}>
                <button className="btn-primary" type="submit" disabled={saving || isDateTimeInPast(date, time)}>{saving ? 'Creando…' : 'Crear evento'}</button>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
