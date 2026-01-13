import http from './http'

export async function loginApi(username, password) {
  const resp = await http.post('/auth/login', { username, password })
  return resp.data.data
}

export async function registerApi(username, password) {
  const resp = await http.post('/auth/register', { username, password })
  return resp.data.data
}
