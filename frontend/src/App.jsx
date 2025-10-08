import { useState } from 'react'
import './App.css'

function App() {
  const [mode, setMode] = useState('login') // 'login' | 'register'
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')

  function validateLogin() {
    const errs = {}
    if (!username) errs.username = 'Username is required'
    if (!password) errs.password = 'Password is required'
    else if (password.length < 6) errs.password = 'Password must be at least 6 characters'
    return errs
  }

  function validateRegister() {
    const errs = {}
    if (!username) errs.username = 'Username is required'
    if (!email) errs.email = 'Email is required'
    else if (!/^\S+@\S+\.\S+$/.test(email)) errs.email = 'Enter a valid email'
    if (!password) errs.password = 'Password is required'
    else if (password.length < 6) errs.password = 'Password must be at least 6 characters'
    if (password !== confirm) errs.confirm = 'Passwords do not match'
    return errs
  }

  function handleSubmit(e) {
    e.preventDefault()
    setMessage('')
    const v = mode === 'login' ? validateLogin() : validateRegister()
    setErrors(v)
    if (Object.keys(v).length > 0) return
    setSubmitting(true)
    // Fake async auth
    setTimeout(() => {
      setSubmitting(false)
      setMessage(mode === 'login' ? 'Login successful (demo)' : 'Registered successfully (demo)')
      setUsername('')
      setEmail('')
      setPassword('')
      setConfirm('')
    }, 900)
  }

  return (
    <div className="app-root">
      <main className="login-wrapper">
        <div className="login-card large" role="region" aria-label={`${mode} form`}>
          <h1 className="title">{mode === 'login' ? 'Log In' : 'Register'}</h1>
          <p className="subtitle">{mode === 'login' ? 'Sign in to continue to Social Fitness' : 'Create a new account'}</p>

          <form onSubmit={handleSubmit} noValidate>
            <label className="label">
              <span className="label-text">Username</span>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className={`input ${errors.username ? 'input-error' : ''}`}
                placeholder="Enter your username"
                aria-invalid={errors.username ? 'true' : 'false'}
              />
              {errors.username && <div className="error">{errors.username}</div>}
            </label>

            {mode === 'register' && (
              <label className="label">
                <span className="label-text">Email</span>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={`input ${errors.email ? 'input-error' : ''}`}
                  placeholder="Enter your email"
                  aria-invalid={errors.email ? 'true' : 'false'}
                />
                {errors.email && <div className="error">{errors.email}</div>}
              </label>
            )}

            <label className="label">
              <span className="label-text">Password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={`input ${errors.password ? 'input-error' : ''}`}
                placeholder="Enter your password"
                aria-invalid={errors.password ? 'true' : 'false'}
              />
              {errors.password && <div className="error">{errors.password}</div>}
            </label>

            {mode === 'register' && (
              <label className="label">
                <span className="label-text">Confirm password</span>
                <input
                  type="password"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  className={`input ${errors.confirm ? 'input-error' : ''}`}
                  placeholder="Confirm your password"
                  aria-invalid={errors.confirm ? 'true' : 'false'}
                />
                {errors.confirm && <div className="error">{errors.confirm}</div>}
              </label>
            )}

            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? (mode === 'login' ? 'Signing in…' : 'Creating…') : (mode === 'login' ? 'Sign in' : 'Create account')}
            </button>
          </form>

          {message && <div className="message">{message}</div>}

          <p className="footnote">
            {mode === 'login' ? (
              <>Don’t have an account? <button className="link" onClick={() => { setMode('register'); setMessage(''); setErrors({}); }}>Register</button></>
            ) : (
              <>Already have an account? <button className="link" onClick={() => { setMode('login'); setMessage(''); setErrors({}); }}>Log in</button></>
            )}
          </p>
        </div>
      </main>
    </div>
  )
}

export default App
