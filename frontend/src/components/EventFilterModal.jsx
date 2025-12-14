import React, { useMemo, useState } from 'react'

// Map of sport IDs to Spanish labels
export const SPORT_LABELS = {
  football: 'Fútbol',
  basketball: 'Básquet',
  tennis: 'Tenis',
  running: 'Running',
  swimming: 'Natación',
  volleyball: 'Volleyball',
  futbol: 'Fútbol',
  basquet: 'Básquet',
  tenis: 'Tenis',
  natacion: 'Natación',
}

// Function to translate English sport names to Spanish
export function translateSport(sportName) {
  if (!sportName) return sportName
  const normalized = sportName.toLowerCase().trim()
  return SPORT_LABELS[normalized] || sportName
}

const SPORTS = [
  { id: 'football', label: 'Fútbol' },
  { id: 'basketball', label: 'Básquet' },
  { id: 'tennis', label: 'Tenis' },
  { id: 'running', label: 'Running' },
  { id: 'swimming', label: 'Natación' },
  { id: 'volleyball', label: 'Volleyball' },
]

const DAYS = [
  { id: 'mon', label: 'L' },
  { id: 'tue', label: 'M' },
  { id: 'wed', label: 'X' },
  { id: 'thu', label: 'J' },
  { id: 'fri', label: 'V' },
  { id: 'sat', label: 'S' },
  { id: 'sun', label: 'D' },
]

export default function EventFilterModal({ open, initialFilters, onClose, onApply, onClear }) {
  const [sports, setSports] = useState(() => new Set(initialFilters?.sports || []))
  const [location, setLocation] = useState(initialFilters?.location || '')
  const [days, setDays] = useState(() => new Set(initialFilters?.days || []))
  const [timeFrom, setTimeFrom] = useState(initialFilters?.timeFrom || '')
  const [timeTo, setTimeTo] = useState(initialFilters?.timeTo || '')

  const filters = useMemo(() => ({
    sports: Array.from(sports),
    location,
    days: Array.from(days),
    timeFrom,
    timeTo,
  }), [sports, location, days, timeFrom, timeTo])

  function toggleSport(s) {
    setSports(prev => {
      const next = new Set(prev)
      next.has(s) ? next.delete(s) : next.add(s)
      return next
    })
  }

  function toggleDay(id) {
    setDays(prev => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })
  }

  if (!open) return null
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h3>Filtrar eventos</h3>
          <button aria-label="Close" className="btn-icon" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <div className="field">
            <div className="label">Deportes</div>
            <div className="chip-row">
              {SPORTS.map(s => (
                <button
                  key={s.id}
                  className={`chip ${sports.has(s.id) ? 'active' : ''}`}
                  onClick={() => toggleSport(s.id)}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>

          <div className="field">
            <div className="label">Ubicación</div>
            <input className="input" placeholder="Buscar por población..." value={location} onChange={(e) => setLocation(e.target.value)} />
          </div>

          <div className="field">
            <div className="label">Días de la semana</div>
            <div className="days">
              {DAYS.map(d => (
                <button key={d.id} className={`day ${days.has(d.id) ? 'active' : ''}`} onClick={() => toggleDay(d.id)}>{d.label}</button>
              ))}
            </div>
          </div>

          <div className="field">
            <div className="label">Rango de horario</div>
            <div className="time-row">
              <div className="time-col">
                <div className="help">Desde</div>
                <input className="input" type="time" value={timeFrom} onChange={e => setTimeFrom(e.target.value)} />
              </div>
              <div className="time-col">
                <div className="help">Hasta</div>
                <input className="input" type="time" value={timeTo} onChange={e => setTimeTo(e.target.value)} />
              </div>
            </div>
          </div>
        </div>

        <div className="modal-actions">
          <button className="btn-ghost" onClick={() => { setSports(new Set()); setLocation(''); setDays(new Set()); setTimeFrom(''); setTimeTo(''); onClear?.() }}>Limpiar filtros</button>
          <button className="btn-primary" onClick={() => onApply?.(filters)}>Guardar</button>
        </div>
      </div>
    </div>
  )
}
