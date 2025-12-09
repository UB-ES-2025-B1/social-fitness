import { useState, useEffect } from 'react'
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
import DirectMessages from './components/DirectMessages'
import UserSearch from './components/UserSearch'
import PrivateChat from './components/PrivateChat'
import Notifications from './components/Notifications'
import * as profileService from './services/profile'
import * as notificationService from './services/notifications'
import * as localNotificationService from './services/localNotifications'

function App() {
  // Dev shortcut: add ?dev=profile or ?dev=explore in the URL to open a view directly during development
  const urlParams = typeof window !== 'undefined' ? new URLSearchParams(window.location.search) : null
  const devParam = urlParams ? urlParams.get('dev') : null
  const startMode = devParam === 'profile' || devParam === 'explore' || devParam === 'create' || devParam === 'messages' || devParam === 'notifications' ? devParam : 'login'
  const [mode, setMode] = useState(startMode) // one of: 'login' | 'register' | 'profile' | 'explore' | 'create' | 'messages' | 'messages-search' | 'messages-chat' | 'notifications'
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [userId, setUserId] = useState(null)
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [selectedChatUser, setSelectedChatUser] = useState(null)
  const [unreadCount, setUnreadCount] = useState(0)

  // Update unread notification count
  useEffect(() => {
    async function fetchUnreadCount() {
      try {
        // Get backend unread count
        const res = await notificationService.getUnreadCount()
        const backendCount = (res.ok && res.data?.count) ? res.data.count : 0
        
        // Get local unread count
        const localCount = localNotificationService.getUnreadLocalCount()
        
        // Set total unread count
        setUnreadCount(backendCount + localCount)
      } catch (err) {
        console.error('Error fetching unread count:', err)
        // On error, at least show local notifications count
        const localCount = localNotificationService.getUnreadLocalCount()
        setUnreadCount(localCount)
      }
    }

    fetchUnreadCount()
    
    // Refresh unread count when local notifications change
    const handleLocalUpdate = () => {
      fetchUnreadCount()
    }
    
    window.addEventListener('localNotificationAdded', handleLocalUpdate)
    window.addEventListener('localNotificationUpdated', handleLocalUpdate)
    
    // Poll for backend notifications every 30 seconds
    const interval = setInterval(fetchUnreadCount, 30000)
    
    return () => {
      window.removeEventListener('localNotificationAdded', handleLocalUpdate)
      window.removeEventListener('localNotificationUpdated', handleLocalUpdate)
      clearInterval(interval)
    }
  }, [])

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
            // Translate common backend error messages to Spanish
            let errorMessage = res.data.message
            if (errorMessage === 'Invalid credentials') {
              errorMessage = 'Usuario o contraseña incorrectos. Por favor, verifica tus datos.'
            }
            setErrors({ general: errorMessage })
          } else {
            setErrors({ general: `Fallo en la solicitud (${res.status})` })
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
        setErrors({ general: 'Error de red — por favor, intenta de nuevo' })
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
          {(mode === 'explore' || mode === 'profile' || mode === 'create' || mode === 'messages' || mode === 'messages-search' || mode === 'messages-chat' || mode === 'notifications') && (
            <TopBar mode={mode === 'messages-search' || mode === 'messages-chat' ? 'messages' : mode} onChange={(m) => { setMode(m); setErrors({}); setMessage('') }} unreadCount={unreadCount} />
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
          ) : mode === 'notifications' ? (
            <Notifications onNavigate={(targetMode) => setMode(targetMode)} />
          ) : mode === 'create' ? (
            <CreateEvent onCreated={(data) => { setMessage('Evento creado'); setMode('explore') }} />
          ) : mode === 'messages' ? (
            <DirectMessages 
              onSelectChat={(user) => {
                setSelectedChatUser(user)
                setMode('messages-chat')
              }}
              onNewChat={() => setMode('messages-search')}
            />
          ) : mode === 'messages-search' ? (
            <UserSearch 
              onSelectUser={(user) => {
                setSelectedChatUser(user)
                setMode('messages-chat')
              }}
              onBack={() => setMode('messages')}
            />
          ) : mode === 'messages-chat' ? (
            <PrivateChat 
              user={selectedChatUser}
              onBack={() => {
                setSelectedChatUser(null)
                setMode('messages')
              }}
            />
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
