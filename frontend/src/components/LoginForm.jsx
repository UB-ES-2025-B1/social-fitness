import React, { useState } from 'react'

export default function LoginForm({ username, password, errors, onUsernameChange, onPasswordChange, onSubmit, submitting }) {
  const [showPassword, setShowPassword] = useState(false)
  
  return (
    <form onSubmit={onSubmit} noValidate>
      <label className="label">
        <span className="label-text">Nombre de usuario</span>
        <input
          type="text"
          value={username}
          onChange={(e) => onUsernameChange(e.target.value)}
          className={`input ${errors.username ? 'input-error' : ''}`}
          placeholder="Introduce tu nombre de usuario"
          aria-invalid={errors.username ? 'true' : 'false'}
        />
        {errors.username && <div className="error">{errors.username}</div>}
      </label>

      <label className="label">
        <span className="label-text">Contraseña</span>
        <div style={{ position: 'relative' }}>
          <input
            type={showPassword ? "text" : "password"}
            value={password}
            onChange={(e) => onPasswordChange(e.target.value)}
            className={`input ${errors.password ? 'input-error' : ''}`}
            placeholder="Introduce tu contraseña"
            aria-invalid={errors.password ? 'true' : 'false'}
            style={{ paddingRight: '2.5rem' }}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="password-toggle"
            aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
          >
            {showPassword ? '👁️' : '👁️‍🗨️'}
          </button>
        </div>
        {errors.password && <div className="error">{errors.password}</div>}
      </label>

      <button className="btn" type="submit" disabled={submitting}>
        {submitting ? 'Iniciando sesión…' : 'Iniciar sesión'}
      </button>
    </form>
  )
}
