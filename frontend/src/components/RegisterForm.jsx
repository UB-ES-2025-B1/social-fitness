import React from 'react'

export default function RegisterForm({ username, email, password, confirm, errors, onChange, onSubmit, submitting }) {
  return (
    <form onSubmit={onSubmit} noValidate>
      <label className="label">
        <span className="label-text">Username</span>
        <input
          type="text"
          value={username}
          onChange={(e) => onChange('username', e.target.value)}
          className={`input ${errors.username ? 'input-error' : ''}`}
          placeholder="Enter your username"
          aria-invalid={errors.username ? 'true' : 'false'}
        />
        {errors.username && <div className="error">{errors.username}</div>}
      </label>

      <label className="label">
        <span className="label-text">Email</span>
        <input
          type="email"
          value={email}
          onChange={(e) => onChange('email', e.target.value)}
          className={`input ${errors.email ? 'input-error' : ''}`}
          placeholder="Enter your email"
          aria-invalid={errors.email ? 'true' : 'false'}
        />
        {errors.email && <div className="error">{errors.email}</div>}
      </label>

      <label className="label">
        <span className="label-text">Password</span>
        <input
          type="password"
          value={password}
          onChange={(e) => onChange('password', e.target.value)}
          className={`input ${errors.password ? 'input-error' : ''}`}
          placeholder="Enter your password"
          aria-invalid={errors.password ? 'true' : 'false'}
        />
        {errors.password && <div className="error">{errors.password}</div>}
      </label>

      <label className="label">
        <span className="label-text">Confirm password</span>
        <input
          type="password"
          value={confirm}
          onChange={(e) => onChange('confirm', e.target.value)}
          className={`input ${errors.confirm ? 'input-error' : ''}`}
          placeholder="Confirm your password"
          aria-invalid={errors.confirm ? 'true' : 'false'}
        />
        {errors.confirm && <div className="error">{errors.confirm}</div>}
      </label>

      <button className="btn" type="submit" disabled={submitting}>
        {submitting ? 'Creating…' : 'Create account'}
      </button>
    </form>
  )
}
