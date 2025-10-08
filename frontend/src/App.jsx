import { useState } from 'react'
import './App.css'
import './components/auth.css'
import LoginForm from './components/LoginForm'
import RegisterForm from './components/RegisterForm'

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

          {mode === 'login' ? (
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
