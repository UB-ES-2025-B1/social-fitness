const API_BASE = import.meta.env.VITE_API_BASE || ''

async function request(path, { method = 'GET', body, headers = {}, credentials = 'include' } = {}) {
  const url = API_BASE + path
  const opts = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    credentials,
  }

  if (body !== undefined) opts.body = JSON.stringify(body)

  const res = await fetch(url, opts)
  const text = await res.text()
  let data
  try {
    data = text ? JSON.parse(text) : null
  } catch (err) {
    data = text
  }

  return {
    ok: res.ok,
    status: res.status,
    data,
  }
}

export { request }
