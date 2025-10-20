const API_BASE = import.meta.env.VITE_API_BASE || ''

console.log('[API] VITE_API_BASE =', import.meta.env.VITE_API_BASE);
console.log('[API] API_BASE =', API_BASE);


async function request(path, { method = 'GET', body, headers = {}, credentials = 'include' } = {}) {
  const url = API_BASE + path
  console.log(`[API] ${method} ->`, url); 

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
