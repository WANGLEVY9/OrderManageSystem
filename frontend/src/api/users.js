import http from './http'

export async function listUsers() {
  const resp = await http.get('/users')
  return resp.data.data
}
