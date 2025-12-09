import React, { useState } from 'react'
import './userSearch.css'
import { searchUsers } from '../services/directMessages'

const DEFAULT_AVATAR = '/img/user-profile-icon-profile.png'

export default function UserSearch({ onSelectUser, onBack }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [hasSearched, setHasSearched] = useState(false)

  async function handleSearch(e) {
    e.preventDefault()
    
    if (!query.trim()) {
      setError('Ingresa un nombre de usuario')
      return
    }

    setLoading(true)
    setError(null)
    setHasSearched(true)

    try {
      const res = await searchUsers(query.trim())
      if (res.ok && res.data) {
        setResults(res.data)
      } else {
        setError('Error al buscar usuarios')
      }
    } catch (err) {
      setError('Error al buscar usuarios')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  function handleSelectUser(user) {
    onSelectUser(user)
  }

  return (
    <div className="user-search-container">
      <div className="user-search-header">
        <button className="btn-back" onClick={onBack}>
          ← Volver
        </button>
        <h2>Nuevo mensaje</h2>
      </div>

      <form className="user-search-form" onSubmit={handleSearch}>
        <input
          type="text"
          className="user-search-input"
          placeholder="Buscar por nombre de usuario..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          autoFocus
        />
        <button type="submit" className="btn-search" disabled={loading}>
          {loading ? 'Buscando...' : 'Buscar'}
        </button>
      </form>

      {error && <div className="user-search-error">{error}</div>}

      {hasSearched && !loading && !error && results.length === 0 && (
        <div className="user-search-empty">
          <p>No se encontraron usuarios con "{query}"</p>
        </div>
      )}

      {results.length > 0 && (
        <div className="user-search-results">
          <div className="user-search-results-header">
            Resultados ({results.length})
          </div>
          <div className="user-search-list">
            {results.map((user) => (
              <div
                key={user.id}
                className="user-search-item"
                onClick={() => handleSelectUser(user)}
              >
                <img
                  src={user.profileImage || DEFAULT_AVATAR}
                  alt={user.username}
                  className="user-search-avatar"
                />
                <span className="user-search-username">{user.username}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
