import React, { useState } from 'react'
import '../components/profile.css'

const SPORTS = [
  { id: 'football', label: 'Fútbol' },
  { id: 'basketball', label: 'Baloncesto' },
  { id: 'tennis', label: 'Tenis' },
  { id: 'swimming', label: 'Natación' },
  { id: 'cycling', label: 'Ciclismo' },
  { id: 'running', label: 'Correr' },
  { id: 'gym', label: 'Gimnasio' },
  { id: 'hiking', label: 'Senderismo' },
  { id: 'paddle', label: 'Pádel' },
]

const LEVELS = [
  { id: 'beginner', label: 'Principiante', desc: 'Empiezo o tengo poca experiencia' },
  { id: 'intermediate', label: 'Intermedio', desc: 'Practico regularmente' },
  { id: 'advanced', label: 'Avanzado', desc: 'Tengo mucha experiencia' },
  { id: 'expert', label: 'Experto', desc: 'Compito o soy profesional' },
]

export default function ProfileConfigurator({ onComplete }) {
  const [step, setStep] = useState(1)
  const [selectedSports, setSelectedSports] = useState([])
  const [levels, setLevels] = useState({})
  const [error, setError] = useState('')

  function toggleSport(id) {
    setSelectedSports(prev => prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id])
  }

  function setLevel(sportId, levelId) {
    setLevels(prev => ({ ...prev, [sportId]: levelId }))
  }

  function next() {
    if (selectedSports.length === 0) {
      setError('Selecciona al menos un deporte')
      setTimeout(() => setError(''), 4000)
      return
    }
    setError('')
    setStep(2)
  }

  function back() { setStep(1) }

  function finish() {
    // Payload expected by the backend: array of selected sports with chosen levels.
    // Example: [{ id: 'football', level: 'intermediate' }, ...]
    const payload = selectedSports.map(id => ({ id, level: levels[id] || null }))
    // Persist locally so the profile view can display saved preferences in the demo
    try {
      localStorage.setItem('profile', JSON.stringify(payload))
    } catch (e) {
      // ignore storage errors
    }
    if (onComplete) onComplete(payload)
  }

  return (
    <div className="profile-root">
      <div className="profile-card">
        {step === 1 ? (
          <>
            <h2>Configura tu perfil deportivo</h2>
            {error && <div className="banner error">{error}</div>}
            <p className="muted">Selecciona tus deportes favoritos para recibir recomendaciones de eventos</p>
            <div className="sports-grid">
              {SPORTS.map(s => (
                <button key={s.id} className={`sport ${selectedSports.includes(s.id) ? 'active' : ''}`} onClick={() => toggleSport(s.id)}>
                  <div className="sport-label">{s.label}</div>
                </button>
              ))}
            </div>
            <div className="actions">
              <button className="btn-ghost" onClick={() => {}}>Atrás</button>
              <button className="btn-primary" onClick={next}>Siguiente</button>
            </div>
          </>
        ) : (
          <>
            <h2>Define tus niveles</h2>
            <p className="muted">Selecciona tu nivel para cada deporte</p>
            <div className="levels">
              {selectedSports.map(sid => (
                <section key={sid} className="sport-level">
                  <h3>{SPORTS.find(x => x.id === sid)?.label}</h3>
                  <div className="level-grid">
                    {LEVELS.map(l => (
                      <button key={l.id} className={`level ${levels[sid] === l.id ? 'active' : ''}`} onClick={() => setLevel(sid, l.id)}>
                        <div className="level-title">{l.label}</div>
                        <div className="level-desc">{l.desc}</div>
                      </button>
                    ))}
                  </div>
                </section>
              ))}
            </div>
            <div className="actions">
              <button className="btn-ghost" onClick={back}>Atrás</button>
              <button className="btn-primary" onClick={finish}>Finalizar</button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
