import React, { useEffect, useState } from 'react'
import './profile.css'
import { getProfile, saveProfile } from '../services/profile'

const SPORTS = [
  { id: 'football', label: 'Fútbol' },
  { id: 'basketball', label: 'Básquet' },
  { id: 'tennis', label: 'Tenis' },
  { id: 'running', label: 'Running' },
  { id: 'swimming', label: 'Natación' },
  { id: 'volleyball', label: 'Volleyball' },
]

const LEVEL_LABEL = {
  beginner: 'Principiante',
  intermediate: 'Intermedio',
  advanced: 'Avanzado',
  expert: 'Experto'
}

function sportLabel(id) {
  return SPORTS.find(s => s.id === id)?.label || id
}

export default function Profile() {
  const [profile, setProfile] = useState([])
  const [username, setUsername] = useState(localStorage.getItem('username') || 'sportsenthusiast')
  const [email, setEmail] = useState(localStorage.getItem('email') || 'athlete@example.com')

  useEffect(() => {
    async function load() {
      const userId = localStorage.getItem('userId')
      if (!userId) {
        // fall back to localStorage 'profile' key if present (demo mode)
        try {
          const p = JSON.parse(localStorage.getItem('profile') || '[]')
          setProfile(p)
        } catch (e) {
          setProfile([])
        }
        return
      }

      try {
        const res = await getProfile(userId)
        if (res.ok && res.data) {
          const d = res.data
          setUsername(d.username || username)
          setEmail(d.email || email)
          // backend stores sports as JSON string in `sports` column
          if (d.sports) {
            try {
              const parsed = JSON.parse(d.sports)
              setProfile(Array.isArray(parsed) ? parsed : [])
            } catch (err) {
              setProfile([])
            }
          }
        }
      } catch (err) {
        // ignore, keep demo/local values
      }
    }
    load()
  }, [])
  // editing state and helpers
  const [editing, setEditing] = useState(false)
  const [savedMessage, setSavedMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  // profile is array of { id, level }
  function addSport(id) {
    if (!id) return
    if (profile.find(p => p.id === id)) return
    setProfile(prev => [...prev, { id, level: 'beginner' }])
  }

  function removeSport(id) {
    setProfile(prev => prev.filter(p => p.id !== id))
  }

  function setLevelFor(id, level) {
    setProfile(prev => prev.map(p => p.id === id ? { ...p, level } : p))
  }

  const availableToAdd = SPORTS.filter(s => !profile.find(p => p.id === s.id))

  async function handleSave() {
    const userId = localStorage.getItem('userId')
    if (userId) {
      const res = await saveProfile(userId, { sports: profile })
      if (res.ok) {
        // refresh display
        setEditing(false)
        setSavedMessage('Guardado exitosamente')
        setErrorMessage('')
        setTimeout(() => setSavedMessage(''), 4000)
        return
      } else {
        setErrorMessage('Error al guardar el perfil')
        setSavedMessage('')
        setTimeout(() => setErrorMessage(''), 6000)
        return
      }
    }
    // fallback: save to localStorage in demo mode
    try {
      localStorage.setItem('profile', JSON.stringify(profile))
      setEditing(false)
      setSavedMessage('Guardado exitosamente (modo demo)')
      setErrorMessage('')
      setTimeout(() => setSavedMessage(''), 4000)
    } catch (e) {
      setErrorMessage('Error al guardar localmente')
      setSavedMessage('')
    }
  }

  return (
    <div className="profile-root">
      <div className="profile-card">
  <h2 className="profile-title">Perfil</h2>
        {savedMessage && <div className="banner success">{savedMessage}</div>}
        {errorMessage && <div className="banner error">{errorMessage}</div>}
        <div className="profile-info">
          <label className="label">Nombre de usuario</label>
          <input className="profile-input" value={username} readOnly />

          <label className="label">Correo electrónico</label>
          <input className="profile-input" value={email} readOnly />
        </div>

        <h3>Deportes y niveles</h3>

        <div className="profile-controls">
          {!editing ? (
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn-primary" onClick={() => setEditing(true)}>Editar deportes</button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn-primary" onClick={handleSave}>Guardar</button>
              <button className="btn-ghost" onClick={() => { setEditing(false); /* reload to discard */ window.location.reload() }}>Cancelar</button>
            </div>
          )}
        </div>

        <div className="sports-list">
          {profile.length === 0 && <div className="muted">Aún no has seleccionado deportes</div>}

          {profile.map(s => (
            <div key={s.id} className="sport-row">
              <div className="sport-name">{sportLabel(s.id)}</div>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                {editing ? (
                  <select className="level-select" value={s.level || 'beginner'} onChange={e => setLevelFor(s.id, e.target.value)}>
                    <option value="beginner">{LEVEL_LABEL.beginner}</option>
                    <option value="intermediate">{LEVEL_LABEL.intermediate}</option>
                    <option value="advanced">{LEVEL_LABEL.advanced}</option>
                    <option value="expert">{LEVEL_LABEL.expert}</option>
                  </select>
                ) : (
                  <div className={`skill-badge level-${s.level || 'beginner'}`}>{LEVEL_LABEL[s.level] || 'Beginner'}</div>
                )}
                {editing && <button className="remove-btn" onClick={() => removeSport(s.id)}>Eliminar</button>}
              </div>
            </div>
          ))}
        </div>

        {editing && (
          <div className="add-sport-row">
            <label className="label">Agregar deporte</label>
            <div className="chip-row">
              {availableToAdd.map(sp => (
                <button key={sp.id} className="chip" onClick={() => addSport(sp.id)}>{sp.label}</button>
              ))}
              {availableToAdd.length === 0 && <div className="muted">No hay más deportes para agregar</div>}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
