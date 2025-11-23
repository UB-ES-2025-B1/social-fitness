import { request } from './api'

async function login({ username, password }) {
  const res = await request('/auth/login', { method: 'POST', body: { username, password } })

  if (res.ok && res.data.user) {
    // Guardamos los datos del usuario en la "variable global" (localStorage)
    try {
      localStorage.setItem('userId', res.data.user.id);
      localStorage.setItem('username', res.data.user.username);
    } catch (e) {
      console.error('Error al guardar en localStorage', e);
    }
  }
  return res
}

async function register({ username, email, password }) {
  return request('/auth/register', { method: 'POST', body: { username, email, password } })
}

export { login, register }
