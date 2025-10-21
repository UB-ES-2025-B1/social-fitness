import React, { useState } from 'react'
import '../components/profile.css'

const SPORTS = [
  { id: 'football', label: 'Futbol' },
  { id: 'basketball', label: 'Bàsquet' },
  { id: 'tennis', label: 'Tenis' },
  { id: 'swimming', label: 'Natació' },
  { id: 'cycling', label: 'Ciclisme' },
  { id: 'running', label: 'Còrrer' },
  { id: 'gym', label: 'Gimnàs' },
  { id: 'hiking', label: 'Senderisme' },
  { id: 'paddle', label: 'Pàdel surf' },
]

const LEVELS = [
  { id: 'beginner', label: 'Principiant', desc: 'Començo o tinc poca experiència' },
  { id: 'intermediate', label: 'Intermedi', desc: 'Practico regularment' },
  { id: 'advanced', label: 'Avançat', desc: 'Tinc molta experiència' },
  { id: 'expert', label: 'Expert', desc: 'Competeixo o sóc professional' },
]

export default function ProfileConfigurator({ onComplete }) {
  const [step, setStep] = useState(1)
  const [selectedSports, setSelectedSports] = useState([])
  const [levels, setLevels] = useState({})

  function toggleSport(id) {
    setSelectedSports(prev => prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id])
  }

  function setLevel(sportId, levelId) {
    setLevels(prev => ({ ...prev, [sportId]: levelId }))
  }

  function next() {
    if (selectedSports.length === 0) return alert('Selecciona almenys un esport')
    setStep(2)
  }

  function back() { setStep(1) }

  function finish() {
    // Payload expected by the backend: array of selected sports with chosen levels.
    // Example: [{ id: 'football', level: 'intermediate' }, ...]
    const payload = selectedSports.map(id => ({ id, level: levels[id] || null }))
    if (onComplete) onComplete(payload)
  }

  return (
    <div className="profile-root">
      <div className="profile-card">
        {step === 1 ? (
          <>
            <h2>Configura el teu perfil esportiu</h2>
            <p className="muted">Selecciona els teus esports favorits per rebre recomanacions d'esdeveniments</p>
            <div className="sports-grid">
              {SPORTS.map(s => (
                <button key={s.id} className={`sport ${selectedSports.includes(s.id) ? 'active' : ''}`} onClick={() => toggleSport(s.id)}>
                  <div className="sport-label">{s.label}</div>
                </button>
              ))}
            </div>
            <div className="actions">
              <button className="btn-ghost" onClick={() => {}}>Enrere</button>
              <button className="btn-primary" onClick={next}>Següent</button>
            </div>
          </>
        ) : (
          <>
            <h2>Defineix els teus nivells</h2>
            <p className="muted">Selecciona el teu nivell per a cada esport</p>
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
              <button className="btn-ghost" onClick={back}>Enrere</button>
              <button className="btn-primary" onClick={finish}>Completar</button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
