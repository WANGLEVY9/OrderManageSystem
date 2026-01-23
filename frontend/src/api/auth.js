import http from './http'


export async function loginApi(username, password) {
  const resp = await http.post('/auth/login', JSON.stringify({ username, password }))
  return resp.data?.data
}


export async function registerApi(username, password) {
  const resp = await http.post('/auth/register', JSON.stringify({ username, password }))
  return resp.data?.data
}
