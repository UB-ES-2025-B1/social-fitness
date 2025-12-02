import React from 'react'
import './events.css'

export default function TopBar({ mode = 'explore', onChange = () => {} }) {
  return (
    <div className="topbar">
      <div className="topbar-inner">
        <div className="topbar-title">Social Fitness</div>
        <div className="topbar-tabs" role="tablist" aria-label="Main navigation">
          <button className={`tab ${mode === 'explore' ? 'active' : ''}`} onClick={() => onChange('explore')}>Explorar</button>
          <button className={`tab ${mode === 'create' ? 'active' : ''}`} onClick={() => onChange('create')}>Crear evento</button>
          <button className={`tab ${mode === 'messages' ? 'active' : ''}`} onClick={() => onChange('messages')}>Mensajes</button>
          <button className={`tab ${mode === 'profile' ? 'active' : ''}`} onClick={() => onChange('profile')}>Perfil</button>
        </div>
      </div>
    </div>
  )
}
