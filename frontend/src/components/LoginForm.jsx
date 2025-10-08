import React from 'react'

export default function LoginForm({ username, password, errors, onUsernameChange, onPasswordChange, onSubmit, submitting }) {
  return (
    <form onSubmit={onSubmit} noValidate>
      <label className="label">
        <span className="label-text">Username</span>
        <input
          type="text"
          value={username}
          onChange={(e) => onUsernameChange(e.target.value)}
          className={`input ${errors.username ? 'input-error' : ''}`}
          placeholder="Enter your username"
          aria-invalid={errors.username ? 'true' : 'false'}
        />
        {errors.username && <div className="error">{errors.username}</div>}
      </label>

      <label className="label">
        <span className="label-text">Password</span>
        <input
          type="password"
          value={password}
          onChange={(e) => onPasswordChange(e.target.value)}
          className={`input ${errors.password ? 'input-error' : ''}`}
          placeholder="Enter your password"
          aria-invalid={errors.password ? 'true' : 'false'}
        />
        {errors.password && <div className="error">{errors.password}</div>}
      </label>

      <button className="btn" type="submit" disabled={submitting}>
        {submitting ? 'Signing in…' : 'Sign in'}
      </button>
    </form>
  )
}
