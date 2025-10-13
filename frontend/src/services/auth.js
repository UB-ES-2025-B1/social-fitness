import { request } from './api'

async function login({ username, password }) {
  return request('/auth/login', { method: 'POST', body: { username, password } })
}

async function register({ username, email, password }) {
  return request('/auth/register', { method: 'POST', body: { username, email, password } })
}

export { login, register }
