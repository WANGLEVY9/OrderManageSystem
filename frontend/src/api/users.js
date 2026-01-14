import http from './http'

export async function listUsers() {
  const resp = await http.get('/users')
  return resp.data.data
}

export async function updateUser(id, payload) {
  const resp = await http.put(`/users/${id}`, payload)
  return resp.data.data
}

export async function listRoles() {
  const resp = await http.get('/users/roles')
  return resp.data.data
}
