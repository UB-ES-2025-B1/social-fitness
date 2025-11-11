import { useState } from 'react'
import './App.css'
import './components/auth.css'
import LoginForm from './components/LoginForm'
import RegisterForm from './components/RegisterForm'
import * as auth from './services/auth'
import ProfileConfigurator from './components/ProfileConfigurator'
import EventExplorer from './components/EventExplorer'
import TopBar from './components/TopBar'
import Profile from './components/Profile'
import CreateEvent from './components/CreateEvent'
import * as profileService from './services/profile'

function App() {
  // Dev shortcut: add ?dev=profile or ?dev=explore in the URL to open a view directly during development
  const urlParams = typeof window !== 'undefined' ? new URLSearchParams(window.location.search) : null
  const devParam = urlParams ? urlParams.get('dev') : null
  const startMode = devParam === 'profile' || devParam === 'explore' || devParam === 'create' ? devParam : 'login'
  const [mode, setMode] = useState(startMode) // one of: 'login' | 'register' | 'profile' | 'explore'
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [userId, setUserId] = useState(null)
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
  // Call auth API (login or register). Returns a normalized { ok, status, data } shape
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
          // The server returns field errors in `errors` or a general message in `message`.
          // The UI shows field errors inline and other errors as a general banner.
          if (res.data && res.data.errors) {
            setErrors(res.data.errors)
          } else if (res.data && res.data.message) {
            setErrors({ general: res.data.message })
          } else {
            setErrors({ general: `Request failed (${res.status})` })
          }
        } else {
          if (mode === 'register') {
            // Registration succeeded: persist username/email (for demo UI) and open the profile configurator
            try {
              localStorage.setItem('username', username);
              localStorage.setItem('email', email);
              if (res.data && res.data.user && res.data.user.id) {
                localStorage.setItem('userId', String(res.data.user.id))
                setUserId(String(res.data.user.id))
              }
            } catch (e) {}
            setMode('profile-config')
          } else {
            setMessage('Login successful')
            try {
              localStorage.setItem('username', username)
              if (res.data && res.data.user && res.data.user.id) {
                localStorage.setItem('userId', String(res.data.user.id))
                setUserId(String(res.data.user.id))
              }
            } catch (e) {}
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
          {(mode === 'login' || mode === 'register') && (
            <>
              <h1 className="title">{mode === 'login' ? 'Iniciar sesión' : 'Crear cuenta'}</h1>
              <p className="subtitle">{mode === 'login' ? 'Inicia sesión para continuar en Social Fitness' : 'Crea una nueva cuenta'}</p>
            </>
          )}
          {(mode === 'explore' || mode === 'profile' || mode === 'create') && (
            <TopBar mode={mode} onChange={(m) => { setMode(m); setErrors({}); setMessage('') }} />
          )}

          {mode === 'profile-config' ? (
            <ProfileConfigurator onComplete={async (payload) => {
              // Save preferences and go to Explore; minimal error handling here
              const id = userId || localStorage.getItem('userId')
              if (!id) {
                setErrors({ general: 'User not identified; cannot save profile' })
                return
              }
              const res = await profileService.saveProfile(id, { sports: payload })
              if (res.ok) {
                setMessage('Profile saved successfully')
                setMode('explore')
              } else {
                setErrors({ general: 'Failed to save profile' })
              }
            }} />
          ) : mode === 'profile' ? (
            <Profile />
          ) : mode === 'create' ? (
            <CreateEvent onCreated={(data) => { setMessage('Evento creado'); setMode('explore') }} />
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
          {mode !== 'explore' && message && <div className="message">{message}</div>}

          {(mode === 'login' || mode === 'register') && (
            <p className="footnote">
              {mode === 'login' ? (
                <>¿No tienes cuenta? <button className="link" onClick={() => { setMode('register'); setMessage(''); setErrors({}); }}>Crear cuenta</button></>
              ) : (
                <>¿Ya tienes una cuenta? <button className="link" onClick={() => { setMode('login'); setMessage(''); setErrors({}); }}>Iniciar sesión</button></>
              )}
            </p>
          )}
          {/* invisible long text to stabilise page width across views */}
          <div className="width-reserver" aria-hidden>
            WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW
          </div>
        </div>
      </main>
    </div>
  )
}

export default App
