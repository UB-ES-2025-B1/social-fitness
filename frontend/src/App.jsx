import { useState } from 'react'
import './App.css'
import './components/auth.css'
import LoginForm from './components/LoginForm'
import RegisterForm from './components/RegisterForm'
import * as auth from './services/auth'
import ProfileConfigurator from './components/ProfileConfigurator'
import EventExplorer from './components/EventExplorer'
import * as profileService from './services/profile'

function App() {
  // Dev helper: add ?dev=profile or ?dev=explore to the URL to jump straight to a view while developing
  const urlParams = typeof window !== 'undefined' ? new URLSearchParams(window.location.search) : null
  const devParam = urlParams ? urlParams.get('dev') : null
  const startMode = devParam === 'profile' || devParam === 'explore' ? devParam : 'login'
  const [mode, setMode] = useState(startMode) // 'login' | 'register' | 'profile' | 'explore'
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
    console.log('handleSubmit', { mode, username, email })
    setMessage('')
    const v = mode === 'login' ? validateLogin() : validateRegister()
    setErrors(v)
    if (Object.keys(v).length > 0) return
    setSubmitting(true)
  // Call the backend auth API (login or register) — this returns a normalized { ok, status, data } result
    ;(async () => {
      try {
        const payload = mode === 'login'
          ? { username, password }
          : { username, email, password }

        const res = mode === 'login'
          ? await auth.login(payload)
          : await auth.register(payload)

  console.log('auth response', res)
  if (!res.ok) {
          // We expect the server to return field-level errors in `errors` or a general message in `message`.
          // That lets the UI show inline feedback for fields or a general banner for other errors.
          if (res.data && res.data.errors) {
            setErrors(res.data.errors)
          } else if (res.data && res.data.message) {
            setErrors({ general: res.data.message })
          } else {
            setErrors({ general: `Request failed (${res.status})` })
          }
        } else {
          if (mode === 'register') {
            // Registration succeeded — show the profile configurator so the user can pick sports/levels
            setMode('profile')
          } else {
            setMessage('Login successful')
            setMode('explore')
          }
          setUsername('')
          setEmail('')
          setPassword('')
          setConfirm('')
        }
      } catch (err) {
        setErrors({ general: 'Network error — please try again' })
      } finally {
        setSubmitting(false)
      }
    })()
  }

  return (
    <div className="app-root">
      <main className="login-wrapper">
        <div className="login-card large" role="region" aria-label={`${mode} form`}>
          {mode !== 'explore' && (
            <>
              <h1 className="title">{mode === 'login' ? 'Log In' : 'Register'}</h1>
              <p className="subtitle">{mode === 'login' ? 'Sign in to continue to Social Fitness' : 'Create a new account'}</p>
            </>
          )}
          {mode === 'profile' ? (
            <ProfileConfigurator onComplete={async (payload) => {
              // In a full app we'd show a loading indicator and handle errors more thoroughly here
              const res = await profileService.saveProfile({ sports: payload })
              if (res.ok) {
                setMessage('Profile saved successfully')
                setMode('explore')
              } else {
                setErrors({ general: 'Failed to save profile' })
              }
            }} />
          ) : mode === 'explore' ? (
            <EventExplorer />
          ) : mode === 'login' ? (
            <LoginForm
              username={username}
              password={password}
              errors={errors}
              onUsernameChange={setUsername}
              onPasswordChange={setPassword}
              onSubmit={handleSubmit}
              submitting={submitting}
            />
          ) : (
            <RegisterForm
              username={username}
              email={email}
              password={password}
              confirm={confirm}
              errors={errors}
              onChange={(field, value) => {
                if (field === 'username') setUsername(value)
                if (field === 'email') setEmail(value)
                if (field === 'password') setPassword(value)
                if (field === 'confirm') setConfirm(value)
              }}
              onSubmit={handleSubmit}
              submitting={submitting}
            />
          )}

          {errors.general && <div className="general-error">{errors.general}</div>}
          {message && <div className="message">{message}</div>}

          {mode !== 'explore' && (
            <p className="footnote">
              {mode === 'login' ? (
                <>Don’t have an account? <button className="link" onClick={() => { setMode('register'); setMessage(''); setErrors({}); }}>Register</button></>
              ) : (
                <>Already have an account? <button className="link" onClick={() => { setMode('login'); setMessage(''); setErrors({}); }}>Log in</button></>
              )}
            </p>
          )}
        </div>
      </main>
    </div>
  )
}

export default App
